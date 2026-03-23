---
title: Structured Streaming
description: "Structured Streaming의 작동 원리와 스트림 처리 모델을 설명합니다."
weight: 8
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- Structured Streaming은 스트림을 무한히 추가되는 테이블로 처리
- 배치와 동일한 DataFrame API 사용 (readStream/writeStream만 변경)
- Output Mode: append(새 행), complete(전체), update(변경분)
- Watermark로 늦게 도착하는 데이터(late data) 처리
{{< /callout >}}

**대상 독자**: 실시간 데이터 처리가 필요한 데이터 엔지니어

**선수 지식**:
- [DataFrame과 Dataset](dataframe-dataset/) API
- Kafka 기본 개념 (선택 사항)
- 이벤트 시간 vs 처리 시간 개념

**소요 시간**: 약 25-30분

---

Structured Streaming은 Spark의 스트림 처리 엔진입니다. 배치 처리와 동일한 DataFrame/Dataset API를 사용하여 실시간 데이터를 처리합니다.

## 비유로 이해하는 Structured Streaming

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **Structured Streaming** | 끝없이 추가되는 회계 장부 | 새 거래가 계속 기록되지만, 장부 읽는 방법은 동일 |
| **Watermark** | 출석부 마감 시간 | "10분 늦으면 지각 처리" — 너무 늦은 데이터는 무시 |
| **Output Mode** | 회의록 작성 방식 | append(새 내용만), complete(전체 다시), update(수정분만) |
| **Trigger** | 알람 시계 | 얼마나 자주 장부를 정산할지 결정 |
| **Window** | 시간대별 정산 | "오전/오후별 매출", "1시간 단위 방문자" |
| **Checkpoint** | 중간 저장 (게임 세이브) | 문제 발생 시 여기서부터 재시작 |
| **Stream-Stream Join** | 두 줄 서기 합류 | 광고 노출 기록과 클릭 기록을 시간 내 매칭 |

## 왜 테이블 모델인가? (설계 철학)

Spark가 스트림을 **무한 테이블**로 모델링한 이유가 있습니다.

**기존 스트리밍의 문제점**

```
기존 스트림 처리 (Storm, Flink 초기):
├── 레코드 단위 처리 (한 건씩)
├── 배치와 완전히 다른 API
├── 동일 로직을 두 번 구현 (배치용 + 스트림용)
└── "Lambda 아키텍처" 복잡성
```

**테이블 모델의 해결책**

| 문제 | 테이블 모델 해결 |
|------|-----------------|
| API 이중화 | 배치/스트림 동일한 DataFrame API |
| 학습 비용 | 배치 알면 스트리밍도 가능 |
| 코드 중복 | 동일 로직 재사용 |
| 테스트 어려움 | 배치 테스트로 스트림 로직 검증 |

```java
// 핵심 통찰: read() → readStream(), write() → writeStream()만 바꾸면 됨
// 이것이 가능한 이유는 스트림을 "끝없이 추가되는 테이블"로 추상화했기 때문
```

**Watermark의 존재 이유**

```
실세계 문제:
├── 이벤트 발생 시간 ≠ 도착 시간
├── 네트워크 지연, 디바이스 오프라인 등
├── 영원히 기다릴 수 없음
└── 언제 "충분히 기다렸다"고 판단할까?

Watermark 해결책:
├── "max(event_time) - threshold" 계산
├── 이보다 오래된 데이터는 무시
├── 정확성 vs 지연 트레이드오프
└── 비즈니스 요구에 맞게 threshold 조정
```

{{< callout type="info" title="설계 원칙" >}}
**"배치와 스트리밍은 같은 문제의 다른 표현"** — Spark는 시간 축만 다르고 본질적으로 같은 데이터 처리라는 통찰을 기반으로, 하나의 API로 두 세계를 통합했습니다.
{{< /callout >}}

## Structured Streaming이란?

**Structured Streaming**은 스트림 데이터를 무한히 추가되는 테이블로 취급합니다. 새 데이터가 도착하면 증분 처리(incremental processing)를 수행합니다.

**핵심 개념**

```
입력 스트림 (무한 테이블)
    ↓
[데이터 도착] → [증분 처리] → [결과 업데이트]
    ↓
출력 (지속적으로 업데이트되는 결과 테이블)
```

