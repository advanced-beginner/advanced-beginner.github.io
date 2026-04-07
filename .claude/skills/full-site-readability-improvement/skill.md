---
name: full-site-readability-improvement
description: "전체 사이트 문서의 가독성 및 독자 이해도를 체계적으로 개선하는 엔드투엔드 오케스트레이터. 교차 도메인 전략 도출 → 개선 계획 수립 → 문서 수정 → 영문 동기화 → 검증을 5단계로 조율한다. 트리거: 전체 문서 가독성 개선, 사이트 전체 독자 이해도, full site readability, 전체 문서 개선 프로젝트, 사이트 품질 개선"
---

# Full-Site Readability Improvement — 전체 사이트 가독성 개선 오케스트레이터

## 개요

7개 도메인(kafka, ddd, kubernetes, elasticsearch, scala, spark, observability)의 **전체 문서**를 대상으로 가독성과 독자 이해도를 체계적으로 개선한다.

기존 `doc-deep-review`가 단일 도메인 심층 리뷰라면, 이 오케스트레이터는 **사이트 전체**를 교차 분석하고 통합 전략에 따라 개선한다.

## 에이전트 구성 (서브 에이전트 모드)

| 에이전트 | 빌트인 타입 | 역할 | 스킬 |
|---------|-----------|------|------|
| **readability-strategist** | general-purpose | 교차 도메인 패턴 분석 + 전략 도출 | readability-strategy |
| **doc-improver** | general-purpose | 전략 기반 문서 수정 실행 | doc-improvement |
| **translator** | general-purpose | 수정된 ko 문서의 en 동기화 | translator |

## 실행 워크플로우

### Phase 1: 전략 도출

**readability-strategist** 실행:
```
작업: 7개 도메인에서 대표 문서 샘플링 → 4축 분석 → 교차 패턴 식별 → 전략 수립
산출물: _workspace/00_readability_strategy.md
```

에이전트 프롬프트 구성:
```
readability-strategist 에이전트 정의를 읽고 (.claude/agents/readability-strategist.md),
readability-strategy 스킬을 읽은 후 (.claude/skills/readability-strategy/skill.md),
content/ko/docs/ 아래 7개 도메인을 대상으로 가독성 전략을 도출하라.
산출물을 _workspace/00_readability_strategy.md에 저장하라.
```

### Phase 2: 개선 계획 수립 (오케스트레이터 직접)

Phase 1 산출물을 읽고 구체적 개선 계획을 수립한다:

1. `_workspace/00_readability_strategy.md` 읽기
2. 전략을 **도메인별 작업 목록**으로 분해
3. 각 작업에 대상 파일, 적용할 개선 패턴(P1~P7), 예상 변경량을 명시
4. `_workspace/01_improvement_plan.md` 작성

**개선 계획 형식**:
```markdown
# 가독성 개선 계획

## 전략 요약
[Phase 1에서 도출된 핵심 전략 3~5개]

## 도메인별 작업 목록

### kafka (N개 파일)
| # | 파일 | 적용 전략 | 개선 패턴 | 예상 변경 |
|---|------|----------|----------|----------|

### ddd (N개 파일)
...

## 실행 순서
1. [임팩트 높은 도메인부터]
2. ...

## 예상 총 변경 파일 수: N개
```

**사용자 확인 게이트**: 개선 계획을 사용자에게 보고하고 승인을 받은 후 Phase 3 진행.

### Phase 3: 문서 개선 실행

**doc-improver**를 도메인별로 순차 실행한다. 도메인 순서는 Phase 2 계획의 우선순위를 따른다.

각 도메인 실행 시 프롬프트:
```
doc-improver 에이전트 정의를 읽고 (.claude/agents/doc-improver.md),
doc-improvement 스킬을 읽은 후 (.claude/skills/doc-improvement/skill.md),
_workspace/01_improvement_plan.md에서 {domain} 섹션의 작업을 실행하라.

핵심 제약:
- 이 도메인의 작업만 실행 (다른 도메인 파일 수정 금지)
- 수정 로그를 _workspace/03_{domain}_changes.md에 저장
- CLAUDE.md의 마크다운/Mermaid 규칙 준수
```

