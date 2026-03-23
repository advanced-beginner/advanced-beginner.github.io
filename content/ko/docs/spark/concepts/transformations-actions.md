---
title: Transformation과 Action
description: "Transformation과 Action의 작동 원리와 실행 계획을 설명합니다."
weight: 5
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- Transformation은 지연 평가(Lazy)되어 DAG에 추가만 됨
- Action 호출 시 전체 DAG가 실행되어 Job 생성
- Narrow(셔플 없음) vs Wide(셔플 발생) Transformation 구분이 성능 핵심
- 같은 DataFrame을 여러 번 사용하면 매번 재계산 → 캐싱 필요
{{< /callout >}}

**대상 독자**: Spark 연산의 실행 시점을 이해하고자 하는 개발자

**선수 지식**:
- [아키텍처](architecture/) 문서의 Job, Stage, Task 개념
- Java Stream API의 지연 평가 개념

---

Spark의 모든 연산은 **Transformation**과 **Action** 두 가지로 분류됩니다. 이 구분을 이해하는 것이 Spark 프로그래밍의 핵심입니다.

## 비유로 이해하는 Transformation과 Action

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **Transformation** | 요리 레시피 작성 | "양파 썰기 → 볶기 → 간 맞추기" 적어두기만 함 |
| **Action** | "요리 시작!" | 레시피대로 실제 조리 시작 |
| **Lazy Evaluation** | 쇼핑 목록 | 목록만 적고, 마트 가서 한 번에 구매 |
| **DAG** | 공정 흐름도 | 어떤 순서로 작업할지 미리 설계 |
| **Narrow Transformation** | 각자 자기 책상 정리 | 옆 사람 안 건드리고 혼자 완료 |
| **Wide Transformation** | 부서 전체 회의 | 모든 팀원이 한자리에 모여야 진행 (셔플) |
| **캐싱** | 중간 결과물 냉장 보관 | 같은 재료 다시 손질 안 해도 됨 |

## 왜 지연 평가(Lazy Evaluation)인가? (설계 철학)

Spark가 즉시 실행하지 않고 **미루는 이유**가 있습니다.

**즉시 실행의 문제점**

```
Python/Java 일반 코드:
filtered = df.filter(...)      # 즉시 실행
selected = filtered.select(...) # 즉시 실행
result = selected.groupBy(...)  # 즉시 실행

문제:
├── 각 단계마다 전체 데이터 순회
├── 중간 결과물 저장에 메모리 낭비
├── 최적화 기회 없음 (이미 실행됨)
└── 분산 환경에서 불필요한 네트워크 통신
```

**지연 평가의 해결책**

| 즉시 실행 | 지연 평가 |
|-----------|-----------|
| 각 연산마다 실행 | Action 호출 시 한 번에 실행 |
| 중간 결과 모두 저장 | 파이프라이닝으로 메모리 절약 |
| 최적화 불가 | 전체 DAG 분석 후 최적화 |
| 비효율적 실행 계획 | Catalyst가 최적 경로 선택 |

**Narrow vs Wide 구분의 이유**

```
핵심 질문: "이 연산에 다른 파티션 데이터가 필요한가?"

Narrow (필요 없음):
├── filter: 각 레코드 독립 판단
├── map: 각 레코드 독립 변환
├── 네트워크 통신 불필요
└── 같은 Stage에서 파이프라이닝

Wide (필요함):
├── groupBy: 같은 키끼리 모아야 함
├── join: 양쪽 테이블 매칭 필요
├── 셔플(네트워크 통신) 발생
└── Stage 경계 생성
```

{{< callout type="info" title="설계 원칙" >}}
**"셔플은 분산 시스템의 가장 비싼 연산"** — Spark는 Narrow/Wide를 명확히 구분하여 Stage를 나누고, 불필요한 셔플을 최소화하도록 설계되었습니다. Wide Transformation이 많을수록 성능이 저하됩니다.
{{< /callout >}}

**Action이 Job을 생성하는 이유**

```
// 같은 DataFrame에서 여러 Action 호출
df.count();    // Job 1: 전체 DAG 실행
df.show();     // Job 2: 전체 DAG 다시 실행!

왜 매번 다시 실행할까?
├── RDD/DataFrame은 불변(immutable)
├── 이전 결과를 기억하지 않음
├── 메모리 효율성 (기본적으로 중간 결과 저장 안 함)
└── 필요하면 명시적으로 cache() 호출
```

