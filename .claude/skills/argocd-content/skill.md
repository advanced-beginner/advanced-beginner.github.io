---
name: argocd-content
description: "ArgoCD 기술 문서 콘텐츠 생성 오케스트레이터. Kubernetes 섹션 하위에 ArgoCD concepts, howto, examples 문서를 일괄 생성하고, 영문 동기화 및 빌드 검증까지 수행한다. 트리거: ArgoCD 문서 생성, ArgoCD 콘텐츠, argocd content, GitOps 문서"
---

# ArgoCD Content — ArgoCD 문서 콘텐츠 생성 오케스트레이터

## 실행 모드: 서브 에이전트 (Fan-out/Fan-in)

4개 문서가 독립적으로 병렬 작성 가능.

## 문서 구성

| # | 위치 | 파일 | 유형 | 내용 |
|---|------|------|------|------|
| 1 | concepts/ | argocd.md | 설명(Explanation) | GitOps 원칙, ArgoCD 아키텍처, Application/Project/Sync 개념 |
| 2 | concepts/ | argocd-advanced.md | 설명(Explanation) | App of Apps, ApplicationSet, Sync Waves/Hooks, 멀티 클러스터 |
| 3 | howto/ | argocd-setup.md | How-To | 설치, 초기 설정, RBAC, 롤백, CLI 사용법 |
| 4 | examples/ | argocd-deploy.md | 튜토리얼 | Spring Boot GitOps 배포 (Kustomize 멀티 환경) |

## 워크플로우

### Phase 1: 기존 패턴 확인
기존 K8s 문서 2-3개를 읽어 구조 패턴(비유, TL;DR, Mermaid, callout)을 파악한다.

### Phase 2: 문서 병렬 작성 (Fan-out)
4개 서브 에이전트를 `run_in_background=true`로 동시 실행.
각 에이전트는 `.claude/agents/argocd-doc-writer.md`의 원칙을 따른다.

### Phase 3: 인덱스 업데이트
- `kubernetes/concepts/_index.md`, `howto/_index.md`, `examples/_index.md`에 ArgoCD 항목 추가
- `kubernetes/appendix/glossary.md`에 ArgoCD 용어 추가

### Phase 4: 영문 동기화
생성된 ko 문서를 en으로 번역하여 `content/en/docs/kubernetes/`에 동일 구조로 생성.

### Phase 5: 검증 및 배포
1. `hugo --gc --minify`로 빌드 검증
2. git commit & push

## 에러 핸들링
- 에이전트 1개 실패 시: 나머지 문서로 진행, 실패 문서는 보고
- 빌드 실패 시: 해당 문서의 frontmatter/shortcode 오류 확인 후 수정

## 테스트 시나리오

### 정상 흐름
1. 4개 에이전트 모두 완료
2. 인덱스 + 용어집 업데이트
3. en 동기화 → 빌드 성공 → 커밋/푸시

### 에러 흐름
1. advanced 문서 작성 에이전트 실패
2. 나머지 3개 문서 + 인덱스로 진행
3. 최종 보고에 "argocd-advanced: 미완료" 표기
