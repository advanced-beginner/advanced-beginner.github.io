---
lastmod: "2026-03-23"
title: 성능 프로파일링
weight: 5
---

Scala 애플리케이션의 성능 병목을 찾고 최적화하는 방법을 안내합니다.

**소요 시간**: 약 20-25분

{{< callout type="tip" title="TL;DR" >}}
- **CPU 프로파일링**: JFR(Java Flight Recorder)로 핫스팟 식별
- **메모리 분석**: 힙 덤프를 VisualVM 또는 MAT로 분석
- **컬렉션 선택**: 용도에 맞는 컬렉션을 사용하면 성능이 크게 달라짐
- **최적화 기법**: `@specialized`, `@tailrec`, 박싱 회피
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- 애플리케이션의 응답 시간이 갑자기 느려진 경우
- 메모리 사용량이 지속적으로 증가하는 경우 (메모리 누수 의심)
- 어떤 컬렉션을 사용해야 할지 성능 관점에서 결정이 필요한 경우
- GC 일시 정지가 빈번하게 발생하는 경우

### 이 가이드가 다루지 않는 것

- **JVM 튜닝 (GC 옵션 등)**: JVM 공식 문서를 참조하세요
- **분산 시스템 성능 최적화**: 별도 주제입니다
- **Cats Effect / ZIO의 성능 최적화**: 해당 라이브러리 문서를 참조하세요

---

## 시작하기 전에

다음 환경이 준비되어 있는지 확인하세요:

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| JDK 버전 | 11+ (JFR 기본 포함) | `java -version` |
| Scala 버전 | 2.13.x 또는 3.x | `scala -version` |
| VisualVM (선택) | 최신 버전 | `visualvm --version` |

```bash
# JDK 버전 확인 (JFR은 JDK 11+에서 기본 제공)
java -version
# 출력 예: openjdk version "17.0.8"

# VisualVM 설치 (macOS)
brew install --cask visualvm
```

---

## 1단계: 프로파일링 도구 선택

어떤 문제를 해결하려는지에 따라 적절한 도구를 선택하세요:

{{< mermaid >}}
flowchart TD
    A["성능 문제 발생"] --> B{"어떤 종류의<br>문제인가?"}
    B -->|"CPU 사용량 높음"| C["JFR로 CPU<br>프로파일링"]
    B -->|"메모리 부족<br>OOM"| D["힙 덤프 분석<br>jmap + MAT"]
    B -->|"GC 일시 정지<br>빈번"| E["GC 로그 분석<br>JFR + GCViewer"]
    B -->|"응답 시간<br>불규칙"| F["JFR로 지연 시간<br>분석"]
    C --> G["핫스팟 메서드 식별"]
    D --> H["큰 객체/누수 식별"]
    E --> I["GC 튜닝 또는<br>할당 최적화"]
    F --> J["스레드 경합<br>I/O 대기 분석"]
{{< /mermaid >}}

---

## 2단계: JFR로 CPU/메모리 프로파일링

### 2.1 JFR 기본 사용

JFR(Java Flight Recorder)은 JDK 11+에 내장된 저오버헤드 프로파일러입니다:

```bash
# 실행 중인 애플리케이션에 JFR 연결
# 1. 먼저 PID 확인
jps -l
# 출력 예: 12345 com.example.MyApp

# 2. JFR 레코딩 시작 (60초)
jcmd 12345 JFR.start duration=60s filename=recording.jfr

# 3. 레코딩 확인
jcmd 12345 JFR.check

# 4. 레코딩 중지 (duration 전에 수동 종료)
jcmd 12345 JFR.stop
```

또는 JVM 시작 시 바로 활성화:

```bash
# sbt에서 JFR 활성화
sbt -J-XX:StartFlightRecording=duration=120s,filename=app.jfr run

# 또는 build.sbt에서 설정
javaOptions += "-XX:StartFlightRecording=duration=120s,filename=app.jfr"
```

### 2.2 JFR 결과 분석

