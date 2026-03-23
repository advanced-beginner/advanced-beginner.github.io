---
bookCollapseSection: true
title: Hands-on Examples
description: "Elasticsearch hands-on examples and document index"
weight: 3
lastmod: 2026-01-08
---

Executable example code based on Spring Boot.

{{< callout type="info" title="Version Information" >}}
All examples are based on **Elasticsearch 8.11.x**, **Spring Boot 3.2.x**, and **Java 17+**.
{{< /callout >}}

## Example List

### [Environment Setup]({{< relref "/docs/elasticsearch/examples/setup" >}})
Configure Elasticsearch + Kibana with Docker and set up a Spring Boot project.

### [Basic Examples]({{< relref "/docs/elasticsearch/examples/basic" >}})
Implement Document CRUD and basic search using Spring Data Elasticsearch.

### [Product Search System]({{< relref "/docs/elasticsearch/examples/product-search" >}})
Implement production-ready product search features:
- Korean morphological analysis (nori)
- Autocomplete
- Filter + search combination
- Search result highlighting

### [Log Analysis System]({{< relref "/docs/elasticsearch/examples/log-analysis" >}})
Implement a system to collect, store, and analyze application logs:
- Logback → direct Elasticsearch transmission
- Error log search and request tracing
- Error rate, response time analysis (aggregations)
- Log lifecycle management using ILM
