---
title: 기본 예제
weight: 2
---

Scala의 핵심 개념을 활용한 종합 예제입니다.

> 💻 **온라인 실행:** 아래 모든 예제는 [Scastie](https://scastie.scala-lang.org/)에서 복사하여 바로 실행할 수 있습니다.
> Scala 3를 선택하고 코드를 붙여넣으세요!

## 예제 1: 데이터 모델링

케이스 클래스로 도메인 모델을 정의합니다.

```scala
// 도메인 모델
case class Product(id: Int, name: String, price: Double)
case class OrderLine(product: Product, quantity: Int)
case class Order(id: Int, customer: String, lines: List[OrderLine])

// 팩토리 메서드
object Order:
  def create(id: Int, customer: String, lines: List[OrderLine]): Option[Order] =
    if lines.isEmpty then None
    else Some(Order(id, customer, lines))

// 사용
val laptop = Product(1, "노트북", 1500000)
val mouse = Product(2, "마우스", 50000)

val order = Order.create(
  1,
  "김철수",
  List(
    OrderLine(laptop, 1),
    OrderLine(mouse, 2)
  )
)
```

## 예제 2: 주문 처리

패턴 매칭과 고차 함수로 비즈니스 로직을 구현합니다.

```scala
// 주문 확장 메서드
extension (order: Order)
  def totalPrice: Double =
    order.lines.map(line => line.product.price * line.quantity).sum

  def itemCount: Int =
    order.lines.map(_.quantity).sum

  def hasProduct(productId: Int): Boolean =
    order.lines.exists(_.product.id == productId)

  def applyDiscount(rate: Double): Order =
    order.copy(
      lines = order.lines.map { line =>
        line.copy(
          product = line.product.copy(
            price = line.product.price * (1 - rate)
          )
        )
      }
    )

// 사용
order.foreach { o =>
  println(s"총 금액: ${o.totalPrice}원")        // 1,600,000원
  println(s"상품 수: ${o.itemCount}개")         // 3개
  println(s"노트북 포함: ${o.hasProduct(1)}")   // true

  val discounted = o.applyDiscount(0.1)
  println(s"할인 후: ${discounted.totalPrice}원")  // 1,440,000원
}
```

## 예제 3: 에러 처리

Either를 사용한 안전한 에러 처리입니다.

```scala
// 에러 타입
enum ValidationError:
  case EmptyName
  case InvalidPrice(price: Double)
  case InvalidQuantity(qty: Int)

import ValidationError.*

// 검증 함수
def validateProduct(name: String, price: Double): Either[ValidationError, Product] =
  if name.isEmpty then Left(EmptyName)
  else if price <= 0 then Left(InvalidPrice(price))
  else Right(Product(0, name, price))

def validateOrderLine(
  product: Product,
  quantity: Int
): Either[ValidationError, OrderLine] =
  if quantity <= 0 then Left(InvalidQuantity(quantity))
  else Right(OrderLine(product, quantity))

// 조합
def createOrderLine(
  name: String,
  price: Double,
  quantity: Int
): Either[ValidationError, OrderLine] =
  for
    product <- validateProduct(name, price)
    line <- validateOrderLine(product, quantity)
  yield line

// 사용
createOrderLine("노트북", 1500000, 1) match
  case Right(line) => println(s"주문 라인: $line")
  case Left(EmptyName) => println("상품명이 비어있습니다")
  case Left(InvalidPrice(p)) => println(s"잘못된 가격: $p")
  case Left(InvalidQuantity(q)) => println(s"잘못된 수량: $q")
```

## 예제 4: 컬렉션 처리

함수형 스타일로 데이터를 처리합니다.

```scala
// 샘플 데이터
val products = List(
  Product(1, "노트북", 1500000),
  Product(2, "마우스", 50000),
  Product(3, "키보드", 150000),
  Product(4, "모니터", 500000),
  Product(5, "스피커", 200000)
)

// 필터링
val expensive = products.filter(_.price >= 200000)
println(s"고가 상품: ${expensive.map(_.name)}")
// List(노트북, 모니터)

// 변환
val priceList = products.map(p => s"${p.name}: ${p.price}원")
println(priceList.mkString("\n"))

// 그룹화
val byPriceRange = products.groupBy { p =>
  if p.price < 100000 then "저가"
  else if p.price < 500000 then "중가"
  else "고가"
}
println(s"가격대별: $byPriceRange")

// 집계
val totalValue = products.map(_.price).sum
val avgPrice = products.map(_.price).sum / products.length
val maxPrice = products.maxBy(_.price)

println(s"총 가치: ${totalValue}원")
println(s"평균 가격: ${avgPrice}원")
println(s"최고가 상품: ${maxPrice.name}")

// 정렬
val sortedByPrice = products.sortBy(_.price)
val sortedByName = products.sortBy(_.name)
```

## 예제 5: Option 활용

null 대신 Option을 사용합니다.

```scala
// 저장소
object ProductRepository:
  private val products = Map(
    1 -> Product(1, "노트북", 1500000),
    2 -> Product(2, "마우스", 50000)
  )

  def findById(id: Int): Option[Product] = products.get(id)

  def findByName(name: String): Option[Product] =
    products.values.find(_.name.contains(name))

// 사용
ProductRepository.findById(1) match
  case Some(product) => println(s"찾음: $product")
  case None => println("상품 없음")

// 체이닝
val price = ProductRepository
  .findById(1)
  .map(_.price)
  .getOrElse(0.0)

// for comprehension
val orderTotal = for
  laptop <- ProductRepository.findById(1)
  mouse <- ProductRepository.findById(2)
yield laptop.price + mouse.price

println(s"주문 합계: ${orderTotal.getOrElse(0.0)}원")
```

## 예제 6: 타입 클래스

타입 클래스로 확장 가능한 기능을 구현합니다.

```scala
// JSON 인코더 타입 클래스
trait JsonEncoder[A]:
  def encode(a: A): String

object JsonEncoder:
  given JsonEncoder[String] with
    def encode(s: String): String = s"\"$s\""

  given JsonEncoder[Int] with
    def encode(i: Int): String = i.toString

  given JsonEncoder[Double] with
    def encode(d: Double): String = d.toString

  given JsonEncoder[Product] with
    def encode(p: Product): String =
      s"""{"id":${p.id},"name":"${p.name}","price":${p.price}}"""

  given [A](using e: JsonEncoder[A]): JsonEncoder[List[A]] with
    def encode(list: List[A]): String =
      list.map(e.encode).mkString("[", ",", "]")

// 확장 메서드
extension [A](a: A)(using e: JsonEncoder[A])
  def toJson: String = e.encode(a)

// 사용
val laptop = Product(1, "노트북", 1500000)
println(laptop.toJson)
// {"id":1,"name":"노트북","price":1500000.0}

val products = List(
  Product(1, "노트북", 1500000),
  Product(2, "마우스", 50000)
)
println(products.toJson)
// [{"id":1,"name":"노트북","price":1500000.0},{"id":2,"name":"마우스","price":50000.0}]
```

## 예제 프로젝트 실행

```bash
cd examples/scala/scala3-basics
sbt run
```

## 예제 7: 실무 시나리오 - REST API 응답 처리

실제 API 응답을 처리하는 패턴입니다.

```scala
import scala.util.{Try, Success, Failure}

// API 응답 모델
case class ApiResponse[T](
  status: Int,
  data: Option[T],
  error: Option[String]
)

// 사용자 도메인
case class User(id: Long, name: String, email: String)

// API 클라이언트 시뮬레이션
object UserApiClient:
  def fetchUser(id: Long): ApiResponse[User] =
    if id > 0 then
      ApiResponse(200, Some(User(id, s"User$id", s"user$id@example.com")), None)
    else
      ApiResponse(404, None, Some("User not found"))

  def fetchUsers(ids: List[Long]): List[ApiResponse[User]] =
    ids.map(fetchUser)

// 응답 처리 유틸리티
object ApiResponseHandler:
  extension [T](response: ApiResponse[T])
    def toEither: Either[String, T] =
      response match
        case ApiResponse(status, Some(data), _) if status < 400 => Right(data)
        case ApiResponse(_, _, Some(error)) => Left(error)
        case _ => Left("Unknown error")

    def toOption: Option[T] = response.data.filter(_ => response.status < 400)

// 사용 예시
import ApiResponseHandler.*

val response = UserApiClient.fetchUser(1)
val userOrError = response.toEither

userOrError match
  case Right(user) => println(s"환영합니다, ${user.name}!")
  case Left(error) => println(s"오류: $error")

// 여러 사용자 처리
val userIds = List(1L, 2L, -1L, 3L)
val results = UserApiClient.fetchUsers(userIds)
  .map(_.toEither)
  .collect { case Right(user) => user }

println(s"성공적으로 조회된 사용자: ${results.length}명")
```

## 예제 8: 실무 시나리오 - 설정 관리

환경별 설정을 타입 안전하게 관리하는 패턴입니다.

```scala
// 설정 ADT
enum Environment:
  case Development, Staging, Production

case class DatabaseConfig(
  host: String,
  port: Int,
  database: String,
  maxConnections: Int
)

case class AppConfig(
  environment: Environment,
  database: DatabaseConfig,
  debug: Boolean
)

object AppConfig:
  import Environment.*

  def load(env: Environment): AppConfig = env match
    case Development =>
      AppConfig(
        environment = Development,
        database = DatabaseConfig("localhost", 5432, "dev_db", 5),
        debug = true
      )
    case Staging =>
      AppConfig(
        environment = Staging,
        database = DatabaseConfig("staging.db.internal", 5432, "staging_db", 20),
        debug = true
      )
    case Production =>
      AppConfig(
        environment = Production,
        database = DatabaseConfig("prod.db.internal", 5432, "prod_db", 100),
        debug = false
      )

  def fromString(envStr: String): Either[String, AppConfig] =
    envStr.toLowerCase match
      case "dev" | "development" => Right(load(Development))
      case "staging" => Right(load(Staging))
      case "prod" | "production" => Right(load(Production))
      case _ => Left(s"Unknown environment: $envStr")

// 사용 예시
val config = AppConfig.fromString("production")

config match
  case Right(cfg) =>
    println(s"환경: ${cfg.environment}")
    println(s"DB 호스트: ${cfg.database.host}")
    println(s"디버그 모드: ${cfg.debug}")
  case Left(error) =>
    println(s"설정 로드 실패: $error")
```

## 연습 과제

1. **재고 관리 추가** ⭐: `Product`에 `stock` 필드를 추가하고, 재고 확인 로직을 구현하세요.

2. **주문 상태** ⭐⭐: `Order`에 상태(PENDING, CONFIRMED, SHIPPED)를 추가하세요.

3. **검색 기능** ⭐⭐: 가격 범위와 이름으로 상품을 검색하는 함수를 구현하세요.

> 💡 연습 과제 해답은 [Scastie](https://scastie.scala-lang.org/)에서 직접 구현하고 테스트해보세요!

## 다음 단계

- [Scala 2 vs 3 비교](../scala2-vs-scala3/) — 버전별 코드 비교
