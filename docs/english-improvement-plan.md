# 영어 콘텐츠 개선 계획

**기준 문서:** english-evaluation-report.md
**작성일:** 2026-01-07

---

## 개선 작업 요약

| 우선순위 | 작업 수 | 예상 영향 |
|----------|---------|----------|
| High (즉시) | 3개 | 메타데이터 완성, 코드 품질 |
| Medium (중기) | 4개 | 콘텐츠 보강 |
| Low (장기) | 3개 | 확장성 |

---

## Phase 1: 즉시 개선 (High Priority)

### 1.1 lastmod 필드 추가

**문제:** 25개 파일에 `lastmod` 필드 누락
**영향:** 메타 정보 신뢰성 저하 (F2 항목)

**대상 파일:**

```
content/en/_index.md
content/en/ddd/_index.md
content/en/ddd/appendix/_index.md
content/en/ddd/appendix/faq.md
content/en/ddd/appendix/glossary.md
content/en/ddd/appendix/references.md
content/en/ddd/concepts/_index.md
content/en/ddd/concepts/aggregate.md
content/en/ddd/concepts/anti-patterns.md
content/en/ddd/concepts/architecture.md
content/en/ddd/concepts/clean-architecture.md
content/en/ddd/concepts/cqrs.md
content/en/ddd/concepts/domain-events.md
content/en/ddd/concepts/hexagonal-architecture.md
content/en/ddd/concepts/layered-architecture.md
content/en/ddd/concepts/onion-architecture.md
content/en/ddd/concepts/strategic-design.md
content/en/ddd/concepts/tactical-design.md
content/en/ddd/concepts/testing.md
content/en/ddd/examples/_index.md
content/en/ddd/examples/application-layer.md
content/en/ddd/examples/order-domain.md
content/en/ddd/examples/setup.md
content/en/ddd/quick-start/_index.md
content/en/scala/appendix/_index.md
```

**작업:**
- [ ] 각 파일 frontmatter에 `lastmod: "2026-01-07"` 추가

---

### 1.2 코드 예제 import 문 검토

**문제:** 일부 코드 예제에서 import 문 누락
**영향:** 코드 실행 가능성 저하 (C1 항목)

**검토 대상:**
- [ ] `content/en/kafka/concepts/*.md` - Spring Kafka import 확인
- [ ] `content/en/ddd/concepts/*.md` - Domain 클래스 import 확인
- [ ] `content/en/scala/concepts/*.md` - Scala import 확인

**작업 기준:**
- 복사-붙여넣기로 바로 실행 가능해야 함
- 필요한 경우 코드 상단에 주석으로 `// Required imports:` 추가

---

### 1.3 언어 품질 검토 (샘플 기반)

**문제:** 일부 직역 표현 존재 가능성
**영향:** 자연스러움 저하

**우선 검토 대상:**
1. [ ] `content/en/_index.md` (홈페이지)
2. [ ] `content/en/kafka/quick-start/_index.md`
3. [ ] `content/en/ddd/quick-start/_index.md`
4. [ ] `content/en/scala/quick-start/_index.md`

**검토 기준:**
- 관사 사용 (a/an/the) 적절성
- 전치사 선택
- 어색한 직역 표현

---

## Phase 2: 중기 개선 (Medium Priority)

### 2.1 긴 문서 분할 검토

**대상:**
- `content/en/ddd/concepts/aggregate.md` (508줄)

**고려 사항:**
- "Aggregate 기본" vs "Aggregate 실전 패턴" 분리 가능성
- 현재 구조가 학습 흐름에 적합한지 검토

---

### 2.2 FAQ 보강

**현황:**
- DDD FAQ: 상세함 (✅)
- Kafka FAQ: 추가 검토 필요
- Scala FAQ: 추가 검토 필요

**작업:**
- [ ] Kafka FAQ에 실무 질문 5-10개 추가
- [ ] Scala FAQ에 버전 관련 질문 추가 (Scala 2 vs 3)

---

### 2.3 저자/기여자 정보 추가

**옵션:**
1. 각 문서 하단에 기여자 섹션
2. `/about/` 페이지 생성
3. README에 명시

**권장:** README + Hugo config에서 author 설정

---

### 2.4 References 링크 검증

**작업:**
- [ ] `content/en/kafka/appendix/references.md` 링크 유효성 확인
- [ ] `content/en/ddd/appendix/references.md` 링크 유효성 확인
- [ ] `content/en/scala/appendix/references.md` 링크 유효성 확인

---

## Phase 3: 장기 개선 (Low Priority)

### 3.1 추가 실무 예제

**현재:** Order System 중심
**확장 가능:**
- 결제 시스템 예제
- 재고 관리 예제
- 알림 시스템 예제

---

### 3.2 심화 콘텐츠

**후보:**
- Kafka Streams 가이드
- Event Sourcing 심화
- Scala 3 Macro 심화

---

### 3.3 다국어 동기화 체계

**목표:** 한글/영어 버전 변경 시 동기화 추적
**방법:**
- 문서별 버전 태그 도입
- 변경 추적 스크립트

---

## 실행 체크리스트

### Phase 1 체크리스트

```
[ ] 1.1 lastmod 필드 추가 (25개 파일)
[ ] 1.2 코드 import 검토 (Kafka, DDD, Scala concepts)
[ ] 1.3 Quick Start 4개 문서 언어 검토
```

### Phase 2 체크리스트

```
[ ] 2.1 aggregate.md 분할 여부 결정
[ ] 2.2 Kafka/Scala FAQ 보강
[ ] 2.3 저자 정보 추가 방식 결정
[ ] 2.4 References 링크 검증
```

---

## 예상 효과

개선 완료 시 예상 점수 변화:

| 섹션 | 현재 | 목표 | 변화 |
|------|------|------|------|
| A. 콘텐츠 품질 | 4.0 | 4.2 | +0.2 |
| B. 구조와 구성 | 4.4 | 4.5 | +0.1 |
| C. 코드와 예제 | 4.2 | 4.5 | +0.3 |
| D. 접근성과 사용성 | 4.0 | 4.2 | +0.2 |
| E. 보조 콘텐츠 | 4.0 | 4.3 | +0.3 |
| F. 메타 정보 | 3.7 | 4.3 | +0.6 |
| **총점** | **4.11** | **4.4** | **+0.29** |

**목표 등급: A (4.5 이상) 도달 가능**

---

## 다음 단계

1. Phase 1 작업 즉시 착수
2. 작업 완료 후 재평가
3. Phase 2 우선순위 조정
