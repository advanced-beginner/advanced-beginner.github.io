# Advanced Beginner

[![Deploy Hugo](https://github.com/advanced-beginner/advanced-beginner.github.io/actions/workflows/hugo.yaml/badge.svg)](https://github.com/advanced-beginner/advanced-beginner.github.io/actions/workflows/hugo.yaml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **기초를 넘어 실무로** — 핵심 원리부터 이해하는 기술 가이드

튜토리얼로 "Hello World"는 만들어봤지만, **왜 이렇게 해야 하는지** 모르겠다면 이 가이드가 그 간극을 메워드립니다.

**[📖 가이드 바로가기](https://advanced-beginner.github.io/)**

---

## 제공하는 가이드

| 가이드 | 설명 | 상태 |
|--------|------|------|
| **[Apache Kafka](https://advanced-beginner.github.io/ko/kafka/)** | 분산 메시징 시스템의 실무 활용. Producer/Consumer부터 트랜잭션, 성능 튜닝까지 | ✅ 완료 |
| **[Domain-Driven Design](https://advanced-beginner.github.io/ko/ddd/)** | 복잡한 비즈니스 로직을 체계적으로 다루는 설계 방법론 | ✅ 완료 |
| **[Scala](https://advanced-beginner.github.io/ko/scala/)** | Scala 2.13 & Scala 3 문법 가이드. 함수형 프로그래밍부터 동시성까지 | ✅ 완료 |

---

## 이런 분께 적합합니다

- 기본 문법은 알지만 **실무 적용이 막막한** 분
- "어떻게"보다 **"왜"** 를 알고 싶은 분
- **설계 원칙과 트레이드오프**를 이해하고 싶은 분

---

## 빠른 시작

### 문서 사이트 로컬 실행

```bash
# 저장소 클론
git clone --recurse-submodules https://github.com/advanced-beginner/advanced-beginner.github.io.git
cd advanced-beginner

# Hugo 개발 서버 실행
hugo server -D

# 브라우저에서 http://localhost:1313 접속
```

### Kafka 예제 실행

```bash
# Kafka 시작 (Docker)
cd docker && docker-compose up -d

# 예제 프로젝트 실행
cd ../examples/quick-start
./gradlew bootRun

# 메시지 전송 테스트
curl -X POST "http://localhost:8080/send?message=Hello"
```

### Scala 예제 실행

```bash
cd examples/scala/scala3-basics
sbt run
```

---

## 프로젝트 구조

```
advanced-beginner/
├── content/ko/              # 문서 소스 (Markdown)
│   ├── kafka/               # Kafka 가이드 (12개 문서)
│   ├── ddd/                 # DDD 가이드 (19개 문서)
│   └── scala/               # Scala 가이드 (29개 문서)
├── examples/                # 실행 가능한 예제 프로젝트
│   ├── quick-start/         # Kafka 최소 설정 예제
│   ├── order-system/        # Kafka 도메인 이벤트 예제
│   └── scala/               # Scala 예제
│       ├── scala2-basics/   # Scala 2.13 예제
│       └── scala3-basics/   # Scala 3 예제
├── docker/                  # Kafka Docker Compose (KRaft)
├── themes/                  # Hugo 테마 (git submodule)
└── .github/workflows/       # GitHub Actions 배포
```

---

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| 문서 | Hugo + hugo-theme-relearn | 0.153+ |
| 다이어그램 | Mermaid.js | 11.x |
| 예제 (Java) | Spring Boot | 3.2.x |
| 예제 (Scala) | Scala 2.13 / Scala 3 | 2.13.x / 3.x |
| 메시징 | Apache Kafka (KRaft) | 3.6.x |

---

## 사전 요구사항

- **Hugo** (Extended) 0.153.2+ — [설치 가이드](https://gohugo.io/installation/)
- **Docker & Docker Compose** — Kafka 실행용
- **Java 17+** — Spring Boot 예제용
- **sbt** — Scala 예제용 (선택)

---

## 배포

`main` 브랜치에 push하면 GitHub Actions가 자동으로 GitHub Pages에 배포합니다.

---

## 기여

이슈 및 PR 환영합니다.

1. Fork
2. Feature 브랜치 생성 (`git checkout -b feature/amazing`)
3. 커밋 (`git commit -m 'feat: amazing feature'`)
4. Push (`git push origin feature/amazing`)
5. Pull Request 생성

---

## 라이선스

[MIT License](LICENSE)
