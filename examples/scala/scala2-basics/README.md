# Scala 2 기본 예제

Scala 2.13의 기본 기능을 보여주는 예제 프로젝트입니다.

## 요구사항

- Java 8 이상
- sbt 1.9.x

## 실행 방법

```bash
# 프로젝트 디렉토리로 이동
cd examples/scala/scala2-basics

# 실행
sbt run

# 또는 자동 재컴파일 모드
sbt ~run
```

## 예제 내용

1. **변수와 타입** - `val`, `var`, 타입 추론
2. **제어 구조** - `if`, `match`, `for` 표현식
3. **함수** - 기본 함수, 기본값, 람다, 고차 함수
4. **케이스 클래스** - 불변 데이터, `copy`, 패턴 매칭
5. **컬렉션** - List, Map, `map`, `filter`, `reduce`
6. **Implicit** - implicit 값, implicit class, implicit 변환

## Scala 2 특징

이 예제는 Scala 2의 전통적인 문법을 사용합니다:

- **중괄호 기반 문법**: 블록을 `{}`로 감쌈
- **전통적인 if 문법**: `if (x > 5) ... else ...`
- **sealed trait + case object**: ADT(열거형) 정의
- **implicit 키워드**: 암시적 값, 클래스, 변환

## Scala 3와의 주요 차이점

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 블록 구문 | `{ }` 필수 | 들여쓰기 옵션 |
| if 조건 | `if (cond)` | `if cond then` |
| 열거형 | `sealed trait` + `case object` | `enum` |
| 확장 메서드 | `implicit class` | `extension` |
| 암시적 값 | `implicit val` | `given` |
| 암시적 매개변수 | `(implicit x: T)` | `(using x: T)` |

## 프로젝트 구조

```
scala2-basics/
├── build.sbt                 # sbt 빌드 설정
├── project/
│   └── build.properties      # sbt 버전
├── src/
│   └── main/
│       └── scala/
│           └── Main.scala    # 메인 예제
└── README.md
```

## 참고 자료

- [Scala 2 공식 문서](https://docs.scala-lang.org/)
- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html)
