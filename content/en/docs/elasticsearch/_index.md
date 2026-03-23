---
title: Elasticsearch
bookCollapseSection: true
description: Practical guide to distributed search engine Elasticsearch - Search, Aggregations, Data Modeling, and Performance Tuning
weight: 5
lastmod: 2026-01-08
---

{{< callout type="info" title="Version Information" >}}
This guide is written based on the following versions:
- **Elasticsearch**: 8.11.x
- **Kibana**: 8.11.x
- **Spring Boot**: 3.2.x
- **Spring Data Elasticsearch**: 5.2.x
- **Java**: 17+

Some APIs or configurations may differ in other versions. In particular, Elasticsearch 7.x and 8.x have significant differences in security settings and client APIs.
{{< /callout >}}

## What is Elasticsearch?

Elasticsearch is a **distributed search and analytics engine**. It's a tool that enables fast searching across large volumes of data and real-time analysis.

### Why is Elasticsearch Needed?

What problems arise when searching with `LIKE '%keyword%'` in an RDB?

| RDB Search Limitations | After Elasticsearch Adoption |
|------------------------|------------------------------|
| Slow due to full table scan | Millisecond search via Inverted Index |
| No morphological analysis | Searching "Samsung Electronics" matches both "Samsung" and "Electronics" |
| No typo tolerance | Fuzzy search finds "Samsng" too |
| No relevance sorting | Accurate results ranked by relevance score |
| Single server limitations | Horizontal scaling via sharding, handles billions of documents |

Elasticsearch solves these problems while providing **real-time search**, **complex aggregations**, and **high availability**.

### When Should You Use Elasticsearch?

**Suitable cases:**
- When **full-text search** for products, posts, etc. is needed
- When **time-series data analysis** for logs, metrics is needed
- When **real-time aggregations** for dashboards are needed
- When **autocomplete**, typo correction, synonym handling is needed
- When RDB search becomes slow due to large data volume

**May be overkill:**
- When only simple CRUD is needed (RDB is sufficient)
- When transaction integrity is critical (Elasticsearch is Eventually Consistent)
- When data volume is small and search requirements are simple
- When there's no capacity for operational infrastructure

### Elasticsearch Limitations and Considerations

**Realistic drawbacks** you should know before adopting Elasticsearch:

| Limitation | Description | Mitigation |
|------------|-------------|------------|
| **Operational Complexity** | Cluster management, shard rebalancing, JVM tuning required | Dedicated personnel or managed services |
| **Cost** | Memory-intensive, minimum 4GB+ per node recommended | Proper capacity planning for data scale |
| **Data Consistency** | Eventually Consistent, not real-time (default 1-second refresh) | Adjust refresh settings if real-time is critical |
| **Schema Changes** | Cannot change existing field types, requires reindexing | Initial Mapping design is crucial |
| **No JOIN Support** | Cannot JOIN between tables, denormalization required | Data model redesign, Application-side JOIN |
| **No Transaction Support** | No ACID transactions | Keep RDB as primary, ES for search only |
| **Learning Curve** | Query DSL, Mapping, Analyzers require learning | Consider team capabilities |

> **Practical Advice:** The safest pattern is using Elasticsearch as a "search-only secondary store" while maintaining RDB as the main store. Core service functionality remains even if ES fails.

### Alternative Technology Comparison

Elasticsearch is not the only option:

| Technology | Characteristics | Best For |
|------------|-----------------|----------|
| **Elasticsearch** | Full-stack search/analytics, most features | Large-scale search, log analysis, complex aggregations |
| **OpenSearch** | Fork of ES 7.10, AWS managed available | AWS environments, licensing concerns |
| **Apache Solr** | Long history, proven stability | Traditional enterprise environments |
| **Meilisearch** | Simple setup, quick start | Small scale, prototypes, instant search |
| **Typesense** | Easy configuration, built-in typo tolerance | Small services, rapid implementation |
| **PostgreSQL FTS** | No separate system needed | Simple search, already using PG |

> **Selection Criteria:** If data volume < 1 million documents and search requirements are simple, PostgreSQL FTS or Meilisearch may suffice. Elasticsearch is suitable for large-scale, complex requirements.

### RDB vs Elasticsearch

| Concept | RDB | Elasticsearch |
|---------|-----|---------------|
| Storage Unit | Row | Document (JSON) |
| Schema | Table Schema | Mapping |
| Table | Table | Index |
| Column | Column | Field |
| Index | B-Tree Index | Inverted Index |
| Join | JOIN | Nested, Parent-Child (limited) |
| Transaction | ACID | Eventually Consistent |

> **Key Difference:** RDB is optimized for accurate retrieval of normalized data, while Elasticsearch is optimized for fast search of denormalized data.

## What This Guide Covers

### [Quick Start](quick-start/)
Store and search data in Elasticsearch in 5 minutes. See it working before diving into concepts.

### [Concepts](concepts/)
Not just "use it this way", but explaining **why it works this way**.

| Topic | What You'll Learn |
|-------|-------------------|
| [Core Components](concepts/core-components/) | Roles and relationships of Cluster, Node, Index, Document, Shard |
| [Data Modeling](concepts/data-modeling/) | Mapping, Field Type, Analyzer design |
| [Query DSL](concepts/query-dsl/) | Writing search queries with Match, Term, Bool |
| [Search Relevance](concepts/search-relevance/) | Improving search quality with Score, BM25, Boosting |
| [Aggregations](concepts/aggregations/) | Data analysis with Bucket and Metric aggregations |
| [Indexing Strategy](concepts/indexing/) | Bulk indexing, Refresh, ILM settings |
| [Cluster Management](concepts/cluster-management/) | Node configuration, shard allocation, status monitoring |
| [Performance Tuning](concepts/performance-tuning/) | Query optimization, caching, JVM settings |
| [High Availability](concepts/high-availability/) | Replica, Snapshot, failure response |

### [Hands-on Examples](examples/)
Executable example code based on Spring Boot.

- [Environment Setup](examples/setup/) - Docker Elasticsearch + Kibana configuration
- [Basic Examples](examples/basic/) - Document CRUD and basic search implementation
- [Product Search System](examples/product-search/) - Korean search, autocomplete, filtering implementation

### [Appendix](appendix/)
- [Glossary](appendix/glossary/) - Quick reference for Elasticsearch terms
- [FAQ](appendix/faq/) - Frequently asked questions
- [References](appendix/references/) - Official docs and additional learning resources

## Prerequisites

- **Required**: REST API basics, JSON format understanding
- **Helpful**: Java/Spring Boot experience, RDB usage experience

## Suggested Learning Path

```
If you're new:      Quick Start → Core Components → Data Modeling → Basic Examples
Search implementation: Query DSL → Search Relevance → Product Search System
Data analysis:      Aggregations → Indexing Strategy
Operations prep:    Cluster Management → Performance Tuning → High Availability
```

Each document can be read independently, but if you're new, we recommend the order above.
