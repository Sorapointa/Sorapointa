# 关于并发安全

Sorapointa 项目大量使用了协程与多线程技术，请时刻注意检查代码在多线程以及高并发环境下的运行状况

## 共享可修改数据

在开发期间，最好暴露不可修改的变量和集合如 `val`, `List`, `Map`，以避免共享可修改数据造成的线程安全问题

如果一定要共享可修改数据，请使用线程安全的数据结构 ，如 Atomic，Sorapointa 使用了 AtomicFU 框架封装了 Java 的原子方法

关于 AtmoicFU，参考 [AtomicFU 指南](/docs/kotlin-atomicfu.zh-CN.md)

但是对于集合，我们一般使用 `ConcurrentHashMap` 或同样类似的线程安全的数据结构

但使用线程安全的数据结构，并不代表一定安全，原子性并不凭空产生，需要小心维护

比如对于 `ConcurrentHashMap`，必须使用其自身提供的方法如 `get`，`put` 才能保证线程安全，
线程安全的数据结构也只能保证它提供的方法的线程安全和原子性。

在使用线程安全的数据结构时，组合其原子操作会导致原子性的丢失，比如

```kotlin
val map = ConcurrentHashMap<Int, String>()
if (!map.containsKey(123)) {
    map.put(123,"foobar")
}
```

就是线程不安全的，因为当多线程同时使用这个方法时，
他们会同时抵达 `containsKey` 并都发现没有 `123`，并重复执行了下面的代码

你必须使用 `ConcurrentHashMap` 提供的方法 `getOrPut()` 或 `putIfCompute()` 以确保线程安全

了解更多，关于 [线程间共享可修改数据](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html)


## 线程安全分析

对于每个对象，如果对象中的任何成员变量是可修改的，且存在被多线程访问的情况，就需要使用 Atomic ，
并且不可以外泄 Atomic 的赋值权，必须使用 Atomic 自带的更新方法。

如果对象中的任何方法中存在使用 Atomic 对象，保证每次访问 Atomic 对象的操作是完全独立的（不产生分支和引用），
如果是存在依赖性操作，必须保证该依赖性操作的原子性。（也就是上文提到的，不可组合原子方法，或使用锁保证原子性）

如果这个对象本身已经保证所有操作的原子性，就需要保证调用这个对象的对象操作的原子性（外层也不可组合原子方法，以此类推）。

## 对于线程安全的要求

对能简单修复的线程安全问题尽量予以修复，比如 使用 `atomic` 代理，
使用 `ConcurrentHashMap` 以及其内置的其他原子方法，
如果内置的所有原子方法已经不足以满足你的需求，可以尝试使用简单的 `Mutex`
（请遵循指导在协程下正确使用 `Mutex`）

但是在使用 `Mutex` 或更复杂的线程安全机制前， 
首先思考，我是否能接受发生问题的风险（如原石操作是很敏感的，
但是重复复写入 `insertDefault` 或者是因为高并发重复启动一些协程，
这个成本是可以承受的）

成本可接受指的是，造成的数据变更可接受，造成的性能损耗可接受，程序不会报错崩溃

## 关于锁

在 Kotlin 协程中，与线程绑定的锁会容易造成死锁问题（比如 `Mutex`），
建议使用 `sorapointa-utils` 模块中拓展的 `withReentrantLock` 方法，以确保锁的上下文一致性。

可以参考，[Phantom of the Coroutine](https://elizarov.medium.com/phantom-of-the-coroutine-afc63b03a131)

## 结构化并发

代码应该是合作式的，并使用结构化并发确保所有的协程不会泄漏与可被管理

在 Sorapointa 中，我们使用了 `ModuleScope` 以确保正确建立协程之间的任务结构

你可以参照 `TaskManager`，`EventManager` 等，写出合作式的代码

通常情况下，请不要实现 `CoroutineScope` 接口以实现结构化并发，也不要往 `launch` 方法中添加上下文。
具体原因可以参考，[Kotlin CoroutineScope 文档](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/)，
[为什么你不应该实现 CoroutineScope 接口](https://proandroiddev.com/why-your-class-probably-shouldnt-implement-coroutinescope-eb34f722e510)，
[结构化并发周年庆 - Roman Elizarov](https://elizarov.medium.com/structured-concurrency-anniversary-f2cc748b2401)，
[CoroutineScope 的 Legacy Convention](https://maxkim.eu/things-every-kotlin-developer-should-know-about-coroutines-part-2-coroutinescope)

简而言之，这是一种过时了的方法。

了解更多，关于 [结构化并发](https://kotlinlang.org/docs/composing-suspending-functions.html#structured-concurrency-with-async)
