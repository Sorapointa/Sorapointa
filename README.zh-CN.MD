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
</p>

<!--Content-->

[English](./README.md) | 简体中文

**WIP**: 该项目正被积极开发，你可以参与贡献，但大多数功能不可用。

## Build

需要:

- JDK 17

```shell
./gradlew shadowJar
# 如果你想运行测试
./gradlew test
```

### 可用的构建选项

在项目根目录创建 `local.properties` 并编辑。配置使用 Java `.properties` 格式。

| key                    | 描述                     | 可用值                    |
|------------------------|-------------------------|------------------------|
| `database.default`     | 为新配置填充的默认数据库    | `SQLITE`, `POSTGRESQL` |
| `database.driver.list` | 构建将提供的数据库驱动列表  | `SQLITE`, `POSTGRESQL` |
| `proto.fullCompile`    | 为 proto 开启全量编译     | `true`                 |

示例：

```properties
database.default=SQLITE
database.driver.list=SQLITE,POSTGRESQL
proto.fullCompile=true
```

## Contributing

参见：[CONTRIBUTING](./CONTRIBUTING.zh-CN.md)，以及对应模块的 README。

## Thanks

- [Grasscutter](https://github.com/Grasscutters/Grasscutter)
- [Kotlin](https://github.com/JetBrains/kotlin) - A modern programming language that makes developers happier.
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - Kotlin multiplatform / multi-format
  **reflectionless** serialization
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - A rich library for coroutines developed by
  JetBrains
- [Ktor](https://github.com/ktorio/ktor) - An asynchronous framework for creating microservices, web applications and
  more.
