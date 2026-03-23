---
lastmod: "2026-01-10"
title: 참고 자료
description: "Kafka 공식 문서와 참고 자료 목록입니다."
weight: 2
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Kafka 학습을 위한 추가 자료들을 정리했습니다. 공식 문서부터 서적, 온라인 강의, 커뮤니티, 도구까지 체계적으로 학습할 수 있는 자료들을 모았습니다.

{{< callout type="tip" title="TL;DR" >}}
- **공식 문서**: Apache Kafka, Spring Kafka, Confluent 문서 필수
- **입문 서적**: "Kafka: The Definitive Guide", "Kafka in Action" 추천
- **온라인 강의**: Confluent Developer(무료), Udemy, 인프런
- **커뮤니티**: Stack Overflow, Confluent Forum, OKKY
- **도구**: Kafka UI(관리), Prometheus+Grafana(모니터링)
{{< /callout >}}

#### 공식 문서

**Apache Kafka**

Apache Kafka 공식 문서는 가장 정확하고 최신 정보를 제공합니다. Apache Kafka Documentation은 Kafka의 전체 아키텍처, 설정, API를 다루는 공식 문서입니다. Kafka Quickstart는 처음 시작하는 사람을 위한 공식 Quick Start 가이드로, 설치부터 기본 메시지 송수신까지 안내합니다. Kafka APIs는 Producer API, Consumer API, Streams API, Admin API 등 각 API의 상세 레퍼런스를 제공합니다. Kafka Configuration은 Broker, Producer, Consumer, Topic 등 모든 설정 옵션의 전체 목록과 설명을 포함합니다.

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka Quickstart](https://kafka.apache.org/quickstart)
- [Kafka APIs](https://kafka.apache.org/documentation/#api)
- [Kafka Configuration](https://kafka.apache.org/documentation/#configuration)

**Spring for Apache Kafka**

Spring Kafka는 Spring Framework와 Kafka를 통합하는 프로젝트입니다. Spring Kafka Reference는 Spring Kafka의 공식 레퍼런스 문서로, KafkaTemplate, @KafkaListener, 트랜잭션, 에러 처리 등을 상세히 다룹니다. Spring Kafka API Docs는 JavaDoc으로, 클래스와 메서드의 상세 API를 확인할 수 있습니다. Spring Boot Kafka Properties는 Spring Boot에서 사용 가능한 spring.kafka.* 설정 옵션들을 정리한 문서입니다.

- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [Spring Kafka API Docs](https://docs.spring.io/spring-kafka/docs/current/api/)
- [Spring Boot Kafka Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.integration.spring.kafka.admin.auto-create)

**Confluent**

Confluent는 Kafka의 상업적 배포판과 관리형 서비스를 제공하는 회사로, Kafka 창시자들이 설립했습니다. Confluent Documentation은 Confluent Platform의 공식 문서로, Schema Registry, Connect, ksqlDB 등 추가 컴포넌트도 다룹니다. Kafka Tutorials는 단계별 실습 튜토리얼로, 다양한 언어와 프레임워크에서 Kafka를 사용하는 방법을 안내합니다. Confluent Blog는 Kafka 관련 심층 기술 아티클과 사례 연구를 게시합니다.

- [Confluent Documentation](https://docs.confluent.io/)
- [Kafka Tutorials](https://developer.confluent.io/tutorials/)
- [Confluent Blog](https://www.confluent.io/blog/)

{{< callout type="info" title="핵심 포인트" >}}
- **Apache Kafka**: 공식 문서로 가장 정확하고 최신 정보 제공
- **Spring Kafka**: Spring 환경에서 Kafka 사용 시 필수 참조
- **Confluent**: Schema Registry, Connect, ksqlDB 등 생태계 확장 정보
{{< /callout >}}

#### 추천 서적

**입문 단계**

Kafka를 처음 시작한다면 다음 책들을 추천합니다. "Kafka: The Definitive Guide"는 Neha Narkhede, Gwen Shapira, Todd Palino가 저술한 책으로, Kafka의 핵심 개념과 아키텍처를 체계적으로 설명합니다. Kafka 공식 프로젝트의 창시자와 핵심 개발자들이 직접 쓴 책이라 정확하고 깊이 있습니다. "Kafka in Action"은 Dylan Scott이 저술한 책으로, 실전 예제 중심으로 Kafka 사용법을 익힐 수 있습니다. 실제 코드를 통해 배우는 것을 선호한다면 이 책이 적합합니다.

**심화 단계**

기본기를 익힌 후 심화 학습을 위해 다음 책들을 추천합니다. "Designing Event-Driven Systems"는 Ben Stopford가 저술한 책으로, 이벤트 기반 아키텍처 설계를 다룹니다. Kafka를 활용한 시스템 설계 패턴과 마이크로서비스 아키텍처를 배울 수 있습니다. Confluent에서 무료 PDF를 제공합니다. "Kafka Streams in Action"은 Bill Bejeck이 저술한 책으로, Kafka Streams를 활용한 실시간 스트림 처리를 심도 있게 다룹니다.

{{< callout type="info" title="핵심 포인트" >}}
- **입문**: "Kafka: The Definitive Guide" (창시자 저술, 체계적), "Kafka in Action" (실전 예제 중심)
- **심화**: "Designing Event-Driven Systems" (무료 PDF), "Kafka Streams in Action"
{{< /callout >}}

#### 온라인 강의

체계적인 학습을 원한다면 온라인 강의를 활용할 수 있습니다. Confluent Developer의 Apache Kafka 101은 무료로 제공되는 공식 강의로, Kafka의 기본 개념부터 실습까지 다룹니다. 영어로 진행되며 자막을 제공합니다. Udemy의 Apache Kafka Series는 유료 강의로, 한글 자막을 지원하며 다양한 수준의 코스가 있습니다. 인프런에서는 한글로 제작된 여러 Kafka 강의들을 찾을 수 있습니다.

- [Confluent Developer](https://developer.confluent.io/)
- [Udemy](https://www.udemy.com/)
- [인프런](https://www.inflearn.com/)

{{< callout type="info" title="핵심 포인트" >}}
- **무료**: Confluent Developer의 Apache Kafka 101 (영어, 자막 제공)
- **유료**: Udemy (한글 자막), 인프런 (한글 강의)
{{< /callout >}}

#### 커뮤니티

**포럼과 Q&A**

문제가 발생했을 때 질문하거나 다른 사람의 해결 사례를 찾을 수 있는 커뮤니티들입니다. Stack Overflow의 apache-kafka 태그에서는 Kafka 관련 기술 Q&A를 찾을 수 있습니다. 검색을 통해 비슷한 문제의 해결책을 찾거나 새 질문을 올릴 수 있습니다. Confluent Community Forum은 Confluent에서 운영하는 공식 포럼으로, Kafka 코어뿐 아니라 Confluent Platform 관련 질문도 다룹니다. Reddit의 r/apachekafka 서브레딧은 Kafka 관련 토론과 정보 공유가 이루어지는 커뮤니티입니다.

- [Stack Overflow - Kafka](https://stackoverflow.com/questions/tagged/apache-kafka)
- [Confluent Community](https://forum.confluent.io/)
- [Reddit r/apachekafka](https://www.reddit.com/r/apachekafka/)

**한국 커뮤니티**

한글로 질문하고 답변을 받을 수 있는 국내 커뮤니티들입니다. OKKY는 한국 개발자 커뮤니티로, Kafka 관련 질문을 올리거나 경험을 공유할 수 있습니다. 카카오톡 오픈채팅방에서 Kafka KR을 검색하면 Kafka 관련 오픈채팅방을 찾을 수 있습니다.

- [OKKY](https://okky.kr/)
- [Kafka KR (카카오톡)](https://open.kakao.com/)

{{< callout type="info" title="핵심 포인트" >}}
- **영문**: Stack Overflow (Q&A), Confluent Forum (공식), Reddit r/apachekafka
- **한글**: OKKY, 카카오톡 Kafka KR 오픈채팅방
{{< /callout >}}

#### 블로그와 아티클

**영문 블로그**

Confluent Blog는 Kafka 관련 공식 기술 블로그로, 새 기능 소개, 성능 튜닝, 아키텍처 패턴 등 깊이 있는 아티클을 게시합니다. Martin Kleppmann의 블로그는 "Designing Data-Intensive Applications"의 저자가 운영하는 블로그로, 분산 시스템과 데이터 일관성에 관한 심층적인 글을 읽을 수 있습니다. Jay Kreps는 Kafka의 창시자로 LinkedIn에서 분산 시스템과 스트리밍에 관한 인사이트를 공유합니다.

- [Confluent Blog](https://www.confluent.io/blog/)
- [Martin Kleppmann's Blog](https://martin.kleppmann.com/)
- [Jay Kreps' LinkedIn](https://www.linkedin.com/in/jaykreps/)

**한글 블로그**

우아한형제들 기술블로그는 배달의민족 서비스를 운영하는 우아한형제들의 기술 블로그로, Kafka 활용 사례와 운영 경험을 공유합니다. 카카오 기술블로그는 대규모 트래픽을 처리하는 카카오의 Kafka 운영 경험을 다룹니다. 네이버 D2는 네이버의 기술 블로그로, 분산 시스템과 메시징 관련 아티클을 게시합니다.

- [우아한형제들 기술블로그](https://techblog.woowahan.com/)
- [카카오 기술블로그](https://tech.kakao.com/)
- [네이버 D2](https://d2.naver.com/)

{{< callout type="info" title="핵심 포인트" >}}
- **영문**: Confluent Blog (공식), Martin Kleppmann (분산 시스템), Jay Kreps (창시자)
- **한글**: 우아한형제들, 카카오, 네이버 D2 기술블로그에서 실무 사례 확인
{{< /callout >}}

#### 도구

**개발과 테스트**

Kafka를 개발하고 테스트할 때 유용한 도구들입니다. Kafka UI는 웹 브라우저에서 Kafka 클러스터를 관리하고 모니터링할 수 있는 오픈소스 도구입니다. Topic 생성, 메시지 조회, Consumer Group 관리 등을 GUI로 수행할 수 있습니다. Conduktor는 Kafka 데스크톱 클라이언트로, 로컬 개발 환경에서 Kafka를 관리하기 편리합니다. 무료 버전과 유료 버전이 있습니다. kcat(구 kafkacat)은 커맨드라인 도구로, 터미널에서 빠르게 메시지를 생산하거나 소비할 수 있습니다.

- [Kafka UI](https://github.com/provectus/kafka-ui)
- [Conduktor](https://www.conduktor.io/)
- [kcat (kafkacat)](https://github.com/edenhill/kcat)

**모니터링**

프로덕션 환경에서 Kafka를 모니터링하기 위한 도구들입니다. Kafka Exporter는 Kafka 메트릭을 Prometheus 형식으로 노출하는 도구입니다. Prometheus와 Grafana를 함께 사용하면 대시보드와 알림을 구성할 수 있습니다. Burrow는 LinkedIn에서 개발한 Consumer Lag 모니터링 전문 도구입니다. Consumer Group의 지연 상태를 추적하고 알림을 설정할 수 있습니다. AKHQ(구 Kafka HQ)는 웹 기반 Kafka 관리 도구로, 모니터링과 관리 기능을 모두 제공합니다.

- [Kafka Exporter](https://github.com/danielqsj/kafka_exporter)
- [Burrow](https://github.com/linkedin/Burrow)
- [AKHQ](https://github.com/tchiotludo/akhq)

{{< callout type="info" title="핵심 포인트" >}}
- **개발/테스트**: Kafka UI (웹 GUI), Conduktor (데스크톱), kcat (CLI)
- **모니터링**: Kafka Exporter + Prometheus + Grafana, Burrow (Consumer Lag)
{{< /callout >}}

#### GitHub 저장소

Kafka 관련 소스 코드와 예제를 확인할 수 있는 GitHub 저장소들입니다. apache/kafka는 Kafka의 공식 소스 코드 저장소입니다. 구현 세부사항을 확인하거나 이슈를 추적할 수 있습니다. spring-projects/spring-kafka는 Spring Kafka 프로젝트의 소스 코드 저장소입니다. Spring 환경에서의 Kafka 사용 패턴을 참고할 수 있습니다. confluentinc/examples는 Confluent에서 제공하는 예제 저장소로, 다양한 언어와 프레임워크에서의 Kafka 사용 예제를 포함합니다.

- [apache/kafka](https://github.com/apache/kafka)
- [spring-projects/spring-kafka](https://github.com/spring-projects/spring-kafka)
- [confluentinc/examples](https://github.com/confluentinc/examples)

{{< callout type="info" title="핵심 포인트" >}}
- **apache/kafka**: 공식 소스 코드, 구현 세부사항 및 이슈 추적
- **spring-projects/spring-kafka**: Spring 환경 사용 패턴 참고
- **confluentinc/examples**: 다양한 언어/프레임워크 예제
{{< /callout >}}

#### 다음 단계

이 가이드의 예제를 모두 실습한 후에는 Kafka 생태계의 다른 컴포넌트를 학습하시기 바랍니다. Kafka Streams는 Kafka 위에서 실시간 스트림 처리 애플리케이션을 구축하는 라이브러리입니다. Kafka Connect는 데이터베이스, 파일 시스템, 다른 메시징 시스템 등 외부 시스템과 Kafka를 연동하는 프레임워크입니다. Schema Registry는 Avro, Protobuf, JSON Schema를 사용하여 메시지 스키마를 중앙에서 관리하는 서비스입니다. ksqlDB는 SQL 구문으로 스트림 처리 쿼리를 작성할 수 있게 해주는 도구입니다.
