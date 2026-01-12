---
title: Quick Start
description: 10분 만에 Prometheus + Grafana 환경을 구축하고 첫 번째 메트릭을 확인합니다
weight: 1
bookCollapseSection: true
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **대상 독자**: Observability를 처음 접하는 개발자
> **선수 지식**: Docker, Docker Compose 기본 사용법
> **소요 시간**: 약 10분
> **이 문서를 읽으면**: Prometheus로 메트릭을 수집하고 Grafana에서 시각화할 수 있습니다

## TL;DR

{{< callout type="info" >}}
**핵심만 빠르게:**
1. Docker Compose로 Prometheus + Grafana 실행
2. 샘플 애플리케이션에서 메트릭 노출
3. Grafana 대시보드에서 실시간 확인
4. 간단한 알림 규칙 설정
{{< /callout >}}

## 전체 흐름

```mermaid
graph LR
    A["Step 1<br>환경 준비"] --> B["Step 2<br>스택 실행"]
    B --> C["Step 3<br>메트릭 확인"]
    C --> D["Step 4<br>대시보드 구성"]

    style A fill:#e1f5fe
    style D fill:#c8e6c9
```

---

## Step 1/4: 환경 준비 (2분)

### 필수 도구 확인

{{< tabs "prereq-check" >}}
{{< tab "macOS/Linux" >}}
```bash
docker --version
docker compose version
```
{{< /tab >}}
{{< tab "Windows" >}}
```powershell
docker --version
docker compose version
```
{{< /tab >}}
{{< /tabs >}}

**예상 출력:**
```
Docker version 24.0.0 이상
Docker Compose version v2.20.0 이상
```

{{< callout type="warning" >}}
Docker Desktop이 **실행 중**인지 확인하세요. 명령어가 실패하면 Docker Desktop을 시작한 후 다시 시도하세요.
{{< /callout >}}

### 프로젝트 디렉토리 생성

```bash
mkdir -p observability-quickstart && cd observability-quickstart
```

### 확인 체크리스트

- [ ] Docker 버전 24.0.0 이상 출력됨
- [ ] Docker Compose 버전 2.20.0 이상 출력됨
- [ ] `observability-quickstart` 디렉토리로 이동함

---

## Step 2/4: Observability 스택 실행 (3분)

### docker-compose.yml 생성

아래 내용을 `docker-compose.yml` 파일로 저장합니다.

```yaml
services:
  prometheus:
    image: prom/prometheus:v2.50.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--web.enable-lifecycle'

  grafana:
    image: grafana/grafana:10.3.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer
    volumes:
      - grafana-data:/var/lib/grafana

  # Node Exporter: 시스템 메트릭 노출
  node-exporter:
    image: prom/node-exporter:v1.7.0
    container_name: node-exporter
    ports:
      - "9100:9100"
    restart: unless-stopped

volumes:
  grafana-data:
```

### prometheus.yml 생성

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
```

### 스택 실행

```bash
docker compose up -d
```

**예상 출력:**
```
[+] Running 4/4
 ✔ Network observability-quickstart_default  Created
 ✔ Container prometheus                      Started
 ✔ Container grafana                         Started
 ✔ Container node-exporter                   Started
