---
title: "위임"
description: "Kotlin by 키워드를 활용한 클래스 위임과 프로퍼티 위임(lazy, observable, Map 위임)의 원리와 실무 패턴을 설명합니다."
weight: 11
lastmod: "2026-05-13"
---

## 전체 비유: 비서와 전문가 팀

위임을 **사장이 비서에게 일을 맡기는 구조**에 비유하면 이해하기 쉽습니다.

| 직장 비유 | Kotlin 개념 | 역할 |
|----------|-----------|------|
| 사장 | 위임 클래스 | 인터페이스를 구현하지만 실제 작업은 위임 |
| 비서 | 위임 객체 | 실제 작업 수행 |
| "비서한테 물어봐" | `by` 키워드 | 컴파일러가 위임 코드 자동 생성 |
| 사장이 직접 하는 일 | 오버라이드 | 위임 중 일부만 직접 처리 |
| 필요할 때만 부르는 전문가 | `by lazy` | 처음 사용 시에만 초기화 |
| 변경 사항 알림 서비스 | `Delegates.observable` | 프로퍼티 변경 추적 |

---

> **대상 독자**: Kotlin 기초 클래스/인터페이스를 이해한 개발자
> **선수 지식**: 인터페이스, 프로퍼티, 제네릭 기초
> **소요 시간**: 약 30분
> **이 문서를 읽으면**: `by lazy`, `Delegates.observable`, Map 위임을 자유롭게 사용하고 자체 Delegate를 작성할 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- `class A(b: B) : Interface by b` — 인터페이스 구현을 b에게 위임
- `val x by lazy { ... }` — 최초 접근 시에만 초기화 (스레드 안전)
- `var y by Delegates.observable(초기값) { _, old, new -> }` — 변경 감지
- `val z by map` — Map의 키로 프로퍼티 값 위임
{{< /callout >}}

#### 클래스 위임

인터페이스 구현을 `by` 키워드로 다른 객체에 위임합니다. 컴파일러가 위임 코드를 자동으로 생성하므로 보일러플레이트가 줄어듭니다.

```kotlin
interface Printer {
    fun print(text: String)
    fun printLine(text: String)
}

// 기존 구현체
class ConsolePrinter : Printer {
    override fun print(text: String) = kotlin.io.print(text)
    override fun printLine(text: String) = println(text)
}

// 클래스 위임 — ConsolePrinter에게 Printer 구현을 맡깁니다
class PrefixedPrinter(
    private val prefix: String,
    private val delegate: Printer = ConsolePrinter()
) : Printer by delegate {
    // printLine만 오버라이드 — print()는 delegate에 위임
    override fun printLine(text: String) {
        delegate.printLine("[$prefix] $text")
    }
}

// 사용
val printer = PrefixedPrinter("INFO")
printer.print("Hello")          // Hello  (delegate 호출)
printer.printLine("World")      // [INFO] World  (오버라이드)
```

**클래스 위임의 실무 활용: 데코레이터 패턴**

```kotlin
interface Cache<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun remove(key: K)
    fun clear()
}

class SimpleCache<K, V> : Cache<K, V> {
    private val store = mutableMapOf<K, V>()
    override fun get(key: K) = store[key]
    override fun put(key: K, value: V) { store[key] = value }
    override fun remove(key: K) { store.remove(key) }
    override fun clear() = store.clear()
}

// 로깅 데코레이터 — 위임으로 간결하게 구현
class LoggingCache<K, V>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
    override fun put(key: K, value: V) {
        println("캐시 저장: $key")
        delegate.put(key, value)
    }

    override fun get(key: K): V? {
        val value = delegate.get(key)
        println("캐시 ${if (value != null) "히트" else "미스"}: $key")
        return value
    }
}

// 사용
val cache: Cache<String, String> = LoggingCache(SimpleCache())
cache.put("user:1", "홍길동")   // 캐시 저장: user:1
cache.get("user:1")             // 캐시 히트: user:1
cache.get("user:2")             // 캐시 미스: user:2
```

#### 프로퍼티 위임

`val`/`var` 프로퍼티의 get/set 동작을 다른 객체에 위임합니다. `by` 뒤의 객체가 `getValue` (와 `setValue`)를 제공하면 됩니다.

```kotlin
// 프로퍼티 위임 기본 구조
class MyDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "위임된 값 (${property.name})"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("${property.name}에 $value 저장")
    }
}

class Example {
    val name: String by MyDelegate()
    var title: String by MyDelegate()
}
```

#### by lazy

**가장 많이 쓰이는 위임**입니다. 최초 접근 시에만 초기화 블록을 실행하고, 이후에는 캐시된 값을 반환합니다. 기본 모드(`LazyThreadSafetyMode.SYNCHRONIZED`)는 스레드 안전합니다.

```kotlin
class HeavyService {
    val client by lazy {
        println("클라이언트 초기화 중...")
        createExpensiveHttpClient()         // 비용이 큰 초기화
    }

    val config by lazy {
        loadConfigFromFile("config.yaml")   // 파일 읽기
    }
}

val service = HeavyService()
// 여기까지는 초기화 안 됨
println("서비스 생성 완료")
service.client.get("/api/health")   // 이 시점에 초기화 실행
service.client.get("/api/users")    // 캐시된 값 사용
```

**lazy 모드 선택**

| 모드 | 설명 | 언제 사용 |
|------|------|---------|
| `SYNCHRONIZED` (기본) | 잠금으로 스레드 안전 | 멀티스레드 환경 |
| `PUBLICATION` | 여러 스레드가 초기화 가능, 최초 값만 사용 | 초기화 비용 낮을 때 |
| `NONE` | 동기화 없음, 빠름 | 단일 스레드 확실할 때 |

