---
title: References
weight: 3
lastmod: 2026-01-08
---

Official documentation, recommended books, and community resource links.

## Official Documentation

### Elasticsearch

- [Elasticsearch Official Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Elasticsearch Client Documentation](https://www.elastic.co/guide/en/elasticsearch/client/index.html)
- [REST API Reference](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)
- [Query DSL Reference](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html)

### Kibana

- [Kibana Official Guide](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Dev Tools Usage](https://www.elastic.co/guide/en/kibana/current/console-kibana.html)

### Spring Data Elasticsearch

- [Spring Data Elasticsearch Official Docs](https://docs.spring.io/spring-data/elasticsearch/reference/)
- [Spring Data Elasticsearch GitHub](https://github.com/spring-projects/spring-data-elasticsearch)

## Recommended Books

### Beginner

| Book | Author | Features |
|------|--------|----------|
| Elasticsearch in Action (2nd Ed) | Madhusudhan Konda | Hands-on examples |
| Learning Elastic Stack 8.0 | Pranav Shukla | Full ELK stack coverage |

### Advanced

| Book | Author | Features |
|------|--------|----------|
| Elasticsearch: The Definitive Guide | Clinton Gormley | Principle understanding (older version but useful for concepts) |
| Relevant Search | Doug Turnbull | Search relevance optimization |

## Online Courses

### Free

- [Elastic Official Training](https://www.elastic.co/training/free) - Free foundational courses
- [YouTube: Elastic Official Channel](https://www.youtube.com/c/Elastic)

### Paid

- [Elastic Certification Program](https://www.elastic.co/training/certification)
- Udemy, Coursera Elasticsearch courses

## Community

### Forums & Q&A

- [Elastic Discuss](https://discuss.elastic.co/) - Official forum
- [Stack Overflow elasticsearch tag](https://stackoverflow.com/questions/tagged/elasticsearch)

### GitHub

- [Elasticsearch GitHub](https://github.com/elastic/elasticsearch)
- [Korean Morphological Analyzer Nori](https://github.com/elastic/elasticsearch/tree/main/plugins/analysis-nori)

## Blogs & Articles

### Elastic Official

- [Elastic Blog](https://www.elastic.co/blog/)
- [Elastic Engineering Blog](https://www.elastic.co/blog/category/engineering)

### Recommended Technical Blog Posts

- Search quality improvement case studies
- High-volume indexing optimization
- Cluster operation experience

## Tools

### Development/Testing

| Tool | Purpose |
|------|---------|
| Kibana Dev Tools | API testing, query writing |
| Elasticsearch Head | Cluster visualization (Chrome extension) |
| Cerebro | Cluster management UI |

### Monitoring

| Tool | Purpose |
|------|---------|
| Kibana Stack Monitoring | Official monitoring |
| Grafana + Prometheus | Custom dashboards |
| Elastic APM | Application performance monitoring |

### Data Synchronization

| Tool | Purpose |
|------|---------|
| Logstash | Data collection from various sources |
| Debezium | CDC-based DB → Elasticsearch sync |
| Kafka Connect | Kafka ↔ Elasticsearch integration |

## Cloud Service Comparison

Consider managed services instead of self-operation.

### Major Services

| Service | Provider | Features | Price Range |
|---------|----------|----------|-------------|
| **Elastic Cloud** | Elastic | Official service, latest features first | Medium-High |
| **Amazon OpenSearch** | AWS | AWS integration, OpenSearch-based | Medium |
| **Azure Cognitive Search** | Microsoft | AI features, Azure integration | Medium-High |
| **Google Cloud Elasticsearch** | GCP | Elastic partnership | Medium |

### Elastic Cloud vs Self-Managed

| Item | Elastic Cloud | Self-Managed |
|------|---------------|--------------|
| **Initial Setup** | Minutes | Hours to days |
| **Operational Burden** | Low | High |
| **Cost** | Higher (hourly billing) | Infrastructure only |
| **Customization** | Limited | Full freedom |
| **Upgrades** | Auto/Easy | Manual planning required |
| **Security Setup** | Built-in | Configure yourself |

### Selection Guide

```
AWS environment + cost optimization → Amazon OpenSearch
Latest features + official support → Elastic Cloud
Full control needed → Self-Managed
Azure/GCP environment → Respective cloud's managed service
```

> **Tip**: Start small with Self-Managed, consider managed services when scaling up

---

## Version Changes

### Elasticsearch 8.x Major Changes

- Security enabled by default
- Java API Client (replaces High-Level REST Client)
- Enhanced vector search (kNN)
- New license model (SSPL + Elastic License 2.0)

### Spring Data Elasticsearch 5.x

- Uses Elasticsearch Java Client (replaces RestHighLevelClient)
- Enhanced Reactive support
- Elasticsearch 8.x compatible

## Certification

### Elastic Certified Engineer

- [Certification Info](https://www.elastic.co/training/certification/engineer)
- Hands-on exam
- Validates Elasticsearch operational skills

### Elastic Certified Analyst

- [Certification Info](https://www.elastic.co/training/certification/analyst)
- Validates Kibana skills
- Data visualization, dashboard building
