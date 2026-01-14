---
lastmod: "2026-01-11"
title: 환경 설정
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **대상 독자**: 로컬에서 Kubernetes를 실습하고 싶은 개발자
> **선수 지식**: Docker 기본
> **이 문서를 읽으면**: Minikube 또는 Kind로 로컬 Kubernetes 환경을 구성할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- Minikube: 단일 노드, 초보자에게 권장, 다양한 애드온 지원
- Kind: 다중 노드 가능, CI/CD에 적합, 빠른 클러스터 생성
- Docker Desktop: macOS/Windows에서 가장 간단한 설정
{{< /callout >}}

## 로컬 Kubernetes 옵션 비교

| 도구 | 장점 | 단점 | 적합한 경우 |
|------|------|------|------------|
| Minikube | 애드온 풍부, 문서 많음 | 단일 노드만 | 학습, 개발 |
| Kind | 빠름, 다중 노드, CI 친화 | 애드온 적음 | CI/CD, 테스트 |
| Docker Desktop | 설치 쉬움 | 리소스 많이 사용 | 빠른 시작 |
| k3d | 경량, 빠름 | 프로덕션과 차이 | 빠른 테스트 |

## Minikube 설치

### 설치

{{< tabs "minikube-install" >}}
{{< tab "macOS" >}}
```bash
# Homebrew로 설치
brew install minikube

# 버전 확인
minikube version
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
# 바이너리 다운로드
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64

# 설치
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# 버전 확인
minikube version
```
{{< /tab >}}
{{< tab "Windows" >}}
```powershell
# Chocolatey로 설치
choco install minikube

# 또는 수동 설치
# https://minikube.sigs.k8s.io/docs/start/
```
{{< /tab >}}
{{< /tabs >}}

### 클러스터 시작

```bash
# 기본 설정으로 시작
minikube start

# 리소스 지정
minikube start --cpus=4 --memory=8192

# 특정 Kubernetes 버전 사용
minikube start --kubernetes-version=v1.29.0

# 드라이버 지정
minikube start --driver=docker
```

사용 가능한 드라이버는 다음과 같습니다.

| 드라이버 | 플랫폼 | 특징 |
|----------|--------|------|
| docker | 모두 | 권장, 가장 안정적 |
| virtualbox | 모두 | 완전한 VM, 느림 |
| hyperkit | macOS | 가벼움 |
| hyperv | Windows | Windows 내장 |

### 상태 확인

```bash
# 클러스터 상태
minikube status

# Kubernetes 버전
kubectl version

# 노드 정보
kubectl get nodes
```

### 유용한 애드온

```bash
# 애드온 목록
minikube addons list

# 주요 애드온 활성화
minikube addons enable metrics-server  # HPA 사용 시 필수
minikube addons enable ingress         # Ingress 테스트
minikube addons enable dashboard       # 웹 UI
```

### Dashboard 접근

```bash
# Dashboard 열기
minikube dashboard
```

### 클러스터 관리

```bash
# 일시 중지
minikube pause

# 재개
minikube unpause

# 중지 (리소스 유지)
minikube stop

# 삭제 (모든 데이터 삭제)
minikube delete
```

## Kind 설치

Kind(Kubernetes IN Docker)는 Docker 컨테이너를 노드로 사용하는 도구입니다.

### 설치

{{< tabs "kind-install" >}}
{{< tab "macOS" >}}
```bash
brew install kind
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind
```
{{< /tab >}}
{{< tab "Windows" >}}
```powershell
choco install kind
```
{{< /tab >}}
{{< /tabs >}}

### 클러스터 생성

```bash
# 기본 클러스터 (단일 노드)
kind create cluster

# 이름 지정
kind create cluster --name my-cluster

# 다중 노드 클러스터
cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
- role: worker
EOF
```

### 다중 노드 설정 파일

```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
  - containerPort: 443
    hostPort: 443
- role: worker
- role: worker
```

```bash
kind create cluster --config=kind-config.yaml
```

### 클러스터 관리

```bash
# 클러스터 목록
kind get clusters

# 클러스터 삭제
kind delete cluster --name my-cluster

# 이미지 로드 (로컬 이미지를 클러스터에 로드)
kind load docker-image my-app:1.0 --name my-cluster
```

## kubectl 설치

kubectl은 Kubernetes 클러스터와 통신하는 CLI 도구입니다.

{{< tabs "kubectl-install" >}}
{{< tab "macOS" >}}
```bash
brew install kubectl
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install kubectl /usr/local/bin/kubectl
```
{{< /tab >}}
{{< tab "Windows" >}}
```powershell
choco install kubernetes-cli
```
{{< /tab >}}
{{< /tabs >}}

### kubectl 자동 완성 설정

{{< tabs "kubectl-completion" >}}
{{< tab "bash" >}}
```bash
# bash-completion 설치
# macOS
brew install bash-completion

# .bashrc에 추가
echo 'source <(kubectl completion bash)' >> ~/.bashrc
echo 'alias k=kubectl' >> ~/.bashrc
echo 'complete -F __start_kubectl k' >> ~/.bashrc
source ~/.bashrc
```
{{< /tab >}}
{{< tab "zsh" >}}
```bash
# .zshrc에 추가
echo 'source <(kubectl completion zsh)' >> ~/.zshrc
echo 'alias k=kubectl' >> ~/.zshrc
source ~/.zshrc
```
{{< /tab >}}
{{< /tabs >}}

### 유용한 kubectl 별칭

```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
alias k='kubectl'
alias kg='kubectl get'
alias kd='kubectl describe'
alias kl='kubectl logs'
alias kx='kubectl exec -it'
alias ka='kubectl apply -f'
alias kdel='kubectl delete'
```

## 설치 확인

환경이 제대로 설정되었는지 확인합니다.

```bash
# 클러스터 정보
kubectl cluster-info

# 노드 목록
kubectl get nodes

# 시스템 Pod 확인
kubectl get pods -n kube-system

# 테스트 Pod 실행
kubectl run test --image=nginx --rm -it --restart=Never -- curl localhost
```

**예상 출력:**
```
Kubernetes control plane is running at https://127.0.0.1:xxxxx
CoreDNS is running at https://127.0.0.1:xxxxx/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

---

## 다음 단계

환경 설정이 완료되었다면 다음 단계로 진행하세요:

| 목표 | 추천 문서 |
|------|----------|
| Kubernetes 시작하기 | [Quick Start](../quick-start/) |
| 기본 리소스 실습 | [기본 예제](basic/) |
| 실제 앱 배포 | [Spring Boot 배포](spring-boot/) |