```bash
# JDK Mission Control (JMC)로 분석
jmc  # GUI 도구 실행 후 .jfr 파일 열기

# CLI로 간단히 확인
jfr summary recording.jfr

# 이벤트 필터링
jfr print --events jdk.CPULoad recording.jfr
jfr print --events jdk.ObjectAllocationSample recording.jfr
```

**확인해야 할 핵심 이벤트**:

| 이벤트 | 설명 |
|--------|------|
| `jdk.CPULoad` | CPU 사용률 |
| `jdk.ExecutionSample` | 핫스팟 메서드 (CPU 프로파일링) |
| `jdk.ObjectAllocationSample` | 객체 할당 빈도 |
| `jdk.GCPhasePause` | GC 일시 정지 시간 |
| `jdk.ThreadPark` | 스레드 대기 |

---

## 3단계: 힙 덤프 분석

### 3.1 힙 덤프 생성

```bash
# 실행 중인 프로세스의 힙 덤프
jmap -dump:format=b,file=heap.hprof 12345

# OOM 발생 시 자동 덤프
sbt -J-XX:+HeapDumpOnOutOfMemoryError -J-XX:HeapDumpPath=./heap.hprof run
```

### 3.2 VisualVM으로 분석

```bash
# VisualVM 실행
visualvm

# 1. File > Load... > heap.hprof 선택
# 2. Summary 탭에서 가장 큰 객체 확인
# 3. Classes 탭에서 인스턴스 수가 많은 클래스 확인
```

**주요 확인 사항**:

| 지표 | 의심 상황 |
|------|----------|
| 인스턴스 수가 비정상적으로 많음 | 객체 누수 가능성 |
| 큰 배열이 많음 | 컬렉션 크기 과대 할당 |
| String 인스턴스가 지나치게 많음 | 문자열 중복 또는 누수 |
| 동일 클래스의 인스턴스가 계속 증가 | GC 되지 않는 참조 확인 |

### 3.3 Scala에서 흔한 메모리 누수 패턴

```scala
// 잘못된 예: 클로저가 외부 객체 전체를 참조
class DataProcessor {
  val largeData: Array[Byte] = new Array[Byte](100 * 1024 * 1024) // 100MB

  def getProcessor(): () => Unit = {
    // 이 클로저는 DataProcessor 전체(largeData 포함)를 참조함
    () => println("Processing...")
  }
}

// 올바른 예: 필요한 데이터만 캡처
class DataProcessor {
  val largeData: Array[Byte] = new Array[Byte](100 * 1024 * 1024)

  def getProcessor(): () => Unit = {
    val message = "Processing..."  // 필요한 값만 로컬에 복사
    () => println(message)
  }
}
```

---

## 4단계: 컬렉션 성능 특성

### 4.1 주요 컬렉션 비교

| 연산 | List | Vector | Array | ArrayBuffer |
|------|------|--------|-------|-------------|
| head | O(1) | O(1) | O(1) | O(1) |
| 인덱스 접근 | O(n) | O(log n) | **O(1)** | **O(1)** |
| append | O(n) | O(1)* | O(n) | O(1)* |
| prepend | **O(1)** | O(1)* | O(n) | O(n) |
| 순회 | O(n) | O(n) | **O(n)** | O(n) |

\* 분할 상환(amortized) 시간 복잡도

### 4.2 상황별 컬렉션 선택

```scala
// 1. 앞에서 추가/제거가 많은 경우 -> List
val stack = List(1, 2, 3)
val pushed = 0 :: stack        // O(1) prepend
val (head, tail) = (stack.head, stack.tail)  // O(1)

// 2. 랜덤 접근이 필요한 경우 -> Vector 또는 Array
val indexed = Vector(1, 2, 3)
indexed(1)                     // O(log n), 사실상 O(1)에 가까움

// 3. 성능이 가장 중요한 수치 연산 -> Array
val numbers = Array(1.0, 2.0, 3.0)
numbers(0)                     // O(1), 박싱 없음(primitive)

// 4. 가변 컬렉션이 필요한 경우 -> ArrayBuffer
import scala.collection.mutable.ArrayBuffer
val buffer = ArrayBuffer(1, 2, 3)
buffer += 4                    // O(1) amortized append
```

