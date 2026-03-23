---
lastmod: "2026-01-15"
title: 용어 사전
description: "Kubernetes 핵심 용어를 정리합니다."
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Kubernetes 학습과 운영에 필요한 핵심 용어를 정리합니다.

## A-C

### Access Modes
PersistentVolume의 접근 방식입니다. RWO(ReadWriteOnce: 단일 노드 읽기/쓰기), ROX(ReadOnlyMany: 여러 노드 읽기), RWX(ReadWriteMany: 여러 노드 읽기/쓰기)가 있습니다.

### API Server (kube-apiserver)
Kubernetes의 프론트엔드입니다. 모든 kubectl 명령, 대시보드, CI/CD 도구가 API Server를 통해 클러스터와 통신합니다. 인증, 권한 검사, 요청 검증을 수행하고 etcd와 통신합니다.

### Cluster
여러 노드(머신)로 구성된 Kubernetes 환경 전체를 의미합니다. 하나의 Control Plane과 여러 Worker Node로 구성됩니다.

### ClusterIP
Service의 기본 유형입니다. 클러스터 내부에서만 접근 가능한 가상 IP를 할당받습니다. 마이크로서비스 간 내부 통신에 사용됩니다.

### ConfigMap
설정 데이터를 키-값 쌍으로 저장하는 리소스입니다. 환경 변수나 설정 파일로 Pod에 주입할 수 있습니다.

### Container
애플리케이션과 그 의존성을 패키징한 격리된 실행 환경입니다. Docker 컨테이너가 가장 대표적입니다.

### Container Runtime
컨테이너를 실행하는 소프트웨어입니다. containerd, CRI-O 등이 있습니다. Kubernetes 1.24부터 Docker를 직접 지원하지 않습니다.

### Control Plane
클러스터의 상태를 관리하는 구성요소들입니다. API Server, etcd, Scheduler, Controller Manager로 구성됩니다.

### CronJob
지정된 일정에 따라 반복적으로 실행되는 Job입니다. 리눅스의 cron과 유사한 스케줄 문법을 사용합니다.

### Controller Manager (kube-controller-manager)
여러 컨트롤러를 실행하는 Control Plane 컴포넌트입니다. Deployment Controller, ReplicaSet Controller, Node Controller 등이 포함됩니다. 각 컨트롤러는 조정 루프(Reconciliation Loop)를 통해 현재 상태를 원하는 상태로 맞춥니다.

## D-H

### DaemonSet
모든 노드(또는 특정 노드)에 Pod를 하나씩 실행하는 워크로드입니다. 로그 수집, 모니터링 에이전트에 주로 사용됩니다.

### emptyDir
Pod가 시작될 때 빈 디렉토리를 생성하는 Volume 유형입니다. Pod 종료 시 삭제됩니다. 컨테이너 간 데이터 공유, 임시 캐시에 사용됩니다.

### Endpoints
Service가 관리하는 Pod IP 목록입니다. selector에 매칭되는 Ready 상태의 Pod들이 자동으로 등록됩니다. `kubectl get endpoints`로 확인할 수 있습니다.

### ExternalName
외부 DNS 이름을 Service로 매핑하는 Service 유형입니다. 클러스터 내부에서 외부 서비스를 Service DNS로 접근할 수 있게 합니다.

### Deployment
Pod의 선언적 업데이트를 관리하는 워크로드입니다. ReplicaSet을 생성하고 롤링 업데이트, 롤백을 지원합니다.

```yaml
# 예시: 3개의 nginx Pod를 관리하는 Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.25
```

### etcd
Kubernetes 클러스터의 모든 상태를 저장하는 분산 키-값 저장소입니다. 고가용성을 위해 홀수 개로 클러스터링합니다.

### HPA (Horizontal Pod Autoscaler)
CPU, 메모리 등 메트릭을 기반으로 Pod 수를 자동으로 조절하는 리소스입니다.

### Helm
Kubernetes 애플리케이션 패키지 관리자입니다. Chart라는 패키지 형식을 사용합니다.

### hostPath
노드의 파일시스템 경로를 Pod에 마운트하는 Volume 유형입니다. 개발/테스트 용도로만 사용하며, 프로덕션에서는 권장되지 않습니다.

## I-L

### Init Container
메인 컨테이너 실행 전에 초기화 작업을 수행하는 컨테이너입니다. 순차 실행되며 모든 Init Container가 성공해야 메인 컨테이너가 시작됩니다. 의존 서비스 대기, 설정 파일 생성 등에 사용됩니다.

