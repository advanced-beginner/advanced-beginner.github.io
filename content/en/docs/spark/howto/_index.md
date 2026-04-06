---
bookCollapseSection: true
title: How-To Guides
description: "Spark operational troubleshooting guides"
weight: 4
lastmod: "2026-04-06"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Step-by-step guides for solving specific problems. Each document explains how to achieve a concrete goal.

## Guide List

**[Troubleshooting OutOfMemoryError](oom-troubleshooting/)**

Diagnose and resolve the most common memory shortage errors in Spark.

- Distinguishing Driver OOM vs Executor OOM
- Optimizing memory settings
- Adjusting partition sizes

**[Resolving Data Skew](data-skew/)**

Fix performance degradation caused by data concentration in specific partitions.

- How to diagnose skew
- Salting techniques
- Enabling AQE skew join

**[Optimizing Shuffle](shuffle-optimization/)**

Improve Spark job performance by reducing network I/O.

- Eliminating unnecessary shuffles
- Leveraging broadcast joins
- Optimizing partition count

**[Reading the Spark UI](spark-ui-guide/)**

Identify performance bottlenecks and diagnose root causes from each tab of the Spark UI.

- Trace bottlenecks in Jobs → Stages → Tasks order
- Diagnose data skew, GC issues, excessive shuffle
- UI access methods by environment (local/YARN/K8s)

## How to Use These Guides

Each guide is structured as follows:

1. **Problem Definition**: When you need this guide
2. **Prerequisites**: What you need before starting
3. **Step-by-Step Solution**: Including commands and code
4. **Verification**: How to confirm the problem is resolved

If you get stuck during troubleshooting, refer to the [FAQ](../appendix/faq/).
