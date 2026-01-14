---
title: 셔플 최적화하기
weight: 3
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
---

{{% notice style="tip" title="TL;DR" %}}
- **셔플 확인**: `df.explain()`에서 `Exchange` 노드 = 셔플 발생
- **불필요한 셔플 제거**: 같은 그룹에서 여러 집계 한 번에 수행
- **브로드캐스트 조인**: 작은 테이블(수십 MB)은 `broadcast()` 사용
- **셔플 파티션 수**: `spark.sql.shuffle.partitions` 조정 (기본 200)
{{% /notice %}}

## 문제 정의

Spark 작업에서 **Stage 간 전환이 오래 걸리거나** 네트워크 I/O가 과도하게 발생합니다. 셔플은 Spark에서 가장 비용이 큰 연산이므로 최소화해야 합니다.

셔플이 발생하는 연산:
- `groupBy`, `reduceByKey`
- `join`, `cogroup`
- `repartition`, `coalesce(shuffle=true)`
- `distinct`, `sortByKey`

---

## 전제 조건

| 항목 | 설명 |
|------|------|
| **환경** | Spark 애플리케이션 실행 환경 |
| **도구** | Spark UI, `explain()` 메서드 사용 가능 |

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

반복 조인되는 테이블은 버케팅으로 셔플을 제거합니다.

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

셔플이 최적화되었는지 확인하세요:

### Spark UI에서 확인

1. **Stages** 탭 → Shuffle Read/Write 크기 확인
2. 이전 대비 셔플 데이터량 감소 확인
3. Stage 수 감소 확인 (셔플 제거 시)

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