## 핵심 개념

**Transformation**

**Transformation**은 기존 RDD/DataFrame에서 새로운 RDD/DataFrame을 생성하는 연산입니다.

- **지연 평가(Lazy Evaluation)**: 호출 시점에 즉시 실행되지 않음
- **불변성**: 원본 데이터를 변경하지 않고 새 데이터 생성
- **DAG 구성**: 실행 계획(DAG)에 연산을 추가

```java
// 이 코드들은 실행되지 않고 DAG에 추가만 됨
Dataset<Row> df = spark.read().csv("data.csv");
Dataset<Row> filtered = df.filter(col("age").gt(30));      // Transformation
Dataset<Row> selected = filtered.select("name", "age");    // Transformation

// 아직 아무 것도 실행되지 않음!
```

**Action**

**Action**은 실제 계산을 트리거하고 결과를 반환하는 연산입니다.

- **즉시 실행**: DAG를 실행하여 결과 생성
- **결과 반환**: Driver에 값을 반환하거나 외부에 저장
- **Job 생성**: 각 Action은 하나의 Job을 생성

```java
// Action 호출 시 비로소 전체 DAG 실행
long count = selected.count();        // Action! → Job 실행
selected.show();                      // Action! → Job 실행
List<Row> rows = selected.collectAsList();  // Action! → Job 실행
```

## 왜 지연 평가인가?

**1. 최적화 기회**

지연 평가를 통해 Spark는 전체 DAG를 분석하고 최적화할 수 있습니다.

```java
// 비효율적으로 보이는 코드
Dataset<Row> result = df
    .select("name", "age", "salary", "department")  // 4개 컬럼 선택
    .filter(col("age").gt(30))                       // 필터링
    .select("name", "age");                          // 2개 컬럼만 사용

// Catalyst Optimizer가 최적화:
// → select("name", "age")를 먼저 실행하여 불필요한 컬럼 제거
// → 실제로는 "name", "age"만 읽고 필터링
```

**2. 파이프라이닝**

여러 Transformation을 하나의 Stage에서 파이프라이닝:

```java
// 논리적으로는 3번의 순회
Dataset<Row> result = df
    .filter(col("status").equalTo("ACTIVE"))
    .map(row -> transform(row))
    .filter(col("score").gt(80));

// 실제로는 1번의 순회로 처리 (파이프라이닝)
// filter1 → map → filter2 를 각 레코드에 연속 적용
```

**3. 불필요한 계산 회피**

```java
Dataset<Row> expensive = df
    .groupBy("category")
    .agg(complexAggregation());

// 만약 이후에 사용하지 않으면 계산 안 함
// take(1)만 호출하면 필요한 만큼만 계산
Row first = expensive.first();
```

## Transformation 종류

**Narrow Transformation**

각 입력 파티션이 최대 하나의 출력 파티션에 기여합니다. **셔플이 발생하지 않습니다.**

```java
// map - 각 요소 변환
Dataset<Row> doubled = df.withColumn("doubled", col("value").multiply(2));

// filter - 조건 필터링
Dataset<Row> adults = df.filter(col("age").geq(18));

// flatMap - 1:N 변환
Dataset<String> words = df.flatMap(
    row -> Arrays.asList(row.getString(0).split(" ")).iterator(),
    Encoders.STRING()
);

// select - 컬럼 선택
Dataset<Row> subset = df.select("name", "email");

// withColumn - 컬럼 추가/수정
Dataset<Row> enhanced = df.withColumn("year", year(col("date")));

// union - 두 DataFrame 합치기 (파티션 유지)
Dataset<Row> combined = df1.union(df2);

// sample - 샘플링
Dataset<Row> sampled = df.sample(0.1);  // 10% 샘플
```

**특징:**
- 같은 파티션 내에서 처리 완료
- 네트워크 I/O 없음
- 매우 효율적
- 연속 Narrow Transformation은 파이프라이닝됨

**Wide Transformation**

여러 입력 파티션의 데이터가 하나의 출력 파티션에 기여합니다. **셔플이 발생합니다.**

