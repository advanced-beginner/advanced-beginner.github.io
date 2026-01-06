// Scala 2 기본 예제
// 중괄호 기반 문법을 사용합니다

object Main {
  def main(args: Array[String]): Unit = {
    println("=" * 50)
    println("Scala 2 기본 예제")
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

    // 6. Implicit
    println("\n[6] Implicit")
    implicitExample()

    println("\n" + "=" * 50)
    println("예제 완료!")
  }

  // 1. 변수와 타입
  def variablesExample(): Unit = {
    val name = "Scala"        // 불변 (타입 추론)
    val year: Int = 2024      // 명시적 타입
    var count = 0             // 가변
    count += 1

    println(s"  name: $name (타입 추론)")
    println(s"  year: $year (명시적 타입)")
    println(s"  count: $count (가변)")
  }

  // 2. 제어 구조
  def controlStructuresExample(): Unit = {
    val x = 10

    // if 표현식
    val result = if (x > 5) "크다" else "작다"
    println(s"  if 표현식: $result")

    // match 표현식
    val day = 3
    val dayName = day match {
      case 1 => "월요일"
      case 2 => "화요일"
      case 3 => "수요일"
      case _ => "기타"
    }
    println(s"  match 표현식: $dayName")

    // for 표현식
    val squares = for (i <- 1 to 5) yield i * i
    println(s"  for-yield: $squares")
  }

  // 3. 함수
  def functionsExample(): Unit = {
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
  }

  // 4. 케이스 클래스와 패턴 매칭
  case class Person(name: String, age: Int)

  def caseClassExample(): Unit = {
    val alice = Person("Alice", 30)
    val bob = alice.copy(name = "Bob")  // copy 메서드

    println(s"  alice: $alice")
    println(s"  bob: $bob")

    // 패턴 매칭
    def describe(p: Person): String = p match {
      case Person(name, age) if age < 20 => s"$name 은(는) 청소년"
      case Person(name, age) if age < 65 => s"$name 은(는) 성인"
      case Person(name, _)               => s"$name 은(는) 시니어"
    }

    println(s"  패턴 매칭: ${describe(alice)}")
  }

  // 5. 컬렉션
  def collectionsExample(): Unit = {
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
  }

  // 6. Implicit
  def implicitExample(): Unit = {
    // Implicit 값
    implicit val defaultName: String = "Guest"

    def greetImplicit(implicit name: String): String = s"Hello, $name!"

    println(s"  implicit 값: ${greetImplicit}")

    // Implicit 클래스 (확장 메서드)
    implicit class StringOps(s: String) {
      def exclaim: String = s + "!"
      def repeatTimes(n: Int): String = s * n
    }

    println(s"  implicit class exclaim: ${"Hello".exclaim}")
    println(s"  implicit class repeatTimes: ${"Ha".repeatTimes(3)}")

    // Implicit 변환
    implicit def intToString(i: Int): String = i.toString

    val strFromInt: String = 42
    println(s"  implicit 변환: $strFromInt (Int -> String)")
  }
}

// Scala 2 스타일 ADT (열거형 대안)
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}

sealed trait HttpStatus {
  def code: Int
}
object HttpStatus {
  case object OK extends HttpStatus { val code = 200 }
  case object NotFound extends HttpStatus { val code = 404 }
  case object ServerError extends HttpStatus { val code = 500 }
}
