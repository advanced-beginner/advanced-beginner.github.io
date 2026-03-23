---
name: site-quality
description: "사이트 품질 종합 관리 오케스트레이터. 5개 전문 에이전트를 조율하여 기술적 오류, SEO, 도메인 연결, 번역, 콘텐츠 품질을 체계적으로 개선한다. 트리거: 사이트 품질, 전체 점검, 종합 개선, site-quality"
---

# Site Quality — 종합 품질 관리 오케스트레이터

## 개요

5개 전문 에이전트를 조율하여 사이트 품질을 체계적으로 개선합니다.

## 에이전트 팀 구성

| 에이전트 | 역할 | 스킬 |
|---------|------|------|
| **site-doctor** | 기술적 오류 진단/수정 | site-doctor |
| **seo-optimizer** | 검색 최적화 | seo-optimizer |
| **cross-linker** | 도메인 간 연결 | cross-linker |
| **translator** | 한영 번역 관리 | translator |
| **content-evaluator** | 문서 품질 평가 | content-evaluator |

## 시나리오별 실행 가이드

### 시나리오 1: 전체 품질 감사 (Full Audit)
> "사이트 전체 품질을 점검해줘"

**실행 순서**:
1. **site-doctor** → 기술적 오류 진단 (빌드, 코드 블록, Mermaid, 링크)
2. **seo-optimizer** → SEO 현황 진단 (description 커버리지)
3. **translator** → 번역 동기화 현황 (ko/en 비교)
4. **content-evaluator** → 각 도메인 대표 문서 Quick 평가
5. **cross-linker** → 도메인 간 참조 매트릭스 생성

**병렬 가능**: 1~3은 독립적이므로 동시 실행 가능.
4~5는 1의 결과가 있으면 더 정확하지만 독립 실행도 가능.

### 시나리오 2: 긴급 수정 (Hotfix)
> "빌드가 깨졌어" / "배포 전 점검해줘"

**실행 순서**:
1. **site-doctor** → 빌드 에러 수정
2. **site-doctor** → Mermaid/코드 블록 검증
3. Hugo 빌드 성공 확인

### 시나리오 3: 신규 문서 품질 게이트
> "새로 추가한 문서 품질 확인해줘"

**실행 순서**:
1. **site-doctor** → 기술적 오류 점검 (해당 파일만)
2. **content-evaluator** → Quick 평가 적용
3. **seo-optimizer** → description 유무 확인
4. **translator** → 영문 버전 필요 여부 판단

### 시나리오 4: SEO 강화 캠페인
> "검색 노출을 개선하고 싶어"

**실행 순서**:
1. **seo-optimizer** → description 일괄 추가
2. **seo-optimizer** → Naver/Bing 검증 코드 추가
3. **cross-linker** → 내부 링크 강화 (SEO에도 기여)

### 시나리오 5: 도메인 확장 후 정비
> "새 도메인 문서를 대량 추가했어"

**실행 순서**:
1. **site-doctor** → 기술적 오류 일괄 점검
2. **content-evaluator** → 배치 Quick 평가
3. **seo-optimizer** → description 일괄 추가
4. **cross-linker** → 기존 도메인과의 연결점 탐색
5. **translator** → 영문 번역 배치 생성

### 시나리오 6: 정기 품질 유지보수
> "분기별 품질 점검"

**실행 순서**:
1. **content-evaluator** → 전체 도메인 Quick 평가 (도메인당 대표 3개)
2. **site-doctor** → 기술적 오류 스캔
3. **translator** → lastmod 비교로 업데이트 필요 번역 식별
4. **cross-linker** → 새로 추가된 문서의 크로스 참조 보완

## 에이전트 간 데이터 흐름

```
site-doctor ──[기술 오류 목록]──→ content-evaluator
                                       │
seo-optimizer ──[description 없는 문서]─┘
                                       │
content-evaluator ──[낮은 점수 문서]──→ cross-linker (연결 부족)
                          │
                          └──→ translator (영문 미동기화)
```

## 실행 원칙

1. **진단 먼저**: 수정 전 반드시 현황을 수치로 파악한다
2. **병렬 최대화**: 독립적인 에이전트는 동시에 실행한다
3. **빌드 검증 필수**: 모든 수정 후 `hugo --gc --minify`로 확인한다
4. **점진적 개선**: 한 번에 모든 것을 고치지 않고, 우선순위별로 진행한다
5. **커밋 분리**: 에이전트별 또는 주제별로 커밋을 분리한다

## 품질 대시보드 지표

정기 점검 시 추적할 핵심 지표:

| 지표 | 목표 | 측정 방법 |
|------|------|----------|
| Hugo 빌드 | 0 에러 | `hugo --gc --minify` |
| 코드 블록 언어 지정률 | 95%+ | grep 미지정 블록 |
| SEO description 커버리지 | 100% | grep description |
| 번역 완성도 | 95%+ | ko/en 파일 비교 |
| 도메인 간 참조 수 | 30+ | grep relref cross-domain |
| Quick 평가 Pass율 | 80%+ | content-evaluator 결과 |
