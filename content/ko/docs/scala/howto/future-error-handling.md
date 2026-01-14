---
lastmod: "2026-01-14"
title: Future 에러 처리
weight: 2
---

비동기 코드에서 예외를 안전하게 처리하고 디버깅하는 방법을 안내합니다.

{{< callout type="tip" title="TL;DR" >}}
- `recover`/`recoverWith`: 특정 예외를 처리하고 대체값 반환
- `transform`: 성공/실패 모두 변환
- `onComplete`: 부수 효과 (로깅 등)에만 사용
- **절대 하지 말 것**: `Await.result`로 예외를 동기적으로 던지기
{{< /callout >}}

## 증상

Future에서 발생한 예외가 조용히 사라지거나, 예상치 못한 곳에서 터집니다:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val future = Future {
  throw new RuntimeException("Something went wrong")
}
// 프로그램은 정상 종료되지만, 예외는 어디로 갔을까?
```

---

## 1단계: Future 실패 기본 이해

### 1.1 Future의 두 가지 상태

```scala
import scala.util.{Success, Failure}

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

---

## 2단계: 에러 처리 패턴

### 2.1 recover - 예외를 값으로 변환

특정 예외를 처리하고 기본값을 반환합니다:

```scala
val future = Future {
  val result = riskyOperation()
  result
}.recover {
  case e: IllegalArgumentException =>
    println(s"Invalid argument: ${e.getMessage}")
    defaultValue
  case e: IOException =>
    println(s"IO error: ${e.getMessage}")
    fallbackValue
}
```

**실제 예시:**
```scala
def fetchUserAge(userId: String): Future[Int] = {
  fetchFromDatabase(userId)
    .map(_.age)
    .recover {
      case _: UserNotFoundException => 0  // 기본값
      case e: DatabaseException =>
        logger.error(s"DB error for user $userId", e)
        -1  // 에러 표시값
    }
}
```

### 2.2 recoverWith - 예외를 다른 Future로 변환

실패 시 대체 Future를 실행합니다:

```scala
def fetchFromPrimary(): Future[Data] = ???
def fetchFromBackup(): Future[Data] = ???

val result = fetchFromPrimary().recoverWith {
  case _: TimeoutException =>
    println("Primary timed out, trying backup...")
    fetchFromBackup()
}
```

### 2.3 transform - 성공/실패 모두 변환

성공과 실패를 모두 처리해야 할 때 사용합니다:

```scala
import scala.util.{Try, Success, Failure}

val future = riskyOperation().transform {
  case Success(value) =>
    Success(s"Got: $value")
  case Failure(e) =>
    Success(s"Failed: ${e.getMessage}")  // 실패를 성공으로 변환
}
```

---

## 3단계: 로깅과 모니터링

### 3.1 onComplete - 부수 효과용

로깅이나 모니터링에 사용합니다. **절대 비즈니스 로직에 사용하지 마세요**:

```scala
val future = processOrder(orderId)

future.onComplete {
  case Success(result) =>
    logger.info(s"Order $orderId processed: $result")
    metrics.incrementCounter("orders.success")
  case Failure(e) =>
    logger.error(s"Order $orderId failed", e)
    metrics.incrementCounter("orders.failure")
}

// 결과를 반환하려면 별도의 map/recover 사용
```

### 3.2 andThen - 체이닝 가능한 부수 효과

`onComplete`과 비슷하지만 체이닝이 가능합니다:

```scala
val result = processOrder(orderId)
  .andThen {
    case Success(_) => logger.info(s"Order $orderId started")
  }
  .map(transformResult)
  .andThen {
    case Success(_) => logger.info(s"Order $orderId completed")
    case Failure(e) => logger.error(s"Order $orderId failed", e)
  }
```

---

## 4단계: 흔한 실수와 해결

### 4.1 Await 남용

```scala
// 잘못된 예: 블로킹으로 예외 처리
import scala.concurrent.Await
import scala.concurrent.duration._

try {
  val result = Await.result(future, 5.seconds)  // 블로킹!
} catch {
  case e: Exception => handleError(e)
}

// 올바른 예: 비동기 처리
future.recover {
  case e: Exception =>
    handleError(e)
    defaultValue
}
```

### 4.2 예외 삼키기

```scala
// 잘못된 예: 예외를 무시
future.onComplete {
  case Failure(_) => // 아무것도 안 함
  case Success(v) => process(v)
}

// 올바른 예: 최소한 로깅
future.onComplete {
  case Failure(e) => logger.error("Unexpected error", e)
  case Success(v) => process(v)
}
```

### 4.3 중첩 Future

```scala
// 잘못된 예: Future[Future[T]]
def fetchUser(id: String): Future[User] = ???
def fetchOrders(user: User): Future[List[Order]] = ???

val nested: Future[Future[List[Order]]] = fetchUser("123").map(fetchOrders)

// 올바른 예: flatMap 사용
val flat: Future[List[Order]] = fetchUser("123").flatMap(fetchOrders)
```

---

## 5단계: 여러 Future 조합 시 에러 처리

### 5.1 for comprehension

하나라도 실패하면 전체가 실패합니다:

```scala
val result = for {
  user <- fetchUser(userId)
  orders <- fetchOrders(user.id)
  payments <- fetchPayments(user.id)
} yield (user, orders, payments)

result.recover {
  case e: UserNotFoundException => (defaultUser, Nil, Nil)
  case e: Exception =>
    logger.error("Failed to fetch data", e)
    throw e  // 다시 던지기
}
```

### 5.2 Future.sequence 에러 처리

```scala
val futures: List[Future[Int]] = List(
  Future.successful(1),
  Future.failed(new Exception("Error")),
  Future.successful(3)
)

// 하나라도 실패하면 전체 실패
Future.sequence(futures).recover {
  case e => List.empty  // 에러 시 빈 리스트
}

// 개별 실패를 허용하려면
val recovered = futures.map(_.recover { case _ => -1 })
Future.sequence(recovered)  // List(1, -1, 3)
```

---

## 체크리스트

Future 에러 처리 시 확인사항:

- [ ] **모든 Future에 에러 핸들러가 있는가?** - `recover` 또는 `recoverWith`
- [ ] **예외가 로깅되는가?** - `onComplete` 또는 `andThen`
- [ ] **Await를 사용하지 않는가?** - 테스트 코드 외에는 피하기
- [ ] **중첩 Future가 없는가?** - `flatMap` 사용
- [ ] **타임아웃이 설정되어 있는가?** - 외부 호출 시 필수

---

## 관련 문서

- [동시성](../../concepts/concurrency/) - Future와 Promise 기초
- [Implicit/Given 디버깅](../implicit-debugging/) - ExecutionContext 문제 해결
- [함수형 패턴](../../concepts/functional-patterns/) - 모나딕 에러 처리
