---
lastmod: "2026-01-11"
title: 기본 예제
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **대상 독자**: Kubernetes 핵심 리소스를 실습하고 싶은 개발자
> **선수 지식**: Pod, Deployment, Service 개념
> **이 문서를 읽으면**: Pod, Deployment, Service, ConfigMap을 직접 생성하고 관리할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- Nginx Deployment를 생성하고 Service로 노출합니다
- ConfigMap으로 설정을 주입합니다
- 롤링 업데이트와 롤백을 실습합니다
{{< /callout >}}

## 사전 준비

다음이 필요합니다:

- 로컬 Kubernetes 클러스터 (Minikube 또는 Kind)
- kubectl

```bash
# 클러스터 상태 확인
kubectl cluster-info
```

## 실습 1: Deployment와 Service

### Deployment 생성

웹 서버를 배포합니다.

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
# Deployment 생성
kubectl apply -f nginx-deployment.yaml

# 상태 확인
kubectl get deployment nginx
kubectl get pods -l app=nginx
```

**예상 출력:**
```
NAME    READY   UP-TO-DATE   AVAILABLE   AGE
nginx   3/3     3            3           30s
```

### Service 생성

Pod에 접근하기 위한 Service를 생성합니다.

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
# Service 생성
kubectl apply -f nginx-service.yaml

# 상태 확인
kubectl get service nginx
kubectl get endpoints nginx
```

### 내부 접근 테스트

```bash
# 임시 Pod에서 Service 접근
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- wget -qO- http://nginx

# 여러 번 요청하면 다른 Pod로 분산되는 것 확인
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- sh -c 'for i in 1 2 3 4 5; do wget -qO- http://nginx 2>/dev/null | head -1; done'
```

### 외부 접근 (NodePort)

```bash
# Service 타입 변경
kubectl patch service nginx -p '{"spec":{"type":"NodePort"}}'

# Minikube에서 접근
minikube service nginx --url

# 할당된 포트 확인
kubectl get service nginx
```

## 실습 2: ConfigMap 활용

### ConfigMap 생성

Nginx 설정을 ConfigMap으로 관리합니다.

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

### Deployment에 ConfigMap 마운트

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
# Deployment 업데이트
kubectl apply -f nginx-deployment-with-config.yaml

# 롤아웃 상태 확인
kubectl rollout status deployment/nginx

# 테스트
kubectl run test --image=busybox:1.36 --rm -it --restart=Never \
  -- wget -qO- http://nginx
```

## 실습 3: 롤링 업데이트와 롤백

### 이미지 업데이트

```bash
# 현재 이미지 확인
kubectl get deployment nginx -o jsonpath='{.spec.template.spec.containers[0].image}'

# 이미지 업데이트
kubectl set image deployment/nginx nginx=nginx:1.26

# 롤아웃 상태 확인
kubectl rollout status deployment/nginx

# Pod 확인 (점진적 교체)
kubectl get pods -l app=nginx -w
```

### 롤아웃 히스토리

```bash
# 히스토리 확인
kubectl rollout history deployment/nginx

# 특정 리비전 상세 정보
kubectl rollout history deployment/nginx --revision=2
```

### 롤백

```bash
# 이전 버전으로 롤백
kubectl rollout undo deployment/nginx

# 특정 버전으로 롤백
kubectl rollout undo deployment/nginx --to-revision=1

# 확인
kubectl get deployment nginx -o jsonpath='{.spec.template.spec.containers[0].image}'
```

## 실습 4: 스케일링

### 수동 스케일링

```bash
# 5개로 스케일 아웃
kubectl scale deployment/nginx --replicas=5

# 확인
kubectl get pods -l app=nginx

# 2개로 스케일 다운
kubectl scale deployment/nginx --replicas=2
```

### HPA 설정 (선택)

```bash
# Metrics Server 활성화 (Minikube)
minikube addons enable metrics-server

# HPA 생성
kubectl autoscale deployment nginx --cpu-percent=50 --min=2 --max=10

# HPA 상태 확인
kubectl get hpa nginx
```

## 실습 5: 리소스 정리

### 개별 삭제

```bash
kubectl delete service nginx
kubectl delete deployment nginx
kubectl delete configmap nginx-config
```

### 한 번에 삭제

```bash
# 레이블로 삭제
kubectl delete all -l app=nginx

# YAML 파일로 삭제
kubectl delete -f nginx-deployment.yaml -f nginx-service.yaml
```

## 전체 YAML 파일

실습에 사용한 전체 리소스를 하나의 파일로 정리합니다.

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
# 전체 배포
kubectl apply -f complete-example.yaml

# 전체 삭제
kubectl delete -f complete-example.yaml
```

---

## 다음 단계

기본 예제를 완료했다면 다음 단계로 진행하세요:

| 목표 | 추천 문서 |
|------|----------|
| 실제 앱 배포 | [Spring Boot 배포](../spring-boot/) |
| 문제 해결 | [Pod 트러블슈팅](../../howto/pod-troubleshooting/) |
| 개념 심화 | [리소스 관리](../../concepts/resources/) |
