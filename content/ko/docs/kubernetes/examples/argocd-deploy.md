---
lastmod: "2026-04-08"
title: ArgoCD GitOps 배포
description: "ArgoCD를 사용하여 Spring Boot 앱을 GitOps 방식으로 배포합니다."
weight: 7
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **대상 독자**: ArgoCD로 실제 앱을 배포하고 싶은 개발자
> **선수 지식**: ArgoCD 기본 개념, Spring Boot, Docker
> **이 문서를 읽으면**: Spring Boot 앱을 GitOps 방식으로 배포하고 환경별 설정을 관리할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- Kustomize로 base/overlay 구조를 만들어 환경별 설정 분리
- ArgoCD Application을 생성하면 Git 변경을 자동 감지하여 배포
- 이미지 태그 변경 → Git Push → ArgoCD Sync → 무중단 배포
{{< /callout >}}

## 전체 흐름

```mermaid
flowchart LR
    A[개발자] --> B[Git Push]
    B --> C[Git Repository]
    C --> D[ArgoCD 감지]
    D --> E[Sync 실행]
    E --> F[K8s 클러스터]
    F --> G[Spring Boot Pod]
```

*개발자가 Git에 매니페스트를 Push하면 ArgoCD가 변경을 감지하고 자동으로 Kubernetes 클러스터에 배포합니다.*

## 1. Git 저장소 구조 (Kustomize 기반)

GitOps에서는 애플리케이션 소스 코드와 배포 매니페스트를 **별도 저장소**로 관리하는 것이 일반적입니다.

```
my-app-gitops/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml
    │   └── config.yaml
    ├── staging/
    │   ├── kustomization.yaml
    │   └── config.yaml
    └── prod/
        ├── kustomization.yaml
        └── config.yaml
```

