---
lastmod: "2026-01-15"
title: 패턴 매칭
description: "패턴 매칭의 작동 원리와 다양한 매칭 전략을 설명합니다."
weight: 6
---

## 전체 비유: 우편 분류 센터

패턴 매칭을 <strong>우편 분류 센터</strong>에 비유하면 이해하기 쉽습니다:

| 우편 분류 비유 | Scala 개념 | 역할 |
|--------------|-----------|------|
| 우편번호로 분류 | 리터럴 패턴 | 정확한 값 매칭 |
| 크기/무게로 분류 | 타입 패턴 | 런타임 타입 검사 |
| 내용물 확인 | 케이스 클래스 패턴 | 구조 분해 |
| 조건부 특송 | 가드 (if) | 추가 조건 검사 |
| 수취인 정보 추출 | 변수 바인딩 | 값 추출 |
| 기타 분류함 | 와일드카드 (_) | 기본값 처리 |
| 스티커 부착 + 분류 | @ 바인딩 | 전체+부분 동시 접근 |
| 분류 규칙 정의 | 추출자 (unapply) | 커스텀 패턴 |

이처럼 우편물을 다양한 기준으로 분류하고 정보를 추출하듯, 패턴 매칭은 값을 분석하고 분기합니다.

---

{{< callout type="info" title="TL;DR" >}}
- 패턴 매칭은 값의 구조를 분석하고 데이터를 추출하는 강력한 기능입니다
- 리터럴, 변수, 타입, 튜플, 케이스 클래스, 시퀀스 등 다양한 패턴을 지원합니다
- 가드(`if`)로 추가 조건을, `@`로 전체 값 바인딩을 할 수 있습니다
- `sealed` 타입에서는 컴파일러가 완전성을 검사합니다
{{< /callout >}}

**대상 독자:** Scala 기본 문법을 익힌 개발자
**선수 지식:** 케이스 클래스, 기본 타입 시스템

패턴 매칭은 Scala의 가장 강력한 기능 중 하나입니다. 값의 구조를 분석하고, 데이터를 추출하며, 조건에 따라 분기하는 것을 우아하게 처리합니다. Java의 switch문과 달리 Scala의 match는 표현식이므로 값을 반환하고, 타입 매칭, 구조 분해, 가드 조건 등 훨씬 풍부한 기능을 제공합니다.

#### 기본 match 표현식

match 표현식은 값을 여러 패턴과 순서대로 비교하여 첫 번째로 매칭되는 케이스의 결과를 반환합니다. 와일드카드 패턴(`_`)은 모든 값과 매칭되므로 기본값으로 사용합니다.

```scala
val x = 3

val result = x match {
  case 1 => "one"
  case 2 => "two"
  case 3 => "three"
  case _ => "other"  // 와일드카드 (기본값)
}
println(result)  // three
```

#### 패턴의 종류

Scala는 다양한 종류의 패턴을 지원합니다. 리터럴 값, 변수 바인딩, 타입 검사, 튜플, 케이스 클래스, 시퀀스 등을 패턴으로 매칭할 수 있습니다.

**1. 리터럴 패턴**

정수, 문자열, 불리언 등 리터럴 값과 직접 매칭합니다.

```scala
def describe(x: Any): String = x match {
  case 0     => "영"
  case true  => "참"
  case "hi"  => "인사"
  case null  => "널"
  case _     => "기타"
}
```

**2. 변수 패턴**

소문자로 시작하는 이름은 변수 패턴으로, 매칭된 값을 바인딩합니다. 대문자로 시작하면 상수 참조로 해석됩니다.

```scala
val x = 42

x match {
  case n => println(s"값은 $n")  // n에 x가 바인딩됨
}

// 주의: 변수 이름이 소문자로 시작하면 변수 패턴
// 대문자로 시작하면 상수 참조
val One = 1
val two = 2

x match {
  case One => "상수 One과 매칭"
  case two => "모든 값과 매칭됨 (변수 패턴)"
  // case `two` => "백틱으로 상수로 취급"
}
```

**3. 타입 패턴**

값의 런타임 타입을 검사하고 해당 타입으로 캐스팅합니다. isInstanceOf/asInstanceOf보다 안전합니다.

