---
title: OutOfMemoryError 해결하기
weight: 1
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "Spark에서 발생하는 OOM 오류를 진단하고 해결하는 단계별 가이드"
---

{{< callout type="info" >}}
**예상 소요 시간**: 약 15분
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Driver OOM**: `collect()` 결과 크기 줄이기, `spark.driver.memory` 증가
- **Executor OOM**: 파티션 수 증가 (`repartition`), `spark.executor.memory` 증가
- **진단 우선**: Spark UI에서 어디서 OOM이 발생하는지 먼저 확인
{{< /callout >}}

## 문제 정의

Spark 애플리케이션 실행 중 다음과 같은 오류가 발생합니다:

```
java.lang.OutOfMemoryError: Java heap space
```

또는:

```
Container killed by YARN for exceeding memory limits
```

이 가이드에서는 OOM 오류의 원인을 진단하고 해결하는 방법을 단계별로 설명합니다.

---

## 전제 조건

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| **Spark 버전** | 2.4 이상 (3.x 권장) | `spark-submit --version` |
| **Java 버전** | 8, 11, 또는 17 | `java -version` |
| **Spark UI** | 접근 가능 | 브라우저에서 `http://localhost:4040` 열기 |
| **권한** | Spark 설정 변경 가능 | spark-submit 실행 권한 확인 |

**지원 환경**: Linux, macOS, Windows (WSL2 권장)

### 환경 확인

다음 명령어로 환경이 준비되었는지 확인하세요:

```bash
# Java 버전 확인
java -version

# Spark 버전 확인
spark-submit --version

# Spark UI 접근 확인 (애플리케이션 실행 중일 때)
curl -s http://localhost:4040/api/v1/applications | head -1
```

---

## Step 1: OOM 발생 위치 확인

OOM은 **Driver**와 **Executor**에서 각각 다른 원인으로 발생합니다. 먼저 어디서 발생했는지 확인하세요.

### Driver OOM 확인

오류 메시지에 다음이 포함되어 있다면 Driver OOM입니다:

```
Exception in thread "main" java.lang.OutOfMemoryError
```

또는 `collect()`, `toPandas()`, `show()` 호출 중 발생했다면 Driver OOM일 가능성이 높습니다.

### Executor OOM 확인

오류 메시지에 다음이 포함되어 있다면 Executor OOM입니다:

```
ExecutorLostFailure (executor X exited caused by one of the running tasks)
Lost task X.X in stage X.X: ExecutorLostFailure
Container killed by YARN for exceeding memory limits
```

---

## Step 2: Driver OOM 해결

Driver OOM은 주로 **대량의 데이터를 Driver로 수집**할 때 발생합니다. 아래 단계를 순서대로 따르세요.

### 2.1 collect() 사용 점검

먼저 코드에서 `collect()` 호출을 확인하세요.

**문제 코드:**
```java
// 수백만 건의 데이터를 Driver로 수집 - OOM 발생!
List<Row> allData = df.collect();
```

**해결 방법:**

```java
// 1. take()로 제한된 수만 수집
List<Row> sample = df.take(1000);

// 2. 결과를 파일로 저장
df.write().parquet("output/result");

// 3. 집계 후 수집 (데이터량 감소)
Dataset<Row> summary = df.groupBy("category")
    .agg(count("*"), sum("amount"));
List<Row> result = summary.collect();  // 집계 결과만 수집
```

### 2.2 Driver 메모리 증가

수집하는 데이터가 정당하게 크다면 Driver 메모리를 증가시키세요:

```bash
# spark-submit 사용 시
spark-submit --driver-memory 8g myapp.jar

# 코드에서 설정
SparkSession spark = SparkSession.builder()
    .config("spark.driver.memory", "8g")
    .getOrCreate();
```

### 2.3 maxResultSize 확인

Driver로 반환되는 결과 크기 제한을 확인하세요:

```java
// 기본값은 1g, 필요시 증가
.config("spark.driver.maxResultSize", "4g")
```

---

## Step 3: Executor OOM 해결

Executor OOM은 주로 **파티션이 너무 크거나** **메모리가 부족**할 때 발생합니다.

### 3.1 파티션 크기 확인

```java
// 현재 파티션 수 확인
int numPartitions = df.rdd().getNumPartitions();
System.out.println("파티션 수: " + numPartitions);

// 파티션별 데이터 분포 확인
df.groupBy(spark_partition_id())
    .count()
    .orderBy(col("count").desc())
    .show(20);
```

### 3.2 파티션 수 증가

파티션당 100~200MB가 되도록 파티션 수를 조정하세요:

```java
// 파티션 수 증가 (셔플 발생)
Dataset<Row> repartitioned = df.repartition(200);

// 또는 coalesce로 감소 (셔플 없음, 증가는 불가)
Dataset<Row> coalesced = df.coalesce(100);
```

**권장 파티션 수 계산:**
```
파티션 수 = 데이터 크기(MB) / 200
```

예: 40GB 데이터 → 40,000 / 200 = 200 파티션

### 3.3 Executor 메모리 증가

{{< callout type="warning" >}}
**주의**: Executor 메모리를 클러스터 노드 물리 메모리의 75% 이상으로 설정하지 마세요. YARN/Kubernetes 오버헤드로 인해 Container가 강제 종료될 수 있습니다.
{{< /callout >}}