- **base/**: 모든 환경에서 공통으로 사용하는 기본 매니페스트
- **overlays/**: 환경별로 다른 설정을 패치(patch)로 관리

## 2. Base 매니페스트 작성

### Deployment

```yaml
# base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
  labels:
    app: my-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: app
        image: my-registry/my-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          periodSeconds: 5
```

### Service

```yaml
# base/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: my-app
spec:
  type: ClusterIP
  selector:
    app: my-app
  ports:
  - port: 80
    targetPort: 8080
```

### kustomization.yaml

```yaml
# base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml

commonLabels:
  managed-by: argocd
```

## 3. 환경별 Overlay

### dev 환경

```yaml
# overlays/dev/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../base

namespace: dev

patches:
  - target:
      kind: Deployment
      name: my-app
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 1
      - op: replace
        path: /spec/template/spec/containers/0/env/0/value
        value: "dev"

configMapGenerator:
  - name: my-app-config
    literals:
      - LOG_LEVEL=DEBUG
      - JAVA_OPTS=-Xms256m -Xmx512m
```

### staging 환경

dev와 구조는 동일하며, replicas와 프로파일만 변경합니다.

```yaml
# overlays/staging/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../base
namespace: staging
patches:
  - target:
      kind: Deployment
      name: my-app
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 2
      - op: replace
        path: /spec/template/spec/containers/0/env/0/value
        value: "staging"
```

### prod 환경

프로덕션은 replicas 3, 리소스 제한을 강화합니다.

```yaml
# overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../base
namespace: prod
patches:
  - target:
      kind: Deployment
      name: my-app
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 3
      - op: replace
        path: /spec/template/spec/containers/0/resources/limits/memory
        value: "2Gi"
      - op: replace
        path: /spec/template/spec/containers/0/resources/limits/cpu
        value: "1000m"
```

### 환경별 설정 비교

| 항목 | dev | staging | prod |
|------|-----|---------|------|
| replicas | 1 | 2 | 3 |
| 프로파일 | dev | staging | prod |
| 로그 레벨 | DEBUG | INFO | WARN |
| 메모리 요청 | 512Mi | 512Mi | 1Gi |
| 메모리 제한 | 1Gi | 1Gi | 2Gi |
| CPU 요청 | 250m | 250m | 500m |
| CPU 제한 | 500m | 500m | 1000m |

## 4. ArgoCD Application 생성

ArgoCD Application은 "어떤 Git 저장소의 어떤 경로를 어떤 클러스터에 배포할지"를 정의합니다. 세 가지 방법으로 생성할 수 있습니다.

### 방법 1: CLI로 생성

```bash
# dev 환경 Application 생성
argocd app create my-app-dev \
  --repo https://github.com/my-org/my-app-gitops.git \
  --path overlays/dev \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace dev \
  --sync-policy automated \
  --auto-prune \
  --self-heal

# staging 환경
argocd app create my-app-staging \
  --repo https://github.com/my-org/my-app-gitops.git \
  --path overlays/staging \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace staging \
  --sync-policy automated \
  --auto-prune \
  --self-heal
```

**주요 옵션 설명:**
- `--sync-policy automated`: Git 변경 시 자동 Sync
- `--auto-prune`: Git에서 삭제한 리소스를 클러스터에서도 삭제
- `--self-heal`: 클러스터에서 수동 변경이 발생하면 Git 상태로 복원

### 방법 2: YAML 매니페스트로 생성

```yaml
# argocd/application-dev.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: my-app-dev
  namespace: argocd
spec:
  project: default

  source:
    repoURL: https://github.com/my-org/my-app-gitops.git
    targetRevision: main
    path: overlays/dev

  destination:
    server: https://kubernetes.default.svc
    namespace: dev

  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
    retry:
      limit: 3
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

```bash
# Application 매니페스트 적용
kubectl apply -f argocd/application-dev.yaml
```

### 방법 3: UI에서 생성

ArgoCD 웹 UI(`https://argocd.example.com`)에서 **+ New App** 클릭 후, Repository URL, Path(`overlays/dev`), Cluster URL, Namespace를 입력하고 **Create**를 클릭합니다.

## 5. 배포 및 확인

### Sync 실행

자동 Sync를 설정하지 않았다면 수동으로 실행합니다.

```bash
# 수동 Sync
argocd app sync my-app-dev

# Sync 결과 대기
argocd app wait my-app-dev
```

### 상태 확인

```bash
# Application 상태 확인
argocd app get my-app-dev
# Sync Status: Synced, Health Status: Healthy 가 나오면 정상

# Kubernetes에서 직접 확인
kubectl get all -n dev -l app=my-app

# Spring Boot Actuator 헬스 확인
kubectl port-forward -n dev svc/my-app 8080:80
curl http://localhost:8080/actuator/health
```

## 6. 업데이트 워크플로우

GitOps에서 배포 업데이트는 Git 커밋으로 수행합니다.

### 이미지 태그 변경

```bash
# GitOps 저장소에서 Kustomize로 이미지 태그 변경
cd my-app-gitops/overlays/dev
kustomize edit set image my-registry/my-app:v2.0

# Git Push → ArgoCD 자동 배포
git add .
git commit -m "chore: update my-app image to v2.0"
git push
```

자동 Sync가 설정되어 있다면 ArgoCD가 변경을 감지하고 약 3분 이내에 자동 배포합니다.

```mermaid
flowchart LR
    A[이미지 태그 변경] --> B[Git Commit & Push]
    B --> C[ArgoCD 감지<br>약 3분 이내]
    C --> D[자동 Sync]
    D --> E[Rolling Update]
    E --> F[배포 완료]
```

*이미지 태그를 변경하고 Git에 Push하면, ArgoCD가 변경을 감지하여 자동으로 Rolling Update를 수행합니다.*

### 롤백

문제가 발생하면 두 가지 방법으로 롤백할 수 있습니다.

**방법 1: Git Revert (권장)**

GitOps 원칙에 따라 Git을 통해 롤백합니다.

```bash
# 마지막 커밋 되돌리기
git revert HEAD
git push

# ArgoCD가 자동으로 이전 상태로 배포
```

**방법 2: ArgoCD CLI 롤백**

빠른 대응이 필요할 때 사용합니다.

```bash
argocd app history my-app-dev     # 배포 히스토리 확인
argocd app rollback my-app-dev 1  # 특정 버전으로 롤백
```

{{< callout type="warning" title="주의" >}}
ArgoCD CLI 롤백은 일시적입니다. 자동 Sync가 켜져 있으면 다시 최신 Git 상태로 되돌아갑니다. 영구적인 롤백은 반드시 Git Revert를 사용하세요.
{{< /callout >}}

## 7. prod 배포 전략

프로덕션 환경은 자동 Sync 대신 **수동 Sync**를 권장합니다. Application YAML에서 `syncPolicy.automated`를 제거하면 수동 모드가 됩니다.

```bash
# prod 배포는 수동으로 실행
argocd app sync my-app-prod

# 배포 상태 확인
argocd app wait my-app-prod
```

## 8. 정리

실습이 끝났다면 생성한 리소스를 삭제합니다.

```bash
# ArgoCD Application 삭제 (K8s 리소스도 함께 삭제)
argocd app delete my-app-dev --cascade
argocd app delete my-app-staging --cascade
argocd app delete my-app-prod --cascade

# Namespace 삭제 (리소스가 남아 있는 경우)
kubectl delete namespace dev staging prod

# Application 매니페스트로 생성한 경우
kubectl delete -f argocd/application-dev.yaml
```

`--cascade` 옵션은 Application이 관리하는 모든 Kubernetes 리소스를 함께 삭제합니다.

---

## 다음 단계

ArgoCD GitOps 배포를 완료했다면 다음 단계로 진행하세요:

| 목표 | 추천 문서 |
|------|----------|
| K8s 기본 배포 | [Spring Boot 배포](../spring-boot/) |
| 자동 스케일링 | [스케일링](../concepts/scaling/) |
| 네트워크 정책 | [Network Policy](../concepts/network-policy/) |