```scala
def describe(x: Any): String = x match {
  case i: Int       => s"정수: $i"
  case s: String    => s"문자열: $s (길이: ${s.length})"
  case d: Double    => s"실수: $d"
  case l: List[_]   => s"리스트 (길이: ${l.length})"
  case _            => "알 수 없는 타입"
}

println(describe(42))          // 정수: 42
println(describe("hello"))     // 문자열: hello (길이: 5)
println(describe(List(1,2,3))) // 리스트 (길이: 3)
```

**4. 튜플 패턴**

튜플의 각 요소를 분해하여 매칭합니다. 중첩 튜플도 지원됩니다.

```scala
val pair = (1, "one")

pair match {
  case (1, s)    => s"일: $s"
  case (2, s)    => s"이: $s"
  case (n, s)    => s"$n: $s"
}

// 중첩 튜플
val nested = ((1, 2), (3, 4))
nested match {
  case ((a, b), (c, d)) => s"$a, $b, $c, $d"
}
```

**5. 케이스 클래스 패턴**

케이스 클래스의 필드를 분해하여 매칭합니다. 중첩된 케이스 클래스도 깊이 분해할 수 있습니다.

```scala
case class Person(name: String, age: Int)
case class Address(city: String, zipCode: String)
case class Employee(person: Person, address: Address)

val emp = Employee(Person("김철수", 30), Address("서울", "12345"))

emp match {
  case Employee(Person(name, age), Address(city, _)) =>
    s"$name ($age세), $city 거주"
}
```

**6. 시퀀스 패턴**

리스트나 배열 같은 시퀀스의 구조를 매칭합니다. `::` 연산자로 head와 tail을 분리하거나, `_*`로 나머지 요소를 무시할 수 있습니다.

```scala
val list = List(1, 2, 3, 4, 5)

list match {
  case Nil            => "빈 리스트"
  case head :: Nil    => s"원소 하나: $head"
  case head :: tail   => s"첫 번째: $head, 나머지: $tail"
}

// 특정 패턴
list match {
  case List(1, 2, _*) => "1, 2로 시작"  // Scala 3
  // case List(1, 2, _*) => "1, 2로 시작"  // Scala 2도 동일
  case _              => "다른 패턴"
}

// 길이 체크
list match {
  case List(a)          => s"원소 1개: $a"
  case List(a, b)       => s"원소 2개: $a, $b"
  case List(a, b, c)    => s"원소 3개: $a, $b, $c"
  case _ :: _ :: _ :: _ => "원소 4개 이상"
  case _                => "빈 리스트"
}
```

**7. OR 패턴**

`|` 연산자로 여러 패턴을 하나의 케이스로 묶을 수 있습니다. 같은 동작을 수행해야 하는 여러 값을 처리할 때 유용합니다.

```scala
val day = "Monday"

day match {
  case "Saturday" | "Sunday" => "주말"
  case _                     => "평일"
}

// 숫자
val n = 5
n match {
  case 1 | 2 | 3 => "작은 수"
  case 4 | 5 | 6 => "중간 수"
  case _         => "큰 수"
}
```

{{< callout type="info" title="핵심 포인트" >}}
- **리터럴 패턴**: 정확한 값과 매칭
- **변수 패턴**: 소문자로 시작, 값을 바인딩
- **타입 패턴**: 런타임 타입 검사와 안전한 캐스팅
- **튜플/케이스 클래스 패턴**: 구조 분해로 필드 추출
- **시퀀스 패턴**: `::`, `_*`로 리스트 분해
- **OR 패턴**: `|`로 여러 패턴을 하나의 케이스로 묶음
{{< /callout >}}

#### 가드 (Guard)

`if` 조건으로 패턴을 더 제한할 수 있습니다. 패턴이 먼저 매칭된 후 가드 조건이 평가됩니다. 가드가 false면 다음 케이스로 넘어갑니다.

```scala
def classify(n: Int): String = n match {
  case x if x < 0   => "음수"
  case x if x == 0  => "영"
  case x if x < 10  => "한 자리 양수"
  case x if x < 100 => "두 자리 양수"
  case _            => "세 자리 이상"
}

// 케이스 클래스와 함께
case class Person(name: String, age: Int)

def describe(p: Person): String = p match {
  case Person(_, age) if age < 0   => "잘못된 나이"
  case Person(name, _) if name.isEmpty => "이름 없음"
  case Person(name, age) if age < 18 => s"$name 은(는) 미성년자"
  case Person(name, age)             => s"$name 은(는) 성인"
}
```