```java
// groupBy - 그룹화
Dataset<Row> grouped = df.groupBy("department").agg(sum("salary"));

// join - 조인
Dataset<Row> joined = employees.join(departments, "department_id");

// distinct - 중복 제거
Dataset<Row> unique = df.distinct();

// repartition - 파티션 재분배
Dataset<Row> repartitioned = df.repartition(100);
Dataset<Row> repartitionedByKey = df.repartition(col("key"));

// orderBy/sort - 전역 정렬
Dataset<Row> sorted = df.orderBy(col("salary").desc());

// reduceByKey (RDD) - 키별 집계
JavaPairRDD<String, Integer> reduced = pairRdd.reduceByKey(Integer::sum);
```

**특징:**
- 데이터 재분배(셔플) 필요
- 네트워크 I/O 발생
- Stage 경계가 됨
- 성능에 가장 큰 영향

## Action 종류

**값 반환 Action**

```java
// collect - 모든 데이터를 Driver로 (주의: 대용량 시 OOM)
List<Row> allRows = df.collectAsList();

// first/head - 첫 번째 요소
Row firstRow = df.first();
Row headRow = df.head();

// take - 처음 n개
List<Row> topN = df.takeAsList(10);

// count - 개수
long rowCount = df.count();

// reduce - 집계 (RDD)
int sum = numbersRdd.reduce(Integer::sum);
```

**출력 Action**

```java
// show - 콘솔 출력
df.show();
df.show(20);                    // 20행
df.show(20, false);             // 문자열 잘림 없이
df.show(20, 100, false);        // 최대 100자, 잘림 없이

// printSchema - 스키마 출력
df.printSchema();

// describe - 통계 출력
df.describe("age", "salary").show();
```

**저장 Action**

```java
// write - 파일 저장
df.write()
    .mode("overwrite")           // overwrite, append, ignore, error
    .partitionBy("year", "month")
    .parquet("output/data");

// saveAsTable - Hive 테이블 저장
df.write()
    .mode("overwrite")
    .saveAsTable("my_table");

// foreach - 각 파티션에서 함수 실행
df.foreach(row -> {
    // Executor에서 실행됨
    externalSystem.write(row);
});

// foreachPartition - 파티션 단위 처리
df.foreachPartition(partition -> {
    Connection conn = getConnection();
    while (partition.hasNext()) {
        Row row = partition.next();
        insertToDb(conn, row);
    }
    conn.close();
});
```

## 실행 흐름 이해

**DAG 구성과 실행**

```java
Dataset<Row> df = spark.read().csv("data.csv");

// 1단계: DAG 구성 (Transformation)
Dataset<Row> step1 = df.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> step2 = step1.select("id", "name", "score");
Dataset<Row> step3 = step2.filter(col("score").gt(80));

// 아직 실행 안 됨 - DAG만 구성됨

// 2단계: Action 호출 → Job 생성 → 실행
long count = step3.count();  // Job 1 실행

// 3단계: 또 다른 Action → 새 Job 실행
step3.show();               // Job 2 실행 (처음부터 다시 계산)
```

**Job, Stage, Task 관계**

```java
Dataset<Row> result = df
    .filter(col("active").equalTo(true))      // Stage 1: Narrow
    .withColumn("year", year(col("date")))    // Stage 1: Narrow
    .groupBy("year")                           // Stage 경계 (Wide)
    .agg(sum("amount").alias("total"))        // Stage 2
    .orderBy(col("total").desc());            // Stage 경계 (Wide) → Stage 3

result.show();  // 1 Job, 3 Stages
```

```
Job 1
├── Stage 1: filter → withColumn (Narrow, 파이프라이닝)
│   └── [Task 1] [Task 2] [Task 3] ...
│       ↓ 셔플 ↓
├── Stage 2: groupBy → agg
│   └── [Task 1] [Task 2] [Task 3] ...
│       ↓ 셔플 ↓
└── Stage 3: orderBy → show
    └── [Task 1] [Task 2] [Task 3] ...
```

## 캐싱과 재사용

같은 DataFrame을 여러 Action에서 사용하면 매번 재계산됩니다. 캐싱으로 이를 방지할 수 있습니다.

