---
title: 환경 설정
weight: 1
---

Docker로 Elasticsearch + Kibana를 구성하고, Spring Boot 프로젝트를 설정합니다.

## Docker 환경 구성

### docker-compose.yml

`docker/elasticsearch/docker-compose.yml` 파일:

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

### 설정 설명

| 설정 | 값 | 설명 |
|------|---|------|
| `discovery.type` | single-node | 단일 노드 모드 (개발용) |
| `xpack.security.enabled` | false | 보안 비활성화 (개발용) |
| `ES_JAVA_OPTS` | -Xms512m -Xmx512m | JVM 힙 메모리 (최소=최대) |
| `9200` | HTTP API 포트 | REST API 통신용 |
| `9300` | Transport 포트 | 노드 간 통신용 |

> **프로덕션 환경에서는** 반드시 보안을 활성화하고, 적절한 메모리를 할당하세요.

### 실행

```bash
cd docker/elasticsearch
docker-compose up -d
```

### 상태 확인

```bash
# 컨테이너 상태
docker-compose ps

# 클러스터 상태
curl http://localhost:9200/_cluster/health?pretty

# 노드 정보
curl http://localhost:9200/_nodes?pretty
```

### 종료

```bash
# 컨테이너 중지 (데이터 유지)
docker-compose stop

# 컨테이너 및 네트워크 삭제 (볼륨 유지)
docker-compose down

# 모든 것 삭제 (데이터 포함)
docker-compose down -v
```

---

## Spring Boot 프로젝트 설정

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

### Elasticsearch 설정 클래스

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

## 한글 분석기 (Nori) 설정

한글 검색을 위해 Nori 분석기를 설치합니다.

### 플러그인 설치

Docker 이미지에 Nori를 포함하려면 Dockerfile을 만듭니다:

```dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Nori 한글 분석기 설치
RUN bin/elasticsearch-plugin install analysis-nori
```

### docker-compose.yml 수정

```yaml
services:
  elasticsearch:
    build:
      context: .
      dockerfile: Dockerfile
    # ... 나머지 설정 동일
```

### Nori 동작 확인

```json
GET /_analyze
{
  "tokenizer": "nori_tokenizer",
  "text": "삼성전자가 새로운 스마트폰을 출시했다"
}
```

응답:
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

---

## 프로젝트 구조

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

## 트러블슈팅

### 연결 거부

```
Connection refused: localhost:9200
```

**해결:**
1. Elasticsearch 컨테이너 실행 확인: `docker ps`
2. 포트 충돌 확인: `lsof -i :9200`
3. 방화벽 설정 확인

### 메모리 부족

```
bootstrap check failure: max virtual memory areas vm.max_map_count [65530] is too low
```

**Linux 해결:**
```bash
sudo sysctl -w vm.max_map_count=262144

# 영구 적용
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

**Docker Desktop (Mac/Windows):**
Docker Desktop 설정에서 Resources → Memory를 4GB 이상으로 설정

### Spring Boot 연결 실패

```
Elasticsearch cluster not available: connect timed out
```

**해결:**
1. `application.yml`의 `uris` 주소 확인
2. Elasticsearch가 완전히 시작될 때까지 대기 (healthcheck 통과 확인)
3. Docker 네트워크 설정 확인 (같은 네트워크인지)

---

## 다음 단계

환경 설정이 완료되면:

| 목표 | 추천 문서 |
|------|----------|
| 기본 CRUD 구현 | [기본 예제](../basic/) |
| Elasticsearch 구조 이해 | [핵심 구성요소](../../concepts/core-components/) |
| 검색 쿼리 작성 | [Query DSL](../../concepts/query-dsl/) |
