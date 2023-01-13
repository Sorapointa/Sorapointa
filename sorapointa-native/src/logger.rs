use anyhow::{Context, Result};
use jni::objects::{JClass, JValue};
use jni::sys::jint;
use jni::{JNIEnv, JavaVM};
use log::{Level, LevelFilter, Metadata, Record};
use once_cell::sync::OnceCell;

use crate::cls::ILLEGAL_STATE_EXCEPTION;
use crate::jnienv::JNIEnvExt;

const LOG_CLASS: &str = "org/sorapointa/rust/logging/RustLogger";
const LOG_SIG: &str = "(Ljava/lang/String;)V";

static INSTANCE: OnceCell<Logger> = OnceCell::new();

/// Logger from JVM
struct Logger {
    level: Level,
    jvm: JavaVM,
}

macro_rules! log_impl {
    ($func_name:ident, $method_name:expr) => {
        fn $func_name(&self, message: &str) -> Result<()> {
            let env = self
                .jvm
                .attach_current_thread_permanently()
                .context("Failed to attach current thread")?;
            let jmessage = env
                .new_string(message)
                .context("Failed to allocate String")?;
            env.call_static_method(LOG_CLASS, $method_name, LOG_SIG, &[JValue::from(jmessage)])
                .context("Failed to call logger from native")?;
            Ok(())
        }
    };
}

impl Logger {
    log_impl!(error, "error");
    log_impl!(warn, "warn");
    log_impl!(info, "info");
    log_impl!(debug, "debug");
    log_impl!(trace, "trace");
}

impl log::Log for Logger {
    fn enabled(&self, metadata: &Metadata) -> bool {
        metadata.level() <= self.level
    }

    fn log(&self, record: &Record) {
        if !self.enabled(record.metadata()) {
            return;
        }
        let message = format!("{}", record.args());
        let result = match record.level() {
            Level::Error => self.error(message.as_str()),
            Level::Warn => self.warn(message.as_str()),
            Level::Info => self.info(message.as_str()),
            Level::Debug => self.debug(message.as_str()),
            Level::Trace => self.trace(message.as_str()),
        };
        if let Err(err) = result {
            eprintln!(
                "{}",
                err.context(format!("Failed to log message `{}`", message))
            );
        };
    }

    fn flush(&self) {}
}

/// See Kotlin side for docs
#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_logging_RustLogger_setup(
    env: JNIEnv,
    _class: JClass,
    level: jint,
) {
    let level_filter = match level {
        0 => LevelFilter::Off,
        1 => LevelFilter::Error,
        2 => LevelFilter::Warn,
        3 => LevelFilter::Info,
        4 => LevelFilter::Debug,
        5 => LevelFilter::Trace,
        _ => {
            let msg = format!("Level `{level}` is not valid");
            env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, format!("{:?}", msg).as_str());
            return;
        }
    };
    let jvm = match env.get_java_vm() {
        Ok(ok) => ok,
        Err(err) => {
            env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, format!("{:?}", err).as_str());
            return;
        }
    };
    let result = INSTANCE.set(Logger {
        jvm,
        level: level_filter.to_level().unwrap_or(Level::Info),
    });
    if result.is_err() {
        env.throw_new_or_eprint(ILLEGAL_STATE_EXCEPTION, "Logger has already inited");
        return;
    };
    let log = INSTANCE.get().unwrap();
    log::set_logger(log).unwrap();
    log::set_max_level(level_filter);
}
