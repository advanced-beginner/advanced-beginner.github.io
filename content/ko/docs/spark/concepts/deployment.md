---
title: 배포와 클러스터 관리
weight: 11
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- 클러스터 매니저: Local(개발), Standalone(소규모), YARN(Hadoop), K8s(클라우드)
- 배포 모드: Client(디버깅), Cluster(프로덕션)
- 동적 할당으로 워크로드에 따라 Executor 자동 조절
- Fat JAR로 의존성 포함하여 spark-submit 실행
{{< /callout >}}

**대상 독자**: Spark 클러스터를 운영하는 DevOps/플랫폼 엔지니어

**선수 지식**:
- [아키텍처](architecture/) 문서의 Driver/Executor 개념
- Docker/Kubernetes 기초 (K8s 배포 시)
- Hadoop YARN 기초 (YARN 배포 시)

---

Spark 애플리케이션을 다양한 클러스터 환경에 배포하고 관리하는 방법을 알아봅니다.

## 비유로 이해하는 배포

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **Local 모드** | 1인 식당 운영 | 주방장(Driver)이 혼자 요리(Executor 역할)도 함. 빠른 테스트용 |
| **Standalone** | 소규모 자체 주방 | 직접 관리하는 주방. 간단하지만 확장에 한계 |
| **YARN** | 대형 호텔 총괄 주방 | 호텔(Hadoop) 전체 리소스를 관리하는 주방장이 따로 있음 |
| **Kubernetes** | 푸드코트 공유 주방 | 컨테이너 단위로 공간 임대. 유연하게 확장/축소 가능 |
| **Client 모드** | 주방장이 손님 옆에서 조리 | 바로 피드백 가능하지만, 손님(클라이언트)이 떠나면 요리 중단 |
| **Cluster 모드** | 주방장이 백주방에서 조리 | 손님이 떠나도 요리 계속. 완성되면 전달 |
| **동적 할당** | 바쁠 때만 알바생 추가 | 주문량(워크로드)에 따라 인력 자동 조절 |

> **핵심 원리**: 배포 환경 선택은 "기존 인프라", "확장 요구", "운영 복잡도"의 균형점을 찾는 것입니다.

## 왜 여러 클러스터 매니저가 존재하는가? (설계 철학)

**질문: Spark 전용 매니저(Standalone)가 있는데 왜 YARN, K8s도 지원하나요?**

각 환경은 서로 다른 문제를 해결합니다:

**1. Standalone - 단순함**
- Spark만 실행하는 전용 클러스터
- 설정이 가장 간단, 학습/테스트에 적합
- **단점**: 다른 워크로드와 리소스 공유 어려움

**2. YARN - 기업 환경 통합**
- 이미 Hadoop 클러스터가 있는 조직
- Spark, Hive, MapReduce가 같은 리소스 풀 공유
- **장점**: 기존 보안/거버넌스 정책 재활용

**3. Kubernetes - 클라우드 네이티브**
- 컨테이너 기반 현대 인프라
- 멀티 클라우드, 빠른 확장/축소
- **장점**: CI/CD 파이프라인 통합, 리소스 격리

**배포 모드 선택 기준**

| 상황 | Client 모드 | Cluster 모드 |
|------|-------------|--------------|
| 대화형 분석/디버깅 | ✅ 권장 | |
| Jupyter/Zeppelin 연동 | ✅ 필수 | |
| 배치 작업 (스케줄러) | | ✅ 권장 |
| 장기 실행 작업 | | ✅ 권장 |
| 네트워크 불안정 환경 | | ✅ 권장 |

Client 모드는 드라이버가 로컬에서 실행되어 **즉각적인 피드백**이 가능하고, Cluster 모드는 드라이버가 클러스터 내에서 실행되어 **네트워크 지연과 클라이언트 종료에 강건**합니다.

## 클러스터 매니저 유형