### 4.3 성능 벤치마크 예시

sbt 프로젝트에 JMH(Java Microbenchmark Harness)를 추가합니다:

```scala
// project/plugins.sbt
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.7")
```

```scala
// build.sbt
enablePlugins(JmhPlugin)
```

벤치마크 코드:

```scala
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class CollectionBenchmark {
  val size = 10000
  val list: List[Int] = (1 to size).toList
  val vector: Vector[Int] = (1 to size).toVector
  val array: Array[Int] = (1 to size).toArray

  @Benchmark
  def listIndexAccess(): Int = list(size / 2)

  @Benchmark
  def vectorIndexAccess(): Int = vector(size / 2)

  @Benchmark
  def arrayIndexAccess(): Int = array(size / 2)
}
```

```bash
# 벤치마크 실행
sbt "jmh:run -i 10 -wi 5 -f 2 CollectionBenchmark"
```

---

## 5단계: 박싱/언박싱 오버헤드

### 5.1 문제 이해

Scala의 제네릭은 JVM에서 타입 소거(erasure)되므로, `Int`, `Double` 등 원시 타입이 박싱(boxing)됩니다:

```scala
// 박싱이 발생하는 코드
def sum[A](list: List[A])(implicit num: Numeric[A]): A = {
  list.foldLeft(num.zero)(num.plus)
  // Int가 java.lang.Integer로 박싱/언박싱 반복
}

// 원시 타입을 직접 사용하면 박싱 없음
def sumInts(list: Array[Int]): Int = {
  var total = 0
  var i = 0
  while (i < list.length) {
    total += list(i)
    i += 1
  }
  total
}
```

### 5.2 @specialized 어노테이션

자주 사용되는 원시 타입에 대해 특수화된 구현을 생성합니다:

```scala
// @specialized 없이: 모든 타입이 Object로 박싱
class Container[A](val value: A)

// @specialized 사용: Int, Double 등에 대해 별도 클래스 생성
class Container[@specialized(Int, Double, Long) A](val value: A)

// 사용 시 박싱이 발생하지 않음
val intContainer = new Container[Int](42)       // Container$mcI$sp 사용
val doubleContainer = new Container[Double](3.14) // Container$mcD$sp 사용
```

{{< callout type="info" title="Scala 3 참고" >}}
Scala 3에서는 `@specialized` 대신 opaque type을 활용하는 것을 권장합니다:
```scala
opaque type Meters = Double
object Meters:
  def apply(d: Double): Meters = d
  extension (m: Meters) def value: Double = m
// 런타임에 박싱 없이 Double로 처리됨
```
{{< /callout >}}

---

## 6단계: 꼬리 재귀 최적화

### 6.1 @tailrec 어노테이션

재귀 함수에서 스택 오버플로를 방지합니다:

```scala
import scala.annotation.tailrec

// 잘못된 예: 꼬리 재귀가 아님 (스택 오버플로 위험)
def factorial(n: Long): Long = {
  if (n <= 1) 1
  else n * factorial(n - 1)  // 재귀 호출 후 곱셈이 있음
}

// 올바른 예: 꼬리 재귀 (컴파일러가 루프로 변환)
@tailrec
def factorial(n: Long, acc: Long = 1): Long = {
  if (n <= 1) acc
  else factorial(n - 1, n * acc)  // 마지막 연산이 재귀 호출
}

factorial(100000)  // 스택 오버플로 없음
```

### 6.2 @tailrec이 실패하는 경우

```scala
import scala.annotation.tailrec

// 컴파일 에러: could not optimize @tailrec annotated method
// 이유: 재귀 호출이 마지막 연산이 아님
// @tailrec
// def sum(list: List[Int]): Int = list match {
//   case Nil => 0
//   case head :: tail => head + sum(tail)  // + 연산이 마지막
// }

// 해결: 누산기(accumulator) 패턴 사용
@tailrec
def sum(list: List[Int], acc: Int = 0): Int = list match {
  case Nil => acc
  case head :: tail => sum(tail, acc + head)  // 재귀 호출이 마지막
}
```

