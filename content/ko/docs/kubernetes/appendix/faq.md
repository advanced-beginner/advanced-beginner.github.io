---
lastmod: "2026-01-11"
title: FAQ
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Kubernetes 학습과 운영에서 자주 묻는 질문과 답변을 정리합니다.

## 기본 개념

### Q: Pod와 컨테이너의 차이는 무엇인가요?

**A:** 컨테이너는 애플리케이션을 실행하는 격리된 환경이고, Pod는 하나 이상의 컨테이너를 포함하는 Kubernetes의 최소 배포 단위입니다. 같은 Pod 내의 컨테이너는 네트워크와 스토리지를 공유합니다. 대부분의 경우 Pod에는 컨테이너 1개만 포함합니다.

### Q: Deployment 대신 Pod를 직접 만들면 안 되나요?

**A:** Pod를 직접 생성할 수 있지만 권장하지 않습니다. Pod가 삭제되면 자동으로 복구되지 않기 때문입니다. Deployment를 사용하면 원하는 replicas 수를 자동으로 유지하고, 롤링 업데이트와 롤백도 지원합니다.

### Q: Service가 필요한 이유는 무엇인가요?

**A:** Pod IP는 Pod가 재생성될 때마다 변경됩니다. Service는 변경되지 않는 고정 IP와 DNS 이름을 제공하여 Pod에 안정적으로 접근할 수 있게 합니다. 또한 여러 Pod에 트래픽을 자동으로 분산합니다.

## 설정과 운영

### Q: requests와 limits의 차이는 무엇인가요?

**A:** requests는 Pod가 최소한 보장받는 리소스이고 스케줄링의 기준이 됩니다. limits는 Pod가 사용할 수 있는 최대 리소스입니다. CPU가 limits를 초과하면 스로틀링되고, 메모리가 limits를 초과하면 OOMKilled됩니다.

### Q: ConfigMap과 Secret의 차이는 무엇인가요?

**A:** 둘 다 설정을 저장하는 리소스이지만, ConfigMap은 일반 설정용이고 Secret은 민감한 정보(비밀번호, API 키)용입니다. Secret은 Base64 인코딩되며, etcd 암호화를 활성화하면 더 안전하게 저장됩니다.

### Q: Liveness Probe와 Readiness Probe의 차이는?

**A:**
- **Liveness Probe**: 컨테이너가 살아있는지 확인. 실패하면 재시작.
- **Readiness Probe**: 트래픽을 받을 준비가 되었는지 확인. 실패하면 Service에서 제외.

Liveness는 데드락 같은 복구 불가능한 상황용이고, Readiness는 일시적인 준비 안 됨 상태(DB 연결 끊김 등)용입니다.

### Q: 적절한 replicas 수는 어떻게 결정하나요?

**A:** 다음을 고려하세요:
- 최소 2개 이상 (고가용성)
- 예상 트래픽과 단일 Pod의 처리량
- 롤링 업데이트 시 가용 Pod 수
- HPA를 사용하면 자동으로 조절됩니다

## 트러블슈팅

### Q: Pod가 Pending 상태에서 멈춰 있어요.

**A:** `kubectl describe pod <pod-name>`으로 Events를 확인하세요. 주요 원인:
- 리소스 부족: 노드에 요청한 CPU/메모리가 없음
- 노드 셀렉터 불일치: 조건에 맞는 노드가 없음
- PVC 바인딩 대기: 볼륨이 준비되지 않음

### Q: CrashLoopBackOff가 발생해요.

**A:** `kubectl logs <pod-name> --previous`로 이전 로그를 확인하세요. 주요 원인:
- 애플리케이션 오류 (설정 누락, 의존성 문제)
- 메모리 부족 (Exit Code 137 = OOMKilled)
- 잘못된 명령어나 환경 변수

### Q: Service에 연결이 안 돼요.

**A:** 확인 순서:
1. `kubectl get endpoints <service-name>` - Endpoints가 있는지
2. `kubectl get pods -l <selector>` - Pod가 Ready인지
3. Pod 내부에서 `curl localhost:<port>` - 앱이 응답하는지
4. Service selector와 Pod labels가 일치하는지

## 클라우드와 도구

### Q: EKS, GKE, AKS 중 어떤 것을 선택해야 하나요?

**A:** 이미 사용 중인 클라우드가 있다면 해당 서비스를 선택하세요:
- **AWS 사용 중**: EKS
- **GCP 사용 중**: GKE
- **Azure 사용 중**: AKS

기능은 대체로 비슷합니다. GKE가 기본 기능이 가장 풍부하고, EKS는 AWS 서비스와의 통합이 좋습니다.

### Q: Helm이 필요한가요?

**A:** 복잡한 애플리케이션이나 여러 환경에 동일한 앱을 배포할 때 유용합니다. 단순한 앱이라면 kubectl과 YAML만으로 충분합니다.

### Q: 로컬 개발에 Minikube vs Kind 중 무엇을 쓰나요?

**A:**
- **Minikube**: 초보자에게 권장, 다양한 애드온, 문서 많음
- **Kind**: 빠른 클러스터 생성, 다중 노드, CI/CD에 적합

학습 목적이면 Minikube, CI 환경이면 Kind가 좋습니다.

## 모범 사례

### Q: YAML 파일은 어떻게 관리하나요?

**A:**
- Git으로 버전 관리
- 환경별로 디렉토리 분리 (`/base`, `/overlays/dev`, `/overlays/prod`)
- Kustomize나 Helm으로 환경별 차이 관리

### Q: 네임스페이스는 어떻게 나누나요?

**A:** 일반적인 방법:
- 환경별: `dev`, `staging`, `prod`
- 팀별: `team-a`, `team-b`
- 기능별: `monitoring`, `logging`

소규모라면 환경별 분리로 시작하세요.

### Q: 프로덕션에서 주의할 점은?

**A:**
- 리소스 requests/limits 필수 설정
- Readiness/Liveness Probe 설정
- 최소 2개 이상의 replicas
- PodDisruptionBudget 설정
- 정기적인 백업 (etcd, PV)
- 모니터링 및 알림 구성
