# Native 模块

[English](README.md)

本模块提供 Rust 原生库。

## 构建

构建本模块需要 Rust 工具链。推荐通过 [Rustup](https://rustup.rs/) 安装，Unix-like 平台可通过以下命令安装：

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

如果你使用非 Unix-like 系统，或想要使用包管理器安装，可参考以下链接：

- [安装 Rust - rust-lang.org](https://www.rust-lang.org/zh-CN/tools/install)
- [其他安装方法 - Rust Forge（英）](https://forge.rust-lang.org/infra/other-installation-methods.html)

### Cargo

安装 Rust 工具链后，可以使用 `cargo` 命令构建并测试本模块。

- 构建：`cargo build`
- Release 模式构建：`cargo build --release`
- 测试：`cargo test`

### Gradle 集成

此外，亦支持通过 Gradle 任务调用 Cargo：

- `build`：构建
- `test`：测试
- `clean`：清理构建输出

以上任务均在 `rust` 分组下。

## 使用

Kotlin 侧应当引入 [`sorapointa-native-wrapper`](../sorapointa-native-wrapper/README.zh-CN.md) 模块，以间接使用 JNI 调用本模块。

## 代码风格

应当使用 [`cargo fmt`](https://github.com/rust-lang/rustfmt) 格式化代码，并使用
[`cargo clippy`](https://github.com/rust-lang/rust-clippy) 静态分析。

通过以下命令安装：

```bash
rustup component add clippy
rustup component add rustfmt
```
