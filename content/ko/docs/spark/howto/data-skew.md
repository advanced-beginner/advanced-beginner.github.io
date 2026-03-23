---
title: 데이터 스큐 해결하기
weight: 2
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "특정 파티션에 데이터가 집중되는 스큐 문제를 진단하고 해결하는 가이드"
---

{{< callout type="info" >}}
**예상 소요 시간**: 약 20분
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **진단**: Spark UI Stages 탭에서 Task Duration Min/Max 비교 (10배 이상 차이 = 스큐)
- **AQE 활성화**: `spark.sql.adaptive.skewJoin.enabled=true` (Spark 3.0+)
- **수동 해결**: Salting 기법으로 핫 키 분산
{{< /callout >}}

## 문제 정의

Spark 작업이 대부분 빠르게 완료되지만 **일부 Task만 오래 걸리는** 현상이 발생합니다:

```
Stage 3: 199/200 tasks completed... (마지막 1개가 수십 분째 실행 중)
```

이는 **데이터 스큐(Data Skew)** - 특정 키에 데이터가 집중되어 해당 파티션만 과부하 상태가 됩니다.

---

## 전제 조건

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| **Spark 버전** | 2.4+ (AQE는 3.0+) | `spark-submit --version` |
| **Spark UI** | 접근 가능 | 브라우저에서 `http://localhost:4040` 열기 |
| **데이터** | 조인/그룹화에 사용되는 키 컬럼 존재 | 스키마에서 키 컬럼 확인 |

**지원 환경**: Linux, macOS, Windows (WSL2 권장)

### 환경 확인

다음 명령어로 환경이 준비되었는지 확인하세요:

```bash
# Spark 버전 확인 (AQE 사용 시 3.0 이상 필요)
spark-submit --version

# Spark UI 접근 확인 (애플리케이션 실행 중일 때)
curl -s http://localhost:4040/api/v1/applications | head -1
```

---

## Step 1: 스큐 진단

### 1.1 Spark UI에서 확인

1. Spark UI → **Stages** 탭 클릭
2. 오래 걸리는 Stage 선택
3. **Summary Metrics** 확인:

| 메트릭 | 정상 | 스큐 의심 |
|--------|------|----------|
| Duration (Min / Max) | 비슷함 | **10배 이상 차이** |
| Shuffle Read Size (Min / Max) | 균등 | 일부만 큼 |
| Records Read (Min / Max) | 균등 | 일부만 많음 |

### 1.2 코드로 확인

```java
import static org.apache.spark.sql.functions.*;

// 파티션별 데이터 분포 확인
df.groupBy(spark_partition_id().alias("partition_id"))
    .count()
    .orderBy(col("count").desc())
    .show(20);

// 결과 예시 (스큐 있음):
// +------------+--------+
// |partition_id|   count|
// +------------+--------+
// |          15| 5000000|  ← 비정상적으로 큼!
// |           3|   50000|
// |           7|   48000|
// |           1|   45000|
// ...
```

### 1.3 핫 키 식별

```java
// 어떤 키가 핫 키인지 확인
df.groupBy("join_key")
    .count()
    .orderBy(col("count").desc())
    .show(20);

// 결과 예시:
// +--------+--------+
// |join_key|   count|
// +--------+--------+
// |   null | 3000000|  ← null 값이 핫 키!
// | user_1 |  500000|
// | user_2 |   10000|
// ...
```

---

## Step 2: AQE 스큐 조인 활성화 (Spark 3.0+)

가장 간단한 해결 방법입니다. Spark가 자동으로 스큐를 감지하고 처리합니다.

```java
SparkSession spark = SparkSession.builder()
    .appName("Skew Fix")
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.adaptive.skewJoin.enabled", "true")
    .config("spark.sql.adaptive.skewJoin.skewedPartitionFactor", "5")
    .config("spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes", "256MB")
    .getOrCreate();

// 일반 조인 수행 - AQE가 자동으로 스큐 처리
Dataset<Row> result = largeTable.join(smallTable, "key");
```

**설정 설명:**

| 설정 | 기본값 | 설명 |
|------|--------|------|
| `skewedPartitionFactor` | 5 | 중간값의 5배 이상이면 스큐로 판단 |
| `skewedPartitionThresholdInBytes` | 256MB | 최소 256MB 이상이어야 스큐로 판단 |

---

## Step 3: Salting 기법 적용

AQE를 사용할 수 없거나 더 세밀한 제어가 필요할 때 Salting 기법을 사용하세요.

{{< callout type="warning" >}}
**주의**: Salting은 작은 테이블을 복제하므로 메모리 사용량이 증가합니다. `numSaltBuckets`를 너무 크게 설정하면 오히려 성능이 저하될 수 있습니다. 10~20 사이 값으로 시작하세요.
{{< /callout >}}

### 3.1 기본 Salting

핫 키에 랜덤 접미사를 추가하여 여러 파티션에 분산시킵니다.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import static org.apache.spark.sql.functions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

int numSaltBuckets = 10;  // 핫 키를 10개 파티션으로 분산

