use jni::{
    descriptors::Desc,
    objects::{JClass, JObject, JValue},
    strings::JNIString,
    sys::{jbyte, jchar, jdouble, jfloat, jint, jlong, jshort},
    JNIEnv, JavaVM,
};

use paste::paste;

macro_rules! get_field_trait {
    ($type_name:ident, $return_ty:ty) => {
        paste! {
            unsafe fn [< get_field_ $type_name _unchecked >]<O, S>(&self, obj: O, name: S) -> $return_ty
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString>;

            fn [< get_field_ $type_name >]<O, S>(&self, obj: O, name: S) -> $return_ty
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString>;
        }
    };
}

macro_rules! get_field_trait_impl {
    ($type_name:ident, $return_ty:ty, $descriptor:expr, $fn_name:ident) => {
        paste! {
            #[inline]
            unsafe fn [< get_field_ $type_name _unchecked >]<O, S>(&self, obj: O, name: S) -> $return_ty
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString>
            {
                self.get_field(obj, name, $descriptor)
                    .unwrap_unchecked()
                    .$fn_name()
                    .unwrap_unchecked()
            }

            #[inline]
            fn [< get_field_ $type_name >]<O, S>(&self, obj: O, name: S) -> $return_ty
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString>
            {
                self.get_field(obj, name, $descriptor)
                    .unwrap()
                    .$fn_name()
                    .unwrap()
            }
        }
    };
}

macro_rules! set_field_trait {
    ($type_name:ident, $input_ty:ty) => {
        paste! {
            fn [< set_field_ $type_name >] <O, S>(&self, obj: O, name: S, val: $input_ty)
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString> + AsRef<str>;
        }
    };
}

macro_rules! set_field_trait_impl {
    ($type_name:ident, $input_ty:ty, $descriptor:expr) => {
        paste! {
            #[inline]
            fn [< set_field_ $type_name >] <O, S>(&self, obj: O, name: S, val: $input_ty)
            where
                O: Into<JObject<'a>>,
                S: Into<JNIString> + AsRef<str>,
            {
                if let Err(err) = self.set_field(obj, name.as_ref(), $descriptor, val.into()) {
                    log::error!("Failed to set field `{:?}`: {:#?}", name.as_ref(), err);
                }
            }
        }
    };
}

pub trait JNIEnvExt<'a> {
    fn throw_new_or_eprint<'c, T>(&self, class: T, msg: &str)
    where
        T: Desc<'a, JClass<'c>>;

    get_field_trait!(jbool, bool);
    get_field_trait!(jbyte, jbyte);
    get_field_trait!(short, jshort);
    get_field_trait!(jint, jint);
    get_field_trait!(jlong, jlong);
    get_field_trait!(jchar, jchar);
    get_field_trait!(jfloat, jfloat);
    get_field_trait!(jdouble, jdouble);

    set_field_trait!(jbool, bool);
    set_field_trait!(jbyte, jbyte);
    set_field_trait!(short, jshort);
    set_field_trait!(jint, jint);
    set_field_trait!(jlong, jlong);
    set_field_trait!(jchar, jchar);
    set_field_trait!(jfloat, jfloat);
    set_field_trait!(jdouble, jdouble);

    fn get_field_jobject<O, S, T>(&self, obj: O, name: S, ty: T) -> JObject
    where
        O: Into<JObject<'a>>,
        S: Into<JNIString>,
        T: Into<JNIString> + AsRef<str>;

    fn call_method_with_err_handle<O, S, T>(&self, obj: O, name: S, sig: T, args: &[JValue])
    where
        O: Into<JObject<'a>>,
        S: Into<JNIString> + AsRef<str>,
        T: Into<JNIString> + AsRef<str>;
}

impl<'a> JNIEnvExt<'a> for JNIEnv<'a> {
    fn throw_new_or_eprint<'c, T>(&self, class: T, msg: &str)
    where
        T: Desc<'a, JClass<'c>>,
    {
        if let Err(err) = self.throw_new(class, msg) {
            log::error!("Faild to throw exception, {}\n  caused by {:?}", msg, err)
        };
    }

    get_field_trait_impl!(jbool, bool, "Z", z);
    get_field_trait_impl!(jbyte, jbyte, "B", b);
    get_field_trait_impl!(short, jshort, "S", s);
    get_field_trait_impl!(jint, jint, "I", i);
    get_field_trait_impl!(jlong, jlong, "J", j);
    get_field_trait_impl!(jchar, jchar, "C", c);
    get_field_trait_impl!(jfloat, jfloat, "F", f);
    get_field_trait_impl!(jdouble, jdouble, "D", d);

    set_field_trait_impl!(jbool, bool, "Z");
    set_field_trait_impl!(jbyte, jbyte, "B");
    set_field_trait_impl!(short, jshort, "S");
    set_field_trait_impl!(jint, jint, "I");
    set_field_trait_impl!(jlong, jlong, "J");
    set_field_trait_impl!(jchar, jchar, "C");
    set_field_trait_impl!(jfloat, jfloat, "F");
    set_field_trait_impl!(jdouble, jdouble, "D");

    #[inline]
    fn get_field_jobject<O, S, T>(&self, obj: O, name: S, ty: T) -> JObject
    where
        O: Into<JObject<'a>>,
        S: Into<JNIString>,
        T: Into<JNIString> + AsRef<str>,
    {
        self.get_field(obj, name, ty).unwrap().l().unwrap()
    }

    fn call_method_with_err_handle<O, S, T>(&self, obj: O, name: S, sig: T, args: &[JValue])
    where
        O: Into<JObject<'a>>,
        S: Into<JNIString> + AsRef<str>,
        T: Into<JNIString> + AsRef<str>,
    {
        if let Err(err) = self.call_method(obj, name.as_ref(), sig, args) {
            if self.exception_check().is_ok() {
                self.exception_describe()
                    .expect("Failed to print exception");
                self.exception_clear().expect("Failed to clear exception");
            }
            log::error!("Call `{:?}` method error: {:#?}", name.as_ref(), err);
        };
    }
}

pub trait JavaVMExt {
    fn attach_or_panic(&self) -> JNIEnv;
}

impl JavaVMExt for JavaVM {
    fn attach_or_panic(&self) -> JNIEnv {
        self.attach_current_thread_permanently()
            .expect("Failed to attach JVM")
    }
}
