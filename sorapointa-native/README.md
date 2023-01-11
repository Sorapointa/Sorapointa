# Native Module

[简体中文](README.zh-CN.md)

This module provide Rust native library.

## Build

To build this module, Rust toolchains are required,
it's recommended to install Rust toolchains via [Rustup](https://rustup.rs/).
If you are running on Unix-like platform, you can follow the on-screen instructions:

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

If you are running on Windows, 
or you want to install Rust toolchains via package managers,
you can see:

- [Install Rust - rust-lang.org](https://www.rust-lang.org/tools/install)
- [Other Rust Installation Methods](https://forge.rust-lang.org/infra/other-installation-methods.html)

### Cargo

After installation, you can build and test this module using `cargo`:

- Build：`cargo build`
- Build with Release profile：`cargo build --release`
- Test：`cargo test`

### Gradle Integration

You can also build this module by Gradle tasks
(which use `cargo` in the under hood): 

- `build`
- `test`
- `clean`：Clean build artifact

These tasks are in `rust` group.

## Dependency

Add a dependency for [`sorapointa-native-wrapper`](../sorapointa-native-wrapper/README.md) module on Kotlin-side,
thus you can call this module intermediately via JNI.

## Code Style

You should use [`cargo fmt`](https://github.com/rust-lang/rustfmt) to format code,
and use [`cargo clippy`](https://github.com/rust-lang/rust-clippy) to lint code.

Install:

```bash
rustup component add clippy
rustup component add rustfmt
```
