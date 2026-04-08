---
lastmod: "2026-04-08"
title: ArgoCD 설치 및 설정
description: "ArgoCD를 설치하고 프로덕션 환경에 맞게 설정하는 단계별 가이드입니다."
weight: 5
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **대상 독자**: K8s 클러스터에 ArgoCD를 도입하려는 DevOps 엔지니어
> **선수 지식**: kubectl, Helm
> **소요 시간**: 약 40분

{{< callout type="tip" title="TL;DR" >}}
ArgoCD는 Kubernetes 네이티브 GitOps CD 도구입니다. `kubectl apply`로 빠르게 설치하고, Helm Chart로 프로덕션 수준의 커스터마이징을 적용할 수 있습니다. 설치 후 초기 비밀번호 변경, Ingress 설정, RBAC 구성, SSO 연동, Repository 연결까지 완료하면 GitOps 워크플로를 바로 시작할 수 있습니다.
{{< /callout >}}

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: ArgoCD 설치, 초기 접속, Ingress, RBAC, SSO, Repository 연결, 트러블슈팅

**다루지 않는 내용**: Application 배포 전략, ApplicationSet 패턴, GitOps 워크플로 설계
{{< /callout >}}

---

## 설치 방법 비교

환경과 요구사항에 따라 적합한 설치 방법을 선택하세요.

| 방법 | 장점 | 적합한 환경 |
|------|------|-------------|
| **kubectl apply** | 가장 간단, 공식 매니페스트 그대로 사용 | 개발/테스트, 빠른 PoC |
| **Helm Chart** | values.yaml로 세밀한 커스터마이징 | 프로덕션, 팀 표준화 |
| **Kustomize** | 환경별 오버레이로 차이점만 관리 | 멀티 클러스터, GitOps 관리 |

{{< callout type="info" >}}
이 가이드에서는 **kubectl apply**(빠른 시작)와 **Helm Chart**(프로덕션)를 모두 다룹니다.
{{< /callout >}}

---

## 시작하기 전에

### 1. kubectl 설치 및 클러스터 연결 확인

```bash
kubectl version --client
kubectl cluster-info
```

**성공 시 출력:**
```
Client Version: v1.28.0
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 2. Helm 설치 확인 (Helm 설치 시)

```bash
helm version
```

**성공 시 출력:**
```
version.BuildInfo{Version:"v3.14.0", ...}
```

### 3. 클러스터 리소스 여유 확인

ArgoCD는 최소 **2 CPU, 4GB 메모리**가 필요합니다.

```bash
kubectl top nodes
```

---

## 1단계: 기본 설치 (kubectl apply)

가장 빠르게 ArgoCD를 설치하는 방법입니다.

### Namespace 생성 및 매니페스트 적용

```bash
# Namespace 생성
kubectl create namespace argocd

# ArgoCD 설치 (안정 버전)
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### 설치 확인

모든 Pod가 Running 상태가 될 때까지 기다립니다.

```bash
kubectl get pods -n argocd -w
```

모든 Pod가 `1/1 Running` 상태가 되면 성공입니다. 총 6개 컴포넌트가 실행됩니다.

| 컴포넌트 | 역할 |
|----------|------|
| **argocd-server** | API 서버 및 Web UI |
| **argocd-application-controller** | Git-클러스터 상태 비교 및 동기화 |
| **argocd-repo-server** | Git 저장소 클론 및 매니페스트 렌더링 |
| **argocd-dex-server** | SSO 인증 |
| **argocd-redis** | 캐시 |
| **argocd-notifications-controller** | 알림 |

---

## 2단계: Helm 설치 (프로덕션 권장)

프로덕션 환경에서는 Helm Chart로 설치하면 values.yaml을 통해 체계적으로 설정을 관리할 수 있습니다.

### Helm Repository 추가 및 설치

```bash
# Helm 저장소 추가
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# ArgoCD 설치
helm install argocd argo/argo-cd \
  -n argocd \
  --create-namespace \
  --version 7.7.7
```

### 프로덕션 values.yaml 예시

```yaml
# values-production.yaml
server:
  replicas: 2
  resources:
    requests: { cpu: 250m, memory: 256Mi }
    limits:   { cpu: 500m, memory: 512Mi }

controller:
  resources:
    requests: { cpu: 500m, memory: 512Mi }
    limits:   { cpu: "1", memory: 1Gi }

repoServer:
  replicas: 2
  resources:
    requests: { cpu: 250m, memory: 256Mi }
    limits:   { cpu: 500m, memory: 512Mi }
```

```bash
helm install argocd argo/argo-cd \
  -n argocd --create-namespace -f values-production.yaml
```

---

## 3단계: 초기 접속 설정

### admin 초기 비밀번호 확인

ArgoCD는 설치 시 `admin` 계정의 초기 비밀번호를 Secret으로 생성합니다.

```bash
# 초기 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d; echo
```

### argocd CLI 설치