**배치 vs 스트리밍 코드 비교**

```java
// 배치 처리 (정적 데이터)
Dataset<Row> batchDf = spark.read()
        .json("input/data.json");

Dataset<Row> result = batchDf
        .groupBy("category")
        .count();

result.write().parquet("output");

// 스트림 처리 (동적 데이터) - 거의 동일!
Dataset<Row> streamDf = spark.readStream()  // readStream으로 변경
        .json("input/");

Dataset<Row> result = streamDf              // 동일한 처리 로직
        .groupBy("category")
        .count();

result.writeStream()                        // writeStream으로 변경
        .outputMode("complete")
        .format("console")
        .start()
        .awaitTermination();
```

{{< callout type="info" title="핵심 포인트" >}}
- 스트림은 무한히 추가되는 테이블로 모델링
- `read()` → `readStream()`, `write()` → `writeStream()`으로 변경만 하면 됨
- 동일한 DataFrame API로 배치와 스트리밍 코드 통합
- 증분 처리(incremental processing)로 효율적인 스트림 처리
{{< /callout >}}

## 기본 사용법

**스트림 읽기**

```java
SparkSession spark = SparkSession.builder()
        .appName("Structured Streaming")
        .master("local[*]")
        .getOrCreate();

// 파일 소스 (디렉토리 모니터링)
Dataset<Row> fileStream = spark.readStream()
        .schema(schema)  // 스트림에서는 스키마 필수
        .json("input/");

// Kafka 소스
Dataset<Row> kafkaStream = spark.readStream()
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", "topic-name")
        .load();

// Socket 소스 (테스트용)
Dataset<Row> socketStream = spark.readStream()
        .format("socket")
        .option("host", "localhost")
        .option("port", 9999)
        .load();

// Rate 소스 (테스트용 - 초당 지정 수의 행 생성)
Dataset<Row> rateStream = spark.readStream()
        .format("rate")
        .option("rowsPerSecond", 10)
        .load();
```

**스트림 처리**

스트림 DataFrame에 일반 DataFrame과 동일한 연산 적용:

```java
// 필터링
Dataset<Row> filtered = stream.filter(col("value").gt(100));

// 변환
Dataset<Row> transformed = stream
        .withColumn("timestamp", current_timestamp())
        .withColumn("doubled", col("value").multiply(2));

// 집계 (상태 유지)
Dataset<Row> aggregated = stream
        .groupBy("category")
        .agg(
            count("*").alias("count"),
            sum("value").alias("total")
        );
```

**스트림 쓰기**

```java
StreamingQuery query = result.writeStream()
        .outputMode("append")           // 출력 모드
        .format("parquet")              // 출력 포맷
        .option("path", "output/")      // 출력 경로
        .option("checkpointLocation", "checkpoint/")  // 체크포인트 필수
        .trigger(Trigger.ProcessingTime("10 seconds"))  // 트리거
        .start();

// 쿼리 종료 대기
query.awaitTermination();

// 또는 백그라운드 실행
// query.isActive(), query.stop() 등으로 관리
```

## 출력 모드 (Output Mode)

| 모드 | 설명 | 사용 시점 |
|------|------|----------|
| **append** | 새 행만 출력 | 집계 없는 단순 변환 |
| **complete** | 전체 결과 테이블 출력 | 집계 쿼리 |
| **update** | 변경된 행만 출력 | 집계 쿼리 (일부 싱크만 지원) |

```java
// append - 새 행만 (집계 없을 때)
filtered.writeStream()
    .outputMode("append")
    .format("parquet")
    .start();

// complete - 전체 결과 (집계 시)
aggregated.writeStream()
    .outputMode("complete")
    .format("console")
    .start();

// update - 변경된 것만 (집계 시)
aggregated.writeStream()
    .outputMode("update")
    .format("console")
    .start();
```

## 트리거 (Trigger)

데이터 처리 빈도를 제어합니다.

```java
import org.apache.spark.sql.streaming.Trigger;

// 기본 - 가능한 빨리 (마이크로 배치)
.trigger(Trigger.ProcessingTime(0))

// 지정 간격
.trigger(Trigger.ProcessingTime("10 seconds"))
.trigger(Trigger.ProcessingTime("1 minute"))

// 한 번만 실행 (배치처럼)
.trigger(Trigger.Once())

// 사용 가능한 데이터 모두 처리 후 종료
.trigger(Trigger.AvailableNow())

// 연속 처리 (밀리초 지연, 실험적)
.trigger(Trigger.Continuous("1 second"))
```

