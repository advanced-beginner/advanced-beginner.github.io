---
bookCollapseSection: true
lastmod: "2026-01-09"
title: 부록
description: "Scala 부록 자료와 참고 문서 목록입니다."
weight: 5
---

Scala 학습에 도움이 되는 참고 자료입니다. 용어 사전, 버전 비교, FAQ, 참고 링크 등 학습 과정에서 필요한 보조 자료들을 모았습니다.

#### 부록 목록

아래 표는 부록에서 제공하는 문서들의 목록입니다. 각 문서는 학습 중 참고용으로 활용하거나, 특정 주제에 대한 빠른 정보를 얻을 때 유용합니다.

| 문서 | 설명 |
|------|------|
| [용어 사전]({{< relref "/docs/scala/appendix/glossary" >}}) | Scala 핵심 용어 정의 |
| [버전 비교]({{< relref "/docs/scala/appendix/version-comparison" >}}) | Scala 2 vs Scala 3 차이점 요약 |
| [FAQ]({{< relref "/docs/scala/appendix/faq" >}}) | 자주 묻는 질문과 답변 |
| [참고 자료]({{< relref "/docs/scala/appendix/references" >}}) | 공식 문서, 서적, 강의 링크 |

#### 빠른 참조

일상적인 Scala 개발에서 자주 사용하는 명령어와 코드 패턴을 모았습니다.

**자주 사용하는 sbt 명령어**

sbt는 Scala의 표준 빌드 도구입니다. 아래 표는 가장 자주 사용하는 명령어들입니다.

| 명령어 | 설명 |
|--------|------|
| `sbt run` | 애플리케이션 실행 |
| `sbt compile` | 컴파일 |
| `sbt test` | 테스트 실행 |
| `sbt console` | REPL 실행 |
| `sbt clean` | 빌드 결과물 삭제 |
| `sbt ~compile` | 파일 변경 시 자동 컴파일 |
| `sbt update` | 의존성 업데이트 |

**주요 타입 변환**

Scala에서 자주 사용되는 타입 변환 패턴입니다. `toIntOption`처럼 Option을 반환하는 메서드를 사용하면 실패 시 예외 대신 None을 받아 안전하게 처리할 수 있습니다.

```scala
// String → Int
"42".toInt           // 42
"42".toIntOption     // Some(42)
"abc".toIntOption    // None

// Int → String
42.toString          // "42"

// 컬렉션 변환
List(1, 2, 3).toSet  // Set(1, 2, 3)
Set(1, 2, 3).toList  // List(1, 2, 3)
```

**Option 다루기**

Option은 null 대신 사용하는 타입 안전한 방법입니다. 다양한 메서드를 통해 값의 존재 여부를 우아하게 처리할 수 있습니다.

```scala
val maybeValue: Option[Int] = Some(42)

// 값 추출
maybeValue.getOrElse(0)      // 42
maybeValue.map(_ * 2)        // Some(84)
maybeValue.filter(_ > 50)    // None
maybeValue.foreach(println)  // 42 출력

// 패턴 매칭
maybeValue match
  case Some(v) => s"값: $v"
  case None    => "값 없음"
```

**컬렉션 주요 연산**

Scala 컬렉션의 핵심 연산들입니다. 함수형 프로그래밍 스타일로 데이터를 변환, 필터링, 집계할 수 있습니다.

```scala
val nums = List(1, 2, 3, 4, 5)

nums.map(_ * 2)          // List(2, 4, 6, 8, 10)
nums.filter(_ % 2 == 0)  // List(2, 4)
nums.reduce(_ + _)       // 15
nums.foldLeft(0)(_ + _)  // 15
nums.find(_ > 3)         // Some(4)
nums.exists(_ > 3)       // true
nums.forall(_ > 0)       // true
nums.take(2)             // List(1, 2)
nums.drop(2)             // List(3, 4, 5)
nums.grouped(2).toList   // List(List(1, 2), List(3, 4), List(5))
```
