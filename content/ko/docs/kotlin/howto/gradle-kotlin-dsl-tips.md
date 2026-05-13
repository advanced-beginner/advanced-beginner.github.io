---
title: "Gradle Kotlin DSL 팁"
description: "Version Catalog, buildSrc, 컨벤션 플러그인 등 Gradle Kotlin DSL 실전 노하우와 멀티모듈 구조의 자주 쓰는 패턴을 안내합니다."
lastmod: "2026-05-13"
---

Gradle Kotlin DSL을 실무에서 효율적으로 사용하는 노하우와 자주 쓰는 패턴을 정리합니다.

**소요 시간**: 약 10~15분

{{< callout type="tip" title="TL;DR" >}}
- `libs.versions.toml`(Version Catalog)으로 모든 의존성 버전을 한 곳에서 관리합니다.
- `buildSrc`나 composite build로 빌드 로직을 재사용합니다.
- `plugins { }` 블록은 `apply(plugin = "...")` 대신 사용하는 최신 방식입니다.
- 멀티모듈 컨벤션 플러그인으로 반복되는 설정을 DRY하게 유지합니다.
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- 여러 모듈에서 동일한 Gradle 설정을 복사-붙여넣기하고 있을 때
- 의존성 버전이 여러 파일에 흩어져 있어 업데이트가 번거로울 때
- Groovy DSL을 Kotlin DSL로 전환하려는데 문법이 헷갈릴 때

---

## 1단계: Version Catalog 설정 (libs.versions.toml)

`gradle/libs.versions.toml`은 Gradle 7.4부터 지원하는 의존성 버전 중앙화 파일입니다.

**파일 구조:**

```toml
# gradle/libs.versions.toml

[versions]
kotlin         = "2.0.0"
springBoot     = "3.2.5"
coroutines     = "1.8.1"
serialization  = "1.6.3"
ktor           = "2.3.10"
kotest         = "5.8.1"
mockk          = "1.13.10"

[libraries]
kotlin-stdlib       = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlin-reflect      = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }
coroutines-core     = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-reactor  = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor", version.ref = "coroutines" }
coroutines-debug    = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-debug", version.ref = "coroutines" }
serialization-json  = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
spring-boot-web     = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-webflux = { module = "org.springframework.boot:spring-boot-starter-webflux" }
spring-boot-test    = { module = "org.springframework.boot:spring-boot-starter-test" }
kotest-runner       = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions   = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
mockk               = { module = "io.mockk:mockk", version.ref = "mockk" }

[bundles]
# 여러 라이브러리를 하나의 번들로 묶음
testing = ["kotest-runner", "kotest-assertions", "mockk"]

[plugins]
kotlin-jvm          = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring       = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot         = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency   = { id = "io.spring.dependency-management", version = "1.1.5" }
```

**사용 방법 (build.gradle.kts):**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)
    implementation(libs.spring.boot.web)

    // 번들 사용
    testImplementation(libs.bundles.testing)
    testImplementation(libs.spring.boot.test)
}
```

---

## 2단계: plugins 블록 vs apply

```kotlin
// 최신 방식 (권장): plugins { } 블록
// - 클래스패스 설정 자동
// - 빌드 스캔에서 추적 가능
// - ID와 버전을 한 곳에서 관리
plugins {
    kotlin("jvm") version "2.0.0"
    id("org.springframework.boot") version "3.2.5"
}

// 구식 방식: apply
// - 먼저 buildscript { classpath } 추가 필요
// - 오류 메시지가 덜 명확
buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    }
}
apply(plugin = "org.springframework.boot")
```

**서브모듈에서 버전 없이 플러그인 적용:**

```kotlin
// settings.gradle.kts에서 plugins 버전을 선언하면
// 서브모듈에서 버전 없이 사용 가능
plugins {
    id("org.springframework.boot") apply false  // 버전만 선언, 적용은 서브모듈에서
}

// subproject/build.gradle.kts
plugins {
    id("org.springframework.boot")  // 버전 생략 가능
}
```

---

## 3단계: buildSrc로 빌드 로직 공유

`buildSrc`는 모든 모듈에서 공유하는 Gradle 빌드 코드를 담는 특별 디렉토리입니다.

**디렉토리 구조:**

```text
프로젝트 루트/
├── buildSrc/
│   ├── build.gradle.kts        # buildSrc 자체 빌드 설정
│   └── src/main/kotlin/
│       ├── kotlin-base.gradle.kts   # Kotlin 공통 설정 컨벤션 플러그인
│       └── spring-app.gradle.kts   # Spring Boot 앱 컨벤션 플러그인
├── api-module/
│   └── build.gradle.kts
└── domain-module/
    └── build.gradle.kts
