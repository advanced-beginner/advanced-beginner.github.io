---
title: 알림 후 액션 가이드
description: PromQL 감지 후 수행할 수 있는 대응 방안
weight: 3
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

알림을 받은 후 어떻게 대응해야 하는지 상황별 가이드입니다.

## 알림 → 액션 워크플로우

```mermaid
graph LR
    A["알림 수신"] --> B["상황 파악"]
    B --> C["영향도 평가"]
    C --> D["즉시 조치"]
    D --> E["근본 원인 분석"]
    E --> F["영구 해결"]
    F --> G["사후 검토"]
```

*알림 수신부터 사후 검토까지 6단계 대응 워크플로우를 순서대로 보여줍니다.*

---

## 상황별 대응

### 1. 서비스 다운 (TargetDown)

**알림**
```yaml
alert: TargetDown
expr: up == 0
```

**즉시 조치**
```bash
# 1. 서비스 상태 확인
kubectl get pods -l app=order-service
docker ps -f name=order-service

# 2. 로그 확인
kubectl logs -l app=order-service --tail=100
docker logs order-service --tail=100

# 3. 재시작 시도
kubectl rollout restart deployment/order-service
docker restart order-service
```

**근본 원인 분석**
- OOM Kill 여부 확인
- 헬스체크 실패 원인
- 의존성 서비스 상태

**자동화 가능**
```yaml
# Kubernetes: 자동 재시작
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  failureThreshold: 3
```

---

### 2. 높은 에러율 (HighErrorRate)

**알림**
```yaml
alert: HighErrorRate
expr: error_rate > 0.05
```

**즉시 조치**
1. **영향 범위 확인**
   ```promql
   sum by (uri) (rate(http_requests_total{status=~"5.."}[5m]))
   ```

2. **최근 배포 확인**
   ```bash
   kubectl rollout history deployment/order-service
   ```

3. **롤백 (배포 원인 시)**
   ```bash
   kubectl rollout undo deployment/order-service
   ```

**근본 원인 분석**
```logql
{app="order-service"} | json | level="ERROR"
```

---

### 3. 높은 지연시간 (HighP99Latency)

**알림**
```yaml
alert: HighP99Latency
expr: p99 > 0.5
```

**즉시 조치**
1. **병목 구간 확인** (Tracing)
2. **리소스 확인**
   ```promql
   # CPU
   rate(process_cpu_seconds_total[5m])
   # 메모리
   jvm_memory_used_bytes / jvm_memory_max_bytes
   # DB 연결
   hikaricp_connections_active / hikaricp_connections_max
   ```

3. **스케일 아웃 (리소스 부족 시)**
   ```bash
   kubectl scale deployment/order-service --replicas=5
   ```

**근본 원인 분석**
- 슬로우 쿼리 확인
- 외부 API 응답 시간
- GC 로그

---

### 4. 리소스 부족 (HighCPU/Memory/Disk)

**알림**
```yaml
alert: HighCPUUsage
expr: cpu_usage > 80

alert: HighMemoryUsage
expr: memory_usage > 85

alert: DiskSpaceLow
expr: disk_avail_percent < 10
```

**CPU 높음**
```bash
# 프로세스 확인
top -c

# 스케일 아웃
kubectl scale deployment/order-service --replicas=5
```

**메모리 부족**
```bash
# 메모리 사용 프로세스
ps aux --sort=-%mem | head

# JVM 힙 덤프 (Java)
jmap -dump:format=b,file=heap.bin <pid>
```

**디스크 부족**
```bash
# 큰 파일 찾기
du -sh /* | sort -hr | head

# 로그 정리
journalctl --vacuum-time=7d
docker system prune -a
```

---

### 5. Kafka Consumer Lag

**알림**
```yaml
alert: KafkaConsumerLagHigh
expr: kafka_consumer_lag > 10000
```

**즉시 조치**
1. **Consumer 상태 확인**
   ```bash
   kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
     --describe --group order-consumer-group
   ```

2. **Consumer 재시작**
   ```bash
   kubectl rollout restart deployment/order-consumer
   ```

3. **Consumer 증설**
   ```bash
   kubectl scale deployment/order-consumer --replicas=5
   ```

**근본 원인 분석**
- 처리 속도 vs 유입 속도
- Consumer 에러 로그
- 파티션 불균형

---

## 자동화 액션

### 1. Alertmanager Webhook

```yaml
# alertmanager.yml
receivers:
  - name: 'auto-scale'
    webhook_configs:
      - url: 'http://auto-scaler:8080/scale'
        send_resolved: true
```

```python
# auto-scaler.py
@app.route('/scale', methods=['POST'])
def handle_alert():
    alert = request.json['alerts'][0]
    if alert['labels']['alertname'] == 'HighCPUUsage':
        scale_deployment(alert['labels']['service'], replicas=5)
    return 'OK'
```

### 2. Kubernetes HPA

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### 3. 서킷 브레이커 자동 활성화

```java
@CircuitBreaker(name = "payment", fallbackMethod = "fallback")
public Payment process(PaymentRequest request) {
    return paymentClient.process(request);
}

public Payment fallback(PaymentRequest request, Exception e) {
    return Payment.pending(request.getId());
}
```

---

## 런북 템플릿

```markdown
# 알림: HighErrorRate

## 증상
- 에러율 5% 초과
- 영향 서비스: order-service

## 확인 사항
1. [ ] 최근 배포 여부 확인
2. [ ] 에러 로그 확인
3. [ ] 의존성 서비스 상태 확인

## 즉시 조치
1. 배포 원인: `kubectl rollout undo deployment/order-service`
2. 트래픽 급증: 스케일 아웃
3. 의존성 장애: 서킷 브레이커 확인

## 에스컬레이션
- 15분 내 해결 안 되면: 팀 리드 호출
- 30분 내 해결 안 되면: 온콜 엔지니어 호출

## 관련 링크
- 대시보드: https://grafana/d/order-service
- 로그: https://grafana/explore?datasource=loki
- 트레이스: https://grafana/explore?datasource=tempo
```

---

## 핵심 정리

| 알림 유형 | 즉시 조치 | 자동화 가능 |
|----------|----------|------------|
| 서비스 다운 | 재시작 | Liveness Probe |
| 높은 에러율 | 롤백 | 카나리 배포 |
| 높은 지연시간 | 스케일 아웃 | HPA |
| 리소스 부족 | 정리/스케일 | HPA, 정리 CronJob |
| Kafka Lag | Consumer 증설 | KEDA |
