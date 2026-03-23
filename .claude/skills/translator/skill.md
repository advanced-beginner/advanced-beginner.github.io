---
name: translator
description: "한영 번역 관리. ko/en 문서 동기화, 미번역 문서 번역, 번역 품질 관리를 수행한다. 트리거: 번역, 영문 동기화, en 문서, translation, translator"
---

# Translator — 한영 번역 관리

## 워크플로우

### Step 1: 동기화 현황 파악
```bash
# ko 문서 목록
find content/ko/docs/ -name "*.md" | sort > /tmp/ko_files.txt
# en 문서 목록
find content/en/docs/ -name "*.md" | sort > /tmp/en_files.txt
# 차이 비교 (ko에만 있는 파일 = 미번역)
diff /tmp/ko_files.txt /tmp/en_files.txt
```

ko 경로를 en 경로로 변환하여 1:1 대응을 확인한다.

### Step 2: 미번역 문서 우선순위 결정

우선순위 기준:
1. **Quick Start**: 진입점이므로 최우선
2. **Concepts (weight 낮은 순)**: 기초 개념부터
3. **Examples**: 실습 예제
4. **How-To**: 문제 해결 가이드
5. **Appendix**: 참고 자료

### Step 3: 번역 수행

각 문서에 대해:
1. ko 원본을 읽는다
2. 영문으로 번역한다
3. content/en/ 동일 경로에 파일을 생성한다

### 번역 규칙

**변환하는 것**:
- 본문 텍스트 → 영문
- title, description → 영문
- Mermaid 노드/에지 레이블 → 영문
- 코드 주석 → 영문
- 표 내 텍스트 → 영문

**유지하는 것**:
- frontmatter 구조 (weight, lastmod, bookCollapseSection 등)
- 코드 블록 내용 (코드 자체는 번역하지 않음)
- Hugo shortcode 문법
- relref 경로 (ko → en 자동 매핑)
- Mermaid 다이어그램 구조

**번역 스타일**:
- 기술 문서 영문체 (간결, 명확, 능동태)
- "You" 주어 사용 (2인칭)
- 한글 고유 비유가 있으면 영어권에 맞게 조정

### Step 4: 번역 검증

- 빌드 검증: `hugo --gc --minify`
- relref 링크가 en 경로에서도 유효한지 확인
- Mermaid 다이어그램 렌더링 확인

### Step 5: lastmod 동기화 확인

ko 원본의 lastmod가 en 번역보다 최신이면 업데이트가 필요한 문서로 플래그한다.

## 도구 사용법

- **Bash**: 파일 목록 비교, 통계
- **Read**: ko 원본 읽기
- **Write**: en 번역 파일 생성
- **Bash**: Hugo 빌드 검증

## 출력 규칙

- 도메인별 번역 완성도 표 (before/after)
- 이번 배치에서 번역한 문서 목록
- 업데이트 필요(ko가 더 최신) 문서 목록
