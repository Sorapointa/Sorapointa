# JUnit Guideline

[简体中文](unittest.zh-CN.md)

[JUnit Official User Guide](https://junit.org/junit5/docs/current/user-guide/)

## Basic Usage

You can create unit test in `test` source set,
the package structure should keep the same as the `main` source set.
For example, you're going to write unit test for `org.sorapointa.db.Account`,
you can create class `org.sorapointa.db.AccountTest`:

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

By annotating test function with `@Test`, JUnit and IDEA can recognize it.

You may have noticed the test name is surrounded by backticks.
It's for readability, you can use natural language in test name.
(By the way, under_score_style is popular in Java and Android test.)
See [Kotlin documentation](https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods).

Assertion is a way for test by describing the program **MUST** or **MUST NOT** do something.
If condition equals to false, assertion throw exception, then test fails.

Following method is frequently used:

- `assert(condition)`
- `assertEquals(excepted, actual)`
- `assertContentEquals(excepted, actual)` for arrays
- `assertTrue { block }`

## BeforeAll / BeforeEach

1.

```kotlin
@TestInstance(Lifecycle.PER_CLASS)
class DatabaseTest() {
  @BeforeAll
  fun initDatabase() {
    // do init
  }

  @Test
  fun test1() {
    // do test...
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
      // do init
    }
  }

  @Test
  fun test1() {
    // do test...
  }
}
```

Both 1 and 2 are correct. The function annotated by `@BeforeAll` will be invoked before other function.

`@BeforeEach` is similar, but the function will be invoked before each function.

## Test Dependency

If a dependency is test only, add it in `build.gradle.kts` in this way:

```kotlin
dependencies {
  testImplementation("com.example.artifact:example:version")
  // add test dependency from subprojects:
  testImplementation(project(":sorapointa-event", "test"))
}
```

## Sorapointa Test Util

### Properties

- `TEST_DIR` is the Gradle root project directory
- `IS_CI`, is run in CI

### Functions

- `runTest` provide a suspend block, and SKIP_OPTIONs

```kotlin
// SKIP_CI: skip test if run in CI
runTest(TestOption.SKIP_CI) {
  val atomicInt = atomic(0)
  (1..10).map {
    // suspend and CoroutineScope extension function is applicable
    launch {
      repeat(1000) {
        atomicInt.getAndIncrement()
      }
    }
  }.joinAll()
  assertEquals(10000, 10000)
}
```
