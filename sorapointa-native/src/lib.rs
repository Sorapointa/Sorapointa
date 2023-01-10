use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_org_sorapointa_rust_Native_rustNative(_env: JNIEnv) {
    println!("Heya from Rust!");
}
