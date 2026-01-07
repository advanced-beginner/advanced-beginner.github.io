---
title: 용어 사전
weight: 1
---

# 용어 사전

Spark에서 사용되는 주요 용어와 개념을 정리합니다.

## 핵심 개념

### Action

RDD/DataFrame의 실제 계산을 트리거하고 결과를 반환하는 연산. `count()`, `collect()`, `show()`, `write()` 등이 있다.

### Application

사용자가 제출한 Spark 프로그램. Driver와 Executor로 구성된다.

### Broadcast Variable

모든 노드에 읽기 전용으로 배포되는 공유 변수. 작은 데이터셋을 효율적으로 공유할 때 사용한다.

### Catalyst Optimizer

Spark SQL의 쿼리 최적화 엔진. 논리 계획을 최적화된 물리 계획으로 변환한다.

### Checkpoint

RDD/DataFrame을 안정적인 저장소에 저장하여 lineage를 끊고 장애 복구를 빠르게 하는 메커니즘.

### Cluster Manager

클러스터의 리소스를 관리하는 외부 서비스. Standalone, YARN, Kubernetes, Mesos 등이 있다.

### Coalesce

파티션 수를 줄이는 연산. 셔플 없이 파티션을 병합한다.

### DAG (Directed Acyclic Graph)

Transformation의 의존 관계를 나타내는 방향성 비순환 그래프. Spark가 실행 계획을 최적화하는 데 사용한다.

### DataFrame

이름 있는 컬럼으로 구성된 분산 데이터 컬렉션. Java에서는 `Dataset<Row>`로 표현된다.

### Dataset

특정 타입을 가진 분산 데이터 컬렉션. 컴파일 타임 타입 안전성을 제공한다.

### Driver

Spark 애플리케이션의 main() 함수를 실행하고 SparkSession을 생성하는 프로세스.

### Executor

Worker 노드에서 실행되는 JVM 프로세스. Task를 실행하고 데이터를 저장한다.

### Job

하나의 Action에 대응하는 병렬 계산 단위. 여러 Stage로 구성된다.

### Lazy Evaluation (지연 평가)

Transformation 호출 시 즉시 실행하지 않고, Action이 호출될 때까지 실행을 지연하는 방식.

### Lineage

RDD가 어떤 Transformation을 통해 생성되었는지에 대한 정보. 장애 복구에 사용된다.

### Narrow Transformation

각 입력 파티션이 최대 하나의 출력 파티션에만 기여하는 Transformation. 셔플이 발생하지 않는다.

### Partition

RDD/DataFrame 데이터의 논리적 분할 단위. 각 파티션은 클러스터의 한 노드에서 처리된다.

### Persist

RDD/DataFrame을 지정된 Storage Level로 메모리나 디스크에 저장하는 것.

### RDD (Resilient Distributed Dataset)

Spark의 기본 데이터 추상화. 불변, 분산, 장애 복구 가능한 데이터 컬렉션.

### Repartition

파티션 수를 변경하는 연산. 셔플이 발생한다.

### Serialization (직렬화)

객체를 바이트 스트림으로 변환하는 과정. 네트워크 전송이나 디스크 저장에 필요하다.

### Shuffle

파티션 간 데이터 재분배. Wide Transformation에서 발생하며 성능에 큰 영향을 미친다.

### SparkContext

Spark 클러스터에 대한 연결을 나타내는 객체. Spark 2.0 이후 SparkSession으로 통합되었다.

### SparkSession

Spark 애플리케이션의 통합 진입점. SparkContext, SQLContext, HiveContext를 포함한다.

### Stage

셔플 경계로 나뉜 Task의 집합. 하나의 Job은 여러 Stage로 구성된다.

### Storage Level

데이터 캐싱 시 저장 방식을 지정. MEMORY_ONLY, MEMORY_AND_DISK, DISK_ONLY 등이 있다.

### Task

단일 파티션에서 실행되는 최소 작업 단위. Executor에서 실행된다.

### Transformation

기존 RDD/DataFrame에서 새로운 RDD/DataFrame을 생성하는 연산. 지연 평가된다.

### Tungsten

Spark의 실행 엔진 최적화 프로젝트. 메모리 관리, 코드 생성 등을 개선한다.

### Wide Transformation

여러 입력 파티션이 하나의 출력 파티션에 기여하는 Transformation. 셔플이 발생한다.

### Worker Node

Executor를 실행하는 클러스터의 노드.

## 스트리밍 용어

### Micro-Batch

스트림 데이터를 작은 배치로 처리하는 Structured Streaming의 기본 모드.

### Trigger

스트림 처리가 언제 실행될지 결정하는 설정.

### Watermark

늦게 도착하는 데이터를 처리하기 위한 지연 허용 시간 설정.

### Window

시간 기반 그룹화를 위한 연산. Tumbling, Sliding, Session Window가 있다.

## 머신러닝 용어

### Estimator

fit() 메서드로 학습하여 Transformer를 생성하는 알고리즘.

### Pipeline

여러 Estimator와 Transformer를 연결한 워크플로우.

### Transformer

transform() 메서드로 데이터를 변환하는 컴포넌트.

## 설정 관련

### AQE (Adaptive Query Execution)

런타임에 쿼리 계획을 동적으로 최적화하는 기능. Spark 3.0+에서 사용 가능.

### Broadcast Join

작은 테이블을 모든 노드에 배포하여 셔플 없이 조인하는 방식.

### CBO (Cost-Based Optimization)

테이블 통계를 기반으로 최적의 실행 계획을 선택하는 최적화 기법.

### Dynamic Allocation

워크로드에 따라 Executor 수를 자동으로 조절하는 기능.
