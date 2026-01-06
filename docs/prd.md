# Kafka Guidance 101 Product Requirements Document (PRD)

## Goals and Background Context

### Goals

- Kafka 핵심 개념을 체계적으로 이해할 수 있는 문서 제공
- Spring Boot 환경에서 바로 실행 가능한 예제 코드 제공
- 5분 안에 Kafka 메시지 송수신을 경험할 수 있는 Quick Start 제공
- GitHub Pages에서 접근 가능한 정적 사이트로 배포
- 학습 효과를 높이는 보조 자료 (용어 사전, 참고 자료) 제공

### Background Context

Apache Kafka는 현대 분산 시스템의 핵심 인프라로, 백엔드 개발자에게 필수 역량이 되었습니다. 그러나 양질의 한국어 학습 자료가 부족하고, 기존 자료들은 개념 설명 위주로 실제 코드 구현과 괴리가 있습니다.

이 프로젝트는 "First Principles" 접근 방식으로 Kafka의 본질적 개념을 설명하고, Spring Boot와 통합된 실행 가능한 예제를 제공하여 "따라하면서 배우는" 학습 경험을 제공합니다. Hugo 정적 사이트 생성기와 GitHub Pages를 활용하여 누구나 무료로 접근할 수 있는 가이드를 목표로 합니다.

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-12-25 | 0.1 | 초안 작성 | BMad Master |

---

## Requirements

### Functional Requirements

#### 문서 콘텐츠

- **FR1:** Quick Start 섹션은 Docker Compose를 사용하여 5분 안에 Kafka 환경을 구성하고 메시지 송수신을 확인할 수 있어야 한다.
- **FR2:** 개념 이해 섹션은 Producer, Consumer, Broker, Topic, Partition의 핵심 구성요소를 Mermaid 다이어그램과 함께 설명해야 한다.
- **FR3:** 개념 이해 섹션은 메시지 흐름 (발행 → 저장 → 소비)을 시각적으로 설명해야 한다.
- **FR4:** 심화 개념 섹션은 Consumer Group, Offset, Replication, acks, Message Key, Retention을 다루어야 한다.
- **FR5:** 환경 구성 섹션은 Docker Compose 파일과 Spring Boot 프로젝트 설정 방법을 제공해야 한다.
- **FR6:** 기본 Producer/Consumer 예제는 단순 메시지 전송 및 수신 코드를 포함해야 한다.
- **FR7:** 주문 시스템 예제는 실무에 가까운 시나리오 (주문 생성, 결제, 배송)를 구현해야 한다.
- **FR8:** 용어 사전은 Kafka 핵심 용어를 정의하고 설명해야 한다.
- **FR9:** 참고 자료 섹션은 공식 문서 및 추가 학습 자료 링크를 제공해야 한다.

#### 예제 코드

- **FR10:** 모든 예제 코드는 Java 17+ 및 Spring Boot 3.x 환경에서 실행 가능해야 한다.
- **FR11:** 예제 프로젝트는 독립적으로 실행 가능한 완전한 프로젝트여야 한다.
- **FR12:** Docker Compose 파일은 Kafka를 KRaft 모드로 실행해야 한다 (Zookeeper 미사용).
- **FR13:** 각 예제는 README를 포함하여 실행 방법을 설명해야 한다.

#### 다이어그램

- **FR14:** 모든 개념 설명에는 Mermaid.js 다이어그램이 포함되어야 한다.
- **FR15:** 다이어그램은 Hugo 사이트에서 정상 렌더링되어야 한다.

### Non-Functional Requirements

#### 배포 및 접근성

- **NFR1:** 사이트는 GitHub Pages에서 호스팅되어야 한다.
- **NFR2:** Hugo 정적 사이트 생성기를 사용해야 한다.
- **NFR3:** 사이트는 모바일 및 데스크톱에서 반응형으로 동작해야 한다.
- **NFR4:** 페이지 로딩 시간은 3초 이내여야 한다.

#### 콘텐츠 품질

- **NFR5:** 모든 문서는 한국어로 작성되어야 한다 (기술 용어는 영어 허용).
- **NFR6:** 문서는 일관된 스타일과 톤을 유지해야 한다.
- **NFR7:** 코드 예제는 복사-붙여넣기로 바로 사용 가능해야 한다.

#### 유지보수

- **NFR8:** GitHub Actions를 사용하여 자동 배포해야 한다.
- **NFR9:** 문서 소스는 Markdown 형식이어야 한다.

---

## Technical Assumptions

### Repository Structure: Monorepo

```
kafka-guidance-101/
├── content/              # Hugo 콘텐츠 (Markdown)
│   ├── docs/
│   │   ├── _index.md
│   │   ├── quick-start/
│   │   ├── concepts/
│   │   ├── examples/
│   │   └── appendix/
├── examples/             # 예제 프로젝트
│   ├── quick-start/
│   └── order-system/
├── static/               # 정적 자산
├── docker/               # Docker Compose 파일
├── themes/               # Hugo 테마
├── config.toml           # Hugo 설정
└── .github/
    └── workflows/        # GitHub Actions
```

### Service Architecture

- **정적 사이트:** Hugo로 생성된 정적 HTML/CSS/JS
- **호스팅:** GitHub Pages (무료)
- **예제 애플리케이션:** 독립적인 Spring Boot 프로젝트들
- **로컬 인프라:** Docker Compose (Kafka KRaft 모드)

### Testing Requirements

- **문서:** 링크 유효성 검사, Mermaid 렌더링 확인
- **예제 코드:** 각 예제 프로젝트의 빌드 및 실행 테스트
- **배포:** GitHub Pages 배포 후 사이트 접근 확인

### Additional Technical Assumptions

- Hugo 테마: 문서에 적합한 테마 선택 (Book, Docsy 등 검토)
- Mermaid.js: Hugo 테마 또는 shortcode로 통합
- Kafka 버전: 최신 LTS (3.x)
- Spring Boot 버전: 3.x
- Java 버전: 17+
- Spring Kafka: spring-kafka 최신 버전

