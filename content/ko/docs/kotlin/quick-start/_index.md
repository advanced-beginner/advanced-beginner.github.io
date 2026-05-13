---
bookCollapseSection: true
lastmod: "2026-05-13"
title: Quick Start
description: "Kotlin 개발 환경을 구축하고 첫 번째 프로그램을 실행합니다."
weight: 1
---

> **대상 독자**: Kotlin을 처음 시작하는 JVM 개발자 또는 Android·백엔드 진입을 준비하는 개발자
> **선수 지식**: 프로그래밍 기본 개념, 터미널/명령줄 사용 경험
> **이 문서를 읽으면**: Kotlin 개발 환경을 구축하고, REPL과 Gradle 프로젝트에서 간단한 코드를 실행할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- SDKMAN 또는 Homebrew로 JDK 17과 Kotlin을 설치합니다
- `val` (불변), `var` (가변), `fun` (함수)이 Kotlin의 기본 키워드입니다
- Gradle Kotlin DSL이 사실상의 표준 빌드 도구입니다 (`./gradlew run`)
- IntelliJ IDEA Community 또는 VS Code + Kotlin 확장으로 개발할 수 있습니다
{{< /callout >}}

5분 만에 Kotlin을 설치하고 첫 번째 프로그램을 실행해봅니다. 이 가이드는 설치부터 Gradle 프로젝트 생성, IDE 설정까지 실무에서 바로 사용할 수 있는 환경을 구축하는 과정을 안내합니다.

> 🎯 **설치 없이 바로 실행:** [Kotlin Playground](https://play.kotlinlang.org/)에서 브라우저로 Kotlin을 바로 실행할 수 있습니다.

**시작 전 확인**

| 항목 | 확인 명령어 | 예상 결과 |
|------|-------------|-----------|
| Java | `java -version` | `openjdk version "17.x.x"` 이상 |
| 터미널 | - | bash, zsh, PowerShell 중 하나 |

## Step 1/5: Kotlin 설치 (~2분)

Kotlin을 설치하는 가장 쉬운 방법은 SDKMAN(macOS·Linux) 또는 Homebrew를 사용하는 것입니다. 두 도구 모두 JDK와 Kotlin을 손쉽게 관리할 수 있게 해줍니다.

**SDKMAN으로 설치 (권장, macOS·Linux·WSL)**

SDKMAN은 JVM 생태계 도구 버전을 관리하는 사실상의 표준입니다.

```bash
# SDKMAN 설치
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# JDK 17 설치
sdk install java 17.0.10-tem

# Kotlin 설치
sdk install kotlin
```

**Homebrew로 설치 (macOS)**

Homebrew를 사용하는 경우 다음 명령으로 설치합니다.

```bash
brew install openjdk@17 kotlin
```

**Windows (Scoop)**

```powershell
scoop install openjdk17 kotlin
```

설치 후 새 터미널을 열고 버전을 확인합니다.

```bash
java -version
# openjdk version "17.x.x"

kotlin -version
# Kotlin version 2.x.x
```

## Step 2/5: Hello World (~1분)

Kotlin의 기본 문법을 확인하는 가장 좋은 방법은 간단한 Hello World 프로그램을 작성하는 것입니다. REPL에서 바로 실행하거나 파일로 저장해서 실행할 수 있습니다.

**REPL에서 실행**

Kotlin REPL(Read-Eval-Print Loop)로 바로 코드를 실행해봅니다.

```bash
kotlinc
```

```kotlin
>>> println("Hello, Kotlin!")
Hello, Kotlin!

>>> val name = "World"
>>> println("Hello, $name!")
Hello, World!

>>> :quit
```

**파일로 실행**

코드를 파일로 저장하면 재사용과 버전 관리가 가능합니다.

```kotlin
// hello.kt
fun main() {
    val message = "Hello, Kotlin!"
    println(message)
}
```

```bash
# 컴파일 + 실행
kotlinc hello.kt -include-runtime -d hello.jar
java -jar hello.jar
# Hello, Kotlin!

# 또는 스크립트로 바로 실행 (.kts)
kotlin hello.kt
```

## Step 3/5: Gradle 프로젝트 생성 (~3분)

실제 프로젝트에서는 Gradle Kotlin DSL을 사용합니다. Gradle은 의존성 관리, 컴파일, 테스트, 패키징 등 프로젝트 빌드에 필요한 모든 기능을 제공합니다.

**프로젝트 생성**

```bash
mkdir kotlin-quickstart && cd kotlin-quickstart

# Gradle init으로 Kotlin 애플리케이션 프로젝트 생성
gradle init \
  --type kotlin-application \
  --dsl kotlin \
  --test-framework junit-jupiter \
  --package com.example \
  --project-name kotlin-quickstart
```

생성된 디렉토리는 다음과 같은 구조를 가집니다.

```text
kotlin-quickstart/
├── build.gradle.kts            # 빌드 스크립트 (Kotlin DSL)
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/kotlin/com/example/App.kt
        └── test/kotlin/com/example/AppTest.kt
```

**수동 프로젝트 구성**

템플릿 없이 직접 구성하면 구조를 더 잘 이해할 수 있습니다. 최소한 `build.gradle.kts`와 소스 디렉토리만 있으면 됩니다.

```bash
mkdir -p src/main/kotlin
```

**build.gradle.kts**:

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(17)
}
```

**src/main/kotlin/Main.kt**:

```kotlin
fun main() {
    println("Hello from Gradle project!")
}
```

**실행**

Gradle Wrapper로 프로젝트를 실행하면 의존성 해결, 컴파일, 실행까지 자동으로 처리됩니다.

```bash
./gradlew run
# > Task :run
# Hello from Gradle project!
```

**자주 사용하는 Gradle 명령어**

아래 표는 일상적인 개발에서 가장 자주 사용하는 명령어들입니다. `--continuous` 플래그를 붙이면 파일 변경을 감지하여 자동으로 다시 실행합니다.

| 명령어 | 설명 |
|--------|------|
| `./gradlew run` | 메인 클래스 실행 |
| `./gradlew build` | 컴파일 + 테스트 + JAR 패키징 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew clean` | 빌드 결과물 삭제 |
| `./gradlew run --continuous` | 파일 변경 시 자동 실행 |