#### 패턴 바인딩 (@)

`@` 연산자로 전체 값을 변수에 바인딩하면서 동시에 내부 구조를 분해할 수 있습니다. 패턴 매칭 후 원본 객체가 필요할 때 유용합니다.

```scala
case class Person(name: String, age: Int)

val person = Person("Alice", 30)

person match {
  case p @ Person(_, age) if age >= 18 =>
    println(s"성인: $p")  // 전체 Person 객체 사용
  case _ =>
    println("미성년자")
}

// 리스트에서 활용
List(1, 2, 3) match {
  case all @ (first :: rest) =>
    println(s"전체: $all, 첫 번째: $first, 나머지: $rest")
  case _ =>
    println("빈 리스트")
}
```

#### 추출자 (Extractor)

`unapply` 메서드를 정의하여 커스텀 패턴을 만들 수 있습니다. 케이스 클래스가 아닌 타입에 대해서도 패턴 매칭을 지원하려면 추출자를 직접 정의합니다.

```scala
object Even {
  def unapply(n: Int): Boolean = n % 2 == 0
}

object Odd {
  def unapply(n: Int): Boolean = n % 2 != 0
}

42 match {
  case Even() => "짝수"
  case Odd()  => "홀수"
}

// 값을 추출하는 추출자
object Email {
  def unapply(email: String): Option[(String, String)] = {
    val parts = email.split("@")
    if (parts.length == 2) Some((parts(0), parts(1)))
    else None
  }
}

"user@example.com" match {
  case Email(user, domain) => s"사용자: $user, 도메인: $domain"
  case _ => "유효하지 않은 이메일"
}
```

{{< callout type="info" title="핵심 포인트" >}}
- **가드(`if`)**: 패턴 매칭 후 추가 조건 검사
- **패턴 바인딩(`@`)**: 전체 값을 변수에 바인딩하면서 내부 구조 분해
- **추출자(`unapply`)**: 커스텀 패턴을 정의하여 패턴 매칭 확장
{{< /callout >}}

#### Scala 3 새로운 기능

Scala 3에서는 패턴 매칭에 여러 새로운 기능이 추가되었습니다.

**들여쓰기 기반 문법**

중괄호 대신 들여쓰기로 match 블록을 정의할 수 있습니다.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 중괄호 없이 들여쓰기로 블록 정의
val x: Any = "hello"

x match
  case s: String => s"문자열: $s"
  case i: Int    => s"정수: $i"
  case _         => "기타"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val x: Any = "hello"

x match {
  case s: String => s"문자열: $s"
  case i: Int    => s"정수: $i"
  case _         => "기타"
}
```
{{% /tab %}}
{{< /tabs >}}

**@switch 어노테이션**

`@switch` 어노테이션은 컴파일러가 점프 테이블을 생성하도록 보장합니다. 점프 테이블이 생성되지 않으면 컴파일 에러가 발생합니다.

```scala
import scala.annotation.switch

// 컴파일러가 점프 테이블 생성을 보장
def dayOfWeek(n: Int): String = (n: @switch) match
  case 1 => "월"
  case 2 => "화"
  case 3 => "수"
  case 4 => "목"
  case 5 => "금"
  case 6 => "토"
  case 7 => "일"
  case _ => "?"
```

**Match Types (Scala 3 전용)**

Match Types는 타입 레벨에서 패턴 매칭을 수행하는 Scala 3의 강력한 기능입니다. 입력 타입에 따라 다른 출력 타입을 정의할 수 있습니다.

```scala
// 타입에 따라 반환 타입이 결정됨
type Elem[X] = X match
  case String      => Char
  case Array[t]    => t
  case Iterable[t] => t

