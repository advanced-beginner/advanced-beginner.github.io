---
lastmod: "2026-01-10"
title: 환경 설정
weight: 1
---

{{< callout type="info" title="TL;DR" >}}
- <strong>Coursier</strong>로 JDK, sbt, Scala CLI를 한 번에 설치
- <strong>sbt</strong>는 Scala의 표준 빌드 도구로 컴파일, 테스트, 의존성 관리 담당
- <strong>IntelliJ IDEA</strong> 또는 <strong>VS Code + Metals</strong>로 IDE 설정
- 프로젝트 구조: `build.sbt` (설정) + `src/main/scala/` (소스) + `src/test/scala/` (테스트)
{{< /callout >}}

**대상 독자**: Scala 프로그래밍을 처음 시작하는 개발자

**선수 지식**:
- 기본적인 터미널/명령줄 사용법
- Java 또는 다른 JVM 언어 경험 (권장)
- IDE 사용 경험

---

Scala 개발 환경을 설정하는 방법을 상세히 설명합니다. Scala 프로그래밍을 시작하려면 JDK, sbt(Scala Build Tool), 그리고 IDE가 필요합니다. 이 문서에서는 각 도구의 설치 방법과 설정 과정을 단계별로 안내합니다.

#### sbt 설치

sbt는 Scala의 표준 빌드 도구입니다. 프로젝트 컴파일, 테스트 실행, 의존성 관리 등을 담당합니다. Coursier를 통해 설치하는 것을 권장하며, 운영체제에 맞는 설치 방법을 선택하면 됩니다.

**Coursier (권장)**

Coursier는 Scala 도구들을 관리하는 범용 설치 도구입니다. `cs setup` 명령어 하나로 JDK, sbt, Scala CLI 등 필요한 모든 도구를 자동으로 설치해줍니다.

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

**직접 설치**

Coursier 대신 패키지 관리자를 통해 sbt만 직접 설치할 수도 있습니다. 이 경우 JDK는 별도로 설치해야 합니다.

```bash
# macOS
brew install sbt

# Linux (SDKMAN)
sdk install sbt
```

{{< callout type="tip" title="핵심 포인트" >}}
- Coursier(`cs setup`)는 모든 필수 도구를 자동 설치하는 가장 편리한 방법
- 직접 설치 시 JDK는 별도로 설치 필요
{{< /callout >}}

#### 프로젝트 구조

sbt 프로젝트는 정해진 디렉토리 구조를 따릅니다. 이 구조를 이해하면 프로젝트 내 파일들의 역할을 쉽게 파악할 수 있습니다. 아래는 일반적인 sbt 프로젝트의 디렉토리 구조입니다.

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

`build.sbt`는 프로젝트의 핵심 설정 파일로, 프로젝트 이름, Scala 버전, 의존성 등을 정의합니다. `project/` 디렉토리에는 빌드 자체에 대한 메타 설정이 들어갑니다. `src/main/scala/`에는 애플리케이션 소스 코드가, `src/test/scala/`에는 테스트 코드가 위치합니다. `target/` 디렉토리는 컴파일 결과물이 생성되는 곳으로, 버전 관리에서 제외해야 합니다.

{{< callout type="tip" title="핵심 포인트" >}}
- `build.sbt`: 프로젝트 메타데이터 및 의존성 정의
- `src/main/scala/`: 메인 소스 코드 위치
- `src/test/scala/`: 테스트 코드 위치
- `target/`: 빌드 결과물 (`.gitignore`에 추가)
{{< /callout >}}

#### build.sbt 설정

build.sbt 파일에서 프로젝트의 기본 정보와 의존성을 설정합니다. Scala 3와 Scala 2.13에 따라 설정이 약간 다르므로 사용하는 버전에 맞는 설정을 적용하세요.

**Scala 3**

Scala 3 프로젝트의 기본 build.sbt 설정입니다. MUnit을 테스트 프레임워크로 사용합니다.

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

**Scala 2.13**

Scala 2.13 프로젝트의 기본 build.sbt 설정입니다. ScalaTest를 테스트 프레임워크로 사용합니다.

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

**project/build.properties**

이 파일에는 프로젝트에서 사용할 sbt 버전을 명시합니다. 팀원 간에 동일한 sbt 버전을 사용하도록 보장하므로 반드시 버전 관리에 포함해야 합니다.

```properties
sbt.version=1.10.6
```