| 유형 | 특징 | 적합한 환경 |
|------|------|------------|
| **Local** | 단일 JVM | 개발/테스트 |
| **Standalone** | Spark 내장 | 소규모 전용 클러스터 |
| **YARN** | Hadoop 생태계 통합 | 기존 Hadoop 환경 |
| **Kubernetes** | 컨테이너 기반 | 클라우드 네이티브 |
| **Mesos** | 범용 스케줄러 | 다양한 워크로드 혼합 |

{{< callout type="info" title="핵심 포인트" >}}
- **Local**: 단일 JVM, 개발/테스트용
- **Standalone**: Spark 내장, 소규모 전용 클러스터
- **YARN**: Hadoop 생태계 통합, 기업 환경에서 가장 많이 사용
- **Kubernetes**: 컨테이너 기반, 클라우드 네이티브 환경
{{< /callout >}}

## Local 모드

개발과 테스트에 사용합니다.

```java
SparkSession spark = SparkSession.builder()
    .appName("Local App")
    .master("local")      // 단일 스레드
    .master("local[4]")   // 4개 스레드
    .master("local[*]")   // 모든 코어
    .getOrCreate();
```

## Standalone 클러스터

Spark에 내장된 간단한 클러스터 매니저입니다.

**클러스터 시작**

```bash
# Master 시작
$SPARK_HOME/sbin/start-master.sh

# Worker 시작 (각 노드에서)
$SPARK_HOME/sbin/start-worker.sh spark://master-host:7077

# 또는 한 번에 시작 (conf/workers 파일 필요)
$SPARK_HOME/sbin/start-all.sh
```

**애플리케이션 제출**

```bash
spark-submit \
  --master spark://master-host:7077 \
  --deploy-mode client \
  --executor-memory 4G \
  --total-executor-cores 16 \
  myapp.jar
```

**conf/spark-defaults.conf**

```properties
spark.master                     spark://master-host:7077
spark.driver.memory              4g
spark.executor.memory            8g
spark.executor.cores             4
spark.default.parallelism        200
```

## YARN

Hadoop YARN에서 Spark를 실행합니다.

**환경 설정**

```bash
# HADOOP_CONF_DIR 설정
export HADOOP_CONF_DIR=/etc/hadoop/conf

# 또는 Spark 설정에서
spark.hadoop.fs.defaultFS=hdfs://namenode:8020
```

**애플리케이션 제출**

```bash
# Client 모드 (Driver가 로컬)
spark-submit \
  --master yarn \
  --deploy-mode client \
  --executor-memory 8G \
  --executor-cores 4 \
  --num-executors 10 \
  myapp.jar

# Cluster 모드 (Driver가 클러스터 내)
spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --executor-memory 8G \
  --executor-cores 4 \
  --num-executors 10 \
  myapp.jar
```

**YARN 설정**

```properties
# Executor 설정
spark.executor.instances=10
spark.executor.memory=8g
spark.executor.cores=4

# 동적 할당 (권장)
spark.dynamicAllocation.enabled=true
spark.dynamicAllocation.minExecutors=2
spark.dynamicAllocation.maxExecutors=20
spark.dynamicAllocation.initialExecutors=5
spark.shuffle.service.enabled=true

# 큐 지정
spark.yarn.queue=production
```

**동적 할당**

워크로드에 따라 Executor 수를 자동 조절합니다.

```properties
spark.dynamicAllocation.enabled=true
spark.dynamicAllocation.minExecutors=2
spark.dynamicAllocation.maxExecutors=100
spark.dynamicAllocation.initialExecutors=5

# 스케일 업/다운 조건
spark.dynamicAllocation.schedulerBacklogTimeout=1s
spark.dynamicAllocation.sustainedSchedulerBacklogTimeout=5s
spark.dynamicAllocation.executorIdleTimeout=60s

# 외부 셔플 서비스 필요
spark.shuffle.service.enabled=true
```

## Kubernetes

컨테이너 환경에서 Spark를 실행합니다.

**애플리케이션 제출**