---

## Epic List

| Epic | 제목 | 목표 |
|------|------|------|
| **Epic 1** | 프로젝트 기반 구축 | Hugo 사이트 초기화, GitHub Actions 설정, 기본 구조 확립 |
| **Epic 2** | Quick Start 완성 | 5분 만에 Kafka 체험할 수 있는 가이드 작성 |
| **Epic 3** | 개념 이해 섹션 | Kafka 핵심 개념 및 심화 개념 문서화 |
| **Epic 4** | 실습 예제 확장 | 주문 시스템 예제 및 추가 실습 작성 |
| **Epic 5** | 부록 및 마무리 | 용어 사전, 참고 자료, 최종 검토 |

---

## Epic 1: 프로젝트 기반 구축

**목표:** Hugo 프로젝트를 초기화하고, GitHub Actions를 통한 자동 배포 파이프라인을 구축하며, 프로젝트의 기본 구조를 확립합니다. 이 Epic 완료 시 빈 사이트가 GitHub Pages에 배포되어 접근 가능해야 합니다.

### Story 1.1: Hugo 프로젝트 초기화

**As a** 개발자,
**I want** Hugo 프로젝트를 초기화하고 적합한 테마를 설정하고 싶다,
**So that** 문서 사이트의 기본 구조를 갖출 수 있다.

**Acceptance Criteria:**
1. Hugo 프로젝트가 초기화되어 있다
2. 문서에 적합한 테마가 적용되어 있다 (Book 또는 Docsy)
3. config.toml에 기본 설정이 완료되어 있다
4. 로컬에서 `hugo server`로 사이트 확인 가능하다
5. 메인 페이지에 프로젝트 제목과 간단한 소개가 표시된다

### Story 1.2: 프로젝트 디렉토리 구조 설정

**As a** 개발자,
**I want** 프로젝트의 디렉토리 구조를 설정하고 싶다,
**So that** 문서와 예제 코드를 체계적으로 관리할 수 있다.

**Acceptance Criteria:**
1. content/docs/ 디렉토리 구조가 생성되어 있다 (quick-start, concepts, examples, appendix)
2. examples/ 디렉토리가 생성되어 있다
3. docker/ 디렉토리가 생성되어 있다
4. 각 섹션에 _index.md 파일이 있다
5. 사이드바 네비게이션에 섹션 구조가 반영된다

### Story 1.3: GitHub Actions 배포 설정

**As a** 개발자,
**I want** GitHub Actions를 통한 자동 배포를 설정하고 싶다,
**So that** main 브랜치에 푸시하면 자동으로 GitHub Pages에 배포된다.

**Acceptance Criteria:**
1. .github/workflows/deploy.yml 파일이 생성되어 있다
2. main 브랜치 푸시 시 자동으로 빌드 및 배포가 실행된다
3. GitHub Pages 설정이 완료되어 있다
4. 배포된 사이트에 접근 가능하다
5. 빌드 실패 시 알림이 발생한다

### Story 1.4: Mermaid.js 통합

**As a** 독자,
**I want** Mermaid 다이어그램이 문서에서 정상 렌더링되기를 원한다,
**So that** 시각적으로 개념을 이해할 수 있다.

**Acceptance Criteria:**
1. Hugo 테마 또는 shortcode로 Mermaid.js가 통합되어 있다
2. Markdown 내 mermaid 코드 블록이 다이어그램으로 렌더링된다
3. 테스트용 다이어그램이 포함된 샘플 페이지가 있다
4. 로컬 및 배포 환경 모두에서 정상 동작한다

---

## Epic 2: Quick Start 완성

**목표:** Docker Compose로 Kafka 환경을 구성하고, Spring Boot Producer/Consumer를 실행하여 5분 안에 메시지 송수신을 경험할 수 있는 가이드를 완성합니다.

### Story 2.1: Docker Compose 파일 작성

**As a** 개발자,
**I want** Kafka를 KRaft 모드로 실행하는 Docker Compose 파일을 원한다,
**So that** 로컬에서 쉽게 Kafka 환경을 구성할 수 있다.

**Acceptance Criteria:**
1. docker/docker-compose.yml 파일이 생성되어 있다
2. Kafka가 KRaft 모드로 실행된다 (Zookeeper 없음)
3. `docker-compose up -d`로 Kafka가 정상 시작된다
4. localhost:9092로 Kafka에 접근 가능하다
5. 종료 및 재시작이 정상 동작한다

### Story 2.2: Quick Start Spring Boot 프로젝트 생성

**As a** 개발자,
**I want** 최소한의 설정으로 동작하는 Spring Boot Kafka 예제를 원한다,
**So that** 빠르게 메시지 송수신을 테스트할 수 있다.

**Acceptance Criteria:**
1. examples/quick-start/ 디렉토리에 Spring Boot 프로젝트가 있다
2. Producer가 메시지를 전송할 수 있다
3. Consumer가 메시지를 수신하고 로그로 출력한다
4. application.yml에 Kafka 설정이 포함되어 있다
5. README.md에 실행 방법이 설명되어 있다

### Story 2.3: Quick Start 문서 작성

**As a** Kafka 입문자,
**I want** 5분 안에 Kafka를 체험할 수 있는 가이드를 원한다,
**So that** 복잡한 개념 없이 빠르게 시작할 수 있다.

**Acceptance Criteria:**
1. content/docs/quick-start/ 문서가 작성되어 있다
2. Docker Compose 실행 방법이 설명되어 있다
3. Spring Boot 프로젝트 실행 방법이 설명되어 있다
4. 메시지 전송 및 수신 확인 방법이 설명되어 있다
5. 예상 결과 (로그 출력)가 포함되어 있다
6. 트러블슈팅 팁이 포함되어 있다

---

## Epic 3: 개념 이해 섹션

