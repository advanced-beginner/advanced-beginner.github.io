---
title: 고가용성
description: "Elasticsearch 고가용성의 작동 원리와 장애 대응 전략을 설명합니다."
weight: 9
lastmod: 2026-01-15
---

## 전체 비유: 도서관의 재난 대비 시스템

고가용성을 **도서관 체인의 재난 대비 시스템**에 비유하면 이해하기 쉽습니다:

| 도서관 비유 | Elasticsearch | 역할 |
|------------|---------------|------|
| 인기 도서 복본 비치 | Replica Shard | 데이터 복제로 장애 대비 + 대출 분산 |
| 장서 목록 사진 백업 | Snapshot | 특정 시점 데이터 백업 |
| 백업 사진 정기 촬영 | SLM (Snapshot Lifecycle) | 자동 백업 정책 |
| 타 지역 도서관에 복본 보관 | CCR (Cross-Cluster Replication) | 원격 클러스터로 실시간 복제 |
| 화재 시 백업 지점 운영 | Active-Passive DR | 장애 시 대체 클러스터 활성화 |
| 지점 폐쇄 → 타 지점 자동 대출 | 자동 Failover | 노드 장애 시 Replica 승격 |
| 서울/부산 양방향 운영 | Active-Active | 각 리전에서 독립 운영 |

이처럼 고가용성은 "지진이나 화재에도 도서관 서비스를 중단 없이 제공"하는 재난 대비 시스템과 같습니다.

Elasticsearch 클러스터의 Replica, Snapshot, 장애 대응 전략을 배웁니다.

## 고가용성 개념

### HA(High Availability) 목표

| 지표 | 설명 | 목표 |
|------|------|------|
| **가용성** | 서비스 정상 운영 시간 | 99.9% (연간 8.76시간 다운타임) |
| **내구성** | 데이터 유실 방지 | 99.999999% (9-nines) |
| **복구 시간** | 장애 발생 → 복구 완료 | < 30분 |

### HA 구성 요소

```mermaid
flowchart TB
    A[고가용성] --> B[Replica Shard]
    A --> C[Snapshot & Restore]
    A --> D[Cross-Cluster Replication]
    A --> E[클러스터 설계]
```

---

## Replica Shard

### 역할

```mermaid
flowchart LR
    subgraph Node1
        P0[Primary 0]
    end
    subgraph Node2
        R0[Replica 0]
    end
    subgraph Node3
        P1[Primary 1]
    end

    P0 -->|복제| R0
    Client -->|쓰기| P0
    Client -->|읽기| R0
```

1. **데이터 이중화**: Primary 장애 시 Replica가 승격
2. **읽기 성능 향상**: 검색 요청 분산

### Replica 설정

```json
PUT /products
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  }
}
```

### 동적 변경

```json
PUT /products/_settings
{
  "number_of_replicas": 2
}
```

### 권장 설정

| 환경 | number_of_replicas |
|------|-------------------|
| 개발 | 0 |
| 소규모 프로덕션 | 1 |
| 대규모/중요 데이터 | 2 |

### Auto-Expand Replicas

노드 수에 따라 자동 조정:

```json
PUT /products/_settings
{
  "index.auto_expand_replicas": "0-2"  // 최소 0, 최대 2
}
```

---

## Snapshot & Restore

### 스냅샷이란?

특정 시점의 인덱스 상태를 저장하는 백업입니다.

### Repository 설정

**S3 Repository:**

```json
PUT /_snapshot/my_s3_backup
{
  "type": "s3",
  "settings": {
    "bucket": "my-elasticsearch-backups",
    "region": "ap-northeast-2",
    "base_path": "snapshots"
  }
}
```

**파일 시스템:**

```json
PUT /_snapshot/my_fs_backup
{
  "type": "fs",
  "settings": {
    "location": "/mount/backups",
    "compress": true
  }
}
```

> `elasticsearch.yml`에 `path.repo` 설정 필요

### 스냅샷 생성

```json
// 전체 클러스터
PUT /_snapshot/my_backup/snapshot_2024_01_15
{
  "indices": "*",
  "include_global_state": true
}

// 특정 인덱스만
PUT /_snapshot/my_backup/products_backup
{
  "indices": "products,orders",
  "include_global_state": false
}
```

### 스냅샷 상태 확인

```json
GET /_snapshot/my_backup/snapshot_2024_01_15/_status
```

### 스냅샷 목록

```json
GET /_snapshot/my_backup/_all
```

### 복원

```json
// 전체 복원
POST /_snapshot/my_backup/snapshot_2024_01_15/_restore

// 특정 인덱스만 다른 이름으로
POST /_snapshot/my_backup/snapshot_2024_01_15/_restore
{
  "indices": "products",
  "rename_pattern": "(.+)",
  "rename_replacement": "restored_$1"
}
```

### SLM (Snapshot Lifecycle Management)

자동 백업 정책:

