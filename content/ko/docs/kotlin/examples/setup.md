---
title: "환경 설정"
description: "JDK 17, Gradle Wrapper, IntelliJ IDEA, build.gradle.kts 핵심 옵션을 단계별로 설정합니다."
weight: 1
lastmod: "2026-05-13"
---

{{< callout type="tip" title="TL;DR" >}}
- JDK 17 설치 (SDKMAN 권장)
- `gradle init`으로 Kotlin DSL 프로젝트 생성
- IntelliJ IDEA Community에서 Gradle 프로젝트로 열기
- `build.gradle.kts`에 `kotlin("jvm")` 플러그인과 `jvmToolchain(17)` 설정
{{< /callout >}}

**대상 독자**: Kotlin 개발을 처음 시작하는 개발자
**선수 지식**: 기본적인 터미널 사용법, IDE 경험

---

Kotlin 개발 환경을 단계별로 구성합니다. JDK 설치부터 첫 빌드까지, 모든 과정을 따라하면 바로 코드를 작성할 수 있습니다.

#### Step 1 — JDK 17 설치

Kotlin은 JVM 위에서 실행되므로 JDK 17이 필요합니다. SDKMAN을 사용하면 여러 JDK 버전을 편리하게 관리할 수 있습니다.

**SDKMAN 사용 (macOS/Linux 권장)**

```bash
# SDKMAN 설치
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# JDK 17 설치 (Eclipse Temurin 권장)
sdk install java 17.0.10-tem

# 설치 확인
java -version
# openjdk version "17.0.10" 2024-01-16
```

**Homebrew 사용 (macOS)**

```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**winget 사용 (Windows)**

```powershell
winget install Microsoft.OpenJDK.17
```

설치 후 `java -version`으로 JDK 17이 출력되는지 확인합니다.

#### Step 2 — Gradle Wrapper 프로젝트 생성

Gradle Wrapper를 사용하면 별도로 Gradle을 설치하지 않아도 되고, 팀 전체가 동일한 버전을 사용할 수 있습니다.

```bash
# 프로젝트 디렉토리 생성
mkdir my-kotlin-project
cd my-kotlin-project

# Gradle 초기화 (Gradle이 미설치된 경우 sdk install gradle 8.7)
gradle init \
  --type kotlin-application \
  --dsl kotlin \
  --project-name my-kotlin-project \
  --package com.example
```

`gradle init`이 묻는 질문에는 기본값(Enter)을 선택하면 됩니다. 생성된 프로젝트 구조는 다음과 같습니다.

```
my-kotlin-project/
├── build.gradle.kts          # 빌드 스크립트
├── settings.gradle.kts       # 프로젝트 설정
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties   # Gradle 버전 명시
├── gradlew                   # Unix용 Wrapper
├── gradlew.bat               # Windows용 Wrapper
└── src/
    ├── main/
    │   └── kotlin/
    │       └── com/example/
    │           └── App.kt
    └── test/
        └── kotlin/
            └── com/example/
                └── AppTest.kt
```

#### Step 3 — build.gradle.kts 핵심 설정

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

#### Step 4 — IntelliJ IDEA Community 설정

IntelliJ IDEA는 Kotlin 개발에 가장 최적화된 IDE입니다. Community 버전은 무료입니다.

1. [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/) 다운로드 및 설치
2. **File** → **Open** → 프로젝트 폴더 선택
3. 우측 하단 "Gradle" 아이콘 클릭 → **Reload All Gradle Projects**
4. **File** → **Project Structure** (⌘ + ;) → **SDK**: JDK 17 확인

**유용한 단축키 (macOS 기준)**

| 단축키 | 기능 |
|--------|------|
| ⌘ + Shift + O | 파일 빠른 열기 |
| ⌘ + B | 정의로 이동 |
| ⌥ + Enter | 빠른 수정 / import 추가 |
| ⌃ + Shift + R | 실행 |
| ⌘ + Shift + F | 전체 검색 |
| ⌃ + T | 리팩토링 메뉴 |

#### Step 5 — 빌드 및 실행

프로젝트가 정상적으로 설정되었는지 확인합니다.

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew run

# 파일 변경 시 자동 재실행
./gradlew run --continuous
```

빌드 성공 시 `BUILD SUCCESSFUL` 메시지가 출력됩니다.

#### Step 6 — 코드 스타일 설정 (선택)

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
