---
title: "기본 예제"
description: "Hello Kotlin부터 데이터 클래스, 컬렉션 처리, 확장 함수, 작은 CLI 프로그램까지 Kotlin 핵심 기능을 실습합니다."
lastmod: "2026-05-13"
---

{{< callout type="tip" title="TL;DR" >}}
- `data class`로 간결한 데이터 모델 정의
- `filter`, `map`, `sortedBy` 등 컬렉션 처리 함수 활용
- 확장 함수로 기존 타입에 도메인 특화 기능 추가
- `when`, `?:`, `?.` 등 Kotlin 표현식 적극 활용
{{< /callout >}}

**대상 독자**: Kotlin 환경 설정을 마친 개발자
**선수 지식**: [환경 설정](setup/)

---

설치된 환경에서 Kotlin의 핵심 기능을 직접 실행하며 익힙니다. 각 예제는 `src/main/kotlin/` 아래에 파일을 만들어 실행합니다.

#### Step 1 — Hello Kotlin

가장 간단한 Kotlin 프로그램부터 시작합니다.

```kotlin
// src/main/kotlin/hello/Hello.kt
package hello

fun main() {
    println("안녕하세요, Kotlin!")

    // 변수
    val name = "Kotlin"          // val: 읽기 전용
    var version = "2.0"          // var: 변경 가능

    // 문자열 템플릿
    println("언어: $name $version")
    println("길이: ${name.length}자")

    // 조건식 — if는 표현식
    val message = if (version >= "2.0") "최신 버전" else "구버전"
    println(message)

    // when — switch의 강화 버전
    val score = 85
    val grade = when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        score >= 70 -> "C"
        else -> "F"
    }
    println("점수: $score, 등급: $grade")
}
```

```bash
./gradlew run --args="hello"
# 또는 IntelliJ에서 main() 왼쪽 ▶ 클릭
```

#### Step 2 — 데이터 클래스로 사용자 모델링

`data class`는 `equals`, `hashCode`, `toString`, `copy`를 자동으로 생성합니다.

```kotlin
// src/main/kotlin/model/User.kt
package model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val role: Role = Role.USER   // 기본값
)

enum class Role { USER, ADMIN, MODERATOR }

// 사용 예
fun main() {
    val user1 = User(1, "홍길동", "hong@example.com", 30)
    val user2 = User(2, "김영희", "kim@example.com", 25, Role.ADMIN)

    println(user1)
    // User(id=1, name=홍길동, email=hong@example.com, age=30, role=USER)

    // copy — 일부만 변경한 복사본
    val promoted = user1.copy(role = Role.MODERATOR)
    println(promoted.role)    // MODERATOR

    // 구조 분해
    val (id, name, email) = user1
    println("$id: $name <$email>")

    // 동등성 비교 — 값 기반
    val same = user1.copy()
    println(user1 == same)    // true
}
```

#### Step 3 — 컬렉션 처리

Kotlin 컬렉션은 함수형 스타일로 데이터를 처리합니다.

