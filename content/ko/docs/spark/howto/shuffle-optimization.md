---
title: 셔플 최적화하기
weight: 3
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "Spark 셔플을 최소화하여 작업 성능을 대폭 개선하는 가이드"
---

{{< callout type="info" >}}
**예상 소요 시간**: 약 20분
{{< /callout >}}

{{% notice style="tip" title="TL;DR" %}}
- **셔플 확인**: `df.explain()`에서 `Exchange` 노드 = 셔플 발생
- **불필요한 셔플 제거**: 같은 그룹에서 여러 집계 한 번에 수행
- **브로드캐스트 조인**: 작은 테이블(수십 MB)은 `broadcast()` 사용
- **셔플 파티션 수**: `spark.sql.shuffle.partitions` 조정 (기본 200)
{{% /notice %}}

## 문제 정의

다음과 같은 증상이 나타나면 셔플 최적화가 필요합니다:

| 증상 | 확인 위치 |
|------|----------|
| Stage 간 전환이 10초 이상 걸림 | Spark UI → Jobs 탭 |
| Shuffle Read/Write가 수십 GB 이상 | Spark UI → Stages 탭 |
| 네트워크 I/O로 인한 타임아웃 | 애플리케이션 로그 |
| `explain()`에서 `Exchange` 노드 다수 | 실행 계획 출력 |

셔플은 Spark에서 가장 비용이 큰 연산입니다. 아래 연산은 셔플을 발생시킵니다:
- `groupBy`, `reduceByKey`
- `join`, `cogroup`
- `repartition`, `coalesce(shuffle=true)`
- `distinct`, `sortByKey`

---

## 전제 조건

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| **Spark 버전** | 2.4+ (AQE는 3.0+) | `spark-submit --version` |
| **Spark UI** | 접근 가능 | 브라우저에서 `http://localhost:4040` 열기 |
| **권한** | Spark 설정 변경 가능 | spark-submit 실행 권한 확인 |

**지원 환경**: Linux, macOS, Windows (WSL2 권장)

### 환경 확인

다음 명령어로 환경이 준비되었는지 확인하세요:

```bash
# Spark 버전 확인
spark-submit --version

# Spark UI 접근 확인 (애플리케이션 실행 중일 때)
curl -s http://localhost:4040/api/v1/applications | head -1
```

### 권장 해결 순서

최적화 효과가 큰 순서로 진행하세요:

1. **Step 2**: 불필요한 셔플 제거 (가장 효과적)
2. **Step 3**: 브로드캐스트 조인 (작은 테이블 조인 시)
3. **Step 4**: 셔플 파티션 수 조정
4. **Step 5~6**: 고급 최적화 (필요 시)

---

## Step 1: 셔플 발생 지점 확인

### 1.1 실행 계획에서 확인

```java
Dataset<Row> result = df
    .groupBy("category")
    .agg(count("*"), sum("amount"))
    .orderBy(col("count").desc());

// 실행 계획 확인
result.explain();
```

**출력 예시:**
```
== Physical Plan ==
*(3) Sort [count DESC]
+- Exchange rangepartitioning    ← 셔플!
   +- *(2) HashAggregate
      +- Exchange hashpartitioning   ← 셔플!
         +- *(1) HashAggregate
            +- FileScan parquet
```

`Exchange` 노드가 보이면 셔플이 발생합니다.

### 1.2 Spark UI에서 확인

1. **Jobs** 탭 → 특정 Job 선택
2. **DAG Visualization** 확인
3. Stage 경계(점선)가 셔플 발생 지점

### 1.3 셔플 메트릭 확인

```java
// 셔플 메트릭 로깅
df.write()
    .mode("overwrite")
    .parquet("output");

// Spark UI → Stages → 완료된 Stage → Shuffle Read/Write 확인
```

---

## Step 2: 불필요한 셔플 제거

### 2.1 집계 통합

