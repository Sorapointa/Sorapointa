# Kotlin AtomicFU Guideline

[简体中文](kotlin-atomicfu.zh-CN.md)

## Setup

```kotlin
dependencies {
  implementation("org.jetbrains.kotlinx:atomicfu:_")
}
```

## Usage

```kotlin
val atomicInt = atomic(123)
atomicInt.value // read
atomicInt.value = 1234 // write
```

Must ensure all operations are atomic. You can only operate value with provided methods.

If you only need read and write operation, use kotlin delegation:

```kotlin
var delegatedAtomic by atomic(1231)
delegatedAtomic // read
delegatedAtomic = 123 // write
```

**BUT, please use the methods `atomicfu` provided only. Atomicity requires careful maintenance. The snippet below
is completely wrong:**

**Multiple atomic methods cannot be combined, combining atomic methods will lose atomicity**

```kotlin
var delegatedAtomic by atomic(123)

if (delegatedAtomic == 1) delegatedAtomic = 1000
```

Should be:

```kotlin
val atomicInt = atomic(123)
atomicInt.compareAndSet(expect = 1, update = 1000)
// or... using high-order function
atomicInt.getAndUpdate { if (it == 1) 1000 else it }
```

Idiomatic lock-free methods:

```kotlin
fun push(v: Value) = top.update { cur -> Node(v, cur) }
fun pop(): Value? = top.getAndUpdate { cur -> cur?.next }?.value
```

Int and Long atomics provide all the usual `getAndIncrement`, `incrementAndGet`, `getAndAdd`, `addAndGet`, etc. They can
be also atomically modified via `+=` and `-=` operators.

## Notice

### Avoid using unprovided API

Notice again, atomicity needs careful maintenance. You must use provided API.

- Do not read references on atomic variables into local variables
- Do not introduce complex data flow in parameters to atomic variable operations, please use function
  like `atomicValue.update` instead

### Hide internal implementation

As you can see, it's hard to maintenance thread-safe, so do not leak reference to other modules.

Use the following convention if you need to expose the value of atomic property to the public:

```kotlin
private val _foo = atomic(100) // private atomic, starts with underscore
public var foo: Int by _foo    // public delegated property (val/var)
```
