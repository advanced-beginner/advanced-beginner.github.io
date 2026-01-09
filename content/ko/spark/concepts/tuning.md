---
title: 성능 튜닝
weight: 10
lastmod: "2026-01-09"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Spark 애플리케이션의 성능을 최적화하는 전략과 구체적인 설정 방법을 알아봅니다.

#### 튜닝 원칙

**튜닝 순서**

1. **코드 최적화** — 알고리즘과 로직 개선
2. **데이터 구조 최적화** — 파티셔닝, 스키마 최적화
3. **리소스 설정** — 메모리, CPU, 파티션 수
4. **세부 설정** — Spark 설정 파라미터

**측정 도구**

- **Spark UI**: Job, Stage, Task 분석
- **Event Log**: 상세 실행 이력
- **Metrics**: Executor 메트릭 모니터링

#### 코드 수준 최적화

**1. 셔플 최소화**

```java
// 나쁨: 불필요한 셔플
Dataset<Row> result = df
    .repartition(100)  // 셔플
    .groupBy("key")    // 셔플
    .agg(sum("value"))
    .orderBy("key");   // 셔플

// 좋음: 필요한 셔플만
Dataset<Row> result = df
    .groupBy("key")
    .agg(sum("value"));
// orderBy는 정말 필요할 때만
```

**2. 조기 필터링**

```java
// 나쁨: 조인 후 필터
Dataset<Row> joined = large.join(small, "key")
    .filter(col("status").equalTo("ACTIVE"));

// 좋음: 필터 후 조인
Dataset<Row> filtered = large.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> joined = filtered.join(small, "key");
```

**3. 컬럼 선택**

```java
// 나쁨: 모든 컬럼 유지
Dataset<Row> result = df.join(other, "id")
    .groupBy("category")
    .agg(sum("value"));

// 좋음: 필요한 컬럼만
Dataset<Row> result = df.select("id", "category", "value")
    .join(other.select("id"), "id")
    .groupBy("category")
    .agg(sum("value"));
```

**4. Broadcast Join**

```java
import static org.apache.spark.sql.functions.broadcast;

// 작은 테이블(수십 MB)은 브로드캐스트
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);

// 자동 브로드캐스트 임계값 설정
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "100MB");
```

**5. 캐싱 활용**

```java
// 반복 사용 데이터 캐시
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
processed.cache();

// 여러 번 사용
process1(processed);
process2(processed);
process3(processed);

processed.unpersist();
```

#### 데이터 구조 최적화

**1. 파일 포맷**

| 포맷 | 압축 | 컬럼 프루닝 | 용도 |
|------|------|------------|------|
| Parquet | 높음 | 지원 | 분석 쿼리 (권장) |
| ORC | 높음 | 지원 | Hive 호환 |
| Avro | 보통 | 미지원 | 스트리밍, 스키마 진화 |
| JSON | 없음 | 미지원 | 호환성 필요 시 |
| CSV | 없음 | 미지원 | 간단한 데이터 교환 |

```java
// Parquet 저장 (권장)
df.write()
    .mode("overwrite")
    .option("compression", "snappy")  // snappy, gzip, zstd
    .parquet("output/data");
```

**2. 파티셔닝 전략**

```java
// 쿼리 패턴에 맞는 파티셔닝
df.write()
    .partitionBy("year", "month")
    .parquet("output/data");

// 쿼리 시 파티션 프루닝
Dataset<Row> jan2024 = spark.read()
    .parquet("output/data")
    .filter(col("year").equalTo(2024).and(col("month").equalTo(1)));
// year=2024/month=1 파티션만 읽음
```

**3. 버케팅**

```java
// 조인 키로 버케팅
df.write()
    .bucketBy(100, "user_id")
    .sortBy("timestamp")
    .saveAsTable("events");

// 같은 버케팅의 테이블 조인 시 셔플 없음
```

**4. 작은 파일 문제 해결**

```java
// 쓰기 전 파티션 조정
df.coalesce(10)  // 10개 파일로 병합
  .write()
  .parquet("output");

// 또는 최대 레코드 수 지정
df.write()
    .option("maxRecordsPerFile", 1000000)
    .parquet("output");
```

#### 리소스 설정

**메모리 구조**

```
Executor 메모리 (spark.executor.memory)
├── 실행 메모리 (Execution): 셔플, 조인, 정렬
├── 저장 메모리 (Storage): 캐시, 브로드캐스트
└── 사용자 메모리 (User): UDF, 사용자 객체

기본 비율 (spark.memory.fraction = 0.6):
├── 실행 + 저장: 60%
└── 사용자: 40%
```

