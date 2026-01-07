# 영어 콘텐츠 검수 계획

## 1. 개요

### 1.1 목적
최근 추가된 영어 콘텐츠의 품질을 검토하여 영어권 독자가 자연스럽게 읽을 수 있도록 보장합니다.

### 1.2 검수 범위
| 가이드 | 파일 수 | 구성 |
|--------|---------|------|
| Kafka | 20개 | Quick Start, Concepts(12), Examples(3), Appendix(3) |
| DDD | 19개 | Quick Start, Concepts(12), Examples(3), Appendix(3) |
| Scala | 36개 | Quick Start, Concepts(17), Examples(3), Appendix(4) |
| **총계** | **75개** | - |

---

## 2. 검수 기준

### 2.1 언어 품질 (Language Quality)

| 항목 | 기준 | 체크포인트 |
|------|------|------------|
| **자연스러움** | 영어권 원어민이 읽기에 자연스러운가? | - 번역투 표현 제거<br>- 한국어 어순 잔재 확인 |
| **문법 정확성** | 문법적으로 올바른가? | - 시제 일관성<br>- 주어-동사 일치<br>- 관사(a/an/the) 사용 |
| **용어 일관성** | 기술 용어가 일관되게 사용되었는가? | - 동일 개념에 동일 용어<br>- 업계 표준 용어 사용 |
| **명확성** | 의미가 명확하게 전달되는가? | - 모호한 표현 제거<br>- 대명사 참조 명확성 |

### 2.2 기술 콘텐츠 품질 (Technical Quality)

| 항목 | 기준 | 체크포인트 |
|------|------|------------|
| **코드 예제** | 코드가 정확하고 실행 가능한가? | - 문법 오류<br>- 복사-붙여넣기 실행 가능성 |
| **기술 정확성** | 기술 설명이 정확한가? | - 버전 정보<br>- API 명칭 |
| **다이어그램** | 다이어그램이 명확한가? | - Mermaid 렌더링<br>- 레이블 영어화 |

---

## 3. 검수 절차

### Phase 1: 샘플 검수 (Spot Check)
각 가이드에서 대표 문서 선정하여 깊이 있는 검토 수행

**대상 문서:**
- `content/en/_index.md` (홈페이지)
- `content/en/kafka/quick-start/_index.md`
- `content/en/kafka/concepts/error-handling.md`
- `content/en/ddd/concepts/aggregate.md`
- `content/en/scala/concepts/pattern-matching.md`
- `content/en/kafka/appendix/glossary.md`
- `content/en/ddd/appendix/faq.md`

### Phase 2: 전체 검수 (Full Review)
발견된 패턴을 기반으로 전체 문서 검수

### Phase 3: 수정 및 검증
식별된 문제 수정 후 최종 검증

---

## 4. EVALUATION.md 기준 평가

### 평가 항목 요약

| 섹션 | 가중치 | 항목 수 |
|------|--------|---------|
| A. 콘텐츠 품질 | 40% | 6개 |
| B. 구조와 구성 | 20% | 5개 |
| C. 코드와 예제 | 20% | 5개 |
| D. 접근성과 사용성 | 10% | 4개 |
| E. 보조 콘텐츠 | 5% | 3개 |
| F. 메타 정보와 신뢰성 | 5% | 3개 |

---

## 5. 우선순위

### 높음 (Critical)
1. 홈페이지 (`_index.md`) - 첫인상 결정
2. Quick Start 문서 - 진입점
3. 용어 사전 (Glossary) - 용어 일관성 기준

### 중간 (High)
4. Concepts 섹션 핵심 문서
5. FAQ 문서

### 낮음 (Medium)
6. Examples 섹션
7. References

---

## 6. 일정 (예상)

| 단계 | 작업 | 상태 |
|------|------|------|
| 1 | 검수 계획 수립 | 완료 |
| 2 | EVALUATION.md 기준 평가 | 진행 중 |
| 3 | 샘플 검수 수행 | 예정 |
| 4 | 개선 사항 도출 | 예정 |
| 5 | 개선 계획 작성 | 예정 |

---

## 7. 산출물

1. `english-review-plan.md` - 본 문서
2. `english-evaluation-report.md` - EVALUATION.md 기준 평가 결과
3. `english-improvement-plan.md` - 개선 계획 및 작업 목록
