---
title: 파티셔닝과 셔플
description: "파티셔닝과 셔플의 작동 원리와 성능 영향을 설명합니다."
weight: 6
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- 파티션은 데이터의 논리적 분할 단위, 각 파티션이 하나의 Task로 처리
- 셔플은 파티션 간 데이터 재분배로 Spark에서 가장 비용이 높은 연산
- 권장 파티션 크기: 100~200MB, 파티션 수 = 코어 수 x 2~4
- AQE(Spark 3.0+)가 런타임에 파티션 수 자동 조정
{{< /callout >}}

**대상 독자**: Spark 성능 튜닝을 시작하는 데이터 엔지니어

**선수 지식**:
- [아키텍처](architecture/) 문서의 Stage, Task 개념
- [Transformation과 Action](transformations-actions/)의 Wide/Narrow 구분

**소요 시간**: 약 25-30분

---

파티셔닝은 Spark 성능의 핵심입니다. 데이터가 어떻게 분산되는지 이해하고 최적화하는 것이 대규모 데이터 처리의 관건입니다.

## 비유로 이해하는 파티셔닝

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **파티션** | 피자 조각 | 큰 피자를 여러 조각으로 나눠 여러 사람이 동시에 먹을 수 있음 |
| **셔플** | 카드 섞기 후 무늬별 분류 | 52장을 펼쳐서 하트/스페이드/다이아/클럽별로 재배치. 모든 카드를 봐야 함 |
| **repartition** | 피자 다시 자르기 | 8조각을 12조각으로 재분할. 전체를 한번 섞어야 함 |
| **coalesce** | 남은 피자 합치기 | 3조각씩 남은 4판을 2판으로 합침. 옮기기만 하면 됨 |
| **데이터 스큐** | 불균형한 피자 조각 | 한 조각이 거대하면 그 사람만 오래 먹음 |
| **Salting** | 핫케이크 나눠 굽기 | 큰 반죽을 여러 팬에 나눠 동시에 조리 |
| **Broadcast Join** | 메뉴판 복사 배포 | 작은 테이블(메뉴판)을 모든 테이블에 복사해두면 매번 물어볼 필요 없음 |

> **핵심 원리**: 파티션 수는 "병렬성"을, 셔플은 "비용"을 결정합니다. 적절한 파티션 수로 병렬성을 확보하되, 셔플을 최소화하는 것이 성능 최적화의 핵심입니다.

## 왜 파티셔닝이 성능의 핵심인가? (설계 철학)

**질문: 왜 Spark는 데이터를 파티션으로 나누나요?**

**1. 병렬 처리의 단위**
```
10억 행 데이터 + 100 Executor
→ 파티션 100개 = 각 Executor가 1000만 행 처리
→ 파티션 10개 = 90 Executor가 놀고 있음
```

**2. 셔플 비용의 이해**

| 작업 | 비용 | 이유 |
|------|------|------|
| filter | 낮음 | 각 파티션에서 독립 처리 |
| map | 낮음 | 각 파티션에서 독립 처리 |
| groupBy | **높음** | 같은 키를 같은 파티션으로 모아야 함 |
| join | **매우 높음** | 양쪽 데이터를 키별로 재배치 |

셔플은 **네트워크 I/O + 디스크 I/O + 직렬화**를 동반합니다. 10GB 데이터의 groupBy는 최대 20GB(쓰기 + 읽기)의 I/O를 유발할 수 있습니다.

**3. 스큐가 위험한 이유**
```
파티션 1: 100만 행 (1분)
파티션 2: 100만 행 (1분)
파티션 3: 9800만 행 (98분) ← 전체 Job이 98분 대기
```
하나의 거대 파티션이 전체 성능을 결정합니다.

## 파티션이란?

**파티션(Partition)**은 RDD/DataFrame 데이터의 논리적 분할 단위입니다. 각 파티션은 클러스터의 한 노드에서 처리됩니다.

```
DataFrame (1억 행)
├── Partition 0 (1000만 행) → Executor 1, Task 0
├── Partition 1 (1000만 행) → Executor 2, Task 1
├── Partition 2 (1000만 행) → Executor 1, Task 2
├── ...
└── Partition 9 (1000만 행) → Executor 4, Task 9
```

**파티션의 중요성**

