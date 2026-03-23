---
title: Glossary
description: "Key Elasticsearch terminology and definitions"
weight: 1
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **Index/Document/Field**: Corresponds to Table/Row/Column in RDB
- **Shard/Replica**: Basic units of data distribution and replication
- **Analyzer/Tokenizer**: Breaks text into searchable tokens
- **Query/Filter Context**: Search methods distinguished by scoring calculation
- Sorted alphabetically, each term links to related concept documents
{{< /callout >}}

Quick reference for Elasticsearch core terms. For detailed explanations, see the [Concepts]({{< relref "/docs/elasticsearch/concepts" >}}) section.

## A-E

### Aggregation
Feature for grouping search results and calculating statistics. Similar to SQL's `GROUP BY`. Three types: Bucket/Metric/Pipeline.
→ [Aggregations]({{< relref "/docs/elasticsearch/concepts/aggregations" >}}) | [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}})

### Alias
An alternative name for an [Index](#index). Useful for zero-downtime index switching and multi-index search. Used with [ILM](#ilm-index-lifecycle-management).
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}})

### Analyzer
Component that breaks text into [Terms](#term). Processes in order: Character Filter → [Tokenizer](#tokenizer) → Token Filter. Use [Nori](#nori) analyzer for Korean.
→ [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}})

### BM25 (Best Matching 25)
Elasticsearch's default [Score](#score) calculation algorithm. Based on [TF](#tf-term-frequency) and [IDF](#idf-inverse-document-frequency). Can be adjusted with [Boosting](#boosting).
→ [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}})

### Boosting
Technique of adding weight to the [Score](#score) of specific fields or conditions.
→ [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}})

### Bulk API
API for indexing multiple [Documents](#document) at once. Essential for performance. Use with [Refresh](#refresh) control.
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}})

### Cluster
A group of Elasticsearch servers consisting of one or more [Nodes](#node). State managed by [Master Node](#master-node).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}}) | [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})

### Coordinating Node
Node that receives search requests, distributes to [Data Nodes](#data-node), and merges results. All nodes perform this role by default.
→ [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})

### Data Node
Node that stores actual data and performs search/[Aggregation](#aggregation). [Shards](#shard) are assigned to it.
→ [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})

### Document
JSON data unit stored in Elasticsearch. Similar to a Row in RDB. Stored within an [Index](#index).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}})

### DSL (Domain Specific Language)
JSON-based language for writing Elasticsearch [Queries](#query-context). Provides various queries like Bool, Match, Term.
→ [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}})

---

## F-M

### Field
Individual data item within a [Document](#document). Similar to a Column in RDB. Type defined by [Mapping](#mapping).
→ [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}})

### Filter Context
Performs condition matching without [Score](#score) calculation. Cached for excellent performance. Used with [Query Context](#query-context) in Bool queries.
→ [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}})

### Flush
Operation to permanently store memory buffer data to disk. Clears [Translog](#translog). Distinct from [Refresh](#refresh).
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}})

### IDF (Inverse Document Frequency)
Indicator of how rare a word is across all [Documents](#document). Component of [BM25](#bm25-best-matching-25).
→ [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}})

### ILM (Index Lifecycle Management)
Automatic management of [Index](#index) lifecycle from creation to deletion. Hot → Warm → Cold → Delete phases.
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}})

### Index
Collection of [Documents](#document). Similar to a Table in RDB. Distributed storage via [Shards](#shard).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}})

### Inverted Index
Data structure mapping [Terms](#term) → [Document](#document) locations. Core of fast search.
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}})

### kNN (k-Nearest Neighbors)
Vector similarity-based search. Algorithm that finds the k closest documents in [Vector Search](#vector-search).
→ [Vector Search]({{< relref "/docs/elasticsearch/concepts/vector-search" >}})

### Mapping
Defines how [Documents](#document) and [Fields](#field) are stored/indexed. Similar to Schema in RDB. Dynamic/Explicit methods.
→ [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}})

### Master Node
Node that manages [Cluster](#cluster) state and handles [Index](#index) creation/deletion. Recommended to separate from [Data Node](#data-node).
→ [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})

---

## N-R

