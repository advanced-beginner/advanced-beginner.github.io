---
name: seo-optimizer
description: "SEO 메타데이터 보강. frontmatter description 일괄 추가, Naver/Bing 검증 코드, 구조화 데이터 점검을 수행한다. 트리거: SEO 개선, description 추가, 검색 최적화, seo-optimizer"
---

# SEO Optimizer — 검색 최적화

## 워크플로우

### Step 1: 현황 진단
```bash
# description 있는 문서 수
grep -rl '^description:' content/ko/docs/ --include="*.md" | wc -l
# 전체 문서 수
find content/ko/docs/ -name "*.md" | wc -l
```

### Step 2: description 없는 문서 목록 추출
description 필드가 없는 .md 파일을 식별한다.
각 파일의 title과 본문 첫 단락을 읽어 description 초안을 생성한다.

### Step 3: description 작성 규칙

**형식**: 한글 50~80자, 문서의 핵심 가치를 한 문장으로 요약

**유형별 패턴**:
- 튜토리얼: "[주제]를 단계별로 학습합니다. [결과물]을 만들어봅니다."
- 하우투: "[문제]를 진단하고 해결하는 방법을 안내합니다."
- 설명: "[개념]의 작동 원리와 설계 의도를 설명합니다."
- 레퍼런스: "[대상]의 설정 옵션과 API를 정리합니다."
- 부록: "[도메인] 관련 용어 사전과 참고 자료입니다."

**금지 사항**:
- "이 문서는..." 으로 시작하지 않는다
- 키워드를 나열하지 않는다
- 100자를 넘기지 않는다

### Step 4: frontmatter에 description 삽입
title 필드 바로 아래에 description을 추가한다.
기존 필드 순서를 유지하며 삽입한다.

### Step 5: 검색엔진 등록 점검
- `layouts/partials/docs/inject/head.html`에서 검증 코드 확인
- Google: ✓ (이미 설정)
- Naver: 코드 추가 필요
- Bing: 코드 추가 필요

### Step 6: 영문 문서 동기화
content/en/ 문서에도 영문 description을 추가한다.

## 도구 사용법

- **Grep**: description 유무 탐지
- **Read**: 문서 내용 파악하여 description 생성
- **Edit**: frontmatter에 description 삽입
- **Bash**: 통계 산출

## 출력 규칙

- 추가 전/후 커버리지를 수치로 보고
- 추가된 description 전체 목록을 파일명과 함께 출력
- 도메인별 커버리지 비율 표시
