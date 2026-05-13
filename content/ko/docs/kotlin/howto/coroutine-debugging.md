---
title: "코루틴 디버깅"
weight: 1
description: "kotlinx-coroutines-debug, IntelliJ 코루틴 도구창, JVM 옵션으로 코루틴 실행 흐름을 추적하고 데드락·누수를 진단하는 방법을 안내합니다."
lastmod: "2026-05-13"
---

비동기 코루틴 코드에서 예외가 어디서 왔는지, 어떤 코루틴이 멈춰 있는지 추적하는 절차를 단계별로 안내합니다.

**소요 시간**: 약 15~20분

{{< callout type="tip" title="TL;DR" >}}
- JVM 옵션 `-Dkotlinx.coroutines.debug`를 추가하면 스레드 이름에 코루틴 이름이 표시됩니다.
- `CoroutineName("이름")`으로 코루틴에 이름을 부여해 로그에서 식별합니다.
- IntelliJ의 Coroutines 탭에서 실행 중인 모든 코루틴 상태를 시각적으로 확인합니다.
- `kotlinx-coroutines-debug`의 `DebugProbes`로 스택 트레이스를 복원합니다.
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- 코루틴 예외의 스택 트레이스가 의미 없는 `kotlinx.coroutines` 내부 프레임만 보일 때
- 어느 코루틴이 어느 스레드에서 실행 중인지 알 수 없을 때
- 코루틴이 완료되지 않고 무한 대기하거나 메모리 누수가 의심될 때
- `CancellationException`이 예상치 않게 발생할 때

---

## 시작하기 전에

| 항목 | 요구 사항 |
|------|----------|
| kotlinx-coroutines-core | 1.8.x |
| kotlinx-coroutines-debug | 1.8.x (선택, 스택 트레이스 복원용) |
| IntelliJ IDEA | 2023.1 이상 (Coroutines 탭 지원) |
| JVM | 11 이상 |

**의존성 추가 (Gradle Kotlin DSL):**

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // 디버그 빌드에만 추가 권장
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")
    // 또는 개발 환경에만 추가
    debugImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")
}
```

---

## 1단계: JVM 디버그 옵션 활성화

JVM 실행 옵션에 다음을 추가하면 스레드 이름에 코루틴 정보가 포함됩니다.

```bash
-Dkotlinx.coroutines.debug
```

**IntelliJ Run Configuration 설정:**

```text
Run/Debug Configurations → VM options:
-Dkotlinx.coroutines.debug
```

**Gradle 실행 시:**

```kotlin
// build.gradle.kts
tasks.withType<JavaExec> {
    jvmArgs("-Dkotlinx.coroutines.debug")
}

tasks.test {
    jvmArgs("-Dkotlinx.coroutines.debug")
}
```

**활성화 전/후 스레드 이름 비교:**

```text
// 활성화 전
Thread[DefaultDispatcher-worker-1,5,main]

