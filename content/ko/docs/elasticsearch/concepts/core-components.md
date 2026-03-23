---
title: 핵심 구성요소
description: "Index, Shard, Node 등 Elasticsearch 핵심 구성요소를 설명합니다."
weight: 1
lastmod: 2026-01-15
---

{{< callout type="tip" title="TL;DR" >}}
- **Cluster**: 여러 노드를 묶어 고가용성과 분산 처리를 제공하는 서버 그룹
- **Node**: 클러스터를 구성하는 단일 Elasticsearch 서버 (Master, Data, Coordinating 역할)
- **Index**: 문서들의 논리적 모음 (RDB의 테이블과 유사)
- **Document**: JSON 형태의 데이터 단위 (RDB의 Row와 유사)
- **Shard**: 인덱스를 수평 분할한 조각 (Primary/Replica로 구성)
{{< /callout >}}

**대상 독자**: Elasticsearch를 처음 접하는 개발자
**선수 지식**: JSON 기본 문법, REST API 개념

**소요 시간**: 약 25-30분

Elasticsearch의 핵심 구성요소인 Cluster, Node, Index, Document, Shard의 역할과 관계를 이해합니다.

## 전체 비유: 대형 도서관 체인

Elasticsearch 구성요소를 **대형 도서관 체인**에 비유하면 이해하기 쉽습니다:

| 도서관 비유 | Elasticsearch | 역할 |
|------------|---------------|------|
| 도서관 체인 본사 | Cluster | 모든 지점을 통합 관리 |
| 각 지점 건물 | Node | 실제 서버 (책 보관, 대출 처리) |
| 분야별 서가 (소설, 과학) | Index | 비슷한 종류의 문서 모음 |
| 책 한 권 | Document | JSON 형태의 개별 데이터 |
| 같은 책이 여러 지점에 분산 | Shard | 데이터를 조각내어 분산 저장 |
| 복본 (인기 도서 여러 부) | Replica | 읽기 성능 향상, 장애 대비 |
| 색인 카드 (단어→책 목록) | Inverted Index | 빠른 검색을 위한 역색인 |

**핵심 원리**: 한 지점(노드)이 문을 닫아도 다른 지점에서 같은 책(Replica)을 찾을 수 있습니다.

## 전체 구조

```mermaid
flowchart TB
    subgraph Cluster["Cluster (클러스터)"]
        subgraph Node1["Node 1 (Master)"]
            subgraph Index1["products 인덱스"]
                P0["Primary Shard 0"]
                P1["Primary Shard 1"]
            end
        end
        subgraph Node2["Node 2 (Data)"]
            R0["Replica Shard 0"]
            R1["Replica Shard 1"]
        end
    end

    P0 -.복제.-> R0
    P1 -.복제.-> R1
```

*다이어그램: 클러스터 내 Master 노드와 Data 노드가 있으며, products 인덱스의 Primary Shard가 Replica Shard로 복제되는 구조를 보여줍니다.*

## Cluster (클러스터)

**클러스터**는 하나 이상의 노드로 구성된 Elasticsearch 서버 그룹입니다.

### 주요 특징

- 고유한 이름으로 식별 (기본값: `elasticsearch`)
- 같은 클러스터 이름을 가진 노드들이 자동으로 연결
- 데이터와 부하를 여러 노드에 분산

### 클러스터 상태

| 상태 | 의미 | 조치 |
|------|------|------|
| 🟢 Green | 모든 샤드 정상 | 정상 운영 |
| 🟡 Yellow | Primary는 정상, Replica 일부 미할당 | 노드 추가 검토 |
| 🔴 Red | 일부 Primary 샤드 미할당 | 즉시 조치 필요 |

```bash
# 클러스터 상태 확인
GET /_cluster/health
```

```json
{
  "cluster_name": "my-cluster",
  "status": "green",
  "number_of_nodes": 3,
  "active_primary_shards": 10,
  "active_shards": 20
}
```

{{< callout type="info" title="핵심 포인트" >}}
- 클러스터는 고유한 이름으로 식별되며, 같은 이름의 노드들이 자동으로 연결됩니다
- 클러스터 상태(Green/Yellow/Red)로 전체 시스템 건강 상태를 한눈에 파악할 수 있습니다
- `/_cluster/health` API로 현재 상태를 확인하세요
{{< /callout >}}

---

## Node (노드)

**노드**는 클러스터를 구성하는 단일 Elasticsearch 서버입니다.

### 노드 역할

```mermaid
flowchart LR
    subgraph Cluster
        M[Master Node<br>클러스터 관리]
        D1[Data Node<br>데이터 저장/검색]
        D2[Data Node<br>데이터 저장/검색]
        C[Coordinating Node<br>요청 라우팅]
    end

    Client --> C
    C --> D1
    C --> D2
    M -.관리.-> D1
    M -.관리.-> D2
```

