---
lastmod: "2026-01-16"
title: 리소스 최적화
description: "Kubernetes 리소스를 분석하고 최적화하는 방법을 안내합니다."
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **목표**: 적절한 CPU/메모리 설정을 찾아 리소스 효율성을 높입니다
> **예상 시간**: 45분

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: 리소스 사용량 측정, requests/limits 적정값 산정, 스로틀링/OOM 해결

**다루지 않는 내용**: 자동 스케일링([스케일링]({{< relref "/docs/kubernetes/concepts/scaling" >}}) 참조), Pod 시작 문제([Pod 트러블슈팅](pod-troubleshooting/) 참조)
{{< /callout >}}

## 시작하기 전에

다음 조건을 확인하세요.

### 1. kubectl 설치 및 버전 확인

```bash
kubectl version --client
```

**성공 시 출력:**
```
Client Version: v1.28.0
```

### 2. Metrics Server 설치 확인

```bash
kubectl top nodes
```

**성공 시 출력:**
```
NAME           CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%
node-1         250m         12%    1024Mi          50%
```

**오류가 발생하면 Metrics Server를 설치하세요.**

{{< tabs "metrics-server-install" >}}
{{< tab "Minikube" >}}
```bash
minikube addons enable metrics-server
```
{{< /tab >}}
{{< tab "기타 환경" >}}
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```
{{< /tab >}}
{{< /tabs >}}

설치 후 1-2분 대기한 후 다시 확인하세요.

### 3. 대상 Pod 확인

최적화할 Pod가 Running 상태인지 확인하세요.

```bash
kubectl get pods -l app=<your-app>
```

**성공 시 출력:**
```
NAME                     READY   STATUS    RESTARTS   AGE
my-app-xxx-yyy           1/1     Running   0          5m
```

---

## 1단계: 현재 사용량 측정

{{< callout type="warning" title="측정 기간" >}}
정확한 분석을 위해 최소 1시간 이상 측정하세요. 피크 시간대를 포함하면 더 좋습니다.
{{< /callout >}}

### Pod 리소스 사용량 확인

```bash
kubectl top pods
```

**예상 출력:**
```
NAME                    CPU(cores)   MEMORY(bytes)
my-app-xxx-yyy          50m          256Mi
my-app-xxx-zzz          45m          248Mi
```

컨테이너별로 확인하려면 다음을 실행하세요.

```bash
kubectl top pods --containers
```

**성공 확인:** CPU(cores)와 MEMORY(bytes) 값이 표시되면 성공입니다.

### 시간별 사용량 추적

2초마다 갱신되는 실시간 모니터링을 시작하세요.

```bash
watch -n 2 kubectl top pods
```

{{< callout type="tip" title="팁" >}}
피크 시간대의 최대값과 평상시 값을 모두 기록하세요.
{{< /callout >}}

**기록 예시:**

| 시간대 | CPU | Memory |
|--------|-----|--------|
| 평상시 | 50m | 256Mi |
| 피크 | 200m | 400Mi |

---

## 2단계: 현재 설정 확인

현재 Deployment의 리소스 설정을 확인하세요.

```bash
kubectl get deployment <deployment-name> -o jsonpath='{.spec.template.spec.containers[0].resources}'
```

**예상 출력:**
```json
{"limits":{"cpu":"1000m","memory":"2Gi"},"requests":{"cpu":"500m","memory":"1Gi"}}
```

### 문제 진단 기준

| 현재 상태 | 문제 | 조치 |
|----------|------|------|
| requests가 실제 사용량의 5배 이상 | 리소스 낭비 | requests 감소 |
| requests가 실제 사용량보다 낮음 | 스로틀링 위험 | requests 증가 |
| limits가 피크 사용량보다 낮음 | OOM/스로틀링 발생 | limits 증가 |

**성공 확인:** 현재 설정값을 확인하고 실제 사용량과 비교할 수 있으면 성공입니다.

---

## 3단계: 적정 값 산정

### 권장 계산 공식

```
CPU requests = 평상시 사용량 × 1.2 (20% 여유)
CPU limits   = 피크 사용량 × 1.5 또는 미설정

Memory requests = 평상시 사용량 × 1.2
Memory limits   = requests × 1.5
```

### 계산 예시

**측정값:**
- 평상시 CPU: 50m, 피크 CPU: 200m
- 평상시 Memory: 256Mi, 피크 Memory: 400Mi

**계산:**
```
CPU requests = 50m × 1.2 = 60m → 100m (반올림)
CPU limits   = 200m × 1.5 = 300m → 500m

Memory requests = 256Mi × 1.2 = 307Mi → 320Mi
Memory limits   = 320Mi × 1.5 = 480Mi → 512Mi
```

### VPA Recommender 사용 (선택 사항)

VPA를 설치하면 권장값을 자동으로 계산합니다.

```yaml
# vpa.yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: my-app-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  updatePolicy:
    updateMode: "Off"  # 권장값만 확인, 자동 적용 안 함