**권장 설정**

```bash
# Executor 설정
spark.executor.instances=10       # Executor 수
spark.executor.memory=8g          # Executor 메모리
spark.executor.cores=4            # Executor당 코어

# Driver 설정
spark.driver.memory=4g            # Driver 메모리
spark.driver.cores=2              # Driver 코어

# 병렬성
spark.default.parallelism=200     # RDD 기본 파티션 수
spark.sql.shuffle.partitions=200  # SQL 셔플 파티션 수
```

**클러스터 사이징 가이드**

```
총 코어 수 = spark.executor.instances × spark.executor.cores
권장 파티션 수 = 총 코어 수 × 2~4

예시: 50 Executor × 4 코어 = 200 코어
→ 파티션 수: 400~800
```

**Executor 메모리 계산**

```
spark.executor.memory = 컨테이너 메모리 × 0.9 - 오버헤드

예시: 10GB 컨테이너
- 오버헤드(10%): 1GB
- spark.executor.memory: 약 8GB
```

#### 셔플 최적화

**셔플 관련 설정**

```java
// 셔플 파티션 수 (가장 중요)
spark.conf().set("spark.sql.shuffle.partitions", "400");

// 셔플 버퍼 크기
spark.conf().set("spark.shuffle.file.buffer", "64k");

// 셔플 압축
spark.conf().set("spark.shuffle.compress", "true");

// 셔플 스필 임계값
spark.conf().set("spark.shuffle.spill.compress", "true");
```

**AQE (Adaptive Query Execution)**

Spark 3.0+에서 자동 최적화:

```java
// AQE 활성화 (3.2+에서 기본 활성화)
spark.conf().set("spark.sql.adaptive.enabled", "true");

// 파티션 자동 병합
spark.conf().set("spark.sql.adaptive.coalescePartitions.enabled", "true");
spark.conf().set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "64MB");

// 브로드캐스트 조인 자동 전환
spark.conf().set("spark.sql.adaptive.localShuffleReader.enabled", "true");

// 스큐 조인 처리
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
```

#### 직렬화 최적화

**Kryo 직렬화**

```java
SparkSession spark = SparkSession.builder()
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .config("spark.kryo.registrationRequired", "false")
    .getOrCreate();

// 클래스 등록 (성능 향상)
spark.conf().set("spark.kryo.classesToRegister",
    "com.example.MyClass1,com.example.MyClass2");
```

**직렬화 비교**

| 직렬화 | 속도 | 크기 | 설정 |
|--------|------|------|------|
| Java (기본) | 느림 | 큼 | 없음 |
| Kryo | 빠름 | 작음 | 권장 |

#### GC 최적화

**GC 설정**

```bash
# G1GC 사용 (큰 힙에 적합)
spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=200

# GC 로깅
spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps
```

**Off-Heap 메모리**

```java
// Off-Heap 활성화 (GC 영향 감소)
spark.conf().set("spark.memory.offHeap.enabled", "true");
spark.conf().set("spark.memory.offHeap.size", "2g");
```

#### SQL 최적화

**조인 전략 힌트**

```sql
-- 브로드캐스트 힌트
SELECT /*+ BROADCAST(small) */ *
FROM large JOIN small ON large.id = small.id;

-- 셔플 해시 조인
SELECT /*+ SHUFFLE_HASH(t1) */ *
FROM t1 JOIN t2 ON t1.id = t2.id;

-- 소트 머지 조인
SELECT /*+ SORT_MERGE(t1, t2) */ *
FROM t1 JOIN t2 ON t1.id = t2.id;
```

**쿼리 최적화**

```java
// 실행 계획 확인
df.explain("extended");

// 비용 기반 최적화
spark.conf().set("spark.sql.cbo.enabled", "true");
spark.conf().set("spark.sql.cbo.joinReorder.enabled", "true");

// 테이블 통계 수집 (Hive 테이블)
spark.sql("ANALYZE TABLE my_table COMPUTE STATISTICS");
spark.sql("ANALYZE TABLE my_table COMPUTE STATISTICS FOR COLUMNS col1, col2");
```

#### 스트리밍 튜닝

**처리량 최적화**

```java
// 트리거 간격
.trigger(Trigger.ProcessingTime("5 seconds"))

// Kafka 배치 크기
.option("maxOffsetsPerTrigger", "100000")

// 상태 저장소 설정
spark.conf().set("spark.sql.streaming.stateStore.providerClass",
    "org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider");
```