**목표:** Kafka의 핵심 구성요소, 메시지 흐름, 심화 개념을 Mermaid 다이어그램과 함께 체계적으로 문서화합니다.

### Story 3.1: 핵심 구성요소 문서 작성

**As a** Kafka 입문자,
**I want** Producer, Consumer, Broker, Topic, Partition에 대한 명확한 설명을 원한다,
**So that** Kafka의 기본 구조를 이해할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/core-components.md 문서가 작성되어 있다
2. 각 구성요소의 역할이 명확히 설명되어 있다
3. 구성요소 간의 관계를 보여주는 Mermaid 다이어그램이 포함되어 있다
4. Kafka가 해결하는 본질적 문제 (비동기, 고용량, 고가용성)와 연결되어 있다
5. 간단한 비유나 예시가 포함되어 있다

### Story 3.2: 메시지 흐름 문서 작성

**As a** Kafka 입문자,
**I want** 메시지가 발행되고 소비되는 전체 흐름을 이해하고 싶다,
**So that** Kafka가 어떻게 동작하는지 파악할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/message-flow.md 문서가 작성되어 있다
2. 발행 과정이 설명되어 있다
3. 저장 구조 (Topic, Partition, Offset)가 설명되어 있다
4. 소비 과정이 설명되어 있다
5. 전체 흐름을 보여주는 시퀀스 다이어그램이 포함되어 있다

### Story 3.3: Consumer Group & Offset 문서 작성

**As a** Kafka 학습자,
**I want** Consumer Group과 Offset 개념을 이해하고 싶다,
**So that** 병렬 처리와 장애 복구 메커니즘을 알 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/consumer-group-offset.md 문서가 작성되어 있다
2. Consumer Group의 역할과 규칙이 설명되어 있다
3. Offset의 개념과 커밋 방식이 설명되어 있다
4. 관련 Mermaid 다이어그램이 포함되어 있다
5. 장애 복구 시나리오가 설명되어 있다

### Story 3.4: Replication & Leader Election 문서 작성

**As a** Kafka 학습자,
**I want** 데이터 복제와 리더 선출 메커니즘을 이해하고 싶다,
**So that** 고가용성이 어떻게 보장되는지 알 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/replication.md 문서가 작성되어 있다
2. Leader/Follower 개념이 설명되어 있다
3. Replication Factor와 ISR이 설명되어 있다
4. Leader Election 과정이 설명되어 있다
5. Zookeeper vs KRaft 비교가 포함되어 있다
6. 관련 Mermaid 다이어그램이 포함되어 있다

### Story 3.5: acks & Message Key & Retention 문서 작성

**As a** Kafka 학습자,
**I want** 전송 보장, 파티셔닝, 보관 정책을 이해하고 싶다,
**So that** 실무에서 적절한 설정을 선택할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/advanced-concepts.md 문서가 작성되어 있다
2. acks 옵션 (0, 1, all)과 Trade-off가 설명되어 있다
3. Message Key와 파티션 결정 방식이 설명되어 있다
4. Retention 정책 (시간, 용량, compaction)이 설명되어 있다
5. 각 개념에 적합한 사용 사례가 포함되어 있다

---

## Epic 4: 실습 예제 확장

**목표:** 환경 구성 상세 가이드와 주문 시스템 예제를 작성하여 실무에 가까운 학습 경험을 제공합니다.

### Story 4.1: 환경 구성 상세 문서 작성

**As a** Spring Boot 개발자,
**I want** Kafka 연동을 위한 상세한 Spring Boot 설정 방법을 원한다,
**So that** 내 프로젝트에 Kafka를 적용할 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/setup.md 문서가 작성되어 있다
2. Spring Boot 의존성 설정이 설명되어 있다
3. application.yml 설정이 상세히 설명되어 있다
4. Producer/Consumer 설정 옵션이 설명되어 있다
5. 일반적인 설정 오류와 해결 방법이 포함되어 있다

### Story 4.2: 기본 Producer/Consumer 예제 문서

**As a** Spring Boot 개발자,
**I want** 기본적인 Producer/Consumer 구현 방법을 상세히 알고 싶다,
**So that** 코드 작성 방법을 익힐 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/basic.md 문서가 작성되어 있다
2. KafkaTemplate을 사용한 Producer 코드가 설명되어 있다
3. @KafkaListener를 사용한 Consumer 코드가 설명되어 있다
4. 동기/비동기 전송 방식이 설명되어 있다
5. 코드에 대한 상세 설명이 포함되어 있다

### Story 4.3: 주문 시스템 예제 프로젝트 생성

**As a** 개발자,
**I want** 실무에 가까운 주문 시스템 예제를 원한다,
**So that** 실제 사용 사례를 경험할 수 있다.

**Acceptance Criteria:**
1. examples/order-system/ 디렉토리에 프로젝트가 있다
2. 주문 생성 이벤트 Producer가 구현되어 있다
3. 주문 처리 Consumer가 구현되어 있다
4. 여러 이벤트 타입 (주문생성, 결제완료, 배송시작)이 구현되어 있다
5. Message Key를 사용한 순서 보장이 구현되어 있다
6. README.md에 실행 방법이 설명되어 있다

### Story 4.4: 주문 시스템 예제 문서 작성

**As a** Kafka 학습자,
**I want** 주문 시스템 예제에 대한 상세한 설명을 원한다,
**So that** 코드를 이해하고 응용할 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/order-system.md 문서가 작성되어 있다
2. 시스템 아키텍처가 다이어그램으로 설명되어 있다
3. 각 이벤트 타입과 처리 로직이 설명되어 있다
4. Message Key 사용 이유가 설명되어 있다
5. 실행 방법과 결과 확인 방법이 포함되어 있다

---

## Epic 5: 부록 및 마무리

**목표:** 용어 사전, 참고 자료를 작성하고, 전체 문서를 검토하여 최종 완성합니다.

### Story 5.1: 용어 사전 작성

