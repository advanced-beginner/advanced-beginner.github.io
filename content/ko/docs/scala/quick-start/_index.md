---
bookCollapseSection: true
lastmod: "2026-01-10"
title: Quick Start
weight: 1
---

> **대상 독자**: Scala를 처음 시작하는 Java/Kotlin 개발자 또는 함수형 프로그래밍에 관심 있는 개발자
> **선수 지식**: 프로그래밍 기본 개념, 터미널/명령줄 사용 경험
> **이 문서를 읽으면**: Scala 개발 환경을 구축하고, REPL과 sbt 프로젝트에서 간단한 코드를 실행할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- Coursier(`cs setup`)로 Scala, sbt를 한 번에 설치합니다
- `val`(불변), `var`(가변), `def`(함수)가 Scala의 기본 키워드입니다
- sbt는 Scala 표준 빌드 도구로, `sbt run`으로 프로젝트를 실행합니다
{{< /callout >}}

5분 만에 Scala를 설치하고 첫 번째 프로그램을 실행해봅니다. 이 가이드는 설치부터 sbt 프로젝트 생성, IDE 설정까지 실무에서 바로 사용할 수 있는 환경을 구축하는 과정을 안내합니다.

> 🎯 **설치 없이 바로 실행:** [Scastie](https://scastie.scala-lang.org/)에서 브라우저로 Scala를 바로 실행해볼 수 있습니다!

**시작 전 확인**

| 항목 | 확인 명령어 | 예상 결과 |
|------|-------------|-----------|
| Java (선택) | `java -version` | `openjdk version "17.x.x"` (sbt가 자동 설치 가능) |
| 터미널 | - | bash, zsh, PowerShell 중 하나 |

## Step 1/5: Scala 설치 (~2분)

Scala를 설치하는 가장 쉬운 방법은 Coursier를 사용하는 것입니다. Coursier는 Scala, sbt, 그리고 다양한 Scala 도구를 한 번에 설치해주는 공식 설치 도구입니다.

**Coursier로 설치 (권장)**

Coursier는 Scala 생태계의 표준 설치 도구입니다.

{{< tabs groupid="os" >}}
{{% tab title="macOS" %}}
```bash
# Homebrew로 Coursier 설치
brew install coursier/formulas/coursier

# Scala 설치 (Scala 3가 기본)
cs setup
```
{{% /tab %}}
{{% tab title="Linux" %}}
```bash
# Coursier 설치 스크립트
curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
./cs setup
```
{{% /tab %}}
{{% tab title="Windows" %}}
```powershell
# PowerShell에서 실행
Invoke-WebRequest -Uri "https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-win32.zip" -OutFile "cs.zip"
Expand-Archive -Path "cs.zip"
.\cs\cs.exe setup
```
{{% /tab %}}
{{< /tabs >}}

설치 후 새 터미널을 열고 버전을 확인합니다:

```bash
scala --version
# Scala code runner version 3.x.x

sbt --version
# sbt script version: 1.x.x
```

**특정 버전 설치**

프로젝트 요구사항에 따라 특정 Scala 버전을 설치해야 할 수 있습니다. 특히 Apache Spark를 사용하는 경우 Scala 2.13이 필요합니다.

```bash
# Scala 3 최신
cs install scala:3

# Scala 2.13 (Spark 등에서 필요)
cs install scala:2.13.12
```

## Step 2/5: Hello World (~1분)

Scala의 기본 문법을 확인하는 가장 좋은 방법은 간단한 Hello World 프로그램을 작성하는 것입니다. REPL에서 바로 실행하거나 파일로 저장해서 실행할 수 있습니다.

**REPL에서 실행**

Scala REPL(Read-Eval-Print Loop)로 바로 코드를 실행해봅니다:

```bash
scala
```

```scala
scala> println("Hello, Scala!")
Hello, Scala!

scala> val name = "World"
val name: String = World

scala> println(s"Hello, $name!")
Hello, World!

scala> :quit
```

**파일로 실행**

코드를 파일로 저장하면 재사용과 버전 관리가 가능합니다. Scala 3는 들여쓰기 기반의 간결한 문법을 지원하고, Scala 2 스타일의 중괄호 문법도 계속 사용할 수 있습니다.

**Scala 3 (들여쓰기 기반):**

```scala
// hello.scala
@main def hello() =
  val message = "Hello, Scala 3!"
  println(message)
```

```bash
scala hello.scala
# Hello, Scala 3!
```

**Scala 2 스타일 (중괄호 기반):**

```scala
// Hello.scala
object Hello {
  def main(args: Array[String]): Unit = {
    val message = "Hello, Scala 2!"
    println(message)
  }
}
```

```bash
scala Hello.scala
# Hello, Scala 2!
```

## Step 3/5: sbt 프로젝트 생성 (~3분)

실제 프로젝트에서는 sbt(Scala Build Tool)를 사용합니다. sbt는 의존성 관리, 컴파일, 테스트, 패키징 등 프로젝트 빌드에 필요한 모든 기능을 제공하는 Scala 표준 빌드 도구입니다.

**프로젝트 생성**

```bash
# 새 디렉토리 생성
mkdir scala-quickstart && cd scala-quickstart

# sbt 프로젝트 초기화
sbt new scala/scala3.g8
# 또는 Scala 2: sbt new scala/hello-world.g8
```

프롬프트에서 프로젝트 이름을 입력하면 기본 구조가 생성됩니다.

**수동 프로젝트 구성**

템플릿 없이 직접 프로젝트를 구성하면 구조를 더 잘 이해할 수 있습니다. 최소한 build.sbt 파일과 소스 디렉토리만 있으면 됩니다.

```bash
mkdir -p src/main/scala
```

**build.sbt** (Scala 3):
```scala
val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-quickstart",
    version := "0.1.0",
    scalaVersion := scala3Version
  )
```

**build.sbt** (Scala 2.13):
```scala
val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-quickstart",
    version := "0.1.0",
    scalaVersion := scala2Version
  )
```

**project/build.properties**:
```properties
sbt.version=1.10.6
```

> 💡 **팁:** sbt 최신 버전은 [sbt 릴리스 페이지](https://github.com/sbt/sbt/releases)에서 확인하세요.

**src/main/scala/Main.scala** (Scala 3):
```scala
@main def run(): Unit =
  println("Hello from sbt project!")
```

**실행**

sbt로 프로젝트를 실행하면 의존성 해결, 컴파일, 실행까지 자동으로 처리됩니다.

```bash
sbt run
# [info] running run
# Hello from sbt project!
```

**자주 사용하는 sbt 명령어**

아래 표는 일상적인 개발에서 가장 자주 사용하는 sbt 명령어들입니다. `~compile`처럼 물결표(~)를 붙이면 파일 변경을 감지하여 자동으로 해당 명령을 다시 실행합니다.

| 명령어 | 설명 |
|--------|------|
| `sbt run` | 메인 클래스 실행 |
| `sbt compile` | 컴파일 |
| `sbt test` | 테스트 실행 |
| `sbt console` | REPL 실행 (프로젝트 의존성 포함) |
| `sbt ~compile` | 파일 변경 시 자동 컴파일 |

## Step 4/5: IDE 설정 (~2분)

효율적인 Scala 개발을 위해서는 IDE 설정이 중요합니다. 코드 자동 완성, 타입 검사, 리팩토링 지원 등을 받을 수 있습니다. 두 가지 주요 선택지가 있습니다.

**IntelliJ IDEA (권장)**

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) 설치 (Community Edition 무료)
2. **Plugins** → "Scala" 검색 → 설치
3. **File** → **Open** → sbt 프로젝트 폴더 선택
4. "Import as sbt project" 선택

**VS Code + Metals**

VS Code는 가볍고 빠른 에디터로, Metals 확장을 통해 Scala Language Server Protocol(LSP) 지원을 받을 수 있습니다.

1. [VS Code](https://code.visualstudio.com/) 설치
2. Extensions에서 "Metals" 검색 → 설치
3. sbt 프로젝트 폴더 열기
4. "Import build" 클릭

## Step 5/5: 간단한 예제 (~2분)

설치가 완료되었으니 Scala의 기본 문법을 간단한 예제로 살펴봅니다. 아래 코드들은 Scala의 핵심적인 특징인 타입 추론, 불변성, 함수형 컬렉션 연산을 보여줍니다.

> 💻 아래 예제들을 [Scastie](https://scastie.scala-lang.org/)에서 직접 실행해보세요!

**변수와 타입**

```scala
// 불변 (권장)
val name: String = "Scala"
val year = 2024  // 타입 추론

// 가변 (필요한 경우만)
var count = 0
count = count + 1

// 기본 타입
val number: Int = 42
val pi: Double = 3.14
val isScala: Boolean = true
val char: Char = 'S'
```

**함수 정의**

Scala에서 함수는 `def` 키워드로 정의합니다. 반환 타입은 대부분 추론되지만, 명시적으로 지정하는 것이 좋은 습관입니다.

```scala
// Scala 3
def greet(name: String): String =
  s"Hello, $name!"

// Scala 2 스타일
def add(a: Int, b: Int): Int = {
  a + b
}

// 호출
println(greet("World"))  // Hello, World!
println(add(1, 2))       // 3
```

**컬렉션 맛보기**

Scala 컬렉션은 함수형 프로그래밍의 강력함을 보여줍니다. `map`, `filter`, `reduce` 등의 고차 함수로 데이터를 선언적으로 변환할 수 있습니다.

```scala
val numbers = List(1, 2, 3, 4, 5)

// 변환
val doubled = numbers.map(n => n * 2)
// List(2, 4, 6, 8, 10)

// 필터링
val evens = numbers.filter(n => n % 2 == 0)
// List(2, 4)

// 축약형
val sum = numbers.reduce(_ + _)
// 15
```

## 다음 단계

{{< callout type="success" title="🎉 축하합니다!" >}}
Scala Quick Start를 성공적으로 완료했습니다! 이제 여러분은:
- Scala 개발 환경을 구축했습니다
- REPL에서 코드를 실행할 수 있습니다
- sbt 프로젝트를 생성하고 관리할 수 있습니다
- IDE에서 Scala 프로젝트를 열고 작업할 수 있습니다
{{< /callout >}}

환경 설정이 끝났으니 이제 Scala의 핵심 개념들을 하나씩 배워볼 차례입니다.

1. **[기본 문법](../concepts/basics/)** — 변수, 타입, 타입 추론 자세히 배우기
2. **[제어 구조](../concepts/control-structures/)** — if, for, match 표현식
3. **[함수와 메서드](../concepts/functions-methods/)** — 함수 정의와 고급 기능

## 트러블슈팅

설치나 실행 중 문제가 발생하면 아래 해결책을 참고하세요. 대부분의 문제는 환경 변수 설정이나 캐시 관련입니다.

**`scala` 명령어를 찾을 수 없음**

```bash
# PATH 확인
echo $PATH | grep coursier

# PATH 재설정
source ~/.bashrc  # 또는 ~/.zshrc
```

**sbt가 느림**

처음 실행 시 의존성 다운로드로 느릴 수 있습니다. 두 번째부터는 캐시를 사용하므로 빨라집니다. 메모리 부족이 원인인 경우 아래 설정으로 JVM 메모리를 늘릴 수 있습니다.

```bash
# sbt 메모리 설정 (선택)
export SBT_OPTS="-Xmx2G"
```

**IntelliJ에서 빨간 줄이 표시됨**

IntelliJ의 인덱스가 손상되었거나 sbt 프로젝트 설정이 동기화되지 않은 경우 발생합니다. 대부분 캐시 무효화로 해결됩니다.

1. **File** → **Invalidate Caches** → **Restart**
2. 또는 프로젝트 reimport: **sbt** 탭 → **Reload**
