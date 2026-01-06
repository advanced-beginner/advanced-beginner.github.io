---
title: 부록
weight: 4
---

Scala 학습에 도움이 되는 참고 자료입니다.

## 부록 목록

| 문서 | 설명 |
|------|------|
| [용어 사전](glossary/) | Scala 핵심 용어 정의 |
| [버전 비교](version-comparison/) | Scala 2 vs Scala 3 차이점 요약 |
| [FAQ](faq/) | 자주 묻는 질문과 답변 |
| [참고 자료](references/) | 공식 문서, 서적, 강의 링크 |

## 빠른 참조

### 자주 사용하는 sbt 명령어

| 명령어 | 설명 |
|--------|------|
| `sbt run` | 애플리케이션 실행 |
| `sbt compile` | 컴파일 |
| `sbt test` | 테스트 실행 |
| `sbt console` | REPL 실행 |
| `sbt clean` | 빌드 결과물 삭제 |
| `sbt ~compile` | 파일 변경 시 자동 컴파일 |
| `sbt update` | 의존성 업데이트 |

### 주요 타입 변환

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

### Option 다루기

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

### 컬렉션 주요 연산

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
