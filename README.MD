<!--Logo-->

![Sorapointa Logo](https://socialify.git.ci/Sorapointa/Sorapointa/image?description=1&descriptionEditable=A%20server%20software%20re-implementation%20for%20a%20certain%20anime%20game%2C%20and%20avoid%20sorapointa&font=Bitter&forks=1&issues=1&logo=https%3A%2F%2Fuser-images.githubusercontent.com%2F62297254%2F171603732-a594e3e0-6968-485f-bb50-344ac7b3a57d.png&name=1&owner=1&pattern=Signal&pulls=1&stargazers=1&theme=Light)

<!--Badges-->

<p align="center">
<a href="https://kotlinlang.org"><img 
src="https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white" 
alt="Kotlin"/></a><a 
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

English | [简体中文](README.zh-CN.MD)

**WIP**: This project is under active development, you can take a part in contributing, but most of the features is unavailable.

## Build

Requirement:

- JDK 17

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
| `proto.fullCompile`    | do full compile for proto       | `true`                 |

Example:

```properties
database.default=SQLITE
database.driver.list=SQLITE,POSTGRESQL
proto.fullCompile=true
```

## Contributing

See: [CONTRIBUTING](CONTRIBUTING.md), and README in your interested subproject.

## Thanks

- [HolographicHat](https://github.com/HolographicHat) - Supports a lot on algorithms and computer security.
- [Grasscutter](https://github.com/Grasscutters/Grasscutter)
- [Kotlin](https://github.com/JetBrains/kotlin) - A modern programming language that makes developers happier.
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - Kotlin multiplatform / multi-format
  **reflectionless** serialization
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - A rich library for coroutines developed by
  JetBrains
- [Ktor](https://github.com/ktorio/ktor) - An asynchronous framework for creating microservices, web applications and
  more.
