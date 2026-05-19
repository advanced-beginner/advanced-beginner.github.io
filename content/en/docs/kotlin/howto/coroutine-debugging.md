---
title: "Coroutine Debugging"
weight: 1
description: "Use kotlinx-coroutines-debug, IntelliJ's coroutine tool window, and JVM options to trace coroutine execution flow and diagnose deadlocks and leaks."
lastmod: "2026-05-13"
---

A step-by-step procedure for tracing where exceptions originate in asynchronous coroutine code and which coroutines are stuck.

**Estimated time**: about 15-20 minutes

{{< callout type="tip" title="TL;DR" >}}
- Adding the JVM option `-Dkotlinx.coroutines.debug` shows the coroutine name in the thread name.
- Use `CoroutineName("name")` to give a coroutine a name for easy identification in logs.
- The IntelliJ Coroutines tab visually shows the state of every running coroutine.
- `DebugProbes` from `kotlinx-coroutines-debug` restores stack traces.
{{< /callout >}}

---

## What This Guide Solves

Use this guide in the following situations:

- When coroutine exception stack traces show only meaningless `kotlinx.coroutines` internal frames
- When you can't tell which coroutine runs on which thread
- When coroutines never complete and you suspect a leak
- When a `CancellationException` is thrown unexpectedly

---

## Before You Start

| Item | Requirement |
|------|-------------|
| kotlinx-coroutines-core | 1.8.x |
| kotlinx-coroutines-debug | 1.8.x (optional, for stack trace restoration) |
| IntelliJ IDEA | 2023.1 or later (Coroutines tab support) |
| JVM | 11 or later |

**Add dependencies (Gradle Kotlin DSL):**

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // Recommend adding only for debug builds
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")
    // Or only for dev environment
    debugImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")
}
```

---

## Step 1: Enable the JVM Debug Option

Add the following to JVM run options to include coroutine info in thread names.

```bash
-Dkotlinx.coroutines.debug
```

**IntelliJ Run Configuration:**

```text
Run/Debug Configurations → VM options:
-Dkotlinx.coroutines.debug
```

**For Gradle execution:**

```kotlin
// build.gradle.kts
tasks.withType<JavaExec> {
    jvmArgs("-Dkotlinx.coroutines.debug")
}

tasks.test {
    jvmArgs("-Dkotlinx.coroutines.debug")
}
```

**Thread name comparison before/after enabling:**

```text
// Before
Thread[DefaultDispatcher-worker-1,5,main]

