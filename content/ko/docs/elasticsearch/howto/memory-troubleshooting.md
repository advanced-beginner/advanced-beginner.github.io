---
lastmod: "2026-01-16"
title: 메모리 문제 해결
description: "Elasticsearch 메모리 문제를 진단하고 해결하는 방법을 안내합니다."
weight: 2
---

OutOfMemoryError나 GC 문제가 발생할 때 원인을 진단하고 해결하는 방법을 안내합니다.

**소요 시간**: 약 20-40분 (GC 로그 분석 시 추가 10분)

{{< callout type="info" title="이 가이드의 범위" >}}
**다루는 내용**: 힙 메모리 설정, Circuit Breaker, Field Data 최적화, GC 튜닝

**다루지 않는 내용**: 노드 추가, 하드웨어 업그레이드는 [클러스터 관리]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})를 참조하세요.
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **힙 메모리**: 전체 메모리의 50% 이하, 최대 31GB
- **Circuit Breaker**: 메모리 과사용 방지 설정 확인
- **필드 데이터**: text 필드 집계 피하기, doc_values 활용
- **GC 튜닝**: G1GC 사용, 로그 분석으로 문제 파악
{{< /callout >}}

---

## 시작하기 전에

다음 조건을 확인하세요:

| 항목 | 요구사항 | 확인 방법 |
|------|---------|----------|
| 서버 접근 권한 | SSH 또는 콘솔 접근 | 서버에 로그인 가능 |
| jvm.options 수정 권한 | root 또는 elasticsearch 사용자 | 아래 경로 확인 |
| ES 재시작 권한 | 서비스 재시작 가능 | `systemctl restart elasticsearch` |

**jvm.options 파일 위치**:

| 설치 방식 | 경로 |
|----------|------|
| Debian/Ubuntu (apt) | `/etc/elasticsearch/jvm.options` |
| RPM/CentOS (yum) | `/etc/elasticsearch/jvm.options` |
| tar.gz 압축 해제 | `{ES_HOME}/config/jvm.options` |
| Docker | 환경 변수 `ES_JAVA_OPTS` 사용 |

```bash
# jvm.options 파일 위치 확인
ls -la /etc/elasticsearch/jvm.options 2>/dev/null || \
ls -la $ES_HOME/config/jvm.options 2>/dev/null || \
echo "jvm.options 파일을 찾을 수 없습니다"
```

{{< callout type="warning" title="주의" >}}
jvm.options 변경 후 Elasticsearch를 재시작해야 합니다. 운영 환경에서는 롤링 재시작을 권장합니다.
{{< /callout >}}

---

## 증상

다음과 같은 문제가 발생합니다:

**OutOfMemoryError:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Circuit Breaker 발동:**
```json
{
  "error": {
    "type": "circuit_breaking_exception",
    "reason": "[parent] Data too large, data for [<query>] would be larger than limit of [xxx/yyy]"
  }
}
```

**GC 오버헤드:**
```
GC overhead limit exceeded
```

---

## 1단계: 현재 메모리 상태 확인

### 1.1 노드별 메모리 사용량

```bash
# 노드별 힙 메모리 상태
curl -X GET "localhost:9200/_cat/nodes?v&h=name,heap.percent,heap.current,heap.max"

# 예시 출력:
# name    heap.percent heap.current heap.max
# node-1  75           11.2gb       16gb
```

### 1.2 상세 메모리 분석

```bash
# 전체 노드 통계
curl -X GET "localhost:9200/_nodes/stats/jvm?pretty"

# Circuit Breaker 상태
curl -X GET "localhost:9200/_nodes/stats/breaker?pretty"
```

**확인 포인트:**
- `heap.percent` > 85%: 위험 수준
- `fielddata` 사용량이 높으면 집계 쿼리 문제
- `request` breaker가 자주 발동하면 쿼리 최적화 필요

---

## 2단계: 힙 메모리 설정 최적화

### 2.1 적절한 힙 크기

```bash
# jvm.options 파일 수정
# 위치: /etc/elasticsearch/jvm.options 또는 config/jvm.options

# 권장 설정 (16GB 시스템 예시)
-Xms8g
-Xmx8g
```

