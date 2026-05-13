---
title: "DSL 빌더"
description: "람다 with receiver, 타입 세이프 빌더 패턴, @DslMarker로 컨텍스트를 격리하고 Kotlin DSL을 설계하는 방법을 설명합니다."
weight: 16
lastmod: "2026-05-13"
---

## 전체 비유: 레고 조립 설명서

레고 조립 설명서는 특정 규칙 안에서만 블록을 끼울 수 있습니다. 성 세트를 조립할 때는 성 블록만, 우주선 세트를 조립할 때는 우주선 블록만 맞습니다. `@DslMarker`가 바로 이 "세트 구분 규칙"입니다.

| 레고 비유 | Kotlin DSL | 역할 |
|---------|-----------|------|
| 조립 설명서 블록 | 람다 with receiver | 블록 내부에서 수신 객체의 메서드에 접근 |
| 세트 구분 스티커 | `@DslMarker` | 서로 다른 DSL 컨텍스트 혼용 방지 |
| 블록 연결 포인트 | 빌더 함수 | 부모 DSL에서 자식 DSL을 시작하는 함수 |
| 완성된 레고 모형 | 빌더 결과 객체 | 불변 데이터 구조 또는 설정 객체 |

---

> **대상 독자**: Kotlin 람다, 확장 함수, 스코프 함수를 이해한 중급 이상 개발자
> **선수 지식**: 확장 함수(`fun Type.method()`), 람다, 고차 함수
> **소요 시간**: 약 30~40분
> **이 문서를 읽으면**: 타입 세이프 빌더를 직접 설계하고, `@DslMarker`로 DSL 오용을 컴파일 타임에 막을 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- `A.() -> Unit` (람다 with receiver)은 람다 내부에서 `A`의 멤버를 `this` 없이 호출할 수 있게 합니다.
- 타입 세이프 빌더는 람다 with receiver를 중첩하여 계층적 DSL을 만듭니다.
- `@DslMarker`로 주석된 어노테이션을 DSL 클래스에 붙이면, 내부 람다에서 외부 DSL의 메서드를 실수로 호출하는 것을 방지합니다.
- 실제 사용: `buildString`, `buildList`, Gradle Kotlin DSL, Spring Boot Kotlin Router DSL.
{{< /callout >}}

---

#### 왜 DSL이 필요한가?

프로그래밍 언어로 **특정 도메인의 문법**을 표현할 때 DSL(Domain-Specific Language)이 유용합니다. XML이나 JSON으로 설정을 표현하는 대신, Kotlin 코드로 타입 안전하고 IDE 자동완성이 지원되는 설정을 작성할 수 있습니다.

```kotlin
// XML 방식 (타입 안전 없음, 오타 → 런타임 오류)
// <server port="8080"><database url="jdbc:..." /></server>

// Kotlin DSL 방식 (컴파일 타임 검증, 자동완성 지원)
server {
    port = 8080
    database {
        url = "jdbc:postgresql://localhost:5432/mydb"
    }
}
```

---

#### 람다 with receiver 이해하기

일반 람다와 람다 with receiver의 차이를 먼저 이해합니다.

**일반 람다:**

```kotlin
// (Int, Int) -> Int : 파라미터 2개, Int 반환
val add: (Int, Int) -> Int = { a, b -> a + b }
```

**람다 with receiver:**

```kotlin
// Int.() -> Int : Int가 수신자, Int 반환
// 람다 내부에서 this(Int)의 멤버에 바로 접근
val double: Int.() -> Int = { this * 2 }

println(5.double())  // 10
println(3.double())  // 6
```

`this`를 생략할 수 있어서 더욱 자연스럽습니다:

```kotlin
val describe: Int.() -> String = {
    when {
        this < 0 -> "음수"
        this == 0 -> "영"
        else -> "양수"
    }
}

println((-3).describe())  // 음수
println(0.describe())     // 영
println(7.describe())     // 양수
```

---

#### 타입 세이프 빌더 패턴

빌더 클래스와 람다 with receiver를 조합하면 계층적 DSL을 만들 수 있습니다.

**단계 1: 빌더 클래스 정의**

```kotlin
// 최종 결과 데이터 클래스
data class ServerConfig(
    val host: String,
    val port: Int,
    val maxConnections: Int
)

// 빌더 클래스
class ServerConfigBuilder {
    var host: String = "localhost"
    var port: Int = 8080
    var maxConnections: Int = 100

    fun build(): ServerConfig = ServerConfig(host, port, maxConnections)
}
```

**단계 2: DSL 진입 함수 정의**

```kotlin
// 람다 with receiver를 받는 최상위 함수
fun server(block: ServerConfigBuilder.() -> Unit): ServerConfig {
    val builder = ServerConfigBuilder()
    builder.block()       // 빌더를 수신자로 람다 실행
    return builder.build()
}
```

