---
title: FAQ
weight: 2
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **ES vs RDB**: 풀텍스트 검색/로그 분석은 ES, 트랜잭션/JOIN은 RDB
- **text vs keyword**: text는 검색용(분석됨), keyword는 필터/정렬용(분석 안 됨)
- **성능 문제**: Filter Context 사용, 필요한 필드만 반환, `search_after` 페이지네이션
- **한글 검색**: Nori 분석기 설치 필수
- **흔한 에러**: 대부분 디스크 부족, 메모리 부족, 타입 불일치가 원인
{{< /callout >}}

자주 묻는 질문과 답변을 정리했습니다.

## 기본 개념

### Elasticsearch vs RDB, 언제 무엇을 써야 하나요?

| 상황 | 권장 |
|------|------|
| 풀텍스트 검색 | Elasticsearch |
| 트랜잭션 무결성 필요 | RDB |
| 복잡한 JOIN | RDB |
| 실시간 집계/분석 | Elasticsearch |
| 일반 CRUD | RDB |
| 대용량 로그 저장 | Elasticsearch |

**일반적인 패턴:** RDB를 메인 저장소로, Elasticsearch를 검색용 보조 저장소로 사용.

### text vs keyword 타입의 차이는?

| 특성 | text | keyword |
|------|------|---------|
| 분석 | O (토큰화) | X |
| 검색 방식 | match 쿼리 | term 쿼리 |
| 정렬/집계 | 불가 | 가능 |
| 용도 | 풀텍스트 검색 | 정확한 값 매칭, 필터 |

```json
// text: "삼성전자" → ["삼성", "전자"]
// keyword: "삼성전자" → "삼성전자"
```

### 샤드 수는 어떻게 정해야 하나요?

- **Rule of Thumb:** 샤드당 20-40GB
- 너무 적으면: 병렬 처리 효율 저하
- 너무 많으면: 오버헤드 증가, 메모리 부족

```
# 예시: 100GB 데이터
권장 Primary 샤드: 3-5개
```

---

## 성능

### 검색이 느린데 어떻게 최적화하나요?

1. **Filter Context 사용**: Score 불필요한 조건은 `filter`로
2. **필요한 필드만 반환**: `_source` 지정
3. **페이지네이션 최적화**: 깊은 페이지는 `search_after`
4. **캐시 활용**: 자주 사용하는 필터는 자동 캐싱
5. **인덱스 설계 검토**: 적절한 샤드 수, 불필요한 필드 제외

### Refresh Interval을 늘리면 어떤 영향이 있나요?

| 값 | 영향 |
|----|------|
| 짧게 (1s) | 실시간 검색, 인덱싱 부하 증가 |
| 길게 (30s) | 인덱싱 성능 향상, 검색 지연 |
| -1 | 수동 Refresh만, 대량 인덱싱에 유용 |

### JVM Heap은 얼마로 설정해야 하나요?

- 시스템 메모리의 50% (최대 30-31GB)
- 최소(-Xms)와 최대(-Xmx) 동일하게
- 나머지는 파일 시스템 캐시용으로 남겨둠

---

## 운영

### 클러스터 상태가 Yellow인데 괜찮나요?

**개발 환경 (단일 노드):** 정상입니다. Replica를 할당할 다른 노드가 없어서 Yellow.

**프로덕션:** 노드를 추가하거나, Replica 수를 줄여야 합니다.

```json
// Replica 0으로 설정 (개발용)
PUT /products/_settings
{ "number_of_replicas": 0 }
```

### 디스크 공간이 부족하면 어떻게 되나요?

| 사용률 | 동작 |
|--------|------|
| 85% | 새 샤드 할당 중지 |
| 90% | 해당 노드에 샤드 할당 완전 중지 |
| 95% | 인덱스 read-only 전환 |

**대응:** 오래된 데이터 삭제, 노드 추가, ILM 설정

### 인덱스 Mapping을 변경하고 싶은데요?

기존 필드 타입은 변경 불가. **재인덱싱** 필요:

```json
// 1. 새 인덱스 생성 (새 Mapping)
PUT /products-v2

// 2. 데이터 복사
POST /_reindex
{
  "source": { "index": "products-v1" },
  "dest": { "index": "products-v2" }
}

// 3. Alias 전환
POST /_aliases
{
  "actions": [
    { "remove": { "index": "products-v1", "alias": "products" } },
    { "add": { "index": "products-v2", "alias": "products" } }
  ]
}
```