// 1. 큰 테이블에 salt 추가
Dataset<Row> saltedLarge = largeTable.withColumn("salt",
    expr("FLOOR(RAND() * " + numSaltBuckets + ")"))
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));

// 2. 작은 테이블을 salt 수만큼 복제
List<Row> saltValues = IntStream.range(0, numSaltBuckets)
    .mapToObj(i -> RowFactory.create(i))
    .collect(Collectors.toList());

Dataset<Row> saltDf = spark.createDataFrame(saltValues,
    new StructType().add("salt", DataTypes.IntegerType));

Dataset<Row> replicatedSmall = smallTable.crossJoin(saltDf)
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));

// 3. salted_key로 조인
Dataset<Row> result = saltedLarge.join(replicatedSmall, "salted_key")
    .drop("salt", "salted_key");
```

### 3.2 선택적 Salting (핫 키만)

핫 키만 salting하여 오버헤드를 최소화합니다:

```java
// 핫 키 목록 (사전에 식별)
List<String> hotKeys = Arrays.asList("hot_user_1", "hot_user_2");

Dataset<Row> saltedLarge = largeTable.withColumn("salt",
    when(col("join_key").isin(hotKeys.toArray()),
        expr("FLOOR(RAND() * 10)"))
    .otherwise(lit(0)))
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));
```

---

## Step 4: 브로드캐스트 조인 활용

작은 테이블(수십 MB 이하)과 조인할 때는 브로드캐스트가 가장 효과적입니다.

{{< callout type="warning" >}}
**주의**: 브로드캐스트 테이블 크기가 Executor 메모리의 20%를 초과하면 OOM이 발생할 수 있습니다. 100MB 이상의 테이블은 브로드캐스트하지 마세요.
{{< /callout >}}

```java
import static org.apache.spark.sql.functions.broadcast;

// 작은 테이블을 모든 Executor에 복제
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),  // 셔플 없이 조인
    "key"
);
```

**브로드캐스트 임계값 조정:**

```java
// 기본 10MB, 필요시 증가
.config("spark.sql.autoBroadcastJoinThreshold", "104857600")  // 100MB
```

---

## Step 5: NULL 값 처리

NULL 값이 핫 키인 경우가 많습니다.

```java
// 1. NULL 분리 처리
Dataset<Row> nonNull = df.filter(col("key").isNotNull());
Dataset<Row> nullRows = df.filter(col("key").isNull());

// 2. non-null만 조인
Dataset<Row> joined = nonNull.join(other, "key");

// 3. null 행 별도 처리 후 합치기
Dataset<Row> result = joined.union(
    nullRows.withColumn("other_col", lit(null))
);
```

---

## 검증

스큐가 해결되었는지 다음 기준으로 확인하세요:

### 성공 기준

| 항목 | 성공 조건 |
|------|----------|
| **Task Duration** | Min/Max 차이 3배 이내 |
| **Shuffle Read Size** | 파티션별 크기 균등 (최대 2배 이내) |
| **작업 완료** | 모든 Task가 SUCCEEDED 상태 |
| **skew_ratio** | 2~3 이하 |

### Spark UI에서 확인

1. **Stages** 탭에서 Task Duration 분포를 확인하세요.
2. Min/Max 차이가 2~3배 이내로 줄었는지 확인하세요.
3. **Event Timeline**에서 Task가 균등하게 분포하는지 확인하세요.

### 코드로 확인

```java
// 처리 후 파티션 분포 재확인
result.groupBy(spark_partition_id())
    .count()
    .agg(
        max("count").alias("max"),
        min("count").alias("min"),
        avg("count").alias("avg")
    )
    .withColumn("skew_ratio", col("max").divide(col("avg")))
    .show();

// skew_ratio가 2~3 이하면 정상
```

---

## 트러블슈팅 체크리스트

### 오류 메시지별 해결 방법

| 증상/로그 메시지 | 원인 | 해결 방법 |
|-----------------|------|----------|
| `Stage X: 199/200 tasks completed` (마지막 Task 지연) | 데이터 스큐 | AQE 활성화 또는 Salting 적용 |
| `FetchFailedException: Too large frame` | 단일 파티션 데이터 과다 | 파티션 분할 또는 Salting |
| `Container killed by YARN` (특정 Task만) | 핫 파티션 메모리 초과 | 핫 키 분산 처리 |
| Spark UI에서 Task Duration Min/Max 10배 이상 차이 | 데이터 스큐 | 스큐 해결 기법 적용 |

### 상황별 권장 해결책

| 상황 | 권장 해결책 |
|------|------------|
| Spark 3.0+ 사용 중 | AQE 스큐 조인 활성화 |
| 작은 테이블과 조인 | 브로드캐스트 조인 |
| 특정 키만 핫 키 | 선택적 Salting |
| NULL 값이 핫 키 | NULL 분리 처리 |
| 모든 키가 고르게 스큐 | 전체 Salting |

---

## 다음 단계

- [셔플 최적화하기](shuffle-optimization/) - 셔플 I/O 최소화
- [성능 튜닝](../concepts/tuning/) - 전체 성능 최적화
