---
lastmod: "2026-01-15"
title: 동시성
description: "Future, Actor 등 Scala 동시성 모델의 작동 원리를 설명합니다."
weight: 16
---

## 전체 비유: 레스토랑 주방

Scala 동시성을 <strong>레스토랑 주방</strong>에 비유하면 이해하기 쉽습니다:

| 레스토랑 비유 | Scala 개념 | 역할 |
|-------------|-----------|------|
| 주문서 | Future | 비동기 계산의 결과를 나타내는 약속 |
| 주방장들 | ExecutionContext | 작업을 실행하는 스레드 풀 |
| 요리 단계 연결 | flatMap/map | 순차적 비동기 작업 연결 |
| 여러 요리 동시 조리 | 병렬 Future | 독립적 작업의 동시 실행 |
| 요리 완료 알림벨 | Promise | 외부에서 결과를 설정 |
| 첫 번째 완성 요리 | firstCompletedOf | 가장 먼저 완료되는 작업 |
| 모든 요리 완성 대기 | sequence | 모든 Future 완료 대기 |

레스토랑에서 여러 주방장이 동시에 요리하듯이, Future를 사용하면 여러 작업을 동시에 처리할 수 있습니다. 중요한 것은 <strong>작업 간의 의존성을 파악</strong>하여 순차/병렬 실행을 올바르게 선택하는 것입니다.

{{< callout type="info" title="TL;DR" >}}
- **Future**: 비동기 계산 결과를 나타내는 타입
- **ExecutionContext**: 스레드 풀 관리, 암시적 파라미터로 전달
- **map/flatMap**: Future 조합, for comprehension 사용 가능
- **Promise**: Future를 직접 완료할 수 있는 쓰기 전용 타입
- 고급 라이브러리: Cats Effect, ZIO, Akka
{{< /callout >}}

**소요 시간**: 약 25-30분

**대상 독자:** 비동기 프로그래밍 기초를 이해한 개발자
**선수 지식:** 고차 함수, for comprehension, 암시적 매개변수

Scala는 `Future`를 통해 비동기 프로그래밍을 지원합니다. 이 문서에서는 `Future`, `Promise`, `ExecutionContext`를 다룹니다. 비동기 프로그래밍을 통해 I/O 대기 시간 동안 다른 작업을 수행할 수 있어 애플리케이션의 처리량을 크게 향상시킬 수 있습니다.

#### 왜 비동기 프로그래밍이 필요한가?

외부 API 호출, DB 쿼리, 파일 I/O를 순차적으로 실행하면 각 작업이 끝날 때까지 스레드가 멈춰 있습니다. Future를 사용하면 I/O 대기 중에도 다른 작업을 수행할 수 있고, 독립적인 작업들을 병렬로 실행하여 전체 응답 시간을 단축할 수 있습니다.

#### Future 기초

`Future`는 아직 계산되지 않았거나 진행 중인 값을 나타냅니다. Future를 생성하면 즉시 백그라운드에서 계산이 시작되고, 결과가 준비되면 콜백이나 조합 메서드를 통해 처리할 수 있습니다.

**생성**

Future는 `Future { ... }` 블록으로 생성합니다. 블록 내의 코드는 별도의 스레드에서 비동기적으로 실행됩니다.

```scala
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

// 비동기 계산 시작
val future: Future[Int] = Future {
  Thread.sleep(1000)  // 시간이 오래 걸리는 작업
  42
}

// 즉시 반환, 계산은 백그라운드에서 진행
println("계산 시작됨")
```

**ExecutionContext**

`Future`는 스레드 풀을 관리하는 `ExecutionContext`가 필요합니다. 암시적 파라미터로 전달되며, 대부분의 경우 전역 ExecutionContext를 사용합니다.

```scala
import scala.concurrent.ExecutionContext.Implicits.global

// 또는 커스텀 ExecutionContext
import java.util.concurrent.Executors
implicit val ec: ExecutionContext =
  ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
```

{{< callout type="info" title="핵심 포인트" >}}
- Future는 `Future { ... }` 블록으로 생성
- ExecutionContext가 스레드 풀 관리
- 생성 즉시 백그라운드에서 계산 시작
{{< /callout >}}

#### Future 조합

Future의 진정한 힘은 조합(composition)에 있습니다. map, flatMap 등의 메서드를 통해 여러 비동기 연산을 연결할 수 있습니다.

**map**

`map`은 성공 결과를 변환합니다. Future가 실패한 경우에는 변환이 적용되지 않고 실패가 전파됩니다.

```scala
val future = Future(42)
val doubled = future.map(_ * 2)  // Future(84)
```

**flatMap**

`flatMap`은 Future를 반환하는 함수로 변환합니다. 순차적인 비동기 연산을 연결할 때 사용합니다.