> 💡 **팁:** 최신 sbt 버전은 [sbt 릴리스 페이지](https://github.com/sbt/sbt/releases)에서 확인하세요.

{{< callout type="tip" title="핵심 포인트" >}}
- Scala 3: `scalaVersion := "3.3.1"`, MUnit 테스트 프레임워크 권장
- Scala 2.13: `scalaVersion := "2.13.12"`, ScalaTest 프레임워크 권장
- `project/build.properties`에 sbt 버전 명시로 팀 간 일관성 유지
{{< /callout >}}

#### 자주 사용하는 sbt 명령어

sbt 셸에서 사용하는 명령어들입니다. sbt를 실행하면 인터랙티브 셸에 진입하고, 여기서 다양한 명령어를 실행할 수 있습니다. 아래 표는 가장 자주 사용하는 명령어들을 정리한 것입니다.

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

`~` 접두사가 붙은 명령어는 파일이 변경될 때마다 해당 작업을 자동으로 반복 실행합니다. 개발 중에 코드를 수정하면서 즉시 결과를 확인할 때 유용합니다.

**sbt 셸에서**

터미널에서 `sbt`를 실행하면 인터랙티브 셸에 진입합니다. 셸 내에서 명령어를 입력하면 빠르게 실행됩니다.

```bash
sbt
> compile
> run
> test
> ~compile
```

{{< callout type="tip" title="핵심 포인트" >}}
- `compile`, `run`, `test`: 가장 자주 사용하는 기본 명령어
- `~` 접두사: 파일 변경 감지 후 자동 재실행 (개발 중 필수)
- sbt 셸 내에서 명령어 실행이 더 빠름
{{< /callout >}}

#### IDE 설정

Scala 개발에는 IntelliJ IDEA나 VS Code를 주로 사용합니다. 두 IDE 모두 훌륭한 Scala 지원을 제공하므로, 익숙한 도구를 선택하면 됩니다.

**IntelliJ IDEA**

IntelliJ IDEA는 가장 완성도 높은 Scala IDE입니다. JetBrains에서 공식 Scala 플러그인을 제공합니다.

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) 설치
2. **Plugins** → "Scala" 검색 → 설치
3. **File** → **Open** → 프로젝트 폴더 선택
4. "Import as sbt project" 선택
5. JDK 설정 확인 (Java 11+)

IntelliJ에서 Scala 개발 시 자주 사용하는 단축키를 알아두면 생산성이 크게 향상됩니다. 아래는 macOS 기준 단축키입니다.

| 단축키 (macOS) | 기능 |
|---------------|------|
| ⌘ + ⇧ + Enter | 세미콜론/괄호 자동 완성 |
| ⌥ + Enter | 퀵 픽스, import 추가 |
| ⌘ + B | 정의로 이동 |
| ⌘ + ⌥ + B | 구현으로 이동 |
| ⌘ + ⇧ + T | 테스트 파일로 이동/생성 |
| ⌃ + ⇧ + R | 현재 테스트 실행 |

**VS Code + Metals**

VS Code는 가볍고 빠른 편집기로, Metals 확장을 통해 Scala를 지원합니다. 무료로 사용할 수 있으며 설정이 간단합니다.