| 파티션 수 | 영향 |
|----------|------|
| 너무 적음 | 병렬성 저하, 메모리 부족 가능, 일부 노드만 사용 |
| 너무 많음 | 스케줄링 오버헤드, 작은 태스크 비효율, 셔플 비용 증가 |
| 적절함 | 균형 잡힌 부하 분산, 효율적인 리소스 사용 |

{{< callout type="info" title="핵심 포인트" >}}
- 파티션 1개 = Task 1개 = 병렬 처리 단위
- 너무 적으면 병렬성 저하, 너무 많으면 스케줄링 오버헤드
- 권장: 파티션당 100~200MB, 총 파티션 수 = 코어 수 x 2~4
- `df.rdd().getNumPartitions()`으로 현재 파티션 수 확인
{{< /callout >}}

## 파티션 수 확인 및 조정

**현재 파티션 수 확인**

```java
Dataset<Row> df = spark.read().parquet("data.parquet");
int partitions = df.rdd().getNumPartitions();
System.out.println("파티션 수: " + partitions);

// 각 파티션의 데이터 수
df.mapPartitions(
    iter -> {
        int count = 0;
        while (iter.hasNext()) { iter.next(); count++; }
        return Collections.singletonList(count).iterator();
    },
    Encoders.INT()
).collectAsList().forEach(System.out::println);
```

**파티션 수 결정 가이드라인**

```
권장 파티션 크기: 100MB ~ 200MB
최소 파티션 수: 클러스터 총 코어 수 × 2~4
최대 파티션 수: 데이터 크기(GB) × 2~4
```

예시:
- 100GB 데이터, 50코어 클러스터
- 최소: 50 × 2 = 100 파티션
- 최대: 100 × 4 = 400 파티션
- 권장: 약 200 파티션 (100GB ÷ 200MB × 4)

**파티션 조정**

```java
// repartition - 파티션 수 변경 (셔플 발생)
Dataset<Row> repartitioned = df.repartition(200);

// coalesce - 파티션 수 줄이기 (셔플 없음)
Dataset<Row> coalesced = df.coalesce(100);

// 키 기반 재파티셔닝
Dataset<Row> keyPartitioned = df.repartition(col("department"));
Dataset<Row> keyWithCount = df.repartition(100, col("department"));
```

**repartition vs coalesce**

| 특성 | repartition | coalesce |
|------|------------|----------|
| 셔플 | 발생 | 안 함 |
| 파티션 수 | 늘리기/줄이기 모두 가능 | 줄이기만 가능 |
| 데이터 분포 | 균등 분배 | 불균등할 수 있음 |
| 사용 시점 | 파티션 증가, 균등 분배 필요 시 | 파티션 감소 시 |

```java
// 파일 쓰기 전 파티션 줄이기 (파일 수 제어)
df.coalesce(10)
  .write()
  .parquet("output");
// → 10개의 parquet 파일 생성

// 스큐 해결을 위한 재파티셔닝
df.repartition(200, col("key"))
  .groupBy("key")
  .agg(sum("value"));
```

## 셔플(Shuffle)

**셔플(Shuffle)**은 파티션 간 데이터 재분배 과정입니다. Wide Transformation에서 발생합니다.

**셔플이 발생하는 연산**

```java
// groupBy - 같은 키를 같은 파티션으로
df.groupBy("key").agg(sum("value"));

// join - 같은 키를 같은 파티션으로
df1.join(df2, "key");

// distinct - 중복 제거를 위해 재분배
df.distinct();

// repartition - 명시적 재분배
df.repartition(100);

// orderBy/sort - 전역 정렬
df.orderBy("column");
```

**셔플 과정**

```
Map Phase (셔플 쓰기)
├── 각 Task가 데이터를 키별로 분류
├── 파티션별로 셔플 파일 생성
└── 디스크에 저장 (메모리 버퍼 사용)

Reduce Phase (셔플 읽기)
├── 각 Task가 필요한 파티션의 셔플 파일 읽기
├── 네트워크를 통해 데이터 전송
└── 정렬/집계 수행
```

**셔플의 비용**

셔플은 Spark에서 가장 비용이 높은 연산입니다:

1. **디스크 I/O**: 중간 결과를 디스크에 저장
2. **네트워크 I/O**: 노드 간 데이터 전송
3. **직렬화**: 데이터 직렬화/역직렬화
4. **정렬**: 키 기준 정렬

## 셔플 파티션 설정

