---
lastmod: "2026-01-11"
title: 리소스 최적화
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **목표**: 적절한 CPU/메모리 설정을 찾아 리소스 효율성을 높입니다
> **선수 지식**: 리소스 관리 개념
> **예상 시간**: 45분

{{< callout type="tip" title="TL;DR" >}}
- Metrics Server로 실제 사용량 측정
- VPA Recommender로 권장 값 확인
- 점진적으로 조정하며 모니터링
{{< /callout >}}

## 왜 리소스 최적화가 필요한가?

| 설정 | 문제 |
|------|------|
| requests가 너무 높음 | 리소스 낭비, 스케줄링 어려움 |
| requests가 너무 낮음 | 스로틀링, OOM 위험 |
| limits가 너무 높음 | 과도한 리소스 사용 허용 |
| limits가 너무 낮음 | 스로틀링, OOM 빈번 |

## 1. 현재 사용량 측정

### Metrics Server 설치 확인

```bash
# Metrics Server 동작 확인
kubectl top nodes
kubectl top pods
```

오류가 발생하면 Metrics Server를 설치하세요.

```bash
# Minikube
minikube addons enable metrics-server

# 기타 환경
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### 사용량 확인

```bash
# Pod별 사용량
kubectl top pods

# 컨테이너별 사용량
kubectl top pods --containers

# 특정 네임스페이스
kubectl top pods -n production
```

**예상 출력:**
```
NAME                    CPU(cores)   MEMORY(bytes)
my-app-xxx-yyy          50m          256Mi
my-app-xxx-zzz          45m          248Mi
```

### 시간별 사용량 추적

지속적인 모니터링을 위해 watch와 함께 사용합니다.

```bash
# 2초마다 갱신
watch -n 2 kubectl top pods
```

## 2. 적정 값 산정

### 경험적 가이드라인

| 항목 | 권장 값 |
|------|--------|
| CPU requests | 평상시 사용량의 80-100% |
| CPU limits | requests의 2-4배 또는 미설정 |
| Memory requests | 평상시 사용량의 110-120% |
| Memory limits | requests의 1.2-1.5배 |

### VPA Recommender 사용

VPA를 설치하면 권장 값을 자동으로 계산합니다.

```bash
# VPA 설치 (컴포넌트만 설치)
git clone https://github.com/kubernetes/autoscaler.git
cd autoscaler/vertical-pod-autoscaler
./hack/vpa-up.sh
```

```yaml
# vpa.yaml - 권장 값만 확인 (적용 안 함)
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
    updateMode: "Off"  # 적용하지 않고 권장만
```

```bash
# VPA 권장 값 확인
kubectl describe vpa my-app-vpa
```

## 3. 리소스 설정 조정

### 점진적 조정 방법

1. 현재 사용량 측정 (최소 1시간)
2. requests를 평상시 사용량 + 20%로 설정
3. limits를 requests의 1.5배로 설정
4. 배포 후 모니터링
5. 필요시 조정 반복

### 설정 예시

**Before (과도한 설정):**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

**실제 사용량:** CPU 50m, Memory 256Mi

**After (최적화):**
```yaml
resources:
  requests:
    memory: "300Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

## 4. 모니터링 및 검증

### 스로틀링 확인

CPU 스로틀링이 발생하는지 확인합니다.

```bash
# Pod에서 확인 (cgroup v1)
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu/cpu.stat
# nr_throttled가 증가하면 스로틀링 발생

# cgroup v2
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu.stat
```

### OOM 확인

```bash
# OOM 이벤트 확인
kubectl get events --field-selector reason=OOMKilling

# Pod 상태에서 확인
kubectl describe pod <pod-name> | grep -A 5 "Last State"
```

### QoS 클래스 확인

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.qosClass}'
```

## 5. Java 애플리케이션 특화 설정

### JVM 힙과 컨테이너 메모리

```yaml
resources:
  requests:
    memory: "512Mi"
  limits:
    memory: "1Gi"
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0"  # 컨테이너 메모리의 75%를 힙으로
```

JVM 힙 설정 가이드라인은 다음과 같습니다.

| 컨테이너 메모리 | 권장 힙 비율 |
|----------------|-------------|
| < 512Mi | 50-60% |
| 512Mi - 2Gi | 65-75% |
| > 2Gi | 75-80% |

### Spring Boot 설정

```yaml
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

## 체크리스트

- [ ] Metrics Server가 설치되어 있는가?
- [ ] 최소 1시간 이상 사용량을 측정했는가?
- [ ] requests가 평상시 사용량보다 약간 높은가?
- [ ] limits가 피크 사용량을 수용할 수 있는가?
- [ ] Java 앱의 경우 JVM 힙이 적절히 설정되었는가?
- [ ] 배포 후 스로틀링/OOM이 발생하지 않는가?

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 자동 스케일링 | [스케일링](../../concepts/scaling/) |
| Pod 문제 해결 | [Pod 트러블슈팅](../pod-troubleshooting/) |
| 리소스 관리 개념 | [리소스 관리](../../concepts/resources/) |