### Node
Single Elasticsearch server that forms a [Cluster](#cluster). Roles include [Master](#master-node), [Data](#data-node), [Coordinating](#coordinating-node).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}}) | [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})

### Nori
Official Elasticsearch Korean morphological [Analyzer](#analyzer). Provides `nori_tokenizer`, `nori_part_of_speech` filter. Used for autocomplete, initial consonant search.
→ [Korean Search Optimization]({{< relref "/docs/elasticsearch/concepts/korean-search" >}})

### Primary Shard
[Shard](#shard) where original data is stored. Count cannot be changed after creation. Source of [Replica Shard](#replica-shard).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}})

### Query Context
Calculates relevance [Score](#score) between search term and [Document](#document). Used with [Filter Context](#filter-context) in Bool queries.
→ [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}})

### Refresh
Operation to make memory buffer data searchable. Default 1 second. Recommended to adjust when using [Bulk API](#bulk-api). Distinct from [Flush](#flush).
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}}) | [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}})

### Reindex
Copy/transform existing [Index](#index) to new index. Used for [Mapping](#mapping) changes, data migration.
→ [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}})

### Replica Shard
Copy of [Primary Shard](#primary-shard). Improves read performance and failover. Placed on different [Nodes](#node) in [Cluster](#cluster).
→ [High Availability]({{< relref "/docs/elasticsearch/concepts/high-availability" >}})

---

## S-Z

### Score
Number indicating relevance between search term and [Document](#document). Calculated by [BM25](#bm25-best-matching-25) algorithm. Adjustable with [Boosting](#boosting).
→ [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}})

### Segment
Immutable file piece that composes an [Index](#index). Created during [Refresh](#refresh). Consolidated by Merge.
→ [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}})

### Shard
Horizontal partition of an [Index](#index). Unit of distributed storage and parallel processing. Divided into [Primary](#primary-shard) and [Replica](#replica-shard).
→ [Core Components]({{< relref "/docs/elasticsearch/concepts/core-components" >}})

### Snapshot
Backup of [Index](#index) state at a specific point. Stored in remote storage (S3, GCS, etc.). Automated with [SLM](#slm-snapshot-lifecycle-management).
→ [High Availability]({{< relref "/docs/elasticsearch/concepts/high-availability" >}})

### TF (Term Frequency)
Frequency of [Term](#term) appearing in a [Document](#document). Component of [BM25](#bm25-best-matching-25).
→ [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}})

### Term
Individual token generated after [Analyzer](#analyzer) processing. Stored in [Inverted Index](#inverted-index).
→ [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}})

### Tokenizer
Component of [Analyzer](#analyzer) that breaks text into tokens. Standard, Whitespace, [Nori](#nori), etc.
→ [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}})

### Translog
Write-Ahead Log for preventing data loss. Used for recovery until [Flush](#flush).
→ [High Availability]({{< relref "/docs/elasticsearch/concepts/high-availability" >}})

### Vector Search
Semantic search using embedding vectors. Uses [kNN](#knn-k-nearest-neighbors) algorithm. Used for semantic search, similar product recommendations.
→ [Vector Search]({{< relref "/docs/elasticsearch/concepts/vector-search" >}})

---

## Abbreviations

| Abbr | Full Name | Meaning | Reference |
|------|-----------|---------|-----------|
| BM25 | Best Matching 25 | Default scoring algorithm | [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}}) |
| CCR | Cross-Cluster Replication | Real-time cross-cluster replication | [High Availability]({{< relref "/docs/elasticsearch/concepts/high-availability" >}}) |
| DSL | Domain Specific Language | Query language | [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) |
| IDF | Inverse Document Frequency | Word rarity indicator | [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}}) |
| ILM | Index Lifecycle Management | Index lifecycle management | [Indexing Strategy]({{< relref "/docs/elasticsearch/concepts/indexing" >}}) |
| kNN | k-Nearest Neighbors | k-nearest neighbor search | [Vector Search]({{< relref "/docs/elasticsearch/concepts/vector-search" >}}) |
| SLM | Snapshot Lifecycle Management | Snapshot lifecycle management | [High Availability]({{< relref "/docs/elasticsearch/concepts/high-availability" >}}) |
| TF | Term Frequency | Word frequency indicator | [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}}) |

---

## Next Steps

- [Concepts]({{< relref "/docs/elasticsearch/concepts" >}}) - Elasticsearch core concepts
- [Quick Start]({{< relref "/docs/elasticsearch/quick-start" >}}) - Quick start guide
- [References]({{< relref "/docs/elasticsearch/appendix/references" >}}) - Official docs, blogs
- [FAQ]({{< relref "/docs/elasticsearch/appendix/faq" >}}) - Frequently asked questions