**단계 3: DSL 사용**

```kotlin
val config: ServerConfig = server {
    host = "0.0.0.0"      // this.host = "0.0.0.0"와 동일
    port = 9090
    maxConnections = 500
}

println(config)
// ServerConfig(host=0.0.0.0, port=9090, maxConnections=500)
```

---

#### 중첩 DSL 구조

실제 DSL은 여러 레벨로 중첩됩니다. 데이터베이스 설정을 추가하는 예제를 봅니다.

```kotlin
data class DatabaseConfig(
    val url: String,
    val username: String,
    val password: String,
    val poolSize: Int
)

data class AppConfig(
    val server: ServerConfig,
    val database: DatabaseConfig
)

class DatabaseBuilder {
    var url: String = ""
    var username: String = "root"
    var password: String = ""
    var poolSize: Int = 10

    fun build() = DatabaseConfig(url, username, password, poolSize)
}

class AppConfigBuilder {
    private var serverConfig: ServerConfig = ServerConfig("localhost", 8080, 100)
    private var dbConfig: DatabaseConfig = DatabaseConfig("", "root", "", 10)

    fun server(block: ServerConfigBuilder.() -> Unit) {
        serverConfig = ServerConfigBuilder().apply(block).build()
    }

    fun database(block: DatabaseBuilder.() -> Unit) {
        dbConfig = DatabaseBuilder().apply(block).build()
    }

    fun build() = AppConfig(serverConfig, dbConfig)
}

fun application(block: AppConfigBuilder.() -> Unit): AppConfig {
    return AppConfigBuilder().apply(block).build()
}

// 사용
val appConfig = application {
    server {
        host = "0.0.0.0"
        port = 8080
    }
    database {
        url = "jdbc:postgresql://localhost:5432/mydb"
        username = "admin"
        poolSize = 20
    }
}
```

---

#### @DslMarker — 컨텍스트 격리

중첩 DSL에서 외부 DSL의 메서드를 내부 DSL에서 실수로 호출할 수 있습니다. `@DslMarker`로 이를 방지합니다.

**문제 상황:**

```kotlin
// @DslMarker 없이는 이런 실수가 발생할 수 있음
application {
    server {
        database {  // database는 AppConfigBuilder의 메서드
            // 내부에서 실수로 외부 DSL의 메서드 호출 가능
        }
        host = "0.0.0.0"  // 의도: server 블록의 host
    }
}
```

**@DslMarker 적용:**

```kotlin
// 1. 마커 어노테이션 정의
@DslMarker
annotation class AppDsl

// 2. 모든 DSL 빌더 클래스에 마커 어노테이션 부착
@AppDsl
class AppConfigBuilder { /* ... */ }

@AppDsl
class ServerConfigBuilder { /* ... */ }

@AppDsl
class DatabaseBuilder { /* ... */ }
```

이제 내부 람다에서 외부 스코프의 메서드를 호출하면 **컴파일 오류**가 발생합니다:

```kotlin
application {
    server {
        host = "0.0.0.0"   // OK: ServerConfigBuilder의 host
        // database { }    // 컴파일 오류! database는 AppConfigBuilder 소속
    }
    database {
        url = "jdbc:..."   // OK: DatabaseBuilder의 url
    }
}
```

---

#### 실전 예제: HTML 빌더 미니 버전

```kotlin
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class Tag(val name: String) {
    private val children = mutableListOf<Tag>()
    private val attributes = mutableMapOf<String, String>()
    var text: String = ""

    fun attr(key: String, value: String) {
        attributes[key] = value
    }

    fun tag(name: String, block: Tag.() -> Unit): Tag {
        val child = Tag(name).apply(block)
        children.add(child)
        return child
    }

    fun render(indent: Int = 0): String = buildString {
        val pad = "  ".repeat(indent)
        val attrs = attributes.entries.joinToString(" ") { (k, v) -> "$k=\"$v\"" }
        val attrStr = if (attrs.isNotEmpty()) " $attrs" else ""
        append("$pad<$name$attrStr>")

        if (text.isNotEmpty()) {
            append(text)
            append("</$name>")
        } else {
            children.forEach { append("\n${it.render(indent + 1)}") }
            if (children.isNotEmpty()) append("\n$pad")
            append("</$name>")
        }
    }
}

fun html(block: Tag.() -> Unit): Tag = Tag("html").apply(block)

fun Tag.head(block: Tag.() -> Unit) = tag("head", block)
fun Tag.body(block: Tag.() -> Unit) = tag("body", block)
fun Tag.div(block: Tag.() -> Unit) = tag("div", block)
fun Tag.p(block: Tag.() -> Unit) = tag("p", block)
fun Tag.h1(block: Tag.() -> Unit) = tag("h1", block)
fun Tag.a(href: String, block: Tag.() -> Unit) = tag("a") {
    attr("href", href)
    block()
}

// 사용
fun main() {
    val page = html {
        head {
            tag("title") { text = "Kotlin DSL 예제" }
        }
        body {
            h1 { text = "환영합니다" }
            div {
                attr("class", "content")
                p { text = "Kotlin DSL로 HTML을 생성했습니다." }
                a("https://kotlinlang.org") {
                    text = "Kotlin 공식 사이트"
                }
            }
        }
    }

    println(page.render())
}
```

