---
title: Environment Setup
weight: 1
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- Quickly set up Elasticsearch 8.11 + Kibana environment with **Docker Compose**
- Complete integration setup with **Spring Boot 3.2** + Spring Data Elasticsearch
- Install **Nori analyzer** for Korean search
- Total time required: approximately 15 minutes
{{< /callout >}}

{{< callout type="info" title="Version Information" >}}
- **Elasticsearch / Kibana**: 8.11.0
- **Spring Boot**: 3.2.0
- **Spring Data Elasticsearch**: 5.2.x (included in Spring Boot 3.2)
- **Java**: 17+
{{< /callout >}}

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

{{< callout type="info" title="Key Points" >}}
- `single-node` mode is for development environments only
- `xpack.security.enabled=false` is for development; always enable it in production
- Ensure ports 9200 (REST API) and 5601 (Kibana) are open
{{< /callout >}}

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

{{< callout type="info" title="Key Points" >}}
- Complete integration setup with a single `spring-boot-starter-data-elasticsearch` dependency
- The `uris` setting in `application.yml` must match the Docker environment
- Setting the log level to DEBUG allows you to see ES queries
{{< /callout >}}

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

### Modify docker-compose.yml

```yaml
services:
  elasticsearch:
    build:
      context: .
      dockerfile: Dockerfile
    # ... rest of the configuration remains the same
```

### Verify Nori Operation

```json
GET /_analyze
{
  "tokenizer": "nori_tokenizer",
  "text": "삼성전자가 새로운 스마트폰을 출시했다"
}
```

Response:
```json
{
  "tokens": [
    { "token": "삼성", "position": 0 },
    { "token": "전자", "position": 1 },
    { "token": "새롭", "position": 3 },
    { "token": "스마트폰", "position": 5 },
    { "token": "출시", "position": 7 }
  ]
}
```

{{< callout type="info" title="Key Points" >}}
- Nori installation uses the `elasticsearch-plugin install analysis-nori` command in the Dockerfile
- Verify analyzer operation by checking tokenization results with the `_analyze` API
- Use `decompound_mode: mixed` to decompose compound words (e.g., `삼성전자` → `삼성`, `전자`)
{{< /callout >}}

---

## Project Structure

```
examples/elasticsearch-quick-start/
├── build.gradle.kts
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── src/main/java/com/example/
│   ├── ElasticsearchApplication.java
│   ├── config/
│   │   └── ElasticsearchConfig.java
│   ├── domain/
│   │   └── Product.java
│   ├── repository/
│   │   └── ProductRepository.java
│   └── controller/
│       └── ProductController.java
└── src/main/resources/
    └── application.yml
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

### Spring Boot Connection Failure

```
Elasticsearch cluster not available: connect timed out
```

**Solution:**
1. Verify the `uris` address in `application.yml`
2. Wait for Elasticsearch to fully start (check healthcheck passes)
3. Verify Docker network settings (check if on same network)

{{< callout type="info" title="Key Points" >}}
- Most connection issues occur when attempting to connect before Elasticsearch is fully started
- Check status with `docker-compose ps`, verify response with `curl localhost:9200`
- On Linux, the `vm.max_map_count` setting is mandatory
{{< /callout >}}

---

## Next Steps

Once setup is complete:

| Goal | Recommended Document |
|------|---------------------|
| Implement basic CRUD | [Basic Examples](basic/) |
| Understand Elasticsearch structure | [Core Components](../concepts/core-components/) |
| Write search queries | [Query DSL](../concepts/query-dsl/) |
