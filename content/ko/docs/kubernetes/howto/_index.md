---
lastmod: "2026-01-11"
title: 하우투 가이드
weight: 4
author:
  name: Advanced Beginner
  github: advanced-beginner
---

이 섹션에서는 Kubernetes 운영 중 마주치는 특정 문제를 해결하기 위한 단계별 가이드를 제공합니다. 각 가이드는 명확한 목표와 함께 구체적인 해결 방법을 제시합니다.

## 문제 상황별 가이드 선택

어떤 가이드를 봐야 할지 모르겠다면, 아래 플로차트를 따라가세요.

```mermaid
flowchart TD
    START[문제 발생] --> Q1{Pod 상태가<br>정상인가?}

    Q1 -->|Pending/Error/CrashLoop| A[Pod 트러블슈팅]
    Q1 -->|Running| Q2{성능 문제가<br>있는가?}

    Q2 -->|느림/OOM/스로틀링| B[리소스 최적화]
    Q2 -->|없음| Q3{어떤 문제인가?}

    Q3 -->|네트워크 연결| C["개념 > 네트워킹 참고"]
    Q3 -->|스케일링| D["개념 > 스케일링 참고"]

    A --> A1["kubectl describe pod<br>kubectl logs 확인"]
    B --> B1["kubectl top pods<br>메트릭 분석"]

    style A fill:#f9f,stroke:#333
    style B fill:#bbf,stroke:#333
```

| 증상 | 추천 가이드 |
|------|------------|
| Pod가 시작 안 됨, CrashLoopBackOff | [Pod 트러블슈팅](pod-troubleshooting/) |
| 느린 응답, OOMKilled, CPU 스로틀링 | [리소스 최적화](resource-optimization/) |

#### 가이드 목록

| 가이드 | 상황 | 예상 시간 |
|--------|------|----------|
| [Pod 트러블슈팅](pod-troubleshooting/) | Pod가 시작되지 않거나 비정상 종료될 때 | 30분 |
| [리소스 최적화](resource-optimization/) | 적절한 CPU/메모리 설정을 찾고 싶을 때 | 45분 |

#### 하우투 가이드 사용법

1. 현재 겪고 있는 문제와 일치하는 가이드를 선택하세요
2. 가이드의 전제 조건을 확인하세요
3. 단계별 지침을 순서대로 따라가세요
4. 각 단계의 예상 결과를 확인하며 진행하세요