{{< tabs "cli-install" >}}
{{< tab "macOS" >}}
```bash
brew install argocd
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
curl -sSL -o argocd-linux-amd64 \
  https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
sudo install -m 555 argocd-linux-amd64 /usr/local/bin/argocd
rm argocd-linux-amd64
```
{{< /tab >}}
{{< /tabs >}}

### Port-Forward로 접속

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

브라우저에서 `https://localhost:8080`으로 접속합니다. 자체 서명 인증서 경고가 나타나면 무시하고 진행하세요.

### CLI 로그인 및 비밀번호 변경

```bash
# CLI 로그인
argocd login localhost:8080 --insecure

# 비밀번호 변경 (반드시 변경하세요)
argocd account update-password

# 초기 비밀번호 Secret 삭제
kubectl -n argocd delete secret argocd-initial-admin-secret
```

{{< callout type="warning" title="보안 주의" >}}
초기 비밀번호는 반드시 변경하고, `argocd-initial-admin-secret` Secret을 삭제하세요. 프로덕션 환경에서는 SSO를 설정한 후 admin 계정을 비활성화하는 것을 권장합니다.
{{< /callout >}}

---

## 4단계: Ingress 설정

외부에서 ArgoCD Web UI에 접근하려면 Ingress를 설정합니다.

### NGINX Ingress + TLS 설정

```yaml
# argocd-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd-server-ingress
  namespace: argocd
  annotations:
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/ssl-passthrough: "true"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
spec:
  ingressClassName: nginx
  rules:
    - host: argocd.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: argocd-server
                port:
                  number: 443
  tls:
    - hosts:
        - argocd.example.com
      secretName: argocd-tls
```

```bash
kubectl apply -f argocd-ingress.yaml
```

cert-manager를 사용한다면 annotations에 `cert-manager.io/cluster-issuer: letsencrypt-prod`를 추가하여 자동 인증서 발급을 설정할 수 있습니다.

argocd CLI 접속 시에는 `--grpc-web` 플래그를 사용하면 별도 gRPC Ingress 없이 동일한 엔드포인트로 접속 가능합니다.

```bash
argocd login argocd.example.com --grpc-web
```

---

## 5단계: RBAC 설정

ArgoCD의 RBAC은 ConfigMap으로 관리됩니다.

### 기본 RBAC 정책

```yaml
# argocd-rbac-cm.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-rbac-cm
  namespace: argocd
data:
  # 기본 정책: 읽기 전용
  policy.default: role:readonly

  policy.csv: |
    # 개발팀: dev 프로젝트 조회 + 동기화
    p, role:dev-team, applications, get, dev/*, allow
    p, role:dev-team, applications, sync, dev/*, allow
    p, role:dev-team, logs, get, dev/*, allow

    # 운영팀: 스테이징 전체 + 프로덕션 조회/동기화
    p, role:ops-team, applications, *, staging/*, allow
    p, role:ops-team, applications, get, production/*, allow
    p, role:ops-team, applications, sync, production/*, allow

    # SSO 그룹 → ArgoCD 역할 매핑
    g, devops-admins, role:admin
    g, developers, role:dev-team
    g, operations, role:ops-team
```

```bash
kubectl apply -f argocd-rbac-cm.yaml
```

### AppProject로 팀별 접근 제어

```yaml
# dev-project.yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: dev
  namespace: argocd
spec:
  description: "개발팀 프로젝트"
  # 허용할 소스 저장소
  sourceRepos:
    - "https://github.com/my-org/dev-*"
  # 배포 가능한 대상 클러스터/네임스페이스
  destinations:
    - namespace: "dev-*"
      server: https://kubernetes.default.svc
  # 허용 리소스 (필요에 따라 추가)
  namespaceResourceWhitelist:
    - group: "apps"
      kind: Deployment
    - group: ""
      kind: Service
    - group: ""
      kind: ConfigMap
```

```bash
kubectl apply -f dev-project.yaml
```

---

## 6단계: SSO 연동

프로덕션 환경에서는 SSO를 설정하여 개별 계정 관리 부담을 줄이세요.

### GitHub OAuth 설정

**GitHub에서 OAuth App 생성:**

1. GitHub Settings > Developer settings > OAuth Apps > New OAuth App
2. **Homepage URL**: `https://argocd.example.com`
3. **Authorization callback URL**: `https://argocd.example.com/api/dex/callback`
4. Client ID와 Client Secret을 기록합니다

**ArgoCD ConfigMap 설정** (`argocd-cm`):

```yaml
# argocd-cm의 data 섹션
data:
  url: https://argocd.example.com
  dex.config: |
    connectors:
      - type: github
        id: github
        name: GitHub
        config:
          clientID: $dex.github.clientID
          clientSecret: $dex.github.clientSecret
          orgs:
            - name: my-org
```

```bash
# Secret에 자격 증명 저장
kubectl -n argocd create secret generic argocd-github-oauth \
  --from-literal=dex.github.clientID=<YOUR_CLIENT_ID> \
  --from-literal=dex.github.clientSecret=<YOUR_CLIENT_SECRET>
```

