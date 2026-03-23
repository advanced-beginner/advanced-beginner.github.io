---
lastmod: "2026-01-16"
title: 로그 수집 및 분석
description: "Kubernetes 로그를 수집하고 분석하는 방법을 안내합니다."
weight: 4
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **목표**: 효과적으로 로그를 수집하고 분석하여 문제를 진단합니다
> **예상 시간**: 25분

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: kubectl logs 활용, 멀티 컨테이너 로그, 로그 필터링, stern 도구

**다루지 않는 내용**: 중앙 로깅 시스템(ELK, Loki) 설정, 장기 로그 보관
{{< /callout >}}

## 시작하기 전에

다음 조건을 확인하세요.

### 1. kubectl 설치 및 클러스터 접근 확인

```bash
kubectl cluster-info
```

**성공 시 출력:**
```
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 2. 로그 접근 권한 확인

```bash
kubectl auth can-i get pods/log
```

**성공 시 출력:**
```
yes
```

### 3. 대상 Pod 확인

로그를 확인할 Pod가 존재하는지 확인하세요.

```bash
kubectl get pods
```

**성공 시 출력:**
```
NAME                     READY   STATUS    RESTARTS   AGE
my-app-xxx-yyy           1/1     Running   0          5m
```

---

## 1단계: 기본 로그 수집

### 현재 로그 확인

```bash
kubectl logs <pod-name>
```

**예상 출력:**
```
2024-01-15T10:30:45.123Z INFO  Starting application...
2024-01-15T10:30:46.456Z INFO  Connected to database
2024-01-15T10:30:47.789Z INFO  Server listening on port 8080
```

### 최근 N줄만 확인

전체 로그가 너무 길 때 사용하세요.

```bash
kubectl logs <pod-name> --tail=100
```

**성공 확인:** 로그가 출력되면 성공입니다.

### 실시간 로그 스트리밍

애플리케이션 동작을 실시간으로 모니터링하세요.

```bash
kubectl logs <pod-name> -f
```

{{< callout type="tip" title="종료 방법" >}}
`Ctrl+C`로 스트리밍을 종료하세요.
{{< /callout >}}

### 시간 범위로 필터링

최근 1시간의 로그만 확인하세요.

```bash
kubectl logs <pod-name> --since=1h
```

또는 특정 시간 이후의 로그를 확인하세요.

```bash
kubectl logs <pod-name> --since-time="2024-01-15T10:00:00Z"
```

**성공 확인:** 지정한 시간 범위의 로그만 출력되면 성공입니다.

---

## 2단계: 재시작된 컨테이너 로그

Pod가 재시작된 경우 이전 컨테이너의 로그를 확인하세요.

### 이전 컨테이너 로그 확인

```bash
kubectl logs <pod-name> --previous
```

**예상 출력:**
```
2024-01-15T10:25:00.000Z ERROR Connection timeout
2024-01-15T10:25:01.000Z ERROR Application crashed
```

{{< callout type="warning" title="주의" >}}
`--previous` 옵션은 직전에 종료된 컨테이너의 로그만 보여줍니다. 더 이전 로그는 중앙 로깅 시스템을 사용해야 합니다.
{{< /callout >}}

### 재시작 횟수 확인

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[0].restartCount}'
```

**성공 확인:** 이전 컨테이너 로그가 출력되면 성공입니다.

---

## 3단계: 멀티 컨테이너 Pod 로그

하나의 Pod에 여러 컨테이너가 있는 경우입니다.

### 컨테이너 목록 확인

```bash
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].name}'
```

**예상 출력:**
```
app sidecar
```

### 특정 컨테이너 로그 확인

```bash
kubectl logs <pod-name> -c <container-name>
```

**예시:**
```bash
kubectl logs my-app-xxx-yyy -c sidecar
```

### 모든 컨테이너 로그 확인

```bash
kubectl logs <pod-name> --all-containers=true
```

**성공 확인:** 각 컨테이너의 로그가 구분되어 출력되면 성공입니다.

---

## 4단계: 여러 Pod 로그 동시 수집

Deployment에 속한 모든 Pod의 로그를 수집하세요.

### 라벨로 여러 Pod 로그 수집

```bash
kubectl logs -l app=<app-name> --all-containers=true
```

{{< callout type="warning" title="제한사항" >}}
라벨 선택자로 로그를 수집할 때는 `-f` (follow) 옵션을 사용할 수 없습니다.
{{< /callout >}}

### stern 도구 사용 (권장)

stern은 여러 Pod의 로그를 실시간으로 수집하는 도구입니다.

**설치:**
{{< tabs "stern-install" >}}
{{< tab "macOS" >}}
```bash
brew install stern
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
# 최신 릴리스 다운로드
curl -Lo stern https://github.com/stern/stern/releases/latest/download/stern_linux_amd64
chmod +x stern
sudo mv stern /usr/local/bin/
```
{{< /tab >}}
{{< /tabs >}}

**사용법:**
```bash
# Pod 이름 패턴으로 로그 수집
stern my-app

# 네임스페이스 지정
stern my-app -n production

# 특정 컨테이너만
stern my-app -c app
```

**예상 출력 (색상 구분됨):**
```
my-app-xxx-yyy app 2024-01-15T10:30:45Z INFO Starting...
my-app-xxx-zzz app 2024-01-15T10:30:46Z INFO Starting...
```

**성공 확인:** 여러 Pod의 로그가 함께 출력되면 성공입니다.

---

## 5단계: 로그 분석 패턴

### 오류 로그 필터링

