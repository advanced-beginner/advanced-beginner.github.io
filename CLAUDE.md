# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 핵심 제약사항

1. **모든 대화와 결과물은 한글로 작성** (단, 기술 용어 및 널리 알려진 용어는 영어 허용)
2. **모호한 질문/지시에는 반드시 되물을 것**
3. **작업 시작 전 계획 수립 후 순차적으로 진행할 것**

## 프로젝트 개요

Kafka Guidance 101은 **한글 기술 문서 사이트**입니다. Hugo 정적 사이트 + Spring Boot 예제 프로젝트를 모노레포로 관리합니다.

- **사이트 URL**: https://advanced-beginner.github.io/
- **콘텐츠 언어**: 한글 (ko)
- **테마**: hugo-theme-relearn (git submodule)

## 개발 명령어

```bash
# Hugo 문서 사이트
hugo server -D              # 개발 서버 (http://localhost:1313)
hugo --gc --minify          # 프로덕션 빌드

# Kafka 인프라
cd docker && docker-compose up -d    # Kafka 시작 (KRaft 모드, 포트 9092)
docker-compose ps                     # 상태 확인
docker-compose down                   # 중지

# 예제 프로젝트 (examples/quick-start 또는 examples/order-system 디렉토리에서)
./gradlew bootRun           # Spring Boot 앱 실행 (포트 8080)
./gradlew build             # 빌드
./gradlew test              # 테스트 실행

# 메시지 전송 테스트 (quick-start 예제)
curl -X POST "http://localhost:8080/send?message=Hello"
```

## 아키텍처

### 디렉토리 구조

```
content/ko/           # 한글 문서 (메인 콘텐츠)
├── kafka/            # Kafka 가이드 (20개 문서)
└── ddd/              # DDD 가이드 (19개 문서)
examples/             # Spring Boot Kafka 예제
├── quick-start/      # 최소 설정 예제
└── order-system/     # 도메인 주도 설계 예제
docker/               # Kafka Docker Compose (KRaft, Zookeeper 없음)
docs/                 # 프로젝트 문서 (PRD, 아키텍처)
themes/               # Hugo 테마 (git submodule)
layouts/partials/     # Mermaid.js 커스텀 설정
```

### 콘텐츠 구성

각 가이드는 **Quick Start → Concepts → Examples → Appendix** 순서로 구성됩니다.

모든 마크다운 파일에는 Hugo frontmatter가 필요합니다. 다이어그램은 Mermaid.js를 사용합니다.

### 예제 프로젝트

두 예제 모두 독립적인 Spring Boot 3.2.x 프로젝트입니다 (Gradle Kotlin DSL):

- **quick-start**: 최소한의 Kafka Producer/Consumer + REST 엔드포인트
- **order-system**: 도메인 이벤트 패턴 예제

Spring Kafka 규칙:
- `KafkaTemplate` 사용 (저수준 Producer API 대신)
- `@KafkaListener`로 선언적 Consumer 구현
- 설정은 `application.yml` 형식
- Consumer Group 네이밍: `{app-name}-group`

## 기술 스택

| 구성요소 | 버전 |
|----------|------|
| Hugo | 0.153.2+ (extended) |
| Java | 17 |
| Spring Boot | 3.2.x |
| Apache Kafka | 3.6.1 (KRaft) |
| Gradle | 8.x (Kotlin DSL) |

## 배포

`main` 브랜치에 push하면 GitHub Actions가 자동으로 GitHub Pages에 배포합니다:
1. Hugo 빌드 (`--gc --minify`)
2. `public/` 디렉토리를 GitHub Pages에 배포

## BMad Method 연동

이 프로젝트는 BMad-Method 프레임워크를 사용합니다 (`.bmad-core/`). 주요 설정은 `.bmad-core/core-config.yaml`:

- PRD: `docs/prd.md`
- 아키텍처: `docs/architecture.md`
- 스토리: `docs/stories/`

BMad 에이전트 호출: `/BMad:agents:<agent-name>`

## Java 코딩 규칙

- Google Java Style Guide 준수
- 클래스: PascalCase (`OrderProducer`)
- 메서드: camelCase (`sendMessage()`)
- 상수: UPPER_SNAKE (`TOPIC_NAME`)
- 불변 데이터에는 Java Record 사용
- null 대신 `Optional` 반환
