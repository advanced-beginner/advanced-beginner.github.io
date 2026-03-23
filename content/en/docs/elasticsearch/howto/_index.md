---
lastmod: "2026-01-16"
title: How-To Guides
description: "Elasticsearch operational troubleshooting guides"
weight: 4
bookCollapseSection: true
---

Step-by-step instructions for solving common Elasticsearch problems in production environments.

{{< callout type="info" title="How to Use These Guides" >}}
Each guide is designed for immediate application in specific problem situations. Check the prerequisites in the "Before You Begin" section, then follow the steps.
{{< /callout >}}

## Troubleshooting

| Guide | Description | Level | Duration |
|-------|-------------|-------|----------|
| [Slow Query Optimization](slow-query-optimization/) | Diagnose and improve slow search response times | Intermediate | 15-30 min |
| [Memory Troubleshooting](memory-troubleshooting/) | Handle OOM and GC issues | Intermediate | 20-40 min |

## Operations

| Guide | Description | Level | Duration |
|-------|-------------|-------|----------|
| [Mapping Migration](mapping-migration/) | Change index mappings without downtime using Reindex and Alias switching | Intermediate | 20-40 min |
| [Index Rebuild](index-rebuild/) | Efficiently rebuild large indices using _reindex, Snapshot/Restore, or Logstash | Intermediate | 30-60 min |
| [Cluster Scaling](cluster-scaling/) | Safely scale an Elasticsearch cluster with node addition and Hot-Warm-Cold architecture | Advanced | 30-60 min |