```scala
def fetchUser(id: Int): Future[String] = Future(s"User$id")
def fetchOrders(user: String): Future[List[String]] =
  Future(List(s"Order1-$user", s"Order2-$user"))

val orders: Future[List[String]] = fetchUser(1).flatMap(fetchOrders)
```

**for comprehension**

flatMap 체인을 더 읽기 쉽게 작성할 수 있습니다. 각 `<-`는 flatMap 호출로 변환됩니다.

```scala
val result = for {
  user <- fetchUser(1)
  orders <- fetchOrders(user)
} yield (user, orders)

// Future(("User1", List("Order1-User1", "Order2-User1")))
```

**병렬 실행**

for comprehension 내에서 Future를 생성하면 순차 실행됩니다. 병렬 실행을 원한다면 Future를 먼저 생성한 후 for comprehension에서 결과를 조합해야 합니다.

```scala
// 순차 실행 (for comprehension)
val sequential = for {
  a <- Future(slowComputation1())
  b <- Future(slowComputation2())
} yield a + b

// 병렬 실행
val futureA = Future(slowComputation1())
val futureB = Future(slowComputation2())

val parallel = for {
  a <- futureA
  b <- futureB
} yield a + b
```

{{< callout type="info" title="핵심 포인트" >}}
- `map`: 성공 결과 변환
- `flatMap`: Future 반환 함수로 변환 (순차 연결)
- for comprehension 내 Future 생성 = 순차 실행
- 미리 Future 생성 후 조합 = 병렬 실행
{{< /callout >}}

#### 에러 처리

Future는 성공 또는 실패 상태를 가집니다. 실패한 Future를 복구하거나 다른 Future로 대체하는 여러 방법이 있습니다.

**recover**

`recover`는 실패를 기본값으로 대체합니다. 부분 함수로 처리할 예외 타입을 선택적으로 지정할 수 있습니다.

```scala
val future = Future {
  throw new RuntimeException("에러!")
}

val recovered = future.recover {
  case _: RuntimeException => 0
}
// Future(0)
```

**recoverWith**

`recoverWith`는 실패를 다른 Future로 대체합니다. 복구 로직 자체가 비동기일 때 유용합니다.

```scala
val fallback = future.recoverWith {
  case _: RuntimeException => Future(0)
}
```

**failed**

`failed`는 실패한 Future의 예외를 추출합니다. 성공한 Future에 대해서는 `NoSuchElementException`이 발생합니다.

```scala
val failure = Future.failed(new Exception("에러"))
val exception: Future[Throwable] = failure.failed
```

{{< callout type="info" title="핵심 포인트" >}}
- `recover`: 실패를 기본값으로 대체
- `recoverWith`: 실패를 다른 Future로 대체
- `failed`: 실패한 Future의 예외 추출
{{< /callout >}}

#### 결과 대기

비동기 결과를 동기적으로 기다려야 하는 경우가 있습니다. 테스트나 애플리케이션 종료 시점에서 주로 사용됩니다.

**Await (테스트용)**

`Await.result`는 Future가 완료될 때까지 블로킹합니다. 테스트 코드에서만 사용하고 프로덕션에서는 피해야 합니다.

```scala
import scala.concurrent.Await
import scala.concurrent.duration._

val future = Future(42)
val result = Await.result(future, 5.seconds)  // 42
```

> **주의:** 프로덕션 코드에서는 `Await` 사용을 피하세요!

**콜백**

`onComplete`는 Future가 완료되면 콜백을 실행합니다. 성공과 실패 모두 처리할 수 있습니다.

```scala
import scala.util.{Success, Failure}

future.onComplete {
  case Success(value) => println(s"성공: $value")
  case Failure(e)     => println(s"실패: ${e.getMessage}")
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `Await.result`: 블로킹 대기 (테스트용)
- `onComplete`: 완료 시 콜백 실행
- 프로덕션에서는 Await 사용 피하기
{{< /callout >}}

#### Promise

`Promise`는 Future를 직접 완료할 수 있게 해줍니다. Future가 읽기 전용인 반면, Promise는 쓰기 전용입니다. Promise를 통해 외부에서 Future의 결과를 설정할 수 있습니다.

```scala
import scala.concurrent.Promise

val promise = Promise[Int]()
val future: Future[Int] = promise.future

// 다른 스레드에서 완료
Future {
  Thread.sleep(1000)
  promise.success(42)  // 또는 promise.failure(exception)
}

