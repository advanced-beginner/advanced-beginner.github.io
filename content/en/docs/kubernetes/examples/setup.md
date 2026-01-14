---
lastmod: "2026-01-11"
title: Environment Setup
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Target Audience**: Developers who want to practice Kubernetes locally
> **Prerequisites**: Docker basics
> **After reading this**: You'll be able to set up a local Kubernetes environment with Minikube or Kind

{{< callout type="tip" title="TL;DR" >}}
- Minikube: Single node, recommended for beginners, rich addon support
- Kind: Multi-node capable, suitable for CI/CD, fast cluster creation
- Docker Desktop: Simplest setup for macOS/Windows
{{< /callout >}}

## Local Kubernetes Options Comparison

| Tool | Pros | Cons | Best For |
|------|------|------|------------|
| Minikube | Rich addons, extensive docs | Single node only | Learning, development |
| Kind | Fast, multi-node, CI-friendly | Fewer addons | CI/CD, testing |
| Docker Desktop | Easy installation | High resource usage | Quick start |
| k3d | Lightweight, fast | Different from production | Quick testing |

## Minikube Installation

### Install

{{< tabs "minikube-install" >}}
{{< tab "macOS" >}}
```bash
# Install via Homebrew
brew install minikube

# Check version
minikube version
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
# Download binary
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64

# Install
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Check version
minikube version
```
{{< /tab >}}
{{< tab "Windows" >}}
```powershell
# Install via Chocolatey
choco install minikube

# Or manual installation
# https://minikube.sigs.k8s.io/docs/start/
```
{{< /tab >}}
{{< /tabs >}}

### Start Cluster

```bash
# Start with default settings
minikube start

# Specify resources
minikube start --cpus=4 --memory=8192

# Use specific Kubernetes version
minikube start --kubernetes-version=v1.29.0

# Specify driver
minikube start --driver=docker
```

Available drivers:

| Driver | Platform | Features |
|----------|--------|------|
| docker | All | Recommended, most stable |
| virtualbox | All | Full VM, slow |
| hyperkit | macOS | Lightweight |
| hyperv | Windows | Windows built-in |

### Check Status

```bash
# Cluster status
minikube status

# Kubernetes version
kubectl version

# Node information
kubectl get nodes
```

### Useful Addons

```bash
# List addons
minikube addons list

# Enable key addons
minikube addons enable metrics-server  # Required for HPA
minikube addons enable ingress         # Ingress testing
minikube addons enable dashboard       # Web UI
```

### Access Dashboard

```bash
# Open Dashboard
minikube dashboard
```

### Cluster Management

```bash
# Pause
minikube pause

# Resume
minikube unpause

# Stop (preserve resources)
minikube stop

# Delete (remove all data)
minikube delete
```

## Kind Installation

Kind (Kubernetes IN Docker) is a tool that uses Docker containers as nodes.

### Install

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

### Create Cluster

```bash
# Basic cluster (single node)
kind create cluster

# Named cluster
kind create cluster --name my-cluster

# Multi-node cluster
cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
- role: worker
EOF
```

### Multi-node Configuration File

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

### Cluster Management

```bash
# List clusters
kind get clusters

# Delete cluster
kind delete cluster --name my-cluster

# Load image (load local image into cluster)
kind load docker-image my-app:1.0 --name my-cluster
```

## kubectl Installation

kubectl is the CLI tool for communicating with Kubernetes clusters.

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

### kubectl Auto-completion Setup

{{< tabs "kubectl-completion" >}}
{{< tab "bash" >}}
```bash
# Install bash-completion
# macOS
brew install bash-completion

# Add to .bashrc
echo 'source <(kubectl completion bash)' >> ~/.bashrc
echo 'alias k=kubectl' >> ~/.bashrc
echo 'complete -F __start_kubectl k' >> ~/.bashrc
source ~/.bashrc
```
{{< /tab >}}
{{< tab "zsh" >}}
```bash
# Add to .zshrc
echo 'source <(kubectl completion zsh)' >> ~/.zshrc
echo 'alias k=kubectl' >> ~/.zshrc
source ~/.zshrc
```
{{< /tab >}}
{{< /tabs >}}

### Useful kubectl Aliases

```bash
# Add to ~/.bashrc or ~/.zshrc
alias k='kubectl'
alias kg='kubectl get'
alias kd='kubectl describe'
alias kl='kubectl logs'
alias kx='kubectl exec -it'
alias ka='kubectl apply -f'
alias kdel='kubectl delete'
```

## Installation Verification

Verify your environment is set up correctly.

```bash
# Cluster information
kubectl cluster-info

# Node list
kubectl get nodes

# Check system Pods
kubectl get pods -n kube-system

# Run test Pod
kubectl run test --image=nginx --rm -it --restart=Never -- curl localhost
```

**Expected output:**
```
Kubernetes control plane is running at https://127.0.0.1:xxxxx
CoreDNS is running at https://127.0.0.1:xxxxx/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

---

## Next Steps

After completing environment setup, proceed to:

| Goal | Recommended Document |
|------|----------|
| Getting started with Kubernetes | [Quick Start](../quick-start/) |
| Practice basic resources | [Basic Example](basic/) |
| Deploy real app | [Spring Boot Deployment](spring-boot/) |
