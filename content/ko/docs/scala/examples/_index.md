---
bookCollapseSection: true
lastmod: "2026-01-09"
title: 실습 예제
weight: 3
---

이론으로 배운 개념을 직접 실행해보는 예제 프로젝트입니다. Scala의 핵심 개념들을 실제 코드로 작성하고 실행해보면서 언어에 대한 이해를 깊게 할 수 있습니다. 각 예제는 난이도별로 구성되어 있으며, 입문자부터 고급 사용자까지 단계적으로 학습할 수 있도록 설계되었습니다.

#### 예제 프로젝트

다음 표는 제공되는 예제 프로젝트의 목록입니다. 환경 설정부터 시작하여 기본 예제, 버전 비교, Spark 연동, 실무 프로젝트까지 순차적으로 진행하는 것을 권장합니다. 각 예제는 이전 예제에서 학습한 개념을 기반으로 하므로, 순서대로 학습하면 더욱 효과적입니다.

| 예제 | 설명 | 난이도 |
|------|------|--------|
| [환경 설정](setup/) | sbt, IDE 설정 상세 가이드 | 입문 |
| [기본 예제](basic/) | 핵심 개념 활용 종합 예제 | 초급 |
| [Scala 2 vs 3 비교](scala2-vs-scala3/) | 버전별 코드 비교 | 중급 |
| [Spark 연동](spark-integration/) | Apache Spark와 Scala 활용 | 중급 |
| [실무 프로젝트](practical-project/) | REST API, 데이터 파이프라인 | 고급 |

#### 예제 프로젝트 구조

예제 프로젝트는 Scala 2와 Scala 3 버전으로 분리되어 있습니다. 각 버전별로 독립적인 sbt 프로젝트로 구성되어 있어서, 원하는 버전의 예제만 선택적으로 실행할 수 있습니다. 아래는 전체 디렉토리 구조입니다.

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

두 프로젝트 모두 표준 sbt 디렉토리 구조를 따릅니다. `build.sbt`에는 프로젝트 설정과 의존성이 정의되어 있고, `project/build.properties`에는 사용할 sbt 버전이 명시되어 있습니다. 소스 코드는 `src/main/scala/` 디렉토리에 위치합니다.

#### 예제 실행 방법

예제를 실행하려면 먼저 저장소를 클론하고, 원하는 예제 디렉토리로 이동한 후 sbt 명령어를 실행합니다. 아래에서 각 단계를 자세히 설명합니다.

**프로젝트 클론**

먼저 GitHub에서 프로젝트를 클론합니다. 이미 클론한 경우에는 이 단계를 건너뛰어도 됩니다.

```bash
git clone https://github.com/advanced-beginner/advanced-beginner.github.io.git
cd advanced-beginner/examples/scala
```

**Scala 3 예제 실행**

Scala 3 예제를 실행하려면 `scala3-basics` 디렉토리로 이동하여 sbt run 명령을 실행합니다. 첫 실행 시에는 의존성 다운로드로 인해 시간이 걸릴 수 있습니다.

```bash
cd scala3-basics
sbt run
```

**Scala 2 예제 실행**

Scala 2.13 예제를 실행하려면 `scala2-basics` 디렉토리에서 동일하게 sbt run을 실행합니다. Spark를 사용하는 프로젝트의 경우 Scala 2 버전이 필요합니다.

```bash
cd scala2-basics
sbt run
```

#### 예제별 학습 포인트

각 예제에서 학습할 수 있는 핵심 개념들을 정리했습니다. 예제를 실행하기 전에 어떤 내용을 다루는지 미리 파악하면 학습 효과가 높아집니다.

**환경 설정**에서는 Scala 개발의 기초가 되는 환경 구성을 다룹니다. sbt 프로젝트 구성 방법, IntelliJ IDEA나 VS Code 같은 IDE 설정, 그리고 일상적으로 자주 사용하는 sbt 명령어들을 배웁니다.

**기본 예제**에서는 Scala의 핵심 기능들을 실습합니다. 케이스 클래스를 사용한 데이터 모델링, 패턴 매칭을 활용한 분기 처리, map, filter, fold 같은 컬렉션 연산, 그리고 고차 함수 작성법을 익힙니다.

**Scala 2 vs 3 비교**에서는 두 버전 간의 차이점을 코드로 직접 비교합니다. 중괄호 vs 들여쓰기 문법, implicit에서 given/using으로의 마이그레이션, 새로운 enum 문법, Extension Methods 등을 다룹니다.

#### 직접 실습하기

예제 코드를 단순히 실행하는 것에서 그치지 않고, 직접 수정하고 실험해보는 것이 중요합니다. 다음 단계를 따라 직접 코드를 수정하고 결과를 확인해보세요.

1. `src/main/scala/` 아래 파일 수정
2. `sbt run` 또는 `sbt ~run` (자동 재실행)
3. 결과 확인

`sbt ~run` 명령을 사용하면 파일이 변경될 때마다 자동으로 다시 컴파일하고 실행합니다. 코드를 수정하면서 즉시 결과를 확인할 수 있어 실습에 편리합니다.

**추천 실습 과제**

아래 과제들을 직접 구현해보면서 Scala 실력을 향상시켜 보세요.

초급 과제로는 리스트에서 짝수만 필터링하고 제곱한 결과를 출력하는 프로그램을 작성해보세요. 또한 케이스 클래스로 `Person(name, age)`을 정의하고 나이순으로 정렬하는 코드를 구현해보세요.

중급 과제로는 Option을 사용한 안전한 나눗셈 함수를 구현해보세요. 0으로 나누는 경우 None을 반환하도록 합니다. For Comprehension을 사용하여 두 리스트의 모든 조합을 생성하는 코드도 작성해보세요.

고급 과제로는 타입 클래스를 활용한 JSON 직렬화를 구현해보세요. 다양한 타입에 대해 확장 가능한 인코더를 만들어봅니다. Future를 사용한 비동기 데이터 처리 코드도 작성해보세요.

