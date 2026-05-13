---
bookCollapseSection: true
lastmod: "2026-05-13"
title: 부록
description: "Kotlin 부록 자료와 참고 문서 목록입니다."
weight: 5
---

Kotlin 학습에 도움이 되는 참고 자료입니다. 용어 사전, 버전 비교, FAQ, 참고 링크 등 학습 과정에서 필요한 보조 자료들을 모았습니다.

#### 부록 목록

아래 표는 부록에서 제공하는 문서들의 목록입니다. 각 문서는 학습 중 참고용으로 활용하거나, 특정 주제에 대한 빠른 정보를 얻을 때 유용합니다.

| 문서 | 설명 |
|------|------|
| [용어 사전](glossary/) | Kotlin 핵심 용어 정의 |
| [버전 비교](version-comparison/) | Kotlin 1.x → 2.x 주요 변경점 요약 |
| [FAQ](faq/) | 자주 묻는 질문과 답변 |
| [참고 자료](references/) | 공식 문서, 서적, 강의 링크 |

#### 빠른 참조

일상적인 Kotlin 개발에서 자주 사용하는 명령어와 코드 패턴을 모았습니다.

**자주 사용하는 Gradle 명령어**

Gradle Kotlin DSL은 Kotlin 프로젝트의 표준 빌드 도구입니다.

| 명령어 | 설명 |
|--------|------|
| `./gradlew run` | 애플리케이션 실행 |
| `./gradlew build` | 컴파일 + 테스트 + 패키징 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew clean` | 빌드 결과물 삭제 |
| `./gradlew bootRun` | Spring Boot 애플리케이션 실행 |
| `./gradlew run --continuous` | 파일 변경 시 자동 실행 |
| `./gradlew dependencies` | 의존성 트리 출력 |

**Null Safety 연산자**

Kotlin의 핵심 안전 장치인 Null 관련 연산자입니다.

```kotlin
val nullable: String? = maybeNull()

// 안전 호출
val length = nullable?.length            // null이면 null 반환

// Elvis 연산자
val safeLength = nullable?.length ?: 0   // null이면 기본값

// 비-null 단언 (지양)
val forced = nullable!!.length           // null이면 NullPointerException

// 안전 캐스팅
val asInt = (any as? Int) ?: -1
```

**스코프 함수 빠른 비교**

| 함수 | 컨텍스트 객체 | 반환값 | 주 용도 |
|------|---------------|--------|---------|
| `let` | `it` | 람다 결과 | nullable 처리, 변환 |
| `run` | `this` | 람다 결과 | 객체 설정 + 결과 계산 |
| `with` | `this` | 람다 결과 | 비-null 객체에 여러 호출 |
| `apply` | `this` | 객체 자신 | 객체 초기화 |
| `also` | `it` | 객체 자신 | 사이드 이펙트 (로깅 등) |

**컬렉션 주요 연산**

Kotlin 컬렉션의 핵심 연산들입니다. 함수형 스타일로 데이터를 변환, 필터링, 집계할 수 있습니다.

```kotlin
val nums = listOf(1, 2, 3, 4, 5)

nums.map { it * 2 }            // [2, 4, 6, 8, 10]
nums.filter { it % 2 == 0 }    // [2, 4]
nums.reduce { acc, n -> acc + n }   // 15
nums.fold(0) { acc, n -> acc + n }  // 15
nums.find { it > 3 }           // 4
nums.any { it > 3 }            // true
nums.all { it > 0 }            // true
nums.take(2)                   // [1, 2]
nums.drop(2)                   // [3, 4, 5]
nums.chunked(2)                // [[1, 2], [3, 4], [5]]
nums.groupBy { it % 2 }        // {1=[1, 3, 5], 0=[2, 4]}
```

**코루틴 빌더 빠른 참조**

```kotlin
import kotlinx.coroutines.*

// runBlocking - 메인 스레드를 막고 코루틴 실행 (테스트, main 함수)
runBlocking {
    delay(1000)
    println("done")
}

// launch - 결과가 필요 없는 비동기 작업 (Job 반환)
val job = CoroutineScope(Dispatchers.IO).launch {
    doWork()
}

// async - 결과가 필요한 비동기 작업 (Deferred 반환)
val deferred = CoroutineScope(Dispatchers.IO).async {
    fetchValue()
}
val result = deferred.await()
```
