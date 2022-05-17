# Concurrency Safety

The Sorapointa project has applied a lot of coroutine and multi-thread techniques, 
so please to be very careful with thread-safe and 
the code performance in the multi-thread and high volume situation.

## Shared Mutable State

During development, it is best to expose **unmodifiable** variables and collections 
such as `val`, `List`, `Map` to avoid thread safety issues caused by sharing mutable data

If you must share mutable data, 
use a thread-safe data structure such as Atomic, 
Sorapointa has already used the AtomicFU framework.

About AtmoicFU，please refers to [AtomicFU Guideline](kotlin-atomicfu.md)

But for collections, 
we usually use `ConcurrentHashMap` or some similar thread-safe data structures

However, using thread-safe data structures does not guarantee are safe; 
atomicity is not magic or comes from void, it needs to be carefully maintained

For example, for `ConcurrentHashMap`, 
you must use the provided methods such as `get`, `put`, `getOrPut` to ensure thread safety.
A thread-safe data structure can only guarantee the atomicity of the methods it provides.

So, when using thread-safe data structures, combining their atomic operations 
can lead to a loss of atomicity and to be thread-unsafe, for example

```kotlin
val map = ConcurrentHashMap<Int, String>()
if (!map.containsKey(123)) {
    map.put(123,"foobar")
}
```

This is thread-unsafe because when multiple threads use this method at the same time,
they will both arrive at `containsKey` and both find there is not `123` 
and it will repeatedly execute following code

So, that's why you must use the methods `getOrPut()` or `putIfCompute()` 
provided by `ConcurrentHashMap` to ensure thread safety.

Refers to [Shared Mutable State](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html)

## Thread Safety Review

For every object, if any properties is variable
and will be access by multiple threads, you have to use Atomic.
You cannot leak the atomic reference to public,
and must use the provided update functions.

If there are any methods in the object that use Atomic objects,
ensure that every operation accesses it
is completely independent (no branches and local variables).
If there is a dependent operation,
you must ensure the atomicity of that dependent operation.
(As mentioned above, atomic methods cannot be combined,
or locks can be used to ensure atomicity)

If this object itself already guarantees atomicity for all operations,
it is necessary ensure that the operations of the object is atomic.
(and the outer methods must not combine atomic methods, and so on).

## Requirements for Thread Safety

Fix as many thread-safety issues as possible that can be fixed easily,
such as using the atomic delegation, ConcurrentHashMap
and other built-in atomic objects.
If all built-in atomic methods are no longer enough for your needs,
try using the simple Mutex.

But before using Mutex or more complex thread-safe mechanism,
first think about whether I can accept the risk of problems occurring
(e.g., the primogem operation is very sensitive to
But repeatedly rewriting insertDefault
or repeatedly starting some coroutines because of high concurrency.
This cost is affordable.)

Acceptable cost means that
the resulting data changes are acceptable,
the resulting performance loss is acceptable,
and the program will not crash with errors.

## Structured Concurrency

The code is cooperative and use structured concurrency to
ensure that all concurrent processes do not leak and are manageable.

In Sorapointa, we use ModuleScope to
ensure that the task structure between concurrent processes is proper.

In general，please don't implement `CoroutineScope` interface to make coroutine to be structured concurrency,
and also don't add context in the parameter of `launch` method.
For specific reasons, please refer to, [Kotlin CoroutineScope Documentation](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/)，
[Why your class probably should not implement CoroutineScope](https://proandroiddev.com/why-your-class-probably-shouldnt-implement-coroutinescope-eb34f722e510)，
[Structured Concurrency Anniversary](https://elizarov.medium.com/structured-concurrency-anniversary-f2cc748b2401)，
[Legacy Convention of CoroutineScope](https://maxkim.eu/things-every-kotlin-developer-should-know-about-coroutines-part-2-coroutinescope)

In short, it's an outdated approach.

You can write cooperative code by referring to TaskManager, EventManager, etc.
