---
name: markup-check
description: "마크다운 및 Mermaid 표기 정합성 자동 검증 및 수정. 한글 인라인 강조 오류(**bold**한글), Mermaid 구문 위반(<br/>, 색상 스타일), 코드 블록 언어 미지정을 탐지하고 수정한다. 트리거: 마크다운 검사, 표기 정합성, markup check, mermaid 검사, 인라인 강조"
---

# Markup Check — 표기 정합성 검증 스킬

## 워크플로우

### Step 1: 한글 인라인 강조 오류 탐지

Hugo의 Goldmark 파서에서 `**bold**한글`처럼 강조 마커 직후 한글(또는 CJK 문자)이 공백 없이 이어지면 렌더링이 깨진다. 이는 CommonMark 스펙의 "flanking delimiter run" 규칙 때문이다.

**탐지 명령:**
```bash
# Bold 직후 한글
grep -rn '\*\*[^*]*\*\*[가-힣]' content/ko/docs/ --include="*.md"
# Italic 직후 한글 (단, bold의 일부가 아닌 것)
grep -rn '[^*]\*[^*]*\*[가-힣]' content/ko/docs/ --include="*.md"
# Inline code 직후 한글
grep -rn '`[^`]*`[가-힣]' content/ko/docs/ --include="*.md"
```

**수정 규칙:**
- `**text**한글` → `**text** 한글` (공백 1개 삽입)
- `` `code`한글 `` → `` `code` 한글 ``
- 예외: 괄호 내 (`(**text**)`) 또는 조사 연결 시 의미 변형이 되는 경우는 수동 확인 목록에 추가

### Step 2: Mermaid 구문 검증

CLAUDE.md 규칙 기반 검사:

```bash
# <br/> 사용 (→ <br>로 교체)
grep -rn '<br/>' content/ko/docs/ --include="*.md"
# 색상 스타일 사용
grep -rn 'style\s.*fill:' content/ko/docs/ --include="*.md"
grep -rn 'classDef.*fill:' content/ko/docs/ --include="*.md"
# \n 줄바꿈 (→ <br>로 교체)
grep -rn '\\n' content/ko/docs/ --include="*.md"  # Mermaid 블록 내에서만
```

### Step 3: 코드 블록 언어 미지정

```bash
grep -rn '^\`\`\`$' content/ko/docs/ --include="*.md"
```

컨텍스트 기반 언어 추론:
- Java import/class → `java`
- kubectl/docker/curl → `bash`
- key: value → `yaml`
- SELECT/INSERT → `sql`
- def/val/case class → `scala`
- 확신 없으면 `text`로 지정 + 수동 확인 목록

### Step 4: 수정 적용

발견된 이슈를 Edit 도구로 수정한다. 수정 내역을 `_workspace/markup_check_log.md`에 저장한다.

## 출력

`_workspace/markup_check_report.md`에 결과 저장.
