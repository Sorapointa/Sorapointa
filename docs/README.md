<!--Logo-->

![Sorapointa Logo](https://socialify.git.ci/Sorapointa/Sorapointa/image?description=1&descriptionEditable=A%20server%20software%20re-implementation%20for%20a%20certain%20anime%20game%2C%20and%20avoid%20sorapointa&font=Bitter&forks=1&issues=1&logo=https%3A%2F%2Fuser-images.githubusercontent.com%2F62297254%2F171603732-a594e3e0-6968-485f-bb50-344ac7b3a57d.png&name=1&owner=1&pattern=Signal&pulls=1&stargazers=1&theme=Light)

<!--Badges-->

<p align="center">
<a href="https://kotlinlang.org"><img 
src="https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white" 
alt="Kotlin"/></a><a href="https://www.rust-lang.org/"><img 
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

<div align="center"><a href="https://discord.gg/MRadGNhqce"><img alt="Discord - Sorapointa" src="https://img.shields.io/discord/976764233029140550?label=Discord&logo=discord&style=for-the-badge"></a></div>

<!--Content-->

English | [简体中文](README.zh-CN.md)

**WIP**: This project is under active development, you can take a part in contributing, but most of the features is
unavailable.

## Name

As you see, our project name is **Sorapointa**. This name was inspired from Java well-known `NullPointerException`.
We translated `NullPointer` to Japanese and phoneticized it in English.

Sorapointa is aim to reduce runtime-error, write readable, easy-to-maintain code. So, **Sorapointa avoid Sorapointa**.

Sorapointa can be written as the Chinese equivalent of "空想家", but in any case, please read it as <ruby>Sorapointa<rt>
ソラポインタ</rt></ruby>

## Build

Requirement:

- JDK 17
- Rust Toolchains, see：[sorapointa-native/README.md](../sorapointa-native/README.md)

```shell
./gradlew shadowJar
# if you want to run test
./gradlew test
```

### Available Build Configs

Create `local.properties` in project root and edit it. Config uses Java `.properties` format.

| key                    | description                     | available value        |
|------------------------|---------------------------------|------------------------|
| `database.default`     | default database for new config | `SQLITE`, `POSTGRESQL` |
| `database.driver.list` | database drivers to build       | `SQLITE`, `POSTGRESQL` |

Example:

```properties
database.default=SQLITE
database.driver.list=SQLITE,POSTGRESQL
```

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
- [kaml](https://github.com/charleskorn/kaml) - YAML support for kotlinx.serialization
- [Protobuf](https://developers.google.com/protocol-buffers) - Protocol buffers are a language-neutral, platform-neutral
  extensible mechanism for serializing structured data