1. [VS Code](https://code.visualstudio.com/) 설치
2. Extensions에서 "Metals" 검색 → 설치
3. 프로젝트 폴더 열기
4. "Import build" 팝업 클릭

Metals가 제공하는 유용한 기능들입니다. Hover로 타입 정보를 확인하고, F12로 정의로 이동하며, ⇧ + F12로 참조를 찾고, F2로 심볼 이름을 변경할 수 있습니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **IntelliJ IDEA**: 가장 완성도 높은 Scala IDE, Scala 플러그인 설치 필요
- **VS Code + Metals**: 가볍고 빠름, 무료, "Import build" 클릭으로 프로젝트 설정
- 둘 다 훌륭한 지원 제공 - 익숙한 도구 선택
{{< /callout >}}

#### 의존성 추가

외부 라이브러리를 프로젝트에 추가하려면 build.sbt의 libraryDependencies에 의존성을 추가합니다. Maven Central에서 원하는 라이브러리를 검색하여 sbt 형식으로 복사해 사용할 수 있습니다.

**Maven Central에서 찾기**

[search.maven.org](https://search.maven.org/)에서 원하는 라이브러리를 검색하면 sbt 형식의 의존성 선언을 얻을 수 있습니다.

**sbt 형식**

의존성은 groupID, artifactID, version 세 부분으로 구성됩니다. 테스트 전용 의존성에는 `% Test`를, 컴파일 전용에는 `% Provided`를 붙입니다.

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

**%% vs %**

`%%`와 `%`의 차이를 이해하는 것이 중요합니다. `%%`를 사용하면 Scala 버전이 자동으로 아티팩트 이름에 추가됩니다. 예를 들어 Scala 3을 사용한다면 `cats-core_3`으로, Scala 2.13을 사용한다면 `cats-core_2.13`으로 변환됩니다. Java 라이브러리는 Scala 버전과 무관하므로 단일 `%`를 사용합니다.

{{< callout type="tip" title="핵심 포인트" >}}
- `%%`: Scala 라이브러리 (Scala 버전 자동 추가)
- `%`: Java 라이브러리 (버전 무관)
- `% Test`: 테스트 전용, `% Provided`: 컴파일 전용
{{< /callout >}}

#### 멀티 프로젝트

대규모 프로젝트에서는 코드를 여러 서브 프로젝트로 분리합니다. sbt는 멀티 프로젝트 빌드를 기본으로 지원하며, 프로젝트 간 의존성도 쉽게 설정할 수 있습니다.

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

위 예제에서 `root` 프로젝트는 `core`와 `api`를 집계(aggregate)합니다. `api` 프로젝트는 `core`에 의존(dependsOn)하므로 `core`의 클래스들을 사용할 수 있습니다.

{{< callout type="tip" title="핵심 포인트" >}}
- `aggregate`: 여러 서브프로젝트를 한 번에 빌드
- `dependsOn`: 다른 프로젝트의 클래스 사용 가능
- 대규모 프로젝트에서 코드 분리에 유용
{{< /callout >}}

#### 유용한 플러그인

sbt 플러그인은 빌드 기능을 확장합니다. `project/plugins.sbt` 파일에 플러그인을 추가하면 됩니다.

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

sbt-updates는 의존성의 새 버전이 있는지 확인해줍니다. sbt-scalafmt은 코드를 일관된 스타일로 포맷팅합니다. sbt-native-packager는 Docker 이미지나 네이티브 패키지를 생성하고, sbt-assembly는 모든 의존성을 포함한 단일 JAR 파일을 만듭니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **sbt-updates**: 의존성 업데이트 확인
- **sbt-scalafmt**: 코드 자동 포맷팅
- **sbt-assembly**: 배포용 단일 JAR 생성
- 플러그인은 `project/plugins.sbt`에 추가
{{< /callout >}}

#### 트러블슈팅

개발 환경 설정 중 발생할 수 있는 일반적인 문제와 해결 방법을 정리했습니다.

**"Unable to find matching Java version"**

JDK가 설치되지 않았거나 JAVA_HOME 환경 변수가 설정되지 않은 경우 발생합니다.

```bash
# Java 버전 확인
java -version

# JAVA_HOME 설정
export JAVA_HOME=/path/to/java
```

**의존성 해결 오류**

캐시가 손상되었거나 의존성 정보가 오래된 경우 발생합니다. 캐시를 삭제하고 다시 로드하면 대부분 해결됩니다.

```bash
# 캐시 클리어
rm -rf ~/.sbt/1.0/plugins/target
rm -rf ~/.cache/coursier
sbt clean reload
```

**IntelliJ에서 빨간 줄**

IntelliJ가 프로젝트 구조를 제대로 인식하지 못하는 경우 발생합니다. 캐시를 무효화하고 프로젝트를 다시 빌드하면 해결됩니다.

1. **File** → **Invalidate Caches** → **Restart**
2. sbt 탭에서 **Reload**
3. **Build** → **Rebuild Project**

**"not found: type xxx" 컴파일 에러**

필요한 import 문이 누락된 경우 발생합니다. Scala 3과 2에서 와일드카드 import 문법이 다르다는 점에 주의하세요.

```bash
# import 문 확인
# 올바른 예:
import scala.collection.mutable.ListBuffer  # 특정 타입
import scala.collection.mutable.*            # Scala 3
import scala.collection.mutable._            # Scala 2
```

**sbt 메모리 부족**

대규모 프로젝트에서 컴파일 시 메모리가 부족할 수 있습니다. 프로젝트 루트에 `.sbtopts` 파일을 생성하여 메모리 설정을 조정합니다.

```bash
# .sbtopts 파일 생성 (프로젝트 루트)
-J-Xmx4G
-J-XX:+UseG1GC

# 또는 환경 변수
export SBT_OPTS="-Xmx4G -XX:+UseG1GC"
```

**Scala 3 마이그레이션 에러**

Scala 2 프로젝트를 Scala 3로 마이그레이션할 때 발생하는 일반적인 문제들입니다. 문법 변경 사항을 확인하고 코드를 수정해야 합니다.

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

**Metals (VS Code) 문제**

VS Code에서 Metals가 프로젝트를 제대로 인식하지 못하는 경우, 메타데이터를 삭제하고 다시 import하면 해결됩니다.

```bash
# 메타데이터 재생성
rm -rf .metals .bloop .bsp
# VS Code 재시작 후 "Import build" 클릭
```

{{< callout type="tip" title="핵심 포인트" >}}
- **JDK 문제**: `java -version` 확인, `JAVA_HOME` 설정
- **의존성 문제**: 캐시 삭제 (`rm -rf ~/.cache/coursier`)
- **IDE 문제**: 캐시 무효화 및 프로젝트 재빌드
- **메모리 부족**: `.sbtopts`에 `-J-Xmx4G` 추가
{{< /callout >}}

#### 다음 단계

환경 설정이 완료되었다면 기본 예제를 통해 Scala의 핵심 개념들을 실습해보세요.

- [기본 예제](basic/) — 핵심 개념 활용 예제
- [Scala 2 vs 3 비교](scala2-vs-scala3/) — 버전별 코드 비교

