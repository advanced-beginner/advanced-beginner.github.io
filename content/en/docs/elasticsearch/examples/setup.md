---
title: Environment Setup
weight: 1
lastmod: 2026-01-08
---

{{% notice style="info" title="Version Information" %}}
- **Elasticsearch / Kibana**: 8.11.0
- **Spring Boot**: 3.2.0
- **Spring Data Elasticsearch**: 5.2.x (included in Spring Boot 3.2)
- **Java**: 17+
{{% /notice %}}

Configure Elasticsearch + Kibana with Docker and set up a Spring Boot project.

## Docker Environment Setup

### docker-compose.yml

`docker/elasticsearch/docker-compose.yml` file:

```yaml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - xpack.security.enrollment.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -s http://localhost:9200/_cluster/health | grep -q '\"status\":\"green\"\\|\"status\":\"yellow\"'"]
      interval: 10s
      timeout: 10s
      retries: 10
    networks:
      - elastic

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
    depends_on:
      elasticsearch:
        condition: service_healthy
    networks:
      - elastic

volumes:
  elasticsearch-data:
    driver: local

networks:
  elastic:
    driver: bridge
```

### Settings Explanation

| Setting | Value | Description |
|---------|-------|-------------|
| `discovery.type` | single-node | Single node mode (for development) |
| `xpack.security.enabled` | false | Security disabled (for development) |
| `ES_JAVA_OPTS` | -Xms512m -Xmx512m | JVM heap memory (min=max) |
| `9200` | HTTP API port | REST API communication |
| `9300` | Transport port | Inter-node communication |

> **In production environments**, always enable security and allocate appropriate memory.

### Running

```bash
cd docker/elasticsearch
docker-compose up -d
```

### Status Check

```bash
# Container status
docker-compose ps

# Cluster status
curl http://localhost:9200/_cluster/health?pretty

# Node information
curl http://localhost:9200/_nodes?pretty
```

### Shutdown

```bash
# Stop containers (keep data)
docker-compose stop

# Remove containers and network (keep volumes)
docker-compose down

# Remove everything (including data)
docker-compose down -v
```

---

## Spring Boot Project Setup

### build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### application.yml

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
  data:
    elasticsearch:
      repositories:
        enabled: true

logging:
  level:
    org.springframework.data.elasticsearch: DEBUG
```

### Elasticsearch Configuration Class

```java
package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .build();
    }
}
```

---

## Korean Analyzer (Nori) Setup

Install the Nori analyzer for Korean search.

### Plugin Installation

To include Nori in the Docker image, create a Dockerfile:

```dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Install Nori Korean analyzer
RUN bin/elasticsearch-plugin install analysis-nori
```

### Verify Nori Operation

```json
GET /_analyze
{
  "tokenizer": "nori_tokenizer",
  "text": "Samsung Electronics released a new smartphone"
}
```

---

## Troubleshooting

### Connection Refused

```
Connection refused: localhost:9200
```

**Solution:**
1. Verify Elasticsearch container is running: `docker ps`
2. Check for port conflicts: `lsof -i :9200`
3. Check firewall settings

### Out of Memory

```
bootstrap check failure: max virtual memory areas vm.max_map_count [65530] is too low
```

**Linux Solution:**
```bash
sudo sysctl -w vm.max_map_count=262144

# Permanent
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

**Docker Desktop (Mac/Windows):**
Set Resources → Memory to 4GB or higher in Docker Desktop settings

---

## Next Steps

Once setup is complete:

| Goal | Recommended Document |
|------|---------------------|
| Implement basic CRUD | [Basic Examples](../basic/) |
| Understand Elasticsearch structure | [Core Components](../../concepts/core-components/) |
| Write search queries | [Query DSL](../../concepts/query-dsl/) |