출력:
```html
<html>
  <head>
    <title>Kotlin DSL 예제</title>
  </head>
  <body>
    <h1>환영합니다</h1>
    <div class="content">
      <p>Kotlin DSL로 HTML을 생성했습니다.</p>
      <a href="https://kotlinlang.org">Kotlin 공식 사이트</a>
    </div>
  </body>
</html>
```

---

#### 실전 예제: Spring Boot Kotlin Router DSL 미니 버전

Spring Boot의 Kotlin Router DSL과 유사한 패턴입니다.

```kotlin
// 요청/응답 시뮬레이션
data class Request(val method: String, val path: String)
data class Response(val status: Int, val body: String)

typealias Handler = (Request) -> Response

@DslMarker
annotation class RouterDsl

@RouterDsl
class RouterBuilder {
    private val routes = mutableListOf<Pair<Pair<String, String>, Handler>>()

    fun GET(path: String, handler: Handler) {
        routes.add(Pair("GET", path) to handler)
    }

    fun POST(path: String, handler: Handler) {
        routes.add(Pair("POST", path) to handler)
    }

    fun handle(request: Request): Response {
        val route = routes.find { (key, _) ->
            key.first == request.method && key.second == request.path
        }
        return route?.second?.invoke(request)
            ?: Response(404, "Not Found")
    }
}

fun router(block: RouterBuilder.() -> Unit): RouterBuilder =
    RouterBuilder().apply(block)

// 사용
fun main() {
    val app = router {
        GET("/health") {
            Response(200, """{"status":"UP"}""")
        }

        GET("/users") {
            Response(200, """[{"id":1,"name":"Alice"}]""")
        }

        POST("/users") { req ->
            Response(201, """{"message":"사용자 생성됨"}""")
        }
    }

    println(app.handle(Request("GET", "/health")))
    println(app.handle(Request("GET", "/users")))
    println(app.handle(Request("GET", "/unknown")))
}
```

---

#### 표준 라이브러리 DSL 활용

Kotlin 표준 라이브러리에도 DSL 패턴이 내장되어 있습니다.

```kotlin
// buildString: StringBuilderScope
val result = buildString {
    append("Hello")
    append(", ")
    append("World")
    appendLine("!")
    repeat(3) { append("Kotlin ") }
}
println(result)

// buildList: 불변 리스트 빌더
val numbers = buildList {
    add(1)
    addAll(listOf(2, 3, 4))
    if (true) add(5)
}

// buildMap
val config = buildMap<String, Any> {
    put("host", "localhost")
    put("port", 8080)
    putAll(mapOf("debug" to true, "timeout" to 30))
}
```

---

#### DSL 설계 체크리스트

{{< callout type="info" title="좋은 DSL 설계 원칙" >}}
- **최소 원칙**: DSL은 필요한 것만 노출합니다. `internal`이나 `private`으로 내부 구현을 숨깁니다.
- **컴파일 타임 검증**: 잘못된 조합은 런타임이 아니라 컴파일 시 발견되어야 합니다.
- **`@DslMarker` 적용**: 중첩 DSL에서는 반드시 컨텍스트를 격리합니다.
- **불변 결과**: 빌더가 만드는 객체는 `data class`처럼 불변으로 설계합니다.
- **기본값 제공**: 모든 옵션에 합리적인 기본값을 제공하여 최소한의 설정으로 동작하게 합니다.
{{< /callout >}}

---

#### 핵심 포인트

{{< callout type="info" title="핵심 정리" >}}
- `A.() -> Unit`은 람다 내부에서 `A`의 멤버를 `this` 없이 호출하는 타입입니다.
- 빌더 클래스 + 람다 with receiver = 타입 세이프 빌더 패턴의 기본 구조입니다.
- `@DslMarker`는 중첩 DSL에서 외부 스코프 메서드 호출을 컴파일 타임에 막습니다.
- `buildString`, `buildList`, `buildMap`은 표준 라이브러리의 DSL 예시입니다.
{{< /callout >}}

---

#### 다음 단계

- [Multiplatform 개요](../multiplatform-overview/) — KMP에서 expect/actual로 플랫폼별 DSL 구현
- [인라인/Reified](../inline-reified/) — DSL 성능 최적화에 활용되는 inline 함수
- [Gradle Kotlin DSL 팁](../../howto/gradle-kotlin-dsl-tips/) — 실무 빌드 스크립트 DSL