### Ingress
클러스터 외부에서 내부 Service로 HTTP/HTTPS 트래픽을 라우팅하는 규칙을 정의합니다. 도메인, 경로 기반 라우팅을 지원합니다.

### Ingress Controller
Ingress 리소스를 실제로 구현하는 컴포넌트입니다. NGINX, Traefik 등이 있습니다.

### Job
일회성 작업을 실행하는 워크로드입니다. 완료까지 Pod를 관리하며, 실패 시 재시도합니다.

### Kubelet
각 Worker Node에서 실행되는 에이전트입니다. Pod 실행, 상태 보고, 헬스 체크를 담당합니다.

### kubectl
Kubernetes 클러스터와 통신하는 CLI 도구입니다. 대부분의 작업은 kubectl을 통해 수행합니다.

### kube-proxy
각 노드에서 네트워크 규칙을 관리합니다. Service로 들어오는 트래픽을 Pod로 전달합니다.

### LimitRange
네임스페이스 수준에서 기본 리소스 제한을 설정하는 리소스입니다. 리소스를 명시하지 않은 Pod에 기본 requests/limits를 적용합니다.

### Liveness Probe
컨테이너가 살아있는지 확인하는 헬스 체크입니다. 실패하면 kubelet이 컨테이너를 재시작합니다. 데드락 같은 상황을 감지합니다.

### LoadBalancer
클라우드 제공자의 로드밸런서를 프로비저닝하는 Service 유형입니다. 고정 외부 IP로 프로덕션 서비스를 노출할 때 사용합니다.

## M-P

### maxSurge
Rolling Update 시 원하는 Pod 수 대비 추가로 허용되는 Pod 수입니다. 예: replicas=3, maxSurge=1이면 최대 4개 Pod가 동시에 실행될 수 있습니다.

### maxUnavailable
Rolling Update 시 동시에 중단 가능한 Pod 수입니다. 예: replicas=3, maxUnavailable=0이면 항상 3개 이상의 Pod가 실행됩니다.

### Metrics Server
클러스터의 리소스 사용량(CPU, 메모리)을 수집하는 컴포넌트입니다. HPA가 스케일링 결정을 내리는 데 필요합니다. `kubectl top` 명령에도 사용됩니다.

### Namespace
클러스터를 논리적으로 분리하는 단위입니다. 환경별(dev, staging, prod) 또는 팀별로 분리할 수 있습니다.

### NetworkPolicy
Pod 간 트래픽을 제어하는 리소스입니다. 기본적으로 모든 Pod는 서로 통신할 수 있지만, NetworkPolicy로 특정 Pod/네임스페이스만 허용할 수 있습니다.

### Node
Kubernetes 클러스터를 구성하는 머신(물리 또는 가상)입니다. Worker Node에서 실제 워크로드가 실행됩니다.

### NodePort
모든 노드의 특정 포트(30000-32767)로 외부 접근을 허용하는 Service 유형입니다. 개발/테스트 환경에서 간단히 외부 노출할 때 사용합니다.

### OOMKilled
컨테이너가 메모리 limits를 초과하여 커널에 의해 종료된 상태입니다. `kubectl describe pod`에서 Reason: OOMKilled로 확인할 수 있습니다.

### PersistentVolume (PV)
관리자가 프로비저닝한 스토리지 리소스입니다. Pod와 독립적인 생명주기를 가집니다.

### PersistentVolumeClaim (PVC)
사용자가 요청하는 스토리지입니다. PV에 바인딩되어 Pod에서 사용합니다.

### Pod
Kubernetes의 최소 배포 단위입니다. 하나 이상의 컨테이너를 포함하며, 네트워크와 스토리지를 공유합니다.

```yaml
# 예시: nginx Pod
apiVersion: v1
kind: Pod
metadata:
  name: nginx
spec:
  containers:
  - name: nginx
    image: nginx:1.25
```

### Probe
컨테이너 상태를 확인하는 메커니즘입니다. Liveness, Readiness, Startup Probe가 있습니다.

## Q-S

### QoS Class
Pod의 서비스 품질 등급입니다. Guaranteed, Burstable, BestEffort 세 가지가 있습니다.

### Readiness Probe
컨테이너가 트래픽을 받을 준비가 되었는지 확인하는 헬스 체크입니다. 실패하면 Service의 Endpoints에서 제외됩니다. DB 연결 확인 등에 사용됩니다.

