---
title: "코루틴 고급"
description: "CoroutineContext, 구조화된 예외 전파, SupervisorJob, Channel, select 표현식, 코루틴 누수 방지 패턴을 설명합니다."
weight: 15
lastmod: "2026-05-13"
---

## 전체 비유: 항공사 운항 시스템

항공사 운항 시스템에서 하나의 편 지연이 전체 스케줄에 영향을 줄 수 있지만, `SupervisorJob`처럼 각 노선을 독립적으로 관리하면 한 편의 결항이 다른 편에 영향을 주지 않습니다.

| 항공사 비유 | Kotlin 코루틴 | 역할 |
|------------|-------------|------|
| 항공 관제 데이터 | `CoroutineContext` | 코루틴 실행 정보 묶음 |
| 편명 식별자 | `CoroutineName` | 코루틴 이름 지정 |
| 노선별 독립 스케줄 | `SupervisorJob` | 자식 실패가 형제에 영향 없음 |
| 항공편 탑승구 채널 | `Channel<T>` | 코루틴 간 안전한 데이터 전달 |
| 먼저 도착한 편 처리 | `select { }` | 여러 채널 중 먼저 도착한 것 처리 |

---

> **대상 독자**: 코루틴 기초와 Flow를 이해한 개발자
> **선수 지식**: [코루틴 기초](../coroutines-basics/), [Flow와 비동기 스트림](../flow-async-streams/)
> **소요 시간**: 약 45~55분
> **이 문서를 읽으면**: CoroutineContext를 직접 조합하고, 예외 전파 구조를 제어하며, Channel과 select로 복잡한 비동기 통신을 설계할 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- `CoroutineContext`는 Dispatcher, Job, CoroutineName 등의 요소를 `+`로 조합합니다.
- `SupervisorJob`/`supervisorScope`를 쓰면 자식 코루틴의 실패가 다른 자식에 전파되지 않습니다.
- `CoroutineExceptionHandler`는 `launch`에서 잡히지 않은 예외를 처리합니다.
- `Channel`은 코루틴 간 안전한 큐로, Rendezvous/Buffered/Unlimited 세 가지 모드가 있습니다.
- `select { }`는 여러 채널 중 먼저 준비된 것을 선택합니다.
{{< /callout >}}

---

#### CoroutineContext — 코루틴의 DNA

`CoroutineContext`는 코루틴 실행에 필요한 정보를 담는 불변 컨테이너입니다. `+` 연산자로 요소를 조합합니다.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // 여러 컨텍스트 요소를 + 로 조합
    val context = Dispatchers.IO +
                  CoroutineName("DataLoader") +
                  CoroutineExceptionHandler { _, e -> println("예외: $e") }

    launch(context) {
        println("실행 중 스레드: ${Thread.currentThread().name}")
        println("코루틴 이름: ${coroutineContext[CoroutineName]?.name}")
    }
}
// 실행 중 스레드: DefaultDispatcher-worker-1
// 코루틴 이름: DataLoader
```

**주요 CoroutineContext 요소:**

| 요소 | 타입 | 역할 |
|------|------|------|
| `Dispatchers.IO` | `CoroutineDispatcher` | 실행 스레드 풀 지정 |
| `Job()` | `Job` | 생명주기 관리, 취소 전파 |
| `CoroutineName("name")` | `CoroutineName` | 디버깅용 이름 |
| `CoroutineExceptionHandler` | `CoroutineExceptionHandler` | 미처리 예외 핸들러 |

---

#### CoroutineScope — 생명주기 관리의 핵심

`CoroutineScope`는 `CoroutineContext`를 감싸고, 해당 스코프 내 모든 코루틴의 생명주기를 관리합니다.

```kotlin
import kotlinx.coroutines.*

class UserRepository {
    // 이 Repository와 생명주기를 같이 하는 스코프
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun loadUser(id: Int) {
        scope.launch {
            // DB 조회
            delay(100)
            println("사용자 $id 로드 완료")
        }
    }

    fun close() {
        scope.cancel()  // 모든 자식 코루틴 취소
    }
}

