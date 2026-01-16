---
lastmod: "2026-01-16"
title: Pod 트러블슈팅
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **목표**: Pod가 정상적으로 시작되지 않거나 비정상 종료될 때 원인을 파악하고 해결합니다
> **예상 시간**: 30분

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: Pod 시작 실패, CrashLoopBackOff, ImagePullBackOff, Readiness 실패

**다루지 않는 내용**: 네트워크 연결 문제([네트워크 트러블슈팅](network-troubleshooting/) 참조), 리소스 부족으로 인한 성능 저하([리소스 최적화](resource-optimization/) 참조)
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

{{< callout type="warning" title="버전 호환성" >}}
kubectl 버전은 클러스터 버전과 ±1 마이너 버전 이내여야 합니다.
예: 클러스터 1.27 → kubectl 1.26~1.28 사용 가능
{{< /callout >}}

### 2. 클러스터 접근 확인

```bash
kubectl cluster-info
```

**성공 시 출력:**
```
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 3. 권한 확인

```bash
kubectl auth can-i get pods
kubectl auth can-i get events
```

**성공 시 출력 (각각):**
```
yes
```

---

## 1단계: Pod 상태 확인

먼저 Pod의 현재 상태를 확인하세요.

```bash
kubectl get pods
```

**예상 출력:**
```
NAME                     READY   STATUS             RESTARTS   AGE
my-app-xxx-yyy           1/1     Running            0          5m
my-app-xxx-zzz           0/1     CrashLoopBackOff   3          2m
```

Pod 상태에 따라 다음 단계로 진행하세요.

| 상태 | 의미 | 다음 단계 |
|------|------|----------|
| `Pending` | 스케줄링 대기 중 | [2단계: Pending 해결](#2단계-pending-상태-해결) |
| `ImagePullBackOff` | 이미지 가져오기 실패 | [3단계: 이미지 문제 해결](#3단계-이미지-문제-해결) |
| `CrashLoopBackOff` | 컨테이너 반복 재시작 | [4단계: 크래시 해결](#4단계-crashloopbackoff-해결) |
| `Running` but not Ready | 헬스 체크 실패 | [5단계: Readiness 해결](#5단계-running-but-not-ready-해결) |

---

## 2단계: Pending 상태 해결

Pod가 Pending 상태로 멈춰 있는 경우입니다.

### 원인 진단

```bash
kubectl describe pod <pod-name>
```

**Events 섹션을 확인하세요.** 아래로 스크롤하여 마지막 부분을 확인합니다.

### 일반적인 원인과 해결

#### 원인 1: 리소스 부족

**이벤트 메시지:**
```
Warning  FailedScheduling  Insufficient cpu/memory
```

**해결:**

1. 노드 리소스 상태를 확인하세요.
   ```bash
   kubectl top nodes
   ```

2. 리소스 requests를 줄이거나 노드를 추가하세요.
   ```yaml
   resources:
     requests:
       memory: "256Mi"  # 줄이기
       cpu: "100m"
   ```

#### 원인 2: 노드 셀렉터 불일치

**이벤트 메시지:**
```
Warning  FailedScheduling  0/3 nodes are available: node(s) didn't match node selector
```

**해결:**

1. 노드 레이블을 확인하세요.
   ```bash
   kubectl get nodes --show-labels
   ```

2. Pod의 nodeSelector가 일치하는지 확인하세요.

#### 원인 3: PVC 바인딩 대기

**이벤트 메시지:**
```
Warning  FailedScheduling  persistentvolumeclaim "my-pvc" not found
```

**해결:**

1. PVC 상태를 확인하세요.
   ```bash
   kubectl get pvc
   ```

2. PVC가 Bound 상태인지 확인하세요.

**성공 확인:** `kubectl get pod <pod-name>`에서 상태가 `Pending`에서 다른 상태로 변경되면 성공입니다.

---

## 3단계: 이미지 문제 해결

ImagePullBackOff 또는 ErrImagePull 상태입니다.

### 원인 진단

```bash
kubectl describe pod <pod-name>
```

**Events 섹션에서 이미지 관련 메시지를 확인하세요.**

### 일반적인 원인과 해결

#### 원인 1: 이미지 이름/태그 오타

**이벤트 메시지:**
```
Failed to pull image "ngninx:latest": rpc error: code = NotFound
```

**해결:**

1. 이미지 이름을 확인하세요.
   ```bash
   kubectl get deployment <name> -o jsonpath='{.spec.template.spec.containers[0].image}'
   ```

2. 정확한 이미지 이름과 태그로 수정하세요.

#### 원인 2: 프라이빗 레지스트리 인증 필요

**이벤트 메시지:**
```
Failed to pull image: unauthorized: authentication required
```

**해결:**

1. imagePullSecret을 생성하세요.
   ```bash
   kubectl create secret docker-registry my-registry-secret \
     --docker-server=registry.example.com \
     --docker-username=myuser \
     --docker-password=mypassword
   ```

2. Deployment에 secret을 추가하세요.
   ```yaml
   spec:
     imagePullSecrets:
     - name: my-registry-secret
   ```

**성공 확인:** `kubectl get pod <pod-name>`에서 상태가 `Running`으로 변경되면 성공입니다.

---

## 4단계: CrashLoopBackOff 해결

컨테이너가 시작 후 즉시 종료되어 반복 재시작되는 경우입니다.

### 원인 진단

**가장 먼저 이전 컨테이너 로그를 확인하세요.**

```bash
kubectl logs <pod-name> --previous
```

{{< callout type="tip" title="로그가 없을 때" >}}
컨테이너가 너무 빨리 종료되면 로그가 없을 수 있습니다. 이 경우 Exit Code를 확인하세요.
{{< /callout >}}

**Exit Code 확인:**

```bash
kubectl describe pod <pod-name>
```

**Last State 섹션을 찾아 Exit Code를 확인하세요:**
```
Last State:     Terminated
  Exit Code:    137
  Reason:       OOMKilled
