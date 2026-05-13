---
title: "용어 사전"
description: "Kotlin 핵심 용어 30개를 정리합니다."
weight: 1
lastmod: "2026-05-13"
---

Kotlin 핵심 용어를 알파벳/한글 순으로 정리합니다. 각 용어는 간결한 정의와 함께 관련 개념 문서 링크를 제공합니다.

{{< callout type="tip" title="핵심 용어 5선" >}}
- **Coroutine (코루틴)**: 일시 중단 가능한 경량 비동기 실행 단위
- **Data Class**: equals/hashCode/copy를 자동 생성하는 불변 데이터 클래스
- **Null Safety**: nullable(`T?`)과 non-null(`T`)을 타입으로 구분하는 안전 장치
- **Sealed Class**: 하위 타입을 같은 모듈·패키지로 제한해 `when` 완전성 검사를 지원
- **확장 함수**: 기존 타입을 수정하지 않고 새 함수를 추가하는 기법
{{< /callout >}}

---

#### A

**Any**
: Kotlin 타입 계층의 최상위 타입. 모든 Kotlin 객체의 공통 조상. `equals()`, `hashCode()`, `toString()`을 정의. → [변수와 타입]({{< relref "/docs/kotlin/concepts/variables-types/" >}})

**apply**
: 스코프 함수 중 하나. 수신 객체(`this`)를 컨텍스트로 사용하고 수신 객체 자신을 반환. 객체 초기화 패턴에 자주 사용.

**as?** (안전 캐스팅)
: 타입 변환을 시도하고 실패 시 null을 반환하는 연산자. 일반 `as`와 달리 `ClassCastException`을 던지지 않음. → [Null Safety]({{< relref "/docs/kotlin/concepts/null-safety/" >}})

---

#### B

**by** (위임 키워드)
: 프로퍼티 또는 인터페이스 구현을 다른 객체에 위임하는 키워드. `val x by lazy { ... }`, `class A : B by b` 형태로 사용.

**buildList / buildMap / buildSet**
: 빌더 블록으로 read-only 컬렉션을 구성하는 표준 라이브러리 함수. → [컬렉션]({{< relref "/docs/kotlin/concepts/collections/" >}})

---

#### C

**Companion Object**
: 클래스 내부에 선언하는 싱글톤 객체. 클래스 이름으로 직접 접근 가능. 팩토리 메서드와 상수 정의에 활용. → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

**Component Function (componentN)**
: `data class`가 자동 생성하는 함수. 구조 분해 선언(`val (a, b) = obj`)의 기반. → [Data/Sealed Class]({{< relref "/docs/kotlin/concepts/data-sealed-classes/" >}})

**Context Receiver**
: Kotlin 1.6에서 실험적으로 도입된 기능. 여러 수신 객체를 함수 시그니처에 명시하는 방법. → [버전 비교]({{< relref "/docs/kotlin/appendix/version-comparison/" >}})

**Coroutine (코루틴)**
: 일시 중단(suspend)하고 나중에 재개할 수 있는 경량 실행 단위. 스레드보다 훨씬 가볍고 구조화된 동시성(Structured Concurrency)을 지원. `launch`, `async`, `runBlocking`으로 시작.

**copy()**
: `data class`가 자동 생성하는 함수. 일부 프로퍼티만 변경한 새 인스턴스를 반환. 불변 객체의 변형 패턴. → [Data/Sealed Class]({{< relref "/docs/kotlin/concepts/data-sealed-classes/" >}})

{{< callout type="info" title="A-C 핵심 포인트" >}}
- **by**: 위임 키워드, `lazy`, `observable`, 인터페이스 위임에 사용
- **companion object**: 클래스 수준 팩토리/상수, `object` 선언과 구분
- **코루틴**: 경량 비동기, 스레드 대비 수천 개를 동시에 실행 가능
{{< /callout >}}

---

#### D

**Data Class**
: `data` 키워드로 선언하는 클래스. `equals`, `hashCode`, `toString`, `copy`, `componentN`을 자동 생성. 불변 DTO·값 객체에 적합. → [Data/Sealed Class]({{< relref "/docs/kotlin/concepts/data-sealed-classes/" >}})