**도메인별 순차 실행 이유**: 병렬 실행 시 git 충돌 위험 (feedback_agent_conflicts 메모리 참조)

### Phase 4: 영문 동기화

**translator**를 도메인별로 순차 실행한다:

```
translator 에이전트 정의를 읽고 (.claude/agents/translator.md),
translator 스킬을 읽은 후 (.claude/skills/translator/skill.md),
_workspace/03_{domain}_changes.md를 참조하여
수정된 ko 문서에 대응하는 en 문서를 동기화하라.

규칙:
- ko 원본이 SSOT
- 변경된 부분만 en에 반영 (전체 재번역 아님)
- 동기화 로그를 _workspace/04_{domain}_translation.md에 저장
```

### Phase 5: 검증 + 커밋 + 푸시

1. **Hugo 빌드 확인**: `hugo --gc --minify` 에러 없음 확인
2. **변경 요약 보고서** 작성: `_workspace/05_final_report.md`
3. **커밋**: 변경 사항을 적절한 메시지로 커밋
4. **푸시**: 원격 저장소에 push
5. 사용자에게 최종 결과 보고

## 데이터 흐름

```
[Phase 1 - 전략]
readability-strategist ──→ _workspace/00_readability_strategy.md
                                    ↓
[Phase 2 - 계획]
오케스트레이터 ──→ _workspace/01_improvement_plan.md
                          ↓
                    [사용자 승인]
                          ↓
[Phase 3 - 개선] (도메인별 순차)
doc-improver(kafka) ──→ _workspace/03_kafka_changes.md
doc-improver(ddd) ──→ _workspace/03_ddd_changes.md
  ...
                          ↓
[Phase 4 - 번역] (도메인별 순차)
translator(kafka) ──→ _workspace/04_kafka_translation.md
translator(ddd) ──→ _workspace/04_ddd_translation.md
  ...
                          ↓
[Phase 5 - 검증 + 배포]
Hugo 빌드 확인 → 최종 보고서 → 커밋 → 푸시
```

## 에러 핸들링

| 상황 | 대응 |
|------|------|
| 전략 도출 실패 | 1회 재시도 후 재실패 시 기존 EVALUATION.md 기반 간이 전략으로 대체 |
| 특정 도메인 개선 실패 | 해당 도메인 건너뛰고 다음 도메인 진행, 보고서에 누락 명시 |
| 번역 실패 | 해당 도메인 en 동기화 건너뛰기, 보고서에 미동기화 파일 목록 기록 |
| Hugo 빌드 실패 | 마지막 성공 상태로 롤백 후 원인 파악, 문제 파일 수정 재시도 |
| git 충돌 | 도메인별 순차 실행이므로 발생 불가 (방지 설계) |

## 테스트 시나리오

### 정상 흐름
1. Phase 1: strategist가 7개 도메인에서 각 3~5개 문서 샘플링, 교차 분석 후 전략 도출
2. Phase 2: 전략을 도메인별 작업 목록으로 분해, 사용자 승인
3. Phase 3: kafka → ddd → ... 순서로 doc-improver 실행
4. Phase 4: 각 도메인 수정 파일에 대해 translator 실행
5. Phase 5: Hugo 빌드 성공, 커밋 + 푸시

### 에러 흐름
1. Phase 3에서 elasticsearch 도메인 개선 실패
2. 보고서에 "elasticsearch 미개선" 명시
3. 나머지 도메인은 정상 진행
4. Phase 4에서 elasticsearch 제외하고 번역
5. 최종 보고서에 "elasticsearch: 개선 미적용, en 미동기화" 명시

## 실행 방법

```
Phase 1: Agent(readability-strategist, model="opus")
Phase 2: 오케스트레이터 직접 (산출물 통합 + 사용자 보고)
Phase 3: for domain in [우선순위순]:
           Agent(doc-improver, model="opus", prompt="{domain} 작업")
Phase 4: for domain in [수정된 도메인]:
           Agent(translator, model="opus", prompt="{domain} en 동기화")
Phase 5: Bash(hugo --gc --minify) + git commit + git push
```
