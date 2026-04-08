---
name: doc-consistency-check
description: "전체 문서 정합성 종합 검증 오케스트레이터. 표기(마크다운/Mermaid), 링크, 기술적/내용적 정합성을 3개 전문 에이전트로 병렬 검증하고, 이슈 수정 → 영문 동기화 → Hugo 빌드 검증 → 커밋/푸시까지 수행한다. 트리거: 문서 정합성, consistency check, 전체 검증, 정합성 검사, 문서 검토"
---

# Doc Consistency Check — 전체 문서 정합성 검증 오케스트레이터

## 실행 모드: 서브 에이전트 (Fan-out/Fan-in)

3개 검증 에이전트가 독립적으로 병렬 실행되므로 에이전트 간 통신 불필요.

## 에이전트 구성

| 에이전트 | 타입 | 역할 | 스킬 | 출력 |
|---------|------|------|------|------|
| markup-checker | general-purpose | 표기 정합성 | markup-check | `_workspace/markup_check_report.md` |
| link-checker | general-purpose | 링크 정합성 | link-check | `_workspace/link_check_report.md` |
| tech-checker | general-purpose | 기술적 정합성 | tech-check | `_workspace/tech_check_report.md` |

## 워크플로우

### Phase 1: 준비
1. `_workspace/` 디렉토리 기존 파일 확인 (이전 세션 산출물 보존)
2. 검증 대상 범위 확인: `content/ko/docs/` 전체 (7개 도메인)

### Phase 2: 병렬 검증 (Fan-out)

3개 서브 에이전트를 `run_in_background=true`로 동시 실행:

```
Agent(
  name: "markup-checker",
  prompt: ".claude/agents/markup-checker.md 역할로 .claude/skills/markup-check/skill.md 워크플로우를 실행하라. 대상: content/ko/docs/ 전체. 결과를 _workspace/markup_check_report.md에 저장하라.",
  model: "opus",
  run_in_background: true
)

Agent(
  name: "link-checker",
  prompt: ".claude/agents/link-checker.md 역할로 .claude/skills/link-check/skill.md 워크플로우를 실행하라. 대상: content/ko/docs/ 및 content/en/docs/ 전체. 결과를 _workspace/link_check_report.md에 저장하라.",
  model: "opus",
  run_in_background: true
)

Agent(
  name: "tech-checker",
  prompt: ".claude/agents/tech-checker.md 역할로 .claude/skills/tech-check/skill.md 워크플로우를 실행하라. 대상: content/ko/docs/ 전체. 결과를 _workspace/tech_check_report.md에 저장하라.",
  model: "opus",
  run_in_background: true
)
```

### Phase 3: 결과 수집 및 통합 (Fan-in)

모든 에이전트 완료 후:
1. 3개 리포트 읽기
2. 이슈를 심각도별 통합 분류
3. 자동 수정 가능 이슈 목록 정리
4. 수동 확인 필요 이슈 목록 정리

### Phase 4: 이슈 수정

1. markup-checker가 자동 수정한 내역 확인
2. link-checker가 발견한 깨진 링크 수정 (가능한 것만)
3. tech-checker가 발견한 Critical 이슈 수정

### Phase 5: 영문 동기화

ko 파일에 수정이 있으면 대응하는 en 파일에도 동일 수정 적용.

### Phase 6: 검증 및 배포

1. `hugo --gc --minify`로 빌드 검증
2. git add & commit
3. git push

## 에러 핸들링

- 에이전트 1개 실패 시: 나머지 결과로 진행, 실패 영역은 보고서에 "미검증" 표기
- 수정 중 빌드 실패: 해당 수정 롤백 후 수동 확인 목록에 추가
- 영문 파일 미존재: 해당 파일 건너뛰고 보고

## 테스트 시나리오

### 정상 흐름
1. 3개 에이전트 모두 완료
2. 발견: markup 5건, link 2건, tech 1건
3. 자동 수정 6건, 수동 확인 2건
4. en 동기화 → 빌드 성공 → 커밋/푸시

### 에러 흐름
1. tech-checker 타임아웃
2. markup/link 결과로 수정 진행
3. 최종 보고서에 "기술 정합성: 미검증" 표기