fun main() = runBlocking {
    val repo = UserRepository()
    repo.loadUser(1)
    repo.loadUser(2)
    delay(200)
    repo.close()
}
```

**커스텀 스코프 생성:**

```kotlin
import kotlinx.coroutines.*

// Job()을 직접 넘기면 해당 Job으로 취소를 제어할 수 있음
val myScope = CoroutineScope(Dispatchers.Default + Job())

// supervisorScope는 자식 실패가 독립적
val supervisorScopeExample = CoroutineScope(Dispatchers.Default + SupervisorJob())
```

---

#### 예외 전파 구조

코루틴의 예외 전파는 `launch`와 `async`에서 다르게 동작합니다.

**launch에서의 예외:**

`launch`로 시작한 코루틴에서 예외가 발생하면, 예외는 **즉시** 부모 Job으로 전파됩니다. 부모는 취소되고, 다른 자식도 모두 취소됩니다.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    try {
        coroutineScope {
            launch {
                delay(100)
                println("자식 1: 정상 완료")
            }
            launch {
                delay(50)
                throw RuntimeException("자식 2 실패!")
            }
        }
    } catch (e: Exception) {
        println("부모가 받은 예외: ${e.message}")
    }
    // 자식 2가 실패하면 자식 1도 취소됨
}
// 부모가 받은 예외: 자식 2 실패!
```

**async에서의 예외:**

`async`의 예외는 `await()` 호출 시점에 전파됩니다.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val deferred = async {
        delay(100)
        throw RuntimeException("async 실패!")
    }

    try {
        deferred.await()  // 여기서 예외 발생
    } catch (e: Exception) {
        println("await에서 잡힌 예외: ${e.message}")
    }
}
```

---

#### SupervisorJob과 supervisorScope

`SupervisorJob`을 사용하면 자식 코루틴의 실패가 **형제나 부모에게 전파되지 않습니다.** 독립적인 작업들(예: 여러 사용자 데이터 로드, 다수 API 병렬 호출)에 적합합니다.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // supervisorScope: 자식 실패가 다른 자식에 영향 없음
    supervisorScope {
        val child1 = launch {
            delay(100)
            println("자식 1: 정상 완료")
        }

        val child2 = launch {
            delay(50)
            throw RuntimeException("자식 2 실패!")
            // 자식 1은 영향 없이 계속 실행됨
        }

        // child2 실패를 개별적으로 처리
        child2.join()
        println("child2 상태: ${child2.isCancelled}")
    }
    println("supervisorScope 종료")
}
// 자식 2 실패!
// 자식 1: 정상 완료
// child2 상태: true  (예외로 종료된 launch 코루틴은 isCancelled가 true)
// supervisorScope 종료
```

**Job vs SupervisorJob 비교:**

```mermaid
flowchart TD
    subgraph Normal["Job (기본)"]
        P1["부모 Job"] --> C1["자식 1"]
        P1 --> C2["자식 2 (실패)"]
        C2 -->|"예외 전파"| P1
        P1 -->|"취소"| C1
    end

    subgraph Supervisor["SupervisorJob"]
        P2["부모 SupervisorJob"] --> D1["자식 1 (계속 실행)"]
        P2 --> D2["자식 2 (실패)"]
        D2 -->|"전파 차단"| P2
    end
```

---

#### CoroutineExceptionHandler

