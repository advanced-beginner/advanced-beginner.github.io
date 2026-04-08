---
lastmod: "2026-04-08"
title: ArgoCD Installation and Setup
description: "Step-by-step guide for installing ArgoCD and configuring it for production environments."
weight: 5
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Target Audience**: DevOps engineers looking to introduce ArgoCD to a K8s cluster
> **Prerequisites**: kubectl, Helm
> **Estimated Time**: ~40 minutes

{{< callout type="tip" title="TL;DR" >}}
ArgoCD is a Kubernetes-native GitOps CD tool. You can quickly install it with `kubectl apply` or apply production-grade customizations via Helm Chart. After installation, complete the initial password change, Ingress setup, RBAC configuration, SSO integration, and Repository connection to start your GitOps workflow right away.
{{< /callout >}}

{{< callout type="info" title="Scope of This Guide" >}}
**Covered**: ArgoCD installation, initial access, Ingress, RBAC, SSO, Repository connection, troubleshooting

**Not covered**: Application deployment strategies, ApplicationSet patterns, GitOps workflow design
{{< /callout >}}

---

## Installation Method Comparison

Choose the appropriate installation method based on your environment and requirements.

| Method | Advantages | Suitable Environment |
|--------|-----------|---------------------|
| **kubectl apply** | Simplest, uses official manifests as-is | Development/test, quick PoC |
| **Helm Chart** | Fine-grained customization via values.yaml | Production, team standardization |
| **Kustomize** | Manage differences per environment with overlays | Multi-cluster, GitOps-managed |

{{< callout type="info" >}}
This guide covers both **kubectl apply** (quick start) and **Helm Chart** (production).
{{< /callout >}}

---

## Before You Begin

### 1. Verify kubectl Installation and Cluster Connection

```bash
kubectl version --client
kubectl cluster-info
```

**Expected output on success:**
```
Client Version: v1.28.0
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 2. Verify Helm Installation (for Helm install)

```bash
helm version
```

**Expected output on success:**
```
version.BuildInfo{Version:"v3.14.0", ...}
```

### 3. Verify Cluster Resource Availability

ArgoCD requires a minimum of **2 CPU, 4GB memory**.

```bash
kubectl top nodes
```

---

## Step 1: Basic Installation (kubectl apply)

The quickest way to install ArgoCD.

### Create Namespace and Apply Manifests

```bash
# Create namespace
kubectl create namespace argocd

# Install ArgoCD (stable version)
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Verify Installation

Wait until all Pods reach Running status.

```bash
kubectl get pods -n argocd -w
```

Success is when all Pods show `1/1 Running`. A total of 6 components will be running.

| Component | Role |
|-----------|------|
| **argocd-server** | API server and Web UI |
| **argocd-application-controller** | Git-cluster state comparison and synchronization |
| **argocd-repo-server** | Git repository cloning and manifest rendering |
| **argocd-dex-server** | SSO authentication |
| **argocd-redis** | Cache |
| **argocd-notifications-controller** | Notifications |

---

## Step 2: Helm Installation (Recommended for Production)

In production environments, installing via Helm Chart allows systematic configuration management through values.yaml.

### Add Helm Repository and Install

```bash
# Add Helm repository
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# Install ArgoCD
helm install argocd argo/argo-cd \
  -n argocd \
  --create-namespace \
  --version 7.7.7
```

### Production values.yaml Example

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

## Step 3: Initial Access Setup

### Retrieve admin Initial Password

ArgoCD generates an initial password for the `admin` account as a Secret during installation.

```bash
# Retrieve initial password
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d; echo
```

### Install argocd CLI

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

### Access via Port-Forward

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Access `https://localhost:8080` in your browser. If a self-signed certificate warning appears, proceed past it.

### CLI Login and Password Change

```bash
# CLI login
argocd login localhost:8080 --insecure

# Change password (mandatory)
argocd account update-password

# Delete initial password Secret
kubectl -n argocd delete secret argocd-initial-admin-secret
```

{{< callout type="warning" title="Security Notice" >}}
Always change the initial password and delete the `argocd-initial-admin-secret` Secret. For production environments, it's recommended to disable the admin account after setting up SSO.
{{< /callout >}}

---

## Step 4: Ingress Setup

To access the ArgoCD Web UI externally, configure an Ingress.

### NGINX Ingress + TLS Setup

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

If using cert-manager, add `cert-manager.io/cluster-issuer: letsencrypt-prod` to the annotations to set up automatic certificate issuance.

When connecting via the argocd CLI, use the `--grpc-web` flag to connect through the same endpoint without a separate gRPC Ingress.

```bash
argocd login argocd.example.com --grpc-web
```

---

## Step 5: RBAC Configuration

ArgoCD's RBAC is managed through ConfigMaps.

### Basic RBAC Policy

```yaml
# argocd-rbac-cm.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-rbac-cm
  namespace: argocd
data:
  # Default policy: read-only
  policy.default: role:readonly

  policy.csv: |
    # Dev team: view + sync for dev project
    p, role:dev-team, applications, get, dev/*, allow
    p, role:dev-team, applications, sync, dev/*, allow
    p, role:dev-team, logs, get, dev/*, allow

    # Ops team: full staging + view/sync for production
    p, role:ops-team, applications, *, staging/*, allow
    p, role:ops-team, applications, get, production/*, allow
    p, role:ops-team, applications, sync, production/*, allow

    # SSO group -> ArgoCD role mapping
    g, devops-admins, role:admin
    g, developers, role:dev-team
    g, operations, role:ops-team
```

