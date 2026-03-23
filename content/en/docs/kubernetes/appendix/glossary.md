---
lastmod: "2026-01-11"
title: Glossary
description: "Key Kubernetes terminology and definitions"
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

This glossary organizes core terms needed for Kubernetes learning and operations.

## A-C

### Cluster
The entire Kubernetes environment composed of multiple nodes (machines). It consists of one Control Plane and multiple Worker Nodes.

### ConfigMap
A resource that stores configuration data as key-value pairs. Can be injected into Pods as environment variables or configuration files.

### Container
An isolated execution environment that packages an application and its dependencies. Docker containers are the most representative.

### Container Runtime
Software that runs containers. Examples include containerd and CRI-O. Kubernetes no longer directly supports Docker since version 1.24.

### Control Plane
Components that manage the state of the cluster. Consists of API Server, etcd, Scheduler, and Controller Manager.

### CronJob
A Job that runs repeatedly according to a specified schedule. Uses cron-like schedule syntax similar to Linux cron.

## D-H

### DaemonSet
A workload that runs one Pod on all nodes (or specific nodes). Primarily used for log collection and monitoring agents.

### Deployment
A workload that manages declarative updates of Pods. Creates ReplicaSets and supports rolling updates and rollbacks.

```yaml
# Example: Deployment managing 3 nginx Pods
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
A distributed key-value store that holds all Kubernetes cluster state. Clustered in odd numbers for high availability.

### HPA (Horizontal Pod Autoscaler)
A resource that automatically adjusts the number of Pods based on metrics like CPU and memory.

### Helm
A Kubernetes application package manager. Uses a package format called Charts.

## I-L

### Ingress
Defines rules for routing HTTP/HTTPS traffic from outside the cluster to internal Services. Supports domain and path-based routing.

### Ingress Controller
The component that actually implements Ingress resources. Examples include NGINX and Traefik.

### Job
A workload that runs one-time tasks. Manages Pods until completion and retries on failure.

### Kubelet
An agent running on each Worker Node. Responsible for running Pods, reporting status, and health checks.

### kubectl
A CLI tool for communicating with the Kubernetes cluster. Most operations are performed through kubectl.

### kube-proxy
Manages network rules on each node. Forwards traffic coming to Services to Pods.

## M-P

### Namespace
A unit for logically separating a cluster. Can be separated by environment (dev, staging, prod) or by team.

### Node
A machine (physical or virtual) that makes up the Kubernetes cluster. Actual workloads run on Worker Nodes.

### PersistentVolume (PV)
A storage resource provisioned by an administrator. Has a lifecycle independent of Pods.

### PersistentVolumeClaim (PVC)
Storage requested by a user. Binds to a PV and is used by Pods.

### Pod
The smallest deployable unit in Kubernetes. Contains one or more containers and shares network and storage.

```yaml
# Example: nginx Pod
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
A mechanism for checking container health. There are Liveness, Readiness, and Startup Probes.

## Q-S

### QoS Class
The Quality of Service class for a Pod. There are three classes: Guaranteed, Burstable, and BestEffort.

### ReplicaSet
Maintains a specified number of Pod replicas. Usually managed indirectly through Deployments.

### Requests
The minimum amount of resources a Pod should be guaranteed. Used as the basis for scheduling.

### Limits
The maximum amount of resources a Pod can use. CPU is throttled when exceeded, and memory causes OOMKilled.

### Secret
A resource that stores sensitive information like passwords and API keys. Base64 encoded, requires separate configuration for encryption.

### Service
Provides a stable network endpoint for a set of Pods. Types include ClusterIP, NodePort, and LoadBalancer.

```yaml
# Example: Service exposing nginx Pods
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
A workload for stateful applications (like databases). Provides sequential deployment, stable network IDs, and persistent storage.

### StorageClass
Defines storage types for dynamic PV provisioning. Uses different provisioners for different clouds.

## T-Z

### Taint
An attribute that prevents Pods from being scheduled on specific nodes. Only Pods with Toleration can be scheduled on that node.

### Toleration
A Pod attribute that allows scheduling on nodes with Taints.

### VPA (Vertical Pod Autoscaler)
Automatically adjusts Pod resource requests/limits. Not installed by default.

### Volume
Storage mounted to a Pod. Multiple types exist including emptyDir, hostPath, and PVC.

### Worker Node
A node where actual workloads (Pods) run. Kubelet, kube-proxy, and Container Runtime are installed.

### YAML
The primary format for defining Kubernetes resources. JSON is also usable but YAML is more popular due to better readability.