## Kafka 연동

**Kafka에서 읽기**

```java
Dataset<Row> kafkaStream = spark.readStream()
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", "my-topic")  // 단일 토픽
        // .option("subscribePattern", "topic.*")  // 패턴
        // .option("assign", "{\"topic\":[0,1,2]}")  // 특정 파티션
        .option("startingOffsets", "earliest")  // earliest, latest, 또는 JSON
        .load();

// Kafka 메시지 구조:
// key (binary), value (binary), topic, partition, offset, timestamp, ...

// 값 파싱
Dataset<Row> parsed = kafkaStream
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        .select(from_json(col("value"), schema).alias("data"))
        .select("data.*");
```

**Kafka에 쓰기**

```java
// 결과를 Kafka로 전송
result
    .selectExpr("CAST(key AS STRING)", "to_json(struct(*)) AS value")
    .writeStream()
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "output-topic")
    .option("checkpointLocation", "checkpoint/kafka-output")
    .start();
```

## 윈도우 연산

시간 기반 집계를 위한 윈도우 함수입니다.

**Tumbling Window (텀블링 윈도우)**

겹치지 않는 고정 크기 윈도우:

```java
Dataset<Row> windowedCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        window(col("timestamp"), "5 minutes"),  // 5분 윈도우
        col("category")
    )
    .count();

// 결과:
// +------------------------------------------+--------+-----+
// |window                                    |category|count|
// +------------------------------------------+--------+-----+
// |{2024-01-01 10:00:00, 2024-01-01 10:05:00}|A       |  150|
// |{2024-01-01 10:05:00, 2024-01-01 10:10:00}|A       |  180|
// +------------------------------------------+--------+-----+
```

**Sliding Window (슬라이딩 윈도우)**

겹치는 윈도우:

```java
Dataset<Row> slidingCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        window(col("timestamp"), "10 minutes", "5 minutes"),  // 10분 윈도우, 5분 슬라이드
        col("category")
    )
    .count();

// 각 이벤트가 2개 윈도우에 포함될 수 있음
```

**Session Window (세션 윈도우)**

활동 기반 동적 윈도우:

```java
// Spark 3.2+
Dataset<Row> sessionCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        session_window(col("timestamp"), "5 minutes"),  // 5분 비활성 시 세션 종료
        col("user_id")
    )
    .count();
```

## Watermark

늦게 도착하는 데이터(late data)를 처리하기 위한 메커니즘입니다.

```java
Dataset<Row> result = stream
    .withWatermark("eventTime", "10 minutes")  // 10분까지 지연 허용
    .groupBy(
        window(col("eventTime"), "5 minutes"),
        col("category")
    )
    .count();

// 워터마크 = max(eventTime) - 10 minutes
// 워터마크보다 오래된 데이터는 무시됨
```

**Watermark 동작**

```
이벤트 시간 순서:
10:00 → 10:05 → 10:03 (늦음) → 10:10 → 09:55 (매우 늦음)

워터마크 = 10:00 (10분 지연 허용)
- 10:03 이벤트: 워터마크(10:00) 이후 → 처리됨
- 09:55 이벤트: 워터마크(10:00) 이전 → 무시됨
```

## 상태 관리

집계 쿼리는 상태(state)를 유지합니다.

**상태 저장소 설정**

```java
// RocksDB 상태 저장소 사용 (대용량 상태에 적합)
spark.conf().set(
    "spark.sql.streaming.stateStore.providerClass",
    "org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider"
);

// 상태 저장소 메모리 설정
spark.conf().set("spark.sql.streaming.stateStore.rocksdb.memory.mb", "256");
```

**상태 타임아웃**

```java
// 그룹 상태에 타임아웃 설정 (mapGroupsWithState/flatMapGroupsWithState)
.groupByKey(row -> row.getString(0), Encoders.STRING())
.mapGroupsWithState(
    mappingFunc,
    Encoders.bean(State.class),
    Encoders.bean(Output.class),
    GroupStateTimeout.ProcessingTimeTimeout()  // 또는 EventTimeTimeout
);
```