**Deferred**
: `async { }` 코루틴 빌더가 반환하는 타입. 미래의 결과를 나타내며 `await()`로 결과를 가져옴.

**Dispatchers**
: 코루틴이 실행될 스레드를 결정하는 컨텍스트. `Dispatchers.Main`(메인/UI), `Dispatchers.IO`(I/O), `Dispatchers.Default`(CPU).

---

#### E

**Elvis 연산자 (?:)**
: nullable 표현식이 null이면 우변 값을 반환하는 연산자. `val name = rawName ?: "익명"`. → [Null Safety]({{< relref "/docs/kotlin/concepts/null-safety/" >}})

**Expression (표현식)**
: 값을 반환하는 코드 단위. Kotlin에서 `if`, `when`, `try`는 모두 표현식. → [기본 문법]({{< relref "/docs/kotlin/concepts/basics/" >}})

---

#### F

**Flow**
: 코루틴 기반 비동기 스트림. 순차적으로 여러 값을 내보내는 콜드 스트림. `StateFlow`, `SharedFlow` 등 핫 스트림 변형도 있음.

**fun (함수 키워드)**
: Kotlin 함수를 선언하는 키워드. 최상위 함수, 멤버 함수, 확장 함수, 로컬 함수 모두 `fun`으로 선언. → [함수]({{< relref "/docs/kotlin/concepts/functions/" >}})

---

#### I

**inline**
: 호출 지점에 함수 본문을 삽입하도록 하는 키워드. 람다를 매개변수로 받는 고차 함수에 주로 사용해 객체 생성 오버헤드 제거.

**init 블록**
: 클래스 초기화 시 실행되는 코드 블록. primary 생성자 실행 직후 실행되며 유효성 검사나 파생 프로퍼티 초기화에 사용. → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

**internal**
: 같은 Gradle 모듈 내에서만 접근 가능한 가시성 변경자. 라이브러리 내부 구현을 숨기는 데 유용. → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

{{< callout type="info" title="D-I 핵심 포인트" >}}
- **Flow**: 코루틴 기반 비동기 스트림, RxJava의 대안
- **inline**: 람다 고차 함수의 성능 최적화, `reified`와 함께 사용
- **internal**: Java에는 없는 모듈 단위 가시성
{{< /callout >}}

---

#### J

**@JvmOverloads**
: 기본값 인자가 있는 Kotlin 함수를 Java에서 오버로드 형태로 호출할 수 있도록 오버로딩 메서드를 자동 생성하는 어노테이션.

**@JvmStatic**
: `companion object` 멤버를 Java에서 정적 메서드처럼 호출할 수 있도록 만드는 어노테이션.

---

#### K

**K2 컴파일러**
: Kotlin 2.0에서 안정화된 새로운 컴파일러 프론트엔드. 컴파일 속도 향상, 더 정확한 타입 추론, IDE 분석 통합. → [버전 비교]({{< relref "/docs/kotlin/appendix/version-comparison/" >}})

---

#### L

**lateinit**
: non-null 프로퍼티의 초기화를 나중으로 미루는 키워드. `var`에만 사용. 초기화 전에 접근하면 `UninitializedPropertyAccessException`. → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

**lazy**
: 처음 접근할 때 초기화되는 지연 프로퍼티를 만드는 위임. `val x by lazy { ... }`. 스레드 안전(기본값). → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

**let**
: 스코프 함수 중 하나. 수신 객체를 `it`으로 람다에 전달하고 람다 결과를 반환. nullable 처리와 변수 스코프 제한에 주로 사용.

---

#### N

**Nothing**
: 모든 타입의 하위 타입. 정상적으로 반환되지 않는 함수(예외 던짐, 무한 루프)의 반환 타입. → [변수와 타입]({{< relref "/docs/kotlin/concepts/variables-types/" >}})

**Null Safety**
: nullable 타입(`T?`)과 non-null 타입(`T`)을 구분하는 타입 시스템 특성. `NullPointerException`을 컴파일 타임에 방지. → [Null Safety]({{< relref "/docs/kotlin/concepts/null-safety/" >}})

---

#### O

**object**
: 싱글톤 인스턴스를 정의하는 키워드. 클래스 선언과 인스턴스 생성을 동시에 처리. 익명 객체 표현식에도 사용. → [클래스와 객체]({{< relref "/docs/kotlin/concepts/classes-objects/" >}})

