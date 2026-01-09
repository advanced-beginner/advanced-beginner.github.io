---
title: 개념 이해
weight: 2
lastmod: "2026-01-09"
---

Spark의 핵심 구성요소와 동작 원리를 이해합니다. 이 섹션에서는 Spark가 내부적으로 어떻게 동작하는지, 그리고 효율적인 분산 처리를 위해 알아야 할 개념들을 다룹니다.

#### 학습 순서

아래 순서대로 학습하면 Spark의 기초부터 운영까지 체계적으로 이해할 수 있습니다.

**기초 개념**

먼저 Spark의 핵심 구조와 API를 이해합니다:

1. [아키텍처](architecture/) - Driver, Executor, Cluster Manager의 역할과 상호작용
2. [RDD 기초](rdd/) - Spark의 기본 추상화, 분산 컬렉션의 개념
3. [DataFrame과 Dataset](dataframe-dataset/) - 현대적인 고수준 API
4. [Spark SQL](spark-sql/) - SQL로 분산 데이터 처리
5. [Transformation과 Action](transformations-actions/) - 지연 평가와 실행의 핵심

**심화 개념**

기초를 이해했다면 성능 최적화와 고급 기능을 학습합니다:

6. [파티셔닝과 셔플](partitioning/) - 분산 처리의 핵심, 데이터 분배 전략
7. [캐싱과 영속성](caching/) - 인메모리 처리 최적화
8. [Structured Streaming](structured-streaming/) - 실시간 스트림 데이터 처리
9. [MLlib](mllib/) - 분산 머신러닝

**운영 개념**

프로덕션 환경에서 Spark를 운영하기 위한 지식입니다:

10. [성능 튜닝](tuning/) - 메모리, 파티션, 셔플 최적화
11. [배포와 클러스터 관리](deployment/) - Standalone, YARN, Kubernetes 환경
12. [Spark Connect](spark-connect/) - 씬 클라이언트 아키텍처 (Spark 3.4+)

#### 핵심 개념 요약

Spark를 이해하는 데 필수적인 개념들을 간략히 소개합니다. 각 개념의 상세 내용은 개별 문서에서 다룹니다.

**실행 모델**

Spark 애플리케이션이 어떻게 실행되는지 이해하기 위한 핵심 구성요소입니다:

| 개념 | 설명 |
|------|------|
| Driver | 애플리케이션의 main() 실행, 작업 조율 |
| Executor | 실제 데이터 처리를 수행하는 워커 프로세스 |
| Cluster Manager | 리소스 할당 (Standalone, YARN, K8s) |
| Job | 하나의 Action에 대응하는 작업 단위 |
| Stage | 셔플 경계로 나뉜 Task 집합 |
| Task | 단일 파티션에서 실행되는 최소 작업 단위 |

Driver는 전체 작업을 조율하고, 실제 데이터 처리는 여러 Executor에서 병렬로 수행됩니다.

**데이터 추상화**

Spark가 제공하는 세 가지 데이터 API의 특성 비교입니다:

| API | 타입 안전성 | 최적화 | 사용 시점 |
|-----|-----------|--------|----------|
| RDD | 있음 (제네릭) | 제한적 | 저수준 제어 필요 시 |
| DataFrame | 없음 (Row) | Catalyst 최적화 | SQL 스타일 처리 |
| Dataset | 있음 (case class) | Catalyst 최적화 | 타입 안전 + 최적화 |

대부분의 경우 DataFrame을 사용하고, 컴파일 타임 타입 체크가 필요할 때 Dataset을 선택합니다.

**연산 유형**

Spark 연산은 크게 Transformation과 Action으로 구분됩니다:

| 유형 | 특성 | 예시 |
|------|------|------|
| Transformation | 지연 평가, 새 RDD/DataFrame 반환 | map, filter, groupBy |
| Action | 즉시 실행, 값 반환 | collect, count, show |

Transformation은 호출 즉시 실행되지 않고, Action이 호출될 때 최적화된 실행 계획으로 처리됩니다.

**Narrow vs Wide Transformation**

Transformation은 셔플 발생 여부에 따라 두 가지로 구분됩니다:

| 유형 | 셔플 | 예시 |
|------|------|------|
| Narrow | 없음 (1:1 파티션 매핑) | map, filter, union |
| Wide | 있음 (셔플 발생) | groupBy, join, reduceByKey |

Wide Transformation은 네트워크 I/O를 발생시키므로 성능에 큰 영향을 미칩니다. 이러한 개념들이 어떻게 연결되는지 각 문서에서 상세히 다룹니다.