```kotlin
val lazyValue by lazy(LazyThreadSafetyMode.NONE) {
    expensiveComputation()   // 단일 스레드에서만 사용
}
```

#### Delegates.observable

프로퍼티 값이 변경될 때마다 콜백을 실행합니다. UI 바인딩, 이벤트 로깅, 유효성 검사에 유용합니다.

```kotlin
import kotlin.properties.Delegates

class UserProfile {
    var name: String by Delegates.observable("") { property, oldValue, newValue ->
        println("${property.name}: '$oldValue' → '$newValue'")
        // 변경 알림, 유효성 검사 등
    }

    var age: Int by Delegates.observable(0) { _, old, new ->
        if (new < 0) throw IllegalArgumentException("나이는 음수일 수 없습니다")
    }
}

val profile = UserProfile()
profile.name = "홍길동"     // name: '' → '홍길동'
profile.name = "김철수"     // name: '홍길동' → '김철수'
```

**vetoable — 변경 거부 가능**

```kotlin
import kotlin.properties.Delegates

class BoundedValue {
    var score: Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue in 0..100   // true면 변경, false면 거부
    }
}

val bv = BoundedValue()
bv.score = 85
println(bv.score)   // 85
bv.score = 150
println(bv.score)   // 85 — 거부됨
```

#### Delegates.notNull

`lateinit`의 대안으로, 기본 타입(`Int`, `Boolean` 등)이나 nullable 타입에서도 사용할 수 있습니다.

```kotlin
import kotlin.properties.Delegates

class Config {
    var maxRetries: Int by Delegates.notNull()
    var timeout: Long by Delegates.notNull()
}

val config = Config()
// config.maxRetries   // 초기화 전 접근 시 IllegalStateException
config.maxRetries = 3
config.timeout = 5000L
println("최대 재시도: ${config.maxRetries}")
```

#### Map 위임

`Map`(또는 `MutableMap`)을 사용해 프로퍼티 값을 저장합니다. JSON 파싱, 설정 로딩, 동적 속성에 유용합니다.

```kotlin
class UserSettings(private val map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
    val email: String by map
}

// JSON 파싱 결과를 Map으로 받았을 때
val settings = UserSettings(mapOf(
    "name" to "홍길동",
    "age" to 30,
    "email" to "hong@example.com"
))

println(settings.name)   // 홍길동
println(settings.age)    // 30

// MutableMap으로 읽기/쓰기
class MutableSettings(private val map: MutableMap<String, Any?>) {
    var theme: String by map
    var language: String by map
}

val ms = MutableSettings(mutableMapOf(
    "theme" to "dark",
    "language" to "ko"
))
ms.theme = "light"
println(ms.theme)        // light
```

#### 자체 Delegate 작성

`ReadOnlyProperty` 또는 `ReadWriteProperty` 인터페이스를 구현하거나, `getValue`/`setValue` 연산자 함수를 정의하면 됩니다.

```kotlin
import kotlin.reflect.KProperty

// 읽기 전용 Delegate — 값을 암호화하여 저장
class EncryptedDelegate<T>(private val encrypt: (T) -> String,
                           private val decrypt: (String) -> T,
                           initialValue: T) {
    private var stored: String = encrypt(initialValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return decrypt(stored)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        stored = encrypt(value)
        println("${property.name} 암호화 저장 완료")
    }
}

// 사용 예
fun simpleEncrypt(value: String) = value.reversed()
fun simpleDecrypt(value: String) = value.reversed()

class SecureData {
    var password: String by EncryptedDelegate(
        encrypt = ::simpleEncrypt,
        decrypt = ::simpleDecrypt,
        initialValue = ""
    )
}

val data = SecureData()
data.password = "secret123"    // password 암호화 저장 완료
println(data.password)         // secret123
```

**재사용 가능한 유효성 검사 Delegate**

```kotlin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Validated<T>(
    initialValue: T,
    private val validator: (T) -> Boolean,
    private val errorMessage: String
) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        require(validator(value)) { errorMessage }
        this.value = value
    }
}

// 확장 함수로 편리하게 사용
fun <T> validated(
    initialValue: T,
    validator: (T) -> Boolean,
    errorMessage: String
) = Validated(initialValue, validator, errorMessage)

class Order {
    var quantity: Int by validated(
        initialValue = 1,
        validator = { it > 0 },
        errorMessage = "수량은 1 이상이어야 합니다"
    )

    var price: Long by validated(
        initialValue = 0L,
        validator = { it >= 0 },
        errorMessage = "가격은 음수일 수 없습니다"
    )
}

val order = Order()
order.quantity = 5
order.price = 10_000L
// order.quantity = -1  // IllegalArgumentException
```

{{< callout type="info" title="핵심 정리" >}}
- `class A(b: B) : Interface by b` — 인터페이스 구현을 위임하고, 필요한 메서드만 오버라이드
- `by lazy { }` — 최초 접근 시 초기화, 기본 모드는 스레드 안전
- `Delegates.observable` — 프로퍼티 변경 콜백, `vetoable`은 변경 거부 가능
- `Delegates.notNull` — 기본 타입에도 지연 초기화 적용
- Map 위임 — 동적 속성이나 설정 로딩에 활용
- 자체 Delegate는 `getValue`/`setValue` 연산자 함수 구현으로 작성
{{< /callout >}}

#### 다음 단계

- [인라인/Reified](inline-reified/) — 프로퍼티 Delegate가 inline으로 최적화되는 원리
- [확장 함수](extension-functions/) — Delegate와 함께 쓰이는 확장 함수 패턴
