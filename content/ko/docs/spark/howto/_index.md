---
title: 하우투 가이드
weight: 4
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

특정 문제를 해결하기 위한 단계별 가이드입니다. 각 문서는 하나의 구체적인 목표를 달성하는 방법을 설명합니다.

## 가이드 목록

**[OutOfMemoryError 해결하기](oom-troubleshooting/)**

Spark에서 가장 흔히 발생하는 메모리 부족 오류를 진단하고 해결합니다.

- Driver OOM vs Executor OOM 구분
- 메모리 설정 최적화
- 파티션 크기 조정

**[데이터 스큐 해결하기](data-skew/)**

특정 파티션에 데이터가 집중되어 발생하는 성능 저하를 해결합니다.

- 스큐 진단 방법
- Salting 기법
- AQE 스큐 조인 활성화

**[셔플 최적화하기](shuffle-optimization/)**

네트워크 I/O를 줄여 Spark 작업 성능을 개선합니다.

- 불필요한 셔플 제거
- 브로드캐스트 조인 활용
- 파티션 수 최적화

## 하우투 가이드 사용법

각 가이드는 다음 구조로 작성되어 있습니다:

1. **문제 정의**: 어떤 상황에서 이 가이드가 필요한지
2. **전제 조건**: 시작 전 준비물
3. **단계별 해결**: 명령어와 코드 포함
4. **검증**: 문제가 해결되었는지 확인하는 방법

문제 해결 중 막히는 부분이 있다면 [FAQ](../appendix/faq/)를 참고하세요.
