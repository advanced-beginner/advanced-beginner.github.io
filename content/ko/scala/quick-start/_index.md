---
lastmod: "2026-01-06"
title: Quick Start
weight: 1
---

5분 만에 Scala를 설치하고 첫 번째 프로그램을 실행해봅니다.

> 🎯 **설치 없이 바로 실행:** [Scastie](https://scastie.scala-lang.org/)에서 브라우저로 Scala를 바로 실행해볼 수 있습니다!

## 1. Scala 설치

### Coursier로 설치 (권장)

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

### 특정 버전 설치

```bash
# Scala 3 최신
cs install scala:3

# Scala 2.13 (Spark 등에서 필요)
cs install scala:2.13.12
```

## 2. Hello World

### REPL에서 실행

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

### 파일로 실행

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

## 3. sbt 프로젝트 생성

실제 프로젝트에서는 sbt(Scala Build Tool)를 사용합니다.

### 프로젝트 생성

```bash
# 새 디렉토리 생성
mkdir scala-quickstart && cd scala-quickstart

# sbt 프로젝트 초기화
sbt new scala/scala3.g8
# 또는 Scala 2: sbt new scala/hello-world.g8
```

프롬프트에서 프로젝트 이름을 입력하면 기본 구조가 생성됩니다.

### 수동 프로젝트 구성

직접 구성하려면:

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

### 실행

```bash
sbt run
# [info] running run
# Hello from sbt project!
```

### 자주 사용하는 sbt 명령어

| 명령어 | 설명 |
|--------|------|
| `sbt run` | 메인 클래스 실행 |
| `sbt compile` | 컴파일 |
| `sbt test` | 테스트 실행 |
| `sbt console` | REPL 실행 (프로젝트 의존성 포함) |
| `sbt ~compile` | 파일 변경 시 자동 컴파일 |

## 4. IDE 설정

### IntelliJ IDEA (권장)

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) 설치 (Community Edition 무료)
2. **Plugins** → "Scala" 검색 → 설치
3. **File** → **Open** → sbt 프로젝트 폴더 선택
4. "Import as sbt project" 선택

### VS Code + Metals

1. [VS Code](https://code.visualstudio.com/) 설치
2. Extensions에서 "Metals" 검색 → 설치
3. sbt 프로젝트 폴더 열기
4. "Import build" 클릭

## 5. 간단한 예제

> 💻 아래 예제들을 [Scastie](https://scastie.scala-lang.org/)에서 직접 실행해보세요!

### 변수와 타입

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

### 함수 정의

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

### 컬렉션 맛보기

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

Quick Start를 완료했습니다! 다음으로 진행하세요:

1. **[기본 문법](../concepts/basics/)** — 변수, 타입, 타입 추론 자세히 배우기
2. **[제어 구조](../concepts/control-structures/)** — if, for, match 표현식
3. **[함수와 메서드](../concepts/functions-methods/)** — 함수 정의와 고급 기능

## 트러블슈팅

### `scala` 명령어를 찾을 수 없음

```bash
# PATH 확인
echo $PATH | grep coursier

# PATH 재설정
source ~/.bashrc  # 또는 ~/.zshrc
```

### sbt가 느림

처음 실행 시 의존성 다운로드로 느릴 수 있습니다. 두 번째부터는 빨라집니다.

```bash
# sbt 메모리 설정 (선택)
export SBT_OPTS="-Xmx2G"
```

### IntelliJ에서 빨간 줄이 표시됨

1. **File** → **Invalidate Caches** → **Restart**
2. 또는 프로젝트 reimport: **sbt** 탭 → **Reload**