{{< callout type="info" title="J-O 핵심 포인트" >}}
- **K2 컴파일러**: Kotlin 2.0의 핵심, 컴파일 속도와 분석 정확도 향상
- **lateinit vs lazy**: `var`/나중 초기화 vs `val`/첫 접근 시 초기화
- **Nothing**: 예외 던지는 함수의 타입, 타입 계층 최하위
{{< /callout >}}

---

#### R

**reified**
: `inline` 함수 내에서 타입 매개변수의 실제 타입 정보를 런타임에 접근할 수 있게 하는 키워드. `inline fun <reified T> ...`

**Receiver (수신 객체)**
: 확장 함수나 람다 with receiver에서 `this`로 접근할 수 있는 객체. DSL 빌더 패턴의 핵심.

---

#### S

**Sealed Class / Sealed Interface**
: 하위 타입을 같은 모듈·같은 패키지 안으로 제한하는 클래스/인터페이스 (Kotlin 1.5+). `when` 표현식과 함께 완전성 검사 지원. → [Data/Sealed Class]({{< relref "/docs/kotlin/concepts/data-sealed-classes/" >}})

**Sequence**
: 지연(lazy) 평가 컬렉션. 중간 컬렉션 생성 없이 요소를 하나씩 파이프라인으로 처리. 대용량 데이터에 효율적. → [컬렉션]({{< relref "/docs/kotlin/concepts/collections/" >}})

**Structured Concurrency (구조화된 동시성)**
: 코루틴의 생명주기를 `CoroutineScope`로 구조화하여 누수나 미완료 작업 없이 관리하는 원칙.

**suspend**
: 코루틴 안에서만 호출할 수 있는 일시 중단 함수를 나타내는 키워드. 스레드를 차단하지 않고 실행을 일시 중단.

---

#### U

**Unit**
: 반환값이 없는 함수의 반환 타입. Java의 `void`에 해당하지만 실제 타입(싱글톤 값)으로 존재. 생략 가능. → [변수와 타입]({{< relref "/docs/kotlin/concepts/variables-types/" >}})

---

#### V

**val / var**
: `val`은 불변(read-only) 변수, `var`은 가변 변수를 선언. `val`을 기본으로 사용 권장. → [변수와 타입]({{< relref "/docs/kotlin/concepts/variables-types/" >}})

**Value Class**
: `@JvmInline value class`로 선언. 단일 프로퍼티를 래핑하지만 런타임에는 래퍼 객체를 만들지 않는 인라인 클래스.

**vararg**
: 가변 개수의 인자를 받는 매개변수 키워드. 함수 내부에서 배열처럼 다룸. 전달 시 스프레드 연산자(`*`) 사용. → [함수]({{< relref "/docs/kotlin/concepts/functions/" >}})

{{< callout type="info" title="R-V 핵심 포인트" >}}
- **reified**: 제네릭 타입 정보를 런타임에 유지, `inline`과 함께만 사용 가능
- **suspend**: 코루틴의 핵심 키워드, 스레드를 차단하지 않는 비동기
- **value class**: 런타임 오버헤드 없는 타입 안전 래퍼
{{< /callout >}}

---

#### W

**when**
: Kotlin의 분기 표현식. `switch`보다 강력하며 타입 체크, 범위, 조건 등 다양한 패턴 지원. `sealed class`와 함께 완전성 검사. → [기본 문법]({{< relref "/docs/kotlin/concepts/basics/" >}})

---

#### 확장 함수 (Extension Function)

기존 클래스를 수정하거나 상속하지 않고 새 함수를 추가하는 기법. `fun ClassName.newFunction() { ... }` 형식. 표준 라이브러리의 `String.uppercase()`, `List.filter()` 등이 확장 함수로 구현됨.

---

#### 다음 단계

- [개념 이해](../concepts/) - Kotlin 핵심 개념 상세 문서
- [버전 비교](version-comparison/) - Kotlin 1.x → 2.x 변경점
- [FAQ](faq/) - 자주 묻는 질문
- [참고 자료](references/) - 공식 문서 및 학습 자료
