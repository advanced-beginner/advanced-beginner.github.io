---
name: link-check
description: "내부 링크 정합성 자동 검증. relref 경로 존재 확인, 상대 링크 대상 파일 확인, 링크 텍스트와 대상 문서 내용 매칭을 검증한다. 트리거: 링크 검사, link check, 깨진 링크, 내부 링크 검증, relref 검증"
---

# Link Check — 내부 링크 정합성 검증 스킬

## 워크플로우

### Step 1: relref 링크 추출 및 검증

```bash
# 모든 relref 추출
grep -rn 'relref' content/ko/docs/ --include="*.md"
```

각 relref 경로에 대해:
1. 경로에서 파일 경로 추출
2. `content/ko/` 기준으로 실제 파일 존재 여부 확인
3. 존재하지 않으면 깨진 링크로 보고

### Step 2: 상대 링크 검증

```bash
# 마크다운 링크 추출 (relref 아닌 것)
grep -rn '\[.*\](\.\.*/\|[a-z].*/)' content/ko/docs/ --include="*.md"
```

각 상대 링크에 대해:
1. 링크 소스 파일의 디렉토리 기준으로 대상 경로 해석
2. 대상 파일/디렉토리 존재 여부 확인
3. `_index.md` 존재 여부 함께 확인

### Step 3: 링크-내용 매칭 검증

깨지지 않은 링크에 대해:
1. 링크 텍스트 추출
2. 대상 문서의 제목(title frontmatter 또는 첫 번째 `#` 헤더) 추출
3. 링크 텍스트가 대상 문서의 내용과 관련 있는지 확인
4. 명백히 불일치하는 경우만 보고 (예: "Kafka Consumer"라는 텍스트로 DDD 문서 링크)

### Step 4: en 디렉토리 동일 검증

ko에서 발견된 패턴을 en 디렉토리에도 동일 적용.

## 출력

`_workspace/link_check_report.md`에 결과 저장.