---

## 검색

### 한글 검색이 잘 안 되는데요?

기본 `standard` analyzer는 한글 형태소 분석을 하지 않습니다.
**Nori 분석기**를 설치하고 설정하세요.

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "korean": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  }
}
```

### 오타를 허용하는 검색은 어떻게 하나요?

**Fuzzy 검색** 사용:

```json
{
  "query": {
    "match": {
      "name": {
        "query": "맥뷱",
        "fuzziness": "AUTO"
      }
    }
  }
}
```

### 자동완성은 어떻게 구현하나요?

1. **Edge N-gram**: 접두사 매칭 (권장)
2. **Completion Suggester**: 전용 자료구조 (빠름)
3. **match_phrase_prefix**: 간단하지만 성능 주의

---

## 에러와 트러블슈팅

### 1. "index read-only / allow delete" 에러

**증상:**
```
ClusterBlockException: index [products] blocked by: [FORBIDDEN/12/index read-only / allow delete (api)]
```

**원인:** 디스크 사용률 95% 초과 시 자동으로 인덱스가 읽기 전용으로 전환됨

**해결:**
```json
// 1. 디스크 공간 확보 후
// 2. read-only 해제
PUT /products/_settings
{
  "index.blocks.read_only_allow_delete": null
}

// 모든 인덱스 일괄 해제
PUT /_all/_settings
{
  "index.blocks.read_only_allow_delete": null
}
```

**예방:** 디스크 사용률 80%에서 알림 설정

---

### 2. "Result window is too large" 에러

**증상:**
```
IllegalArgumentException: Result window is too large, from + size must be less than or equal to: [10000]
```

**원인:** 기본적으로 `from + size` 합계가 10,000 초과 불가

**해결:**
```json
// 방법 1: search_after 사용 (권장)
GET /products/_search
{
  "size": 100,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-15T10:00:00", "abc123"]
}

// 방법 2: Scroll API (대량 내보내기용)
POST /products/_search?scroll=1m
{ "size": 1000, "query": { "match_all": {} } }

// 방법 3: 제한 완화 (비권장 - 메모리 부담)
PUT /products/_settings
{ "index.max_result_window": 50000 }
```

---

### 3. "mapper_parsing_exception" 에러

**증상:**
```
MapperParsingException: failed to parse field [price] of type [integer]
```

**원인:** 필드 타입과 맞지 않는 데이터 입력

**해결:**
```json
// 잘못된 예
{ "price": "천원" }     // integer 필드에 문자열
{ "price": 1000.5 }     // integer 필드에 소수

// 올바른 예
{ "price": 1000 }
```

**예방:** `dynamic: strict` 설정으로 예기치 않은 필드 방지

---

### 4. "circuit_breaking_exception" 에러

**증상:**
```
CircuitBreakingException: [parent] Data too large, data for [<http_request>] would be [xxx/xxxgb]
```

**원인:** 쿼리가 너무 많은 메모리 사용 시도

**해결:**
```json
// 1. 쿼리 최적화 (집계 크기 줄이기)
{
  "aggs": {
    "categories": {
      "terms": {
        "field": "category",
        "size": 100  // 10000 → 100으로 축소
      }
    }
  }
}

// 2. Heap 메모리 증가 (jvm.options)
-Xms4g
-Xmx4g
```

---

### 5. "rejected execution" 에러

**증상:**
```
EsRejectedExecutionException: rejected execution of search on EsThreadPoolExecutor
```

**원인:** 검색/인덱싱 요청이 스레드풀 큐 용량 초과

**해결:**
```json
// 1. 노드 추가로 부하 분산
// 2. 클라이언트 측 재시도 로직 구현
// 3. Bulk 요청 크기 조정

// 스레드풀 상태 확인
GET /_cat/thread_pool?v&h=node_name,name,active,queue,rejected
```

---

### 6. "ClusterBlockException: no master" 에러

**증상:**
```
MasterNotDiscoveredException: null
ClusterBlockException: blocked by: [SERVICE_UNAVAILABLE/2/no master]
```

**원인:** Master 노드 선출 실패 (네트워크 분리, 노드 장애)

**해결:**
```bash
# 1. 노드 상태 확인
GET /_cat/nodes?v

# 2. 클러스터 상태 확인
GET /_cluster/health

