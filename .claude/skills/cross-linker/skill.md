---
name: cross-linker
description: "도메인 간 크로스 참조 분석 및 삽입. 7개 도메인 사이의 자연스러운 상호 참조를 설계하고 문서에 삽입한다. 트리거: 크로스 참조, 도메인 연결, 학습 경로, cross-linker"
---

# Cross Linker — 도메인 간 연결

## 워크플로우

### Step 1: 현황 분석
```bash
# 도메인 간 relref 참조 수
grep -rn 'relref.*"/docs/' content/ko/docs/ --include="*.md"
```
각 참조를 출발 도메인 → 도착 도메인으로 매핑하여 매트릭스를 생성한다.

### Step 2: 연결 후보 식별

도메인 간 자연스러운 관계를 기반으로 참조 후보를 생성한다:

**강한 관계 (우선)**:
| 출발 | 도착 | 연결 주제 |
|------|------|----------|
| Kafka concepts | DDD examples | 도메인 이벤트 발행 패턴 |
| DDD concepts | Kafka howto | 이벤트 스토밍 → 토픽 설계 |
| K8s howto | Observability howto | 클러스터 모니터링, 로그 수집 |
| Spark examples | Kafka concepts | Kafka-Spark 스트리밍 파이프라인 |
| Spark examples | ES howto | 분석 결과 인덱싱 |
| K8s concepts | Kafka examples | Kafka on Kubernetes 배포 |
| Observability | ES concepts | 로그 저장소로서의 Elasticsearch |

**약한 관계 (보조)**:
| 출발 | 도착 | 연결 주제 |
|------|------|----------|
| Scala concepts | DDD concepts | 함수형 도메인 모델링 |
| ES concepts | Observability concepts | 분산 로그 관리 |
| K8s scaling | Spark concepts | 리소스 오토스케일링 |

### Step 3: 삽입 위치 결정

각 문서를 읽고 가장 자연스러운 삽입 위치를 결정한다:

1. **"관련 문서" 섹션** (문서 말미): 가장 안전하고 비침습적
2. **"다음 단계" 테이블**: 학습 경로 확장에 적합
3. **본문 인라인**: 개념 설명 중 자연스러운 언급

### Step 4: 참조 삽입

relref 형식으로 삽입:
```markdown
[Kafka 토픽 설계 가이드]({{< relref "/docs/kafka/howto/topic-design" >}})
```

### Step 5: 양방향 확인
A → B 참조를 추가했으면, B → A도 자연스러운지 검토한다.
모든 참조가 양방향일 필요는 없지만, 강한 관계는 양방향을 권장한다.

### Step 6: 영문 동기화
ko에 추가한 참조를 en에도 동일하게 추가한다.

## 도구 사용법

- **Grep**: 기존 참조 탐지, 관련 키워드 검색
- **Read**: 문서 맥락 파악, 삽입 위치 결정
- **Edit**: 참조 링크 삽입

## 출력 규칙

- 도메인 간 참조 매트릭스 (before/after)
- 추가된 참조의 전체 목록 (출발 → 도착, 삽입 패턴)
- 총 참조 수 변화 보고
