---
title: FAQ
description: Observability 관련 자주 묻는 질문
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

## 일반

### Q: Monitoring과 Observability의 차이는?

**Monitoring**: 사전에 정의한 지표를 감시 ("이 값이 임계치를 넘었는가?")

**Observability**: 시스템 내부 상태를 외부에서 파악할 수 있는 능력 ("왜 이 문제가 발생했는가?")

Monitoring은 Observability의 일부입니다. Observability는 예상치 못한 문제도 분석할 수 있는 능력을 포함합니다.

### Q: 3요소(Metrics, Logs, Traces) 중 뭘 먼저 도입해야 하나?

**권장 순서**:
1. **Metrics** - 시스템 상태 파악, 알림 설정
2. **Logs** - 에러 원인 분석
3. **Traces** - 분산 시스템 흐름 분석

단일 서비스라면 Metrics + Logs로 충분할 수 있습니다. 마이크로서비스라면 Traces가 필수입니다.

---

## Prometheus

### Q: Pull vs Push 중 어느 게 좋은가?

**Pull (Prometheus)**:
- 장점: 중앙 제어, 헬스체크 내장, 디버깅 용이
- 단점: 방화벽 뒤 타겟 접근 어려움

**Push (Datadog, StatsD)**:
- 장점: 방화벽 친화적, 짧은 수명 작업에 적합
- 단점: 타겟 상태 파악 어려움

**결론**: 대부분의 경우 Pull 모델이 더 운영하기 좋습니다. 짧은 배치 작업은 Pushgateway 사용.

### Q: scrape_interval은 얼마로 설정해야 하나?

| 상황 | 권장값 |
|------|--------|
| 일반적 | 15-30초 |
| 고빈도 변화 감지 | 5-10초 |
| 저우선순위/비용 절감 | 60초 |

**주의**: 너무 짧으면 Prometheus 부하 증가, 너무 길면 이상 감지 지연.

### Q: 카디널리티가 높으면 어떤 문제가 생기나?

- 메모리 사용량 급증
- 쿼리 속도 저하
- 스토리지 비용 증가
- 심하면 OOM 발생

**해결**: [카디널리티 최적화](../howto/reduce-cardinality/) 참고

---

## PromQL

### Q: rate()와 increase()의 차이는?

```promql
# rate(): 초당 평균 증가율
rate(http_requests_total[5m])  # → 10 (초당 10개)

# increase(): 총 증가량
increase(http_requests_total[5m])  # → 3000 (5분간 3000개)
```

**rate()** = increase() / 시간(초)

- 대시보드: rate() 사용
- 기간별 합계: increase() 사용

### Q: Counter에 rate()를 왜 적용해야 하나?

Counter는 누적값이라 원시 값은 의미가 없습니다.

```promql
# ❌ 서버 시작 후 총 요청 수 (언제 시작했는지에 따라 다름)
http_requests_total  # → 1,523,456

# ✅ 초당 요청 수 (현재 부하)
rate(http_requests_total[5m])  # → 42.5
```

### Q: histogram_quantile()에서 le 라벨이 왜 필요한가?

Histogram은 버킷(le = less than or equal) 별로 누적 카운트를 저장합니다.

```
bucket{le="0.1"} 100   # 0.1초 이하 100개
bucket{le="0.5"} 350   # 0.5초 이하 350개
bucket{le="1.0"} 480   # 1.0초 이하 480개
```

`le` 라벨이 없으면 버킷 구조가 깨져서 백분위 계산이 불가능합니다.

---

## Grafana

### Q: 대시보드가 너무 느린데 어떻게 해야 하나?

1. **Recording Rules 사용**: 복잡한 쿼리 사전 계산
2. **시간 범위 제한**: 최근 1시간 → 최근 6시간
3. **샘플 수 제한**: `$__rate_interval` 변수 사용
4. **불필요한 패널 제거**: 스크롤해야 보이는 패널은 lazy loading

### Q: 변수(Variables)를 어떻게 활용하나?

```yaml
# 서비스 선택 드롭다운
- name: service
  query: label_values(http_requests_total, service)

# 쿼리에서 사용
rate(http_requests_total{service="$service"}[5m])
```

변수로 하나의 대시보드에서 여러 서비스를 볼 수 있습니다.

---

## 알림

### Q: for 절을 왜 설정해야 하나?

일시적 스파이크로 인한 오탐을 방지합니다.

```yaml
# ❌ 순간 스파이크에도 알림
expr: error_rate > 0.05

# ✅ 5분 지속 시에만 알림
expr: error_rate > 0.05
for: 5m
```

### Q: 알림이 너무 많은데 어떻게 줄이나?

1. **임계값 조정**: 너무 민감하지 않게
2. **for 시간 증가**: 일시적 이상 필터링
3. **그룹화**: Alertmanager에서 유사 알림 묶기
4. **억제(Inhibition)**: 상위 알림 발생 시 하위 억제
5. **정말 액션이 필요한 것만**: 액션 없는 알림 제거

---

## 분산 추적

### Q: 샘플링률은 얼마로 설정해야 하나?

| 환경 | 샘플링률 | 이유 |
|------|---------|------|
| 개발 | 100% | 모든 요청 추적 |
| 스테이징 | 50% | 충분한 데이터 |
| 운영 | 1-10% | 비용 최적화 |

**팁**: Tail-based sampling으로 에러/느린 요청은 100% 수집

### Q: Trace ID를 어떻게 로그와 연결하나?

로그에 Trace ID를 포함시킵니다.

```java
// Spring Boot (자동)
// 로그 패턴: %X{traceId:-}

// 로그 출력
2026-01-12 10:30:00 [order-service,abc123,span001] Order created
```

Grafana에서 Loki → Tempo 연결 설정하면 로그에서 트레이스로 바로 이동 가능.

---

## 비용

### Q: Observability 비용을 어떻게 줄이나?

**Metrics**:
- 카디널리티 최적화
- 불필요한 메트릭 drop
- scrape_interval 조정

**Logs**:
- ERROR 이상만 장기 보관
- DEBUG는 단기 보관 또는 미수집
- 샘플링

**Traces**:
- 샘플링 (1-10%)
- Tail-based sampling

### Q: 오픈소스 vs 상용 서비스, 어떤 게 좋은가?

**오픈소스 (Prometheus, Loki, Tempo)**:
- 비용: 인프라 비용만
- 운영: 직접 관리 필요
- 적합: DevOps 역량 있는 팀

**상용 (Datadog, New Relic)**:
- 비용: 사용량 기반 요금
- 운영: 관리 불필요
- 적합: 빠른 도입, 운영 인력 부족

**하이브리드**: 오픈소스 + Grafana Cloud (관리형)
