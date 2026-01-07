# Advanced Beginner 프로젝트 현황 문서

## 개요

| 항목 | 값 |
|------|-----|
| **프로젝트명** | Advanced Beginner (Kafka Guidance 101) |
| **유형** | Hugo 정적 사이트 (기술 문서) |
| **호스팅** | GitHub Pages |
| **URL** | https://advanced-beginner.github.io/ |
| **마지막 업데이트** | 2026-01-04 |

## 기술 스택

| 카테고리 | 기술 | 버전/설정 |
|---------|------|----------|
| 정적 사이트 생성기 | Hugo | 0.153.2+ |
| 테마 | hugo-theme-relearn | git submodule |
| 다이어그램 | Mermaid.js | 테마 내장 |
| 배포 | GitHub Actions → GitHub Pages | 자동 |
| 콘텐츠 언어 | 한국어 (ko) | 기본 |

## 디렉토리 구조

```
kafka-guidance-101/
├── .bmad-core/           # BMad 에이전트 시스템
├── .bmad-creative-writing/ # 창작 글쓰기 에이전트
├── .github/workflows/    # GitHub Actions (배포)
├── content/ko/           # 콘텐츠 (40개 마크다운)
│   ├── _index.md         # 홈페이지
│   ├── kafka/            # Apache Kafka 가이드 (20개)
│   │   ├── quick-start/
│   │   ├── concepts/     # 10개 개념 문서
│   │   ├── examples/     # 3개 예제 문서
│   │   └── appendix/     # 2개 부록
│   └── ddd/              # DDD 가이드 (19개)
│       ├── quick-start/
│       ├── concepts/     # 9개 개념 문서
│       ├── examples/     # 3개 예제 문서
│       └── appendix/     # 3개 부록
├── docker/               # Docker Compose (Kafka KRaft)
├── docs/                 # 프로젝트 문서
│   ├── prd.md            # PRD (요구사항)
│   ├── architecture.md   # 아키텍처
│   └── brief.md          # 프로젝트 브리프
├── examples/             # 실행 가능한 예제 프로젝트
│   ├── quick-start/      # Spring Boot Kafka 기본 예제
│   └── order-system/     # 주문 시스템 예제
├── themes/               # Hugo 테마 (git submodule)
└── hugo.yaml             # Hugo 설정
```

## 콘텐츠 현황

### Kafka 가이드 (20개 문서)

| 섹션 | 문서 | 상태 |
|------|------|------|
| **Quick Start** | _index.md | ✅ 완료 |
| **Concepts** | core-components.md | ✅ 완료 |
| | message-flow.md | ✅ 완료 |
| | consumer-group-offset.md | ✅ 완료 |
| | replication.md | ✅ 완료 |
| | advanced-concepts.md | ✅ 완료 |
| | transactions.md | ✅ 완료 |
| | error-handling.md | ✅ 완료 |
| | producer-tuning.md | ✅ 완료 |
| | consumer-tuning.md | ✅ 완료 |
| | monitoring.md | ✅ 완료 |
| **Examples** | setup.md | ✅ 완료 |
| | basic.md | ✅ 완료 |
| | order-system.md | ✅ 완료 |
| **Appendix** | glossary.md | ✅ 완료 |
| | references.md | ✅ 완료 |

### DDD 가이드 (19개 문서)

| 섹션 | 문서 | 상태 |
|------|------|------|
| **Quick Start** | _index.md | ✅ 완료 |
| **Concepts** | strategic-design.md | ✅ 완료 |
| | tactical-design.md | ✅ 완료 |
| | aggregate.md | ✅ 완료 |
| | domain-events.md | ✅ 완료 |
| | cqrs.md | ✅ 완료 |
| | architecture.md | ✅ 완료 |
| | testing.md | ✅ 완료 |
| | anti-patterns.md | ✅ 완료 |
| **Examples** | setup.md | ✅ 완료 |
| | order-domain.md | ✅ 완료 |
| | application-layer.md | ✅ 완료 |
| **Appendix** | glossary.md | ✅ 완료 |
| | faq.md | ✅ 완료 |
| | references.md | ✅ 완료 |

## PRD 대비 진행률

### Epic 1: 프로젝트 기반 구축 ✅ 100%
- [x] Hugo 프로젝트 초기화
- [x] 테마 설정 (hugo-theme-relearn)
- [x] GitHub Actions 배포
- [x] Mermaid.js 통합

### Epic 2: Quick Start ✅ 100%
- [x] Docker Compose 파일 (KRaft 모드)
- [x] Quick Start Spring Boot 프로젝트
- [x] Quick Start 문서

### Epic 3: 개념 이해 섹션 ✅ 100%+
- [x] 핵심 구성요소 (core-components)
- [x] 메시지 흐름 (message-flow)
- [x] Consumer Group & Offset
- [x] Replication & Leader Election
- [x] acks, Message Key, Retention
- [x] **추가:** Transactions, Error Handling
- [x] **추가:** Producer/Consumer Tuning
- [x] **추가:** Monitoring

### Epic 4: 실습 예제 ✅ 100%
- [x] 환경 구성 문서 (setup)
- [x] 기본 Producer/Consumer 예제
- [x] 주문 시스템 예제

### Epic 5: 부록 ✅ 100%
- [x] 용어 사전 (glossary)
- [x] 참고 자료 (references)

### PRD 범위 외 추가 구현
- [x] **DDD 가이드 전체** (19개 문서)
- [x] 전략적/전술적 설계, Aggregate, 도메인 이벤트
- [x] CQRS, 아키텍처 패턴, 테스트 전략
- [x] 안티패턴, FAQ

## 최근 변경사항 (Git 로그)

| 커밋 | 설명 |
|------|------|
| `ff0a0f6` | public/ 디렉토리를 git 추적에서 제거 |
| `c355328` | Mermaid 다이어그램 문법 오류 수정 |
| `ee65362` | 메인 페이지 콘텐츠 대폭 보강 |
| `5c48085` | PR #1 병합: Kafka Quick Start 개선 |
| `6614ff4` | Kafka 예제 문서 일관성 개선 |
| `7dbaeb5` | Kafka Quick Start 문서 대폭 개선 |
| `6a72f56` | DDD 아키텍처 문서 링크 경로 수정 |

## 주요 파일 참조

### 설정 파일
- **Hugo 설정**: `hugo.yaml`
- **Docker Compose**: `docker/docker-compose.yml`
- **GitHub Actions**: `.github/workflows/`

### 진입점
- **홈페이지**: `content/ko/_index.md`
- **Kafka 메인**: `content/ko/kafka/_index.md`
- **DDD 메인**: `content/ko/ddd/_index.md`

### 예제 프로젝트
- **Quick Start**: `examples/quick-start/`
- **주문 시스템**: `examples/order-system/`

## 다음 단계 권장 사항

### 1. 콘텐츠 품질 개선
- [ ] 전체 링크 유효성 검사
- [ ] 코드 예제 실행 테스트
- [ ] 문서 스타일 일관성 검토

### 2. 기능 확장 고려
- [ ] 영어 버전 추가 (i18n)
- [ ] 검색 기능 개선
- [ ] 인터랙티브 예제 추가

### 3. 유지보수
- [ ] Kafka 3.x 최신 기능 반영
- [ ] Spring Boot 3.x 호환성 확인
- [ ] 의존성 업데이트

---

*Generated: 2026-01-04 by BMad Master*