**As a** Kafka 입문자,
**I want** Kafka 용어를 빠르게 찾아볼 수 있는 사전을 원한다,
**So that** 문서를 읽다가 모르는 용어를 확인할 수 있다.

**Acceptance Criteria:**
1. content/docs/appendix/glossary.md 문서가 작성되어 있다
2. 주요 Kafka 용어가 알파벳/가나다 순으로 정리되어 있다
3. 각 용어에 간결한 정의와 설명이 포함되어 있다
4. 관련 문서 섹션으로의 링크가 포함되어 있다

### Story 5.2: 참고 자료 페이지 작성

**As a** Kafka 학습자,
**I want** 추가 학습을 위한 참고 자료 목록을 원한다,
**So that** 더 깊이 공부할 수 있다.

**Acceptance Criteria:**
1. content/docs/appendix/references.md 문서가 작성되어 있다
2. Apache Kafka 공식 문서 링크가 포함되어 있다
3. Spring for Apache Kafka 문서 링크가 포함되어 있다
4. 추천 서적/강의가 포함되어 있다
5. 커뮤니티 리소스 (블로그, 유튜브 등)가 포함되어 있다

### Story 5.3: 최종 검토 및 수정

**As a** 프로젝트 관리자,
**I want** 전체 문서를 검토하고 일관성을 확인하고 싶다,
**So that** 품질 높은 가이드를 제공할 수 있다.

**Acceptance Criteria:**
1. 모든 내부 링크가 유효하다
2. 모든 Mermaid 다이어그램이 정상 렌더링된다
3. 모든 예제 코드가 정상 실행된다
4. 문서 스타일이 일관성 있다
5. 오탈자가 수정되어 있다
6. GitHub Pages에서 최종 배포가 완료되어 있다

---

---

## Brownfield Enhancement: Scala 가이드 추가

### Enhancement Overview

**Enhancement Type:** New Feature Addition (새로운 가이드 콘텐츠 추가)

**Enhancement Description:**
Scala 프로그래밍 언어에 대한 체계적인 한글 가이드를 추가합니다. 기존 Kafka/DDD 가이드와 동일한 구조(Quick Start → Concepts → Examples → Appendix)를 따르며, Scala 2.13과 Scala 3를 모두 다룹니다. 기초부터 고급까지 전체 범위를 커버하고, sbt 기반 예제 프로젝트를 포함합니다.

**Impact Assessment:** Minimal Impact (기존 코드 변경 없이 새로운 콘텐츠 추가)

### Goals

- Scala 핵심 문법과 개념을 체계적으로 이해할 수 있는 한글 문서 제공
- Scala 2.13과 Scala 3의 주요 차이점과 마이그레이션 가이드 제공
- 함수형 프로그래밍 패턴과 타입 시스템 심화 내용 제공
- sbt 기반 실행 가능한 예제 프로젝트 제공
- 기존 Kafka 가이드와 연계하여 Scala + Kafka 활용 사례 제공

### Background Context

Scala는 JVM 기반의 함수형/객체지향 하이브리드 언어로, 대규모 데이터 처리(Apache Spark), 분산 시스템(Akka), 웹 개발(Play Framework) 등에서 널리 사용됩니다. 그러나 양질의 한국어 학습 자료가 부족하고, 특히 Scala 3의 새로운 기능에 대한 한글 자료는 거의 없습니다.

이 Enhancement는 공식 Scala 문서(docs.scala-lang.org)를 참고하여, "First Principles" 접근 방식으로 Scala의 본질적 개념을 설명하고, 실행 가능한 예제를 제공합니다.

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-01-06 | 0.1 | Scala 가이드 Brownfield Enhancement 초안 | BMad Master |

---

## Scala 가이드 Requirements

### Functional Requirements

#### 문서 콘텐츠

- **SFR1:** Quick Start 섹션은 sbt를 사용하여 5분 안에 Scala 환경을 구성하고 Hello World를 실행할 수 있어야 한다.
- **SFR2:** 기초 개념 섹션은 변수, 타입, 제어 구조, 함수, 클래스, 트레이트, 케이스 클래스, 패턴 매칭을 다루어야 한다.
- **SFR3:** 중급 개념 섹션은 컬렉션, 고차 함수, 제네릭, For Comprehension, 암시적 변환/문맥적 추상화를 다루어야 한다.
- **SFR4:** 고급 개념 섹션은 타입 클래스, 공변성/반공변성, 매크로/메타프로그래밍, 동시성(Future), 함수형 프로그래밍 패턴을 다루어야 한다.
- **SFR5:** 모든 개념 설명에 Scala 2.13과 Scala 3 양쪽 문법을 비교하여 제시해야 한다.
- **SFR6:** 용어 사전은 Scala 핵심 용어를 정의하고 설명해야 한다.
- **SFR7:** Scala 2와 Scala 3 차이점 요약 문서를 제공해야 한다.

#### 예제 코드

- **SFR8:** 모든 예제 코드는 Scala 2.13 또는 Scala 3 환경에서 실행 가능해야 한다.
- **SFR9:** 예제 프로젝트는 sbt 빌드 도구를 사용해야 한다.
- **SFR10:** 각 예제는 README를 포함하여 실행 방법을 설명해야 한다.
- **SFR11:** Scala 2와 Scala 3 예제를 별도 프로젝트 또는 별도 소스 디렉토리로 구분해야 한다.

### Non-Functional Requirements

- **SNFR1:** 기존 사이트 구조(Hugo, GitHub Pages)와 완전히 호환되어야 한다.
- **SNFR2:** 기존 문서 스타일과 톤을 일관되게 유지해야 한다.
- **SNFR3:** 공식 Scala 문서(docs.scala-lang.org)를 주요 참고 자료로 활용해야 한다.

### Compatibility Requirements