```

**buildSrc/build.gradle.kts:**

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.5")
}
```

**컨벤션 플러그인 — kotlin-base.gradle.kts:**

```kotlin
// buildSrc/src/main/kotlin/kotlin-base.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Dkotlinx.coroutines.debug")
}
```

**컨벤션 플러그인 — spring-app.gradle.kts:**

```kotlin
// buildSrc/src/main/kotlin/spring-app.gradle.kts
plugins {
    id("kotlin-base")  // 위에서 만든 컨벤션 플러그인 재사용
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}
```

**서브모듈에서 사용:**

```kotlin
// api-module/build.gradle.kts
plugins {
    id("spring-app")  // 한 줄로 Spring Boot 앱 전체 설정 적용
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

---

## 4단계: 자주 쓰는 Task 패턴

**테스트 분리 (단위/통합):**

```kotlin
// build.gradle.kts
val integrationTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "통합 테스트 실행"
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    useJUnitPlatform()

    // 통합 테스트는 느리므로 캐시 비활성화
    outputs.upToDateWhen { false }
}

// 일반 테스트에서 통합 테스트 제외
tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}
```

**Jar 옵션 설정:**

```kotlin
// Executable Jar 설정
tasks.bootJar {
    archiveFileName.set("my-app.jar")
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

// Fat Jar 생성 (Spring Boot 없이)
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

**사용자 정의 Task:**

```kotlin
// 코드 품질 검사 Task
tasks.register("codeQuality") {
    dependsOn("ktlintCheck", "detekt", "test")
    description = "코드 품질 전체 검사"
    group = "verification"
}

// 환경별 application.yml 복사
tasks.register<Copy>("copyConfig") {
    val env = project.findProperty("env") as? String ?: "local"
    from("src/main/resources/config/$env")
    into("src/main/resources")
    description = "환경별 설정 파일 적용 (env=$env)"
}
```

---

## 5단계: 멀티모듈 구조 설정

**settings.gradle.kts:**

```kotlin
rootProject.name = "my-project"

// Version Catalog 활성화
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":domain",
    ":application",
    ":infrastructure",
    ":api",
    ":batch"
)
```

**루트 build.gradle.kts:**

```kotlin
// 모든 서브프로젝트에 공통 설정 적용
subprojects {
    group = "com.example"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}
```

**모듈 간 의존성 (타입 세이프 접근자 사용):**

```kotlin
// api/build.gradle.kts
dependencies {
    implementation(projects.domain)       // :domain 모듈 (타입 세이프)
    implementation(projects.application)  // :application 모듈
    // 문자열 방식: implementation(project(":domain"))
}
```

---

## Groovy DSL → Kotlin DSL 전환 주요 차이점

| Groovy DSL | Kotlin DSL | 설명 |
|-----------|-----------|------|
| `apply plugin: 'java'` | `plugins { java }` | 플러그인 적용 |
| `'groupId:artifactId:1.0'` | `"groupId:artifactId:1.0"` | 문자열은 큰따옴표 |
| `sourceCompatibility = 17` | `java { sourceCompatibility = JavaVersion.VERSION_17 }` | Java 버전 |
| `test { useJUnitPlatform() }` | `tasks.test { useJUnitPlatform() }` | Task 설정 |
| `def myVar = ...` | `val myVar = ...` | 변수 선언 |
| `ext.myProp = 'value'` | `extra["myProp"] = "value"` | 추가 프로퍼티 |

---

## 체크리스트

- [ ] 모든 의존성 버전이 `libs.versions.toml`에 정의됐는가?
- [ ] 공통 빌드 설정이 `buildSrc` 컨벤션 플러그인으로 추출됐는가?
- [ ] `plugins { }` 블록을 `apply(plugin = "...")` 대신 사용하는가?
- [ ] 멀티모듈에서 모듈 간 의존성에 타입 세이프 접근자(`projects.xxx`)를 사용하는가?
- [ ] 단위 테스트와 통합 테스트가 분리됐는가?

---

## 관련 문서

- [Multiplatform 개요](../concepts/multiplatform-overview/) — KMP Gradle 설정
- [Kotest vs JUnit](kotest-vs-junit/) — 테스트 프레임워크 Gradle 설정
- [성능 프로파일링](performance-profiling/) — Gradle 빌드 성능 측정