```

### 상태 확인

```bash
docker compose ps
```

**예상 출력:**
```
NAME            STATUS    PORTS
grafana         Up        0.0.0.0:3000->3000/tcp
prometheus      Up        0.0.0.0:9090->9090/tcp
node-exporter   Up        0.0.0.0:9100->9100/tcp
```

{{< callout type="warning" >}}
**포트 충돌 시**: 이미 3000, 9090, 9100 포트를 사용 중이라면 docker-compose.yml에서 포트 번호를 변경하세요.
{{< /callout >}}

### 확인 체크리스트

- [ ] 3개 컨테이너 모두 `Up` 상태
- [ ] http://localhost:9090 접속 시 Prometheus UI 표시
- [ ] http://localhost:3000 접속 시 Grafana 로그인 화면 표시

---

## Step 3/4: 메트릭 확인 (3분)

### Prometheus UI에서 메트릭 조회

1. 브라우저에서 http://localhost:9090 접속
2. 상단 검색창에 아래 쿼리 입력:

```promql
up
```

3. **Execute** 버튼 클릭

**예상 결과:**
```
up{instance="localhost:9090", job="prometheus"} 1
up{instance="node-exporter:9100", job="node-exporter"} 1
```

{{< callout type="info" >}}
**`up` 메트릭이란?**
타겟이 정상적으로 스크래핑되면 `1`, 실패하면 `0`을 반환합니다. 가장 기본적인 헬스체크 메트릭입니다.
{{< /callout >}}

### 더 많은 메트릭 확인

```promql
node_memory_MemTotal_bytes
```

이 쿼리는 Node Exporter가 수집한 시스템 전체 메모리를 보여줍니다.

### Graph 탭에서 시각화

1. **Graph** 탭 클릭
2. 아래 쿼리 입력:

```promql
100 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100)
```

3. 시간 범위를 **Last 15 minutes**로 설정

**예상 결과:** 시간에 따른 메모리 사용률(%) 그래프가 표시됩니다.

### 확인 체크리스트

- [ ] `up` 쿼리 결과에서 2개 타겟이 `1` 반환
- [ ] `node_memory_MemTotal_bytes` 쿼리 결과 확인
- [ ] Graph 탭에서 메모리 사용률 그래프 시각화 확인

---

## Step 4/4: Grafana 대시보드 구성 (2분)

### Grafana 로그인

1. http://localhost:3000 접속
2. 로그인 정보 입력:
   - **Username**: `admin`
   - **Password**: `admin`
3. 비밀번호 변경 화면에서 **Skip** 클릭 (테스트 환경)

### Prometheus 데이터 소스 추가

1. 좌측 메뉴에서 **Connections** → **Data sources** 클릭
2. **Add data source** 버튼 클릭
3. **Prometheus** 선택
4. URL 입력: `http://prometheus:9090`
5. **Save & test** 클릭

**예상 결과:** "Successfully queried the Prometheus API" 메시지

### 첫 번째 대시보드 생성

1. 좌측 메뉴에서 **Dashboards** → **New** → **New Dashboard** 클릭
2. **Add visualization** 클릭
3. 데이터 소스로 **Prometheus** 선택
4. 쿼리 입력:

```promql
100 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100)
```

5. **Run queries** 클릭
6. Panel options에서 Title을 **Memory Usage (%)** 로 입력
7. 우측 상단 **Apply** 클릭

### 확인 체크리스트

- [ ] Grafana 로그인 성공
- [ ] Prometheus 데이터 소스 연결 성공
- [ ] 첫 번째 패널에 그래프 표시됨

---

## 완료

{{< callout type="info" >}}
**축하합니다!** Prometheus + Grafana 기반 Observability 환경을 성공적으로 구축했습니다.

**구축한 환경:**
- **Prometheus**: 메트릭 수집 및 저장
- **Grafana**: 시각화 및 대시보드
- **Node Exporter**: 호스트 시스템 메트릭 노출 (CPU, 메모리, 디스크 등)
{{< /callout >}}

## 정리하기

실습이 끝나면 리소스를 정리합니다.

```bash
docker compose down -v
```

## 트러블슈팅

### 컨테이너가 시작되지 않음

```bash
docker compose logs prometheus
```

로그를 확인하여 설정 파일 오류가 있는지 점검합니다.

### Grafana에서 Prometheus 연결 실패

- URL이 `http://prometheus:9090`인지 확인 (localhost가 아님)
- Prometheus 컨테이너가 실행 중인지 확인

### 포트 충돌

이미 사용 중인 포트가 있다면:

```bash
lsof -i :9090  # Prometheus
lsof -i :3000  # Grafana
lsof -i :9100  # Node Exporter
```

해당 프로세스를 종료하거나 docker-compose.yml의 포트를 변경하세요.

---

## 다음 단계

Quick Start를 완료했다면, 다음 문서로 개념을 깊이 이해하세요.

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [관측성 3요소]({{< relref "/docs/observability/concepts/three-pillars" >}}) | Metrics, Logs, Traces의 역할 |
| 2 | [메트릭 기초]({{< relref "/docs/observability/concepts/metrics-fundamentals" >}}) | Counter, Gauge, Histogram 타입 |
| 3 | [Prometheus 아키텍처]({{< relref "/docs/observability/concepts/prometheus-architecture" >}}) | Pull 모델, 시계열 DB 원리 |
| 4 | [Spring Boot 예제]({{< relref "/docs/observability/examples/spring-boot-metrics" >}}) | 실제 애플리케이션 적용 |