### 6.3 트램폴린(Trampoline)으로 상호 재귀 최적화

상호 재귀(mutual recursion)는 `@tailrec`으로 최적화할 수 없습니다:

```scala
import scala.util.control.TailCalls._

def isEven(n: Long): TailRec[Boolean] = {
  if (n == 0) done(true)
  else tailcall(isOdd(n - 1))
}

def isOdd(n: Long): TailRec[Boolean] = {
  if (n == 0) done(false)
  else tailcall(isEven(n - 1))
}

// 스택 오버플로 없이 실행
isEven(1000000).result  // true
```

---

## 7단계: 흔한 실수와 해결

### 7.1 불필요한 중간 컬렉션 생성

```scala
// 잘못된 예: 중간 컬렉션이 3개 생성됨
val result = (1 to 1000000)
  .map(_ * 2)       // 중간 컬렉션 1
  .filter(_ > 100)  // 중간 컬렉션 2
  .take(10)          // 중간 컬렉션 3

// 올바른 예: view로 지연 평가
val result = (1 to 1000000).view
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList  // 최종 결과만 생성

// 또는 iterator 사용
val result = (1 to 1000000).iterator
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList
```

### 7.2 문자열 연결 성능

```scala
// 잘못된 예: O(n^2) - 매 연결마다 새 String 생성
var result = ""
for (i <- 1 to 10000) {
  result += s"item-$i,"  // 매번 복사 발생
}

// 올바른 예: StringBuilder 사용 - O(n)
val sb = new StringBuilder
for (i <- 1 to 10000) {
  sb.append(s"item-$i,")
}
val result = sb.toString()

// 또는 mkString 사용
val result = (1 to 10000).map(i => s"item-$i").mkString(",")
```

### 7.3 과도한 패턴 매칭 중첩

```scala
// 성능에 영향이 있을 수 있는 깊은 중첩
def process(data: Any): String = data match {
  case list: List[_] => list.headOption match {
    case Some(map: Map[_, _]) => map.headOption match {
      case Some((k, v)) => s"$k -> $v"
      case None => "empty map"
    }
    case _ => "not a map list"
  }
  case _ => "unknown"
}

// 타입을 명확히 하여 패턴 매칭 단순화
case class ProcessInput(data: List[Map[String, String]])

def process(input: ProcessInput): String = {
  input.data.headOption
    .flatMap(_.headOption)
    .map { case (k, v) => s"$k -> $v" }
    .getOrElse("empty")
}
```

---

## 체크리스트

성능 최적화 시 확인사항:

- [ ] **프로파일링을 먼저 했는가?** - 추측이 아닌 측정 기반으로 최적화
- [ ] **JFR 레코딩으로 핫스팟을 식별했는가?** - 가장 많은 CPU를 소비하는 메서드
- [ ] **메모리 누수가 없는가?** - 힙 덤프 분석으로 확인
- [ ] **적절한 컬렉션을 사용하고 있는가?** - 용도에 맞는 컬렉션 선택
- [ ] **불필요한 박싱이 없는가?** - 숫자 연산에서 원시 타입 활용
- [ ] **view/iterator로 지연 평가를 활용하고 있는가?** - 중간 컬렉션 제거

**모든 항목을 확인했는데도 성능이 부족하다면**, JMH 벤치마크를 작성하여 정확한 병목을 측정하세요.

---

## 관련 문서

- [타입 에러 디버깅]({{< relref "/docs/scala/howto/type-error-debugging" >}}) - 타입 관련 컴파일 에러 해결
- [sbt 의존성 충돌 해결]({{< relref "/docs/scala/howto/sbt-dependency-conflicts" >}}) - 빌드 의존성 문제 해결
- [Future 에러 처리]({{< relref "/docs/scala/howto/future-error-handling" >}}) - 비동기 코드 성능과 에러 처리
