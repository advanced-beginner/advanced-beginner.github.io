# 콘텐츠 심화 개선 계획

**목표:** 평가 점수 4.5+ (A등급) 달성
**대상 항목:** A2(깊이), C5(실무 예제), D2(시각적 보조)
**작성일:** 2026-01-07

---

## A2. 콘텐츠 깊이 강화

### 현재 상태
- 점수: 4/5
- "Why" 설명은 있으나 고급 주제 심화 부족

### 개선 계획

#### 1. Kafka 심화 콘텐츠

| 문서 | 추가할 섹션 | 구체적 내용 | 우선순위 |
|------|-----------|------------|----------|
| `kafka/concepts/transactions.md` | "장애 시나리오별 동작" | Producer 크래시, Broker 장애, Consumer 실패 시 트랜잭션 상태 | High |
| `kafka/concepts/replication.md` | "ISR 동작 원리 심화" | follower.lag.time.ms 설정과 ISR 탈락/복귀 조건 | High |
| `kafka/concepts/core-components.md` | "왜 Zero-copy인가" | sendfile() 시스템콜과 메모리 복사 최소화 설명 | Medium |

**추가 예정 콘텐츠 상세:**

```markdown
## 새 섹션: "왜 이렇게 동작하는가"

### Kafka가 Zero-copy를 사용하는 이유
일반적인 데이터 전송:
1. 디스크 → 커널 버퍼 (DMA)
2. 커널 버퍼 → 애플리케이션 버퍼
3. 애플리케이션 버퍼 → 소켓 버퍼
4. 소켓 버퍼 → NIC

Kafka의 Zero-copy (sendfile):
1. 디스크 → 커널 버퍼 (DMA)
2. 커널 버퍼 → NIC (DMA)

→ 4번의 컨텍스트 스위칭이 2번으로 감소
→ CPU 사용량 감소, 처리량 증가
```

#### 2. DDD 심화 콘텐츠

| 문서 | 추가할 섹션 | 구체적 내용 | 우선순위 |
|------|-----------|------------|----------|
| `ddd/concepts/cqrs.md` | "CQRS 도입 전/후 비교" | 실제 코드 변경 과정, 성능 개선 수치 | High |
| `ddd/concepts/domain-events.md` | "이벤트 버전 관리" | 스키마 진화 전략 (추가/삭제/변경) | Medium |
| `ddd/concepts/aggregate.md` | "실무에서 배운 교훈" | Aggregate 경계 실수 사례 | Medium |

**추가 예정 콘텐츠 상세:**

```markdown
## 새 섹션: "실무에서 배운 교훈"

### 교훈 1: 처음부터 완벽한 Aggregate를 설계하지 마라
- 초기 설계: Order에 Payment, Shipment 모두 포함
- 문제 발생: 결제 실패 시 주문 전체가 롤백
- 해결: Payment를 별도 Aggregate로 분리
- 결과: 결제 재시도가 주문에 영향 없음

### 교훈 2: ID 참조로 시작하되, 필요하면 정보를 복사하라
- 초기: OrderLine에 ProductId만 저장
- 문제: 상품명 변경 시 과거 주문 조회 오류
- 해결: 주문 시점의 productName, price 복사
- 원칙: "이벤트 발생 시점의 스냅샷"
```

#### 3. Scala 심화 콘텐츠

| 문서 | 추가할 섹션 | 구체적 내용 | 우선순위 |
|------|-----------|------------|----------|
| `scala/concepts/type-classes.md` | "Expression Problem 해결" | 기존 타입에 새 연산 추가 사례 | High |
| `scala/concepts/implicits.md` | "implicit 사용 가이드라인" | 언제 쓰고 언제 피해야 하는가 | Medium |

---

## C5. 실무 예제 다양화

### 현재 상태
- 점수: 4/5
- Order System 예제만 존재

### 개선 계획

#### 1. 결제 시스템 예제 (우선순위 1)

**디렉토리 구조:**
```
examples/payment-system/
├── build.gradle.kts
├── src/main/java/com/example/payment/
│   ├── domain/
│   │   ├── Payment.java              # Aggregate Root
│   │   ├── PaymentId.java            # Value Object
│   │   ├── PaymentStatus.java        # Enum (PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED)
│   │   ├── PaymentMethod.java        # Value Object (card, bank_transfer)
│   │   └── Money.java                # Value Object
│   ├── application/
│   │   ├── PaymentService.java       # 유스케이스
│   │   ├── PaymentEventHandler.java  # 이벤트 핸들러
│   │   └── PaymentSaga.java          # Saga 조율자
│   ├── infrastructure/
│   │   ├── pg/
│   │   │   ├── PgGateway.java        # 외부 PG 인터페이스
│   │   │   └── TossPayGateway.java   # 토스 PG 구현
│   │   └── PaymentKafkaPublisher.java
│   └── PaymentApplication.java
└── src/test/java/
    └── PaymentServiceTest.java
```

