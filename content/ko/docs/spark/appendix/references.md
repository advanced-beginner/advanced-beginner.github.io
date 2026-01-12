---
title: 참고 자료
weight: 3
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Apache Spark 학습을 위한 공식 문서와 추가 자료를 안내합니다.

{{% notice style="tip" title="TL;DR" %}}
- **공식 문서**: [spark.apache.org/docs/latest](https://spark.apache.org/docs/latest/) - 가장 정확한 최신 정보
- **Java API**: [Javadoc](https://spark.apache.org/docs/latest/api/java/) - Dataset, SparkSession, functions 클래스 참조
- **학습 추천**: Databricks Academy(공식), Baeldung(Java 개발자용)
- **입문서 추천**: "Learning Spark, 2nd Edition", "Spark: The Definitive Guide"
- **커뮤니티**: Stack Overflow `apache-spark` 태그, GitHub Issues
{{% /notice %}}

## 공식 문서

**Apache Spark 공식 사이트**

- **[Spark 공식 사이트](https://spark.apache.org/)** — 다운로드, 뉴스, 릴리즈 정보
- **[Spark 3.5 문서](https://spark.apache.org/docs/3.5.7/)** — 현재 안정 버전 문서
- **[Spark 최신 문서](https://spark.apache.org/docs/latest/)** — 최신 버전 문서

**프로그래밍 가이드**

- **[RDD Programming Guide](https://spark.apache.org/docs/latest/rdd-programming-guide.html)** — RDD API 상세 설명
- **[Spark SQL, DataFrames and Datasets Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)** — SQL과 DataFrame API
- **[Structured Streaming Programming Guide](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html)** — 실시간 스트림 처리
- **[MLlib Guide](https://spark.apache.org/docs/latest/ml-guide.html)** — 머신러닝 라이브러리
- **[GraphX Programming Guide](https://spark.apache.org/docs/latest/graphx-programming-guide.html)** — 그래프 처리

**운영 가이드**

- **[Cluster Overview](https://spark.apache.org/docs/latest/cluster-overview.html)** — 클러스터 아키텍처
- **[Tuning Guide](https://spark.apache.org/docs/latest/tuning.html)** — 성능 튜닝
- **[Monitoring Guide](https://spark.apache.org/docs/latest/monitoring.html)** — 모니터링
- **[Configuration](https://spark.apache.org/docs/latest/configuration.html)** — 설정 옵션
- **[Security](https://spark.apache.org/docs/latest/security.html)** — 보안 설정

**클러스터 매니저별 가이드**

- **[Standalone Mode](https://spark.apache.org/docs/latest/spark-standalone.html)**
- **[YARN](https://spark.apache.org/docs/latest/running-on-yarn.html)**
- **[Kubernetes](https://spark.apache.org/docs/latest/running-on-kubernetes.html)**

{{% notice style="info" title="공식 문서 핵심 포인트" %}}
- **시작점**: Spark 3.5 문서 또는 latest 문서
- **프로그래밍**: SQL/DataFrame 가이드가 가장 많이 사용됨
- **운영**: Tuning Guide와 Monitoring Guide 필수 숙지
- **클러스터**: 환경에 따라 YARN, Kubernetes, Standalone 중 선택
{{% /notice %}}

## API 문서

**Java API**

- **[Spark Java API (Javadoc)](https://spark.apache.org/docs/latest/api/java/index.html)** — Java API 레퍼런스
- **[Dataset<Row>](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/Dataset.html)** — DataFrame 클래스
- **[SparkSession](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SparkSession.html)** — 진입점 클래스
- **[functions](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/functions.html)** — 내장 함수

**Scala API**

- **[Spark Scala API (Scaladoc)](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/index.html)**

{{% notice style="info" title="API 문서 핵심 포인트" %}}
- **Java 개발자 필수**: Dataset, SparkSession, functions 클래스
- **functions 클래스**: 모든 내장 함수 (col, lit, when, sum, avg 등) 포함
- Scala API도 참조하면 더 많은 예제와 설명 확인 가능
{{% /notice %}}

## 추가 학습 자료

**온라인 강좌**

- **[Databricks Academy](https://www.databricks.com/learn)** — Spark 공동 창시자 회사의 공식 교육
- **[Coursera: Big Data Analysis with Scala and Spark](https://www.coursera.org/learn/scala-spark-big-data)** — EPFL의 Scala/Spark 강좌
- **[edX: Big Data Analytics Using Spark](https://www.edx.org/learn/big-data/university-of-california-san-diego-big-data-analytics-using-spark)** — UC San Diego 강좌

**블로그 및 문서**

- **[Databricks Blog](https://www.databricks.com/blog)** — Spark 최신 기술과 사례
- **[Spark By Examples](https://sparkbyexamples.com/)** — Java, Scala, Python 예제
- **[Baeldung Spark Tutorials](https://www.baeldung.com/apache-spark)** — Java 개발자를 위한 Spark 튜토리얼

**커뮤니티**

- **[Stack Overflow - apache-spark](https://stackoverflow.com/questions/tagged/apache-spark)** — Q&A
- **[Spark Mailing Lists](https://spark.apache.org/community.html)** — 개발자 메일링 리스트
- **[GitHub - apache/spark](https://github.com/apache/spark)** — 소스 코드와 이슈 트래커

{{% notice style="info" title="추가 학습 자료 핵심 포인트" %}}
- **공식 교육**: Databricks Academy - 체계적인 커리큘럼 제공
- **Java 개발자**: Baeldung Spark Tutorials 추천
- **실무 예제**: Spark By Examples - 다양한 언어별 코드 예제
- **문제 해결**: Stack Overflow `apache-spark` 태그 검색
{{% /notice %}}

## 관련 기술 문서

**데이터 소스**

- **[Kafka](https://kafka.apache.org/documentation/)** — 스트리밍 데이터 소스
- **[HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html)** — 분산 파일 시스템
- **[Parquet](https://parquet.apache.org/docs/)** — 컬럼 기반 포맷
- **[Delta Lake](https://docs.delta.io/)** — ACID 트랜잭션 지원 저장소

**클러스터 환경**

- **[Hadoop YARN](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html)** — 리소스 관리
- **[Kubernetes](https://kubernetes.io/docs/home/)** — 컨테이너 오케스트레이션

**클라우드 서비스**

- **[AWS EMR](https://docs.aws.amazon.com/emr/)** — AWS 관리형 Spark
- **[Google Dataproc](https://cloud.google.com/dataproc/docs)** — GCP 관리형 Spark
- **[Azure HDInsight](https://learn.microsoft.com/en-us/azure/hdinsight/)** — Azure 관리형 Hadoop/Spark
- **[Databricks](https://docs.databricks.com/)** — Unified Data Analytics Platform

{{% notice style="info" title="관련 기술 문서 핵심 포인트" %}}
- **데이터 소스**: Kafka(스트리밍), HDFS(분산 저장), Parquet(컬럼 포맷), Delta Lake(ACID)
- **클러스터**: YARN(Hadoop 환경), Kubernetes(컨테이너)
- **클라우드**: AWS EMR, GCP Dataproc, Azure HDInsight, Databricks
{{% /notice %}}

## 버전별 릴리즈 노트

- **[Spark 3.5 Release](https://spark.apache.org/releases/spark-release-3-5-0.html)**
- **[Spark 3.4 Release](https://spark.apache.org/releases/spark-release-3-4-0.html)**
- **[Spark 3.3 Release](https://spark.apache.org/releases/spark-release-3-3-0.html)**

## 성능 벤치마크

- **[TPC-DS Benchmark](http://www.tpc.org/tpcds/)** — 결정 지원 시스템 벤치마크
- **[Spark SQL Performance Tests](https://github.com/databricks/spark-sql-perf)** — Databricks 성능 테스트 도구

## 참고 도서

**입문서**

- **Learning Spark, 2nd Edition** (O'Reilly) — Jules S. Damji 외
- **Spark: The Definitive Guide** (O'Reilly) — Bill Chambers, Matei Zaharia

**심화**

- **High Performance Spark** (O'Reilly) — Holden Karau, Rachel Warren
- **Spark in Action, 2nd Edition** (Manning) — Jean-Georges Perrin

{{% notice style="info" title="참고 도서 핵심 포인트" %}}
- **입문 추천**: "Learning Spark, 2nd Edition" - 최신 Spark 3.x 기준 종합 입문서
- **심화 추천**: "High Performance Spark" - 성능 최적화와 내부 구조 이해
- **실무 지향**: "Spark in Action, 2nd Edition" - 실무 예제 중심
{{% /notice %}}
