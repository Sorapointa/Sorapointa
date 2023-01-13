use std::{net::SocketAddr, str::FromStr, sync::Arc, time::Duration};

use jni::{
    objects::{AutoLocal, JClass, JObject, JString},
    sys::{jbyteArray, jlong},
    JNIEnv,
};
use log::{debug, error};
use tokio::{sync::mpsc, time};
use tokio_kcp::{KcpConfig, KcpListener, KcpNoDelayConfig};

use crate::{
    cls::{ILLEGAL_STATE_EXCEPTION, IO_EXCEPTION},
    ip::SocketAddrExt,
    jnienv::{JNIEnvExt, JavaVMExt},
    rt::global_runtime,
};

pub const NO_DELAY_CONFIG_CLASS: &str = "Lorg/sorapointa/rust/kcp/NoDelayConfig;";
pub const WINDOW_SIZE_CLASS: &str = "Lorg/sorapointa/rust/kcp/WindowSize;";
pub const ATOMIC_BOOLEAN_CLASS: &str = "Ljava/util/concurrent/atomic/AtomicBoolean;";

const NATIVE_PTR_FIELD: &str = "nativePointer";

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_kcp_KcpListener_newNative(
    env: JNIEnv,
    _class: JClass,
    config: JObject,
    socket_addr: JString,
) -> i64 {
    let socket_addr: String = match env.get_string(socket_addr) {
        Ok(ok) => ok,
        Err(err) => {
            env.throw_new_or_eprint(
                ILLEGAL_STATE_EXCEPTION,
                format!("Failed to convert string: {:#?}", err).as_str(),
            );
            return 0;
        }
    }
    .into();
    let socket_addr = match SocketAddr::from_str(socket_addr.as_str()) {
        Ok(ok) => ok,
        Err(err) => {
            env.throw_new_or_eprint(
                ILLEGAL_STATE_EXCEPTION,
                format!("Failed to parse socket addr: {err:?}").as_str(),
            );
            return 0;
        }
    };
    let mtu = env.get_field_jlong(config, "mtu");
    let no_delay_j = env.get_field_jobject(config, "noDelay", NO_DELAY_CONFIG_CLASS);
    let no_delay = get_no_delay(env, no_delay_j);
    let window_size = env.get_field_jobject(config, "windowSize", WINDOW_SIZE_CLASS);
    let wnd_size_1 = env.get_field_jint(window_size, "first");
    let wnd_size_2 = env.get_field_jint(window_size, "second");
    let session_expire = env.get_field_jint(config, "sessionExpire");
    let flush_write = env.get_field_jbool(config, "flushWrite");
    let flush_acks_input = env.get_field_jbool(config, "flushAckInput");
    let stream = env.get_field_jbool(config, "stream");
    let kcp_config = KcpConfig {
        mtu: usize::try_from(mtu).unwrap(),
        nodelay: no_delay,
        wnd_size: (
            u16::try_from(wnd_size_1).unwrap(),
            u16::try_from(wnd_size_2).unwrap(),
        ),
        session_expire: Duration::from_secs(u64::try_from(session_expire).unwrap()),
        flush_write,
        flush_acks_input,
        stream,
    };
    debug!("kcp config: {:?}", kcp_config);
    debug!("server socket addr: {:?}", socket_addr);

    let kcp_listener =
        global_runtime().block_on(async move { KcpListener::bind(kcp_config, socket_addr).await });
    let kcp_listener = match kcp_listener {
        Ok(ok) => ok,
        Err(err) => {
            let msg = format!("Failed to create kcp listener: {err:?}");
            env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, msg.as_str());
            return 0;
        }
    };
    let boxed = Box::new(kcp_listener);
    let ptr = Box::into_raw(boxed);
    ptr as jlong
}

fn get_no_delay(env: JNIEnv, no_delay: JObject) -> KcpNoDelayConfig {
    KcpNoDelayConfig {
        nodelay: env.get_field_jbool(no_delay, "noDelay"),
        interval: env.get_field_jint(no_delay, "interval"),
        resend: env.get_field_jint(no_delay, "resend"),
        nc: env.get_field_jbool(no_delay, "nc"),
    }
}

macro_rules! err_if_closed {
    ($env:ident,$class:ident) => {
        if is_closed($env, $class) {
            $env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, "KcpListener is already closed");
            return;
        }
    };
    ($env:ident,$class:ident,$control:stmt) => {
        if is_closed($env, $class) {
            $env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, "KcpListener is already closed");
            $control
        }
    };
}

fn is_closed(env: JNIEnv, kcp_listener: JClass) -> bool {
    let closed_field = env.get_field_jobject(kcp_listener, "closed", ATOMIC_BOOLEAN_CLASS);
    env.call_method(closed_field, "get", "()Z", &[])
        .unwrap()
        .z()
        .unwrap()
}

fn get_kcp_listener(env: JNIEnv, class: JClass) -> &'static mut KcpListener {
    let ptr = env.get_field_jlong(class, NATIVE_PTR_FIELD);
    unsafe { (ptr as *mut KcpListener).as_mut().unwrap() }
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_kcp_KcpListener_close(env: JNIEnv, class: JClass) {
    let closed = env.get_field_jobject(class, "closed", ATOMIC_BOOLEAN_CLASS);

    let success = env
        .call_method(
            closed,
            "compareAndSet",
            "(ZZ)Z",
            &[false.into(), true.into()],
        )
        .unwrap()
        .z()
        .unwrap();

    if !success {
        env.throw_new_or_eprint(IO_EXCEPTION, "KcpListener is already closed");
        return;
    }

    let ptr = env.get_field_jlong(class, NATIVE_PTR_FIELD);
    let listener = unsafe { (ptr as *mut KcpListener).as_mut().unwrap() };
    let listener = listener;
    debug!("Drop KcpListener {ptr:#0X}");
    unsafe { std::ptr::drop_in_place(listener) }
}

