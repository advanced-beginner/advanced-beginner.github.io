---
lastmod: "2026-01-16"
title: Future 에러 처리
description: "Future 에러를 진단하고 안전하게 처리하는 방법을 안내합니다."
weight: 2
---

비동기 코드에서 예외를 안전하게 처리하고 디버깅하는 방법을 안내합니다.

**소요 시간**: 약 15-20분

{{< callout type="tip" title="TL;DR" >}}
- `recover`/`recoverWith`: 특정 예외를 처리하고 대체값 반환
- `transform`: 성공/실패 모두 변환
- `onComplete`: 부수 효과 (로깅 등)에만 사용
- **절대 하지 말 것**: `Await.result`로 예외를 동기적으로 던지기
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- Future에서 발생한 예외가 조용히 사라지는 경우
- 비동기 코드의 에러를 어떻게 처리해야 할지 모를 때
- `recover`, `recoverWith`, `transform` 중 어떤 것을 사용해야 할지 결정이 필요할 때

### 증상

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val future = Future {
  throw new RuntimeException("Something went wrong")
}
// 프로그램은 정상 종료되지만, 예외는 어디로 갔을까?
```

### 이 가이드가 다루지 않는 것

- **Future의 기본 개념**: [동시성 개념 문서](../concepts/concurrency/)를 참조하세요
- **Cats Effect IO / ZIO의 에러 처리**: 해당 라이브러리 문서를 참조하세요
- **액터 시스템(Akka)의 에러 처리**: Akka 문서를 참조하세요

---

## 시작하기 전에

다음 환경이 준비되어 있는지 확인하세요:

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| Scala 버전 | 2.13.x 또는 3.x | `scala -version` |
| 빌드 도구 | sbt 1.x 또는 Gradle 8.x | `sbt --version` |
| 의존성 | scala-library (기본 포함) | - |

### 필요한 import

모든 예제에서 다음 import가 필요합니다:

```scala
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
```

---

## 1단계: Future 실패 기본 이해

### 1.1 Future의 두 가지 상태

```scala
val successFuture: Future[Int] = Future.successful(42)
val failedFuture: Future[Int] = Future.failed(new Exception("Error"))

successFuture.value  // Some(Success(42))
failedFuture.value   // Some(Failure(java.lang.Exception: Error))
```

### 1.2 예외가 사라지는 이유

Future는 비동기로 실행되므로, 메인 스레드가 먼저 종료되면 예외를 확인할 기회가 없습니다:

```scala
val future = Future {
  Thread.sleep(100)
  throw new RuntimeException("Error")
}

// 메인 스레드가 바로 종료되면 예외가 출력되지 않음
println("Main thread finished")
```

**해결**: 적절한 에러 핸들러를 등록하거나, 테스트에서는 `Await`을 사용하세요.

---

## 2단계: 에러 처리 패턴 선택

다음 결정 가이드를 참고하세요:

| 상황 | 사용할 메서드 |
|------|-------------|
| 예외를 기본값으로 바꾸고 싶다 | `recover` |
| 예외 시 다른 Future를 실행하고 싶다 | `recoverWith` |
| 성공/실패 모두 다른 타입으로 변환하고 싶다 | `transform` |
| 로깅만 하고 결과는 그대로 전달하고 싶다 | `andThen` |

### 2.1 recover - 예외를 값으로 변환

특정 예외를 처리하고 기본값을 반환합니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.IOException

// 예외 정의
class UserNotFoundException(msg: String) extends Exception(msg)
class DatabaseException(msg: String) extends Exception(msg)

// 사용 예시
def fetchUserAge(userId: String): Future[Int] = {
  // 실제로는 DB 조회
  Future {
    if (userId == "unknown") throw new UserNotFoundException(s"User $userId not found")
    30 // 정상 반환
  }.recover {
    case _: UserNotFoundException => 0  // 기본값
    case e: DatabaseException =>
      println(s"DB error for user $userId: ${e.getMessage}")
      -1  // 에러 표시값
  }
}

// 테스트
fetchUserAge("alice").foreach(age => println(s"Age: $age"))  // Age: 30
fetchUserAge("unknown").foreach(age => println(s"Age: $age")) // Age: 0
```