```kotlin
// src/main/kotlin/collection/CollectionExample.kt
package collection

import model.Role
import model.User

fun main() {
    val users = listOf(
        User(1, "홍길동", "hong@example.com", 30),
        User(2, "김영희", "kim@example.com", 25, Role.ADMIN),
        User(3, "이철수", "lee@example.com", 35),
        User(4, "박지수", "park@example.com", 28, Role.ADMIN),
        User(5, "최민준", "choi@example.com", 22)
    )

    // filter — 조건에 맞는 항목만
    val admins = users.filter { it.role == Role.ADMIN }
    println("관리자: ${admins.map { it.name }}")
    // 관리자: [김영희, 박지수]

    // map — 변환
    val names = users.map { it.name }
    println("이름 목록: $names")

    // sortedBy — 정렬
    val byAge = users.sortedBy { it.age }
    println("나이순: ${byAge.map { "${it.name}(${it.age})" }}")

    // find — 조건에 맞는 첫 항목 (없으면 null)
    val youngest = users.minByOrNull { it.age }
    println("최연소: ${youngest?.name}")

    // groupBy — 그룹화
    val byRole = users.groupBy { it.role }
    byRole.forEach { (role, list) ->
        println("$role: ${list.map { it.name }}")
    }

    // any, all, none — 조건 확인
    println("30대 있음: ${users.any { it.age >= 30 }}")
    println("전원 성인: ${users.all { it.age >= 18 }}")

    // count — 개수
    println("관리자 수: ${users.count { it.role == Role.ADMIN }}")

    // 체이닝 — 여러 연산 연결
    val result = users
        .filter { it.age >= 25 }
        .sortedByDescending { it.age }
        .take(3)
        .map { "${it.name}(${it.age}세)" }
    println("25세 이상 상위 3명: $result")

    // 가변 컬렉션
    val mutableList = mutableListOf<User>()
    mutableList.add(users[0])
    mutableList.addAll(users.filter { it.role == Role.ADMIN })

    // Map 활용
    val userMap: Map<Long, User> = users.associateBy { it.id }
    val found = userMap[2L]
    println("ID 2: ${found?.name}")
}
```

#### Step 4 — 확장 함수 활용

도메인에 특화된 확장 함수를 작성하여 가독성을 높입니다.

```kotlin
// src/main/kotlin/extension/Extensions.kt
package extension

import model.Role
import model.User

// User에 비즈니스 로직 확장
fun User.isAdult() = age >= 18
fun User.hasAdminAccess() = role == Role.ADMIN || role == Role.MODERATOR
fun User.displayName() = "[$role] $name"
fun User.maskEmail(): String {
    val parts = email.split("@")
    return "${parts[0].take(3)}***@${parts[1]}"
}

// List<User>에 도메인 특화 확장
fun List<User>.admins() = filter { it.role == Role.ADMIN }
fun List<User>.averageAge() = map { it.age }.average()
fun List<User>.findByEmail(email: String) = find { it.email == email }

// String 확장
fun String.toSlug() = lowercase().replace(" ", "-").replace("[^a-z0-9-]".toRegex(), "")

fun main() {
    val users = listOf(
        User(1, "홍길동", "hong@example.com", 30),
        User(2, "김영희", "kim@example.com", 25, Role.ADMIN),
        User(3, "이철수", "lee@example.com", 17)
    )

    users.forEach { user ->
        println("${user.displayName()} - 성인: ${user.isAdult()}, 관리자 권한: ${user.hasAdminAccess()}")
        println("  이메일 마스킹: ${user.maskEmail()}")
    }

    println("\n관리자 목록: ${users.admins().map { it.name }}")
    println("평균 나이: ${users.averageAge()}")
    println("이메일 검색: ${users.findByEmail("kim@example.com")?.name}")

    val title = "Hello Kotlin World"
    println("슬러그: ${title.toSlug()}")   // hello-kotlin-world
}
```

#### Step 5 — Null Safety 실습

Kotlin의 null 안전 처리를 실습합니다.

```kotlin
// src/main/kotlin/nullsafety/NullExample.kt
package nullsafety

data class Address(val street: String, val city: String, val zipCode: String?)
data class Person(val name: String, val address: Address?)

fun findPerson(id: Int): Person? {
    return when (id) {
        1 -> Person("홍길동", Address("종로구", "서울", "03000"))
        2 -> Person("김영희", null)    // 주소 없음
        else -> null
    }
}

fun main() {
    // 안전 호출 연산자 ?.
    val person1 = findPerson(1)
    println(person1?.address?.city)    // 서울

    val person2 = findPerson(2)
    println(person2?.address?.city)    // null (예외 없음)

    val person3 = findPerson(999)
    println(person3?.name)             // null

    // 엘비스 연산자 ?:
    val city = findPerson(2)?.address?.city ?: "주소 미등록"
    println(city)   // 주소 미등록

    // let으로 null 안전 처리
    findPerson(1)?.let { person ->
        println("${person.name}의 우편번호: ${person.address?.zipCode ?: "없음"}")
    }

    // requireNotNull, checkNotNull
    val id = System.getenv("USER_ID")?.toIntOrNull() ?: 1
    val user = requireNotNull(findPerson(id)) { "ID $id 사용자를 찾을 수 없습니다" }
    println("사용자: ${user.name}")

    // 스마트 캐스트
    val address = person1?.address
    if (address != null) {
        println(address.street)   // Address 타입으로 스마트 캐스트
    }
}
```