```

```bash
kubectl apply -f vpa.yaml
kubectl describe vpa my-app-vpa
```

**성공 확인:** Recommendation 섹션에 권장값이 표시되면 성공입니다.

---

## 4단계: 설정 변경

{{< callout type="warning" title="주의" >}}
프로덕션 환경에서는 한 번에 큰 변경을 하지 마세요. 점진적으로 조정하세요.
{{< /callout >}}

### Deployment 수정

```bash
kubectl edit deployment <deployment-name>
```

또는 patch 명령어를 사용하세요.

```bash
kubectl patch deployment <deployment-name> -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "<container-name>",
          "resources": {
            "requests": {
              "memory": "320Mi",
              "cpu": "100m"
            },
            "limits": {
              "memory": "512Mi",
              "cpu": "500m"
            }
          }
        }]
      }
    }
  }
}'
```

**성공 확인:** 새 Pod가 생성되고 Running 상태가 되면 성공입니다.

```bash
kubectl rollout status deployment <deployment-name>
```

---

## 5단계: 검증

변경 후 다음을 확인하세요.

### 스로틀링 확인

CPU 스로틀링이 발생하는지 확인하세요.

```bash
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu/cpu.stat
```

**출력에서 확인:**
```
nr_throttled 0      # 0이면 스로틀링 없음
throttled_time 0    # 0이면 정상
```

{{< callout type="tip" title="cgroup v2 환경" >}}
cgroup v2 환경에서는 다음 명령어를 사용하세요.
```bash
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu.stat
```
{{< /callout >}}

### OOM 확인

OOM 이벤트가 발생하는지 확인하세요.

```bash
kubectl get events --field-selector reason=OOMKilling
```

**성공 확인:** 이벤트가 없으면 성공입니다.

### QoS 클래스 확인

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.qosClass}'
```

| QoS 클래스 | 의미 | 권장 상황 |
|-----------|------|----------|
| Guaranteed | requests = limits | 중요 워크로드 |
| Burstable | requests < limits | 일반 워크로드 |
| BestEffort | 리소스 미설정 | 테스트용만 권장 |

**성공 확인:** 의도한 QoS 클래스가 적용되면 성공입니다.

---

## 자주 발생하는 오류

### "OOMKilled" 반복 발생

**원인:** Memory limits가 너무 낮습니다.

**해결:**
```bash
# 현재 limits 확인
kubectl describe pod <pod-name> | grep -A 2 "Limits"

# limits 증가 (예: 512Mi → 1Gi)
kubectl patch deployment <name> -p '{"spec":{"template":{"spec":{"containers":[{"name":"<container>","resources":{"limits":{"memory":"1Gi"}}}]}}}}'
```

### CPU 스로틀링으로 응답 지연

**원인:** CPU limits가 너무 낮습니다.

**해결:**
1. CPU limits를 증가시키거나
2. CPU limits를 제거하세요 (requests만 설정)

```yaml
resources:
  requests:
    cpu: "100m"
  # limits.cpu 생략 - 노드 CPU를 사용 가능
  limits:
    memory: "512Mi"
```

### "0/N nodes are available: Insufficient cpu/memory"

**원인:** requests가 노드 가용 리소스보다 큽니다.

**해결:**
```bash
# 노드 가용 리소스 확인
kubectl describe nodes | grep -A 5 "Allocatable"

# requests를 가용 리소스 이하로 조정
```

### Metrics Server "error: Metrics API not available"

**원인:** Metrics Server가 설치되지 않았거나 준비되지 않았습니다.

**해결:**
```bash
# Metrics Server 상태 확인
kubectl get pods -n kube-system | grep metrics-server

# 로그 확인
kubectl logs -n kube-system deployment/metrics-server
```

---

## Java 애플리케이션 특화 설정

Java 애플리케이션은 JVM 힙 메모리 설정이 중요합니다.

{{< callout type="warning" title="주의" >}}
JVM 힙을 컨테이너 메모리와 동일하게 설정하면 OOM이 발생합니다. JVM은 힙 외에 메타스페이스, 스레드 스택 등 추가 메모리를 사용합니다.
{{< /callout >}}

### 권장 설정

```yaml
resources:
  requests:
    memory: "512Mi"
  limits:
    memory: "1Gi"
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

### 컨테이너 메모리별 힙 비율

| 컨테이너 메모리 | 권장 힙 비율 | 이유 |
|----------------|-------------|------|
| < 512Mi | 50-60% | 비힙 메모리 비율이 상대적으로 높음 |
| 512Mi - 2Gi | 65-75% | 일반적인 설정 |
| > 2Gi | 75-80% | 대규모 힙에서는 비힙 비율 감소 |

**성공 확인:** Pod가 OOM 없이 안정적으로 동작하면 성공입니다.

---

## 체크리스트

### 측정
- [ ] Metrics Server가 설치되어 있는가?
- [ ] 최소 1시간 이상 사용량을 측정했는가?
- [ ] 피크 시간대 사용량을 확인했는가?

### 설정
- [ ] requests가 평상시 사용량의 100-120%인가?
- [ ] limits가 피크 사용량을 수용할 수 있는가?
- [ ] Java 앱의 경우 JVM 힙이 적절히 설정되었는가?

### 검증
- [ ] 변경 후 Pod가 정상 Running 상태인가?
- [ ] CPU 스로틀링이 발생하지 않는가?
- [ ] OOM이 발생하지 않는가?
- [ ] 의도한 QoS 클래스가 적용되었는가?

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 자동 스케일링 설정 | [스케일링]({{< relref "/docs/kubernetes/concepts/scaling" >}}) |
| Pod 문제 해결 | [Pod 트러블슈팅](pod-troubleshooting/) |
| 리소스 관리 개념 | [리소스 관리]({{< relref "/docs/kubernetes/concepts/resources" >}}) |
