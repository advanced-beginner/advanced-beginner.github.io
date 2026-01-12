---
title: 용어 사전
weight: 1
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Spark에서 사용되는 주요 용어와 개념을 정리합니다. 각 용어에서 관련 문서로 이동할 수 있습니다.

{{% notice style="tip" title="TL;DR" %}}
- **핵심 추상화**: SparkSession(진입점) → Application → Job → Stage → Task
- **데이터 구조**: RDD(저수준) < DataFrame(스키마, Row) < Dataset(타입 안전)
- **실행 모델**: Transformation(지연 평가) + Action(실제 실행) = DAG 기반 실행
- **성능 핵심**: Partition(분할 단위), Shuffle(데이터 재분배), Cache/Persist(재사용)
- **최적화 도구**: Catalyst Optimizer(쿼리 최적화), Tungsten(실행 엔진), AQE(런타임 최적화)
{{% /notice %}}

## 핵심 개념

**Action**

RDD/DataFrame의 실제 계산을 트리거하고 결과를 반환하는 연산. `count()`, `collect()`, `show()`, `write()` 등이 있다.
→ [Transformation과 Action](../concepts/transformations-actions/)

**Application**

사용자가 제출한 Spark 프로그램. [Driver](#driver)와 [Executor](#executor)로 구성된다.
→ [아키텍처](../concepts/architecture/)

**Broadcast Variable**

모든 노드에 읽기 전용으로 배포되는 공유 변수. 작은 데이터셋을 효율적으로 공유할 때 사용한다.
→ [성능 튜닝](../concepts/tuning/)

**Catalyst Optimizer**

Spark SQL의 쿼리 최적화 엔진. 논리 계획을 최적화된 물리 계획으로 변환한다.
→ [Spark SQL](../concepts/spark-sql/)

**Checkpoint**

RDD/DataFrame을 안정적인 저장소에 저장하여 [Lineage](#lineage)를 끊고 장애 복구를 빠르게 하는 메커니즘.
→ [캐싱과 영속성](../concepts/caching/)

**Cluster Manager**

클러스터의 리소스를 관리하는 외부 서비스. Standalone, YARN, Kubernetes, Mesos 등이 있다.
→ [아키텍처](../concepts/architecture/), [배포와 클러스터 관리](../concepts/deployment/)

**Coalesce**

파티션 수를 줄이는 연산. [Shuffle](#shuffle) 없이 파티션을 병합한다.
→ [파티셔닝과 셔플](../concepts/partitioning/)

**DAG (Directed Acyclic Graph)**

[Transformation](#transformation)의 의존 관계를 나타내는 방향성 비순환 그래프. Spark가 실행 계획을 최적화하는 데 사용한다.
→ [아키텍처](../concepts/architecture/)

**DataFrame**

이름 있는 컬럼으로 구성된 분산 데이터 컬렉션. Java에서는 `Dataset<Row>`로 표현된다.
→ [DataFrame과 Dataset](../concepts/dataframe-dataset/)

**Dataset**

특정 타입을 가진 분산 데이터 컬렉션. 컴파일 타임 타입 안전성을 제공한다.
→ [DataFrame과 Dataset](../concepts/dataframe-dataset/)

**Driver**

Spark 애플리케이션의 main() 함수를 실행하고 [SparkSession](#sparksession)을 생성하는 프로세스.
→ [아키텍처](../concepts/architecture/)

**Executor**

Worker 노드에서 실행되는 JVM 프로세스. [Task](#task)를 실행하고 데이터를 저장한다.
→ [아키텍처](../concepts/architecture/)

**Job**

하나의 [Action](#action)에 대응하는 병렬 계산 단위. 여러 [Stage](#stage)로 구성된다.
→ [아키텍처](../concepts/architecture/)

**Lazy Evaluation (지연 평가)**

[Transformation](#transformation) 호출 시 즉시 실행하지 않고, [Action](#action)이 호출될 때까지 실행을 지연하는 방식.
→ [Transformation과 Action](../concepts/transformations-actions/)

**Lineage**

[RDD](#rdd-resilient-distributed-dataset)가 어떤 [Transformation](#transformation)을 통해 생성되었는지에 대한 정보. 장애 복구에 사용된다.
→ [RDD 기초](../concepts/rdd/)

**Narrow Transformation**

각 입력 파티션이 최대 하나의 출력 파티션에만 기여하는 [Transformation](#transformation). [Shuffle](#shuffle)이 발생하지 않는다.
→ [Transformation과 Action](../concepts/transformations-actions/)

**Partition**

RDD/DataFrame 데이터의 논리적 분할 단위. 각 파티션은 클러스터의 한 노드에서 처리된다.
→ [파티셔닝과 셔플](../concepts/partitioning/)

**Persist**

RDD/DataFrame을 지정된 [Storage Level](#storage-level)로 메모리나 디스크에 저장하는 것.
→ [캐싱과 영속성](../concepts/caching/)

**RDD (Resilient Distributed Dataset)**

Spark의 기본 데이터 추상화. 불변, 분산, 장애 복구 가능한 데이터 컬렉션.
→ [RDD 기초](../concepts/rdd/)

**Repartition**

파티션 수를 변경하는 연산. [Shuffle](#shuffle)이 발생한다.
→ [파티셔닝과 셔플](../concepts/partitioning/)

**Serialization (직렬화)**

객체를 바이트 스트림으로 변환하는 과정. 네트워크 전송이나 디스크 저장에 필요하다.
→ [성능 튜닝](../concepts/tuning/)

**Shuffle**

파티션 간 데이터 재분배. [Wide Transformation](#wide-transformation)에서 발생하며 성능에 큰 영향을 미친다.
→ [파티셔닝과 셔플](../concepts/partitioning/)

**SparkContext**

Spark 클러스터에 대한 연결을 나타내는 객체. Spark 2.0 이후 [SparkSession](#sparksession)으로 통합되었다.
→ [아키텍처](../concepts/architecture/)

**SparkSession**

Spark 애플리케이션의 통합 진입점. [SparkContext](#sparkcontext), SQLContext, HiveContext를 포함한다.
→ [Quick Start](../quick-start/), [아키텍처](../concepts/architecture/)

**Stage**

[Shuffle](#shuffle) 경계로 나뉜 [Task](#task)의 집합. 하나의 [Job](#job)은 여러 Stage로 구성된다.
→ [아키텍처](../concepts/architecture/)

**Storage Level**

데이터 캐싱 시 저장 방식을 지정. MEMORY_ONLY, MEMORY_AND_DISK, DISK_ONLY 등이 있다.
→ [캐싱과 영속성](../concepts/caching/)

**Task**

단일 [Partition](#partition)에서 실행되는 최소 작업 단위. [Executor](#executor)에서 실행된다.
→ [아키텍처](../concepts/architecture/)

**Transformation**

기존 RDD/DataFrame에서 새로운 RDD/DataFrame을 생성하는 연산. [지연 평가](#lazy-evaluation-지연-평가)된다.
→ [Transformation과 Action](../concepts/transformations-actions/)

**Tungsten**

Spark의 실행 엔진 최적화 프로젝트. 메모리 관리, 코드 생성 등을 개선한다.
→ [성능 튜닝](../concepts/tuning/)

**Wide Transformation**

여러 입력 파티션이 하나의 출력 파티션에 기여하는 [Transformation](#transformation). [Shuffle](#shuffle)이 발생한다.
→ [Transformation과 Action](../concepts/transformations-actions/)

**Worker Node**

[Executor](#executor)를 실행하는 클러스터의 노드.
→ [아키텍처](../concepts/architecture/), [배포와 클러스터 관리](../concepts/deployment/)

{{% notice style="info" title="핵심 개념 요약" %}}
| 계층 | 용어 | 설명 |
|------|------|------|
| 진입점 | SparkSession | 모든 Spark 작업의 시작점 |
| 실행 단위 | Application → Job → Stage → Task | 큰 단위에서 작은 단위로 분해 |
| 데이터 추상화 | RDD → DataFrame → Dataset | 저수준에서 고수준으로, 타입 안전성 증가 |
| 실행 방식 | Transformation (지연) + Action (실행) | DAG로 최적화 후 실행 |
| 성능 핵심 | Narrow(셔플X) vs Wide(셔플O) Transformation | Wide일수록 비용 증가 |
{{% /notice %}}

## 스트리밍 용어

**Micro-Batch**

스트림 데이터를 작은 배치로 처리하는 Structured Streaming의 기본 모드.
→ [Structured Streaming](../concepts/structured-streaming/)

**Trigger**

스트림 처리가 언제 실행될지 결정하는 설정.
→ [Structured Streaming](../concepts/structured-streaming/)

**Watermark**

늦게 도착하는 데이터를 처리하기 위한 지연 허용 시간 설정.
→ [Structured Streaming](../concepts/structured-streaming/)

**Window**

시간 기반 그룹화를 위한 연산. Tumbling, Sliding, Session Window가 있다.
→ [Structured Streaming](../concepts/structured-streaming/)

{{% notice style="info" title="스트리밍 용어 요약" %}}
- **Micro-Batch**: 스트림을 작은 배치로 나눠 처리 (기본 모드)
- **Trigger**: 처리 주기 설정 (예: 10초마다, 한 번만)
- **Watermark**: 늦은 데이터 허용 시간 (예: 10분까지 기다림)
- **Window**: 시간 기반 그룹화 (Tumbling: 고정, Sliding: 겹침, Session: 활동 기반)
{{% /notice %}}

## 머신러닝 용어

**Estimator**

fit() 메서드로 학습하여 [Transformer](#transformer)를 생성하는 알고리즘.
→ [MLlib](../concepts/mllib/)

**Pipeline**

여러 [Estimator](#estimator)와 [Transformer](#transformer)를 연결한 워크플로우.
→ [MLlib](../concepts/mllib/)

**Transformer**

transform() 메서드로 데이터를 변환하는 컴포넌트.
→ [MLlib](../concepts/mllib/)

{{% notice style="info" title="머신러닝 용어 요약" %}}
- **Estimator**: 데이터를 학습하여 모델(Transformer) 생성 → `fit()`
- **Transformer**: 데이터를 변환 → `transform()`
- **Pipeline**: 여러 단계를 연결한 ML 워크플로우
- 흐름: `Estimator.fit(data) → Transformer.transform(data)`
{{% /notice %}}

## 설정 관련

**AQE (Adaptive Query Execution)**

런타임에 쿼리 계획을 동적으로 최적화하는 기능. Spark 3.0+에서 사용 가능.
→ [성능 튜닝](../concepts/tuning/)

**Broadcast Join**

작은 테이블을 모든 노드에 배포하여 [Shuffle](#shuffle) 없이 조인하는 방식.
→ [성능 튜닝](../concepts/tuning/)

**CBO (Cost-Based Optimization)**

테이블 통계를 기반으로 최적의 실행 계획을 선택하는 최적화 기법.
→ [성능 튜닝](../concepts/tuning/)

**Dynamic Allocation**

워크로드에 따라 [Executor](#executor) 수를 자동으로 조절하는 기능.
→ [성능 튜닝](../concepts/tuning/), [배포와 클러스터 관리](../concepts/deployment/)

{{% notice style="info" title="설정 관련 용어 요약" %}}
- **AQE**: 런타임 쿼리 최적화 (Spark 3.0+, 파티션 자동 조정, 스큐 조인 해결)
- **Broadcast Join**: 작은 테이블을 모든 노드에 복제하여 셔플 없이 조인
- **CBO**: 테이블 통계 기반 최적 실행 계획 선택
- **Dynamic Allocation**: 워크로드에 따라 Executor 수 자동 조절
{{% /notice %}}
