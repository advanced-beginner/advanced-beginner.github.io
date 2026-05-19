---
bookCollapseSection: true
lastmod: "2026-05-13"
title: Hands-on Examples
description: "Learning guide and document list for Kotlin hands-on examples."
weight: 3
---

A collection of examples that let you run the Kotlin concepts you learned in theory. The progression is designed for step-by-step learning, from environment setup to Spring Boot and Kafka integration, practical coroutine usage, and Kotlin Multiplatform.

#### Example List

The table below lists the available examples. We recommend going through them in order: environment setup first, then basic examples, Spring Boot integration, Kafka integration, practical coroutine usage, and finally the Multiplatform mini project.

| Example | Description | Difficulty |
|---------|-------------|------------|
| [Environment Setup](setup/) | JDK, Gradle Kotlin DSL, IDE configuration | Beginner |
| [Basic Examples](basic/) | Hello Kotlin, applying core concepts | Beginner |
| [Spring Boot Integration](spring-boot-integration/) | Kotlin + Spring Boot REST API | Intermediate |
| [Kafka Integration](kafka-integration/) | Producer/Consumer in Kotlin | Intermediate |
| [Practical Coroutines](coroutines-practical/) | Real-world scenarios using suspend and Flow | Intermediate |
| [Multiplatform Intro](multiplatform-intro/) | KMP mini project | Advanced |

#### Example Project Structure

The examples follow the standard Gradle Kotlin DSL project structure. Below is the typical directory layout.

```text
kotlin-example/
├── build.gradle.kts           # Build script (Kotlin DSL)
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
└── src/
    ├── main/
    │   ├── kotlin/            # Kotlin sources
    │   └── resources/         # Resources (application.yml, etc.)
    └── test/
        ├── kotlin/            # Test code
        └── resources/
```

Project configuration and dependencies are defined in `build.gradle.kts`, and source code lives under `src/main/kotlin/`. Test code goes in `src/test/kotlin/`.

#### How to Run the Examples

To run an example, first clone the repository, move into the example directory you want, and run the Gradle command.

**Clone the project**

```bash
git clone https://github.com/advanced-beginner/advanced-beginner.github.io.git
cd advanced-beginner.github.io
```

**Run with Gradle Wrapper**

Most examples run with a single `./gradlew` command. The first run may take time due to dependency downloads.

```bash
./gradlew run
```

**Spring Boot examples**

Spring Boot examples are launched with the `bootRun` task.

```bash
./gradlew bootRun
```

**Kafka examples**

For Kafka examples, you must start a Kafka broker first. The Docker Compose configuration is in the `docker/` directory at the project root.

```bash
cd docker && docker-compose up -d
cd ..
./gradlew bootRun
```

#### Key Learning Points per Example

Here are the core concepts you can learn from each example.

<strong>Environment Setup</strong> covers the foundations of Kotlin development. You'll learn JDK installation, writing Gradle Kotlin DSL, and IntelliJ IDEA configuration.

<strong>Basic Examples</strong> let you practice Kotlin's core features — modeling data with data classes, writing expressive code using extension functions, and applying collection operations such as map, filter, and reduce.

<strong>Spring Boot Integration</strong> walks through using Kotlin together with Spring Boot. It covers Kotlin-friendly dependencies, writing controllers and services, and pitfalls when designing JPA entities.

<strong>Kafka Integration</strong> shows how to write Kafka Producers and Consumers in Kotlin, using `KafkaTemplate` and `@KafkaListener` for declarative message handling.

<strong>Practical Coroutines</strong> applies `suspend` functions and Flow to real-world scenarios — parallel external API calls, stream processing with backpressure, and structured concurrency patterns.

<strong>Multiplatform Intro</strong> introduces the basic structure of a Kotlin Multiplatform project and the `expect`/`actual` mechanism.

#### Hands-on Practice

Don't just run the example code — modify and experiment with it.

1. Edit files under `src/main/kotlin/`
2. Run `./gradlew run` or `./gradlew run --continuous` (auto re-run)
3. Verify the result

With the `--continuous` flag, Gradle recompiles and re-runs automatically whenever files change.

**Suggested Exercises**

Try implementing the following exercises to sharpen your Kotlin skills.

For beginner exercises, write a program that filters even numbers from a list and prints their squares. Also try defining a `Person(name, age)` data class and sorting by age.

For intermediate exercises, implement a safe division function using `Result<T>`. Build a Spring Boot controller endpoint that uses coroutines to call two external APIs in parallel and combines their responses.

For advanced exercises, experiment with Kafka message stream processing using Flow and apply backpressure. Build a common module with Kotlin Multiplatform that runs on both JVM and JS.