// 활성화 후
Thread[DefaultDispatcher-worker-1 @DataLoader#2,5,main]
//                                  ^ CoroutineName  ^ 코루틴 ID
```

---

## 2단계: CoroutineName으로 코루틴에 이름 붙이기

이름이 없는 코루틴은 로그에서 식별이 어렵습니다. 항상 의미 있는 이름을 부여합니다.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking(CoroutineName("Main")) {
    launch(CoroutineName("UserLoader") + Dispatchers.IO) {
        // 스레드 이름: DefaultDispatcher-worker-1 @UserLoader#2
        println("현재 스레드: ${Thread.currentThread().name}")

        val job = async(CoroutineName("DataFetcher")) {
            delay(100)
            "데이터"
        }
        println(job.await())
    }

    // 이름 조합
    val context = CoroutineName("BatchProcessor") + Dispatchers.Default
    launch(context) {
        println("현재 스레드: ${Thread.currentThread().name}")
    }
}
```

**로깅에 코루틴 이름 포함:**

```kotlin
import kotlinx.coroutines.*

fun log(message: String) {
    val coroutineName = currentCoroutineContext()[CoroutineName]?.name ?: "unknown"
    println("[${Thread.currentThread().name}] [$coroutineName] $message")
}

suspend fun processData(id: Int) {
    log("처리 시작: $id")
    delay(100)
    log("처리 완료: $id")
}
```

---

## 3단계: kotlinx-coroutines-debug로 스택 트레이스 복원

기본적으로 코루틴 예외의 스택 트레이스는 `suspend` 경계를 넘지 못합니다. `DebugProbes`를 설치하면 전체 비동기 스택을 복원합니다.

**DebugProbes 설치:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun main() {
    // 앱 시작 시 한 번 설치
    DebugProbes.install()

    runBlocking(CoroutineName("Root")) {
        try {
            withContext(CoroutineName("Outer") + Dispatchers.IO) {
                withContext(CoroutineName("Inner") + Dispatchers.Default) {
                    throw IllegalStateException("내부 오류 발생!")
                }
            }
        } catch (e: Exception) {
            // 복원된 스택 트레이스 출력
            e.printStackTrace()
        }
    }

    DebugProbes.uninstall()
}
```

**실행 중인 코루틴 덤프:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun main() = runBlocking {
    DebugProbes.install()

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    scope.launch(CoroutineName("Worker1")) {
        delay(Long.MAX_VALUE)  // 무한 대기
    }
    scope.launch(CoroutineName("Worker2")) {
        delay(Long.MAX_VALUE)
    }

    delay(100)

    // 현재 실행 중인 모든 코루틴 출력
    DebugProbes.dumpCoroutines()
    // 또는 특정 스코프만
    // DebugProbes.dumpCoroutinesInfo().forEach { println(it) }

    scope.cancel()
    DebugProbes.uninstall()
}
```

출력 예시:
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

## 4단계: IntelliJ IDEA Coroutines 탭 활용

IntelliJ IDEA에서 디버그 모드로 실행하면 **Coroutines** 탭에서 실행 중인 코루틴을 시각적으로 확인할 수 있습니다.

**활성화 방법:**

1. 디버그 모드(`Shift+F9`)로 실행합니다.
2. Debug 창 하단의 **Coroutines** 탭을 클릭합니다.
3. 브레이크포인트에서 일시 정지하면 코루틴 트리가 표시됩니다.

**Coroutines 탭에서 확인할 수 있는 정보:**

| 정보 | 설명 |
|------|------|
| 코루틴 이름 | `CoroutineName`으로 지정한 이름 |
| 상태 | `RUNNING` / `SUSPENDED` / `CREATED` |
| 스레드 | 현재 실행 중인 스레드 이름 |
| 스택 프레임 | suspend 지점까지의 전체 호출 스택 |

{{< callout type="info" title="팁" >}}
`CoroutineName`을 설정하지 않은 코루틴은 `coroutine#숫자`로 표시됩니다. 복잡한 코드에서는 이름 구분이 어려우므로 항상 이름을 부여하는 것이 좋습니다.
{{< /callout >}}

---

## 5단계: 데드락 진단

코루틴 데드락은 서로가 서로를 기다리거나, 블로킹 호출이 코루틴 스레드를 점유해서 발생합니다.

**일반적인 데드락 패턴:**

```kotlin
import kotlinx.coroutines.*

// 잘못된 예: Dispatchers.Main (단일 스레드)에서 runBlocking 호출
// 실제 Android에서는 ANR 발생
fun badExample() {
    val result = runBlocking {   // Main 스레드 블로킹!
        delay(1000)
        "결과"
    }
}

// 잘못된 예: 코루틴 내부에서 Thread.sleep (스레드 점유)
suspend fun blockingInsideCoroutine() {
    Thread.sleep(1000)  // Dispatchers.Default 스레드 풀을 점유
    // → 다른 코루틴이 스케줄링 기회를 잃음
}

// 올바른 예
suspend fun correctWait() {
    delay(1000)  // 스레드를 점유하지 않음
}

// I/O 블로킹은 Dispatchers.IO에서
suspend fun ioBlocking() = withContext(Dispatchers.IO) {
    Thread.sleep(1000)  // IO 스레드에서 블로킹 → 허용
}
```

**채널 데드락 패턴:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

// 잘못된 예: Rendezvous 채널에서 보내기/받기 순서 문제
fun main() = runBlocking {
    val channel = Channel<Int>()  // Rendezvous: 버퍼 없음

    // send는 receive가 준비될 때까지 suspend
    channel.send(1)   // 여기서 영구 대기! (receive가 없음)
    channel.receive()
}

// 올바른 예: 생산자/소비자를 별개 코루틴으로
fun main2() = runBlocking {
    val channel = Channel<Int>()

    launch { channel.send(1) }      // 별개 코루틴에서 send
    val value = channel.receive()   // 현재 코루틴에서 receive
    println(value)
}
```

---

## 6단계: 코루틴 누수 진단

코루틴 누수는 취소되지 않고 계속 실행되는 코루틴입니다.

**누수 탐지:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*

fun detectLeaks() {
    DebugProbes.install()
    DebugProbes.enableCreationStackTraces = true  // 생성 위치도 추적

    // 테스트 실행
    runSomething()

    // 실행 중인 코루틴 확인
    val coroutines = DebugProbes.dumpCoroutinesInfo()
    if (coroutines.isNotEmpty()) {
        println("누수된 코루틴 발견!")
        coroutines.forEach {
            println("- ${it.name}: ${it.state}")
        }
    }

    DebugProbes.uninstall()
}
```

**일반적인 누수 원인과 해결:**

```kotlin
import kotlinx.coroutines.*

// 잘못된 예: GlobalScope 사용 (취소 불가)
fun leakyCode() {
    GlobalScope.launch {              // 앱 종료까지 실행됨!
        while (true) {
            delay(1000)
            println("누수 중인 코루틴")
        }
    }
}

// 올바른 예: 명시적 스코프 사용
class MyService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch(CoroutineName("Polling")) {
            while (isActive) {        // isActive 확인 필수
                delay(1000)
                println("정상 동작 중")
            }
        }
    }

    fun stop() {
        scope.cancel()               // 모든 코루틴 정리
    }
}
```

---

## 7단계: 테스트에서 코루틴 디버깅

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class CoroutineTest {

    @Test
    fun `코루틴 예외 테스트`() = runTest {
        val deferred = async {
            delay(100)
            throw IllegalStateException("테스트 오류")
        }

        assertFailsWith<IllegalStateException> {
            deferred.await()
        }
    }

    @Test
    fun `시간 지연 테스트 - advanceTimeBy`() = runTest {
        var executed = false

        launch {
            delay(5000)   // 실제로 5초를 기다리지 않음
            executed = true
        }

        advanceTimeBy(5001)  // 가상 시간 진행
        assertTrue(executed)
    }

    @Test
    fun `코루틴 이름 확인`() = runTest(CoroutineName("TestRoot")) {
        val name = coroutineContext[CoroutineName]?.name
        assertEquals("TestRoot", name)
    }
}
```

---

## 체크리스트

코루틴 디버깅 시 다음을 순서대로 확인하세요:

- [ ] **JVM 옵션**: `-Dkotlinx.coroutines.debug` 추가했는가?
- [ ] **코루틴 이름**: `CoroutineName`으로 의미 있는 이름을 붙였는가?
- [ ] **DebugProbes**: 복잡한 스택 트레이스가 필요하면 설치했는가?
- [ ] **IntelliJ Coroutines 탭**: 디버그 모드로 실행해서 상태를 확인했는가?
- [ ] **블로킹 호출**: 코루틴 내부에서 `Thread.sleep` / `Await.result` 등을 사용하지 않는가?
- [ ] **GlobalScope**: 사용하지 않고 명시적 스코프를 쓰는가?
- [ ] **isActive 확인**: 루프 내부에서 취소 신호를 확인하는가?

---

## 관련 문서

- [코루틴 기초](../concepts/coroutines-basics/) — suspend, launch, async 기초
- [코루틴 고급](../concepts/coroutines-advanced/) — CoroutineScope, SupervisorJob, 누수 방지 패턴
- [Flow와 비동기 스트림](../concepts/flow-async-streams/) — Flow 디버깅
- [성능 프로파일링](performance-profiling/) — 코루틴 디스패처 선택과 성능 측정
