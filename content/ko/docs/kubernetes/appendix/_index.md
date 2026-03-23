---
bookCollapseSection: true
lastmod: "2026-01-11"
title: 부록
description: "Kubernetes 부록 자료와 참고 문서 목록입니다."
weight: 5
author:
  name: Advanced Beginner
  github: advanced-beginner
---

이 섹션에서는 Kubernetes 학습과 운영에 도움이 되는 보조 자료들을 제공합니다.

## 상황별 부록 활용 가이드

어떤 자료를 봐야 할지 모르겠다면 아래 가이드를 참고하세요.

```mermaid
flowchart LR
    Q[질문 발생] --> T{질문 유형?}
    T -->|용어/개념| G[용어 사전]
    T -->|오류/문제| F[FAQ]
    T -->|심화 학습| R[참고 자료]
```

| 상황 | 추천 자료 | 활용 예시 |
|------|----------|----------|
| 모르는 용어가 나왔을 때 | [용어 사전]({{< relref "/docs/kubernetes/appendix/glossary" >}}) | "PVC가 뭐지?" → PersistentVolumeClaim 정의 확인 |
| 막히거나 오류가 발생했을 때 | [FAQ]({{< relref "/docs/kubernetes/appendix/faq" >}}) | "Pod가 Pending 상태야" → 원인과 해결책 확인 |
| 더 깊이 공부하고 싶을 때 | [참고 자료]({{< relref "/docs/kubernetes/appendix/references" >}}) | "CKA 준비하려면?" → 자격증/학습 자료 확인 |

## 부록 목록

| 자료 | 설명 | 추천 대상 |
|------|------|----------|
| [용어 사전]({{< relref "/docs/kubernetes/appendix/glossary" >}}) | Kubernetes 핵심 용어 빠른 참조 | 모든 학습자 |
| [FAQ]({{< relref "/docs/kubernetes/appendix/faq" >}}) | 자주 묻는 질문과 답변 | 초보자, 트러블슈팅 시 |
| [참고 자료]({{< relref "/docs/kubernetes/appendix/references" >}}) | 공식 문서 및 추가 학습 자료 링크 | 심화 학습자 |