**힙 메모리 가이드라인:**

| 시스템 메모리 | 힙 권장값 | 남은 메모리 용도 |
|-------------|----------|-----------------|
| 8GB | 4GB | OS 캐시, Lucene |
| 16GB | 8GB | OS 캐시, Lucene |
| 32GB | 16GB | OS 캐시, Lucene |
| 64GB | 31GB | OS 캐시, Lucene |

{{< callout type="warning" title="주의" >}}
힙을 32GB 이상 설정하면 Compressed OOPs가 비활성화되어 오히려 성능이 저하됩니다. 최대 31GB를 권장합니다.
{{< /callout >}}

### 2.2 Xms와 Xmx 동일하게

```bash
# 잘못된 설정: 힙 크기 변동으로 GC 부담 증가
-Xms4g
-Xmx16g

# 올바른 설정: 고정 크기
-Xms8g
-Xmx8g
```

---

## 3단계: Circuit Breaker 조정

### 3.1 Breaker 설정 확인

```bash
curl -X GET "localhost:9200/_cluster/settings?include_defaults=true&filter_path=**.breaker"
```

### 3.2 Breaker 한도 조정

```bash
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "indices.breaker.total.limit": "70%",
    "indices.breaker.fielddata.limit": "40%",
    "indices.breaker.request.limit": "40%"
  }
}'
```

| Breaker | 기본값 | 역할 |
|---------|--------|------|
| `total` | 70% | 전체 메모리 한도 |
| `fielddata` | 40% | 필드 데이터 캐시 |
| `request` | 60% | 단일 요청 메모리 |
| `in_flight_requests` | 100% | 전송 중인 요청 |

---

## 4단계: Field Data 최적화

### 4.1 문제 원인

text 필드에 대한 집계나 정렬은 fielddata를 메모리에 로드합니다:

```json
// 위험: text 필드로 집계
{
  "aggs": {
    "categories": {
      "terms": { "field": "category" }  // category가 text면 문제
    }
  }
}
```

### 4.2 해결 방법

**방법 1: keyword 필드 사용**

```json
// Mapping 설정
{
  "mappings": {
    "properties": {
      "category": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      }
    }
  }
}

// 집계 시 keyword 사용
{
  "aggs": {
    "categories": {
      "terms": { "field": "category.keyword" }
    }
  }
}
```

**방법 2: doc_values 활용**

```json
// Mapping에서 doc_values 활성화 (keyword는 기본 활성화)
{
  "mappings": {
    "properties": {
      "status": {
        "type": "keyword",
        "doc_values": true
      }
    }
  }
}
```

### 4.3 Field Data 캐시 제한

```bash
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "indices.fielddata.cache.size": "20%"
  }
}'
```

---

## 5단계: GC 최적화

### 5.1 G1GC 설정 (권장)

```bash
# jvm.options
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1ReservePercent=25
-XX:InitiatingHeapOccupancyPercent=30
```

### 5.2 GC 로그 분석

```bash
# GC 로그 위치 확인
ls /var/log/elasticsearch/gc.log*

# GC 로그 활성화 (jvm.options)
-Xlog:gc*,gc+age=trace,safepoint:file=/var/log/elasticsearch/gc.log:utctime,pid,tags:filecount=32,filesize=64m
```

**분석 도구:**
- GCViewer: `java -jar gcviewer.jar gc.log`
- GCEasy: https://gceasy.io (온라인)

### 5.3 GC 문제 패턴

| 증상 | 원인 | 해결 |
|------|------|------|
| 빈번한 Young GC | 객체 생성이 많음 | 쿼리 최적화, 배치 크기 조정 |
| 긴 Full GC | 힙 부족 | 힙 증가 또는 데이터 정리 |
| GC 후에도 메모리 부족 | 메모리 누수 | 힙 덤프 분석 |

---

## 6단계: 쿼리 수준 최적화

### 6.1 큰 결과셋 피하기

