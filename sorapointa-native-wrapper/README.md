# Native Wrapper Module

This module is a wrapper for [`sorapointa-native`](../sorapointa-native/README.zh-CN.md) with JNI bindings.

## JNI Header

You can use Gradle task `generateJniHeaders` to generate JNI headers for writing Rust-side native code. The output path
is at `./src/generated/jni`.

## Initialization

To use this module, before any native call, `RustLogger.setup()` should be
invoked exactly once to setup Rust-side logger.

## Build Behaviour

This module only build dynamic library artifact for host os as default.

By default, only the [`sorapointa-native`](../sorapointa-native/README.zh-CN.md) dynamic libraries
for the host system and architecture will be built.

When the `shadowJar` task is runned, the dynamic libraries are packed into `org/sorapointa/rust`.
At runtime, they will be unpacked to a temporary directory and link.
