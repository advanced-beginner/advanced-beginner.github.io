---
lastmod: "2026-01-06"
title: 실습 예제
weight: 3
---

이론으로 배운 개념을 직접 실행해보는 예제 프로젝트입니다.

## 예제 프로젝트

| 예제 | 설명 | 난이도 |
|------|------|--------|
| [환경 설정](setup/) | sbt, IDE 설정 상세 가이드 | 입문 |
| [기본 예제](basic/) | 핵심 개념 활용 종합 예제 | 초급 |
| [Scala 2 vs 3 비교](scala2-vs-scala3/) | 버전별 코드 비교 | 중급 |

## 예제 프로젝트 구조

```
examples/scala/
├── scala2-basics/          # Scala 2.13 예제
│   ├── build.sbt
│   ├── project/
│   │   └── build.properties
│   └── src/main/scala/
│       └── ...
└── scala3-basics/          # Scala 3 예제
    ├── build.sbt
    ├── project/
    │   └── build.properties
    └── src/main/scala/
        └── ...
```

## 예제 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/kimbenji/advanced-beginner.git
cd advanced-beginner/examples/scala
```

### 2. Scala 3 예제 실행

```bash
cd scala3-basics
sbt run
```

### 3. Scala 2 예제 실행

```bash
cd scala2-basics
sbt run
```

## 예제별 학습 포인트

### 환경 설정
- sbt 프로젝트 구성
- IDE 설정 (IntelliJ, VS Code)
- 자주 사용하는 sbt 명령어

### 기본 예제
- 케이스 클래스로 데이터 모델링
- 패턴 매칭 활용
- 컬렉션 연산 (map, filter, fold)
- 고차 함수 작성

### Scala 2 vs 3 비교
- 문법 차이 (중괄호 vs 들여쓰기)
- implicit → given/using 마이그레이션
- 새로운 enum 문법
- Extension Methods

## 직접 실습하기

예제 코드를 수정하고 실행해보세요:

1. `src/main/scala/` 아래 파일 수정
2. `sbt run` 또는 `sbt ~run` (자동 재실행)
3. 결과 확인

### 추천 실습 과제

**초급:**
- 리스트에서 짝수만 필터링하고 제곱한 결과 출력
- 케이스 클래스로 `Person(name, age)` 정의하고 나이순 정렬

**중급:**
- Option을 사용한 안전한 나눗셈 함수 구현
- For Comprehension으로 두 리스트의 조합 생성

**고급:**
- 타입 클래스로 JSON 직렬화 구현
- Future를 사용한 비동기 데이터 처리