- **SCR1:** 기존 Hugo 테마(hugo-theme-relearn)와 호환되어야 한다.
- **SCR2:** 기존 Mermaid.js 설정을 그대로 사용할 수 있어야 한다.
- **SCR3:** 기존 사이트 네비게이션에 Scala 가이드가 자연스럽게 추가되어야 한다.
- **SCR4:** 기존 GitHub Actions 배포 파이프라인에 영향을 주지 않아야 한다.

---

## Scala 가이드 콘텐츠 구조

```
content/ko/scala/
├── _index.md                       # Scala 가이드 메인
├── quick-start/
│   └── _index.md                   # 빠른 시작 (설치, Hello World, REPL)
├── concepts/
│   ├── _index.md                   # 개념 이해 인덱스
│   │
│   │ # 기초 (Basics)
│   ├── basics.md                   # 변수, 상수, 기본 타입
│   ├── control-structures.md       # 제어 구조 (if, while, for, match)
│   ├── functions-methods.md        # 함수와 메서드
│   ├── classes-objects.md          # 클래스, 객체, 트레이트
│   ├── case-classes.md             # 케이스 클래스
│   ├── pattern-matching.md         # 패턴 매칭
│   │
│   │ # 중급 (Intermediate)
│   ├── collections.md              # 컬렉션 (List, Set, Map, Seq)
│   ├── higher-order-functions.md   # 고차 함수
│   ├── generics.md                 # 제네릭과 타입 매개변수
│   ├── for-comprehensions.md       # For Comprehension
│   ├── implicits.md                # 암시적 변환 (Scala 2) / 문맥적 추상화 (Scala 3)
│   │
│   │ # 고급 (Advanced)
│   ├── type-classes.md             # 타입 클래스
│   ├── variance.md                 # 공변성, 반공변성
│   ├── type-system-advanced.md     # 고급 타입 시스템 (Union, Intersection, Match Types)
│   ├── macros-metaprogramming.md   # 매크로 및 메타프로그래밍
│   ├── concurrency.md              # 동시성 (Future, 기초)
│   └── functional-patterns.md      # 함수형 프로그래밍 패턴
│
├── examples/
│   ├── _index.md                   # 실습 예제 인덱스
│   ├── setup.md                    # 환경 설정 (sbt, IDE)
│   ├── basic.md                    # 기본 예제
│   └── scala2-vs-scala3.md         # Scala 2 vs Scala 3 비교 예제
│
└── appendix/
    ├── _index.md
    ├── glossary.md                 # 용어 사전
    ├── version-comparison.md       # Scala 2 vs Scala 3 주요 차이점
    ├── faq.md                      # FAQ
    └── references.md               # 참고 자료

examples/scala/
├── scala2-basics/                  # Scala 2.13 기본 예제
│   ├── build.sbt
│   ├── project/build.properties
│   └── src/main/scala/
└── scala3-basics/                  # Scala 3 기본 예제
    ├── build.sbt
    ├── project/build.properties
    └── src/main/scala/
```

---

## Scala Epic List

| Epic | 제목 | 목표 |
|------|------|------|
| **Epic 6** | Scala 가이드 기반 구축 | 디렉토리 구조, Quick Start, 메인 페이지 작성 |
| **Epic 7** | Scala 기초 문서 | 변수, 타입, 함수, 클래스, 패턴 매칭 등 기초 개념 |
| **Epic 8** | Scala 중급 문서 | 컬렉션, 고차 함수, 제네릭, For Comprehension 등 |
| **Epic 9** | Scala 고급 문서 | 타입 클래스, 매크로, 동시성, 함수형 패턴 등 |
| **Epic 10** | Scala 예제 및 부록 | 예제 프로젝트, 용어 사전, FAQ, 버전 비교 |

---

## Epic 6: Scala 가이드 기반 구축

**목표:** Scala 가이드의 기본 구조를 설정하고, Quick Start를 작성하여 사용자가 5분 안에 Scala를 체험할 수 있게 합니다.

### Story 6.1: Scala 가이드 디렉토리 구조 설정

**As a** 프로젝트 관리자,
**I want** Scala 가이드의 디렉토리 구조를 설정하고 싶다,
**So that** 일관된 구조로 문서를 관리할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/` 디렉토리 구조가 생성되어 있다 (quick-start, concepts, examples, appendix)
2. 각 섹션에 `_index.md` 파일이 있다
3. Hugo frontmatter가 올바르게 설정되어 있다
4. 사이드바 네비게이션에 Scala 가이드가 표시된다

**Integration Verification:**
- IV1: 기존 Kafka/DDD 가이드와 동일한 구조 패턴 확인
- IV2: Hugo 빌드 성공 확인
- IV3: 사이트 네비게이션 정상 동작 확인

### Story 6.2: Scala 가이드 메인 페이지 작성

**As a** Scala 입문자,
**I want** Scala 가이드의 전체 구조와 학습 경로를 파악하고 싶다,
**So that** 어디서부터 시작해야 할지 알 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/_index.md`가 작성되어 있다
2. Scala의 정의와 특징이 설명되어 있다
3. 언제 Scala를 사용해야 하는지 설명되어 있다
4. 각 섹션(Quick Start, Concepts, Examples, Appendix)에 대한 안내가 있다
5. 학습 경로 제안이 포함되어 있다
6. 선수 지식이 명시되어 있다

### Story 6.3: Scala Quick Start 작성

