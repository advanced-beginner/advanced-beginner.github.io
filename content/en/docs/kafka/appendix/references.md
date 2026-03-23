---
lastmod: "2026-01-10"
title: References
description: "Official Kafka documentation and reference materials"
weight: 2
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

This section compiles additional resources for learning Kafka. From official documentation to books, online courses, communities, and tools, we've gathered resources for systematic learning.

{{< callout type="tip" title="TL;DR" >}}
- **Official Documentation**: Apache Kafka, Spring Kafka, Confluent docs are essential
- **Beginner Books**: "Kafka: The Definitive Guide", "Kafka in Action" recommended
- **Online Courses**: Confluent Developer (free), Udemy, Inflearn
- **Communities**: Stack Overflow, Confluent Forum, OKKY
- **Tools**: Kafka UI (management), Prometheus+Grafana (monitoring)
{{< /callout >}}

#### Official Documentation

**Apache Kafka**

Apache Kafka official documentation provides the most accurate and up-to-date information. Apache Kafka Documentation covers Kafka's complete architecture, configuration, and APIs. Kafka Quickstart is the official Quick Start guide for beginners, covering installation through basic message sending/receiving. Kafka APIs provides detailed references for Producer API, Consumer API, Streams API, Admin API, etc. Kafka Configuration includes a complete list and descriptions of all configuration options for Broker, Producer, Consumer, Topic, etc.

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka Quickstart](https://kafka.apache.org/quickstart)
- [Kafka APIs](https://kafka.apache.org/documentation/#api)
- [Kafka Configuration](https://kafka.apache.org/documentation/#configuration)

**Spring for Apache Kafka**

Spring Kafka is a project integrating Spring Framework with Kafka. Spring Kafka Reference is the official reference documentation covering KafkaTemplate, @KafkaListener, transactions, error handling in detail. Spring Kafka API Docs is JavaDoc for checking detailed APIs of classes and methods. Spring Boot Kafka Properties documents available spring.kafka.* configuration options in Spring Boot.

- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [Spring Kafka API Docs](https://docs.spring.io/spring-kafka/docs/current/api/)
- [Spring Boot Kafka Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.integration.spring.kafka.admin.auto-create)

**Confluent**

Confluent is a company providing commercial Kafka distributions and managed services, founded by Kafka's creators. Confluent Documentation is official documentation for Confluent Platform, also covering additional components like Schema Registry, Connect, ksqlDB. Kafka Tutorials provides step-by-step hands-on tutorials covering how to use Kafka in various languages and frameworks. Confluent Blog publishes in-depth technical articles and case studies related to Kafka.

- [Confluent Documentation](https://docs.confluent.io/)
- [Kafka Tutorials](https://developer.confluent.io/tutorials/)
- [Confluent Blog](https://www.confluent.io/blog/)

{{< callout type="info" title="Key Points" >}}
- **Apache Kafka**: Official documentation with most accurate and up-to-date information
- **Spring Kafka**: Essential reference for using Kafka in Spring environments
- **Confluent**: Extended ecosystem information including Schema Registry, Connect, ksqlDB
{{< /callout >}}

#### Recommended Books

**Beginner Level**

If you're just starting with Kafka, these books are recommended. "Kafka: The Definitive Guide" by Neha Narkhede, Gwen Shapira, and Todd Palino systematically explains Kafka's core concepts and architecture. Written by Kafka's creators and core developers, it's accurate and deep. "Kafka in Action" by Dylan Scott focuses on practical examples to learn Kafka usage. If you prefer learning through actual code, this book is suitable.

**Advanced Level**

For advanced learning after mastering the basics, these books are recommended. "Designing Event-Driven Systems" by Ben Stopford covers event-driven architecture design. Learn system design patterns using Kafka and microservices architecture. Confluent provides free PDF. "Kafka Streams in Action" by Bill Bejeck covers real-time stream processing using Kafka Streams in depth.

{{< callout type="info" title="Key Points" >}}
- **Beginner**: "Kafka: The Definitive Guide" (by creators, systematic), "Kafka in Action" (practical examples)
- **Advanced**: "Designing Event-Driven Systems" (free PDF), "Kafka Streams in Action"
{{< /callout >}}

#### Online Courses

For systematic learning, online courses are available. Confluent Developer's Apache Kafka 101 is a free official course covering basic concepts through hands-on practice. Conducted in English with subtitles. Udemy's Apache Kafka Series offers paid courses with Korean subtitles and various levels available. Inflearn offers several Kafka courses produced in Korean.

- [Confluent Developer](https://developer.confluent.io/)
- [Udemy](https://www.udemy.com/)
- [Inflearn](https://www.inflearn.com/)

{{< callout type="info" title="Key Points" >}}
- **Free**: Confluent Developer's Apache Kafka 101 (English, subtitles available)
- **Paid**: Udemy (Korean subtitles), Inflearn (Korean courses)
{{< /callout >}}

#### Communities

**Forums and Q&A**

Communities where you can ask questions or find solutions from others when problems occur. Stack Overflow's apache-kafka tag has technical Q&A related to Kafka. Search for solutions to similar problems or post new questions. Confluent Community Forum is an official forum operated by Confluent, covering questions about Kafka core and Confluent Platform. Reddit's r/apachekafka subreddit is a community for Kafka-related discussions and information sharing.

- [Stack Overflow - Kafka](https://stackoverflow.com/questions/tagged/apache-kafka)
- [Confluent Community](https://forum.confluent.io/)
- [Reddit r/apachekafka](https://www.reddit.com/r/apachekafka/)

**Korean Communities**

Korean communities where you can ask questions and receive answers in Korean. OKKY is a Korean developer community where you can post Kafka-related questions or share experiences. Search for "Kafka KR" in KakaoTalk open chat to find Kafka-related open chat rooms.

- [OKKY](https://okky.kr/)
- [Kafka KR (KakaoTalk)](https://open.kakao.com/)

{{< callout type="info" title="Key Points" >}}
- **English**: Stack Overflow (Q&A), Confluent Forum (official), Reddit r/apachekafka
- **Korean**: OKKY, KakaoTalk Kafka KR open chat rooms
{{< /callout >}}

#### Blogs and Articles

**English Blogs**

Confluent Blog is the official technical blog for Kafka, publishing in-depth articles on new features, performance tuning, architecture patterns. Martin Kleppmann's blog is run by the author of "Designing Data-Intensive Applications," featuring in-depth articles on distributed systems and data consistency. Jay Kreps is Kafka's creator who shares insights on distributed systems and streaming on LinkedIn.

- [Confluent Blog](https://www.confluent.io/blog/)
- [Martin Kleppmann's Blog](https://martin.kleppmann.com/)
- [Jay Kreps' LinkedIn](https://www.linkedin.com/in/jaykreps/)

**Korean Blogs**

Woowahan Brothers Tech Blog is the technical blog of Woowahan Brothers, which operates the Baedal Minjok service, sharing Kafka use cases and operational experiences. Kakao Tech Blog covers Kakao's operational experience with Kafka handling large-scale traffic. Naver D2 is Naver's technical blog, publishing articles on distributed systems and messaging.

- [Woowahan Brothers Tech Blog](https://techblog.woowahan.com/)
- [Kakao Tech Blog](https://tech.kakao.com/)
- [Naver D2](https://d2.naver.com/)

{{< callout type="info" title="Key Points" >}}
- **English**: Confluent Blog (official), Martin Kleppmann (distributed systems), Jay Kreps (creator)
- **Korean**: Check practical cases on Woowahan Brothers, Kakao, Naver D2 tech blogs
{{< /callout >}}

#### Tools

**Development and Testing**

Useful tools for developing and testing Kafka. Kafka UI is an open-source tool for managing and monitoring Kafka clusters via web browser. Perform Topic creation, message viewing, Consumer Group management, etc. through GUI. Conduktor is a Kafka desktop client convenient for managing Kafka in local development environments. Has free and paid versions. kcat (formerly kafkacat) is a command-line tool for quickly producing or consuming messages in the terminal.

- [Kafka UI](https://github.com/provectus/kafka-ui)
- [Conduktor](https://www.conduktor.io/)
- [kcat (kafkacat)](https://github.com/edenhill/kcat)

**Monitoring**

Tools for monitoring Kafka in production environments. Kafka Exporter exposes Kafka metrics in Prometheus format. Combined with Prometheus and Grafana, you can configure dashboards and alerts. Burrow is a Consumer Lag monitoring tool developed by LinkedIn. Track Consumer Group lag status and set up alerts. AKHQ (formerly Kafka HQ) is a web-based Kafka management tool providing both monitoring and management features.

- [Kafka Exporter](https://github.com/danielqsj/kafka_exporter)
- [Burrow](https://github.com/linkedin/Burrow)
- [AKHQ](https://github.com/tchiotludo/akhq)

{{< callout type="info" title="Key Points" >}}
- **Development/Testing**: Kafka UI (web GUI), Conduktor (desktop), kcat (CLI)
- **Monitoring**: Kafka Exporter + Prometheus + Grafana, Burrow (Consumer Lag)
{{< /callout >}}

#### GitHub Repositories

GitHub repositories for checking Kafka-related source code and examples. apache/kafka is Kafka's official source code repository. Check implementation details or track issues. spring-projects/spring-kafka is the Spring Kafka project source code repository. Reference Kafka usage patterns in Spring environments. confluentinc/examples is an example repository provided by Confluent, containing Kafka usage examples in various languages and frameworks.

- [apache/kafka](https://github.com/apache/kafka)
- [spring-projects/spring-kafka](https://github.com/spring-projects/spring-kafka)
- [confluentinc/examples](https://github.com/confluentinc/examples)

{{< callout type="info" title="Key Points" >}}
- **apache/kafka**: Official source code, implementation details and issue tracking
- **spring-projects/spring-kafka**: Reference for Spring environment usage patterns
- **confluentinc/examples**: Examples in various languages/frameworks
{{< /callout >}}

#### Next Steps

After practicing all examples in this guide, proceed to learn other components of the Kafka ecosystem. Kafka Streams is a library for building real-time stream processing applications on Kafka. Kafka Connect is a framework for integrating external systems like databases, file systems, and other messaging systems with Kafka. Schema Registry is a service for centrally managing message schemas using Avro, Protobuf, or JSON Schema. ksqlDB is a tool that enables writing stream processing queries in SQL syntax.
