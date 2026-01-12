---
lastmod: "2026-01-11"
title: 용어 사전
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Kubernetes 학습과 운영에 필요한 핵심 용어를 정리합니다.

## A-C

### Cluster
여러 노드(머신)로 구성된 Kubernetes 환경 전체를 의미합니다. 하나의 Control Plane과 여러 Worker Node로 구성됩니다.

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

## D-H

### DaemonSet
모든 노드(또는 특정 노드)에 Pod를 하나씩 실행하는 워크로드입니다. 로그 수집, 모니터링 에이전트에 주로 사용됩니다.

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

## I-L

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

## M-P

### Namespace
클러스터를 논리적으로 분리하는 단위입니다. 환경별(dev, staging, prod) 또는 팀별로 분리할 수 있습니다.

### Node
Kubernetes 클러스터를 구성하는 머신(물리 또는 가상)입니다. Worker Node에서 실제 워크로드가 실행됩니다.

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

### ReplicaSet
지정된 수의 Pod 복제본을 유지합니다. 보통 Deployment를 통해 간접적으로 관리합니다.

### Requests
Pod가 최소한 보장받아야 하는 리소스 양입니다. 스케줄링의 기준이 됩니다.

### Limits
Pod가 사용할 수 있는 최대 리소스 양입니다. 초과 시 CPU는 스로틀링, 메모리는 OOMKilled됩니다.

### Secret
비밀번호, API 키 등 민감한 정보를 저장하는 리소스입니다. Base64 인코딩되며, 암호화는 별도 설정이 필요합니다.

### Service
Pod 집합에 대한 안정적인 네트워크 엔드포인트를 제공합니다. ClusterIP, NodePort, LoadBalancer 유형이 있습니다.

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