```bash
kubectl logs <pod-name> | grep -i error
```

또는 ERROR와 WARN 모두 찾으세요.

```bash
kubectl logs <pod-name> | grep -iE "(error|warn)"
```

### 특정 요청 추적

Request ID나 Trace ID로 필터링하세요.

```bash
kubectl logs <pod-name> | grep "request-id-12345"
```

### 시간대별 로그 빈도 분석

분당 오류 발생 횟수를 확인하세요.

```bash
kubectl logs <pod-name> | grep -i error | cut -d'T' -f1-2 | cut -d':' -f1-2 | uniq -c
```

**예상 출력:**
```
   5 2024-01-15T10:30
  12 2024-01-15T10:31
   3 2024-01-15T10:32
```

### JSON 로그 파싱 (jq 사용)

애플리케이션이 JSON 형식으로 로그를 출력하는 경우입니다.

```bash
kubectl logs <pod-name> | jq 'select(.level == "error")'
```

특정 필드만 추출하세요.

```bash
kubectl logs <pod-name> | jq '{time: .timestamp, msg: .message, err: .error}'
```

**성공 확인:** 원하는 패턴의 로그가 필터링되면 성공입니다.

---

## 자주 발생하는 오류

### "error: container not found"

**원인:** 컨테이너 이름이 잘못되었거나 Pod에 해당 컨테이너가 없습니다.

**해결:**
```bash
# Pod의 컨테이너 목록 확인
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].name}'
```

### "error: previous terminated container not found"

**원인:** Pod가 재시작된 적이 없거나 이전 로그가 삭제되었습니다.

**해결:**
```bash
# 재시작 횟수 확인
kubectl describe pod <pod-name> | grep "Restart Count"
```

재시작 횟수가 0이면 `--previous` 옵션을 사용할 수 없습니다.

### 로그가 비어있음

**원인 1:** 애플리케이션이 stdout/stderr로 로그를 출력하지 않음

**해결:**
```bash
# 파일로 로그를 기록하는 경우 컨테이너 내부 확인
kubectl exec <pod-name> -- cat /var/log/app.log
```

**원인 2:** 컨테이너가 방금 시작됨

**해결:**
```bash
# 컨테이너 시작 시간 확인
kubectl describe pod <pod-name> | grep "Started:"
```

### 로그가 잘림 (truncated)

**원인:** 로그 버퍼 제한에 도달했습니다.

**해결:**
```bash
# 제한 없이 모든 로그 가져오기 (주의: 메모리 사용량 증가)
kubectl logs <pod-name> --limit-bytes=0
```

또는 시간 범위를 지정하세요.

```bash
kubectl logs <pod-name> --since=10m
```

---

## 유용한 로그 명령어 모음

### 빠른 디버깅

```bash
# 최근 오류 50줄
kubectl logs <pod-name> --tail=500 | grep -i error | tail -50

# 실시간 오류 모니터링
kubectl logs <pod-name> -f | grep -i error

# 모든 Pod의 최근 오류
kubectl logs -l app=my-app --all-containers --tail=100 | grep -i error
```

### 로그 저장

```bash
# 파일로 저장
kubectl logs <pod-name> > pod-logs.txt

# 타임스탬프와 함께 저장
kubectl logs <pod-name> --timestamps > pod-logs-with-time.txt
```

### 컨테이너 이벤트와 함께 확인

```bash
# Pod 이벤트 (로그 외의 정보)
kubectl describe pod <pod-name> | tail -20
```

---

## 환경별 참고사항

{{< tabs "env-logging" >}}
{{< tab "Minikube" >}}
```bash
# Minikube 노드에서 직접 로그 확인
minikube ssh
sudo journalctl -u kubelet

# 컨테이너 런타임 로그
minikube logs
```
{{< /tab >}}
{{< tab "EKS" >}}
```bash
# CloudWatch Logs Insights 쿼리
# AWS Console에서: CloudWatch > Logs Insights

# 쿼리 예시:
fields @timestamp, @message
| filter @logStream like /my-app/
| filter @message like /error/i
| sort @timestamp desc
| limit 100
```
{{< /tab >}}
{{< tab "GKE" >}}
```bash
# Cloud Logging 쿼리
gcloud logging read 'resource.type="k8s_container" AND resource.labels.pod_name:"my-app"' --limit=50

# 오류만 필터링
gcloud logging read 'resource.type="k8s_container" AND severity>=ERROR' --limit=50
```
{{< /tab >}}
{{< /tabs >}}

---

## 체크리스트

### 기본 로그 수집
- [ ] `kubectl logs <pod-name>`으로 로그 확인 가능한가?
- [ ] 재시작된 경우 `--previous` 옵션으로 이전 로그 확인했는가?
- [ ] 멀티 컨테이너 Pod인 경우 `-c` 옵션으로 컨테이너 지정했는가?

### 고급 분석
- [ ] 오류 패턴을 grep으로 필터링했는가?
- [ ] 시간 범위를 `--since` 옵션으로 지정했는가?
- [ ] 여러 Pod 로그가 필요한 경우 stern을 사용했는가?

### 문제 해결
- [ ] 로그가 비어있으면 stdout/stderr 출력 여부 확인했는가?
- [ ] 컨테이너 이름이 올바른지 확인했는가?

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| Pod 문제 해결 | [Pod 트러블슈팅](pod-troubleshooting/) |
| 네트워크 문제 해결 | [네트워크 트러블슈팅](network-troubleshooting/) |
| 헬스 체크 설정 | [헬스 체크](../concepts/health-checks/) |
