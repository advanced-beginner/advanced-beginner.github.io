---
lastmod: "2026-01-11"
title: Networking
description: "Kubernetes networking model and how it works"
weight: 7
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Target Audience**: Backend developers who want to understand Kubernetes network structure
> **Prerequisites**: Service concepts, basic networking knowledge (IP, ports, DNS)
> **After reading this**: You will understand Kubernetes network model and Ingress

{{< callout type="tip" title="TL;DR" >}}
- All Pods have unique IPs and can communicate directly with other Pods
- Service provides stable access point for a set of Pods
- Ingress routes HTTP/HTTPS traffic from outside the cluster
{{< /callout >}}

## Kubernetes Network Model

Kubernetes networking follows three basic principles.

| Principle | Description |
|-----------|-------------|
| Pod-to-Pod | All Pods can communicate with other Pods without NAT |
| Node-to-Pod | All nodes can communicate with all Pods without NAT |
| Pod IP | Pod's own IP and the IP other Pods see are identical |

```mermaid
flowchart TB
    subgraph Node1[Node 1]
        P1[Pod A<br>10.244.1.5]
        P2[Pod B<br>10.244.1.6]
    end
    subgraph Node2[Node 2]
        P3[Pod C<br>10.244.2.3]
    end
    P1 <-->|direct communication| P2
    P1 <-->|cross-node communication| P3
```

## Cluster Internal Communication

### Pod-to-Pod Communication

Pods within the same cluster can communicate directly via IP.

```bash
# Direct communication from Pod A to Pod C
kubectl exec pod-a -- curl http://10.244.2.3:8080
```

However, since Pod IPs can change, we actually use Services.

### Communication Through Service

```mermaid
flowchart LR
    Client[Client Pod] -->|my-service:80| SVC[Service]
    SVC --> P1[Pod 1]
    SVC --> P2[Pod 2]
    SVC --> P3[Pod 3]
```

Using Service DNS prevents impact from Pod IP changes.

```yaml
# Use Service name in application
spring:
  datasource:
    url: jdbc:postgresql://postgres-service:5432/mydb
```

## External Exposure Methods Comparison

Comparing methods to access applications from outside the cluster.

| Method | Use Case | Characteristics |
|--------|----------|----------------|
| NodePort | Dev/test | Simple but port limited (30000-32767) |
| LoadBalancer | Single service external exposure | Uses cloud LB, incurs cost |
| Ingress | HTTP routing to multiple services | Domain/path-based routing |

## Ingress

Ingress manages HTTP/HTTPS traffic from outside the cluster to internal Services.

### External Request Processing Flow

Let's examine step by step how HTTP requests from outside reach Pods.

```mermaid
flowchart TB
    subgraph External[External]
        User[User<br>api.example.com/users]
    end

    subgraph Cluster[Kubernetes Cluster]
        subgraph Ingress[Ingress Layer]
            LB[LoadBalancer<br>External IP]
            IC[Ingress Controller<br>NGINX]
            ING[Ingress Rules]
        end

        subgraph Services[Service Layer]
            SVC[user-service<br>ClusterIP]
        end

        subgraph Pods[Pod Layer]
            P1[Pod 1]
            P2[Pod 2]
        end
    end

    User -->|1. DNS lookup| LB
    LB -->|2. Forward traffic| IC
    IC -->|3. Match rules| ING
    ING -->|4. Route| SVC
    SVC -->|5. Load balance| P1
    SVC -->|5. Load balance| P2
```

| Step | Component | Action |
|------|-----------|--------|
| 1 | DNS/LB | Resolve domain to LoadBalancer IP |
| 2 | LoadBalancer | Forward traffic to Ingress Controller |
| 3 | Ingress Controller | Match host/path in Ingress rules |
| 4 | Ingress → Service | Route to matched Service |
| 5 | Service → Pod | Distribute traffic to multiple Pods |