// 컴파일 타임에 타입이 결정됨
val char: Elem[String] = 'a'        // Char
val int: Elem[Array[Int]] = 1       // Int
val str: Elem[List[String]] = "hi"  // String
```

> 💡 Match Types는 고급 기능으로, 타입 레벨 프로그래밍에 사용됩니다. 자세한 내용은 [고급 타입](type-system-advanced/)을 참조하세요.

#### 패턴 매칭이 사용되는 곳

패턴 매칭은 match 표현식 외에도 다양한 곳에서 사용됩니다.

**1. val 정의**

변수 선언 시 패턴으로 값을 분해할 수 있습니다.

```scala
val (a, b) = (1, 2)
val Person(name, age) = Person("Alice", 30)
val head :: tail = List(1, 2, 3)
```

**2. for 표현식**

for comprehension에서 패턴을 사용하면 값을 분해하면서 순회할 수 있습니다. 패턴이 매칭되지 않는 요소는 자동으로 필터링됩니다.

```scala
val pairs = List((1, "one"), (2, "two"), (3, "three"))

for ((num, str) <- pairs) {
  println(s"$num = $str")
}

// Option 필터링
val maybeValues = List(Some(1), None, Some(3))
for (Some(x) <- maybeValues) {
  println(x)  // 1, 3 (None은 건너뜀)
}
```

**3. catch 절**

try-catch에서 예외를 패턴으로 매칭합니다.

```scala
try {
  // 위험한 코드
} catch {
  case e: NumberFormatException => "숫자 형식 오류"
  case e: IllegalArgumentException => "잘못된 인자"
  case e: Exception => s"기타 오류: ${e.getMessage}"
}
```

**4. 부분 함수**

PartialFunction은 일부 입력에 대해서만 정의된 함수입니다. 패턴 매칭과 함께 사용하면 특정 조건을 만족하는 값만 처리할 수 있습니다.

```scala
val divide: PartialFunction[(Int, Int), Int] = {
  case (a, b) if b != 0 => a / b
}

println(divide.isDefinedAt((10, 2)))  // true
println(divide.isDefinedAt((10, 0)))  // false
println(divide((10, 2)))              // 5

// collect에서 활용
val pairs = List((10, 2), (20, 0), (30, 3))
val results = pairs.collect {
  case (a, b) if b != 0 => a / b
}
println(results)  // List(5, 10)
```

{{< callout type="info" title="핵심 포인트" >}}
- 패턴 매칭은 match 외에도 val, for, catch, PartialFunction에서 사용됩니다
- PartialFunction은 `collect`와 함께 필터링+변환을 수행합니다
- for 표현식에서 매칭되지 않는 요소는 자동으로 필터링됩니다
{{< /callout >}}

#### 완전성 검사 (Exhaustiveness)

`sealed` 타입에 대한 패턴 매칭에서 컴파일러가 모든 케이스를 검사합니다. 누락된 케이스가 있으면 경고를 발생시켜 런타임 에러를 방지합니다.

```scala
sealed trait Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color

// 경고: match may not be exhaustive
def describe(c: Color): String = c match {
  case Red   => "빨강"
  case Green => "초록"
  // Blue 누락 - 경고!
}

// @unchecked로 경고 억제 (권장하지 않음)
def describe2(c: Color): String = (c: @unchecked) match {
  case Red => "빨강"
}
```

#### 흔한 실수와 Anti-patterns

패턴 매칭을 사용할 때 흔히 발생하는 실수와 올바른 해결 방법을 정리했습니다.

**❌ 피해야 할 것**

```scala
// 1. match 대신 isInstanceOf/asInstanceOf 사용
def process(x: Any): String = {
  if (x.isInstanceOf[Int]) x.asInstanceOf[Int].toString
  else if (x.isInstanceOf[String]) x.asInstanceOf[String]
  else "unknown"
}  // 타입 안전하지 않음!

// 2. 와일드카드 패턴이 먼저 오는 경우
x match {
  case _ => "default"     // 항상 매칭됨!
  case n: Int => n.toString  // 도달 불가 코드
}

// 3. Option에서 get 사용
val opt: Option[Int] = Some(5)
opt.get  // None이면 NoSuchElementException!

// 4. 불완전한 패턴 매칭 (sealed 타입)
sealed trait Color
case object Red extends Color
case object Blue extends Color

def name(c: Color) = c match {
  case Red => "red"
  // Blue 누락 - 런타임 에러 가능!
}
```

**✅ 올바른 방법**

```scala
// 1. 패턴 매칭 사용
def process(x: Any): String = x match {
  case n: Int => n.toString
  case s: String => s
  case _ => "unknown"
}