// future가 완료됨
future.foreach(println)  // 42
```

**사용 사례**

Promise는 타임아웃 구현이나 콜백 기반 API를 Future로 래핑할 때 유용합니다.

```scala
def timeout[T](future: Future[T], duration: FiniteDuration): Future[T] = {
  val promise = Promise[T]()

  // 타임아웃 설정
  Future {
    Thread.sleep(duration.toMillis)
    promise.tryFailure(new TimeoutException())
  }

  // 원본 Future 연결
  future.onComplete(result => promise.tryComplete(result))

  promise.future
}
```

{{< callout type="info" title="핵심 포인트" >}}
- Promise는 Future를 직접 완료할 수 있는 쓰기 전용 타입
- `success`/`failure`로 결과 설정
- 타임아웃, 콜백 기반 API 래핑에 유용
{{< /callout >}}

#### 유틸리티 메서드

Future 컴패니언 객체는 여러 Future를 조합하는 유틸리티 메서드를 제공합니다.

**Future.sequence**

`Future.sequence`는 `List[Future[A]]`를 `Future[List[A]]`로 변환합니다. 모든 Future가 성공해야 결과가 성공합니다.

```scala
val futures: List[Future[Int]] = List(Future(1), Future(2), Future(3))
val combined: Future[List[Int]] = Future.sequence(futures)
// Future(List(1, 2, 3))
```

**Future.traverse**

`Future.traverse`는 리스트의 각 요소에 비동기 함수를 적용합니다. `map` + `sequence`의 조합과 동일합니다.

```scala
val ids = List(1, 2, 3)
val users: Future[List[String]] = Future.traverse(ids)(fetchUser)
// Future(List("User1", "User2", "User3"))
```

**Future.firstCompletedOf**

`Future.firstCompletedOf`는 가장 먼저 완료되는 Future를 반환합니다. 레이싱(racing) 패턴에 유용합니다.

```scala
val futures = List(
  Future { Thread.sleep(100); "fast" },
  Future { Thread.sleep(1000); "slow" }
)

val first = Future.firstCompletedOf(futures)
// Future("fast")
```

{{< callout type="info" title="핵심 포인트" >}}
- `Future.sequence`: `List[Future[A]]` -> `Future[List[A]]`
- `Future.traverse`: 리스트 요소에 비동기 함수 적용
- `Future.firstCompletedOf`: 가장 먼저 완료되는 Future 반환
{{< /callout >}}

#### 고급 라이브러리

Scala 생태계에서는 `Future`보다 더 강력한 비동기 라이브러리를 많이 사용합니다. 이들은 참조 투명성, 리소스 관리, 에러 처리 등에서 Future보다 우수한 기능을 제공합니다.

**Cats Effect**

Cats Effect는 순수 함수형 비동기 프로그래밍을 지원합니다 (의존성: `"org.typelevel" %% "cats-effect" % "3.5.7"`). IO 타입은 지연 평가되어 참조 투명성을 보장합니다.

```scala
import cats.effect.{IO, IOApp}
import scala.concurrent.duration.*

object MyApp extends IOApp.Simple:
  val program: IO[Unit] = for
    _ <- IO.println("Hello")
    _ <- IO.sleep(1.second)
    _ <- IO.println("World")
  yield ()

  def run: IO[Unit] = program
```

> 💡 `unsafeRunSync()`보다는 `IOApp`을 사용하는 것이 권장됩니다.

**ZIO**

ZIO는 효과 시스템과 의존성 주입을 통합합니다 (의존성: `"dev.zio" %% "zio" % "2.1.14"`). 에러 타입이 명시되어 타입 안전한 에러 처리가 가능합니다.

```scala
import zio.*

object MyApp extends ZIOAppDefault:
  val program: ZIO[Any, java.io.IOException, Unit] = for
    _ <- Console.printLine("Hello")
    _ <- ZIO.sleep(1.second)
    _ <- Console.printLine("World")
  yield ()

  def run = program
```

**Akka (Classic & Typed)**

Akka는 액터 기반 동시성 모델을 제공합니다. 메시지 전달을 통해 상태를 공유하지 않고 동시성을 달성합니다.

```scala
import akka.actor.typed.*
import akka.actor.typed.scaladsl.*

object HelloWorld {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String)

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom)
    Behaviors.same
  }
}
```

#### 모범 사례

비동기 프로그래밍에서 흔한 실수를 피하기 위한 지침입니다.

**DO**

다음 관행을 따르면 안정적인 비동기 코드를 작성할 수 있습니다.

- 비동기 작업에는 Future 사용
- ExecutionContext를 명시적으로 관리
- 에러 처리 항상 포함
- 병렬 실행이 가능한 경우 활용

**DON'T**

다음은 반드시 피해야 할 안티패턴입니다.

- `Await.result` 프로덕션에서 사용 금지
- 무한 블로킹 피하기
- Future 내에서 예외 삼키지 않기

#### 흔한 실수와 Anti-patterns

Future를 사용할 때 자주 발생하는 실수와 올바른 대안을 알아봅니다.

**❌ 피해야 할 것**

아래 코드들은 성능 문제나 버그를 유발할 수 있습니다.

```scala
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