### Ingress vs LoadBalancer

| Item | LoadBalancer | Ingress |
|------|--------------|---------|
| Target | Single Service | Multiple Services |
| Protocol | TCP/UDP | HTTP/HTTPS |
| Routing | None | Host/path based |
| TLS | Per-Service configuration | Centralized management |
| Cost | LB cost per Service | One LB for multiple services |

```mermaid
flowchart LR
    Client[External client]
    subgraph Cluster
        ING[Ingress Controller]
        S1[Service A]
        S2[Service B]
        S3[Service C]
    end
    Client -->|api.example.com| ING
    Client -->|web.example.com| ING
    ING -->|/api/*| S1
    ING -->|/web/*| S2
    ING -->|/| S3
```

### Ingress Controller

Ingress resource alone doesn't work. An Ingress Controller is needed.

| Controller | Characteristics |
|------------|-----------------|
| NGINX Ingress | Most widely used, community support |
| Traefik | Auto-configuration, Let's Encrypt integration |
| AWS ALB Ingress | Integrates with AWS ALB |
| GKE Ingress | Integrates with GCP HTTP(S) LB |

### Ingress Resource Definition

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
      - path: /orders
        pathType: Prefix
        backend:
          service:
            name: order-service
            port:
              number: 80
  - host: web.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: web-service
            port:
              number: 80
```

Key fields explained:

| Field | Description |
|-------|-------------|
| ingressClassName | Ingress Controller to use |
| rules[].host | Domain to route |
| rules[].http.paths[] | Path-based routing rules |
| pathType | Exact (exact match) or Prefix |
| backend.service | Service to forward traffic to |

### TLS Configuration

TLS configuration for HTTPS.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tls-ingress
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.example.com
    secretName: api-tls-secret
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: api-service
            port:
              number: 80
```

Create TLS Secret as follows:

```bash
kubectl create secret tls api-tls-secret \
  --cert=path/to/cert.crt \
  --key=path/to/cert.key
```

## NetworkPolicy

NetworkPolicy controls traffic between Pods. By default, all Pods can communicate with each other, but NetworkPolicy can restrict this.

### Default Isolation

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: deny-all
spec:
  podSelector: {}  # Apply to all Pods
  policyTypes:
  - Ingress
  - Egress
```

When this policy is applied, all Pods in that namespace have their traffic blocked.

### Allow Specific Traffic

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 8080
```

This policy only allows access from Pods with `app: frontend` label to port 8080 of `app: backend` Pods.

```mermaid
flowchart LR
    F[Frontend Pod] -->|allowed| B[Backend Pod:8080]
    O[Other Pod] -.-x|blocked| B
```

## Practice: Ingress Configuration

### Enable Ingress in Minikube

```bash
# Enable NGINX Ingress Controller
minikube addons enable ingress

# Check status
kubectl get pods -n ingress-nginx
```

### Deploy Test Applications

```bash
# Create two Deployments
kubectl create deployment web --image=nginx
kubectl create deployment api --image=httpd

# Create Services
kubectl expose deployment web --port=80
kubectl expose deployment api --port=80
```

### Create Ingress Resource

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: test-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
  - host: test.local
    http:
      paths:
      - path: /web
        pathType: Prefix
        backend:
          service:
            name: web
            port:
              number: 80
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: api
            port:
              number: 80
```

### Test

```bash
# Check Minikube IP
minikube ip

# Add to /etc/hosts (Linux/macOS)
echo "$(minikube ip) test.local" | sudo tee -a /etc/hosts

# Test
curl http://test.local/web
curl http://test.local/api
```

---

## Next Steps

Once you understand networking, proceed to the next steps:

| Goal | Recommended Doc |
|------|----------------|
| Resource configuration | [Resource Management](resources/) |
| Auto-scaling | [Scaling](scaling/) |
| Actual deployment practice | [Spring Boot Deployment](../examples/spring-boot/) |
