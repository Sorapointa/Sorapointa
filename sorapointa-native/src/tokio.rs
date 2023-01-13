use jni::{objects::JClass, JNIEnv};
use log::{debug, error};
use tokio::task::JoinHandle;

use crate::{cls::ILLEGAL_ARGUMENT_EXCEPTION, jnienv::JNIEnvExt, rt::global_runtime};

const PTR_FIELD: &str = "nativePtr";

macro_rules! get_ptr_field {
    ($env:expr, $class:expr) => {{
        let ptr = $env.get_field_jlong($class, PTR_FIELD);
        if ptr == 0 {
            $env.throw_new_or_eprint(ILLEGAL_ARGUMENT_EXCEPTION, "Pointer is null (0)");
            return;
        } else {
            ptr
        }
    }};
    ($env:expr, $class:expr, $control:stmt) => {{
        let ptr = $env.get_field_jlong($class, PTR_FIELD);
        if ptr == 0 {
            $env.throw_new_or_eprint(ILLEGAL_ARGUMENT_EXCEPTION, "Pointer is null (0)");
            $control
        } else {
            ptr
        }
    }};
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_tokio_TokioHandle_isFinished(
    env: JNIEnv,
    class: JClass,
) -> bool {
    let ptr = get_ptr_field!(env, class, return false);
    let handle = unsafe { Box::from_raw(ptr as *mut JoinHandle<()>) };
    let is_finished = handle.is_finished();
    std::mem::forget(handle);
    is_finished
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_tokio_TokioHandle_abort(
    env: JNIEnv,
    class: JClass,
) {
    let ptr = get_ptr_field!(env, class, return);
    let handle = unsafe { Box::from_raw(ptr as *mut JoinHandle<()>) };
    handle.abort();
    std::mem::forget(handle);
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_tokio_TokioHandle_close(
    env: JNIEnv,
    class: JClass,
) {
    let ptr = get_ptr_field!(env, class);
    let handle = unsafe { Box::from_raw(ptr as *mut JoinHandle<()>) };
    env.set_field_jlong(class, PTR_FIELD, 0);
    drop(handle);
}

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_tokio_TokioHandle_await(
    env: JNIEnv,
    class: JClass,
) {
    let ptr = get_ptr_field!(env, class);
    let handle = unsafe { Box::from_raw(ptr as *mut JoinHandle<()>) };
    global_runtime().block_on(async move {
        if let Err(err) = handle.await {
            if err.is_cancelled() {
                debug!("TokioHandle cancelled: {err:?}");
                return;
            }
            error!("Failed to join handle: {:?}", err)
        };
    });
}
