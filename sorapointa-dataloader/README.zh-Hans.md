# Data Loader 模块

## 注册 Resource

像这样注册资源：

```kotlin
@Serializable // 用 kotlinx.serialization 序列化
data class Example(
  val int: Int = 1111, // 让所有字段都不可变是最佳实践 
  val bool: Boolean = true,
  val set: Set<Int> = emptySet(),
  val list: List<String> = listOf("12312", "asdfads", "xccxkvo")
)

// private loader
// path 是对 $workDir/resources 的相对路径
private val exampleLoader = DataLoader<List<Example>>(path = "path/to/example.json")  

// public data
val exampleData get() = exampleLoader.data
```

把声明放在 **top-level** 以保证 class loader 在程序开始时就加载他们。

## 加载 Resource

```kotlin
DataHolder.loadAll()
```

## 访问 Resource

像访问普通变量一样访问 Resource：

```kotlin
exampleDataList.first().int
exampleDataList.first().set.first()
exampleDataList.asSequence()
  .filter { it.bool == false }
  .map { it.int }
  .sum()
```
