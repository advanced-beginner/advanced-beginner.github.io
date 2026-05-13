---
bookCollapseSection: true
lastmod: "2026-05-13"
title: 실습 예제
description: "Kotlin 실습 예제의 학습 가이드와 문서 목록입니다."
weight: 3
---

이론으로 배운 Kotlin 개념을 직접 실행해보는 예제 모음입니다. 환경 설정부터 Spring Boot·Kafka 연동, 코루틴 실무 적용, Kotlin Multiplatform까지 단계적으로 학습할 수 있도록 설계되었습니다.

#### 예제 목록

다음 표는 제공되는 예제의 목록입니다. 환경 설정부터 시작하여 기본 예제, Spring Boot 연동, Kafka 연동, 코루틴 실무 사용, Multiplatform 미니 프로젝트 순으로 진행하는 것을 권장합니다.

| 예제 | 설명 | 난이도 |
|------|------|--------|
| [환경 설정](setup/) | JDK, Gradle Kotlin DSL, IDE 설정 | 입문 |
| [기본 예제](basic/) | Hello Kotlin, 기본 개념 활용 | 초급 |
| [Spring Boot 연동](spring-boot-integration/) | Kotlin + Spring Boot REST API | 중급 |
| [Kafka 연동](kafka-integration/) | Producer/Consumer를 Kotlin으로 | 중급 |
| [코루틴 실무 사용](coroutines-practical/) | suspend·Flow를 적용한 실제 시나리오 | 중급 |
| [Multiplatform 시작](multiplatform-intro/) | KMP 미니 프로젝트 | 고급 |

#### 예제 프로젝트 구조

예제는 표준 Gradle Kotlin DSL 프로젝트 구조를 따릅니다. 아래는 일반적인 디렉토리 구조입니다.

```
kotlin-example/
├── build.gradle.kts           # 빌드 스크립트 (Kotlin DSL)
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
└── src/
    ├── main/
    │   ├── kotlin/            # Kotlin 소스
    │   └── resources/         # 리소스 (application.yml 등)
    └── test/
        ├── kotlin/            # 테스트 코드
        └── resources/
```

`build.gradle.kts`에는 프로젝트 설정과 의존성이 정의되어 있고, 소스 코드는 `src/main/kotlin/`에 위치합니다. 테스트 코드는 `src/test/kotlin/`에 둡니다.

#### 예제 실행 방법

예제를 실행하려면 먼저 저장소를 클론하고, 원하는 예제 디렉토리로 이동한 후 Gradle 명령을 실행합니다.

**프로젝트 클론**

```bash
git clone https://github.com/advanced-beginner/advanced-beginner.github.io.git
cd advanced-beginner.github.io
```

**Gradle Wrapper로 실행**

대부분의 예제는 `./gradlew` 한 줄로 실행됩니다. 첫 실행 시에는 의존성 다운로드로 인해 시간이 걸릴 수 있습니다.

```bash
./gradlew run
```

**Spring Boot 예제**

Spring Boot 예제는 `bootRun` 태스크로 실행합니다.

```bash
./gradlew bootRun
```

**Kafka 예제**

Kafka 예제는 먼저 Kafka 브로커를 띄워야 합니다. 프로젝트 루트의 `docker/` 디렉토리에 Docker Compose 설정이 있습니다.

```bash
cd docker && docker-compose up -d
cd ..
./gradlew bootRun
```

#### 예제별 학습 포인트

각 예제에서 학습할 수 있는 핵심 개념들을 정리했습니다.

<strong>환경 설정</strong>에서는 Kotlin 개발의 기초가 되는 환경 구성을 다룹니다. JDK 설치, Gradle Kotlin DSL 작성, IntelliJ IDEA 설정을 배웁니다.

<strong>기본 예제</strong>에서는 Kotlin의 핵심 기능들을 실습합니다. 데이터 클래스를 사용한 데이터 모델링, 확장 함수를 활용한 표현력 있는 코드, map·filter·reduce 같은 컬렉션 연산을 익힙니다.

<strong>Spring Boot 연동</strong>에서는 Kotlin과 Spring Boot를 함께 사용하는 방법을 배웁니다. Kotlin 친화적인 의존성, 컨트롤러·서비스 작성법, JPA 엔터티 설계 시 주의점을 다룹니다.

<strong>Kafka 연동</strong>에서는 Kotlin으로 Kafka Producer와 Consumer를 작성합니다. `KafkaTemplate`과 `@KafkaListener`를 활용한 선언적 메시지 처리를 익힙니다.

<strong>코루틴 실무 사용</strong>에서는 `suspend` 함수와 Flow를 실제 시나리오에 적용합니다. 외부 API 병렬 호출, 백프레셔가 있는 스트림 처리, 구조화된 동시성 패턴을 다룹니다.

<strong>Multiplatform 시작</strong>에서는 Kotlin Multiplatform 프로젝트의 기본 구조와 `expect`/`actual` 메커니즘을 배웁니다.

#### 직접 실습하기

예제 코드를 단순히 실행하는 것에서 그치지 않고, 직접 수정하고 실험해보는 것이 중요합니다.

1. `src/main/kotlin/` 아래 파일 수정
2. `./gradlew run` 또는 `./gradlew run --continuous` (자동 재실행)
3. 결과 확인

`--continuous` 플래그를 사용하면 파일이 변경될 때마다 자동으로 다시 컴파일하고 실행합니다.

**추천 실습 과제**

아래 과제들을 직접 구현해보면서 Kotlin 실력을 향상시켜 보세요.

초급 과제로는 리스트에서 짝수만 필터링하고 제곱한 결과를 출력하는 프로그램을 작성해보세요. 또한 데이터 클래스로 `Person(name, age)`을 정의하고 나이순으로 정렬하는 코드를 구현해보세요.

중급 과제로는 `Result<T>`를 활용한 안전한 나눗셈 함수를 구현해보세요. Spring Boot 컨트롤러에서 받은 요청을 코루틴으로 외부 API 두 곳에 병렬 호출한 뒤 응답을 합치는 엔드포인트도 작성해보세요.

고급 과제로는 Flow를 사용한 Kafka 메시지 스트림 처리와 백프레셔 적용을 실험해보세요. 또한 Kotlin Multiplatform으로 JVM과 JS에서 모두 동작하는 공통 모듈을 만들어보세요.