**문제: 두 번의 셔플**
```java
// 두 번의 groupBy = 두 번의 셔플
Dataset<Row> counts = df.groupBy("category").count();
Dataset<Row> sums = df.groupBy("category").agg(sum("amount"));
Dataset<Row> result = counts.join(sums, "category");  // 또 셔플!
```

**해결: 한 번의 셔플**
```java
// 하나의 groupBy에서 모든 집계 수행
Dataset<Row> result = df.groupBy("category")
    .agg(
        count("*").alias("count"),
        sum("amount").alias("total_amount"),
        avg("amount").alias("avg_amount")
    );
```

### 2.2 필터 먼저 적용

**문제: 조인 후 필터**
```java
Dataset<Row> joined = large.join(small, "key");
Dataset<Row> result = joined.filter(col("status").equalTo("ACTIVE"));
```

**해결: 필터 후 조인**
```java
// 데이터량을 먼저 줄여서 셔플 크기 감소
Dataset<Row> filteredLarge = large.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> result = filteredLarge.join(small, "key");
```

### 2.3 중복 제거 최적화

**문제: 전체 distinct**
```java
Dataset<Row> unique = df.distinct();  // 전체 컬럼 기준 distinct
```

**해결: 필요한 컬럼만**
```java
// 필요한 컬럼만 선택 후 distinct (셔플 데이터량 감소)
Dataset<Row> unique = df.select("id", "category").distinct();
```

---

## Step 3: 브로드캐스트 조인 활용

작은 테이블과 조인할 때 셔플을 완전히 제거합니다.

### 3.1 명시적 브로드캐스트

```java
import static org.apache.spark.sql.functions.broadcast;

// 작은 테이블(수십 MB 이하)을 모든 Executor에 복제
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);
```

### 3.2 자동 브로드캐스트 임계값 조정

```java
SparkSession spark = SparkSession.builder()
    // 100MB 이하 테이블 자동 브로드캐스트
    .config("spark.sql.autoBroadcastJoinThreshold", "104857600")
    .getOrCreate();
```

### 3.3 브로드캐스트 효과 확인

```java
result.explain();
// BroadcastHashJoin이 보이면 셔플 없이 조인
```

**출력 예시:**
```
== Physical Plan ==
*(2) BroadcastHashJoin [key], [key], Inner, BuildRight
:- *(2) FileScan parquet [key, col1]
+- BroadcastExchange    ← 셔플 아닌 브로드캐스트!
   +- *(1) FileScan parquet [key, col2]
```

---

## Step 4: 셔플 파티션 수 최적화

### 4.1 기본 설정 조정

```java
SparkSession spark = SparkSession.builder()
    // 기본값 200, 데이터 크기에 따라 조정
    .config("spark.sql.shuffle.partitions", "100")
    .getOrCreate();
```

**권장 파티션 수:**
```
파티션 수 = max(코어 수 × 2, 데이터 크기(MB) / 200)
```

| 데이터 크기 | 권장 파티션 수 |
|------------|---------------|
| 1GB 이하 | 50~100 |
| 10GB | 100~200 |
| 100GB | 500~1000 |
| 1TB | 2000~5000 |

### 4.2 AQE 동적 파티션 통합 (Spark 3.0+)

```java
SparkSession spark = SparkSession.builder()
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
    .config("spark.sql.adaptive.coalescePartitions.minPartitionSize", "64MB")
    .getOrCreate();
```

AQE가 자동으로 작은 파티션을 병합하여 오버헤드를 줄입니다.

---

## Step 5: 버케팅으로 사전 파티셔닝

반복 조인되는 테이블은 버케팅으로 셔플을 제거하세요.

{{< callout type="warning" >}}
**주의**: 버케팅은 Hive 메타스토어가 필요하며, 테이블을 다시 작성해야 합니다. 일회성 조인에는 오버헤드가 클 수 있으므로, 동일한 키로 반복 조인하는 경우에만 사용하세요.
{{< /callout >}}

### 5.1 버케팅된 테이블 생성