// 1. Await.result 프로덕션 사용
val result = Await.result(future, 5.seconds)  // 블로킹! 스레드 낭비

// 2. Future 내에서 블로킹 작업
Future {
  Thread.sleep(10000)  // 스레드 풀 고갈 위험
  Await.result(anotherFuture, 5.seconds)  // 데드락 가능!
}

// 3. 순차 실행 의도치 않게 발생
for {
  a <- Future(compute1())  // 먼저 실행
  b <- Future(compute2())  // a 완료 후 실행 (순차!)
} yield a + b

// 4. 예외 무시
future.foreach(println)  // 실패 시 아무 일도 안 일어남!
```

**✅ 올바른 방법**

위의 문제들을 해결하는 올바른 패턴입니다.

```scala
// 1. 콜백이나 조합 사용
future.map(process).recover {
  case e: Exception => defaultValue
}

// 2. 블로킹 작업은 별도 ExecutionContext
val blockingEc = ExecutionContext.fromExecutor(
  Executors.newCachedThreadPool()
)
Future {
  blocking {  // 블로킹 표시
    Thread.sleep(10000)
  }
}(blockingEc)

// 3. 병렬 실행 명시
val futureA = Future(compute1())  // 즉시 시작
val futureB = Future(compute2())  // 즉시 시작
for {
  a <- futureA
  b <- futureB
} yield a + b

// 4. 에러 처리 포함
future.onComplete {
  case Success(v) => println(s"성공: $v")
  case Failure(e) => println(s"실패: ${e.getMessage}")
}
```

**Future vs IO/ZIO 비교**

Future와 함수형 효과 라이브러리의 주요 차이점을 비교합니다. Future는 즉시 실행되어 참조 투명성을 보장하지 않지만, IO/ZIO는 지연 실행되어 참조 투명성을 유지합니다.

```mermaid
flowchart LR
    subgraph Future
        F1["즉시 실행"]
        F2["참조 투명 X"]
        F3["에러 처리 복잡"]
    end

    subgraph "IO/ZIO"
        Z1["지연 실행"]
        Z2["참조 투명 O"]
        Z3["타입 안전 에러"]
    end

    Future --> |"간단한 비동기"| Use1["웹 API 호출"]
    IO/ZIO --> |"복잡한 비동기"| Use2["비즈니스 로직"]
```

*위 다이어그램은 Future와 IO/ZIO의 특성 및 적합한 사용 사례를 비교합니다.*

#### 연습 문제

다음 연습 문제를 통해 Future 사용법을 복습해보세요.

**1. 병렬 API 호출**

세 개의 API를 병렬로 호출하고 결과를 합치세요.

<details>
<summary>정답 보기</summary>

```scala
def fetchA(): Future[Int] = Future { Thread.sleep(100); 1 }
def fetchB(): Future[Int] = Future { Thread.sleep(100); 2 }
def fetchC(): Future[Int] = Future { Thread.sleep(100); 3 }

// 병렬 실행
val aF = fetchA()
val bF = fetchB()
val cF = fetchC()

val result = for {
  a <- aF
  b <- bF
  c <- cF
} yield a + b + c

// 또는
val result2 = Future.sequence(List(fetchA(), fetchB(), fetchC()))
  .map(_.sum)
```

</details>

#### 관련 개념

동시성은 다음 개념들과 밀접하게 연결됩니다:

| 관련 개념 | 연결 관계 |
|----------|----------|
| [for 표현식](for-comprehensions/) | Future의 `flatMap` 체인을 읽기 쉽게 작성 |
| [고차 함수](higher-order-functions/) | `map`, `flatMap`, `recover` 등 조합 메서드 |
| [함수형 패턴](functional-patterns/) | IO, ZIO 등 참조 투명한 효과 시스템 |
| [Implicits](implicits/) | `ExecutionContext`의 암시적 전달 |
| [타입 클래스](type-classes/) | Cats Effect의 `Async`, `Concurrent` 타입 클래스 |

#### 다음 단계

| 학습 경로 | 설명 |
|----------|------|
| [함수형 패턴](functional-patterns/) | Functor, Monad 등 추상화 심화 학습 |
| [Implicits](implicits/) | ExecutionContext 전달 메커니즘 이해 |
| [Akka 공식 문서](https://akka.io/) | 액터 기반 동시성 모델 |
| [ZIO 공식 문서](https://zio.dev/) | 타입 안전한 효과 시스템 |