```json
PUT /_slm/policy/daily_backup
{
  "schedule": "0 30 2 * * ?",     // 매일 02:30
  "name": "<daily-snap-{now/d}>",
  "repository": "my_backup",
  "config": {
    "indices": "*",
    "include_global_state": true
  },
  "retention": {
    "expire_after": "30d",
    "min_count": 5,
    "max_count": 50
  }
}
```

---

## Cross-Cluster Replication (CCR)

### 개념

원격 클러스터로 데이터를 실시간 복제합니다.

```mermaid
flowchart LR
    subgraph Leader["Leader Cluster (서울)"]
        L[products]
    end
    subgraph Follower["Follower Cluster (부산)"]
        F[products-replica]
    end

    L -->|실시간 복제| F
```

### 사용 사례

- **재해 복구(DR)**: 다른 리전에 복제본 유지
- **지역별 읽기**: 지연 시간 단축
- **데이터 집중화**: 여러 클러스터 → 중앙 집계

### 설정 방법

**1. 원격 클러스터 연결:**

```json
PUT /_cluster/settings
{
  "persistent": {
    "cluster": {
      "remote": {
        "leader_cluster": {
          "seeds": ["leader-node:9300"]
        }
      }
    }
  }
}
```

**2. Follower 인덱스 생성:**

```json
PUT /products-replica/_ccr/follow
{
  "remote_cluster": "leader_cluster",
  "leader_index": "products"
}
```

---

## 장애 시나리오와 대응

### 시나리오 1: 단일 노드 장애

**상황:** Data Node 1대 다운

**자동 대응:**
1. Replica가 Primary로 승격 (즉시)
2. 새 Replica 할당 (다른 노드에)
3. 클러스터 상태: Green 유지 (Replica 있는 경우)

**확인:**
```json
GET /_cluster/health
GET /_cat/shards?v
```

### 시나리오 2: Master 노드 장애

**상황:** Master Node 다운

**자동 대응:**
1. Master 선출 (다른 Master-eligible 노드)
2. 새 Master가 클러스터 상태 관리

**권장:** Master-eligible 노드 최소 3대 (과반수 유지)

### 시나리오 3: 디스크 장애

**상황:** 데이터 디스크 손상

**대응:**
```json
// 1. 해당 노드 제외
PUT /_cluster/settings
{
  "transient": {
    "cluster.routing.allocation.exclude._name": "damaged-node"
  }
}

// 2. 디스크 교체 후 노드 재시작

// 3. 제외 해제
PUT /_cluster/settings
{
  "transient": {
    "cluster.routing.allocation.exclude._name": null
  }
}
```

### 시나리오 4: 전체 클러스터 장애

**상황:** 데이터센터 장애

**대응:**
1. DR 클러스터 활성화 (CCR 사용 시)
2. 또는 스냅샷에서 복원

```json
POST /_snapshot/my_backup/latest/_restore
{
  "indices": "*",
  "include_global_state": true
}
```

---

## 클러스터 설계 패턴

### 패턴 1: Active-Passive

```mermaid
flowchart LR
    subgraph Active["Active 클러스터"]
        A1[Node 1]
        A2[Node 2]
        A3[Node 3]
    end
    subgraph Passive["Passive 클러스터 (DR)"]
        P1[Node 1]
        P2[Node 2]
        P3[Node 3]
    end

    Active -->|CCR| Passive
    Client --> Active
```

- Active에서 읽기/쓰기
- Passive는 대기 (장애 시 활성화)

### 패턴 2: Active-Active

```mermaid
flowchart TB
    subgraph Seoul["서울 클러스터"]
        S[products]
    end
    subgraph Busan["부산 클러스터"]
        B[products]
    end

    SeoulClient --> Seoul
    BusanClient --> Busan
    Seoul <-->|양방향 CCR| Busan
```

- 각 리전에서 읽기/쓰기
- 양방향 동기화 (충돌 관리 필요)

### 패턴 3: 다중 데이터센터

```json
// Zone Awareness 설정
PUT /_cluster/settings
{
  "persistent": {
    "cluster.routing.allocation.awareness.attributes": "zone",
    "cluster.routing.allocation.awareness.force.zone.values": "zone1,zone2"
  }
}
```

```yaml
# elasticsearch.yml (각 노드)
node.attr.zone: zone1  # 또는 zone2
```

→ Primary와 Replica가 다른 Zone에 배치됨

---

## 모니터링 알림 설정

### 핵심 알림 조건

| 조건 | 심각도 | 조치 |
|------|--------|------|
| 클러스터 상태 Yellow | Warning | 노드 확인 |
| 클러스터 상태 Red | Critical | 즉시 대응 |
| 노드 다운 | Critical | 노드 복구 |
| 디스크 > 80% | Warning | 공간 확보 |
| 디스크 > 90% | Critical | 긴급 확장 |
| JVM Heap > 85% | Warning | 메모리 확인 |

### Watcher 알림 (Basic License+)