*다이어그램: 클라이언트 요청이 Coordinating 노드를 통해 Data 노드로 라우팅되고, Master 노드가 전체를 관리하는 흐름을 보여줍니다.*

| 역할 | 설명 | 설정 |
|------|------|------|
| **Master** | 클러스터 상태 관리, 인덱스 생성/삭제 | `node.roles: [master]` |
| **Data** | 데이터 저장, 검색/집계 수행 | `node.roles: [data]` |
| **Coordinating** | 검색 요청 라우팅, 결과 병합 | `node.roles: []` |
| **Ingest** | 인덱싱 전 데이터 전처리 | `node.roles: [ingest]` |

> **소규모 클러스터**에서는 한 노드가 여러 역할을 수행합니다.

### 노드 정보 확인

```bash
GET /_nodes
```

{{< callout type="info" title="핵심 포인트" >}}
- 노드는 Master, Data, Coordinating, Ingest 등 다양한 역할을 수행할 수 있습니다
- 소규모 클러스터에서는 한 노드가 여러 역할을 겸하고, 대규모에서는 역할을 분리합니다
- `/_nodes` API로 노드 정보를 확인할 수 있습니다
{{< /callout >}}

---

## Index (인덱스)

**인덱스**는 비슷한 특성을 가진 문서들의 모음입니다. RDB의 테이블과 유사합니다.

### RDB vs Elasticsearch

| RDB | Elasticsearch |
|-----|---------------|
| Database | Cluster |
| Table | Index |
| Row | Document |
| Column | Field |
| Schema | Mapping |

### 인덱스 생성

```json
PUT /products
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "name": { "type": "text" },
      "price": { "type": "integer" },
      "category": { "type": "keyword" }
    }
  }
}
```

### 인덱스 설정

| 설정 | 기본값 | 설명 |
|------|--------|------|
| `number_of_shards` | 1 | Primary 샤드 수 (생성 후 변경 불가) |
| `number_of_replicas` | 1 | Replica 샤드 수 (동적 변경 가능) |
| `refresh_interval` | 1s | 검색 가능해지는 주기 |

### 인덱스 관리

```bash
# 인덱스 목록
GET /_cat/indices?v

# 인덱스 정보
GET /products

# 인덱스 삭제
DELETE /products
```

{{< callout type="info" title="핵심 포인트" >}}
- 인덱스는 RDB의 테이블과 유사하며, Mapping(스키마)을 가집니다
- `number_of_shards`는 생성 후 변경 불가하므로 신중히 결정하세요
- `number_of_replicas`는 동적으로 변경 가능합니다
{{< /callout >}}

---

## Document (문서)

**문서**는 인덱스에 저장되는 JSON 형태의 데이터 단위입니다. RDB의 Row와 유사합니다.

### 문서 구조

```json
{
  "_index": "products",      // 소속 인덱스
  "_id": "1",                // 문서 고유 ID
  "_version": 1,             // 버전 (수정 시 증가)
  "_source": {               // 실제 데이터
    "name": "맥북 프로",
    "price": 2390000,
    "category": "노트북"
  }
}
```

### 문서 CRUD

```bash
# 생성 (ID 지정)
PUT /products/_doc/1
{
  "name": "맥북 프로",
  "price": 2390000
}

# 생성 (ID 자동 생성)
POST /products/_doc
{
  "name": "아이패드"
}

# 조회
GET /products/_doc/1

# 수정
POST /products/_update/1
{
  "doc": {
    "price": 2290000
  }
}

# 삭제
DELETE /products/_doc/1
```

{{< callout type="info" title="핵심 포인트" >}}
- 문서는 JSON 형태로 저장되며, `_id`로 고유하게 식별됩니다
- `_version`으로 동시성 제어가 가능합니다
- CRUD 작업은 RESTful API로 수행합니다 (PUT/POST/GET/DELETE)
{{< /callout >}}

---

## Shard (샤드)

**샤드**는 인덱스를 수평 분할한 조각입니다. 분산 저장과 병렬 처리를 가능하게 합니다.

### Primary Shard vs Replica Shard

```mermaid
flowchart LR
    subgraph Index["products 인덱스 (3 Primary, 1 Replica)"]
        direction TB
        subgraph Node1
            P0[Primary 0]
            R2[Replica 2]
        end
        subgraph Node2
            P1[Primary 1]
            R0[Replica 0]
        end
        subgraph Node3
            P2[Primary 2]
            R1[Replica 1]
        end
    end

    P0 -.-> R0
    P1 -.-> R1
    P2 -.-> R2
```

