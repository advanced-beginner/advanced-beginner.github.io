# Scala 3 기본 예제

Scala 3의 기본 기능을 보여주는 예제 프로젝트입니다.

## 요구사항

- Java 11 이상
- sbt 1.9.x

## 실행 방법

```bash
# 프로젝트 디렉토리로 이동
cd examples/scala/scala3-basics

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
6. **Scala 3 새 기능** - `enum`, Extension Methods, Union Types

## Scala 3 특징

이 예제는 Scala 3의 특징적인 문법을 사용합니다:

- **들여쓰기 기반 문법**: 중괄호 대신 들여쓰기 사용
- **새로운 if 문법**: `if x > 5 then ... else ...`
- **enum 키워드**: ADT(Algebraic Data Type) 정의
- **extension 키워드**: 확장 메서드
- **Union Types**: `Int | String`

## 프로젝트 구조

```
scala3-basics/
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

- [Scala 3 공식 문서](https://docs.scala-lang.org/scala3/)
- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html)