### OIDC 설정 (Keycloak 등)

OIDC를 사용하려면 `argocd-cm`의 `dex.config` 대신 `oidc.config`를 설정합니다.

```yaml
# argocd-cm의 data 섹션에 추가
  oidc.config: |
    name: Keycloak
    issuer: https://keycloak.example.com/realms/my-realm
    clientID: argocd
    clientSecret: $oidc.keycloak.clientSecret
    requestedScopes: ["openid", "profile", "email", "groups"]
```

{{< callout type="info" >}}
SSO 설정 후 RBAC의 그룹 매핑(`g, <sso-group>, role:<argocd-role>`)을 반드시 업데이트하세요.
{{< /callout >}}

---

## 7단계: Repository 연결

ArgoCD가 Git 저장소에서 매니페스트를 가져오려면 Repository를 등록해야 합니다.

### HTTPS 방식

```bash
argocd repo add https://github.com/my-org/my-repo.git \
  --username <USERNAME> \
  --password <TOKEN>
```

### SSH Key 방식

```bash
# SSH 키 생성 (이미 있다면 건너뛰세요)
ssh-keygen -t ed25519 -f ~/.ssh/argocd -N ""

# ArgoCD에 SSH 키 등록
argocd repo add git@github.com:my-org/my-repo.git \
  --ssh-private-key-path ~/.ssh/argocd
```

### GitHub App 방식 (권장)

GitHub App은 세밀한 권한 제어와 높은 API Rate Limit을 제공합니다.

1. GitHub Settings > Developer settings > GitHub Apps > New GitHub App
2. **Permissions**: Repository contents (Read-only)
3. Private Key를 다운로드합니다

```bash
argocd repo add https://github.com/my-org/my-repo.git \
  --github-app-id <APP_ID> \
  --github-app-installation-id <INSTALLATION_ID> \
  --github-app-private-key-path /path/to/private-key.pem
```

### 선언적 방식 (Secret으로 관리)

GitOps답게 Repository 연결도 Secret 리소스로 선언적 관리가 가능합니다. `argocd.argoproj.io/secret-type: repository` 라벨을 붙인 Secret을 생성하면 ArgoCD가 자동으로 인식합니다.

---

## 8단계: 설치 검증

모든 설정이 완료되면 간단한 Application을 배포하여 검증합니다.

### 테스트 Application 생성

```bash
argocd app create guestbook \
  --repo https://github.com/argoproj/argocd-example-apps.git \
  --path guestbook \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace default
```

### 동기화 및 상태 확인

```bash
# Application 동기화
argocd app sync guestbook

# 상태 확인
argocd app get guestbook
```

`Health Status: Healthy`, `Sync Status: Synced`가 표시되면 정상입니다.

### 테스트 Application 정리

```bash
argocd app delete guestbook --cascade
```

---

## 트러블슈팅

### Pod가 시작되지 않는 경우

```bash
# Pod 이벤트 확인
kubectl describe pod -n argocd -l app.kubernetes.io/name=argocd-server

# 로그 확인
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-server --tail=50
```

**흔한 원인:**

| 증상 | 원인 | 해결 |
|------|------|------|
| `ImagePullBackOff` | 이미지 다운로드 실패 | 네트워크/레지스트리 접근 확인 |
| `CrashLoopBackOff` | 컨테이너 시작 실패 | 로그에서 에러 메시지 확인 |
| `Pending` | 리소스 부족 | 노드 리소스 확보 또는 requests 축소 |
| `OOMKilled` | 메모리 초과 | limits 값 증가 |

### Ingress 접속 불가

- `kubectl get ingress -n argocd`로 상태 확인
- DNS 설정, `backend-protocol: "HTTPS"` 어노테이션, TLS Secret 네임스페이스 확인
- `curl -vk https://argocd.example.com`으로 SSL 연결 테스트

### SSO 로그인 실패

- Dex 로그 확인: `kubectl logs -n argocd -l app.kubernetes.io/name=argocd-dex-server --tail=50`
- Callback URL이 `/api/dex/callback`으로 정확한지 확인
- `argocd-cm`의 `url` 필드가 실제 접속 URL과 일치하는지 확인

### Repository 연결 실패

- `argocd repo list`로 등록 상태 확인
- HTTPS: Token 유효 기간 확인
- SSH: `ssh-keyscan github.com`으로 known hosts 등록 여부 확인

---

## 다음 단계

ArgoCD 설치와 기본 설정이 완료되었습니다. 다음 주제를 살펴보세요.

- **Application 배포**: Application 리소스를 정의하고 GitOps 워크플로로 배포하기
- **ApplicationSet**: 멀티 클러스터/멀티 테넌트 환경에서 Application을 자동 생성하기
- **Notifications**: Slack, Email 등으로 배포 알림 설정하기
- **Image Updater**: 컨테이너 이미지 태그 자동 업데이트 설정하기