*다이어그램: 3개의 Primary Shard가 각각 다른 노드에 분산되고, 각 Primary의 Replica가 다른 노드에 배치되어 장애 대비를 하는 구조입니다.*

| 유형 | 역할 | 특징 |
|------|------|------|
| **Primary** | 원본 데이터 저장 | 인덱스 생성 시 개수 고정 |
| **Replica** | Primary의 복제본 | 읽기 성능 향상, 장애 대비 |

### 샤드의 동작

**쓰기 (Write):**
1. 문서 ID로 해시 계산
2. 담당 Primary 샤드 결정: `shard = hash(id) % number_of_shards`
3. Primary에 쓰기 후 Replica에 복제

**읽기 (Read):**
1. Coordinating 노드가 요청 수신
2. 모든 관련 샤드(Primary 또는 Replica)에 병렬 요청
3. 결과 병합 후 반환

### 샤드 수 결정 가이드

| 데이터 규모 | 권장 Primary 샤드 수 |
|-------------|---------------------|
| 수 GB | 1 |
| 수십 GB | 2-5 |
| 수백 GB | 5-10 |
| TB 이상 | 10+ (노드 수 고려) |

> **Rule of Thumb:** 샤드 하나당 20-40GB가 적정합니다.

### 샤드 정보 확인

```bash
# 샤드 할당 상태
GET /_cat/shards/products?v

# 출력 예시
index    shard prirep state   docs store node
products 0     p      STARTED 100  50mb  node-1
products 0     r      STARTED 100  50mb  node-2
products 1     p      STARTED 120  55mb  node-2
products 1     r      STARTED 120  55mb  node-1
```

{{< callout type="info" title="핵심 포인트" >}}
- Primary Shard는 생성 후 변경 불가, Replica는 동적 변경 가능
- 문서 ID의 해시값으로 담당 샤드가 결정됩니다: `shard = hash(id) % number_of_shards`
- 샤드 하나당 20-40GB가 적정 크기입니다
{{< /callout >}}

---

## 역색인 (Inverted Index)

Elasticsearch가 빠른 검색을 제공하는 핵심 원리입니다.

### 일반 색인 vs 역색인

**일반 색인 (Forward Index):**
```
문서1 → [맥북, 프로, 14인치]
문서2 → [맥북, 에어, 13인치]
```

**역색인 (Inverted Index):**
```
맥북   → [문서1, 문서2]
프로   → [문서1]
에어   → [문서2]
14인치 → [문서1]
13인치 → [문서2]
```

### 검색 과정

"맥북 프로" 검색 시:
1. "맥북" → [문서1, 문서2]
2. "프로" → [문서1]
3. 교집합: 문서1

> **핵심:** 모든 문서를 스캔하지 않고, 역색인에서 바로 찾습니다.

{{< callout type="info" title="핵심 포인트" >}}
- 역색인은 "단어 → 문서 목록" 형태로 구성됩니다
- 일반 색인(문서 → 단어)과 반대 방향이라 "역색인"이라 부릅니다
- 검색어의 교집합/합집합 연산으로 빠른 검색이 가능합니다
{{< /callout >}}

---

## Lucene 내부 구조 (심화)

Elasticsearch는 내부적으로 **Apache Lucene** 라이브러리를 사용합니다. 각 샤드는 하나의 Lucene 인덱스입니다.

### Segment 구조

```mermaid
flowchart TB
    subgraph Shard["Shard (= Lucene Index)"]
        subgraph Segments["Segments"]
            S1["Segment 1<br>(불변)"]
            S2["Segment 2<br>(불변)"]
            S3["Segment 3<br>(불변)"]
        end
        Commit["Commit Point<br>(세그먼트 목록)"]
        Translog["Translog<br>(미커밋 변경사항)"]
    end

    S1 --> Commit
    S2 --> Commit
    S3 --> Commit
```

*다이어그램: 샤드 내부에 여러 개의 불변(Immutable) Segment가 있고, Commit Point가 활성 세그먼트 목록을 추적하며, Translog가 미커밋 변경사항을 보관하는 구조입니다.*

| 구성요소 | 역할 | 특징 |
|----------|------|------|
| **Segment** | 실제 역색인이 저장된 파일 | **불변(Immutable)** - 한번 쓰면 수정 안 됨 |
| **Commit Point** | 현재 활성 세그먼트 목록 | 검색 대상 세그먼트 추적 |
| **Translog** | 변경 사항 로그 | 장애 복구용, fsync 전 데이터 보호 |

### 문서 인덱싱 과정

