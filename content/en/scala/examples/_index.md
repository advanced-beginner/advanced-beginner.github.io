---
lastmod: "2026-01-06"
title: Hands-on Examples
weight: 3
---

Example projects to practice concepts learned in theory.

## Example Projects

| Example | Description | Difficulty |
|---------|-------------|------------|
| [Environment Setup](setup/) | Detailed sbt and IDE setup guide | Beginner |
| [Basic Examples](basic/) | Comprehensive examples using core concepts | Elementary |
| [Scala 2 vs 3 Comparison](scala2-vs-scala3/) | Code comparison by version | Intermediate |

## Example Project Structure

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

## How to Run Examples

### 1. Clone Project

```bash
git clone https://github.com/kimbenji/advanced-beginner.git
cd advanced-beginner/examples/scala
```

### 2. Run Scala 3 Examples

```bash
cd scala3-basics
sbt run
```

### 3. Run Scala 2 Examples

```bash
cd scala2-basics
sbt run
```

## Learning Points by Example

### Environment Setup
- sbt project configuration
- IDE setup (IntelliJ, VS Code)
- Commonly used sbt commands

### Basic Examples
- Data modeling with case classes
- Pattern matching usage
- Collection operations (map, filter, fold)
- Writing higher-order functions

### Scala 2 vs 3 Comparison
- Syntax differences (braces vs indentation)
- implicit → given/using migration
- New enum syntax
- Extension Methods

## Hands-on Practice

Modify and run the example code yourself:

1. Edit files under `src/main/scala/`
2. Run with `sbt run` or `sbt ~run` (auto-reload)
3. Check the results

### Recommended Exercises

**Beginner:**
- Filter even numbers from a list and output their squares
- Define `Person(name, age)` case class and sort by age

**Intermediate:**
- Implement safe division function using Option
- Generate combinations of two lists with For Comprehension

**Advanced:**
- Implement JSON serialization with type classes
- Process asynchronous data with Future
