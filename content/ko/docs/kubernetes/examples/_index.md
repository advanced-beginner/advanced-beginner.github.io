---
bookCollapseSection: true
lastmod: "2026-01-11"
title: 실습 예제
description: "Kubernetes 실습 예제의 학습 가이드와 문서 목록입니다."
weight: 3
author:
  name: Advanced Beginner
  github: advanced-beginner
---

이 섹션에서는 Kubernetes의 핵심 기능들을 실제로 실행해볼 수 있는 예제들을 제공합니다. 각 예제는 독립적으로 실행할 수 있으며, 필요한 YAML 파일과 명령어를 모두 포함하고 있습니다.

## 목표별 예제 선택

**"나는 어떤 예제부터 해야 할까?"**

```mermaid
flowchart TD
    START[실습 시작] --> Q1{Kubernetes<br>설치되어 있나?}

    Q1 -->|아니오| A[환경 설정]
    Q1 -->|예| Q2{Kubernetes<br>경험이 있나?}

    Q2 -->|처음이다| B[기본 예제]
    Q2 -->|조금 있다| Q3{실제 앱을<br>배포하고 싶다}

    Q3 -->|예| C[Spring Boot 배포]
    Q3 -->|아니오| Q4{상태 관리 /<br>운영이 필요하다}

    Q4 -->|상태 관리| D[StatefulSet 실습]
    Q4 -->|접근 제어| E[RBAC 설정]
    Q4 -->|스케줄링| F[CronJob 실습]

    A -->|완료 후| B
    B -->|완료 후| C
    C -->|완료 후| D
```

| 당신의 상황 | 추천 예제 | 배우는 것 |
|------------|----------|----------|
| Kubernetes가 처음이다 | [환경 설정]({{< relref "/docs/kubernetes/examples/setup" >}}) → [기본 예제]({{< relref "/docs/kubernetes/examples/basic" >}}) | 클러스터 구축, Pod/Service 기초 |
| 개념은 알지만 실습이 부족하다 | [기본 예제]({{< relref "/docs/kubernetes/examples/basic" >}}) | Pod, Deployment, Service 직접 실행 |
| 실제 앱을 배포하고 싶다 | [Spring Boot 배포]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) | ConfigMap, Secret, Probe 적용 |
| 상태를 유지하는 앱이 필요하다 | [StatefulSet 실습]({{< relref "/docs/kubernetes/examples/statefulset" >}}) | PVC 템플릿, Headless Service |
| 접근 권한을 관리하고 싶다 | [RBAC 설정 실습]({{< relref "/docs/kubernetes/examples/rbac" >}}) | Role, ServiceAccount, RoleBinding |
| 주기적 작업을 자동화하고 싶다 | [CronJob 실습]({{< relref "/docs/kubernetes/examples/cronjob" >}}) | 스케줄링, 동시 실행 정책 |

#### 예제 목록

| 예제 | 난이도 | 예상 시간 | 다루는 내용 |
|------|--------|----------|-------------|
| [환경 설정]({{< relref "/docs/kubernetes/examples/setup" >}}) | ⭐ 입문 | 30분 | Minikube, Kind 등 로컬 환경 구성 |
| [기본 예제]({{< relref "/docs/kubernetes/examples/basic" >}}) | ⭐⭐ 기초 | 60분 | Pod, Deployment, Service 실습 |
| [Spring Boot 배포]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) | ⭐⭐⭐ 중급 | 90분 | 실제 애플리케이션 배포 |
| [StatefulSet 실습]({{< relref "/docs/kubernetes/examples/statefulset" >}}) | ⭐⭐⭐ 중급 | 60분 | MySQL StatefulSet, PVC, Headless Service |
| [RBAC 설정 실습]({{< relref "/docs/kubernetes/examples/rbac" >}}) | ⭐⭐⭐ 중급 | 45분 | Role, ServiceAccount, 네임스페이스별 접근 제어 |
| [CronJob 실습]({{< relref "/docs/kubernetes/examples/cronjob" >}}) | ⭐⭐⭐ 중급 | 45분 | 주기적 백업, 동시 실행 정책, 알림 설정 |

#### 예제 실행 전 준비사항

모든 예제는 다음 환경이 필요합니다:

- Docker 24.x 이상
- kubectl 1.29.x 이상
- 로컬 Kubernetes 클러스터 (Minikube 또는 Kind)

환경 설정이 되어 있지 않다면 먼저 [환경 설정]({{< relref "/docs/kubernetes/examples/setup" >}}) 예제를 따라 진행하세요.