### Reclaim Policy
PVC 삭제 시 PV의 동작을 정의합니다. Retain(데이터 유지), Delete(삭제), Recycle(삭제 후 재사용, deprecated)이 있습니다.

### Reconciliation Loop (조정 루프)
Controller Manager의 동작 원리입니다. 현재 상태와 원하는 상태를 비교하고, 일치하지 않으면 상태를 조정합니다.

### ReplicaSet
지정된 수의 Pod 복제본을 유지합니다. 보통 Deployment를 통해 간접적으로 관리합니다.

### Requests
Pod가 최소한 보장받아야 하는 리소스 양입니다. 스케줄링의 기준이 됩니다.

### Limits
Pod가 사용할 수 있는 최대 리소스 양입니다. 초과 시 CPU는 스로틀링, 메모리는 OOMKilled됩니다.

### ResourceQuota
네임스페이스 전체의 리소스 총량을 제한하는 리소스입니다. CPU/메모리 requests/limits 합계, Pod 수 등을 제한할 수 있습니다.

### Rollback
문제 발생 시 이전 버전의 Deployment로 되돌리는 것입니다. `kubectl rollout undo deployment/<name>`으로 실행합니다.

### Rolling Update
Deployment의 기본 업데이트 전략입니다. 새 Pod를 점진적으로 생성하고 이전 Pod를 종료하여 무중단 배포를 실현합니다.

### Scheduler (kube-scheduler)
새로 생성된 Pod를 어느 노드에서 실행할지 결정하는 Control Plane 컴포넌트입니다. 리소스 요구사항, 노드 셀렉터, Taint/Toleration, Affinity 등을 고려합니다.

### Secret
비밀번호, API 키 등 민감한 정보를 저장하는 리소스입니다. Base64 인코딩되며, 암호화는 별도 설정이 필요합니다.

### Service
Pod 집합에 대한 안정적인 네트워크 엔드포인트를 제공합니다. ClusterIP, NodePort, LoadBalancer 유형이 있습니다.

### Session Affinity
같은 클라이언트의 요청을 같은 Pod로 보내는 Service 설정입니다. ClientIP 기반으로 설정할 수 있으며, 쿠키 기반은 Ingress Controller에서 지원합니다.

### Sidecar
메인 컨테이너를 보조하는 컨테이너 패턴입니다. 로그 수집(Fluentd), 프록시(Envoy), 설정 동기화 등에 사용됩니다. 같은 Pod에서 네트워크와 볼륨을 공유합니다.

### Startup Probe
애플리케이션 시작 완료 여부를 확인하는 헬스 체크입니다. 성공할 때까지 Liveness/Readiness Probe가 비활성화됩니다. 시작이 오래 걸리는 애플리케이션에 사용합니다.

```yaml
# 예시: nginx Pod를 노출하는 Service
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  ports:
  - port: 80
    targetPort: 80
  type: ClusterIP
```

### StatefulSet
상태가 있는 애플리케이션(DB 등)을 위한 워크로드입니다. 순차 배포, 안정적인 네트워크 ID, 영구 스토리지를 제공합니다.

### StorageClass
동적 PV 프로비저닝을 위한 스토리지 유형 정의입니다. 클라우드별로 다른 provisioner를 사용합니다.

## T-Z

### Taint
특정 노드에 Pod가 스케줄되지 않도록 하는 속성입니다. Toleration이 있는 Pod만 해당 노드에 스케줄됩니다.

### Throttling
CPU 사용량이 limits를 초과할 때 발생하는 제한입니다. 컨테이너가 종료되지 않고 성능만 저하됩니다. 메모리와 달리 CPU는 압축 가능한(compressible) 리소스입니다.

### Toleration
Taint가 있는 노드에도 스케줄될 수 있게 하는 Pod의 속성입니다.

### VPA (Vertical Pod Autoscaler)
Pod의 리소스 requests/limits를 자동으로 조절합니다. 기본 설치되어 있지 않습니다.

### Volume
Pod에 마운트되는 스토리지입니다. emptyDir, hostPath, PVC 등 여러 유형이 있습니다.

### Worker Node
실제 워크로드(Pod)가 실행되는 노드입니다. Kubelet, kube-proxy, Container Runtime이 설치됩니다.

### YAML
Kubernetes 리소스를 정의하는 주요 형식입니다. JSON도 사용 가능하지만 YAML이 가독성이 좋아 더 많이 사용됩니다.
