// Scala 3 기본 예제
// 들여쓰기 기반 문법을 사용합니다

@main def run(): Unit =
  println("=" * 50)
  println("Scala 3 기본 예제")
  println("=" * 50)

  // 1. 변수와 타입
  println("\n[1] 변수와 타입")
  variablesExample()

  // 2. 제어 구조
  println("\n[2] 제어 구조")
  controlStructuresExample()

  // 3. 함수
  println("\n[3] 함수")
  functionsExample()

  // 4. 케이스 클래스와 패턴 매칭
  println("\n[4] 케이스 클래스와 패턴 매칭")
  caseClassExample()

  // 5. 컬렉션
  println("\n[5] 컬렉션")
  collectionsExample()

  // 6. Scala 3 새 기능
  println("\n[6] Scala 3 새 기능")
  scala3FeaturesExample()

  println("\n" + "=" * 50)
  println("예제 완료!")


// 1. 변수와 타입
def variablesExample(): Unit =
  val name = "Scala"        // 불변 (타입 추론)
  val year: Int = 2024      // 명시적 타입
  var count = 0             // 가변
  count += 1

  println(s"  name: $name (타입 추론)")
  println(s"  year: $year (명시적 타입)")
  println(s"  count: $count (가변)")


// 2. 제어 구조
def controlStructuresExample(): Unit =
  val x = 10

  // if 표현식
  val result = if x > 5 then "크다" else "작다"
  println(s"  if 표현식: $result")

  // match 표현식
  val day = 3
  val dayName = day match
    case 1 => "월요일"
    case 2 => "화요일"
    case 3 => "수요일"
    case _ => "기타"
  println(s"  match 표현식: $dayName")

  // for 표현식
  val squares = for i <- 1 to 5 yield i * i
  println(s"  for-yield: $squares")


// 3. 함수
def functionsExample(): Unit =
  // 기본 함수
  def greet(name: String): String = s"Hello, $name!"

  // 기본값 매개변수
  def greetWithDefault(name: String = "World"): String = s"Hello, $name!"

  // 람다
  val double = (x: Int) => x * 2

  // 고차 함수
  def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

  println(s"  greet: ${greet("Scala")}")
  println(s"  기본값: ${greetWithDefault()}")
  println(s"  람다: ${double(5)}")
  println(s"  고차 함수: ${applyTwice(double, 3)}")


// 4. 케이스 클래스와 패턴 매칭
case class Person(name: String, age: Int)

def caseClassExample(): Unit =
  val alice = Person("Alice", 30)
  val bob = alice.copy(name = "Bob")  // copy 메서드

  println(s"  alice: $alice")
  println(s"  bob: $bob")

  // 패턴 매칭
  def describe(p: Person): String = p match
    case Person(name, age) if age < 20 => s"$name 은(는) 청소년"
    case Person(name, age) if age < 65 => s"$name 은(는) 성인"
    case Person(name, _)               => s"$name 은(는) 시니어"

  println(s"  패턴 매칭: ${describe(alice)}")


// 5. 컬렉션
def collectionsExample(): Unit =
  val numbers = List(1, 2, 3, 4, 5)

  val doubled = numbers.map(_ * 2)
  val evens = numbers.filter(_ % 2 == 0)
  val sum = numbers.reduce(_ + _)

  println(s"  원본: $numbers")
  println(s"  map(_ * 2): $doubled")
  println(s"  filter(짝수): $evens")
  println(s"  reduce(합): $sum")

  // Map
  val scores = Map("Alice" -> 90, "Bob" -> 85)
  println(s"  Map: $scores")
  println(s"  Alice 점수: ${scores.get("Alice")}")


// 6. Scala 3 새 기능
enum Color:
  case Red, Green, Blue

enum HttpStatus(val code: Int):
  case OK extends HttpStatus(200)
  case NotFound extends HttpStatus(404)
  case ServerError extends HttpStatus(500)

// Extension Method
extension (s: String)
  def exclaim: String = s + "!"
  def repeatTimes(n: Int): String = s * n

def scala3FeaturesExample(): Unit =
  // enum
  val color = Color.Red
  println(s"  enum: $color")

  val status = HttpStatus.NotFound
  println(s"  enum with value: ${status.code}")

  // Extension Method
  println(s"  extension exclaim: ${"Hello".exclaim}")
  println(s"  extension repeatTimes: ${"Ha".repeatTimes(3)}")

  // Union Type
  def process(input: Int | String): String = input match
    case i: Int    => s"숫자: $i"
    case s: String => s"문자열: $s"

  println(s"  union type (Int): ${process(42)}")
  println(s"  union type (String): ${process("hello")}")