```json
// 위험: 너무 많은 결과
{ "size": 10000 }

// 안전: 페이지네이션 사용
{ "size": 100, "from": 0 }

// 대량 데이터: Scroll API 사용
curl -X POST "localhost:9200/products/_search?scroll=1m" -H 'Content-Type: application/json' -d'
{
  "size": 1000,
  "query": { "match_all": {} }
}'
```

### 6.2 집계 최적화

```json
// 위험: 카디널리티가 높은 집계
{
  "aggs": {
    "all_users": {
      "terms": { "field": "user_id", "size": 1000000 }
    }
  }
}

// 안전: 적절한 크기 제한
{
  "aggs": {
    "top_users": {
      "terms": { "field": "user_id", "size": 100 }
    }
  }
}
```

---

## 체크리스트

메모리 문제 해결 시 확인사항:

- [ ] **힙 크기가 적절한가?** - 시스템 메모리의 50%, 최대 31GB
- [ ] **Xms와 Xmx가 동일한가?** - 힙 크기 변동 방지
- [ ] **text 필드로 집계하고 있지 않은가?** - keyword 또는 doc_values 사용
- [ ] **Circuit Breaker가 적절한가?** - 너무 높으면 OOM, 너무 낮으면 쿼리 실패
- [ ] **GC 로그를 분석했는가?** - 패턴 파악
- [ ] **불필요한 인덱스가 있는가?** - 오래된 인덱스 정리

---

## 성공 확인

메모리 문제가 해결되었는지 다음 방법으로 확인하세요:

1. **힙 사용률 확인**: `heap.percent`가 75% 이하로 안정적으로 유지되는지 확인
   ```bash
   # 힙 사용률 모니터링 (5초 간격으로 10회)
   for i in {1..10}; do
     curl -s "localhost:9200/_cat/nodes?v&h=name,heap.percent" && sleep 5
   done
   ```

2. **Circuit Breaker 확인**: 더 이상 breaker가 발동하지 않는지 확인
   ```bash
   curl -X GET "localhost:9200/_nodes/stats/breaker?pretty" | grep tripped
   ```

3. **OOM 로그 확인**: 새로운 OutOfMemoryError가 발생하지 않는지 확인
   ```bash
   # 최근 로그에서 OOM 검색
   grep -i "OutOfMemory" /var/log/elasticsearch/*.log | tail -5
   ```

{{< callout type="tip" title="성공 기준" >}}
- `heap.percent`가 75% 이하로 안정
- Circuit Breaker `tripped` 값이 증가하지 않음
- 24시간 동안 OOM 미발생
{{< /callout >}}

---

## 자주 발생하는 오류

### jvm.options 문법 오류

**증상**: Elasticsearch가 시작되지 않음

```
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
```

**원인**: jvm.options 파일에 잘못된 옵션이 있음

**해결**:
1. jvm.options 파일의 문법을 확인하세요
2. 각 옵션이 새 줄에 있는지 확인하세요
3. 공백이나 특수문자가 없는지 확인하세요

```bash
# 올바른 형식
-Xms8g
-Xmx8g

# 잘못된 형식 (공백 포함)
-Xms 8g
-Xmx=8g
```

### Elasticsearch 시작 실패 (메모리 부족)

**증상**: 서비스가 시작되지 않음

```
[ERROR] bootstrap checks failed
max virtual memory areas vm.max_map_count [65530] is too low
```

**해결**:
```bash
# 임시 설정
sudo sysctl -w vm.max_map_count=262144

# 영구 설정
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

### Docker 환경에서 메모리 제한

**증상**: 컨테이너가 OOM으로 종료됨

**해결**: Docker 실행 시 메모리 제한과 ES_JAVA_OPTS를 함께 설정하세요:
```bash
docker run -d \
  --memory="4g" \
  -e ES_JAVA_OPTS="-Xms2g -Xmx2g" \
  elasticsearch:8.x
```

---

## 관련 문서

- [클러스터 관리]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}}) - 노드 구성 및 모니터링
- [성능 튜닝]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}}) - 전체적인 성능 최적화
- [느린 쿼리 최적화]({{< relref "/docs/elasticsearch/howto/slow-query-optimization" >}}) - 쿼리 수준 최적화
