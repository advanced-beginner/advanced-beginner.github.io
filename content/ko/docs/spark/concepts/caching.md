---
title: 캐싱과 영속성
description: "Spark 캐싱과 영속성의 작동 원리와 최적화 전략을 설명합니다."
weight: 7
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- cache() = persist(MEMORY_ONLY), 여러 Action에서 재사용할 데이터 캐싱
- Storage Level: MEMORY_ONLY(빠름), MEMORY_AND_DISK(안정), *_SER(메모리 절약)
- 캐시는 첫 Action 시 저장되고, unpersist()로 해제
- 체크포인트는 Lineage를 끊어 장애 복구와 긴 계보 문제 해결
{{< /callout >}}

**대상 독자**: 반복 연산 최적화가 필요한 데이터 엔지니어

**선수 지식**:
- [Transformation과 Action](transformations-actions/) 지연 평가 이해
- JVM 메모리 구조 기초

---

Spark의 인메모리 컴퓨팅 능력을 활용하여 중간 결과를 캐시하고 재사용하는 방법을 알아봅니다.

## 비유로 이해하는 캐싱

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **cache()** | 자주 쓰는 재료 냉장고 보관 | 매번 장 보러 가는 대신 냉장고에서 바로 꺼내 사용 |
| **persist()** | 보관 위치 선택 | 냉장고(메모리) vs 창고(디스크) vs 진공포장(직렬화) |
| **MEMORY_ONLY** | 냉장 보관 | 빠르게 꺼내 쓰지만 공간 많이 차지 |
| **MEMORY_AND_DISK** | 냉장고 + 창고 | 냉장고 가득 차면 창고로 이동 |
| **직렬화 (_SER)** | 진공 포장 | 부피는 줄지만 사용 시 포장 해체 시간 필요 |
| **checkpoint** | 요리 중간 상태 사진 촬영 | 실수해도 사진 시점부터 다시 시작 가능 |
| **unpersist()** | 냉장고 정리 | 안 쓰는 재료 치워서 공간 확보 |

> **핵심 원리**: 캐싱은 "계산 시간"과 "저장 공간"을 교환하는 것입니다. 자주 사용하는 데이터는 저장해두고, 한 번만 쓰는 데이터는 매번 계산하는 게 효율적입니다.

## 왜 캐싱이 필요한가? (설계 철학)

**질문: Spark는 왜 중간 결과를 자동으로 저장하지 않을까요?**

이 질문에 답하려면 Spark의 지연 평가(Lazy Evaluation) 철학을 이해해야 합니다.

**1. 모든 중간 결과 저장의 문제**

```
100GB 원본 → filter → 80GB → join → 150GB → groupBy → 10GB → 결과
```

만약 모든 중간 결과를 저장한다면 80GB + 150GB + 10GB = **240GB** 추가 저장 필요합니다. 대부분은 한 번만 쓰고 버려질 데이터입니다.

**2. 사용자가 결정하는 것이 효율적**

- **한 번만 사용**: 저장 불필요 (기본 동작)
- **여러 번 사용**: 명시적으로 `cache()` 호출
- **장애 복구 필요**: `checkpoint()` 사용

Spark는 "무엇을 저장할지"를 사용자에게 맡김으로써 불필요한 메모리 낭비를 방지합니다.

**3. 캐시 vs 체크포인트 트레이드오프**

| 특성 | cache() | checkpoint() |
|------|---------|--------------|
| 저장 위치 | 메모리 (또는 디스크) | 신뢰할 수 있는 저장소 (HDFS) |
| Lineage | 유지 (복구 시 재계산) | 끊김 (저장 시점부터 시작) |
| 속도 | 빠름 | 느림 (I/O 필요) |
| 장애 복구 | Lineage 따라 재계산 | 즉시 복구 |
| 사용 시점 | 반복 접근 최적화 | 긴 Lineage 끊기, 장애 대비 |

**핵심 질문**: "이 데이터를 다시 만드는 비용"과 "저장하는 비용" 중 무엇이 큰가?

## 캐싱이란?