```bash
# spark-submit
spark-submit \
  --executor-memory 8g \
  --executor-cores 4 \
  myapp.jar
```

**코어당 5GB 메모리 규칙:**
```
executor-memory = executor-cores × 5GB
```

예: 4코어 → 20GB 메모리 권장

### 3.4 Off-Heap 메모리 활성화

대용량 캐시나 셔플에서 GC 부담을 줄이려면:

```java
SparkSession spark = SparkSession.builder()
    .config("spark.memory.offHeap.enabled", "true")
    .config("spark.memory.offHeap.size", "4g")
    .getOrCreate();
```

---

## Step 4: 특수 상황 해결

### 4.1 브로드캐스트 변수로 인한 OOM

큰 테이블을 브로드캐스트하면 모든 Executor에 복사되어 OOM 발생:

```java
// 문제: 1GB 테이블 브로드캐스트
df.join(broadcast(largeTable), "key");  // OOM!

// 해결: 브로드캐스트 임계값 확인
// 기본값 10MB, 초과하면 자동 브로드캐스트 안 함
.config("spark.sql.autoBroadcastJoinThreshold", "10485760")
```

### 4.2 UDF에서 대용량 객체 생성

```java
// 문제: 각 행마다 새 객체 생성
df.withColumn("result", udf(row -> {
    List<String> huge = loadHugeList();  // 매번 생성!
    return process(row, huge);
}));

// 해결: foreachPartition에서 한 번만 생성
df.foreachPartition(partition -> {
    List<String> huge = loadHugeList();  // 파티션당 1번
    while (partition.hasNext()) {
        process(partition.next(), huge);
    }
});
```

### 4.3 윈도우 함수 OOM

큰 윈도우 프레임에서 OOM 발생:

```java
// 문제: 전체 파티션에 대한 윈도우
WindowSpec unbounded = Window.partitionBy("user_id")
    .orderBy("timestamp")
    .rowsBetween(Window.unboundedPreceding, Window.unboundedFollowing);

// 해결: 윈도우 프레임 제한
WindowSpec bounded = Window.partitionBy("user_id")
    .orderBy("timestamp")
    .rowsBetween(-100, 100);  // 앞뒤 100개로 제한
```

---

## 검증

OOM이 해결되었는지 다음 기준으로 확인하세요:

### 성공 기준

| 항목 | 성공 조건 |
|------|----------|
| **작업 완료** | 모든 Stage가 SUCCEEDED 상태 |
| **메모리 사용량** | Executor 메모리 사용률 80% 이하 |
| **GC 시간** | 전체 실행 시간의 10% 미만 |
| **오류 로그** | OOM 관련 메시지 없음 |

### 확인 방법

1. **Spark UI 확인**: Executors 탭에서 메모리 사용량을 확인하세요.
   - **Storage Memory** 열에서 사용량 확인
   - 빨간색 경고가 없어야 합니다

2. **작업 완료 확인**: Jobs 탭에서 모든 Stage가 녹색(SUCCEEDED)인지 확인하세요.

3. **로그 확인**: 다음 명령어로 OOM 관련 오류가 없는지 확인하세요.

```bash
# 로그에서 OOM 확인 (결과가 없으면 성공)
grep -i "outofmemory\|oom\|killed" spark-logs/*.log

# 예상 결과: 아무것도 출력되지 않음
```

---

## 트러블슈팅 체크리스트

### 오류 메시지별 해결 방법

| 오류 메시지 | 원인 | 해결 방법 |
|------------|------|----------|
| `java.lang.OutOfMemoryError: Java heap space` (Driver) | Driver에서 대량 데이터 수집 | `collect()` → `take(n)` 또는 파일 저장 |
| `java.lang.OutOfMemoryError: Java heap space` (Executor) | 파티션 크기 과다 | `repartition`으로 파티션 증가 |
| `Container killed by YARN for exceeding memory limits` | YARN 메모리 오버헤드 부족 | `spark.executor.memoryOverhead` 증가 |
| `java.lang.OutOfMemoryError: GC overhead limit exceeded` | GC에 CPU 90% 이상 사용 | 메모리 증가 또는 Off-Heap 활성화 |
| `ExecutorLostFailure (executor X exited caused by one of the running tasks)` | Executor 메모리 부족 | Executor 메모리 증가, 파티션 크기 감소 |
| `Total size of serialized results is bigger than spark.driver.maxResultSize` | Driver 결과 크기 초과 | `spark.driver.maxResultSize` 증가 또는 결과 크기 줄이기 |

### 빠른 진단 표

| 증상 | 확인 사항 | 해결 방법 |
|------|----------|----------|
| Driver OOM | `collect()` 호출 여부 | `take(n)` 또는 파일 저장 |
| Executor OOM | 파티션당 크기 | `repartition`으로 파티션 증가 |
| GC Overhead | GC 시간 > 10% | 메모리 증가 또는 Off-Heap 활성화 |
| YARN Container killed | 메모리 오버헤드 | `spark.executor.memoryOverhead` 증가 |

---

## 다음 단계

- [데이터 스큐 해결하기](data-skew/) - 특정 파티션에 데이터 집중 문제
- [FAQ](../appendix/faq/) - 기타 오류 해결