#### Step 6 — 작은 CLI 프로그램

앞서 배운 개념들을 활용한 간단한 사용자 관리 CLI입니다.

```kotlin
// src/main/kotlin/cli/UserCli.kt
package cli

import model.Role
import model.User

class UserManager {
    private val users = mutableListOf<User>()
    private var nextId = 1L

    fun addUser(name: String, email: String, age: Int, role: Role = Role.USER): User {
        val user = User(nextId++, name, email, age, role)
        users.add(user)
        println("사용자 추가: ${user.name} (ID: ${user.id})")
        return user
    }

    fun listUsers() {
        if (users.isEmpty()) {
            println("등록된 사용자가 없습니다.")
            return
        }
        println("\n=== 사용자 목록 (${users.size}명) ===")
        users.sortedBy { it.id }.forEach { user ->
            println("  ${user.id}. [${user.role}] ${user.name} <${user.email}> (${user.age}세)")
        }
    }

    fun search(keyword: String): List<User> {
        return users.filter { it.name.contains(keyword) || it.email.contains(keyword) }
    }

    fun stats() {
        println("\n=== 통계 ===")
        println("  전체: ${users.size}명")
        println("  평균 나이: ${"%.1f".format(users.averageOf { it.age.toDouble() })}세")
        val roleGroups = users.groupBy { it.role }
        roleGroups.forEach { (role, list) ->
            println("  $role: ${list.size}명")
        }
    }

    private fun List<User>.averageOf(selector: (User) -> Double): Double {
        return if (isEmpty()) 0.0 else sumOf(selector) / size
    }
}

fun main() {
    val manager = UserManager()

    // 사용자 추가
    manager.addUser("홍길동", "hong@example.com", 30, Role.ADMIN)
    manager.addUser("김영희", "kim@example.com", 25)
    manager.addUser("이철수", "lee@example.com", 35)
    manager.addUser("박지수", "park@example.com", 28, Role.MODERATOR)

    // 목록 출력
    manager.listUsers()

    // 검색
    val searchResult = manager.search("김")
    println("\n'김' 검색 결과: ${searchResult.map { it.name }}")

    // 통계
    manager.stats()
}
```

실행:

```bash
./gradlew run
```

예상 출력:

```
사용자 추가: 홍길동 (ID: 1)
사용자 추가: 김영희 (ID: 2)
사용자 추가: 이철수 (ID: 3)
사용자 추가: 박지수 (ID: 4)

=== 사용자 목록 (4명) ===
  1. [ADMIN] 홍길동 <hong@example.com> (30세)
  2. [USER] 김영희 <kim@example.com> (25세)
  3. [USER] 이철수 <lee@example.com> (35세)
  4. [MODERATOR] 박지수 <park@example.com> (28세)

'김' 검색 결과: [김영희]

=== 통계 ===
  전체: 4명
  평균 나이: 29.5세
  ADMIN: 1명
  USER: 2명
  MODERATOR: 1명
```

{{< callout type="info" title="핵심 정리" >}}
- `data class` — `toString`, `equals`, `copy`를 자동 생성하는 간결한 모델 정의
- 컬렉션 함수 `filter`, `map`, `groupBy`, `sortedBy` — 선언적 데이터 처리
- 확장 함수 — 기존 타입에 도메인 특화 메서드 추가
- `?.`, `?:`, `?.let { }` — null 안전 처리의 기본 패턴
{{< /callout >}}

#### 다음 단계

- [Spring Boot 연동](spring-boot-integration/) — Kotlin으로 REST API 서버 구축
- [확장 함수](../concepts/extension-functions/) — 확장 함수 원리 심화