**As a** Scala 입문자,
**I want** 5분 안에 Scala를 체험하고 싶다,
**So that** 복잡한 개념 없이 빠르게 시작할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/quick-start/_index.md`가 작성되어 있다
2. Scala 설치 방법이 설명되어 있다 (Coursier, sbt)
3. Hello World 프로그램 작성 및 실행 방법이 설명되어 있다
4. REPL 사용법이 설명되어 있다
5. IDE 설정 안내가 포함되어 있다 (IntelliJ, VS Code)
6. Scala 2와 Scala 3 설치 옵션이 모두 안내되어 있다

### Story 6.4: Scala 예제 프로젝트 기본 구조 생성

**As a** 개발자,
**I want** sbt 기반 Scala 예제 프로젝트 템플릿을 원한다,
**So that** 예제를 직접 실행해볼 수 있다.

**Acceptance Criteria:**
1. `examples/scala/scala2-basics/` 프로젝트가 생성되어 있다
2. `examples/scala/scala3-basics/` 프로젝트가 생성되어 있다
3. 각 프로젝트에 `build.sbt`와 `project/build.properties`가 있다
4. Hello World 예제가 포함되어 있다
5. `sbt run`으로 실행 가능하다
6. README.md에 실행 방법이 설명되어 있다

---

## Epic 7: Scala 기초 문서

**목표:** Scala의 핵심 기초 개념을 체계적으로 문서화합니다. 변수, 타입, 함수, 클래스, 트레이트, 케이스 클래스, 패턴 매칭을 다룹니다.

### Story 7.1: 기본 문법 (변수, 상수, 타입) 문서 작성

**As a** Scala 입문자,
**I want** 변수 선언과 기본 타입에 대해 이해하고 싶다,
**So that** Scala 코드를 읽고 쓸 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/basics.md`가 작성되어 있다
2. `val`과 `var`의 차이가 설명되어 있다
3. 기본 타입(Int, Long, Double, String, Boolean 등)이 설명되어 있다
4. 타입 추론이 설명되어 있다
5. Scala 2와 Scala 3 문법 비교가 포함되어 있다
6. 코드 예제가 포함되어 있다

### Story 7.2: 제어 구조 문서 작성

**As a** Scala 입문자,
**I want** Scala의 제어 구조를 이해하고 싶다,
**So that** 조건문과 반복문을 사용할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/control-structures.md`가 작성되어 있다
2. `if-else` 표현식이 설명되어 있다
3. `for` 표현식이 설명되어 있다 (yield 포함)
4. `while` 루프가 설명되어 있다
5. `match` 표현식의 기본 사용법이 설명되어 있다
6. Scala에서 제어 구조가 "표현식"인 점이 강조되어 있다

### Story 7.3: 함수와 메서드 문서 작성

**As a** Scala 입문자,
**I want** 함수와 메서드 정의 방법을 이해하고 싶다,
**So that** 재사용 가능한 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/functions-methods.md`가 작성되어 있다
2. `def`를 사용한 메서드 정의가 설명되어 있다
3. 매개변수와 반환 타입이 설명되어 있다
4. 기본 매개변수 값이 설명되어 있다
5. 가변 인자(varargs)가 설명되어 있다
6. 익명 함수(람다)가 설명되어 있다
7. Scala 3의 새로운 문법(들여쓰기 기반 등)이 포함되어 있다

### Story 7.4: 클래스와 객체 문서 작성

**As a** Scala 입문자,
**I want** 클래스, 객체, 트레이트를 이해하고 싶다,
**So that** 객체지향 Scala 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/classes-objects.md`가 작성되어 있다
2. 클래스 정의와 생성자가 설명되어 있다
3. 싱글톤 객체(`object`)가 설명되어 있다
4. 컴패니언 객체가 설명되어 있다
5. 트레이트 정의와 믹스인이 설명되어 있다
6. 접근 제어자(private, protected)가 설명되어 있다
7. Scala 3의 `enum`이 소개되어 있다

### Story 7.5: 케이스 클래스 문서 작성

**As a** Scala 입문자,
**I want** 케이스 클래스의 개념과 사용법을 이해하고 싶다,
**So that** 불변 데이터 모델을 효과적으로 정의할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/case-classes.md`가 작성되어 있다
2. 케이스 클래스의 정의와 특징이 설명되어 있다
3. 자동 생성되는 메서드들(equals, hashCode, copy 등)이 설명되어 있다
4. 패턴 매칭과의 연계가 설명되어 있다
5. 케이스 클래스 vs 일반 클래스 비교가 있다
6. 실무 사용 사례가 포함되어 있다

### Story 7.6: 패턴 매칭 문서 작성

**As a** Scala 학습자,
**I want** 패턴 매칭을 깊이 이해하고 싶다,
**So that** Scala의 강력한 패턴 매칭을 활용할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/pattern-matching.md`가 작성되어 있다
2. `match` 표현식의 다양한 패턴이 설명되어 있다 (리터럴, 변수, 타입, 튜플, 케이스 클래스)
3. 가드(guard) 조건이 설명되어 있다
4. 중첩 패턴이 설명되어 있다
5. 추출자(Extractor) 패턴이 설명되어 있다
6. Scala 3의 새로운 패턴 매칭 기능이 포함되어 있다

---

## Epic 8: Scala 중급 문서

**목표:** Scala의 중급 개념을 문서화합니다. 컬렉션, 고차 함수, 제네릭, For Comprehension, 암시적 변환/문맥적 추상화를 다룹니다.

### Story 8.1: 컬렉션 문서 작성

**As a** Scala 학습자,
**I want** Scala 컬렉션 라이브러리를 이해하고 싶다,
**So that** 데이터를 효과적으로 처리할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/collections.md`가 작성되어 있다
2. 불변 컬렉션과 가변 컬렉션의 차이가 설명되어 있다
3. List, Set, Map, Seq, Vector 등 주요 컬렉션이 설명되어 있다
4. 컬렉션 연산(map, filter, flatMap, fold, reduce 등)이 설명되어 있다
5. 컬렉션 변환 메서드가 설명되어 있다
6. 성능 특성 비교가 포함되어 있다

### Story 8.2: 고차 함수 문서 작성