```

### Exit Code별 해결

| Exit Code | 의미 | 해결 방법 |
|-----------|------|----------|
| **0** | 정상 종료 | 컨테이너 명령어가 즉시 완료됨. ENTRYPOINT/CMD가 장기 실행 프로세스인지 확인하세요. |
| **1** | 애플리케이션 오류 | 로그에서 오류 메시지를 확인하세요. 환경 변수, 설정 파일을 점검하세요. |
| **137** | OOM Killed | 메모리 limits를 증가시키세요. |
| **143** | SIGTERM | 정상적인 종료 신호. livenessProbe 설정을 확인하세요. |

#### OOM Killed 해결

```yaml
resources:
  limits:
    memory: "512Mi"  # 기존보다 증가
```

**성공 확인:** `kubectl get pod <pod-name>`에서 RESTARTS가 더 이상 증가하지 않고 `Running` 상태가 유지되면 성공입니다.

---

## 5단계: Running but not Ready 해결

Pod가 Running이지만 Ready가 0/1인 경우입니다.

### 원인 진단

```bash
kubectl describe pod <pod-name>
```

**Conditions 섹션에서 `Ready: False`를 확인하세요.**

**Events 섹션에서 Readiness probe 실패 메시지를 찾으세요:**
```
Warning  Unhealthy  Readiness probe failed: Get "http://10.x.x.x:8080/health": connection refused
```

### 일반적인 원인과 해결

#### 원인 1: 잘못된 엔드포인트

**해결:**

1. Pod 내부에서 엔드포인트를 직접 테스트하세요.
   ```bash
   kubectl exec <pod-name> -- curl -s localhost:8080/health
   ```

2. 엔드포인트 경로와 포트를 수정하세요.

#### 원인 2: 애플리케이션 시작 시간 부족

**해결:**

initialDelaySeconds를 증가시키세요.

```yaml
readinessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30  # 증가
  periodSeconds: 10
```

**성공 확인:** `kubectl get pod <pod-name>`에서 READY가 `1/1`로 표시되면 성공입니다.

---

## 자주 발생하는 오류

### "error: pod not found"

**원인:** Pod 이름이 잘못되었거나 네임스페이스가 다릅니다.

**해결:**
```bash
# 모든 네임스페이스에서 Pod 검색
kubectl get pods --all-namespaces | grep <pod-name>

# 올바른 네임스페이스 지정
kubectl get pod <pod-name> -n <namespace>
```

### "error: unable to upgrade connection"

**원인:** kubectl exec 시 API 서버와의 연결 문제입니다.

**해결:**
- 네트워크 연결 확인
- 프록시 설정 확인

### "OCI runtime create failed: container_linux.go"

**원인:** 컨테이너 런타임 문제입니다.

**해결:**
- 이미지가 현재 아키텍처와 호환되는지 확인 (amd64 vs arm64)
- 노드의 컨테이너 런타임 상태 확인

---

## 환경별 참고사항

{{< tabs "env-specific" >}}
{{< tab "Minikube" >}}
```bash
# Minikube SSH로 직접 디버깅
minikube ssh

# 로컬 이미지 사용 시
eval $(minikube docker-env)
docker images
```
{{< /tab >}}
{{< tab "EKS" >}}
```bash
# CloudWatch Logs에서 로그 확인
aws logs filter-log-events --log-group-name /aws/eks/<cluster>/cluster

# 노드 상태 확인
kubectl get nodes -o wide
```
{{< /tab >}}
{{< tab "GKE" >}}
```bash
# Cloud Logging에서 로그 확인
gcloud logging read "resource.type=k8s_container"

# 노드 풀 상태 확인
gcloud container node-pools list --cluster <cluster-name>
```
{{< /tab >}}
{{< /tabs >}}

---

## 체크리스트

### 시작 실패 (Pending)
- [ ] `kubectl describe pod`로 이벤트 확인 완료
- [ ] 노드 리소스가 충분한지 확인 (`kubectl top nodes`)
- [ ] nodeSelector/tolerations 설정 확인

### 이미지 문제
- [ ] 이미지 이름과 태그가 정확한지 확인
- [ ] imagePullSecrets 설정 확인 (프라이빗 레지스트리)
- [ ] 레지스트리 접근 가능 여부 확인

### 크래시 반복
- [ ] `kubectl logs --previous`로 로그 확인
- [ ] Exit Code 확인 (137 = OOM)
- [ ] 환경 변수/ConfigMap/Secret 설정 확인

### Ready 안 됨
- [ ] Readiness Probe 엔드포인트와 포트 확인
- [ ] initialDelaySeconds가 충분한지 확인
- [ ] 외부 의존성(DB 등) 연결 확인

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 네트워크 문제 해결 | [네트워크 트러블슈팅](network-troubleshooting/) |
| 리소스 최적화 | [리소스 최적화](resource-optimization/) |
| 헬스 체크 설정 | [헬스 체크]({{< relref "/docs/kubernetes/concepts/health-checks" >}}) |
| 배포 실습 | [Spring Boot 배포]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) |
