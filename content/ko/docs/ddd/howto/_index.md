---
bookCollapseSection: true
title: How-To Guide
description: "DDD 설계 문제 해결 가이드 목록입니다."
weight: 4
lastmod: "2026-01-13"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **대상 독자**: DDD 기본 개념(Entity, Value Object, Aggregate)을 이해한 개발자
> **선수 지식**: [전술적 설계](../concepts/tactical-design/) 문서를 읽었거나 DDD 빌딩 블록에 대한 기본 지식
> **이 섹션의 목적**: DDD 적용 시 마주치는 구체적인 문제를 단계별로 해결

{{< callout type="info" title="How-To Guide란?" >}}
How-To Guide는 <strong>특정 문제를 해결</strong>하기 위한 실용적 지침입니다. "왜"보다는 "어떻게"에 집중하며, 각 가이드는 명확한 문제 상황에서 시작하여 검증 가능한 해결책으로 끝납니다. 개념 이해가 필요하다면 먼저 [개념 이해](../concepts/) 섹션을 참고하세요.
{{< /callout >}}

## 이 섹션에서 다루는 것

| 가이드 | 해결하는 문제 | 소요 시간 |
|--------|--------------|----------|
| [Aggregate 경계 정하기](aggregate-boundaries/) | "이 Entity들을 하나의 Aggregate로 묶어야 할까?" | 약 30분 |
| [Bounded Context 식별하기](bounded-context-identification/) | "시스템을 어떻게 나눠야 할지 모르겠다" | 약 25분 |
| [Value Object 설계하기](value-object-design/) | "Entity와 Value Object를 어떻게 구분할까?" | 약 20분 |
| [도메인 이벤트 설계하기](domain-event-design/) | "Aggregate 간 통신을 어떻게 해야 할까?" | 약 25분 |

## 이 섹션에서 다루지 않는 것

- DDD 개념 설명 → [개념 이해](../concepts/) 참고
- 전체 프로젝트 구현 예제 → [실습 예제](../examples/) 참고
- 용어 정의 → [용어 사전](../appendix/glossary/) 참고

## 가이드 활용 방법

1. **문제 식별**: 현재 겪고 있는 문제와 가장 유사한 가이드를 선택합니다
2. **전제 조건 확인**: 각 가이드 상단의 선수 지식을 확인합니다
3. **단계별 진행**: 순서대로 따라하며 각 단계의 검증 포인트를 확인합니다
4. **적용**: 자신의 프로젝트에 맞게 조정하여 적용합니다
