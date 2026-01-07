# 콘텐츠 심화 개선 계획

**목표:** 평가 점수 4.5+ (A등급) 달성
**대상 항목:** A2(깊이), C5(실무 예제), D2(시각적 보조)

---

## A2. 콘텐츠 깊이 강화

### 현재 상태
- 점수: 4/5
- "Why" 설명은 있으나 고급 주제 심화 부족

### 개선 계획

#### 1. Kafka 심화 콘텐츠

| 문서 | 추가할 내용 | 우선순위 |
|------|------------|----------|
| `kafka/concepts/transactions.md` | 실제 장애 시나리오와 복구 과정 | High |
| `kafka/concepts/replication.md` | ISR 동작 원리 심화, 리더 선출 과정 | High |
| `kafka/concepts/consumer-tuning.md` | 실무 성능 튜닝 케이스 스터디 | Medium |

**추가 예정 콘텐츠:**
```
"왜 이렇게 동작하는가" 섹션 추가
- Kafka가 Zero-copy를 사용하는 이유
- Log Compaction의 내부 동작
- Consumer Rebalance 프로토콜 상세
```

#### 2. DDD 심화 콘텐츠

| 문서 | 추가할 내용 | 우선순위 |
|------|------------|----------|
| `ddd/concepts/cqrs.md` | CQRS 없이 시작했다가 전환한 사례 | High |
| `ddd/concepts/domain-events.md` | 이벤트 스키마 진화 전략 | Medium |
| `ddd/concepts/testing.md` | 레거시 코드를 DDD로 리팩토링하는 과정 | Medium |

**추가 예정 콘텐츠:**
```
실무 경험 기반 팁
- "처음부터 완벽한 Aggregate를 설계하지 마라"
- "이벤트 소싱을 도입하기 전 고려사항"
- "Bounded Context를 잘못 나눈 경험과 교훈"
```

#### 3. Scala 심화 콘텐츠

| 문서 | 추가할 내용 | 우선순위 |
|------|------------|----------|
| `scala/concepts/type-classes.md` | 타입 클래스가 해결하는 Expression Problem | High |
| `scala/concepts/implicits.md` | implicit 남용의 위험과 가이드라인 | Medium |
| `scala/concepts/macros-metaprogramming.md` | 매크로 사용 판단 기준 | Low |

---

## C5. 실무 예제 다양화

### 현재 상태
- 점수: 4/5
- Order System 예제만 존재

### 개선 계획

#### 1. 추가 도메인 예제 후보

| 도메인 | 학습 포인트 | 난이도 |
|--------|------------|--------|
| **결제 시스템** | 외부 API 통합, 보상 트랜잭션 | Medium |
| **재고 관리** | 동시성 제어, 낙관적 락 | Medium |
| **알림 서비스** | 비동기 처리, 재시도 패턴 | Easy |
| **추천 시스템** | 데이터 파이프라인, 배치 처리 | Hard |

#### 2. 결제 시스템 예제 (우선순위 1)

```
examples/payment-system/
├── src/main/java/
│   ├── domain/
│   │   ├── Payment.java          # Aggregate
│   │   ├── PaymentStatus.java    # Status machine
│   │   └── PaymentMethod.java    # Value Object
│   ├── application/
│   │   ├── PaymentService.java   # 유스케이스
│   │   └── PaymentEventHandler.java
│   └── infrastructure/
│       ├── PgGateway.java        # 외부 PG 연동
│       └── PaymentEventPublisher.java
└── 학습 포인트
    - Saga 패턴 (주문→결제→재고)
    - 보상 트랜잭션
    - 멱등성 보장
```

#### 3. 재고 관리 예제 (우선순위 2)

```
examples/inventory-system/
├── 학습 포인트
│   - 동시성 제어 (비관적 vs 낙관적 락)
│   - 재고 예약과 확정 분리
│   - Consumer Lag 대응
```

---

## D2. 시각적 보조 개선

### 현재 상태
- 점수: 4/5
- Mermaid 다이어그램 풍부하나 일부 복잡함

### 개선 계획

#### 1. 복잡한 다이어그램 단순화

| 대상 문서 | 문제 | 개선 방안 |
|----------|------|----------|
| `kafka/concepts/replication.md` | subgraph 과다 | 단계별 분리 |
| `ddd/concepts/cqrs.md` | 전체 아키텍처가 한 다이어그램 | 읽기/쓰기 분리 |

#### 2. 새 시각 자료 추가

| 유형 | 대상 | 내용 |
|------|------|------|
| **비교 테이블** | Kafka vs RabbitMQ | 장단점 명확화 |
| **플로우차트** | Consumer Rebalance | 단계별 진행 |
| **시퀀스 다이어그램** | Saga 패턴 | 보상 트랜잭션 흐름 |

#### 3. 다이어그램 스타일 가이드

```
권장 사항:
- subgraph는 최대 3개
- 노드는 화면당 최대 10개
- 한 다이어그램 = 한 개념
- 복잡한 흐름은 여러 다이어그램으로 분할
```

---

## 실행 계획

### Phase 1: 즉시 실행 (1주)
- [x] About 페이지 추가
- [x] aggregate.md 분할
- [ ] 복잡한 다이어그램 3개 단순화

### Phase 2: 단기 (2주)
- [ ] Kafka transactions.md 심화 섹션 추가
- [ ] DDD cqrs.md "왜 CQRS인가" 섹션 강화
- [ ] 결제 시스템 예제 기본 구조

### Phase 3: 중기 (4주)
- [ ] 결제 시스템 예제 완성
- [ ] 재고 관리 예제 시작
- [ ] 전체 다이어그램 스타일 통일

---

## 예상 점수 변화

| 항목 | 현재 | Phase 1 후 | Phase 2 후 | Phase 3 후 |
|------|------|------------|------------|------------|
| A2 깊이 | 4.0 | 4.0 | 4.3 | 4.5 |
| C5 실무 예제 | 4.0 | 4.0 | 4.2 | 4.5 |
| D2 시각적 보조 | 4.0 | 4.2 | 4.3 | 4.5 |
| **총점 예상** | 4.11 | 4.2 | 4.35 | **4.5+** |
