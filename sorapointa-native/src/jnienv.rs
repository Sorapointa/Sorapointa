use jni::{descriptors::Desc, objects::JClass, JNIEnv};

pub trait JNIEnvExt<'a> {
    fn throw_new_or_eprint<'c, T>(&self, class: T, msg: &str)
    where
        T: Desc<'a, JClass<'c>>;
}

impl<'a> JNIEnvExt<'a> for JNIEnv<'a> {
    fn throw_new_or_eprint<'c, T>(&self, class: T, msg: &str)
    where
        T: Desc<'a, JClass<'c>>,
    {
        if let Err(err) = self.throw_new(class, msg) {
            eprintln!("Faild to throw exception, {}\n  caused by {:?}", msg, err)
        };
    }
}