```java
// 조인 키 기준으로 버케팅
df.write()
    .bucketBy(100, "user_id")  // 100개 버킷
    .sortBy("user_id")
    .saveAsTable("users_bucketed");

transactions.write()
    .bucketBy(100, "user_id")  // 동일한 버킷 수
    .sortBy("user_id")
    .saveAsTable("transactions_bucketed");
```

### 5.2 셔플 없는 조인

```java
Dataset<Row> users = spark.table("users_bucketed");
Dataset<Row> transactions = spark.table("transactions_bucketed");

// 같은 버킷 수 + 같은 키 = 셔플 없이 조인
Dataset<Row> result = users.join(transactions, "user_id");
```

---

## Step 6: 셔플 스토리지 최적화

### 6.1 셔플 압축 활성화

```java
SparkSession spark = SparkSession.builder()
    .config("spark.shuffle.compress", "true")  // 기본 true
    .config("spark.shuffle.spill.compress", "true")  // 기본 true
    .getOrCreate();
```

### 6.2 셔플 디렉토리 최적화

```java
// 빠른 SSD에 셔플 파일 저장
.config("spark.local.dir", "/ssd/spark-local")

// 여러 디스크 분산
.config("spark.local.dir", "/disk1/spark,/disk2/spark,/disk3/spark")
```

---

## 검증

셔플이 최적화되었는지 다음 기준으로 확인하세요:

### 성공 기준

| 항목 | 성공 조건 |
|------|----------|
| **Shuffle Write** | 최적화 전 대비 50% 이상 감소 |
| **Stage 수** | `explain()`의 Exchange 노드 수 감소 |
| **실행 시간** | 최적화 전 대비 30% 이상 단축 |
| **브로드캐스트 조인** | `explain()`에서 `BroadcastHashJoin` 확인 |

### Spark UI에서 확인

1. **Stages** 탭 → Shuffle Read/Write 크기를 확인하세요.
2. 이전 대비 셔플 데이터량이 감소했는지 확인하세요.
3. Stage 수가 감소했는지 확인하세요 (셔플 제거 시).

### 실행 시간 비교

```java
// 최적화 전후 실행 시간 비교
long start = System.currentTimeMillis();
result.count();
long duration = System.currentTimeMillis() - start;
System.out.println("실행 시간: " + duration + "ms");
```

---

## 트러블슈팅 체크리스트

### 오류 메시지별 해결 방법

| 오류 메시지/증상 | 원인 | 해결 방법 |
|-----------------|------|----------|
| `FetchFailedException: Failed to connect` | 셔플 데이터 전송 실패 | 셔플 파일 압축 활성화, 네트워크 타임아웃 증가 |
| `java.io.IOException: No space left on device` | 셔플 디스크 공간 부족 | `spark.local.dir` 용량 확보 또는 경로 변경 |
| `TimeoutException` (셔플 중) | 네트워크 병목 | 셔플 파티션 수 증가, 브로드캐스트 활용 |
| Spark UI에서 Shuffle Write > 100GB | 과도한 셔플 | 불필요한 셔플 제거, 필터 먼저 적용 |
| Stage 간 전환 10초 이상 | 셔플 오버헤드 | 브로드캐스트 조인 또는 버케팅 적용 |

### 상황별 권장 해결책

| 상황 | 권장 해결책 |
|------|------------|
| 작은 테이블과 조인 | `broadcast()` 사용 |
| 여러 집계 수행 | 하나의 `groupBy`에서 모든 `agg` 수행 |
| 반복 조인 | 버케팅 적용 |
| 셔플 파티션 너무 많음 | `shuffle.partitions` 감소 또는 AQE 활성화 |
| 셔플 파티션 너무 적음 | `shuffle.partitions` 증가 |

---

## 다음 단계

- [데이터 스큐 해결하기](data-skew/) - 파티션 불균형 해결
- [성능 튜닝](../concepts/tuning/) - 전체 성능 최적화
