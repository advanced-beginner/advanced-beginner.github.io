---
lastmod: "2026-01-11"
title: Basic Example
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Target Audience**: Developers who want to practice Kubernetes core resources hands-on
> **Prerequisites**: Pod, Deployment, Service concepts
> **After reading this**: You'll be able to create and manage Pod, Deployment, Service, and ConfigMap directly

{{< callout type="tip" title="TL;DR" >}}
- Create Nginx Deployment and expose via Service
- Inject configuration with ConfigMap
- Practice rolling updates and rollbacks
{{< /callout >}}

## Prerequisites

You need:

- Local Kubernetes cluster (Minikube or Kind)
- kubectl

```bash
# Check cluster status
kubectl cluster-info
```

## Exercise 1: Deployment and Service

### Create Deployment

Deploy a web server.

```yaml
# nginx-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
  labels:
    app: nginx
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
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "64Mi"
            cpu: "100m"
          limits:
            memory: "128Mi"
            cpu: "200m"
```

```bash
# Create Deployment
kubectl apply -f nginx-deployment.yaml

# Check status
kubectl get deployment nginx
kubectl get pods -l app=nginx
```

**Expected output:**
```
NAME    READY   UP-TO-DATE   AVAILABLE   AGE
nginx   3/3     3            3           30s
```

### Create Service

Create a Service to access the Pods.

```yaml
# nginx-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
spec:
  type: ClusterIP
  selector:
    app: nginx
  ports:
  - port: 80
    targetPort: 80
```

```bash
# Create Service
kubectl apply -f nginx-service.yaml

# Check status
kubectl get service nginx
kubectl get endpoints nginx
```

### Test Internal Access

```bash
# Access Service from temporary Pod
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- wget -qO- http://nginx

# Multiple requests will be distributed across different Pods
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- sh -c 'for i in 1 2 3 4 5; do wget -qO- http://nginx 2>/dev/null | head -1; done'
```

### External Access (NodePort)

```bash
# Change Service type
kubectl patch service nginx -p '{"spec":{"type":"NodePort"}}'

# Access via Minikube
minikube service nginx --url

# Check assigned port
kubectl get service nginx
```

## Exercise 2: Using ConfigMap

### Create ConfigMap

Manage Nginx configuration with ConfigMap.

```yaml
# nginx-configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-config
data:
  nginx.conf: |
    server {
        listen 80;
        server_name localhost;

        location / {
            root /usr/share/nginx/html;
            index index.html;
        }

        location /health {
            return 200 'healthy\n';
            add_header Content-Type text/plain;
        }
    }
  index.html: |
    <!DOCTYPE html>
    <html>
    <head><title>Hello Kubernetes!</title></head>
    <body>
    <h1>Hello from Kubernetes!</h1>
    <p>Pod Name: $HOSTNAME</p>
    </body>
    </html>
```

```bash
kubectl apply -f nginx-configmap.yaml
```

### Mount ConfigMap in Deployment

```yaml
# nginx-deployment-with-config.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
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
        ports:
        - containerPort: 80
        volumeMounts:
        - name: config
          mountPath: /etc/nginx/conf.d/default.conf
          subPath: nginx.conf
        - name: config
          mountPath: /usr/share/nginx/html/index.html
          subPath: index.html
        resources:
          requests:
            memory: "64Mi"
            cpu: "100m"
          limits:
            memory: "128Mi"
            cpu: "200m"
      volumes:
      - name: config
        configMap:
          name: nginx-config
```

```bash
# Update Deployment
kubectl apply -f nginx-deployment-with-config.yaml

# Check rollout status
kubectl rollout status deployment/nginx

# Test
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- wget -qO- http://nginx
```

## Exercise 3: Rolling Updates and Rollbacks

### Update Image

```bash
# Check current image
kubectl get deployment nginx -o jsonpath='{.spec.template.spec.containers[0].image}'

# Update image
kubectl set image deployment/nginx nginx=nginx:1.26

# Check rollout status
kubectl rollout status deployment/nginx

# Watch Pods (gradual replacement)
kubectl get pods -l app=nginx -w
```

### Rollout History

```bash
# Check history
kubectl rollout history deployment/nginx

# Detailed info for specific revision
kubectl rollout history deployment/nginx --revision=2
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/nginx

# Rollback to specific version
kubectl rollout undo deployment/nginx --to-revision=1

# Verify
kubectl get deployment nginx -o jsonpath='{.spec.template.spec.containers[0].image}'
```

## Exercise 4: Scaling

### Manual Scaling

```bash
# Scale out to 5
kubectl scale deployment/nginx --replicas=5

# Verify
kubectl get pods -l app=nginx

# Scale down to 2
kubectl scale deployment/nginx --replicas=2
```

### HPA Setup (Optional)

```bash
# Enable Metrics Server (Minikube)
minikube addons enable metrics-server

# Create HPA
kubectl autoscale deployment nginx --cpu-percent=50 --min=2 --max=10

# Check HPA status
kubectl get hpa nginx
```

## Exercise 5: Cleanup

### Delete Individually

```bash
kubectl delete service nginx
kubectl delete deployment nginx
kubectl delete configmap nginx-config
```

### Delete All at Once

```bash
# Delete by label
kubectl delete all -l app=nginx

# Delete by YAML files
kubectl delete -f nginx-deployment.yaml -f nginx-service.yaml
```

## Complete YAML File

A single file containing all resources used in the exercises.

```yaml
# complete-example.yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-config
data:
  nginx.conf: |
    server {
        listen 80;
        location / {
            root /usr/share/nginx/html;
        }
        location /health {
            return 200 'healthy\n';
        }
    }
  index.html: |
    <h1>Hello from Kubernetes!</h1>
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
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
        ports:
        - containerPort: 80
        volumeMounts:
        - name: config
          mountPath: /etc/nginx/conf.d/default.conf
          subPath: nginx.conf
        - name: config
          mountPath: /usr/share/nginx/html/index.html
          subPath: index.html
        resources:
          requests:
            memory: "64Mi"
            cpu: "100m"
          limits:
            memory: "128Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: config
        configMap:
          name: nginx-config
---
apiVersion: v1
kind: Service
metadata:
  name: nginx
spec:
  type: ClusterIP
  selector:
    app: nginx
  ports:
  - port: 80
    targetPort: 80
```

```bash
# Deploy all
kubectl apply -f complete-example.yaml

# Delete all
kubectl delete -f complete-example.yaml
```

---

## Next Steps

After completing the basic example, proceed to:

| Goal | Recommended Document |
|------|----------|
| Deploy real app | [Spring Boot Deployment](spring-boot/) |
| Troubleshooting | [Pod Troubleshooting](../howto/pod-troubleshooting/) |
| Deepen concepts | [Resource Management](../concepts/resources/) |
