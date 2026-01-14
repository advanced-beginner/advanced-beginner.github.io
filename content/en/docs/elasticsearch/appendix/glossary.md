---
title: Glossary
weight: 1
lastmod: 2026-01-10
---

{{% notice style="tip" title="TL;DR" %}}
- **Index/Document/Field**: Corresponds to Table/Row/Column in RDB
- **Shard/Replica**: Basic units of data distribution and replication
- **Analyzer/Tokenizer**: Breaks text into searchable tokens
- **Query/Filter Context**: Search methods distinguished by scoring calculation
- Sorted alphabetically, each term links to related concept documents
{{% /notice %}}

Quick reference for Elasticsearch core terms. For detailed explanations, see the [Concepts](../concepts/) section.

## A-E

### Aggregation
Feature for grouping search results and calculating statistics. Similar to SQL's `GROUP BY`. Three types: Bucket/Metric/Pipeline.
→ [Aggregations](../concepts/aggregations/) | [Query DSL](../concepts/query-dsl/)

### Alias
An alternative name for an [Index](#index). Useful for zero-downtime index switching and multi-index search. Used with [ILM](#ilm-index-lifecycle-management).
→ [Indexing Strategy](../concepts/indexing/)

### Analyzer
Component that breaks text into [Terms](#term). Processes in order: Character Filter → [Tokenizer](#tokenizer) → Token Filter. Use [Nori](#nori) analyzer for Korean.
→ [Data Modeling](../concepts/data-modeling/)

### BM25 (Best Matching 25)
Elasticsearch's default [Score](#score) calculation algorithm. Based on [TF](#tf-term-frequency) and [IDF](#idf-inverse-document-frequency). Can be adjusted with [Boosting](#boosting).
→ [Search Relevance](../concepts/search-relevance/)

### Boosting
Technique of adding weight to the [Score](#score) of specific fields or conditions.
→ [Search Relevance](../concepts/search-relevance/)

### Bulk API
API for indexing multiple [Documents](#document) at once. Essential for performance. Use with [Refresh](#refresh) control.
→ [Indexing Strategy](../concepts/indexing/)

### Cluster
A group of Elasticsearch servers consisting of one or more [Nodes](#node). State managed by [Master Node](#master-node).
→ [Core Components](../concepts/core-components/) | [Cluster Management](../concepts/cluster-management/)

### Coordinating Node
Node that receives search requests, distributes to [Data Nodes](#data-node), and merges results. All nodes perform this role by default.
→ [Cluster Management](../concepts/cluster-management/)

### Data Node
Node that stores actual data and performs search/[Aggregation](#aggregation). [Shards](#shard) are assigned to it.
→ [Cluster Management](../concepts/cluster-management/)

### Document
JSON data unit stored in Elasticsearch. Similar to a Row in RDB. Stored within an [Index](#index).
→ [Core Components](../concepts/core-components/)

### DSL (Domain Specific Language)
JSON-based language for writing Elasticsearch [Queries](#query-context). Provides various queries like Bool, Match, Term.
→ [Query DSL](../concepts/query-dsl/)

---

## F-M

### Field
Individual data item within a [Document](#document). Similar to a Column in RDB. Type defined by [Mapping](#mapping).
→ [Data Modeling](../concepts/data-modeling/)

### Filter Context
Performs condition matching without [Score](#score) calculation. Cached for excellent performance. Used with [Query Context](#query-context) in Bool queries.
→ [Query DSL](../concepts/query-dsl/)

### Flush
Operation to permanently store memory buffer data to disk. Clears [Translog](#translog). Distinct from [Refresh](#refresh).
→ [Indexing Strategy](../concepts/indexing/)

### IDF (Inverse Document Frequency)
Indicator of how rare a word is across all [Documents](#document). Component of [BM25](#bm25-best-matching-25).
→ [Search Relevance](../concepts/search-relevance/)

### ILM (Index Lifecycle Management)
Automatic management of [Index](#index) lifecycle from creation to deletion. Hot → Warm → Cold → Delete phases.
→ [Indexing Strategy](../concepts/indexing/)

### Index
Collection of [Documents](#document). Similar to a Table in RDB. Distributed storage via [Shards](#shard).
→ [Core Components](../concepts/core-components/)

### Inverted Index
Data structure mapping [Terms](#term) → [Document](#document) locations. Core of fast search.
→ [Core Components](../concepts/core-components/)

### kNN (k-Nearest Neighbors)
Vector similarity-based search. Algorithm that finds the k closest documents in [Vector Search](#vector-search).
→ [Vector Search](../concepts/vector-search/)

### Mapping
Defines how [Documents](#document) and [Fields](#field) are stored/indexed. Similar to Schema in RDB. Dynamic/Explicit methods.
→ [Data Modeling](../concepts/data-modeling/)

### Master Node
Node that manages [Cluster](#cluster) state and handles [Index](#index) creation/deletion. Recommended to separate from [Data Node](#data-node).
→ [Cluster Management](../concepts/cluster-management/)

---

## N-R

### Node
Single Elasticsearch server that forms a [Cluster](#cluster). Roles include [Master](#master-node), [Data](#data-node), [Coordinating](#coordinating-node).
→ [Core Components](../concepts/core-components/) | [Cluster Management](../concepts/cluster-management/)

### Nori
Official Elasticsearch Korean morphological [Analyzer](#analyzer). Provides `nori_tokenizer`, `nori_part_of_speech` filter. Used for autocomplete, initial consonant search.
→ [Korean Search Optimization](../concepts/korean-search/)

### Primary Shard
[Shard](#shard) where original data is stored. Count cannot be changed after creation. Source of [Replica Shard](#replica-shard).
→ [Core Components](../concepts/core-components/)

### Query Context
Calculates relevance [Score](#score) between search term and [Document](#document). Used with [Filter Context](#filter-context) in Bool queries.
→ [Query DSL](../concepts/query-dsl/)

### Refresh
Operation to make memory buffer data searchable. Default 1 second. Recommended to adjust when using [Bulk API](#bulk-api). Distinct from [Flush](#flush).
→ [Indexing Strategy](../concepts/indexing/) | [Performance Tuning](../concepts/performance-tuning/)

### Reindex
Copy/transform existing [Index](#index) to new index. Used for [Mapping](#mapping) changes, data migration.
→ [Indexing Strategy](../concepts/indexing/)

### Replica Shard
Copy of [Primary Shard](#primary-shard). Improves read performance and failover. Placed on different [Nodes](#node) in [Cluster](#cluster).
→ [High Availability](../concepts/high-availability/)

---

## S-Z

### Score
Number indicating relevance between search term and [Document](#document). Calculated by [BM25](#bm25-best-matching-25) algorithm. Adjustable with [Boosting](#boosting).
→ [Search Relevance](../concepts/search-relevance/)

### Segment
Immutable file piece that composes an [Index](#index). Created during [Refresh](#refresh). Consolidated by Merge.
→ [Performance Tuning](../concepts/performance-tuning/)

### Shard
Horizontal partition of an [Index](#index). Unit of distributed storage and parallel processing. Divided into [Primary](#primary-shard) and [Replica](#replica-shard).
→ [Core Components](../concepts/core-components/)

### Snapshot
Backup of [Index](#index) state at a specific point. Stored in remote storage (S3, GCS, etc.). Automated with [SLM](#slm-snapshot-lifecycle-management).
→ [High Availability](../concepts/high-availability/)

### TF (Term Frequency)
Frequency of [Term](#term) appearing in a [Document](#document). Component of [BM25](#bm25-best-matching-25).
→ [Search Relevance](../concepts/search-relevance/)

### Term
Individual token generated after [Analyzer](#analyzer) processing. Stored in [Inverted Index](#inverted-index).
→ [Data Modeling](../concepts/data-modeling/)

### Tokenizer
Component of [Analyzer](#analyzer) that breaks text into tokens. Standard, Whitespace, [Nori](#nori), etc.
→ [Data Modeling](../concepts/data-modeling/)

### Translog
Write-Ahead Log for preventing data loss. Used for recovery until [Flush](#flush).
→ [High Availability](../concepts/high-availability/)

### Vector Search
Semantic search using embedding vectors. Uses [kNN](#knn-k-nearest-neighbors) algorithm. Used for semantic search, similar product recommendations.
→ [Vector Search](../concepts/vector-search/)

---

## Abbreviations

| Abbr | Full Name | Meaning | Reference |
|------|-----------|---------|-----------|
| BM25 | Best Matching 25 | Default scoring algorithm | [Search Relevance](../concepts/search-relevance/) |
| CCR | Cross-Cluster Replication | Real-time cross-cluster replication | [High Availability](../concepts/high-availability/) |
| DSL | Domain Specific Language | Query language | [Query DSL](../concepts/query-dsl/) |
| IDF | Inverse Document Frequency | Word rarity indicator | [Search Relevance](../concepts/search-relevance/) |
| ILM | Index Lifecycle Management | Index lifecycle management | [Indexing Strategy](../concepts/indexing/) |
| kNN | k-Nearest Neighbors | k-nearest neighbor search | [Vector Search](../concepts/vector-search/) |
| SLM | Snapshot Lifecycle Management | Snapshot lifecycle management | [High Availability](../concepts/high-availability/) |
| TF | Term Frequency | Word frequency indicator | [Search Relevance](../concepts/search-relevance/) |

---

## Next Steps

- [Concepts](../concepts/) - Elasticsearch core concepts
- [Quick Start](../quick-start/) - Quick start guide
- [References](references/) - Official docs, blogs
- [FAQ](faq/) - Frequently asked questions