**As a** Scala 학습자,
**I want** 고차 함수를 이해하고 싶다,
**So that** 함수형 프로그래밍 스타일로 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/higher-order-functions.md`가 작성되어 있다
2. 고차 함수의 정의와 개념이 설명되어 있다
3. 함수를 인자로 받는 함수가 설명되어 있다
4. 함수를 반환하는 함수(커링 포함)가 설명되어 있다
5. 클로저(Closure)가 설명되어 있다
6. 실무에서 자주 사용되는 패턴이 포함되어 있다

### Story 8.3: 제네릭 문서 작성

**As a** Scala 학습자,
**I want** 제네릭과 타입 매개변수를 이해하고 싶다,
**So that** 재사용 가능한 타입 안전 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/generics.md`가 작성되어 있다
2. 타입 매개변수 기본 사용법이 설명되어 있다
3. 타입 경계(upper/lower bounds)가 설명되어 있다
4. 컨텍스트 경계가 설명되어 있다
5. 타입 제약(Type Constraints)이 설명되어 있다
6. Java 제네릭과의 비교가 포함되어 있다

### Story 8.4: For Comprehension 문서 작성

**As a** Scala 학습자,
**I want** For Comprehension을 깊이 이해하고 싶다,
**So that** 모나딕 연산을 우아하게 표현할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/for-comprehensions.md`가 작성되어 있다
2. For Comprehension의 기본 문법이 설명되어 있다
3. `<-`, `=`, `if` 가드의 역할이 설명되어 있다
4. `yield`를 사용한 값 생성이 설명되어 있다
5. For Comprehension이 flatMap/map/withFilter로 변환되는 원리가 설명되어 있다
6. Option, List, Future 등 다양한 타입에서의 활용이 포함되어 있다

### Story 8.5: 암시적 변환 / 문맥적 추상화 문서 작성

**As a** Scala 학습자,
**I want** implicit(Scala 2) / given-using(Scala 3)을 이해하고 싶다,
**So that** 고급 Scala 라이브러리를 이해하고 활용할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/implicits.md`가 작성되어 있다
2. Scala 2의 implicit 변환, implicit 매개변수가 설명되어 있다
3. Scala 3의 given/using이 설명되어 있다
4. Extension Methods가 설명되어 있다
5. 암시적 해석 규칙이 설명되어 있다
6. Scala 2에서 Scala 3로의 마이그레이션 가이드가 포함되어 있다

---

## Epic 9: Scala 고급 문서

**목표:** Scala의 고급 개념을 문서화합니다. 타입 클래스, 공변성/반공변성, 고급 타입 시스템, 매크로, 동시성, 함수형 패턴을 다룹니다.

### Story 9.1: 타입 클래스 문서 작성

**As a** Scala 고급 학습자,
**I want** 타입 클래스 패턴을 이해하고 싶다,
**So that** 확장 가능한 추상화를 설계할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/type-classes.md`가 작성되어 있다
2. 타입 클래스의 개념과 동기가 설명되어 있다
3. 타입 클래스 정의, 인스턴스, 사용 패턴이 설명되어 있다
4. Scala 2의 implicit 기반 구현이 설명되어 있다
5. Scala 3의 given/using 기반 구현이 설명되어 있다
6. 실무 예제(Ordering, Numeric 등)가 포함되어 있다

### Story 9.2: 공변성/반공변성 문서 작성

**As a** Scala 고급 학습자,
**I want** 변성(Variance)을 이해하고 싶다,
**So that** 타입 안전한 제네릭 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/variance.md`가 작성되어 있다
2. 공변성(+T), 반공변성(-T), 무공변(T)이 설명되어 있다
3. 각 변성의 사용 시점과 제약이 설명되어 있다
4. 실제 라이브러리 예제(Function, List 등)가 포함되어 있다
5. 변성 규칙과 컴파일러 검사가 설명되어 있다

### Story 9.3: 고급 타입 시스템 문서 작성

**As a** Scala 고급 학습자,
**I want** Scala 3의 고급 타입 기능을 이해하고 싶다,
**So that** 더 표현력 있는 타입을 활용할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/type-system-advanced.md`가 작성되어 있다
2. Union Types(|)가 설명되어 있다
3. Intersection Types(&)가 설명되어 있다
4. Match Types가 설명되어 있다
5. Opaque Types가 설명되어 있다
6. Type Lambdas가 설명되어 있다
7. Dependent Function Types가 소개되어 있다

### Story 9.4: 매크로와 메타프로그래밍 문서 작성

**As a** Scala 고급 학습자,
**I want** Scala의 메타프로그래밍 기능을 이해하고 싶다,
**So that** 컴파일 타임 코드 생성을 활용할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/macros-metaprogramming.md`가 작성되어 있다
2. Scala 3의 inline이 설명되어 있다
3. 컴파일 타임 연산이 설명되어 있다
4. Scala 3 매크로 기초가 설명되어 있다
5. Scala 2 매크로와의 차이점이 언급되어 있다
6. 실무 활용 사례가 포함되어 있다

### Story 9.5: 동시성 문서 작성

**As a** Scala 학습자,
**I want** Scala의 동시성 프로그래밍 기초를 이해하고 싶다,
**So that** 비동기 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/concurrency.md`가 작성되어 있다
2. Future의 개념과 사용법이 설명되어 있다
3. ExecutionContext가 설명되어 있다
4. Future 조합(map, flatMap, recover 등)이 설명되어 있다
5. Promise가 설명되어 있다
6. 비동기 에러 처리가 설명되어 있다
7. Akka/ZIO 등 고급 라이브러리 소개가 포함되어 있다

### Story 9.6: 함수형 프로그래밍 패턴 문서 작성

**As a** Scala 고급 학습자,
**I want** 함수형 프로그래밍 패턴을 이해하고 싶다,
**So that** 더 안전하고 조합 가능한 코드를 작성할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/concepts/functional-patterns.md`가 작성되어 있다
2. Functor, Applicative, Monad 개념이 설명되어 있다
3. Option, Either, Try의 함수형 활용이 설명되어 있다
4. 함수 합성(Function Composition)이 설명되어 있다
5. 참조 투명성과 부수 효과 관리가 설명되어 있다
6. Cats/ZIO 등 FP 라이브러리 소개가 포함되어 있다

---

## Epic 10: Scala 예제 및 부록

