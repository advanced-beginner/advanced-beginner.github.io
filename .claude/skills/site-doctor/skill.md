---
name: site-doctor
description: "Hugo 빌드 에러, 코드 블록 언어 미지정, Mermaid 규칙 위반, 깨진 링크를 자동 탐지하고 수정한다. 트리거: 빌드 오류, 기술 점검, lint, 코드 블록 수정, site-doctor"
---

# Site Doctor — 기술적 오류 진단 및 수정

## 워크플로우

### Step 0: 환경 준비
```bash
git submodule update --init --recursive
ls themes/hugo-book/layouts/
```
테마 submodule이 초기화되어 있는지 확인한다. 비어있으면 shortcode 존재 여부를 오판할 수 있다.

### Step 1: 빌드 진단
```bash
hugo --gc --minify 2>&1
```
에러/경고를 파싱하여 카테고리별로 분류한다.
- **HAHAHUGO 경고**: relref shortcode가 Goldmark 파서와 충돌. 테이블/glossary 내 relref를 상대경로로 변환하면 해결.
- **실제 WARN**: 깨진 링크, 존재하지 않는 파일 참조.

### Step 2: 코드 블록 언어 미지정 탐지
````
grep -rn '^\`\`\`$' content/ko/docs/ --include="*.md"
````
각 미지정 블록의 앞뒤 컨텍스트를 보고 언어를 추론한다:
- Java import/class → `java`
- docker/kubectl/curl → `bash`
- key: value 구조 → `yaml`
- { } 중괄호 + 따옴표 → `json`
- SELECT/INSERT → `sql`
- def/val/case class → `scala`
- 확신 없으면 `text`로 지정하고 수동 확인 목록에 추가

### Step 3: Mermaid 규칙 점검
CLAUDE.md의 Mermaid 규칙 기준으로 검사:
- `<br/>` 사용 여부 → `<br>`로 교체
- self-closing 태그 여부
- 색상 스타일(fill, stroke) 사용 여부
- 특수문자 이스케이프 여부 (`[`, `]`, `<`, `>`)

### Step 4: 링크 검증
```bash
grep -rn 'relref' content/ko/docs/ --include="*.md"
```
각 relref 경로가 실제 파일로 존재하는지 확인한다.

### Step 5: Shortcode 검증
```bash
grep -rn '{{<\|{{% ' content/ko/docs/ --include="*.md"
```
사용된 shortcode가 테마에서 지원하는지 확인:
- 지원: `callout`, `tabs`, `tab`, `columns`, `details`, `mermaid`
- 미지원 가능: `hint`, `notice`, `expand` 등

### Step 6: 수정 및 검증
자동 수정 가능한 항목을 일괄 처리 → `hugo --gc --minify`로 재빌드 확인

## 도구 사용법

- **Grep**: 패턴 탐지 (코드 블록, shortcode, Mermaid)
- **Read**: 컨텍스트 확인 (코드 블록 주변 내용)
- **Edit**: 일괄 수정 (replace_all 활용)
- **Bash**: Hugo 빌드 검증

## 출력 규칙

- 수치 기반 보고 (발견 N개, 수정 N개, 수동 확인 N개)
- 수정된 파일 목록과 변경 내용을 명확히 기록
- 수동 확인이 필요한 항목은 파일:줄번호와 이유를 명시
