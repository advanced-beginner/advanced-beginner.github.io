---
lastmod: "2026-01-16"
title: 느린 쿼리 최적화
weight: 1
---

검색 응답 시간이 느릴 때 원인을 진단하고 개선하는 방법을 안내합니다.

**소요 시간**: 약 15-30분

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: 쿼리 수준의 성능 최적화, Profile API 분석, 캐시 활용

**다루지 않는 내용**: 클러스터 수준 튜닝(노드 추가, 하드웨어 업그레이드)은 [성능 튜닝]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}})을 참조하세요.
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Profile API**로 쿼리 실행 계획 분석
- **불필요한 필드 제거**: `_source` 필터링, stored fields 활용
- **쿼리 유형 최적화**: `match` 대신 `term`, 와일드카드 앞자리 피하기
- **캐시 활용**: filter context 사용, request cache 활성화
{{< /callout >}}

---

## 시작하기 전에

다음 조건을 확인하세요:

| 항목 | 요구사항 | 확인 방법 |
|------|---------|----------|
| Elasticsearch 버전 | 7.x 이상 | `curl -X GET "localhost:9200"` |
| 클러스터 상태 | green 또는 yellow | `curl -X GET "localhost:9200/_cluster/health"` |
| 인덱스 접근 권한 | 읽기 + 설정 변경 | 아래 명령어로 테스트 |

```bash
# Elasticsearch 실행 상태 확인
curl -X GET "localhost:9200/_cluster/health?pretty"

# 정상 응답 예시: "status": "green" 또는 "yellow"
```

{{< callout type="warning" title="주의" >}}
Slow Log 설정 변경은 인덱스 설정 변경 권한이 필요합니다. 권한이 없으면 관리자에게 문의하세요.
{{< /callout >}}

---

## 증상

검색 요청의 응답 시간이 예상보다 느립니다:

```bash
# 응답에서 took 값 확인
curl -X GET "localhost:9200/products/_search" -H 'Content-Type: application/json' -d'
{
  "query": { "match_all": {} }
}'

# 응답: "took": 2500  ← 2.5초, 너무 느림
```

---

## 1단계: 현재 상태 측정

### 1.1 Profile API로 쿼리 분석

쿼리에 `profile: true`를 추가하여 실행 계획을 확인하세요:

```bash
curl -X GET "localhost:9200/products/_search" -H 'Content-Type: application/json' -d'
{
  "profile": true,
  "query": {
    "match": { "name": "삼성 갤럭시" }
  }
}'
```

**응답에서 확인할 것:**
```json
{
  "profile": {
    "shards": [{
      "searches": [{
        "query": [{
          "type": "BooleanQuery",
          "time_in_nanos": 1234567,  // 이 값이 크면 문제
          "breakdown": {
            "score": 100000,
            "build_scorer": 50000,
            "create_weight": 10000
          }
        }]
      }]
    }]
  }
}
```

### 1.2 Slow Log 활성화

느린 쿼리를 자동으로 로깅하도록 설정하세요:

```bash
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{
  "index.search.slowlog.threshold.query.warn": "1s",
  "index.search.slowlog.threshold.query.info": "500ms",
  "index.search.slowlog.threshold.fetch.warn": "500ms"
}'
```

로그 확인 위치: `logs/{cluster-name}_index_search_slowlog.log`

---

## 2단계: 쿼리 최적화

### 2.1 정확한 쿼리 유형 사용

| 상황 | 느린 쿼리 | 빠른 쿼리 |
|------|----------|----------|
| 정확한 값 검색 | `match` | `term` |
| 여러 값 중 하나 | 여러 `should` | `terms` |
| 범위 검색 | `script` | `range` |
| 존재 여부 | `script` | `exists` |

```json
// 느림: match는 분석기를 거침
{ "match": { "status": "active" } }

// 빠름: term은 정확한 값 검색
{ "term": { "status": "active" } }
```

### 2.2 와일드카드 최적화

```json
// 매우 느림: 앞에 와일드카드 (전체 스캔)
{ "wildcard": { "name": "*phone" } }

// 빠름: 뒤에만 와일드카드
{ "wildcard": { "name": "phone*" } }

// 더 빠름: prefix 쿼리 사용
{ "prefix": { "name": "phone" } }
```

### 2.3 Script 쿼리 피하기

```json
// 느림: 모든 문서에서 스크립트 실행
{
  "script": {
    "script": "doc['price'].value * doc['quantity'].value > 1000"
  }
}

// 빠름: 인덱싱 시 계산된 필드 추가
// Mapping에 total_value 필드 추가 후
{ "range": { "total_value": { "gt": 1000 } } }
```

---

## 3단계: 필터 컨텍스트 활용

### 3.1 filter vs query

`filter`는 점수 계산을 생략하고 캐싱됩니다:

```json
// 느림: 모든 조건에서 점수 계산
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "갤럭시" } },
        { "range": { "price": { "gte": 100000 } } },
        { "term": { "category": "phone" } }
      ]
    }
  }
}

// 빠름: 필터링 조건은 filter로 분리
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "갤럭시" } }
      ],
      "filter": [
        { "range": { "price": { "gte": 100000 } } },
        { "term": { "category": "phone" } }
      ]
    }
  }
}
```

