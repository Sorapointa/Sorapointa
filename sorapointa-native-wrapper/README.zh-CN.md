# Native Wrapper 模块

本模块是对 [`sorapointa-native`](../sorapointa-native/README.zh-CN.md) 的 JNI 绑定和封装。

## JNI 头文件

可以使用 Gradle 任务 `generateJniHeaders` 生成 JNI 头文件，以便于在 Rust 侧编写原生代码实现。输出位于 `./src/generated/jni`。

## 初始化

其他模块在使用本模块时，应当在任何原生代码被调用前，通过 `RustLogger.setup()` 初始化日志，且应当只调用一次。

## 构建行为

默认情况下只会构建宿主系统和架构的 [`sorapointa-native`](../sorapointa-native/README.zh-CN.md) 动态库。

在执行 `shadowJar` 任务时，会将动态库打包至 `org/sorapointa/rust`，并在运行时解压到临时目录。
