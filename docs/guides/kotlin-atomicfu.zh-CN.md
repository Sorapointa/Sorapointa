# Kotlin AtomicFU 使用指南

[English](kotlin-atomicfu.md)

## Setup

```kotlin
dependencies {
  implementation("org.jetbrains.kotlinx:atomicfu:_")
}
```

## 用法

```kotlin
val atomicInt = atomic(123)
atomicInt.value // 读
atomicInt.value = 1234 // 写
```

必须确保所有的操作都是原子的。亦即只能通过 `atomicfu` 已经提供的方法操作。

如果只需要读/写操作，可以使用代理：

```kotlin
var delegatedAtomic by atomic(1231)
delegatedAtomic // 读
delegatedAtomic = 123 // 写
```

**但切记只能通过 `atomicfu` 已经提供的方法操作，原子性需要小心维护，以下的写法是完全错误的：**

**多个原子方法不可组合使用，组合原子方法会失去原子性**

```kotlin
var delegatedAtomic by atomic(123)

if (delegatedAtomic == 1) delegatedAtomic = 1000
```

正确的写法：

```kotlin
val atomicInt = atomic(123)
atomicInt.compareAndSet(expect = 1, update = 1000)
// 或... 使用高阶函数
atomicInt.getAndUpdate { if (it == 1) 1000 else it }
```

无需加锁的函数式写法：

```kotlin
fun push(v: Value) = top.update { cur -> Node(v, cur) }
fun pop(): Value? = top.getAndUpdate { cur -> cur?.next }?.value
```

`Int` 和 `Long` 也有 `getAndIncrement`, `incrementAndGet`, `getAndAdd`, `addAndGet` 方法，以及对 `+=` `-=` 的操作符重载。

## 注意事项

### 避免使用未提供的操作

再次强调，原子性并不凭空产生。你需要使用已经提供的原子性 API。

- 不要使用局部变量存储 atomic 变量的引用
- 不要使用复杂的表达式，任何有分支的语句都会造成问题，需要时请使用诸如 `atomicValue.update` 的方法。

### 隐藏内部实现

如你所见，维护线程安全并不容易，所以不要向外泄漏引用。

需要向外公开API时，请像这样：

```kotlin
private val _foo = atomic(100) // 内部原子变量，以 _ 开头
public var foo: Int by _foo    // 公开代理属性 (val/var)
```
