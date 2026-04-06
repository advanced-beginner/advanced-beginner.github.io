---
name: doc-deep-review
description: "문서 심층 리뷰 오케스트레이터. EVALUATION.md 평가 + 독자 공감 분석 + 실제 개선을 3단계로 조율한다. 특정 도메인의 전체 문서를 체계적으로 검토하여 독자 친화적 가이드로 만든다. 트리거: 문서 심층 리뷰, 전체 문서 검토, 문서 개선 프로젝트, 친절한 가이드, doc deep review, 도메인 문서 점검"
---

# Doc Deep Review — 문서 심층 리뷰 오케스트레이터

## 개요

하나의 도메인 문서 전체를 **평가 → 분석 → 개선** 3단계로 체계적으로 리뷰한다.

기존 site-quality 오케스트레이터가 "사이트 전체의 기술적 품질"을 관리한다면, 이 오케스트레이터는 **"특정 도메인 문서의 독자 이해도"**에 집중한다.

## 에이전트 팀 구성

| 에이전트 | 빌트인 타입 | 역할 | 스킬 |
|---------|-----------|------|------|
| **content-evaluator** | general-purpose | EVALUATION.md 기반 배치 평가 | content-evaluator |
| **reader-empathy-analyst** | general-purpose | 독자 관점 이해도 장벽 분석 | reader-empathy-analysis |
| **doc-improver** | general-purpose | 분석 기반 문서 개선 실행 | doc-improvement |

## 실행 워크플로우

### Phase 0: 준비

1. 대상 도메인 경로 확인 (예: `content/ko/docs/spark/`)
2. `_workspace/` 디렉토리 생성
3. 대상 파일 목록과 학습 순서(weight) 매핑

### Phase 1: 평가 + 분석 (팬아웃 — 병렬)

두 에이전트를 동시에 실행한다:

**content-evaluator** (배경 실행):
```
대상: content/ko/docs/{domain}/ 전체 문서
작업: EVALUATION.md 기반 배치 Quick 평가
산출물: _workspace/01_evaluation_results.md
- 문서별 Quick 평가 결과 (Pass/조건부/Fail)
- Fail 문서에 대해 상세 평가 (점수 + 개선 제안)
- 도메인 전체 품질 요약
```

**reader-empathy-analyst** (배경 실행):
```
대상: content/ko/docs/{domain}/ 전체 문서
작업: 6가지 장벽 프레임워크로 독자 경험 분석
산출물:
- _workspace/02_reader_barriers_by_file.md (문서별 장벽)
- _workspace/02_reader_barriers_summary.md (종합 패턴)
```

### Phase 2: 개선 계획 수립 (팬인)

Phase 1의 두 산출물을 통합하여 개선 우선순위를 결정한다:

1. `_workspace/01_evaluation_results.md` 읽기
2. `_workspace/02_reader_barriers_by_file.md` 읽기
3. `_workspace/02_reader_barriers_summary.md` 읽기
4. 교차 분석으로 우선순위 매트릭스 생성
5. `_workspace/03_improvement_plan.md` 작성

**우선순위 기준**:
| 조건 | 우선순위 |
|------|---------|
| 평가 Fail + 장벽 심각도 상 | 1순위 |
| 장벽 심각도 상 (평가 무관) | 2순위 |
| 평가 Fail + 장벽 심각도 중 | 3순위 |
| 도메인 전체 반복 패턴 | 일괄 개선 |

**사용자 확인 게이트**: 개선 계획을 사용자에게 보고하고 승인을 받은 후 Phase 3 진행.

### Phase 3: 개선 실행

**doc-improver** 실행:
```
입력: _workspace/03_improvement_plan.md
작업: 우선순위 순서로 문서 수정
산출물:
- _workspace/04_improvement_log.md (수정 내역)
- _workspace/04_improvement_report.md (종합 보고서)
```

### Phase 4: 검증

1. Hugo 빌드 확인: `hugo --gc --minify` 에러 없음
2. 수정된 문서에 대해 content-evaluator 재평가 (Before/After 비교)
3. 최종 보고서 작성

## 데이터 흐름

```
[Phase 1 - 병렬]
content-evaluator ──→ _workspace/01_evaluation_results.md
reader-empathy-analyst ──→ _workspace/02_reader_barriers_*.md

[Phase 2 - 통합]
오케스트레이터가 01 + 02를 읽고 → _workspace/03_improvement_plan.md
                                    ↓
                              [사용자 승인]
                                    ↓
[Phase 3 - 순차]
doc-improver ──→ _workspace/04_improvement_*.md

[Phase 4 - 검증]
Hugo 빌드 확인 + 재평가
```

## 에이전트 간 통신

| 발신 | 수신 | 방식 | 내용 |
|------|------|------|------|
| content-evaluator | 오케스트레이터 | 파일 | 평가 결과 |
| reader-empathy-analyst | 오케스트레이터 | 파일 | 장벽 분석 |
| 오케스트레이터 | doc-improver | 파일 | 개선 계획 |
| doc-improver | 오케스트레이터 | 파일 | 수정 내역/보고서 |

## 에러 핸들링

| 상황 | 대응 |
|------|------|
| 에이전트 실패 | 1회 재시도 후 재실패 시 해당 산출물 없이 진행, 보고서에 누락 명시 |
| 평가와 장벽 분석 상충 | 두 관점 모두 보존하여 사용자에게 판단 위임 |
| Hugo 빌드 실패 | 수정 롤백 후 원인 파악, 해당 파일 수정 건너뛰기 |
| 파일 수정 충돌 | 한 번에 한 파일씩 순차 수정 (병렬 수정 금지) |

## 테스트 시나리오

### 정상 흐름
1. `content/ko/docs/spark/` 대상으로 실행
2. Phase 1에서 evaluator와 analyst가 병렬 실행
3. Phase 2에서 두 산출물 통합하여 개선 계획 생성
4. 사용자 승인 후 Phase 3에서 문서 수정
5. Phase 4에서 빌드 확인 및 재평가

### 에러 흐름
1. Phase 1에서 reader-empathy-analyst가 실패
2. evaluator 결과만으로 개선 계획 수립 (장벽 분석 없이)
3. 보고서에 "독자 분석 미완료" 명시
4. 나머지 워크플로우 정상 진행

## 실행 방법

오케스트레이터가 직접 에이전트를 호출한다 (서브 에이전트 모드):

```
Phase 1: Agent(content-evaluator, run_in_background=true, model="opus")
         Agent(reader-empathy-analyst, run_in_background=true, model="opus")
Phase 2: 오케스트레이터가 산출물 통합 + 사용자 보고
Phase 3: Agent(doc-improver, model="opus")
Phase 4: Bash(hugo --gc --minify) + 재평가
```
