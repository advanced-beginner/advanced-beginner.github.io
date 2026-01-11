---
lastmod: "2026-01-11"
title: 개념 이해
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Kubernetes를 제대로 활용하려면 단순히 kubectl 명령어를 아는 것만으로는 부족합니다. 왜 Pod가 여러 개 필요한지, Service가 어떻게 트래픽을 분산하는지, 설정은 어디에 저장해야 하는지를 이해해야 운영 중 발생하는 문제를 빠르게 진단하고 해결할 수 있습니다. 이 섹션에서는 Kubernetes의 핵심 구성요소와 동작 원리를 단계별로 학습합니다.

#### 학습 순서

아래 학습 순서는 개념 간 의존성을 고려하여 설계되었습니다. 기초 개념을 충분히 이해한 후 심화 학습으로 넘어가는 것을 권장합니다. 특히 Pod와 Deployment는 이후 모든 개념의 토대가 되므로 확실하게 이해하고 넘어가야 합니다.

**기초 개념**

기초 개념에서는 Kubernetes 클러스터를 구성하는 핵심 요소들과 애플리케이션이 배포되는 과정을 다룹니다. Pod가 어떻게 생성되고, Deployment가 Pod를 어떻게 관리하며, Service가 어떻게 트래픽을 전달하는지 이해하는 것이 목표입니다.

1. [아키텍처](architecture/) - Control Plane과 Worker Node의 구성요소를 이해합니다. Kubernetes가 어떻게 동작하는지 전체 그림을 파악합니다.
2. [Pod](pod/) - Kubernetes의 최소 배포 단위인 Pod의 개념과 생명주기를 학습합니다. 왜 컨테이너 대신 Pod를 사용하는지 이해합니다.
3. [Deployment](deployment/) - Pod의 생성, 업데이트, 롤백을 관리하는 Deployment를 학습합니다. 무중단 배포의 원리를 이해합니다.
4. [Service](service/) - Pod에 대한 안정적인 네트워크 접근을 제공하는 Service를 학습합니다. ClusterIP, NodePort, LoadBalancer의 차이를 이해합니다.
5. [ConfigMap과 Secret](configmap-secret/) - 애플리케이션 설정과 민감 정보를 분리하여 관리하는 방법을 학습합니다.

**심화 학습**

심화 학습에서는 운영 환경에서 Kubernetes를 안정적으로 운영하기 위한 고급 주제들을 다룹니다. 영구 데이터 저장, 네트워크 구성, 리소스 관리, 자동 스케일링 등 실제 서비스 운영에 필수적인 내용입니다.

6. [Volume과 스토리지](storage/) - Pod가 종료되어도 데이터를 유지하는 영구 볼륨(PV)과 볼륨 클레임(PVC)을 학습합니다.
7. [네트워킹](networking/) - 클러스터 내부/외부 통신의 원리와 Ingress를 통한 HTTP 라우팅을 학습합니다.
8. [리소스 관리](resources/) - CPU와 메모리의 요청(requests)과 제한(limits) 설정 방법을 학습합니다. 리소스 부족 상황에서의 동작을 이해합니다.
9. [스케일링](scaling/) - HPA(Horizontal Pod Autoscaler)를 통한 자동 스케일링과 VPA의 개념을 학습합니다.
10. [헬스 체크](health-checks/) - Liveness, Readiness, Startup Probe를 통해 애플리케이션 상태를 모니터링하고 자동 복구하는 방법을 학습합니다.
