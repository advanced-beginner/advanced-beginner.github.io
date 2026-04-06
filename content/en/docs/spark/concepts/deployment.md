---
title: Deployment and Cluster Management
description: "Spark deployment modes and cluster management strategies"
weight: 11
lastmod: "2026-04-06"
---

# Deployment and Cluster Management

Learn how to deploy and manage Spark applications in various cluster environments.

## Cluster Manager Types

| Type | Characteristics | Suitable Environment |
|------|-----------------|---------------------|
| **Local** | Single JVM | Development/Testing |
| **Standalone** | Spark built-in | Small dedicated clusters |
| **YARN** | Hadoop ecosystem integration | Existing Hadoop environments |
| **Kubernetes** | Container-based | Cloud native |
| **Mesos** | General-purpose scheduler | Mixed workloads |

## Local Mode

Used for development and testing.

```java
SparkSession spark = SparkSession.builder()
    .appName("Local App")
    .master("local")      // Single thread
    .master("local[4]")   // 4 threads
    .master("local[*]")   // All cores
    .getOrCreate();
```

## Standalone Cluster

A simple cluster manager built into Spark.

### Starting the Cluster

```bash
# Start Master
$SPARK_HOME/sbin/start-master.sh

# Start Worker (on each node)
$SPARK_HOME/sbin/start-worker.sh spark://master-host:7077

# Or start all at once (requires conf/workers file)
$SPARK_HOME/sbin/start-all.sh
```

### Submitting Applications

```bash
spark-submit \
  --master spark://master-host:7077 \
  --deploy-mode client \
  --executor-memory 4G \
  --total-executor-cores 16 \
  myapp.jar
```

### conf/spark-defaults.conf

```properties
spark.master                     spark://master-host:7077
spark.driver.memory              4g
spark.executor.memory            8g
spark.executor.cores             4
spark.default.parallelism        200
```

## YARN

Run Spark on Hadoop YARN.

### Environment Setup

```bash
# Set HADOOP_CONF_DIR
export HADOOP_CONF_DIR=/etc/hadoop/conf

# Or in Spark configuration
spark.hadoop.fs.defaultFS=hdfs://namenode:8020
```

### Submitting Applications

```bash
# Client mode (Driver runs locally)
spark-submit \
  --master yarn \
  --deploy-mode client \
  --executor-memory 8G \
  --executor-cores 4 \
  --num-executors 10 \
  myapp.jar

# Cluster mode (Driver runs in cluster)
spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --executor-memory 8G \
  --executor-cores 4 \
  --num-executors 10 \
  myapp.jar
```

### YARN Configuration

```properties
# Executor settings
spark.executor.instances=10
spark.executor.memory=8g
spark.executor.cores=4

# Dynamic allocation (recommended)
spark.dynamicAllocation.enabled=true
spark.dynamicAllocation.minExecutors=2
spark.dynamicAllocation.maxExecutors=20
spark.dynamicAllocation.initialExecutors=5
spark.shuffle.service.enabled=true

# Queue specification
spark.yarn.queue=production
```

### Dynamic Allocation

Automatically adjusts Executor count based on workload.

```properties
spark.dynamicAllocation.enabled=true
spark.dynamicAllocation.minExecutors=2
spark.dynamicAllocation.maxExecutors=100
spark.dynamicAllocation.initialExecutors=5

# Scale up/down conditions
spark.dynamicAllocation.schedulerBacklogTimeout=1s
spark.dynamicAllocation.sustainedSchedulerBacklogTimeout=5s
spark.dynamicAllocation.executorIdleTimeout=60s

# External shuffle service required — preserves shuffle data in a separate service so it is not lost when Executors are removed by dynamic allocation
spark.shuffle.service.enabled=true
```

## Kubernetes

Run Spark in container environments.

### Submitting Applications

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

### Kubernetes Configuration

```properties
# Basic settings
spark.kubernetes.container.image=my-registry/spark:3.5.1
spark.kubernetes.namespace=spark
spark.kubernetes.authenticate.driver.serviceAccountName=spark

# Driver settings
spark.kubernetes.driver.pod.name=spark-driver
spark.kubernetes.driver.request.cores=2
spark.kubernetes.driver.limit.cores=4

# Executor settings
spark.executor.instances=5
spark.kubernetes.executor.request.cores=2
spark.kubernetes.executor.limit.cores=4

# Volume mount
spark.kubernetes.driver.volumes.persistentVolumeClaim.data.mount.path=/data
spark.kubernetes.driver.volumes.persistentVolumeClaim.data.options.claimName=spark-data

# Secret
spark.kubernetes.driver.secretKeyRef.AWS_ACCESS_KEY_ID=aws-secret:access-key
```

### Building Docker Images

```dockerfile
# Dockerfile
FROM apache/spark:3.5.1

# Add dependencies
COPY target/myapp.jar /opt/spark/jars/

# Environment settings
ENV SPARK_HOME=/opt/spark
```

```bash
# Build image
docker build -t my-registry/spark-app:1.0 .
docker push my-registry/spark-app:1.0
```

## spark-submit Options

### Basic Options

```bash
spark-submit \
  --class com.example.MyApp \          # Main class
  --master yarn \                       # Cluster manager
  --deploy-mode cluster \               # Deploy mode
  --name "My Application" \             # Application name
  myapp.jar \                           # JAR file
  arg1 arg2                             # Application arguments
```