```java
// 셔플 파티션 수 설정 (기본값: 200)
spark.conf().set("spark.sql.shuffle.partitions", "400");

// 또는 SparkSession 생성 시
SparkSession spark = SparkSession.builder()
    .appName("App")
    .config("spark.sql.shuffle.partitions", "400")
    .getOrCreate();
```

**Adaptive Query Execution (AQE)**

Spark 3.0+에서는 AQE가 셔플 파티션을 자동 조정합니다.

```java
// AQE 활성화 (Spark 3.2+에서는 기본 활성화)
spark.conf().set("spark.sql.adaptive.enabled", "true");

// 파티션 병합 활성화
spark.conf().set("spark.sql.adaptive.coalescePartitions.enabled", "true");

// 최소 파티션 크기 (병합 기준)
spark.conf().set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "64MB");

// 스큐 조인 최적화
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
```

AQE의 장점:
- 런타임에 실제 데이터 크기를 보고 파티션 수 조정
- 작은 파티션 자동 병합
- 데이터 스큐 자동 처리

## 파티셔닝 전략

**Hash Partitioning**

가장 일반적인 파티셔닝. 키의 해시값을 기준으로 파티션 결정.

```java
// Hash Partitioning
df.repartition(100, col("user_id"));

// 해시 함수: partition = hash(key) % numPartitions
```

**Range Partitioning**

키 값의 범위를 기준으로 파티션 결정. 정렬된 데이터에 적합.

```java
// Range Partitioning
df.repartitionByRange(100, col("timestamp"));

// 예: timestamp 0-100 → Partition 0
//     timestamp 100-200 → Partition 1
```

**Custom Partitioning (RDD)**

RDD에서는 커스텀 파티셔너를 정의할 수 있습니다.

```java
import org.apache.spark.Partitioner;

public class RegionPartitioner extends Partitioner {
    private int numPartitions;

    public RegionPartitioner(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    @Override
    public int numPartitions() {
        return numPartitions;
    }

    @Override
    public int getPartition(Object key) {
        String region = (String) key;
        switch (region) {
            case "ASIA": return 0;
            case "EUROPE": return 1;
            case "AMERICA": return 2;
            default: return 3;
        }
    }
}

// 사용
JavaPairRDD<String, Integer> partitioned =
    pairRDD.partitionBy(new RegionPartitioner(4));
```

## 데이터 스큐(Data Skew)

데이터 스큐는 특정 파티션에 데이터가 집중되는 현상입니다.

**스큐 탐지**

```java
// 파티션별 데이터 수 확인
df.groupBy(spark_partition_id().alias("partition"))
  .count()
  .orderBy(col("count").desc())
  .show();

// 출력:
// +----------+--------+
// |partition |   count|
// +----------+--------+
// |        5 |10000000|  ← 스큐!
// |        3 |   50000|
// |        1 |   45000|
// ...
```

**스큐 해결 방법**

**1. Salting (솔팅)**

```java
import java.util.Random;

// 핫 키에 랜덤 솔트 추가
int saltBuckets = 10;
Random rand = new Random();

Dataset<Row> salted = df.withColumn(
    "salted_key",
    concat(col("key"), lit("_"), lit(rand.nextInt(saltBuckets)))
);

// 솔팅된 키로 집계
Dataset<Row> partialAgg = salted
    .groupBy("salted_key")
    .agg(sum("value").alias("partial_sum"));

// 원래 키로 최종 집계
Dataset<Row> finalResult = partialAgg
    .withColumn("original_key", split(col("salted_key"), "_").getItem(0))
    .groupBy("original_key")
    .agg(sum("partial_sum").alias("total"));
```

**2. Broadcast Join**

작은 테이블과 조인 시 스큐를 회피합니다.

```java
import static org.apache.spark.sql.functions.broadcast;

// 작은 테이블을 브로드캐스트
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);
```

**3. AQE 스큐 조인**

```java
// Spark 3.0+ AQE 스큐 조인 활성화
spark.conf().set("spark.sql.adaptive.enabled", "true");
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
spark.conf().set("spark.sql.adaptive.skewJoin.skewedPartitionFactor", "5");
spark.conf().set("spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes", "256MB");
```

**4. 두 단계 집계**

