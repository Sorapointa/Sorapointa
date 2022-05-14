# JUnit 使用指南

[English](unittest.md)

[JUnit 官方文档](https://junit.org/junit5/docs/current/user-guide/)

## 基本用法

你可以在 `test` 源集中创建单元测试，包结构应该和 `main` 源集保持一致。
例如，要为 `org.sorapointa.db.Account` 编写单元测试，
就可以创建 `org.sorapointa.db.AccountTest`。

```kotlin
// org.sorapointa.db.Account
data class Account(
  val name: String,
  val level: Int,
)

// org.sorapointa.db.AccountTest
class AccountTest {
  @Test
  fun `account must be equals`() {
    val var1 = Account(name = "foo", level = 20)
    val var2 = var1.copy()
    assertEquals(var1, var2)
  }
}
```

通过添加 `@Test` 注解，JUnit 和 IDEA 就可以识别出单元测试。

你或许已经注意到，测试方法名被反引号包裹。
这是为了可读性，你可以在测试名中使用自然语言。
(另外，下划线命名法在 Java 和 Android 测试中也很流行。)
参见：[Kotlin 文档](https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods).

断言通过描述**必须**(MUST)和**一定不能**(MUST NOT)做的事，以测试程序。
如果条件为 false，断言抛出异常，同时测试也失败。

以下方法经常用到:

- `assert(条件)`
- `assertEquals(预期值, 实际值)`
- `assertContentEquals(预期值, 实际值)` 用于数组
- `assertTrue { 代码块 }`

## BeforeAll / BeforeEach

1.

```kotlin
@TestInstance(Lifecycle.PER_CLASS)
class DatabaseTest() {
  @BeforeAll
  fun initDatabase() {
    // 初始化
  }

  @Test
  fun test1() {
    // 测试...
  }
}
```

2.

```kotlin
class DatabaseTest() {
  companion object {
    @BeforeAll
    @JvmStatic
    fun initDatabase() {
      // 初始化
    }
  }

  @Test
  fun test1() {
    // 测试...
  }
}
```

1 和 2 都是正确的。被 `@BeforeAll` 注解的方法将会在所有方法之前被调用。

`@BeforeEach` 类似，但函数在每个方法之前被调用。

## 测试依赖

如果一个依赖仅用于测试，在 `build.gradle.kts` 中这样添加:

```kotlin
dependencies {
  testImplementation("com.example.artifact:example:version")
  // 添加子模块的依赖:
  testImplementation(project(":sorapointa-event", "test"))
}
```

## Sorapointa 测试工具

### 属性

- `TEST_DIR` is the Gradle root project directory
- `IS_CI`, is run in CI

### 方法

- `runTest` 提供 suspend 块和 SKIP_OPTION

```kotlin
// SKIP_CI: 如果运行在 CI，跳过该测试
runTest(TestOption.SKIP_CI) {
  val atomicInt = atomic(0)
  (1..10).map {
    // 可以使用 suspend 函数和 CoroutineScope 的拓展方法
    launch {
      repeat(1000) {
        atomicInt.getAndIncrement()
      }
    }
  }.joinAll()
  assertEquals(10000, 10000)
}
```
