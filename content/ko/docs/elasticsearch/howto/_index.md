---
lastmod: "2026-01-16"
title: How-To Guide
description: "Elasticsearch 운영 문제 해결 가이드 목록입니다."
weight: 4
bookCollapseSection: true
---

실무에서 자주 발생하는 Elasticsearch 관련 문제를 해결하는 방법을 단계별로 안내합니다.

{{< callout type="info" title="가이드 사용법" >}}
각 가이드는 특정 문제 상황에서 즉시 적용할 수 있도록 작성되었습니다. "시작하기 전에" 섹션에서 전제 조건을 확인한 후 단계별로 따라하세요.
{{< /callout >}}

## 트러블슈팅

| 가이드 | 설명 | 난이도 | 소요 시간 |
|--------|------|--------|----------|
| [느린 쿼리 최적화]({{< relref "/docs/elasticsearch/howto/slow-query-optimization" >}}) | 검색 응답 시간이 느릴 때 진단하고 개선하는 방법 | 중급 | 15-30분 |
| [메모리 문제 해결]({{< relref "/docs/elasticsearch/howto/memory-troubleshooting" >}}) | OOM, GC 문제가 발생할 때 대응하는 방법 | 중급 | 20-40분 |

## 인덱스 관리

| 가이드 | 설명 | 난이도 | 소요 시간 |
|--------|------|--------|----------|
| [매핑 마이그레이션]({{< relref "/docs/elasticsearch/howto/mapping-migration" >}}) | 다운타임 없이 인덱스 매핑을 변경하는 방법 | 중급 | 20-40분 |
| [인덱스 재구축]({{< relref "/docs/elasticsearch/howto/index-rebuild" >}}) | 대용량 인덱스를 효율적으로 재구축하는 방법 | 중급 | 30-60분 |

## 클러스터 운영

| 가이드 | 설명 | 난이도 | 소요 시간 |
|--------|------|--------|----------|
| [클러스터 확장]({{< relref "/docs/elasticsearch/howto/cluster-scaling" >}}) | Elasticsearch 클러스터를 안전하게 확장하는 방법 | 고급 | 30-60분 |