`launch` 코루틴에서 잡히지 않은 예외를 처리하는 마지막 수단입니다. `async`에서는 동작하지 않습니다(`await` 시점에 예외가 전파되므로).

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { context, exception ->
        val name = context[CoroutineName]?.name ?: "이름없음"
        println("[$name] 예외 처리: ${exception.message}")
    }

    val scope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() + handler
    )

    scope.launch(CoroutineName("작업A")) {
        delay(100)
        throw IllegalStateException("작업A 실패")
    }

    scope.launch(CoroutineName("작업B")) {
        delay(200)
        println("작업B: 정상 완료")
    }

    delay(300)
    scope.cancel()
}
// [작업A] 예외 처리: 작업A 실패
// 작업B: 정상 완료
```

{{< callout type="warning" title="CoroutineExceptionHandler 주의사항" >}}
`CoroutineExceptionHandler`는 **루트 코루틴**에서만 동작합니다. 자식 코루틴에 달아도 예외가 이미 부모로 전파된 후에 처리됩니다. 또한 `async`에서는 `await()` 시 예외가 발생하므로 `try-catch`를 사용하세요.
{{< /callout >}}

---

#### Channel — 코루틴 간 통신

`Channel`은 코루틴 간에 데이터를 주고받는 **안전한 큐**입니다. 여러 생산자-소비자 패턴을 구현할 수 있습니다.

**Channel 종류:**

| 종류 | 생성 방법 | 특징 |
|------|---------|------|
| Rendezvous | `Channel()` | 버퍼 없음. 송신자·수신자가 동시에 준비되어야 함 |
| Buffered | `Channel(capacity)` | 지정 크기의 버퍼. 버퍼 가득 차면 송신 suspend |
| Unlimited | `Channel(UNLIMITED)` | 버퍼 무한. 수신자 없어도 즉시 전달 |
| Conflated | `Channel(CONFLATED)` | 최신 값만 유지. 버퍼 크기 1 + 덮어쓰기 |

**기본 사용:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking {
    val channel = Channel<Int>()

    // 생산자
    launch {
        for (i in 1..5) {
            println("전송: $i")
            channel.send(i)
        }
        channel.close()  // 전송 완료 신호
    }

    // 소비자
    for (value in channel) {  // channel.close() 시 반복 종료
        println("수신: $value")
    }
    println("채널 소비 완료")
}
```

**Buffered Channel:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking {
    val channel = Channel<Int>(capacity = 3)  // 버퍼 3개

    launch {
        for (i in 1..5) {
            channel.send(i)
            println("전송 완료: $i")  // 버퍼 가득 차면 3번에서 suspend
        }
        channel.close()
    }

    delay(100)  // 생산자가 버퍼를 채울 시간
    for (value in channel) {
        delay(50)  // 소비를 느리게
        println("수신: $value")
    }
}
```

---

#### produce와 consumeEach

코루틴 스코프를 활용한 채널 생산자/소비자 패턴입니다.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

// produce: CoroutineScope 내에서 채널 생산자 정의
fun CoroutineScope.numbersProducer(from: Int, to: Int): ReceiveChannel<Int> = produce {
    for (i in from..to) {
        delay(100)
        send(i)
    }
}

fun main() = runBlocking {
    val channel = numbersProducer(1, 5)

    channel.consumeEach { value ->
        println("처리: $value")
    }
}
```

---

#### 팬아웃과 팬인 패턴

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

// 팬아웃: 하나의 채널을 여러 소비자가 나눠 처리
fun main() = runBlocking {
    val channel = Channel<Int>(capacity = 10)

    // 생산자
    launch {
        for (i in 1..10) {
            channel.send(i)
        }
        channel.close()
    }

    // 소비자 3명 (팬아웃)
    repeat(3) { workerId ->
        launch {
            for (value in channel) {
                delay(100)
                println("워커 $workerId: $value 처리")
            }
        }
    }

    delay(2000)
}
```

---

#### select 표현식

`select`는 여러 채널 중 **먼저 준비된 것**을 선택해서 처리합니다.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

fun CoroutineScope.fastChannel(): ReceiveChannel<String> = produce {
    delay(50)
    send("빠른 서버 응답")
}

fun CoroutineScope.slowChannel(): ReceiveChannel<String> = produce {
    delay(200)
    send("느린 서버 응답")
}

fun main() = runBlocking {
    val fast = fastChannel()
    val slow = slowChannel()

    repeat(2) {
        val result = select<String> {
            fast.onReceive { it }
            slow.onReceive { it }
        }
        println("선택됨: $result")
    }

    fast.cancel()
    slow.cancel()
}
// 선택됨: 빠른 서버 응답
// 선택됨: 느린 서버 응답
```

**select with onSend:**

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