### 3.2 Filter 순서 최적화

가장 많이 걸러내는 필터를 먼저 배치합니다:

```json
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "is_deleted": false } },  // 대부분 거름
        { "term": { "status": "active" } },   // 그 다음으로 거름
        { "range": { "price": { "gte": 100000 } } }  // 마지막
      ]
    }
  }
}
```

---

## 4단계: 결과 크기 최적화

### 4.1 _source 필터링

필요한 필드만 반환합니다:

```json
// 느림: 전체 문서 반환
{ "query": { "match_all": {} } }

// 빠름: 필요한 필드만
{
  "_source": ["name", "price"],
  "query": { "match_all": {} }
}

// 더 빠름: _source 비활성화
{
  "_source": false,
  "fields": ["name", "price"],
  "query": { "match_all": {} }
}
```

### 4.2 페이지네이션 최적화

```json
// 느림: 깊은 페이지네이션
{ "from": 10000, "size": 10 }  // 10,000개를 읽고 버림

// 빠름: search_after 사용
{
  "size": 10,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-01T00:00:00", "abc123"]
}
```

---

## 5단계: 인덱스 수준 최적화

### 5.1 샤드 수 조정

인덱스당 샤드가 너무 많으면 오버헤드가 증가합니다:

```bash
# 현재 샤드 수 확인
curl -X GET "localhost:9200/_cat/shards/products?v"

# 권장: 샤드당 20-40GB
# 10GB 데이터면 샤드 1개로 충분
```

### 5.2 Refresh Interval 조정

{{< callout type="warning" title="주의" >}}
refresh_interval을 `-1`로 설정하면 새로 인덱싱된 문서가 검색되지 않습니다. 반드시 작업 완료 후 원래 값으로 복구하세요.
{{< /callout >}}

인덱싱이 많은 시간에는 refresh를 줄이세요:

```bash
# 배치 인덱싱 중 refresh 비활성화
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{ "refresh_interval": "-1" }'

# 완료 후 복구
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{ "refresh_interval": "1s" }'

# 수동 refresh
curl -X POST "localhost:9200/products/_refresh"
```

---

## 체크리스트

느린 쿼리 최적화 시 확인사항:

- [ ] **Profile API로 분석했는가?** - 병목 지점 파악
- [ ] **적절한 쿼리 유형인가?** - term vs match, 와일드카드 위치
- [ ] **filter context를 활용했는가?** - 점수 계산 불필요한 조건 분리
- [ ] **_source 필터링을 했는가?** - 필요한 필드만 반환
- [ ] **페이지네이션이 적절한가?** - 깊은 페이지는 search_after 사용
- [ ] **캐시가 활용되고 있는가?** - filter 캐시, request cache 확인

---

## 성공 확인

최적화가 성공했는지 다음 방법으로 확인하세요:

1. **took 값 비교**: 동일한 쿼리의 `took` 값이 50% 이상 감소했는지 확인
   ```bash
   # 최적화 전후 took 값 비교
   curl -X GET "localhost:9200/products/_search?pretty" -H 'Content-Type: application/json' -d'
   {
     "query": { "match": { "name": "테스트" } }
   }' | grep took
   ```

2. **Profile API 비교**: `time_in_nanos` 값이 감소했는지 확인

3. **Slow Log 확인**: 설정한 임계값(예: 500ms)을 초과하는 쿼리가 더 이상 기록되지 않는지 확인

{{< callout type="tip" title="성공 기준" >}}
- took 값이 기존 대비 50% 이상 감소
- Slow Log에 새로운 경고가 기록되지 않음
- 사용자 체감 응답 시간 개선
{{< /callout >}}

---

## 자주 발생하는 오류

### "index_not_found_exception"

```json
{
  "error": {
    "type": "index_not_found_exception",
    "reason": "no such index [products]"
  }
}
```

**원인**: 인덱스 이름이 잘못되었거나 존재하지 않음

**해결**: 인덱스 목록을 확인하세요:
```bash
curl -X GET "localhost:9200/_cat/indices?v"
```

### "search_phase_execution_exception"

```json
{
  "error": {
    "type": "search_phase_execution_exception",
    "reason": "all shards failed"
  }
}
```

**원인**: 쿼리 문법 오류 또는 매핑과 맞지 않는 쿼리

**해결**:
1. 쿼리 JSON 문법을 검증하세요
2. 인덱스 매핑을 확인하세요: `curl -X GET "localhost:9200/products/_mapping?pretty"`

### 권한 오류 (403 Forbidden)

**원인**: 인덱스 읽기 또는 설정 변경 권한 부족

**해결**: 클러스터 관리자에게 다음 권한을 요청하세요:
- `read` - 검색 실행
- `manage` - Slow Log 설정 변경

---

## 관련 문서

- [Query DSL](../concepts/query-dsl/) - 쿼리 유형별 특징
- [성능 튜닝](../concepts/performance-tuning/) - 클러스터 수준 튜닝
- [메모리 문제 해결](memory-troubleshooting/) - OOM 발생 시 대응