```json
PUT /_watcher/watch/cluster_health_watch
{
  "trigger": {
    "schedule": { "interval": "1m" }
  },
  "input": {
    "http": {
      "request": {
        "host": "localhost",
        "port": 9200,
        "path": "/_cluster/health"
      }
    }
  },
  "condition": {
    "compare": {
      "ctx.payload.status": { "eq": "red" }
    }
  },
  "actions": {
    "send_email": {
      "email": {
        "to": "admin@example.com",
        "subject": "Elasticsearch 클러스터 RED 상태!",
        "body": "클러스터 상태가 RED입니다. 즉시 확인하세요."
      }
    }
  }
}
```

---

## 실제 장애 사례와 교훈

### 사례 1: 디스크 풀로 인한 클러스터 마비

**상황:**
- 로그 인덱스가 예상보다 빠르게 증가
- 디스크 사용률 95% 초과 → 전체 인덱스 read-only 전환
- 새 로그 유입 불가, 서비스 모니터링 중단

**대응:**
```bash
# 1. 긴급: 오래된 인덱스 삭제
DELETE /logs-2024.01.*

# 2. read-only 해제
PUT /_all/_settings
{ "index.blocks.read_only_allow_delete": null }

# 3. 재발 방지: ILM 정책 적용
```

**교훈:**
- 디스크 사용률 80%에서 알림 설정 필수
- ILM으로 자동 삭제 정책 필수
- 용량 계획 시 2배 여유 확보

---

### 사례 2: Master 노드 단일 장애점

**상황:**
- 비용 절감으로 Master-eligible 노드 1개만 운영
- Master 노드 장애 → 전체 클러스터 다운
- 신규 노드 추가해도 Master 선출 불가 (과반수 미충족)

**대응:**
```yaml
# elasticsearch.yml - 강제 Master 선출 (위험!)
cluster.initial_master_nodes: ["node-1"]
```

**교훈:**
- Master-eligible 노드 **최소 3개** 필수
- 홀수 개 유지 (2개보다 3개가 안전)
- `discovery.seed_hosts` 정확히 설정

---

### 사례 3: 대량 인덱싱 중 OOM

**상황:**
- 1억 건 데이터 마이그레이션 중 bulk 인덱싱
- JVM Heap 100% → OOM → 노드 다운
- 연쇄적으로 다른 노드도 과부하

**대응:**
```bash
# 1. Bulk 크기 조정 (5-15MB 권장)
# 2. Refresh 비활성화
PUT /products/_settings
{ "refresh_interval": "-1" }

# 3. Replica 임시 비활성화
PUT /products/_settings
{ "number_of_replicas": 0 }

# 4. 인덱싱 완료 후 복원
```

**교훈:**
- Bulk 크기는 문서 수가 아닌 **바이트 크기**로 관리
- 대량 작업 시 refresh_interval 비활성화
- 인덱싱 전용 노드 분리 고려

---

### 사례 4: 샤드 불균형으로 인한 핫스팟

**상황:**
- 특정 노드에 샤드가 집중
- 해당 노드 CPU 100%, 다른 노드는 10%
- 검색 응답 시간 10배 증가

**대응:**
```json
// 샤드 재배치
POST /_cluster/reroute
{
  "commands": [{
    "move": {
      "index": "products",
      "shard": 0,
      "from_node": "hot-node",
      "to_node": "cold-node"
    }
  }]
}
```

**교훈:**
- `/_cat/allocation` 정기 모니터링
- Hot-Warm 아키텍처 적용
- Zone awareness로 균등 분배

---

### 사례 5: 스냅샷 복원 실패

**상황:**
- 장애 발생 → 스냅샷 복원 시도
- 스냅샷이 손상되어 복원 실패
- 백업 검증을 안 해서 발견 늦음

**대응:**
```bash
# 매주 복원 테스트 자동화
# 테스트 클러스터에서 복원 검증
POST /_snapshot/my_backup/weekly_snapshot/_restore?wait_for_completion=true
{
  "indices": "products",
  "rename_pattern": "(.+)",
  "rename_replacement": "test_$1"
}
```

**교훈:**
- **복원 테스트 없는 백업은 백업이 아님**
- 월 1회 이상 복원 훈련 필수
- 다른 리전에 스냅샷 복제

---

## 체크리스트

### 일일 점검

- [ ] 클러스터 상태 확인 (`/_cluster/health`)
- [ ] 노드 상태 확인 (`/_cat/nodes`)
- [ ] 디스크 사용량 확인 (`/_cat/allocation`)

### 주간 점검

- [ ] 스냅샷 정상 생성 확인
- [ ] JVM 메모리 트렌드 확인
- [ ] 느린 쿼리 로그 검토

### 분기 점검

- [ ] 스냅샷 복원 테스트
- [ ] DR 전환 훈련
- [ ] 용량 계획 검토

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 클러스터 구성 | [클러스터 관리](cluster-management/) |
| 성능 최적화 | [성능 튜닝](performance-tuning/) |
| 실전 구현 | [상품 검색 시스템](../examples/product-search/) |
