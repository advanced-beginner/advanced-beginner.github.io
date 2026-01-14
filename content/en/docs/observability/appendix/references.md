---
title: References
description: Official documentation, books, and additional learning resources
weight: 4
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

## Official Documentation

### Prometheus

| Resource | Link | Description |
|----------|------|-------------|
| Prometheus Official Docs | https://prometheus.io/docs/ | Configuration, PromQL, operations guide |
| PromQL Reference | https://prometheus.io/docs/prometheus/latest/querying/basics/ | Query language details |
| Alerting Rules | https://prometheus.io/docs/prometheus/latest/configuration/alerting_rules/ | Writing alerting rules |

### Grafana

| Resource | Link | Description |
|----------|------|-------------|
| Grafana Official Docs | https://grafana.com/docs/grafana/latest/ | Dashboard, panel configuration |
| Loki Docs | https://grafana.com/docs/loki/latest/ | Log collection/querying |
| Tempo Docs | https://grafana.com/docs/tempo/latest/ | Distributed tracing |

### OpenTelemetry

| Resource | Link | Description |
|----------|------|-------------|
| OpenTelemetry Official | https://opentelemetry.io/docs/ | Concepts, SDK, Collector |
| Java Instrumentation | https://opentelemetry.io/docs/languages/java/ | Java auto/manual instrumentation |
| Semantic Conventions | https://opentelemetry.io/docs/concepts/semantic-conventions/ | Standardized attribute names |

---

## Books

### Essential

| Book | Author | Content |
|------|--------|---------|
| **Site Reliability Engineering** | Google SRE Team | SRE principles, Golden Signals, SLO |
| **Observability Engineering** | Charity Majors, Liz Fong-Jones | Modern observability concepts |
| **The SRE Workbook** | Google SRE Team | Practical SRE application |

### Recommended

| Book | Author | Content |
|------|--------|---------|
| **Prometheus: Up & Running** | Brian Brazil | Detailed Prometheus guide |
| **Distributed Tracing in Practice** | Austin Parker et al. | Advanced distributed tracing |
| **Database Reliability Engineering** | Laine Campbell, Charity Majors | Database observability |

---

## Blogs & Articles

### Prometheus/Grafana

- [Prometheus Best Practices](https://prometheus.io/docs/practices/) - Official recommendations
- [Grafana Blog](https://grafana.com/blog/) - New features, case studies
- [Robust Perception Blog](https://www.robustperception.io/blog/) - Brian Brazil's Prometheus tips

### SRE/Observability

- [Google SRE Books (Free)](https://sre.google/books/) - Full content freely available
- [Honeycomb Blog](https://www.honeycomb.io/blog/) - Advanced observability
- [Charity Majors' Blog](https://charity.wtf/) - Insights from observability leader

---

## Videos

### Conferences

| Video | Link | Content |
|-------|------|---------|
| PromCon | https://www.youtube.com/@PrometheusIo | Prometheus conference |
| GrafanaCon | https://www.youtube.com/@Grafana | Grafana conference |
| KubeCon | https://www.youtube.com/@caborgg | Kubernetes, observability sessions |

### Tutorials

- [Prometheus Tutorial for Beginners](https://www.youtube.com/watch?v=h4Sl21AKiDg) - TechWorld with Nana
- [Grafana Tutorials](https://www.youtube.com/playlist?list=PLDGkOdUX1Ujrrse-cdj20RRah9hyHdxBu) - Grafana official

---

## Online Courses

| Course | Platform | Description |
|--------|----------|-------------|
| Prometheus & Grafana | Udemy | Hands-on focused |
| Site Reliability Engineering | Coursera | Google's SRE course |
| Observability with OpenTelemetry | Linux Foundation | OTel introduction |

---

## Community

### Slack

- [Prometheus Users](https://prometheus-users.slack.com/)
- [Grafana Community](https://grafana.slack.com/)
- [CNCF Slack](https://slack.cncf.io/) - #prometheus, #opentelemetry

### GitHub

| Project | Link |
|---------|------|
| Prometheus | https://github.com/prometheus/prometheus |
| Grafana | https://github.com/grafana/grafana |
| Loki | https://github.com/grafana/loki |
| Tempo | https://github.com/grafana/tempo |
| OpenTelemetry | https://github.com/open-telemetry |

---

## Dashboards & Rules

### Grafana Dashboards

| ID | Name | Purpose |
|----|------|---------|
| 1860 | Node Exporter Full | Server monitoring |
| 3662 | Prometheus Stats | Prometheus self-monitoring |
| 4701 | JVM Micrometer | Spring Boot JVM |
| 7362 | MySQL Overview | MySQL monitoring |
| 7587 | PostgreSQL | PostgreSQL monitoring |
| 11074 | Kafka Exporter | Kafka monitoring |

Searchable at https://grafana.com/grafana/dashboards/

### Alerting Rules

- [Awesome Prometheus Alerts](https://awesome-prometheus-alerts.grep.to/) - Collection of alerts by situation
- [Prometheus Operator Rules](https://github.com/prometheus-operator/kube-prometheus/tree/main/manifests) - Kubernetes environments

---

## Tools

### Testing & Validation

| Tool | Purpose |
|------|---------|
| `promtool` | Validate Prometheus config/rules |
| `amtool` | Validate Alertmanager config |
| `logcli` | Loki CLI query tool |

### Simulation

| Tool | Purpose |
|------|---------|
| `prometheus-fake-exporter` | Generate fake metrics |
| `hey` | HTTP load testing |
| `k6` | Load testing + metrics |

---

## Certifications

| Certification | Provider | Content |
|--------------|----------|---------|
| CKA/CKAD | CNCF | Kubernetes (Prometheus integration) |
| Prometheus Certified Associate | CNCF | Prometheus official certification (2024~) |
| Grafana Associate | Grafana Labs | Grafana fundamentals |