fun main() = runBlocking {
    val ch1 = Channel<String>(1)
    val ch2 = Channel<String>(1)

    launch {
        // 먼저 받을 수 있는 채널로 보내기
        select<Unit> {
            ch1.onSend("채널1로 전달") { println("ch1으로 보냄") }
            ch2.onSend("채널2로 전달") { println("ch2로 보냄") }
        }
    }

    delay(100)
    println(ch1.tryReceive().getOrNull() ?: ch2.tryReceive().getOrNull())
}
```

---

#### 코루틴 누수 방지 패턴

코루틴 누수는 취소되지 않은 코루틴이 계속 실행되는 상태입니다.

**누수가 발생하는 패턴:**

```kotlin
import kotlinx.coroutines.*

// 잘못된 예: GlobalScope 사용
// GlobalScope는 앱 전체 생명주기와 연결 → 취소 불가
fun badPattern() {
    GlobalScope.launch {   // 절대 사용하지 마세요!
        delay(10_000)
        println("이 코루틴은 언제 끝날까?")
    }
}
```

**올바른 패턴:**

```kotlin
import kotlinx.coroutines.*

class MyService {
    // SupervisorJob: 자식 실패가 스코프 전체를 취소하지 않음
    private val serviceScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineName("MyService")
    )

    fun start() {
        serviceScope.launch {
            // 서비스 작업
        }
    }

    fun stop() {
        serviceScope.cancel()  // 모든 작업 취소 + 자원 해제
    }
}
```

**Kotlin 공식 권고: `currentCoroutineContext()` 전달:**

```kotlin
import kotlinx.coroutines.*

// 콜백 기반 API를 코루틴으로 래핑할 때
suspend fun <T> awaitCallback(block: (callback: (T) -> Unit) -> Unit): T =
    suspendCancellableCoroutine { continuation ->
        block { result ->
            continuation.resume(result)
        }
        // 취소 시 자원 정리
        continuation.invokeOnCancellation {
            println("코루틴 취소 → 콜백 정리")
        }
    }
```

---

#### suspendCancellableCoroutine — 콜백 래핑

기존 콜백 기반 API를 suspend 함수로 변환할 때 사용합니다.

```kotlin
import kotlinx.coroutines.*

// 예: 콜백 기반 라이브러리 래핑
interface NetworkCallback {
    fun onSuccess(data: String)
    fun onError(error: Exception)
}

fun fetchDataWithCallback(url: String, callback: NetworkCallback) {
    Thread {
        Thread.sleep(200)
        callback.onSuccess("데이터: $url")
    }.start()
}

// suspend 함수로 변환
suspend fun fetchData(url: String): String = suspendCancellableCoroutine { cont ->
    fetchDataWithCallback(url, object : NetworkCallback {
        override fun onSuccess(data: String) {
            if (cont.isActive) cont.resume(data)
        }

        override fun onError(error: Exception) {
            if (cont.isActive) cont.resumeWithException(error)
        }
    })

    cont.invokeOnCancellation {
        println("요청 취소됨: $url")
    }
}

fun main() = runBlocking {
    val result = fetchData("https://example.com/api")
    println(result)
}
```

---

#### 핵심 포인트

{{< callout type="info" title="핵심 정리" >}}
- `CoroutineContext`는 `+`로 Dispatcher, Job, Name, Handler를 조합합니다.
- 기본 `Job`은 자식 실패 → 부모 취소 → 형제 취소 순으로 전파됩니다.
- `SupervisorJob`/`supervisorScope`는 자식 실패를 격리합니다.
- `CoroutineExceptionHandler`는 루트 `launch` 코루틴의 미처리 예외를 처리합니다.
- `Channel`은 타입별로 Rendezvous/Buffered/Unlimited를 선택해서 사용합니다.
- `select { }`로 여러 채널 중 먼저 준비된 것을 처리합니다.
- `GlobalScope` 사용을 피하고 명시적 스코프로 누수를 방지합니다.
{{< /callout >}}

---

#### 다음 단계

- [코루틴 디버깅](../../howto/coroutine-debugging/) — 디버깅 도구, 누수 진단
- [DSL 빌더](../dsl-builders/) — 코루틴 스코프를 활용한 DSL 설계
- [Flow와 비동기 스트림](../flow-async-streams/) — Channel과 SharedFlow 비교
