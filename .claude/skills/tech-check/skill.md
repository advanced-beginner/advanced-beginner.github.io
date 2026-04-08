---
name: tech-check
description: "기술적/내용적 정합성 검증. 문서의 기술 내용이 실제 기술 사양과 일치하는지 도메인별로 객관적으로 검증한다. API명, 설정값, 아키텍처 설명, 코드 예시의 정확성을 확인한다. 트리거: 기술 검증, tech check, 사실 확인, 내용 정합성, 기술 정확성"
---

# Tech Check — 기술적 정합성 검증 스킬

## 워크플로우

### Step 1: 도메인별 기술 스택 확인

CLAUDE.md에서 기술 스택 버전 확인:
- Java 17, Spring Boot 3.2.x, Kafka 3.6.1
- 각 도메인별 대상 기술 버전

### Step 2: 도메인별 검증

각 도메인의 concepts 문서를 중심으로 검증한다. 전체 문서를 읽고 다음을 확인:

**공통 체크리스트:**
- API/메서드명이 실제와 일치하는가
- 설정 키/값이 해당 버전에서 유효한가
- 아키텍처/동작 설명이 실제와 일치하는가
- 코드 예시에 문법 오류가 없는가
- 성능 수치/벤치마크가 합리적인 범위인가

**도메인별 추가 검증:**

| 도메인 | 추가 확인 사항 |
|--------|--------------|
| kafka | Consumer Group 프로토콜, Partition 전략, KRaft 모드 |
| ddd | 패턴 정의와 Eric Evans 원서 일치 여부 |
| kubernetes | API 오브젝트 스펙, kubectl 명령어 정확성 |
| elasticsearch | Query DSL 문법, REST API 경로 |
| scala | 컴파일러 동작, 타입 시스템 규칙 |
| spark | RDD/DataFrame API, 실행 모델 |
| observability | PromQL 문법, Prometheus/Grafana 설정 |

### Step 3: 오류 분류

발견된 이슈를 심각도별로 분류:
- 🔴 Critical: 명백한 사실 오류 (잘못된 API명, 틀린 동작 설명)
- 🟡 Warning: 부정확하거나 오해 소지 (불완전한 설명, 구버전 정보)
- 🔵 Info: 개선 권장 (더 나은 표현, 최신 정보 반영)

### Step 4: 수정 제안

Critical/Warning 이슈에 대해 올바른 내용과 근거를 함께 제시한다.

## 출력

`_workspace/tech_check_report.md`에 결과 저장.