// After
Thread[DefaultDispatcher-worker-1 @DataLoader#2,5,main]
//                                  ^ CoroutineName  ^ coroutine ID
```

---

## Step 2: Name Coroutines with CoroutineName

Unnamed coroutines are hard to identify in logs. Always give them meaningful names.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking(CoroutineName("Main")) {
    launch(CoroutineName("UserLoader") + Dispatchers.IO) {
        // Thread name: DefaultDispatcher-worker-1 @UserLoader#2
        println("Current thread: ${Thread.currentThread().name}")

        val job = async(CoroutineName("DataFetcher")) {
            delay(100)
            "data"
        }
        println(job.await())
    }

    // Combining names
    val context = CoroutineName("BatchProcessor") + Dispatchers.Default
    launch(context) {
        println("Current thread: ${Thread.currentThread().name}")
    }
}
```

**Include the coroutine name in logs:**

```kotlin
import kotlinx.coroutines.*

fun log(message: String) {
    val coroutineName = currentCoroutineContext()[CoroutineName]?.name ?: "unknown"
    println("[${Thread.currentThread().name}] [$coroutineName] $message")
}

suspend fun processData(id: Int) {
    log("Processing started: $id")
    delay(100)
    log("Processing finished: $id")
}
```

---

## Step 3: Restore Stack Traces with kotlinx-coroutines-debug

By default, coroutine exception stack traces don't cross `suspend` boundaries. Installing `DebugProbes` restores the entire async stack.

**Install DebugProbes:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun main() {
    // Install once at app startup
    DebugProbes.install()

    runBlocking(CoroutineName("Root")) {
        try {
            withContext(CoroutineName("Outer") + Dispatchers.IO) {
                withContext(CoroutineName("Inner") + Dispatchers.Default) {
                    throw IllegalStateException("Inner error!")
                }
            }
        } catch (e: Exception) {
            // Print the restored stack trace
            e.printStackTrace()
        }
    }

    DebugProbes.uninstall()
}
```

**Dump running coroutines:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun main() = runBlocking {
    DebugProbes.install()

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    scope.launch(CoroutineName("Worker1")) {
        delay(Long.MAX_VALUE)  // Wait forever
    }
    scope.launch(CoroutineName("Worker2")) {
        delay(Long.MAX_VALUE)
    }

    delay(100)

    // Print all currently running coroutines
    DebugProbes.dumpCoroutines()
    // Or only a specific scope
    // DebugProbes.dumpCoroutinesInfo().forEach { println(it) }

    scope.cancel()
    DebugProbes.uninstall()
}
```

Example output:
```text
Coroutines dump 2026/05/13 10:00:00

Coroutine "Worker1#2":StandaloneCoroutine{Active}@..., state: SUSPENDED
    at kotlinx.coroutines.DelayKt.delay(Delay.kt)
    at MainKt$main$1$1.invokeSuspend(Main.kt:15)

Coroutine "Worker2#3":StandaloneCoroutine{Active}@..., state: SUSPENDED
    at kotlinx.coroutines.DelayKt.delay(Delay.kt)
    at MainKt$main$1$2.invokeSuspend(Main.kt:18)
```

---

## Step 4: Use the IntelliJ IDEA Coroutines Tab

When you run in debug mode, IntelliJ IDEA shows running coroutines visually in the **Coroutines** tab.

**How to enable:**

1. Run in debug mode (`Shift+F9`).
2. Click the **Coroutines** tab at the bottom of the Debug window.
3. When you pause at a breakpoint, the coroutine tree is displayed.

**Information shown in the Coroutines tab:**

| Information | Description |
|-------------|-------------|
| Coroutine name | The name set via `CoroutineName` |
| State | `RUNNING` / `SUSPENDED` / `CREATED` |
| Thread | The thread currently running it |
| Stack frames | Full call stack up to the suspend point |

{{< callout type="info" title="Tip" >}}
Coroutines without `CoroutineName` are shown as `coroutine#<number>`. Names become hard to distinguish in complex code, so always give them names.
{{< /callout >}}

---

## Step 5: Diagnose Deadlocks

Coroutine deadlocks happen when coroutines wait on each other or when a blocking call occupies a coroutine thread.

**Common deadlock patterns:**

```kotlin
import kotlinx.coroutines.*

// Bad: calling runBlocking from Dispatchers.Main (single thread)
// On Android this causes an ANR
fun badExample() {
    val result = runBlocking {   // Blocks the Main thread!
        delay(1000)
        "result"
    }
}

// Bad: Thread.sleep inside a coroutine (occupies a thread)
suspend fun blockingInsideCoroutine() {
    Thread.sleep(1000)  // Occupies a Dispatchers.Default thread
    // → other coroutines lose scheduling opportunities
}

// Good
suspend fun correctWait() {
    delay(1000)  // Does not occupy a thread
}

// Use Dispatchers.IO for blocking I/O
suspend fun ioBlocking() = withContext(Dispatchers.IO) {
    Thread.sleep(1000)  // Blocking on an IO thread is acceptable
}
```

**Channel deadlock pattern:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

// Bad: send/receive ordering issue on a Rendezvous channel
fun main() = runBlocking {
    val channel = Channel<Int>()  // Rendezvous: no buffer

    // send suspends until receive is ready
    channel.send(1)   // Waits forever! (no receive)
    channel.receive()
}

// Good: producer/consumer in separate coroutines
fun main2() = runBlocking {
    val channel = Channel<Int>()

    launch { channel.send(1) }      // Send in a separate coroutine
    val value = channel.receive()   // Receive in the current coroutine
    println(value)
}
```

---

## Step 6: Diagnose Coroutine Leaks

A coroutine leak is a coroutine that keeps running without being cancelled.

**Detect leaks:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun detectLeaks() {
    DebugProbes.install()
    DebugProbes.enableCreationStackTraces = true  // Track creation location

    // Run the test
    runSomething()

    // Check running coroutines
    val coroutines = DebugProbes.dumpCoroutinesInfo()
    if (coroutines.isNotEmpty()) {
        println("Leaked coroutines found!")
        coroutines.forEach {
            println("- ${it.name}: ${it.state}")
        }
    }

    DebugProbes.uninstall()
}
```

**Common leak causes and fixes:**

```kotlin
import kotlinx.coroutines.*

// Bad: using GlobalScope (cannot be cancelled)
fun leakyCode() {
    GlobalScope.launch {              // Runs until the app exits!
        while (true) {
            delay(1000)
            println("Leaking coroutine")
        }
    }
}

// Good: explicit scope
class MyService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch(CoroutineName("Polling")) {
            while (isActive) {        // isActive check is required
                delay(1000)
                println("Running normally")
            }
        }
    }

    fun stop() {
        scope.cancel()               // Clean up all coroutines
    }
}
```

---

## Step 7: Debug Coroutines in Tests

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class CoroutineTest {

    @Test
    fun `coroutine exception test`() = runTest {
        val deferred = async {
            delay(100)
            throw IllegalStateException("Test error")
        }

        assertFailsWith<IllegalStateException> {
            deferred.await()
        }
    }

    @Test
    fun `time delay test - advanceTimeBy`() = runTest {
        var executed = false

        launch {
            delay(5000)   // Does not wait 5 real seconds
            executed = true
        }

        advanceTimeBy(5001)  // Advance virtual time
        assertTrue(executed)
    }

    @Test
    fun `verify coroutine name`() = runTest(CoroutineName("TestRoot")) {
        val name = coroutineContext[CoroutineName]?.name
        assertEquals("TestRoot", name)
    }
}
```

---

## Checklist

Go through these items in order when debugging coroutines:

- [ ] **JVM option**: Did you add `-Dkotlinx.coroutines.debug`?
- [ ] **Coroutine name**: Did you give meaningful names via `CoroutineName`?
- [ ] **DebugProbes**: Did you install it if you need complex stack traces?
- [ ] **IntelliJ Coroutines tab**: Did you check state in debug mode?
- [ ] **Blocking calls**: Are you avoiding `Thread.sleep` / `Await.result` inside coroutines?
- [ ] **GlobalScope**: Are you using explicit scopes instead?
- [ ] **isActive check**: Are you checking the cancellation signal inside loops?

---

## Related Documents

- [Coroutines Basics](../concepts/coroutines-basics/) — suspend, launch, async fundamentals
- [Coroutines Advanced](../concepts/coroutines-advanced/) — CoroutineScope, SupervisorJob, leak prevention
- [Flow and Async Streams](../concepts/flow-async-streams/) — Flow debugging
- [Performance Profiling](performance-profiling/) — coroutine dispatcher choice and performance measurement