**캐싱(Caching)**은 RDD/DataFrame을 메모리나 디스크에 저장하여 후속 연산에서 재사용하는 것입니다.

**캐싱이 필요한 이유**

```java
// 캐싱 없이: 비효율적
Dataset<Row> processed = df
    .filter(col("status").equalTo("ACTIVE"))
    .groupBy("category")
    .agg(sum("amount").alias("total"));

// 같은 데이터를 3번 처리 (각각 전체 재계산)
long count = processed.count();              // Job 1: 전체 계산
processed.show();                            // Job 2: 전체 재계산
processed.write().parquet("output");         // Job 3: 전체 재계산

// 캐싱 사용: 효율적
processed.cache();                           // 캐시 등록
long count = processed.count();              // Job 1: 계산 + 캐싱
processed.show();                            // 캐시에서 읽음 (빠름)
processed.write().parquet("output");         // 캐시에서 읽음 (빠름)
processed.unpersist();                       // 캐시 해제
```

{{< callout type="info" title="핵심 포인트" >}}
- 같은 DataFrame을 여러 Action에서 사용하면 매번 재계산
- `cache()`로 중간 결과를 메모리에 저장하여 재계산 방지
- 첫 Action 호출 시 실제 캐싱 발생 (지연 평가)
- 사용 후 `unpersist()`로 메모리 해제 권장
{{< /callout >}}

## 기본 사용법

**cache()**

```java
Dataset<Row> df = spark.read().parquet("large-data.parquet");

// cache() 호출 - 기본 스토리지 레벨로 캐시
df.cache();

// 첫 Action에서 실제 캐싱 발생
df.count();  // 데이터 처리 + 메모리에 캐시

// 이후 Action은 캐시에서 읽음
df.filter(col("x").gt(10)).count();
df.groupBy("category").count().show();

// 캐시 해제
df.unpersist();
```

**persist()**

스토리지 레벨을 직접 지정할 수 있습니다.

```java
import org.apache.spark.storage.StorageLevel;

// 메모리에만 캐시 (기본값)
df.persist(StorageLevel.MEMORY_ONLY());

// 메모리 + 디스크 (메모리 부족 시 디스크 사용)
df.persist(StorageLevel.MEMORY_AND_DISK());

// 직렬화하여 메모리에 저장 (메모리 절약, CPU 사용)
df.persist(StorageLevel.MEMORY_ONLY_SER());

// 직렬화 + 디스크 백업
df.persist(StorageLevel.MEMORY_AND_DISK_SER());

// 디스크에만 저장
df.persist(StorageLevel.DISK_ONLY());

// 복제본 2개 유지 (장애 대비)
df.persist(StorageLevel.MEMORY_AND_DISK_2());
```

## Storage Level 상세

| Storage Level | 메모리 사용 | 디스크 사용 | 직렬화 | 복제 | 특징 |
|--------------|------------|------------|--------|------|------|
| MEMORY_ONLY | O | X | X | 1 | 가장 빠름, 메모리 많이 사용 |
| MEMORY_AND_DISK | O | O | X | 1 | 메모리 부족 시 디스크로 |
| MEMORY_ONLY_SER | O | X | O | 1 | 메모리 절약, CPU 사용 |
| MEMORY_AND_DISK_SER | O | O | O | 1 | 메모리 절약 + 디스크 백업 |
| DISK_ONLY | X | O | O | 1 | 메모리 사용 안 함 |
| OFF_HEAP | X (off-heap) | X | O | 1 | GC 영향 없음 |
| *_2 | - | - | - | 2 | 복제본 2개 |

**어떤 레벨을 선택할까?**

```
메모리 충분 + 빠른 접근 필요 → MEMORY_ONLY
메모리 제한 + 빠른 접근 필요 → MEMORY_ONLY_SER
메모리 제한 + 안정성 필요 → MEMORY_AND_DISK_SER
메모리 극히 제한 → DISK_ONLY
고가용성 필요 → *_2 계열
```

## 캐싱 vs 체크포인트

**캐싱 (cache/persist)**

```java
df.cache();
```