```bash
spark-submit \
  --master k8s://https://<k8s-apiserver-host>:<k8s-apiserver-port> \
  --deploy-mode cluster \
  --name spark-app \
  --conf spark.kubernetes.container.image=spark:3.5.1 \
  --conf spark.kubernetes.namespace=spark \
  --conf spark.executor.instances=5 \
  --conf spark.executor.memory=8g \
  --conf spark.executor.cores=4 \
  local:///opt/spark/examples/jars/myapp.jar
```

**Kubernetes 설정**

```properties
# 기본 설정
spark.kubernetes.container.image=my-registry/spark:3.5.1
spark.kubernetes.namespace=spark
spark.kubernetes.authenticate.driver.serviceAccountName=spark

# Driver 설정
spark.kubernetes.driver.pod.name=spark-driver
spark.kubernetes.driver.request.cores=2
spark.kubernetes.driver.limit.cores=4

# Executor 설정
spark.executor.instances=5
spark.kubernetes.executor.request.cores=2
spark.kubernetes.executor.limit.cores=4

# 볼륨 마운트
spark.kubernetes.driver.volumes.persistentVolumeClaim.data.mount.path=/data
spark.kubernetes.driver.volumes.persistentVolumeClaim.data.options.claimName=spark-data

# Secret
spark.kubernetes.driver.secretKeyRef.AWS_ACCESS_KEY_ID=aws-secret:access-key
```

**Docker 이미지 빌드**

```dockerfile
# Dockerfile
FROM apache/spark:3.5.1

# 의존성 추가
COPY target/myapp.jar /opt/spark/jars/

# 환경 설정
ENV SPARK_HOME=/opt/spark
```

```bash
# 이미지 빌드
docker build -t my-registry/spark-app:1.0 .
docker push my-registry/spark-app:1.0
```

## spark-submit 옵션

**기본 옵션**

```bash
spark-submit \
  --class com.example.MyApp \          # 메인 클래스
  --master yarn \                       # 클러스터 매니저
  --deploy-mode cluster \               # 배포 모드
  --name "My Application" \             # 애플리케이션 이름
  myapp.jar \                           # JAR 파일
  arg1 arg2                             # 애플리케이션 인자
```

**리소스 옵션**

```bash
spark-submit \
  --driver-memory 4g \                  # Driver 메모리
  --driver-cores 2 \                    # Driver 코어
  --executor-memory 8g \                # Executor 메모리
  --executor-cores 4 \                  # Executor당 코어
  --num-executors 10 \                  # Executor 수
  --total-executor-cores 40 \           # 총 Executor 코어 (Standalone)
  myapp.jar
```

**의존성 옵션**

```bash
spark-submit \
  --jars lib/dep1.jar,lib/dep2.jar \   # 추가 JAR
  --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1 \  # Maven 패키지
  --repositories https://repo.example.com \  # 추가 저장소
  --files config.properties \           # 배포할 파일
  --py-files lib.py,utils.py \          # Python 파일
  myapp.jar
```

**설정 옵션**

```bash
spark-submit \
  --conf spark.sql.shuffle.partitions=200 \
  --conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
  --conf spark.executor.extraJavaOptions="-XX:+UseG1GC" \
  --properties-file spark.properties \  # 설정 파일
  myapp.jar
```

## Fat JAR 생성

**Gradle (Shadow Plugin)**

```groovy
// build.gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

dependencies {
    implementation 'org.apache.spark:spark-sql_2.13:3.5.1'
    // provided로 지정하면 Fat JAR에서 제외
    compileOnly 'org.apache.spark:spark-core_2.13:3.5.1'
}

shadowJar {
    archiveBaseName.set('myapp')
    archiveClassifier.set('')
    archiveVersion.set('1.0')

    // Spark/Scala 라이브러리 제외 (클러스터에 있음)
    dependencies {
        exclude(dependency('org.apache.spark:.*'))
        exclude(dependency('org.scala-lang:.*'))
    }
}
```