```java
// 1단계: 파티션 내 부분 집계
Dataset<Row> partial = df
    .repartition(1000)  // 많은 파티션으로 분산
    .groupBy("key", spark_partition_id().alias("part"))
    .agg(sum("value").alias("partial_sum"));

// 2단계: 최종 집계
Dataset<Row> result = partial
    .groupBy("key")
    .agg(sum("partial_sum").alias("total"));
```

## 조인과 파티셔닝

**조인 전략**

```java
// 1. Broadcast Hash Join (작은 테이블)
// 자동 또는 명시적으로 브로드캐스트
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "100MB");
df1.join(broadcast(df2), "key");

// 2. Sort-Merge Join (큰 테이블)
// 양쪽 테이블을 키로 정렬 후 머지
// 기본 조인 전략

// 3. Shuffle Hash Join
// 한쪽만 해시 테이블 생성
df1.join(df2.hint("shuffle_hash"), "key");
```

**조인 최적화**

```java
// 조인 전 필터링 (셔플 데이터 감소)
Dataset<Row> filtered1 = df1.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> filtered2 = df2.filter(col("type").equalTo("VALID"));
Dataset<Row> joined = filtered1.join(filtered2, "key");

// 필요한 컬럼만 선택
Dataset<Row> slim1 = df1.select("key", "needed_col1");
Dataset<Row> slim2 = df2.select("key", "needed_col2");
Dataset<Row> joined = slim1.join(slim2, "key");

// 사전 파티셔닝 (같은 키로 파티셔닝되면 셔플 회피)
Dataset<Row> part1 = df1.repartition(100, col("key"));
Dataset<Row> part2 = df2.repartition(100, col("key"));
Dataset<Row> joined = part1.join(part2, "key");
// → 이미 같은 키가 같은 파티션에 있으므로 셔플 불필요
```

## 파일 파티셔닝

데이터를 저장할 때 파일 시스템 레벨에서도 파티셔닝할 수 있습니다.

```java
// 파티션 컬럼으로 저장
df.write()
    .partitionBy("year", "month")
    .parquet("output/data");

// 생성되는 디렉토리 구조:
// output/data/
// ├── year=2024/
// │   ├── month=01/
// │   │   ├── part-00000.parquet
// │   │   └── part-00001.parquet
// │   ├── month=02/
// │   ...
// └── year=2025/
//     ...

// 파티션 프루닝 (해당 파티션만 읽음)
Dataset<Row> filtered = spark.read()
    .parquet("output/data")
    .filter(col("year").equalTo(2024).and(col("month").equalTo(1)));
// → year=2024/month=01/ 디렉토리만 스캔
```

**버케팅(Bucketing)**

```java
// 버케팅으로 저장 (조인 최적화)
df.write()
    .bucketBy(100, "user_id")
    .sortBy("timestamp")
    .saveAsTable("user_events");

// 같은 버케팅으로 저장된 테이블 조인 시 셔플 없음
Dataset<Row> users = spark.table("users").bucketBy(100, "user_id");
Dataset<Row> events = spark.table("user_events");
Dataset<Row> joined = users.join(events, "user_id");
// → 셔플 없이 조인 (같은 user_id가 같은 버킷에 있음)
```

## 모니터링

**Spark UI에서 셔플 확인**

1. **Stages 탭**: 각 Stage의 셔플 읽기/쓰기 크기
2. **Tasks 탭**: 개별 Task의 셔플 데이터 크기
3. **Executors 탭**: Executor별 셔플 데이터 통계

**셔플 메트릭**

주목할 메트릭:
- **Shuffle Write**: Stage에서 출력한 셔플 데이터
- **Shuffle Read**: Stage에서 읽은 셔플 데이터
- **Shuffle Spill (Memory)**: 메모리 스필 크기
- **Shuffle Spill (Disk)**: 디스크 스필 크기 (많으면 메모리 부족)

## 다음 단계

- [캐싱과 영속성](caching/) - 중간 결과 저장으로 재계산 방지
- [성능 튜닝](tuning/) - 전체적인 성능 최적화 전략

## 관련 문서

- [아키텍처](architecture/) - 분산 처리 구조 이해
- [Transformation과 Action](transformations-actions/) - Narrow/Wide Transformation
- [Spark SQL](spark-sql/) - SQL 쿼리의 파티션 처리
- [배포와 클러스터 관리](deployment/) - 클러스터 환경에서의 파티션 설정
- [용어 사전](../appendix/glossary/) - Partition, Shuffle 용어 정의