- **장점**: 빠름, 간편
- **단점**: 장애 시 lineage 재계산 필요
- **사용**: 반복 접근 최적화

**체크포인트 (checkpoint)**

```java
// 체크포인트 디렉토리 설정
spark.sparkContext().setCheckpointDir("hdfs:///checkpoints");

// 체크포인트 저장
df.checkpoint();
// 또는 eager checkpoint (즉시 저장)
df.checkpoint(true);
```

- **장점**: lineage 끊음, 장애 시 즉시 복구
- **단점**: 디스크 I/O 필요
- **사용**: 긴 lineage, 장애 복구 필요 시

**언제 무엇을 사용?**

```java
// 단순 반복 사용 → cache
Dataset<Row> frequently = df.filter(...).cache();
for (int i = 0; i < 10; i++) {
    process(frequently);
}

// 긴 lineage 끊기 → checkpoint
Dataset<Row> stage1 = df.map(...).filter(...).join(...);
stage1 = stage1.checkpoint();  // lineage 끊음

Dataset<Row> stage2 = stage1.map(...).filter(...).join(...);
stage2 = stage2.checkpoint();  // 또 끊음

// 머신러닝 반복 알고리즘 → 둘 다 사용
Dataset<Row> data = loadData().cache();  // 반복 접근

for (int iter = 0; iter < 100; iter++) {
    data = processIteration(data);
    if (iter % 10 == 0) {
        data = data.checkpoint();  // 주기적으로 lineage 끊음
    }
}
```

## 캐시 관리

**캐시 상태 확인**

```java
// 캐시 여부 확인
boolean isCached = spark.catalog().isCached("table_name");

// Spark UI에서 확인
// http://localhost:4040 → Storage 탭
```

**캐시 해제**

```java
// 특정 DataFrame 해제
df.unpersist();

// 블로킹 방식 (해제 완료까지 대기)
df.unpersist(true);

// 테이블 캐시 해제
spark.catalog().uncacheTable("table_name");

// 모든 캐시 해제
spark.catalog().clearCache();
```

**캐시 새로고침**

```java
// 캐시된 테이블 새로고침 (원본 변경 반영)
spark.catalog().refreshTable("table_name");
```

## SQL에서의 캐싱

```sql
-- 테이블 캐시
CACHE TABLE employees;

-- 레이지 캐시 (첫 쿼리 시 캐싱)
CACHE LAZY TABLE employees;

-- 쿼리 결과 캐시
CACHE TABLE active_employees AS
SELECT * FROM employees WHERE status = 'ACTIVE';

-- 캐시 해제
UNCACHE TABLE employees;

-- 모든 캐시 해제
CLEAR CACHE;
```

## 캐싱 모범 사례

**1. 여러 번 사용되는 데이터만 캐시**

```java
// 좋음: 여러 번 사용
Dataset<Row> base = loadAndProcess().cache();
analyze1(base);
analyze2(base);
analyze3(base);
base.unpersist();

// 나쁨: 한 번만 사용 (불필요한 캐싱)
Dataset<Row> oneTime = loadData().cache();
oneTime.write().parquet("output");  // 한 번만 사용
```

**2. 적절한 시점에 캐시**

```java
// 좋음: 비용이 큰 연산 후 캐시
Dataset<Row> expensive = df
    .filter(...)
    .join(otherDf, ...)      // 비용 큰 조인
    .groupBy(...).agg(...)   // 비용 큰 집계
    .cache();                // 여기서 캐시

// 나쁨: 너무 일찍 캐시 (불필요한 데이터 저장)
Dataset<Row> early = df.cache();  // 아직 필터링 전
Dataset<Row> filtered = early.filter(col("needed").equalTo(true));
```

**3. 메모리 모니터링**

```java
// Executor 메모리의 약 60%가 저장/캐시에 사용 (기본)
spark.conf().set("spark.memory.storageFraction", "0.5");  // 50%로 조정

// 캐시 강제 삭제 방지 (메모리 부족 시 캐시 유지)
spark.conf().set("spark.memory.storageFraction", "0.6");
```

**4. 사용 후 즉시 해제**