## Step 4/5: IDE 설정 (~2분)

효율적인 Kotlin 개발을 위해서는 IDE 설정이 중요합니다. 코드 자동 완성, 타입 검사, 리팩토링 지원 등을 받을 수 있습니다.

**IntelliJ IDEA (권장)**

JetBrains는 Kotlin을 만든 회사로, IntelliJ IDEA의 Kotlin 지원이 가장 우수합니다.

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) 설치 (Community Edition 무료)
2. **File** → **Open** → Gradle 프로젝트 폴더 선택
3. "Trust Project" 클릭 → Gradle 동기화 자동 진행

Kotlin 플러그인은 기본 내장되어 있어 별도 설치가 필요 없습니다.

**VS Code + Kotlin 확장**

VS Code는 가볍고 빠른 에디터로, Kotlin Language Server를 통해 LSP 지원을 받을 수 있습니다.

1. [VS Code](https://code.visualstudio.com/) 설치
2. Extensions에서 "Kotlin Language" 검색 → 설치
3. Gradle 프로젝트 폴더 열기

## Step 5/5: 간단한 예제 (~2분)

설치가 완료되었으니 Kotlin의 기본 문법을 간단한 예제로 살펴봅니다. 아래 코드들은 Kotlin의 핵심적인 특징인 타입 추론, Null Safety, 함수형 컬렉션 연산을 보여줍니다.

> 💻 아래 예제들을 [Kotlin Playground](https://play.kotlinlang.org/)에서 직접 실행해보세요.

**변수와 타입**

```kotlin
// 불변 (권장)
val name: String = "Kotlin"
val year = 2026  // 타입 추론

// 가변 (필요한 경우만)
var count = 0
count = count + 1

// 기본 타입
val number: Int = 42
val pi: Double = 3.14
val isKotlin: Boolean = true
val char: Char = 'K'
```

**함수 정의**

Kotlin에서 함수는 `fun` 키워드로 정의합니다. 표현식 본문(`= 값`)을 사용하면 더 간결합니다.

```kotlin
// 표현식 본문
fun greet(name: String): String = "Hello, $name!"

// 블록 본문
fun add(a: Int, b: Int): Int {
    return a + b
}

// 호출
println(greet("World"))  // Hello, World!
println(add(1, 2))       // 3
```

**Null Safety**

Kotlin의 가장 큰 특징은 Null 안정성입니다. 타입 뒤에 `?`를 붙여야 null을 허용합니다.

```kotlin
val nonNull: String = "value"
// nonNull = null  // 컴파일 에러

val nullable: String? = null
val length = nullable?.length ?: 0  // 안전 호출 + Elvis 연산자
println(length)  // 0
```

**컬렉션 맛보기**

Kotlin 컬렉션은 함수형 프로그래밍의 강력함을 보여줍니다. `map`, `filter`, `reduce` 등의 고차 함수로 데이터를 선언적으로 변환할 수 있습니다.

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// 변환
val doubled = numbers.map { it * 2 }
// [2, 4, 6, 8, 10]

// 필터링
val evens = numbers.filter { it % 2 == 0 }
// [2, 4]

// 축약
val sum = numbers.reduce { acc, n -> acc + n }
// 15
```

## 다음 단계

{{< callout type="success" title="🎉 축하합니다!" >}}
Kotlin Quick Start를 성공적으로 완료했습니다. 이제 여러분은:
- Kotlin 개발 환경을 구축했습니다
- REPL에서 코드를 실행할 수 있습니다
- Gradle Kotlin DSL 프로젝트를 생성하고 관리할 수 있습니다
- IDE에서 Kotlin 프로젝트를 열고 작업할 수 있습니다
{{< /callout >}}

환경 설정이 끝났으니 이제 Kotlin의 핵심 개념들을 하나씩 배워볼 차례입니다.

1. **[기본 문법](../concepts/basics/)** — 표현식 기반 문법, 패키지 구성
2. **[변수와 타입](../concepts/variables-types/)** — `val`/`var`, 타입 추론, 기본 타입
3. **[Null Safety](../concepts/null-safety/)** — Kotlin의 핵심 안전 기능
4. **[함수](../concepts/functions/)** — 함수 정의, 람다, 고차 함수

## 트러블슈팅

설치나 실행 중 문제가 발생하면 아래 해결책을 참고하세요.

**`kotlin` 또는 `kotlinc` 명령어를 찾을 수 없음**

```bash
# PATH 확인
echo $PATH | grep -E "sdkman|kotlin"

# SDKMAN 사용 시 PATH 재설정
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

**Gradle Wrapper 실행 시 권한 오류 (macOS·Linux)**

```bash
chmod +x gradlew
./gradlew --version
```

**IntelliJ에서 빨간 줄이 표시됨**

IntelliJ의 인덱스가 손상되었거나 Gradle 동기화가 깨진 경우 발생합니다.

1. **File** → **Invalidate Caches** → **Restart**
2. 또는 Gradle 탭에서 **Reload All Gradle Projects** 클릭