```bash
kubectl apply -f argocd-rbac-cm.yaml
```

### Per-Team Access Control with AppProject

```yaml
# dev-project.yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: dev
  namespace: argocd
spec:
  description: "Development team project"
  # Allowed source repositories
  sourceRepos:
    - "https://github.com/my-org/dev-*"
  # Allowed deployment targets (clusters/namespaces)
  destinations:
    - namespace: "dev-*"
      server: https://kubernetes.default.svc
  # Allowed resources (add as needed)
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

## Step 6: SSO Integration

In production environments, configure SSO to reduce the burden of managing individual accounts.

### GitHub OAuth Setup

**Create OAuth App on GitHub:**

1. GitHub Settings > Developer settings > OAuth Apps > New OAuth App
2. **Homepage URL**: `https://argocd.example.com`
3. **Authorization callback URL**: `https://argocd.example.com/api/dex/callback`
4. Note down the Client ID and Client Secret

**ArgoCD ConfigMap Configuration** (`argocd-cm`):

```yaml
# data section of argocd-cm
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
# Store credentials in Secret
kubectl -n argocd create secret generic argocd-github-oauth \
  --from-literal=dex.github.clientID=<YOUR_CLIENT_ID> \
  --from-literal=dex.github.clientSecret=<YOUR_CLIENT_SECRET>
```

### OIDC Setup (Keycloak, etc.)

To use OIDC, configure `oidc.config` instead of `dex.config` in `argocd-cm`.

```yaml
# Add to the data section of argocd-cm
  oidc.config: |
    name: Keycloak
    issuer: https://keycloak.example.com/realms/my-realm
    clientID: argocd
    clientSecret: $oidc.keycloak.clientSecret
    requestedScopes: ["openid", "profile", "email", "groups"]
```

{{< callout type="info" >}}
After setting up SSO, be sure to update the RBAC group mapping (`g, <sso-group>, role:<argocd-role>`).
{{< /callout >}}

---

## Step 7: Repository Connection

ArgoCD needs a registered Repository to fetch manifests from Git repositories.

### HTTPS Method

```bash
argocd repo add https://github.com/my-org/my-repo.git \
  --username <USERNAME> \
  --password <TOKEN>
```

### SSH Key Method

```bash
# Generate SSH key (skip if you already have one)
ssh-keygen -t ed25519 -f ~/.ssh/argocd -N ""

# Register SSH key with ArgoCD
argocd repo add git@github.com:my-org/my-repo.git \
  --ssh-private-key-path ~/.ssh/argocd
```

### GitHub App Method (Recommended)

GitHub Apps provide fine-grained permission control and higher API Rate Limits.

1. GitHub Settings > Developer settings > GitHub Apps > New GitHub App
2. **Permissions**: Repository contents (Read-only)
3. Download the Private Key

```bash
argocd repo add https://github.com/my-org/my-repo.git \
  --github-app-id <APP_ID> \
  --github-app-installation-id <INSTALLATION_ID> \
  --github-app-private-key-path /path/to/private-key.pem
```

### Declarative Method (Managed as Secret)

In true GitOps fashion, Repository connections can also be managed declaratively as Secret resources. Creating a Secret with the `argocd.argoproj.io/secret-type: repository` label allows ArgoCD to automatically recognize it.

---

## Step 8: Installation Verification

Once all configuration is complete, verify by deploying a simple Application.

### Create Test Application

```bash
argocd app create guestbook \
  --repo https://github.com/argoproj/argocd-example-apps.git \
  --path guestbook \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace default
```

### Sync and Check Status

```bash
# Sync the Application
argocd app sync guestbook

# Check status
argocd app get guestbook
```

If `Health Status: Healthy` and `Sync Status: Synced` are displayed, everything is working correctly.

### Clean Up Test Application

```bash
argocd app delete guestbook --cascade
```

---

## Troubleshooting

### Pod Won't Start

```bash
# Check Pod events
kubectl describe pod -n argocd -l app.kubernetes.io/name=argocd-server

# Check logs
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-server --tail=50
```

**Common causes:**

| Symptom | Cause | Solution |
|---------|-------|----------|
| `ImagePullBackOff` | Image download failed | Check network/registry access |
| `CrashLoopBackOff` | Container start failed | Check error message in logs |
| `Pending` | Insufficient resources | Free up node resources or reduce requests |
| `OOMKilled` | Memory exceeded | Increase limits value |

### Ingress Access Failure

- Check status with `kubectl get ingress -n argocd`
- Verify DNS settings, `backend-protocol: "HTTPS"` annotation, and TLS Secret namespace
- Test SSL connection with `curl -vk https://argocd.example.com`

### SSO Login Failure

- Check Dex logs: `kubectl logs -n argocd -l app.kubernetes.io/name=argocd-dex-server --tail=50`
- Verify callback URL ends with `/api/dex/callback`
- Ensure `url` field in `argocd-cm` matches the actual access URL

### Repository Connection Failure

- Check registration status with `argocd repo list`
- HTTPS: Verify token expiration
- SSH: Verify known hosts registration with `ssh-keyscan github.com`

---

## Next Steps

ArgoCD installation and basic configuration are complete. Explore the following topics:

- **Application Deployment**: Define Application resources and deploy with GitOps workflow
- **ApplicationSet**: Auto-create Applications in multi-cluster/multi-tenant environments
- **Notifications**: Configure deployment notifications for Slack, Email, etc.
- **Image Updater**: Set up automatic container image tag updates
