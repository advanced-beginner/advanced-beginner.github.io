---
title: "환경 설정 심화"
description: "build.gradle.kts 핵심 옵션, 의존성 패턴, IntelliJ 단축키, ktlint 통합 등 Quick Start 이후 알아두면 좋은 환경 설정 심화 내용을 다룹니다."
weight: 1
lastmod: "2026-05-13"
---

> **소요 시간**: 약 10분

{{< callout type="tip" title="TL;DR" >}}
- `build.gradle.kts`의 핵심 블록(plugins, dependencies, kotlin, application) 의미 정리
- `jvmToolchain(17)` vs `sourceCompatibility` 차이 이해
- IntelliJ IDEA 단축키와 ktlint 통합으로 협업 환경 정비
- 트러블슈팅 패턴(클래스 버전, 의존성 갱신, 캐시 무효화)
{{< /callout >}}

**대상 독자**: Kotlin 환경 설정의 기본을 끝낸 개발자
**선수 지식**: [Quick Start](../../quick-start/) 완료 (JDK 17 설치, `gradle init`, 첫 `./gradlew run` 실행까지)

{{< callout type="info" title="기본 설치는 Quick Start에서" >}}
JDK 17 설치, `gradle init`으로 Kotlin DSL 프로젝트 생성, IntelliJ IDEA 처음 열기 등 첫 환경 구축은 [Quick Start](../../quick-start/)에서 다룹니다. 이 페이지는 그 다음 단계 — 빌드 스크립트의 핵심 옵션과 협업에 필요한 추가 설정을 정리합니다.
{{< /callout >}}

---

#### Step 1 — build.gradle.kts 핵심 설정

Kotlin DSL로 작성된 빌드 스크립트입니다. 핵심 옵션을 이해합니다.

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.0"   // Kotlin JVM 플러그인
    application                      // 실행 가능한 애플리케이션
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // 표준 라이브러리 (kotlin("jvm") 플러그인이 자동 추가)
    // implementation(kotlin("stdlib"))

    // 테스트 — JUnit 5
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)   // JDK 17 사용 — JAVA_HOME 없이도 자동 설정
}

application {
    mainClass.set("com.example.AppKt")  // 메인 클래스 (파일명 + Kt 접미사)
}
```

**의존성 추가 패턴**

```kotlin
dependencies {
    // 런타임 의존성
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // 테스트 전용
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.11")

    // 컴파일 시에만 필요
    compileOnly("org.projectlombok:lombok:1.18.32")
}
```

**settings.gradle.kts**

```kotlin
// settings.gradle.kts
rootProject.name = "my-kotlin-project"

// 멀티 모듈일 때
// include("core", "api", "infra")
```

{{< callout type="info" title="jvmToolchain vs sourceCompatibility" >}}
`jvmToolchain(17)`은 컴파일러 JDK 버전, 바이트코드 버전, 컴파일 소스 버전을 한 번에 설정합니다. `sourceCompatibility = JavaVersion.VERSION_17`은 Java 소스에만 영향을 미칩니다. Kotlin 프로젝트에서는 `jvmToolchain` 사용을 권장합니다.
{{< /callout >}}

#### Step 2 — IntelliJ IDEA 협업 설정

IntelliJ IDEA 처음 열기는 [Quick Start](../../quick-start/)에서 다뤘으니, 여기서는 협업과 생산성에 필요한 추가 설정을 정리합니다.

**SDK 일치 확인**

팀 프로젝트에서 SDK가 어긋나면 빌드 결과가 달라집니다.

- **File** → **Project Structure** (⌘ + ;) → **SDK**: JDK 17 확인
- 우측 하단 "Gradle" 아이콘 → **Reload All Gradle Projects** 로 빌드 스크립트와 IDE 인덱스를 동기화

**유용한 단축키 (macOS 기준)**

| 단축키 | 기능 |
|--------|------|
| ⌘ + Shift + O | 파일 빠른 열기 |
| ⌘ + B | 정의로 이동 |
| ⌥ + Enter | 빠른 수정 / import 추가 |
| ⌃ + Shift + R | 실행 |
| ⌘ + Shift + F | 전체 검색 |
| ⌃ + T | 리팩토링 메뉴 |

#### Step 3 — 자주 쓰는 Gradle 명령

Quick Start의 `./gradlew run` 외에 실무에서 자주 쓰는 명령들을 정리했습니다.

```bash
# 빌드 + 테스트
./gradlew build

# 테스트만 실행
./gradlew test

# 파일 변경 시 자동 재실행 (개발 중 유용)
./gradlew run --continuous

# 의존성 트리 출력 (어떤 라이브러리가 들어오는지 확인)
./gradlew dependencies

# 캐시 갱신과 함께 다시 빌드 (의존성 해결 문제 시)
./gradlew build --refresh-dependencies
```

#### Step 4 — 코드 스타일 통합 (협업 권장)

팀 프로젝트에서는 ktlint로 코드 스타일을 통일합니다.

```kotlin
// build.gradle.kts에 추가
plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

ktlint {
    version.set("1.3.1")
    android.set(false)
    outputToConsole.set(true)
}
```

```bash
# 스타일 검사
./gradlew ktlintCheck

# 자동 수정
./gradlew ktlintFormat
```

#### 트러블슈팅

**`java.lang.UnsupportedClassVersionError`**

컴파일 버전과 실행 JDK 버전이 다를 때 발생합니다. `java -version`으로 실행 환경이 JDK 17인지 확인합니다.

**`Could not resolve com.example:lib:1.0.0`**

의존성 캐시 문제일 수 있습니다.

```bash
./gradlew build --refresh-dependencies
```

**IntelliJ에서 import 오류**

Gradle 설정을 다시 로드합니다.

```bash
./gradlew --stop   # Gradle 데몬 종료
```

그런 다음 IntelliJ에서 **File** → **Invalidate Caches** → **Invalidate and Restart**

{{< callout type="tip" title="핵심 포인트" >}}
- JDK 17, `kotlin("jvm") 2.0.0`, `jvmToolchain(17)` — 이 세 가지가 기본 설정의 전부
- `./gradlew` 명령으로 Gradle 별도 설치 없이 빌드 가능
- IntelliJ에서 Gradle 프로젝트 열기 → Reload가 환경 설정의 마지막 단계
{{< /callout >}}

#### 다음 단계

- [기본 예제](basic/) — Hello Kotlin, 데이터 클래스, 컬렉션 처리
- [Spring Boot 연동](spring-boot-integration/) — Kotlin + Spring Boot REST API 구성