### 2.2 recoverWith - 예외를 다른 Future로 변환

실패 시 대체 Future를 실행합니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException

case class Data(value: String)

def fetchFromPrimary(): Future[Data] = Future {
  // 시뮬레이션: 타임아웃 발생
  throw new TimeoutException("Primary server timeout")
}

def fetchFromBackup(): Future[Data] = Future {
  Data("backup data")
}

val result: Future[Data] = fetchFromPrimary().recoverWith {
  case _: TimeoutException =>
    println("Primary timed out, trying backup...")
    fetchFromBackup()
}

result.foreach(d => println(s"Got: ${d.value}"))
// 출력:
// Primary timed out, trying backup...
// Got: backup data
```

### 2.3 transform - 성공/실패 모두 변환

성공과 실패를 모두 처리해야 할 때 사용합니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

def riskyOperation(): Future[Int] = Future {
  if (scala.util.Random.nextBoolean()) 42
  else throw new RuntimeException("Random failure")
}

val future: Future[String] = riskyOperation().transform {
  case Success(value) =>
    Success(s"Got: $value")
  case Failure(e) =>
    Success(s"Failed: ${e.getMessage}")  // 실패를 성공으로 변환
}

future.foreach(println)  // "Got: 42" 또는 "Failed: Random failure"
```

---

## 3단계: 로깅과 모니터링

### 3.1 onComplete - 부수 효과용

{{< callout type="warning" title="주의" >}}
`onComplete`의 반환 타입은 `Unit`입니다. 비즈니스 로직에 사용하지 마세요.
{{< /callout >}}

로깅이나 메트릭에만 사용하세요:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

def processOrder(orderId: String): Future[String] = Future {
  s"Order $orderId processed"
}

val future = processOrder("ORD-123")

future.onComplete {
  case Success(result) =>
    println(s"[INFO] Order processed: $result")
    // metrics.incrementCounter("orders.success")
  case Failure(e) =>
    println(s"[ERROR] Order failed: ${e.getMessage}")
    // metrics.incrementCounter("orders.failure")
}

// 결과를 반환하려면 별도의 map/recover 사용
```

### 3.2 andThen - 체이닝 가능한 부수 효과

`onComplete`과 비슷하지만 원래 Future를 그대로 반환합니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

def processOrder(orderId: String): Future[String] = Future {
  s"Order $orderId processed"
}

def transformResult(s: String): String = s.toUpperCase

val result = processOrder("ORD-123")
  .andThen {
    case Success(_) => println("[INFO] Order started")
  }
  .map(transformResult)
  .andThen {
    case Success(r) => println(s"[INFO] Order completed: $r")
    case Failure(e) => println(s"[ERROR] Order failed: ${e.getMessage}")
  }

result.foreach(println)
// 출력:
// [INFO] Order started
// [INFO] Order completed: ORDER ORD-123 PROCESSED
// ORDER ORD-123 PROCESSED
```

---

## 4단계: 흔한 실수와 해결

### 4.1 Await 남용

{{< callout type="error" title="위험" >}}
프로덕션 코드에서 `Await.result`는 스레드를 블로킹하여 성능을 심각하게 저하시킵니다.
테스트 코드에서만 사용하세요.
{{< /callout >}}

```scala
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// 잘못된 예: 블로킹으로 예외 처리
def badExample(): Unit = {
  val future = Future { throw new RuntimeException("Error") }
  try {
    val result = Await.result(future, 5.seconds)  // 블로킹!
  } catch {
    case e: Exception => println(s"Error: ${e.getMessage}")
  }
}

// 올바른 예: 비동기 처리
def goodExample(): Future[Int] = {
  Future { throw new RuntimeException("Error") }
    .recover {
      case e: Exception =>
        println(s"Error: ${e.getMessage}")
        -1  // 기본값 반환
    }
}
```