**목표:** 실습 예제 문서를 작성하고, 용어 사전, FAQ, 버전 비교 등 부록을 완성합니다.

### Story 10.1: 환경 설정 문서 작성

**As a** Scala 개발자,
**I want** 개발 환경 설정 방법을 상세히 알고 싶다,
**So that** 효율적인 개발 환경을 구축할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/examples/setup.md`가 작성되어 있다
2. sbt 설치 및 기본 사용법이 설명되어 있다
3. IntelliJ IDEA Scala 플러그인 설정이 설명되어 있다
4. VS Code Metals 설정이 설명되어 있다
5. build.sbt 주요 설정이 설명되어 있다
6. 자주 사용하는 sbt 명령어가 정리되어 있다

### Story 10.2: 기본 예제 문서 작성

**As a** Scala 학습자,
**I want** 종합적인 기본 예제를 따라해보고 싶다,
**So that** 학습한 개념을 실습할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/examples/basic.md`가 작성되어 있다
2. 간단한 데이터 처리 예제가 포함되어 있다
3. 케이스 클래스와 패턴 매칭 활용 예제가 포함되어 있다
4. 컬렉션 연산 예제가 포함되어 있다
5. 예제 프로젝트 실행 방법이 설명되어 있다

### Story 10.3: Scala 2 vs Scala 3 비교 예제 문서 작성

**As a** Scala 학습자,
**I want** Scala 2와 Scala 3의 차이를 코드로 비교하고 싶다,
**So that** 버전 간 차이를 명확히 이해할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/examples/scala2-vs-scala3.md`가 작성되어 있다
2. 동일한 로직을 Scala 2와 Scala 3로 구현한 비교가 있다
3. 새로운 문법(들여쓰기, enum 등) 비교가 있다
4. given/using vs implicit 비교가 있다
5. 마이그레이션 팁이 포함되어 있다

### Story 10.4: 용어 사전 작성

**As a** Scala 입문자,
**I want** Scala 용어를 빠르게 찾아볼 수 있는 사전을 원한다,
**So that** 모르는 용어를 쉽게 확인할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/appendix/glossary.md`가 작성되어 있다
2. 주요 Scala 용어가 알파벳/가나다 순으로 정리되어 있다
3. 각 용어에 간결한 정의와 설명이 포함되어 있다
4. 관련 문서 섹션으로의 링크가 포함되어 있다

### Story 10.5: Scala 2 vs Scala 3 차이점 요약 문서 작성

**As a** Scala 학습자,
**I want** Scala 2와 Scala 3의 주요 차이점을 한눈에 보고 싶다,
**So that** 마이그레이션이나 버전 선택에 참고할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/appendix/version-comparison.md`가 작성되어 있다
2. 새로운 기능(enum, union types, given/using 등)이 정리되어 있다
3. 변경된 기능이 정리되어 있다
4. 제거된 기능이 정리되어 있다
5. 마이그레이션 가이드 링크가 포함되어 있다

### Story 10.6: FAQ 및 참고 자료 작성

**As a** Scala 학습자,
**I want** 자주 묻는 질문과 추가 학습 자료를 원한다,
**So that** 더 깊이 공부할 수 있다.

**Acceptance Criteria:**
1. `content/ko/scala/appendix/faq.md`가 작성되어 있다
2. `content/ko/scala/appendix/references.md`가 작성되어 있다
3. FAQ에 자주 묻는 질문과 답변이 포함되어 있다
4. 참고 자료에 공식 문서 링크가 포함되어 있다
5. 추천 서적/강의가 포함되어 있다
6. 커뮤니티 리소스가 포함되어 있다

### Story 10.7: Scala 예제 프로젝트 완성

**As a** 개발자,
**I want** 종합적인 Scala 예제 프로젝트를 원한다,
**So that** 실제로 코드를 실행해볼 수 있다.

**Acceptance Criteria:**
1. `examples/scala/scala2-basics/`가 완성되어 있다
2. `examples/scala/scala3-basics/`가 완성되어 있다
3. 각 프로젝트에 기초/중급 개념을 활용한 예제가 포함되어 있다
4. 컬렉션, 패턴 매칭, 고차 함수 활용 예제가 있다
5. 각 예제에 주석과 설명이 포함되어 있다
6. README.md에 전체 실행 방법이 설명되어 있다

---

## Technical Constraints and Integration

### Existing Technology Stack Integration

**Languages**: Scala 2.13, Scala 3 (새로 추가) + 기존 Java 17
**Build Tools**: sbt (새로 추가) + 기존 Gradle
**Frameworks**: Scala Standard Library (새로 추가)
**Infrastructure**: 기존 Hugo + GitHub Pages + GitHub Actions 그대로 사용

### Integration Approach

**Hugo Integration**: 기존 `content/ko/` 구조에 `scala/` 디렉토리 추가
**Navigation Integration**: 기존 메뉴 구조에 Scala 가이드 항목 추가
**Build Integration**: 기존 Hugo 빌드 프로세스 그대로 사용
**Testing Integration**: Scala 예제는 sbt test로 독립 테스트

### Code Organization

**File Structure**: 기존 Kafka/DDD 가이드와 동일한 패턴 사용
**Naming Conventions**: 기존 문서 명명 규칙 준수
**Documentation Standards**: 기존 Hugo frontmatter 형식 준수

---

## Next Steps

### Architect Prompt

이 Brownfield Enhancement PRD를 기반으로 Architecture 문서를 업데이트해 주세요:
- Scala 예제 프로젝트 구조 설계 (sbt)
- Scala 2와 Scala 3 프로젝트 구성 방법
- 기존 아키텍처와의 통합 방안

### Development Prompt

Epic 6부터 순차적으로 Story를 구현해 주세요. 각 Story는 독립적으로 완료 가능하며, Story 완료 시 GitHub에 커밋해 주세요.

---

*Generated from Project Brief: Kafka Guidance 101*
*Brownfield Enhancement: Scala Guide Addition*