```mermaid
sequenceDiagram
    participant Client
    participant ES as Elasticsearch
    participant Buffer as In-Memory Buffer
    participant Segment
    participant Translog

    Client->>ES: 문서 인덱싱
    ES->>Buffer: 메모리 버퍼에 추가
    ES->>Translog: Translog에 기록
    ES-->>Client: 성공 응답

    Note over Buffer,Segment: Refresh (기본 1초)
    Buffer->>Segment: 새 세그먼트 생성
    Note right of Segment: 이제 검색 가능

    Note over Segment,Translog: Flush (주기적)
    Segment->>Segment: 디스크에 fsync
    Translog->>Translog: Translog 삭제
```

*다이어그램: 문서 인덱싱 시 메모리 버퍼와 Translog에 기록 후, Refresh로 새 세그먼트가 생성되어 검색 가능해지고, Flush로 디스크에 영구 저장되는 과정입니다.*

### 왜 Segment는 불변(Immutable)인가?

1. **동시성 보장**: 락 없이 읽기 가능
2. **캐시 효율**: OS 파일 시스템 캐시 최대 활용
3. **안정성**: 데이터 손상 위험 감소

> **단점**: 삭제/수정 시 실제로 지우지 않고 "삭제 표시"만 함 → 나중에 Merge

### Segment Merge

세그먼트가 많아지면 성능이 저하됩니다. Elasticsearch는 백그라운드에서 세그먼트를 병합합니다:

```mermaid
flowchart LR
    subgraph Before["병합 전"]
        S1["Seg 1<br>100 docs"]
        S2["Seg 2<br>50 docs"]
        S3["Seg 3<br>30 docs"]
    end

    subgraph After["병합 후"]
        SM["Merged Seg<br>180 docs"]
    end

    S1 --> SM
    S2 --> SM
    S3 --> SM
```

*다이어그램: 여러 개의 작은 세그먼트(Seg 1, 2, 3)가 하나의 큰 세그먼트로 병합되는 과정을 보여줍니다.*

**병합 시 발생하는 일:**
- 삭제 표시된 문서 실제 제거
- 여러 세그먼트 → 하나의 큰 세그먼트
- I/O 부하 발생 (프로덕션에서 모니터링 필요)

### Refresh vs Flush vs Merge

| 작업 | 트리거 | 결과 | 성능 영향 |
|------|--------|------|----------|
| **Refresh** | 1초마다 (기본) | 버퍼 → 새 세그먼트 (검색 가능) | 낮음 |
| **Flush** | 주기적 / translog 크기 | 세그먼트 fsync + translog 삭제 | 중간 |
| **Merge** | 백그라운드 | 여러 세그먼트 → 하나 | 높음 (I/O 집약) |

### Near Real-Time (NRT) 검색

Elasticsearch는 "실시간"이 아니라 "**준실시간(NRT)**" 검색입니다:

```
문서 인덱싱 → (최대 1초 대기) → Refresh → 검색 가능
```

- **기본 refresh_interval**: 1초
- **즉시 검색 필요 시**: `?refresh=true` 파라미터 (성능 주의)
- **대량 인덱싱 시**: `refresh_interval: -1`로 비활성화 후 완료 시 수동 refresh

```json
// 대량 인덱싱 최적화
PUT /products/_settings
{ "refresh_interval": "-1" }

// 인덱싱 완료 후
POST /products/_refresh

PUT /products/_settings
{ "refresh_interval": "1s" }
```

{{< callout type="info" title="핵심 포인트" >}}
- Segment는 불변(Immutable)이므로 삭제/수정 시 "삭제 표시"만 하고 나중에 Merge합니다
- Refresh(1초)로 검색 가능해지고, Flush로 디스크에 영구 저장됩니다
- Elasticsearch는 "준실시간(NRT)" 검색이며, 즉시 검색이 필요하면 `?refresh=true` 사용 (성능 주의)
{{< /callout >}}

---

## 정리

```mermaid
flowchart TB
    A[Cluster] --> B[Node]
    B --> C[Index]
    C --> D[Shard]
    D --> E[Document]

    A2["여러 노드의 집합<br>고가용성 제공"] -.-> A
    B2["실제 서버<br>역할별 분리 가능"] -.-> B
    C2["문서의 논리적 그룹<br>RDB 테이블과 유사"] -.-> C
    D2["인덱스의 물리적 분할<br>분산 처리 단위"] -.-> D
    E2["JSON 데이터<br>RDB Row와 유사"] -.-> E
```

*다이어그램: Cluster → Node → Index → Shard → Document의 계층 구조와 각 구성요소의 역할을 요약합니다.*

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 스키마 설계 | [데이터 모델링]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}}) |
| 검색 쿼리 작성 | [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) |
| 실습 | [기본 예제]({{< relref "/docs/elasticsearch/examples/basic" >}}) |