### 4.2 예외 삼키기

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

// 잘못된 예: 예외를 무시
val future = Future { throw new RuntimeException("Error") }
future.onComplete {
  case Failure(_) => // 아무것도 안 함 - 위험!
  case Success(v) => println(v)
}

// 올바른 예: 최소한 로깅
future.onComplete {
  case Failure(e) => println(s"[ERROR] Unexpected error: ${e.getMessage}")
  case Success(v) => println(v)
}
```

### 4.3 중첩 Future

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: String, name: String)
case class Order(id: String, userId: String)

def fetchUser(id: String): Future[User] = Future { User(id, "Alice") }
def fetchOrders(user: User): Future[List[Order]] = Future {
  List(Order("1", user.id))
}

// 잘못된 예: Future[Future[T]]
val nested: Future[Future[List[Order]]] = fetchUser("123").map(fetchOrders)

// 올바른 예: flatMap 사용
val flat: Future[List[Order]] = fetchUser("123").flatMap(fetchOrders)
```

---

## 5단계: 여러 Future 조합 시 에러 처리

### 5.1 for comprehension

하나라도 실패하면 전체가 실패합니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: String, name: String)
case class Order(id: String)
case class Payment(id: String)

def fetchUser(userId: String): Future[User] = Future { User(userId, "Alice") }
def fetchOrders(userId: String): Future[List[Order]] = Future { List(Order("1")) }
def fetchPayments(userId: String): Future[List[Payment]] = Future { List(Payment("p1")) }

val defaultUser = User("default", "Guest")

val result = for {
  user <- fetchUser("user-123")
  orders <- fetchOrders(user.id)
  payments <- fetchPayments(user.id)
} yield (user, orders, payments)

result.recover {
  case e: NoSuchElementException => (defaultUser, Nil, Nil)
  case e: Exception =>
    println(s"[ERROR] Failed to fetch data: ${e.getMessage}")
    throw e  // 다시 던지기
}
```

### 5.2 Future.sequence 에러 처리

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val futures: List[Future[Int]] = List(
  Future.successful(1),
  Future.failed(new Exception("Error")),
  Future.successful(3)
)

// 하나라도 실패하면 전체 실패
Future.sequence(futures).recover {
  case e =>
    println(s"Sequence failed: ${e.getMessage}")
    List.empty  // 에러 시 빈 리스트
}

// 개별 실패를 허용하려면
val recovered: List[Future[Int]] = futures.map(_.recover { case _ => -1 })
Future.sequence(recovered).foreach(println)  // List(1, -1, 3)
```

### 5.3 firstCompletedOf - 가장 빠른 성공 사용

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def fetchFromServer1(): Future[String] = Future {
  Thread.sleep(100)
  "Server 1 response"
}

def fetchFromServer2(): Future[String] = Future {
  Thread.sleep(50)
  "Server 2 response"
}

val fastest = Future.firstCompletedOf(List(
  fetchFromServer1(),
  fetchFromServer2()
))

fastest.foreach(println)  // "Server 2 response" (더 빠른 응답)
```

---

## 체크리스트

Future 에러 처리 시 확인사항:

- [ ] **모든 Future에 에러 핸들러가 있는가?** - `recover` 또는 `recoverWith`
- [ ] **예외가 로깅되는가?** - `onComplete` 또는 `andThen`
- [ ] **Await를 사용하지 않는가?** - 테스트 코드 외에는 피하기
- [ ] **중첩 Future가 없는가?** - `flatMap` 사용
- [ ] **타임아웃이 설정되어 있는가?** - 외부 호출 시 필수

**모든 항목을 확인했는데도 예외가 사라진다면**, `onComplete`으로 디버깅 로그를 추가하세요.

---

## 관련 문서

- [동시성](../concepts/concurrency/) - Future와 Promise 기초
- [Implicit/Given 디버깅](implicit-debugging/) - ExecutionContext 문제 해결
- [함수형 패턴](../concepts/functional-patterns/) - 모나딕 에러 처리