### Resource Options

```bash
spark-submit \
  --driver-memory 4g \                  # Driver memory
  --driver-cores 2 \                    # Driver cores
  --executor-memory 8g \                # Executor memory
  --executor-cores 4 \                  # Cores per Executor
  --num-executors 10 \                  # Number of Executors
  --total-executor-cores 40 \           # Total Executor cores (Standalone)
  myapp.jar
```

### Dependency Options

```bash
spark-submit \
  --jars lib/dep1.jar,lib/dep2.jar \   # Additional JARs
  --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1 \  # Maven packages
  --repositories https://repo.example.com \  # Additional repositories
  --files config.properties \           # Files to distribute
  --py-files lib.py,utils.py \          # Python files
  myapp.jar
```

### Configuration Options

```bash
spark-submit \
  --conf spark.sql.shuffle.partitions=200 \
  --conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
  --conf spark.executor.extraJavaOptions="-XX:+UseG1GC" \
  --properties-file spark.properties \  # Configuration file
  myapp.jar
```

## Fat JAR Creation

A Fat JAR packages the application code and all dependency libraries into a single JAR file. Since cluster nodes do not have user libraries installed, you must deploy as a single JAR containing all dependencies.

### Gradle (Shadow Plugin)

```groovy
// build.gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

dependencies {
    implementation 'org.apache.spark:spark-sql_2.13:3.5.1'  // 2.13 is the Scala version, required because Spark is built with Scala
    // Marked as provided to exclude from Fat JAR
    compileOnly 'org.apache.spark:spark-core_2.13:3.5.1'
}

shadowJar {
    archiveBaseName.set('myapp')
    archiveClassifier.set('')
    archiveVersion.set('1.0')

    // Exclude Spark/Scala libraries (available on cluster)
    dependencies {
        exclude(dependency('org.apache.spark:.*'))
        exclude(dependency('org.scala-lang:.*'))
    }
}
```

```bash
./gradlew shadowJar
# Creates build/libs/myapp-1.0.jar
```

### Maven (Shade Plugin)

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

## Monitoring

### Spark History Server

Provides UI for completed applications.

```bash
# Event log directory settings
spark.eventLog.enabled=true
spark.eventLog.dir=hdfs:///spark-history

# Start History Server
$SPARK_HOME/sbin/start-history-server.sh

# Access: http://history-server:18080
```

### Metrics Collection

```properties
# Prometheus metrics
spark.metrics.conf.*.sink.prometheusServlet.class=org.apache.spark.metrics.sink.PrometheusServlet
spark.metrics.conf.*.sink.prometheusServlet.path=/metrics

# Graphite
spark.metrics.conf.*.sink.graphite.class=org.apache.spark.metrics.sink.GraphiteSink
spark.metrics.conf.*.sink.graphite.host=graphite-host
spark.metrics.conf.*.sink.graphite.port=2003
```

### Logging Settings

```properties
# log4j2.properties
rootLogger.level = WARN
rootLogger.appenderRef.console.ref = console

# Spark logging
logger.spark.name = org.apache.spark
logger.spark.level = WARN

# Application logging
logger.myapp.name = com.example
logger.myapp.level = INFO
```

## Security

### Kerberos Authentication

```properties
spark.yarn.principal=spark@EXAMPLE.COM
spark.yarn.keytab=/path/to/spark.keytab
```

### SSL/TLS

```properties
# Encrypt Executor-Driver communication
spark.ssl.enabled=true
spark.ssl.keyStore=/path/to/keystore
spark.ssl.keyStorePassword=password
spark.ssl.trustStore=/path/to/truststore
spark.ssl.trustStorePassword=password
```

### Access Control

```properties
# Enable ACL
spark.acls.enable=true
spark.admin.acls=admin1,admin2
spark.modify.acls=developer1,developer2
spark.ui.view.acls=viewer1,viewer2
```

## Operations Tips

### Application Management

```bash
# List running applications (YARN)
yarn application -list

# Kill application
yarn application -kill application_xxx

# View logs
yarn logs -applicationId application_xxx
```

### Failure Handling

```properties
# Retry settings
spark.task.maxFailures=4
spark.stage.maxConsecutiveAttempts=4

# Speculative execution (re-run slow Tasks)
spark.speculation=true
spark.speculation.interval=100ms
spark.speculation.multiplier=1.5
spark.speculation.quantile=0.75
```

### Resource Cleanup

```properties
# Temporary file cleanup
spark.cleaner.periodicGC.interval=30min
spark.cleaner.referenceTracking.cleanCheckpoints=true

# Shuffle file cleanup
spark.shuffle.service.enabled=true
spark.shuffle.registration.timeout=60000ms
```

## Cloud Deployment

### AWS EMR

```bash
aws emr create-cluster \
  --name "Spark Cluster" \
  --release-label emr-6.15.0 \
  --applications Name=Spark \
  --instance-type m5.xlarge \
  --instance-count 3
```

### GCP Dataproc

```bash
gcloud dataproc clusters create spark-cluster \
  --region=us-central1 \
  --master-machine-type=n1-standard-4 \
  --worker-machine-type=n1-standard-4 \
  --num-workers=3
```

### Azure HDInsight / Databricks

Create clusters through each platform's management console or CLI.

## Next Steps

- [FAQ](../appendix/faq/) - Common operational issues
- [References](../appendix/references/) - Additional learning resources
