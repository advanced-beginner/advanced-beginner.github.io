---
title: FAQ
weight: 2
---

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

## 에러

### "index read-only / allow delete" 에러

디스크 사용률이 95% 초과하면 발생.

```json
// 임시 해제
PUT /products/_settings
{
  "index.blocks.read_only_allow_delete": null
}

// 디스크 정리 후 영구 해결
```

### "Result window is too large" 에러

`from + size` > 10,000 일 때 발생.

```json
// 방법 1: 제한 완화 (비권장)
PUT /products/_settings
{
  "index.max_result_window": 50000
}

// 방법 2: search_after 사용 (권장)
```

### "mapper_parsing_exception" 에러

필드 타입과 맞지 않는 데이터 입력.

```
// 예: integer 필드에 문자열 입력
"price": "천원"  // 에러!
"price": 1000    // 정상
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