```java
Dataset<Row> cached = expensive.cache();
try {
    // 캐시된 데이터 사용
    doAnalysis(cached);
} finally {
    cached.unpersist();  // 반드시 해제
}
```

**5. 직렬화 고려**

```java
// 큰 객체가 많은 경우 직렬화가 효율적
Dataset<Row> largeObjects = df.persist(StorageLevel.MEMORY_ONLY_SER());

// Kryo 직렬화 사용 (더 효율적)
spark.conf().set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
spark.conf().set("spark.kryo.registrationRequired", "false");
```

## 캐싱과 파티셔닝

```java
// 캐시 전 파티션 조정 권장
Dataset<Row> optimized = df
    .filter(...)
    .repartition(100)  // 적절한 파티션 수로 조정
    .cache();

// 파티션이 너무 많으면 메모리 오버헤드
// 파티션이 너무 적으면 병렬성 저하
```

## 실전 예제: 머신러닝 파이프라인

```java
public class MLPipelineWithCaching {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("ML Pipeline")
                .master("local[*]")
                .getOrCreate();

        // 1. 데이터 로드 및 전처리 (한 번만)
        Dataset<Row> rawData = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("training-data.csv");

        Dataset<Row> processed = rawData
                .na().fill(0)
                .filter(col("label").isNotNull())
                .withColumn("features", createFeatures());

        // 2. 전처리된 데이터 캐시 (반복 학습에 사용)
        processed.cache();
        System.out.println("데이터 캐싱 완료: " + processed.count() + " 행");

        // 3. 학습/검증 분할
        Dataset<Row>[] splits = processed.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> training = splits[0].cache();
        Dataset<Row> validation = splits[1].cache();

        // 4. 하이퍼파라미터 튜닝 (캐시된 데이터 반복 사용)
        double[] learningRates = {0.1, 0.01, 0.001};
        int[] maxDepths = {5, 10, 15};

        for (double lr : learningRates) {
            for (int depth : maxDepths) {
                // 학습 (캐시에서 읽음 - 빠름)
                trainModel(training, lr, depth);

                // 검증 (캐시에서 읽음 - 빠름)
                double score = evaluate(validation);
                System.out.printf("lr=%.3f, depth=%d, score=%.4f%n", lr, depth, score);
            }
        }

        // 5. 캐시 해제
        training.unpersist();
        validation.unpersist();
        processed.unpersist();

        spark.stop();
    }
}
```

## 트러블슈팅

**메모리 부족으로 캐시 실패**

```
WARN MemoryStore: Not enough space to cache rdd_X in memory!
```

해결:
```java
// 1. 직렬화 사용
df.persist(StorageLevel.MEMORY_AND_DISK_SER());

// 2. 디스크 사용
df.persist(StorageLevel.DISK_ONLY());

// 3. Executor 메모리 증가
spark.conf().set("spark.executor.memory", "8g");

// 4. 불필요한 캐시 해제
anotherDf.unpersist();
```

**캐시가 예상대로 동작하지 않음**

```java
// 주의: cache() 후 새 DataFrame이 생성되면 캐시 적용 안 됨
Dataset<Row> df = spark.read().parquet("data");
df.cache();
Dataset<Row> filtered = df.filter(...);  // 새 DataFrame
// filtered는 캐시 안 됨!

// 올바른 방법
Dataset<Row> df = spark.read().parquet("data").cache();
Dataset<Row> filtered = df.filter(...);  // df가 캐시됨
```

## 다음 단계

- [Structured Streaming](structured-streaming/) - 실시간 데이터 처리
- [성능 튜닝](tuning/) - 전체 성능 최적화

## 관련 문서

- [Transformation과 Action](transformations-actions/) - 지연 평가와 캐싱의 관계
- [파티셔닝과 셔플](partitioning/) - 캐시 전 파티션 최적화
- [RDD 기초](rdd/) - RDD 수준의 persist/cache
- [MLlib](mllib/) - 머신러닝에서의 캐싱 활용
- [용어 사전](../appendix/glossary/) - Persist, Storage Level 용어 정의
