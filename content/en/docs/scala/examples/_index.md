---
bookCollapseSection: true
lastmod: "2026-01-14"
title: Practical Examples
weight: 3
---

These are example projects where you can directly execute concepts learned in theory. Writing and running Scala's core concepts in actual code will deepen your understanding of the language. Each example is organized by difficulty level and designed to enable step-by-step learning from beginners to advanced users.

#### Example projects

The following table lists the example projects provided. We recommend progressing sequentially from environment setup to basic examples, version comparison, Spark integration, and practical projects. Each example builds on concepts learned in previous examples, so learning in order is more effective.

| Example | Description | Difficulty |
|------|------|--------|
| [Environment Setup](setup/) | Detailed guide for sbt, IDE setup | Beginner |
| [Basic Examples](basic/) | Comprehensive examples applying core concepts | Beginner |
| [Scala 2 vs 3 Comparison](scala2-vs-scala3/) | Code comparison by version | Intermediate |
| [Spark Integration](spark-integration/) | Using Apache Spark with Scala | Intermediate |
| [Practical Project](practical-project/) | REST API, data pipelines | Advanced |

#### Example project structure

Example projects are separated into Scala 2 and Scala 3 versions. Each version is organized as an independent sbt project, so you can selectively run examples from your desired version. Below is the complete directory structure.

```
examples/scala/
├── scala2-basics/          # Scala 2.13 examples
│   ├── build.sbt
│   ├── project/
│   │   └── build.properties
│   └── src/main/scala/
│       └── ...
└── scala3-basics/          # Scala 3 examples
    ├── build.sbt
    ├── project/
    │   └── build.properties
    └── src/main/scala/
        └── ...
```

Both projects follow the standard sbt directory structure. `build.sbt` defines project settings and dependencies, `project/build.properties` specifies the sbt version to use, and source code is located in the `src/main/scala/` directory.

#### How to run examples

To run examples, first clone the repository, navigate to the desired example directory, and then execute sbt commands. Each step is explained in detail below.

**Clone project**

First, clone the project from GitHub. If you've already cloned it, you can skip this step.

```bash
git clone https://github.com/advanced-beginner/advanced-beginner.github.io.git
cd advanced-beginner/examples/scala
```

**Run Scala 3 examples**

To run Scala 3 examples, navigate to the `scala3-basics` directory and run the sbt run command. The first run may take time due to dependency downloads.

```bash
cd scala3-basics
sbt run
```

**Run Scala 2 examples**

To run Scala 2.13 examples, similarly run sbt run in the `scala2-basics` directory. Scala 2 version is required for projects using Spark.

```bash
cd scala2-basics
sbt run
```

#### Learning points by example

We've summarized the core concepts you can learn from each example. Understanding what each example covers before running it will enhance your learning effectiveness.

**Environment Setup** covers the foundational environment configuration for Scala development. You'll learn how to configure sbt projects, set up IDEs like IntelliJ IDEA or VS Code, and commonly used sbt commands in daily work.

**Basic Examples** practice Scala's core features. You'll learn data modeling with case classes, branching with pattern matching, collection operations like map, filter, fold, and how to write higher-order functions.

**Scala 2 vs 3 Comparison** directly compares code differences between the two versions. You'll cover braces vs indentation syntax, migration from implicit to given/using, new enum syntax, Extension Methods, etc.

#### Hands-on practice

It's important not just to run example code, but to modify and experiment with it yourself. Follow these steps to modify code and check the results:

1. Modify files under `src/main/scala/`
2. Run with `sbt run` or `sbt ~run` (auto re-run)
3. Check results

Using the `sbt ~run` command automatically recompiles and runs whenever files change. This is convenient for practice as you can immediately see results while modifying code.

**Recommended practice assignments**

Try implementing these assignments to improve your Scala skills.

For beginner assignments, write a program that filters only even numbers from a list and prints the squared results. Also define a `Person(name, age)` case class and implement code to sort by age.

For intermediate assignments, implement a safe division function using Option. Return None when dividing by zero. Also write code to generate all combinations of two lists using For Comprehension.

For advanced assignments, implement JSON serialization using type classes. Create extensible encoders for various types. Also write asynchronous data processing code using Future.