# 3. Master eligible 노드가 과반수 이상인지 확인
# 권장: 3개 이상의 Master eligible 노드
```

---

### 7. "shard failed" / "all shards failed" 에러

**증상:**
```
SearchPhaseExecutionException: all shards failed
```

**원인:** 검색 대상 샤드 모두 실패 (노드 다운, 샤드 손상)

**해결:**
```json
// 1. 클러스터 상태 확인
GET /_cluster/health?level=shards

// 2. 미할당 샤드 원인 확인
GET /_cluster/allocation/explain

// 3. 특정 인덱스 샤드 상태 확인
GET /_cat/shards/products?v&h=index,shard,prirep,state,node,unassigned.reason
```

---

### 8. "version conflict" 에러

**증상:**
```
VersionConflictEngineException: [1]: version conflict, current version [5] is different than the one provided [4]
```

**원인:** 낙관적 락(Optimistic Locking) 충돌 - 동시 수정 시도

**해결:**
```json
// 방법 1: retry_on_conflict 사용
POST /products/_update/1?retry_on_conflict=3
{
  "doc": { "price": 2000000 }
}

// 방법 2: 명시적 버전 지정 대신 if_seq_no 사용
PUT /products/_doc/1?if_seq_no=10&if_primary_term=1
{
  "name": "맥북 프로",
  "price": 2000000
}
```

---

### 9. "connection refused" / "connection timeout" 에러

**증상:**
```
ConnectException: Connection refused: localhost:9200
java.net.SocketTimeoutException: connect timed out
```

**원인:** Elasticsearch가 실행되지 않음 / 네트워크 문제

**해결:**
```bash
# 1. ES 프로세스 확인
ps aux | grep elasticsearch
docker ps | grep elasticsearch

# 2. 포트 리스닝 확인
lsof -i :9200
netstat -tlnp | grep 9200

# 3. 방화벽 확인
sudo iptables -L -n | grep 9200

# 4. ES 로그 확인
tail -f /var/log/elasticsearch/elasticsearch.log
docker logs elasticsearch
```

---

### 10. "OOM (OutOfMemoryError)" 에러

**증상:**
```
java.lang.OutOfMemoryError: Java heap space
```

**원인:** JVM Heap 메모리 부족

**해결:**
```bash
# 1. Heap 크기 증가 (jvm.options)
-Xms8g
-Xmx8g

# 주의: 시스템 메모리의 50% 이하, 최대 30-31GB

# 2. 메모리 사용량 확인
GET /_nodes/stats/jvm

# 3. 필드데이터 캐시 확인 (text 필드 집계 시 문제)
GET /_nodes/stats/indices/fielddata

# 4. 필드데이터 캐시 제한
PUT /_cluster/settings
{
  "persistent": {
    "indices.fielddata.cache.size": "20%"
  }
}
```

---

### 11. "no such index" 에러

**증상:**
```
IndexNotFoundException: no such index [products]
```

**원인:** 존재하지 않는 인덱스 접근

**해결:**
```json
// 1. 인덱스 존재 여부 확인
HEAD /products

// 2. 인덱스 목록 확인
GET /_cat/indices?v

// 3. Alias 확인 (Alias로 접근 시)
GET /_cat/aliases?v
```

---

### 12. "illegal_argument_exception: Text fields are not optimised for operations" 에러

**증상:**
```
IllegalArgumentException: Text fields are not optimised for operations that require per-document field data like aggregations and sorting
```

**원인:** text 타입 필드로 정렬/집계 시도

**해결:**
```json
// 방법 1: keyword 서브필드 사용
GET /products/_search
{
  "sort": [{ "name.keyword": "asc" }],
  "aggs": {
    "names": { "terms": { "field": "name.keyword" } }
  }
}

// 방법 2: 처음부터 keyword 타입으로 정의
{
  "mappings": {
    "properties": {
      "category": { "type": "keyword" }
    }
  }
}
```

---

## 보안

### 프로덕션에서 보안 설정 없이 운영해도 되나요?

**절대 안 됩니다!** Elasticsearch 8.x부터 보안이 기본 활성화.

최소 설정:
1. TLS/SSL 암호화
2. 사용자 인증
3. 역할 기반 접근 제어

### 외부에서 9200 포트에 직접 접근해도 되나요?

**안 됩니다.**

- API Gateway나 프록시 뒤에 배치
- 방화벽으로 내부망만 허용
- 인증 필수