```java
// 캐싱 없이 - 비효율적
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
long count = processed.count();      // 전체 계산
processed.show();                    // 전체 재계산!
processed.write().parquet("...");    // 전체 재계산!!

// 캐싱 사용 - 효율적
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
processed.cache();                   // 캐시 등록 (아직 계산 안 함)

long count = processed.count();      // 계산 + 캐싱
processed.show();                    // 캐시에서 읽음
processed.write().parquet("...");    // 캐시에서 읽음

processed.unpersist();               // 캐시 해제
```

**언제 캐싱하나?**

1. **같은 DataFrame을 여러 Action에서 사용할 때**
2. **반복 알고리즘** (머신러닝 등)
3. **대화형 분석** (동일 데이터 반복 탐색)

**캐싱하면 안 되는 경우**

1. **한 번만 사용하는 DataFrame**
2. **메모리가 부족한 경우**
3. **원본 데이터가 자주 변경되는 경우**

## 주의사항

**1. Action 없이 Transformation만으로는 실행 안 됨**

```java
// 아무 것도 실행되지 않음!
Dataset<Row> result = df
    .filter(...)
    .groupBy(...)
    .agg(...);

// Action 필요
result.count();  // 이제 실행됨
```

**2. foreach에서 Driver 변수 수정 불가**

```java
// 잘못된 코드 - 동작 안 함!
int[] counter = {0};
df.foreach(row -> counter[0]++);  // Executor에서 실행됨
System.out.println(counter[0]);   // 항상 0 출력

// 올바른 방법
long count = df.count();          // Action 사용
```

**3. collect는 대용량 데이터에서 위험**

```java
// 위험! - 10억 행을 Driver 메모리로
List<Row> allRows = hugeDataFrame.collectAsList();  // OOM!

// 안전한 대안
hugeDataFrame.take(1000);                     // 일부만
hugeDataFrame.show(100);                      // 출력만
hugeDataFrame.write().parquet("output");      // 분산 저장
```

**4. 실행 계획 확인하기**

```java
// 실행 전에 계획 확인
df.filter(...).groupBy(...).agg(...).explain();

// 출력:
// == Physical Plan ==
// *(2) HashAggregate(...)
// +- Exchange hashpartitioning(...)  ← 셔플!
//    +- *(1) HashAggregate(...)
//       +- *(1) Filter(...)
//          +- FileScan parquet(...)
```

## 실전 팁

**1. Wide Transformation 최소화**

```java
// 비효율: 두 번의 Wide Transformation
df.groupBy("dept").agg(sum("salary"))
  .orderBy("dept");

// 효율: 필요한 경우만 정렬
df.groupBy("dept").agg(sum("salary"))
  .show();  // 정렬 불필요하면 orderBy 제거
```

**2. 필터링은 가능한 빨리**

```java
// 비효율: 조인 후 필터
employees.join(departments, "dept_id")
         .filter(col("status").equalTo("ACTIVE"));

// 효율: 필터 후 조인 (셔플할 데이터 감소)
employees.filter(col("status").equalTo("ACTIVE"))
         .join(departments, "dept_id");
```

**3. select로 필요한 컬럼만**

```java
// 비효율: 모든 컬럼 유지
df.join(other, "id")
  .groupBy("category")
  .agg(sum("value"));

// 효율: 필요한 컬럼만 선택
df.select("id", "category", "value")
  .join(other.select("id", "factor"), "id")
  .groupBy("category")
  .agg(sum("value"));
```

## 요약

| 구분 | Transformation | Action |
|------|---------------|--------|
| 실행 시점 | 지연 (Lazy) | 즉시 (Eager) |
| 반환값 | RDD/DataFrame | 값 또는 void |
| DAG | 추가 | 트리거 |
| 예시 | map, filter, groupBy | count, show, collect |

## 다음 단계

- [파티셔닝과 셔플](partitioning/) - Wide Transformation의 내부 동작
- [캐싱과 영속성](caching/) - 중간 결과 재사용

## 관련 문서

- [아키텍처](architecture/) - Job, Stage, Task의 실행 구조
- [RDD 기초](rdd/) - 저수준 Transformation API
- [DataFrame과 Dataset](dataframe-dataset/) - 고수준 데이터 처리 API
- [성능 튜닝](tuning/) - 실행 계획 최적화
- [용어 사전](../appendix/glossary/) - Transformation, Action 용어 정의