macro_rules! on_ip_addr_event {
    ($func_to_call:expr, $ptr:expr, $class:ident, $jvm:ident,$peer_addr:ident) => {{
        let env = $jvm.attach_or_panic();
        let (ip, port) = $peer_addr.divide_into_ip_and_port();
        let ip_jarr = vec_to_jbyte_array!(env, ip.as_slice(), return);
        env.call_method_with_err_handle(
            $class.as_obj(),
            $func_to_call,
            "(J[BI)V",
            &[$ptr.into(), ip_jarr.as_obj().into(), (port as i32).into()],
        );
    }};
}

macro_rules! jbyte_array_to_vec {
    ($env:ident, $slice:expr, $control:stmt) => {
        match $env.convert_byte_array($slice) {
            Ok(ok) => ok,
            Err(err) => {
                $env.throw_new_or_eprint(
                    ILLEGAL_STATE_EXCEPTION,
                    format!("Failed to convert jbytearray to rust Vec<u8>: {err:#?}").as_str(),
                );
                $control
            }
        }
    };
}

macro_rules! vec_to_jbyte_array {
    ($env:ident, $slice:expr, $control:stmt) => {
        match $env.byte_array_from_slice($slice) {
            Ok(bytes) => AutoLocal::new(&$env, unsafe { JObject::from_raw(bytes) }),
            Err(err) => {
                error!("Failed to convert slice to jbytearray: {err:#?}");
                $control
            }
        }
    };
}

/// return 0 if error
#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_kcp_KcpListener_send0(
    env: JNIEnv,
    class: JClass,
    ptr: jlong,
    bytes: jbyteArray,
) -> i64 {
    err_if_closed!(env, class, return 0);
    let tx = unsafe { Box::from_raw(ptr as *mut mpsc::Sender<Vec<u8>>) };
    let data = jbyte_array_to_vec!(env, bytes, return 0);
    let handle = global_runtime().spawn(async move {
        let result = tx.send(data).await;
        std::mem::forget(tx);
        if let Err(err) = result {
            error!("Failed to send data: {err:?}");
        };
    });
    let handle = Box::into_raw(Box::new(handle));
    handle as jlong
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_kcp_KcpListener_mainLoop0(
    env: JNIEnv,
    class: JClass,
) -> jlong {
    err_if_closed!(env, class, return 0);
    let jvm = match env.get_java_vm() {
        Ok(jvm) => jvm,
        Err(err) => {
            env.throw_new_or_eprint(
                ILLEGAL_STATE_EXCEPTION,
                format!("Failed to create Java VM: {:?}", err).as_str(),
            );
            return 0;
        }
    };

    let jvm = Arc::new(jvm);
    let kcp = get_kcp_listener(env, class);
    let class = Arc::new(env.new_global_ref(class).unwrap());

    let handle = global_runtime().spawn(async move {
        loop {
            let (mut stream, peer_addr) = match kcp.accept().await {
                Ok(s) => s,
                Err(err) => {
                    error!("accept failed, error: {}", err);
                    break;
                }
            };

            let (tx, mut rx) = mpsc::channel::<Vec<u8>>(32);

            let boxed_tx = Box::new(tx);
            let ptr = Box::into_raw(boxed_tx) as jlong;

            // onPeerAccept
            on_ip_addr_event!("onPeerAccept0", ptr, class, jvm, peer_addr);
            let jvm = Arc::clone(&jvm);
            let class = Arc::clone(&class);

            let job = async move {
                let mut buffer = [0u8; 8192];

                while let Ok(n) = stream.recv(&mut buffer).await {
                    if n == 0 {
                        break;
                    }

                    // onRecv
                    {
                        let env = jvm.attach_or_panic();

                        let (ip, port) = peer_addr.divide_into_ip_and_port();
                        let ip_jarr = vec_to_jbyte_array!(env, ip.as_slice(), return);
                        let bytes = vec_to_jbyte_array!(env, &buffer[..n], continue);

                        env.call_method_with_err_handle(
                            class.as_obj(),
                            "onRecv0",
                            "([BI[B)V",
                            &[
                                ip_jarr.as_obj().into(),
                                (port as i32).into(),
                                bytes.as_obj().into(),
                            ],
                        );
                    }

                    time::sleep(Duration::from_millis(1)).await;

                    loop {
                        let try_recv = rx.try_recv();
                        match try_recv {
                            Ok(to_send) => {
                                if let Err(err) = stream.send(to_send.as_slice()).await {
                                    error!("Failed to send data: {err:#?}");
                                };
                            }
                            Err(_) => break,
                        }
                    }
                }
                // onPeerClose
                on_ip_addr_event!("onPeerClose0", ptr, class, jvm, peer_addr);

                unsafe { std::ptr::drop_in_place(ptr as *mut mpsc::Sender<Vec<u8>>) }
            };
            tokio::spawn(job);
        }
    });
    let handle = Box::into_raw(Box::new(handle));
    handle as jlong
}
