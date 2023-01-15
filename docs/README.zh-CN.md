<!--Logo-->

![Sorapointa Logo](https://socialify.git.ci/Sorapointa/Sorapointa/image?description=1&descriptionEditable=A%20server%20software%20re-implementation%20for%20a%20certain%20anime%20game%2C%20and%20avoid%20sorapointa&font=Bitter&forks=1&issues=1&logo=https%3A%2F%2Fuser-images.githubusercontent.com%2F62297254%2F171603732-a594e3e0-6968-485f-bb50-344ac7b3a57d.png&name=1&owner=1&pattern=Signal&pulls=1&stargazers=1&theme=Light)

<!--Badges-->

<p align="center">
<a href="https://kotlinlang.org"><img 
src="https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white" 
alt="Kotlin"/></a><a href="https://www.rust-lang.org/zh-CN/"><img 
src="https://img.shields.io/badge/rust-%23704b34.svg?style=for-the-badge&logo=rust&logoColor=white" 
alt="Rust"/></a><a 
href="https://gradle.org/"><img 
src="https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white" 
alt="Gradle"/></a><a 
href="https://www.jetbrains.com/idea/"><img 
src="https://img.shields.io/badge/IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white" 
alt="IntelliJ IDEA"/></a>
</p>

<p align="center">
<a 
href="https://www.apache.org/licenses/LICENSE-2.0"><img 
src="https://img.shields.io/badge/License-Apache2.0-lightgreen?style=for-the-badge&logo=opensourceinitiative&logoColor=white" 
alt="Apache 2.0 Open Source License"/></a><a 
href="https://s01.oss.sonatype.org/content/repositories/snapshots/moe/sdl/sorapointa/"><img 
src="https://img.shields.io/nexus/s/moe.sdl.sorapointa/sorapointa-core?logo=apache-maven&label=Maven%20Dev&server=https%3A%2F%2Fs01.oss.sonatype.org&style=for-the-badge" 
alt="Maven Developer"/></a>
</p>

<!--Content-->

[English](README.md) | 简体中文

**WIP**: 该项目正被积极开发，你可以参与贡献，但大多数功能不可用。

## 名称

如你所见，我们的项目名为 **Sorapointa**。这来自于 `Java` 中闻名的 `NullPointerException`。
我们将 `NullPointer` 日语化再以英语拟音，得到了这个名字。

Sorapointa 旨在减少运行时错误，编写可读性高、易于维护的代码。因此，**Sorapointa 避免 Sorapointa**。

Sorapointa 可以写成对应的汉字「空想家」，但无论如何，请你读作 <ruby>Sorapointa<rt>ソラポインタ</rt></ruby>

## Build

需要:

- JDK 17
- Rust 工具链，详情参见：[sorapointa-native/README.md](../sorapointa-native/README.zh-CN.md)

```shell
./gradlew shadowJar
# 如果你想运行测试
./gradlew test
```

### 可用的构建选项

在项目根目录创建 `local.properties` 并编辑。配置使用 Java `.properties` 格式。

| key                    | 描述         | 可用值                    |
|------------------------|------------|------------------------|
| `database.default`     | 默认配置使用的数据库 | `SQLITE`, `POSTGRESQL` |
| `database.driver.list` | 编译时打包哪些数据库 | `SQLITE`, `POSTGRESQL` |

示例：

```properties
database.default=SQLITE
database.driver.list=SQLITE,POSTGRESQL
```

## Contributing

参见：[CONTRIBUTING](CONTRIBUTING.zh-CN.md)，以及对应模块的 README。

## Thanks

### Person

- [HolographicHat](https://github.com/HolographicHat) - Supports a lot on algorithms and computer security.

### Project

- [JVM](https://openjdk.org/) - The best programming language VM
- [Kotlin](https://github.com/JetBrains/kotlin) - A modern programming language that makes developers happier.
- [Rust](https://github.com/rust-lang/rust) - A language empowering everyone to build reliable and efficient software.
- [IDEA](https://www.jetbrains.com/idea/) - Capable and Ergonomic IDE for JVM
- [Grasscutter](https://github.com/Grasscutters/Grasscutter)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - Kotlin multiplatform / multi-format
  **reflectionless** serialization
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - A rich library for coroutines developed by
  JetBrains
- [kotlinx.atomicfu](https://github.com/Kotlin/kotlinx.atomicfu) - The idiomatic way to use atomic operations in Kotlin
- [Ktor](https://github.com/ktorio/ktor) - An asynchronous framework for creating microservices, web applications and
  more.
- [Netty](https://netty.io/) - Netty is an asynchronous event-driven network application framework
- [Exposed](https://github.com/JetBrains/Exposed) - Kotlin SQL Framework
- [Clikt](https://github.com/ajalt/clikt/tree/master/clikt) - Multiplatform command line interface parsing for Kotlin
- [Kotlin-Logging](https://github.com/MicroUtils/kotlin-logging) - Lightweight logging framework for Kotlin
- [Password4j](https://github.com/Password4j/password4j) - Password4j is a user-friendly cryptographic library that
  supports Argon2 and so on
- [JLine](https://github.com/jline/jline3) - JLine is a Java library for handling console input.
- [yamlkt](https://github.com/him188/yamlkt) - Multiplatform YAML parser & serializer for kotlinx.serialization
- [Protobuf](https://developers.google.com/protocol-buffers) - Protocol buffers are a language-neutral, platform-neutral
  extensible mechanism for serializing structured data