**핵심 학습 포인트:**
```java
// 1. Saga 패턴 - 주문→결제→재고 흐름
public class OrderPaymentSaga {
    // 성공 흐름: OrderCreated → PaymentRequested → PaymentCompleted → StockReserved
    // 실패 시: PaymentFailed → OrderCancelled (보상 트랜잭션)
}

// 2. 멱등성 보장 - 중복 요청 처리
public class PaymentService {
    public Payment processPayment(PaymentRequest request) {
        // 동일한 orderId로 이미 결제 존재하면 기존 결제 반환
        return paymentRepository.findByOrderId(request.orderId())
            .orElseGet(() -> createNewPayment(request));
    }
}

// 3. 외부 API 장애 대응 - Circuit Breaker
@CircuitBreaker(name = "pg", fallbackMethod = "fallbackPayment")
public PaymentResult requestPayment(PaymentRequest request) {
    return pgGateway.authorize(request);
}
```

#### 2. 재고 관리 예제 (우선순위 2)

**핵심 학습 포인트:**
```java
// 1. 낙관적 락 - 동시 재고 차감
@Version
private Long version;

public void deduct(int quantity) {
    if (this.quantity < quantity) {
        throw new InsufficientStockException();
    }
    this.quantity -= quantity;
}

// 2. 재고 예약/확정 분리
public class Stock {
    private int available;   // 가용 재고
    private int reserved;    // 예약된 재고

    public void reserve(int qty) {
        if (available < qty) throw new InsufficientStockException();
        available -= qty;
        reserved += qty;
    }

    public void confirm(int qty) {
        reserved -= qty;  // 예약 → 확정 (재고 실제 차감)
    }

    public void cancel(int qty) {
        reserved -= qty;
        available += qty;  // 예약 취소 → 가용 복원
    }
}
```

---

## D2. 시각적 보조 개선

### 현재 상태
- 점수: 4/5
- Mermaid 다이어그램 풍부하나 일부 복잡함

### 단순화 대상 다이어그램

#### 1. `kafka/concepts/replication.md` - ISR 다이어그램

**현재 문제:** 한 다이어그램에 Leader, Follower, ISR, 동기화 흐름이 모두 포함

**개선안:** 3개 다이어그램으로 분할

```markdown
### 다이어그램 1: 기본 구조
[Broker 1 (Leader)] ← [Broker 2 (Follower)]
                    ← [Broker 3 (Follower)]

### 다이어그램 2: ISR 개념
ISR = {Broker 1, Broker 2}  ← 동기화 완료된 복제본
Non-ISR = {Broker 3}        ← 동기화 지연

### 다이어그램 3: 장애 복구
1. Leader 장애 발생
2. ISR 중 새 Leader 선출
3. 클라이언트 재연결
```

#### 2. `ddd/concepts/cqrs.md` - 전체 아키텍처

**현재 문제:** Command/Query/Event/Read Model이 한 다이어그램에 혼재

**개선안:** 2개 다이어그램으로 분할

```markdown
### 다이어그램 1: Command 흐름 (쓰기)
Client → Command → Command Handler → Aggregate → Event Store

### 다이어그램 2: Query 흐름 (읽기)
Client → Query → Query Handler → Read Model → Response
         ↑
     Event Projector (Event Store에서 Read Model 구축)
```

#### 3. `kafka/concepts/consumer-group-offset.md` - Rebalance 흐름

**현재 문제:** Consumer 추가/제거/장애 시나리오가 한 다이어그램

**개선안:** 단계별 시퀀스 다이어그램

```markdown
### Consumer 추가 시 Rebalance
1. 새 Consumer가 Group에 Join 요청
2. Coordinator가 Rebalance 트리거
3. 모든 Consumer가 현재 Partition 해제
4. Coordinator가 새로운 할당 계산
5. 각 Consumer가 새 Partition 할당받음
```

### 다이어그램 스타일 가이드

| 규칙 | 기준 | 이유 |
|------|------|------|
| subgraph 수 | 최대 3개 | 시각적 복잡도 제한 |
| 노드 수 | 최대 10개 | 한눈에 파악 가능 |
| 화살표 | 한 방향 우선 | 흐름 명확화 |
| 텍스트 | 3단어 이내 | 가독성 확보 |

---

## 실행 계획

### Phase 1: 즉시 실행 (완료/진행중)
- [x] About 페이지 추가
- [x] aggregate.md 분할
- [x] lastmod 실제 날짜 반영
- [x] 언어 품질 개선 (Quick Start 문서들)
- [ ] replication.md 다이어그램 분할
- [ ] cqrs.md 다이어그램 분할

### Phase 2: 단기
- [ ] Kafka transactions.md "장애 시나리오" 섹션 추가
- [ ] DDD aggregate.md "실무 교훈" 섹션 추가
- [ ] 결제 시스템 예제 기본 구조 생성

### Phase 3: 중기
- [ ] 결제 시스템 예제 완성 (Saga, 멱등성)
- [ ] 재고 관리 예제 (낙관적 락, 예약/확정)
- [ ] 전체 다이어그램 스타일 통일

---

## 예상 점수 변화

| 항목 | 현재 | Phase 1 후 | Phase 2 후 | Phase 3 후 |
|------|------|------------|------------|------------|
| A2 깊이 | 4.0 | 4.0 | 4.3 | 4.5 |
| C5 실무 예제 | 4.0 | 4.0 | 4.2 | 4.5 |
| D2 시각적 보조 | 4.0 | 4.3 | 4.4 | 4.5 |
| **총점 예상** | 4.24 | 4.30 | 4.40 | **4.5+** |

---

*마지막 업데이트: 2026-01-07*
