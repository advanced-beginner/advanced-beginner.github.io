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
- **테마**: hugo-book (git submodule)

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
└── docs/             # hugo-book 테마용 문서 섹션
    ├── kafka/        # Kafka 가이드
    ├── ddd/          # DDD 가이드
    ├── kubernetes/   # Kubernetes 가이드
    ├── elasticsearch/ # Elasticsearch 가이드
    ├── scala/        # Scala 가이드
    ├── spark/        # Spark 가이드
    └── observability/ # Observability 가이드
examples/             # Spring Boot Kafka 예제
├── quick-start/      # 최소 설정 예제
└── order-system/     # 도메인 주도 설계 예제
docker/               # Kafka Docker Compose (KRaft, Zookeeper 없음)
docs/                 # 프로젝트 문서 (PRD, 아키텍처)
themes/               # Hugo 테마 (git submodule)
layouts/partials/     # SEO, GTM, Mermaid 커스텀 설정 (docs/inject/)
EVALUATION.md         # 문서 품질 평가 프레임워크 (Diátaxis 기반, v2.2)
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

## 마크다운 작성 규칙

### 헤더

`#`, `##`, `###` 뒤에 반드시 공백 필요:

```markdown
## 제목        ✅ 올바름
##제목         ❌ 헤더로 렌더링 안 됨
```

### 코드 블록

- 백틱(```) 쌍이 반드시 일치해야 함
- 언어 지정 권장: ```java, ```bash, ```yaml 등

### Hugo Shortcode

열고 닫는 태그 쌍 확인:

```markdown
{{< callout type="info" >}}...{{< /callout >}}                 ✅
{{< callout type="warning" title="제목" >}}...{{< /callout >}} ✅ (title 속성 지원)
{{< tabs >}}...{{< /tabs >}}                                    ✅
{{% notice style="tip" %}}...{{% /notice %}}                   ❌ (callout으로 통일)
```

### Hugo 링크

relref 사용 시 `/docs/` 구조에 맞는 경로:

```markdown
[링크]({{< relref "/docs/kafka/concepts/..." >}})   ✅
[링크]({{< relref "/kafka/concepts/..." >}})        ❌
```

### Frontmatter

- 모든 파일에 `---`로 시작/종료
- 같은 디렉토리 내 `weight` 값 중복 금지
- `lastmod` 날짜 형식: `"YYYY-MM-DD"`

## Mermaid 다이어그램 규칙

### 노드 내 줄바꿈

`\n`은 텍스트로 노출됨. `<br>` 사용:

```mermaid
# ❌ 잘못된 예
Q1{Consumer\n살아있음?}

# ✅ 올바른 예
Q1{Consumer<br>살아있음?}
```

### 특수문자

Mermaid에서 일부 문자는 특수 의미를 가지므로 HTML 엔터티 사용:

| 원본 | 대체 | 설명 |
|------|------|------|
| `[` | `&#91;` | 대괄호 (노드 정의와 충돌 방지) |
| `]` | `&#93;` | 대괄호 |
| `<` | `&lt;` | 꺾쇠 (HTML 태그와 충돌 방지) |
| `>` | `&gt;` | 꺾쇠 |

예시:
```mermaid
# 중첩 대괄호 표현
A["배열 &#91;1,2,3&#93;"]
```

### Self-closing 태그

`<br/>` 대신 `<br>` 사용 (Mermaid 11.x 호환):

```mermaid
# ❌ 잘못된 예
A["Line1<br/>Line2"]

# ✅ 올바른 예
A["Line1<br>Line2"]
```

### 스타일

- 색상 스타일 (`style`, `fill`, `stroke`) 사용 금지 (테마 호환성)
- `classDef`로 정의된 색상 클래스도 동일하게 금지
- 단순 나열형 다이어그램은 텍스트로 대체 권장

### 캡션 (접근성)

모든 Mermaid 다이어그램 직후에 한 줄 캡션을 추가합니다. 형식: `*그림: <한 문장 설명>*`. 스크린 리더 사용자도 다이어그램 내용을 이해할 수 있게 합니다.
