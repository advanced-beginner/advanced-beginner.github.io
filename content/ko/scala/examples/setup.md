---
lastmod: "2026-01-06"
title: 환경 설정
weight: 1
---

Scala 개발 환경을 설정하는 방법을 상세히 설명합니다.

## sbt 설치

### Coursier (권장)

```bash
# macOS
brew install coursier/formulas/coursier
cs setup

# Linux
curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
./cs setup

# Windows
# Coursier 설치 후 cs setup 실행
```

### 직접 설치

```bash
# macOS
brew install sbt

# Linux (SDKMAN)
sdk install sbt
```

## 프로젝트 구조

```
my-project/
├── build.sbt                 # 빌드 설정
├── project/
│   ├── build.properties      # sbt 버전
│   └── plugins.sbt           # sbt 플러그인
├── src/
│   ├── main/
│   │   ├── scala/            # 메인 소스
│   │   └── resources/        # 리소스 파일
│   └── test/
│       ├── scala/            # 테스트 소스
│       └── resources/        # 테스트 리소스
└── target/                   # 빌드 결과물
```

## build.sbt 설정

### Scala 3

```scala
val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "my-project",
    version := "0.1.0",
    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
```

### Scala 2.13

```scala
val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "my-project",
    version := "0.1.0",
    scalaVersion := scala2Version,

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )
```

### project/build.properties

```properties
sbt.version=1.10.6
```

> 💡 **팁:** 최신 sbt 버전은 [sbt 릴리스 페이지](https://github.com/sbt/sbt/releases)에서 확인하세요.

## 자주 사용하는 sbt 명령어

| 명령어 | 설명 |
|--------|------|
| `sbt` | sbt 셸 시작 |
| `compile` | 컴파일 |
| `run` | 메인 클래스 실행 |
| `test` | 테스트 실행 |
| `console` | REPL 시작 |
| `clean` | 빌드 결과물 삭제 |
| `reload` | build.sbt 다시 로드 |
| `~compile` | 파일 변경 시 자동 컴파일 |
| `~run` | 파일 변경 시 자동 실행 |

### sbt 셸에서

```bash
sbt
> compile
> run
> test
> ~compile
```

## IDE 설정

### IntelliJ IDEA

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) 설치
2. **Plugins** → "Scala" 검색 → 설치
3. **File** → **Open** → 프로젝트 폴더 선택
4. "Import as sbt project" 선택
5. JDK 설정 확인 (Java 11+)

**유용한 단축키:**

| 단축키 (macOS) | 기능 |
|---------------|------|
| ⌘ + ⇧ + Enter | 세미콜론/괄호 자동 완성 |
| ⌥ + Enter | 퀵 픽스, import 추가 |
| ⌘ + B | 정의로 이동 |
| ⌘ + ⌥ + B | 구현으로 이동 |
| ⌘ + ⇧ + T | 테스트 파일로 이동/생성 |
| ⌃ + ⇧ + R | 현재 테스트 실행 |

### VS Code + Metals

1. [VS Code](https://code.visualstudio.com/) 설치
2. Extensions에서 "Metals" 검색 → 설치
3. 프로젝트 폴더 열기
4. "Import build" 팝업 클릭

**유용한 기능:**

- Hover: 타입 정보 표시
- Go to Definition: F12
- Find References: ⇧ + F12
- Rename Symbol: F2

## 의존성 추가

### Maven Central에서 찾기

[search.maven.org](https://search.maven.org/)에서 라이브러리 검색

### sbt 형식

```scala
libraryDependencies ++= Seq(
  // 일반 의존성
  "org.typelevel" %% "cats-core" % "2.10.0",

  // Java 라이브러리 (단일 %)
  "com.google.guava" % "guava" % "32.1.3-jre",

  // 테스트 전용
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,

  // 컴파일 전용
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
```

### %% vs %

- `%%`: Scala 버전 자동 추가 (예: `_3` 또는 `_2.13`)
- `%`: 정확한 아티팩트 이름 사용

## 멀티 프로젝트

```scala
lazy val root = project
  .in(file("."))
  .aggregate(core, api)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "my-project-core"
  )

lazy val api = project
  .in(file("api"))
  .dependsOn(core)
  .settings(
    name := "my-project-api"
  )
```

## 유용한 플러그인

### project/plugins.sbt

```scala
// 의존성 업데이트 확인
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// 코드 포맷팅
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

// 네이티브 패키징
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

// 어셈블리 JAR
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.4")
```

## 트러블슈팅

### "Unable to find matching Java version"

```bash
# Java 버전 확인
java -version

# JAVA_HOME 설정
export JAVA_HOME=/path/to/java
```

### 의존성 해결 오류

```bash
# 캐시 클리어
rm -rf ~/.sbt/1.0/plugins/target
rm -rf ~/.cache/coursier
sbt clean reload
```

### IntelliJ에서 빨간 줄

1. **File** → **Invalidate Caches** → **Restart**
2. sbt 탭에서 **Reload**
3. **Build** → **Rebuild Project**

### "not found: type xxx" 컴파일 에러

```bash
# import 문 확인
# 올바른 예:
import scala.collection.mutable.ListBuffer  # 특정 타입
import scala.collection.mutable.*            # Scala 3
import scala.collection.mutable._            # Scala 2
```

### sbt 메모리 부족

```bash
# .sbtopts 파일 생성 (프로젝트 루트)
-J-Xmx4G
-J-XX:+UseG1GC

# 또는 환경 변수
export SBT_OPTS="-Xmx4G -XX:+UseG1GC"
```

### Scala 3 마이그레이션 에러

```bash
# 공통 문제들:

# 1. 와일드카드 import 변경
# Scala 2: import foo._
# Scala 3: import foo.*

# 2. implicit → given/using
# Scala 2: implicit val x: Int = 1
# Scala 3: given x: Int = 1

# 3. 패키지 객체 deprecation 경고
# package object 대신 최상위 정의 사용 권장
```

### Metals (VS Code) 문제

```bash
# 메타데이터 재생성
rm -rf .metals .bloop .bsp
# VS Code 재시작 후 "Import build" 클릭
```

## 다음 단계

- [기본 예제](../basic/) — 핵심 개념 활용 예제
- [Scala 2 vs 3 비교](../scala2-vs-scala3/) — 버전별 코드 비교