// 2. 구체적인 패턴을 먼저
x match {
  case n: Int => n.toString
  case _ => "default"
}

// 3. 패턴 매칭이나 getOrElse 사용
opt match {
  case Some(n) => n.toString
  case None => "default"
}
// 또는
opt.getOrElse(0)
opt.fold("default")(_.toString)

// 4. 모든 케이스 처리
def name(c: Color) = c match {
  case Red => "red"
  case Blue => "blue"  // 완전한 매칭
}
```

#### 연습 문제

다음 연습 문제들을 통해 패턴 매칭을 복습해보세요.

**1. 리스트 합계 ⭐**

패턴 매칭으로 리스트의 합을 계산하는 재귀 함수를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
def sum(list: List[Int]): Int = list match {
  case Nil         => 0
  case head :: tail => head + sum(tail)
}

println(sum(List(1, 2, 3, 4, 5)))  // 15
```

</details>

**2. JSON 파서 ⭐⭐**

간단한 JSON 값을 나타내는 ADT와 출력 함수를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
sealed trait Json
case class JString(value: String) extends Json
case class JNumber(value: Double) extends Json
case class JBool(value: Boolean) extends Json
case object JNull extends Json
case class JArray(items: List[Json]) extends Json
case class JObject(fields: Map[String, Json]) extends Json

def stringify(json: Json): String = json match {
  case JString(s)    => s"\"$s\""
  case JNumber(n)    => n.toString
  case JBool(b)      => b.toString
  case JNull         => "null"
  case JArray(items) => items.map(stringify).mkString("[", ",", "]")
  case JObject(fields) =>
    fields.map { case (k, v) => s"\"$k\":${stringify(v)}" }
          .mkString("{", ",", "}")
}

val json = JObject(Map(
  "name" -> JString("Alice"),
  "age" -> JNumber(30),
  "active" -> JBool(true)
))

println(stringify(json))
// {"name":"Alice","age":30.0,"active":true}
```

</details>

**3. 추출자 만들기 ⭐⭐⭐**

URL을 프로토콜, 호스트, 경로로 분해하는 추출자를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
object URL {
  def unapply(url: String): Option[(String, String, String)] = {
    val pattern = """(\w+)://([^/]+)(.*)""".r
    url match {
      case pattern(protocol, host, path) =>
        Some((protocol, host, if (path.isEmpty) "/" else path))
      case _ => None
    }
  }
}

"https://example.com/path/to/page" match {
  case URL(protocol, host, path) =>
    println(s"프로토콜: $protocol, 호스트: $host, 경로: $path")
  case _ =>
    println("유효하지 않은 URL")
}
// 프로토콜: https, 호스트: example.com, 경로: /path/to/page
```

</details>

#### 관련 개념

패턴 매칭은 다음 개념들과 밀접하게 연결됩니다:

| 관련 개념 | 연결 관계 |
|----------|----------|
| [케이스 클래스]({{< relref "/docs/scala/concepts/case-classes" >}}) | 패턴 매칭의 핵심 대상, `unapply` 자동 생성 |
| [대수적 데이터 타입]({{< relref "/docs/scala/concepts/case-classes#sealed-트레이트와-adt" >}}) | `sealed trait`로 완전성 검사 가능한 타입 정의 |
| [for 표현식]({{< relref "/docs/scala/concepts/for-comprehensions" >}}) | 제너레이터에서 패턴으로 값 분해 및 필터링 |
| [고차 함수]({{< relref "/docs/scala/concepts/higher-order-functions" >}}) | `collect`, `partition` 등에서 부분 함수 활용 |
| [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) | Option, Either 등 대수적 타입과 패턴 매칭 |
| [고급 타입]({{< relref "/docs/scala/concepts/type-system-advanced" >}}) | Match Types로 타입 레벨 패턴 매칭 |

#### 다음 단계

| 학습 경로 | 설명 |
|----------|------|
| [컬렉션]({{< relref "/docs/scala/concepts/collections" >}}) | 패턴 매칭과 함께 사용되는 풍부한 컬렉션 API |
| [고차 함수]({{< relref "/docs/scala/concepts/higher-order-functions" >}}) | `map`, `filter`, `collect`로 함수형 데이터 처리 |
| [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) | Option, Either, Try를 활용한 안전한 프로그래밍 |