```bash
./gradlew shadowJar
# build/libs/myapp-1.0.jar 생성
```

**Maven (Shade Plugin)**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <artifactSet>
                    <excludes>
                        <exclude>org.apache.spark:*</exclude>
                        <exclude>org.scala-lang:*</exclude>
                    </excludes>
                </artifactSet>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 모니터링

**Spark History Server**

완료된 애플리케이션의 UI를 제공합니다.

```bash
# 이벤트 로그 디렉토리 설정
spark.eventLog.enabled=true
spark.eventLog.dir=hdfs:///spark-history

# History Server 시작
$SPARK_HOME/sbin/start-history-server.sh

# 접속: http://history-server:18080
```

**메트릭 수집**

```properties
# Prometheus 메트릭
spark.metrics.conf.*.sink.prometheusServlet.class=org.apache.spark.metrics.sink.PrometheusServlet
spark.metrics.conf.*.sink.prometheusServlet.path=/metrics

# Graphite
spark.metrics.conf.*.sink.graphite.class=org.apache.spark.metrics.sink.GraphiteSink
spark.metrics.conf.*.sink.graphite.host=graphite-host
spark.metrics.conf.*.sink.graphite.port=2003
```

**로깅 설정**

```properties
# log4j2.properties
rootLogger.level = WARN
rootLogger.appenderRef.console.ref = console

# Spark 로깅
logger.spark.name = org.apache.spark
logger.spark.level = WARN

# 애플리케이션 로깅
logger.myapp.name = com.example
logger.myapp.level = INFO
```

## 보안

**Kerberos 인증**

```properties
spark.yarn.principal=spark@EXAMPLE.COM
spark.yarn.keytab=/path/to/spark.keytab
```

**SSL/TLS**

```properties
# Executor-Driver 간 통신 암호화
spark.ssl.enabled=true
spark.ssl.keyStore=/path/to/keystore
spark.ssl.keyStorePassword=password
spark.ssl.trustStore=/path/to/truststore
spark.ssl.trustStorePassword=password
```

**접근 제어**

```properties
# ACL 활성화
spark.acls.enable=true
spark.admin.acls=admin1,admin2
spark.modify.acls=developer1,developer2
spark.ui.view.acls=viewer1,viewer2
```

## 운영 팁

**애플리케이션 관리**

```bash
# 실행 중인 애플리케이션 목록 (YARN)
yarn application -list

# 애플리케이션 종료
yarn application -kill application_xxx

# 로그 확인
yarn logs -applicationId application_xxx
```

**장애 대응**

```properties
# 재시도 설정
spark.task.maxFailures=4
spark.stage.maxConsecutiveAttempts=4

# 추측 실행 (느린 Task 재실행)
spark.speculation=true
spark.speculation.interval=100ms
spark.speculation.multiplier=1.5
spark.speculation.quantile=0.75
```

**리소스 정리**

```properties
# 임시 파일 정리
spark.cleaner.periodicGC.interval=30min
spark.cleaner.referenceTracking.cleanCheckpoints=true

# 셔플 파일 정리
spark.shuffle.service.enabled=true
spark.shuffle.registration.timeout=60000ms
```

## 클라우드 배포

**AWS EMR**

```bash
aws emr create-cluster \
  --name "Spark Cluster" \
  --release-label emr-6.15.0 \
  --applications Name=Spark \
  --instance-type m5.xlarge \
  --instance-count 3
```

**GCP Dataproc**

```bash
gcloud dataproc clusters create spark-cluster \
  --region=us-central1 \
  --master-machine-type=n1-standard-4 \
  --worker-machine-type=n1-standard-4 \
  --num-workers=3
```

**Azure HDInsight / Databricks**

각 플랫폼의 관리 콘솔 또는 CLI를 통해 클러스터를 생성합니다.

## 다음 단계

- [FAQ](../appendix/faq/) - 운영 중 자주 발생하는 문제
- [참고 자료](../appendix/references/) - 추가 학습 자료