## 조인

**Stream-Static 조인**

스트림과 정적 데이터 조인:

```java
Dataset<Row> staticDf = spark.read().parquet("dimension-data");

Dataset<Row> enriched = stream.join(
    staticDf,
    stream.col("product_id").equalTo(staticDf.col("id")),
    "left"
);
```

**Stream-Stream 조인**

두 스트림 조인 (워터마크 필수):

```java
Dataset<Row> impressions = spark.readStream()
    .format("kafka")
    .option("subscribe", "impressions")
    .load()
    .withWatermark("timestamp", "2 hours");

Dataset<Row> clicks = spark.readStream()
    .format("kafka")
    .option("subscribe", "clicks")
    .load()
    .withWatermark("timestamp", "3 hours");

// 시간 범위 조인 조건
Dataset<Row> joined = impressions.join(
    clicks,
    expr("""
        impressionId = clickImpressionId AND
        clickTime >= impressionTime AND
        clickTime <= impressionTime + interval 1 hour
    """),
    "leftOuter"
);
```

## 쿼리 모니터링

```java
StreamingQuery query = result.writeStream()
    .format("console")
    .start();

// 쿼리 상태 확인
System.out.println("Query ID: " + query.id());
System.out.println("Run ID: " + query.runId());
System.out.println("Is Active: " + query.isActive());
System.out.println("Status: " + query.status());

// 진행 상황
StreamingQueryProgress progress = query.lastProgress();
if (progress != null) {
    System.out.println("Input rows: " + progress.numInputRows());
    System.out.println("Processing rate: " + progress.processedRowsPerSecond());
}

// 쿼리 중지
query.stop();
```

**쿼리 리스너**

```java
spark.streams().addListener(new StreamingQueryListener() {
    @Override
    public void onQueryStarted(QueryStartedEvent event) {
        System.out.println("Query started: " + event.id());
    }

    @Override
    public void onQueryProgress(QueryProgressEvent event) {
        System.out.println("Progress: " + event.progress().numInputRows() + " rows");
    }

    @Override
    public void onQueryTerminated(QueryTerminatedEvent event) {
        System.out.println("Query terminated: " + event.id());
    }
});
```

## 실전 예제: 실시간 매출 집계

```java
public class RealTimeSalesAggregation {
    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession.builder()
                .appName("Real-Time Sales")
                .master("local[*]")
                .getOrCreate();

        // 스키마 정의
        StructType schema = new StructType()
                .add("orderId", StringType, false)
                .add("productId", StringType, false)
                .add("category", StringType, false)
                .add("amount", DoubleType, false)
                .add("timestamp", TimestampType, false);

        // Kafka에서 주문 데이터 읽기
        Dataset<Row> orders = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "orders")
                .option("startingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING)")
                .select(from_json(col("value"), schema).alias("order"))
                .select("order.*")
                .withWatermark("timestamp", "5 minutes");

        // 5분 윈도우별 카테고리 매출 집계
        Dataset<Row> salesByCategory = orders
                .groupBy(
                    window(col("timestamp"), "5 minutes"),
                    col("category")
                )
                .agg(
                    count("*").alias("orderCount"),
                    sum("amount").alias("totalSales"),
                    avg("amount").alias("avgOrderValue")
                );

        // 콘솔 출력 (디버깅용)
        StreamingQuery consoleQuery = salesByCategory.writeStream()
                .outputMode("update")
                .format("console")
                .option("truncate", false)
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .start();

        // Kafka로 결과 전송
        StreamingQuery kafkaQuery = salesByCategory
                .selectExpr("to_json(struct(*)) AS value")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("topic", "sales-summary")
                .option("checkpointLocation", "checkpoint/sales-summary")
                .outputMode("update")
                .start();

        spark.streams().awaitAnyTermination();
    }
}
```

## 관련 문서

- [Kafka 핵심 구성요소]({{< relref "/docs/kafka/concepts/core-components" >}}) - Structured Streaming의 주요 소스/싱크인 Kafka의 Topic, Partition, Consumer Group 개념

## 다음 단계

- [MLlib](mllib/) - Spark로 머신러닝
- [성능 튜닝](tuning/) - 스트리밍 성능 최적화
