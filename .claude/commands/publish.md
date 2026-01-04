---
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Bash(git push:*), Bash(git log:*), Bash(git branch:*)
description: 변경사항을 커밋하고 원격 저장소에 push
---

# Publish (Commit & Push)

변경사항을 분석하여 커밋 메시지를 작성하고, 원격 저장소에 push합니다.

## 현재 상태

- Git 상태: !`git status`
- 최근 커밋: !`git log --oneline -3`
- 현재 브랜치: !`git branch --show-current`

## 작업 절차

1. `git status`와 `git diff`로 변경사항 분석
2. Conventional Commits 형식으로 커밋 메시지 작성:
   - `feat:` 새 기능
   - `fix:` 버그 수정
   - `docs:` 문서 변경
   - `chore:` 기타 작업
   - `refactor:` 리팩토링
3. 관련 파일 staging (`git add`)
4. 커밋 생성 (HEREDOC 형식 사용)
5. 원격 저장소에 push

## 커밋 메시지 형식

```
<type>: <한글 설명>

- <세부 내용>

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

## 주의사항

- 민감한 정보 (.env, credentials 등) 커밋 금지
- force push 금지
- main/master 브랜치 직접 push 전 확인