#### 성능 모니터링

**Spark UI 활용**

주요 확인 사항:
1. **Jobs**: 전체 Job 수행 시간
2. **Stages**: Stage별 셔플 크기, 스필
3. **Tasks**: Task 분포, 스큐 확인
4. **Storage**: 캐시 사용량
5. **SQL**: 쿼리 실행 계획

**메트릭 확인**

```java
// 실행 중 메트릭
spark.sparkContext().statusTracker().getJobInfo(jobId);
spark.sparkContext().statusTracker().getStageInfo(stageId);

// 실행 후 통계
Dataset<Row> df = spark.read().parquet("data");
df.cache();
df.count();  // 캐시 로드

// 논리/물리 계획
df.explain(true);
```

#### 체크리스트

**코드 체크리스트**

- [ ] 불필요한 셔플 제거
- [ ] 조기 필터링 적용
- [ ] 필요한 컬럼만 선택
- [ ] 작은 테이블 브로드캐스트
- [ ] 반복 사용 데이터 캐싱
- [ ] UDF 대신 내장 함수 사용

**데이터 체크리스트**

- [ ] Parquet 포맷 사용
- [ ] 적절한 파티셔닝
- [ ] 작은 파일 병합
- [ ] 스키마 명시적 지정

**설정 체크리스트**

- [ ] AQE 활성화
- [ ] 적절한 셔플 파티션 수
- [ ] Kryo 직렬화
- [ ] 브로드캐스트 임계값 조정
- [ ] 메모리/코어 적절 할당

#### 실무 인사이트

**실제 튜닝 시나리오**

1. **ETL 파이프라인 최적화 사례**
   ```
   문제: 일일 100GB 데이터 처리, 4시간 소요
   원인: 200개 기본 셔플 파티션으로 불균형 발생
   해결: spark.sql.shuffle.partitions=2000 + AQE 활성화
   결과: 45분으로 단축 (80% 개선)
   ```

2. **조인 성능 개선 패턴**
   ```java
   // Before: 3시간 소요
   large.join(small, "key").join(medium, "key2")

   // After: 20분 소요
   // 1. 작은 테이블 브로드캐스트
   // 2. 조인 순서 최적화 (작은 결과 먼저)
   large
     .join(broadcast(small), "key")   // 셔플 없음
     .filter(col("status").equalTo("active"))  // 조기 필터
     .join(medium, "key2")
   ```

3. **데이터 스큐 해결 실전**
   ```java
   // 특정 키에 데이터 집중 (예: null, default 값)
   // Salting 기법으로 분산
   int saltBuckets = 10;
   Dataset<Row> salted = skewedDf
       .withColumn("salt", expr("floor(rand() * " + saltBuckets + ")"))
       .withColumn("salted_key", concat(col("key"), lit("_"), col("salt")));
   ```

4. **GC 튜닝 경험칙**
   | Executor 메모리 | 권장 GC | 이유 |
   |-----------------|---------|------|
   | ~8GB | 기본 (Parallel) | 충분히 작음 |
   | 8~32GB | G1GC | 대용량에 효과적 |
   | 32GB+ | ZGC/Shenandoah | 초저지연 필요 시 |

5. **Spring Boot 환경에서의 튜닝 팁**
   - REST API 응답 시간 제약이 있다면 비동기 처리 (CompletableFuture)
   - 대용량 결과는 파일 저장 후 URL 반환 패턴 권장
   - SparkSession 생성은 애플리케이션 시작 시 1회만

**성능 개선 우선순위**

```
1순위: 데이터 스큐 해결 (가장 큰 효과)
2순위: 불필요한 셔플 제거 (groupBy, join 최적화)
3순위: 브로드캐스트 조인 활용
4순위: 캐싱/파티셔닝 전략
5순위: Executor 리소스 튜닝
```

#### 다음 단계

- [배포와 클러스터 관리](../deployment/) - 프로덕션 환경 구성
- [FAQ](../../appendix/faq/) - 자주 발생하는 성능 문제 해결

#### 관련 문서

- [아키텍처](../architecture/) - Driver/Executor 메모리 구조
- [파티셔닝과 셔플](../partitioning/) - 셔플 최적화 상세
- [캐싱과 영속성](../caching/) - 캐시 메모리 관리
- [Spark SQL](../spark-sql/) - SQL 쿼리 최적화
- [기본 예제](../../examples/basic/) - 최적화 적용 예제
- [용어 사전](../../appendix/glossary/) - AQE, Tungsten 용어 정의
