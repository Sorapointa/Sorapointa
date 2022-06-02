# Data Loader Module

[简体中文](README.zh-CN.md)

## Register Resource

You can register resource like this:

```kotlin
@Serializable // use kotlinx.serialization to decode data
data class Example(
  val int: Int = 1111, // making all field immutable is best practice 
  val bool: Boolean = true,
  val set: Set<Int> = emptySet(),
  val list: List<String> = listOf("12312", "asdfads", "xccxkvo")
)

// private loader
// path is relative to $workDir/resources
private val exampleLoader = DataLoader<List<Example>>(path = "path/to/example.json")  

// public data
val exampleData get() = exampleLoader.data
```

Please put declaration at **top-level** to make sure class loader init them at program start.

## Load Resource

```kotlin
DataHolder.loadAll()
```

## Visit Resource

Just visit variables as usual:

```kotlin
exampleDataList.first().int
exampleDataList.first().set.first()
exampleDataList.asSequence()
  .filter { it.bool == false }
  .map { it.int }
  .sum()
```
