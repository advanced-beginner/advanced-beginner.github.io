---
lastmod: "2026-01-15"
title: 용어 사전
weight: 1
---

Scala 핵심 용어를 알파벳 순으로 정리합니다. 상세 설명은 [개념 이해](../concepts/) 섹션을 참고하세요. 각 용어는 정의와 함께 관련 문서 링크를 제공합니다.

{{% notice style="tip" title="TL;DR - 핵심 용어 5선" %}}
- **Case Class**: 불변 데이터 클래스, `equals`/`copy` 자동 생성
- **Option[A]**: null 대체, `Some(값)` 또는 `None`
- **Pattern Matching**: 구조 분석 및 데이터 추출 (`match` 표현식)
- **Trait**: 구현 포함 가능한 인터페이스, 믹스인 상속 지원
- **given/using** (Scala 3): 암시적 값/매개변수의 새로운 문법
{{% /notice %}}

#### A

**ADT (Algebraic Data Type)**
: 대수적 데이터 타입. `sealed trait`과 [Case Class](#case-class)로 정의되는 합 타입과 곱 타입의 조합. Scala 3에서는 `enum`으로 더 간단히 정의 가능. → [패턴 매칭](../concepts/pattern-matching/)과 함께 사용

**Applicative**
: 독립적인 효과를 결합하는 [타입 클래스](#type-class). `pure`와 `ap` 연산을 제공. [Functor](#functor)보다 강력하고 [Monad](#monad)보다 약함. → [함수형 패턴](../concepts/functional-patterns/)

**apply 메서드**
: 객체를 함수처럼 호출할 수 있게 하는 특별한 메서드. `obj(args)`는 `obj.apply(args)`로 해석됨. [Companion Object](#companion-object)에서 팩토리 메서드로 자주 사용.

**Abstract Class (추상 클래스)**
: 인스턴스화할 수 없으며 하위 클래스에서 구현해야 하는 추상 멤버를 정의하는 클래스. [Trait](#trait)와 달리 생성자 매개변수를 가질 수 있음. → [클래스와 객체](../../concepts/classes-objects/)

**Await**
: [Future](#futuret)의 결과를 동기적으로 기다리는 함수. `Await.result(future, duration)` 형태로 사용. 테스트에서만 사용하고 프로덕션에서는 피해야 함. → [동시성](../../concepts/concurrency/)

#### C

**Cats Effect**
: 순수 함수형 비동기 프로그래밍을 지원하는 라이브러리. IO 타입으로 참조 투명성을 보장하며, [Future](#futuret)보다 더 강력한 효과 시스템 제공. → [함수형 패턴](../../concepts/functional-patterns/)

**Case Class**
: 불변 데이터를 위한 특별한 클래스. `equals`, `hashCode`, `copy`, `unapply` 등이 자동 생성됨. [Pattern Matching](#pattern-matching)과 함께 사용. → [Case Classes 상세](../concepts/case-classes/)

**Companion Object**
: 클래스와 같은 이름을 가진 싱글톤 객체. 클래스의 `private` 멤버에 접근 가능. [apply 메서드](#apply-메서드)로 팩토리 패턴 구현. → [클래스와 객체](../concepts/classes-objects/)

**Context Bound**
: `def f[A: Ordering]`처럼 [타입 클래스](#type-class) 인스턴스 존재를 요구하는 문법. → [암시적 변환](../concepts/implicits/)

**Currying (커링)**
: 여러 인자를 받는 함수를 단일 인자 함수의 체인으로 변환하는 기법. [Higher-Order Function](#higher-order-function)과 함께 사용. → [함수와 메서드](../concepts/functions-methods/)

{{% notice style="note" title="A-C 핵심 포인트" %}}
- **ADT**: `sealed trait` + `case class`로 타입 안전한 데이터 모델링
- **Case Class**: 불변 데이터에 최적, 패턴 매칭과 함께 사용
- **Companion Object**: 팩토리 메서드(`apply`)와 유틸리티 함수 배치
{{% /notice %}}

#### E

**Either[L, R]**
: 두 가지 타입 중 하나의 값을 담는 타입. 보통 `Left`는 실패, `Right`는 성공. [Option](#optiona)과 유사하나 실패 사유를 담을 수 있음. [flatMap](#flatmap)으로 체이닝 가능.

**Extension Method**
: 기존 타입에 새 메서드를 추가하는 기법. Scala 3에서는 `extension` 키워드 사용. [Type Class](#type-class) 구현에 활용. → [Scala 3 기능 비교](../appendix/version-comparison/)

**Enum (Scala 3)**
: Scala 3에서 열거형을 정의하는 키워드. 단순 열거형부터 ADT 스타일까지 지원. Scala 2에서는 `sealed trait` + `case object` 조합으로 구현. → [클래스와 객체](../../concepts/classes-objects/)

**ExecutionContext**
: [Future](#futuret) 실행을 위한 스레드 풀을 제공하는 컨텍스트. → [동시성](../concepts/concurrency/)

#### F

**fold**
: 초기값과 함께 컬렉션의 모든 요소를 하나의 값으로 축소하는 연산. `foldLeft`는 왼쪽에서 오른쪽으로, `foldRight`는 오른쪽에서 왼쪽으로 축소. → [컬렉션](../../concepts/collections/)

**Function Composition (함수 합성)**
: 작은 함수들을 결합하여 더 큰 함수를 만드는 기법. `andThen`은 왼쪽에서 오른쪽으로, `compose`는 오른쪽에서 왼쪽으로 연결. → [함수형 패턴](../../concepts/functional-patterns/)

**flatMap**
: 컨테이너 내의 값을 변환하고 결과를 평탄화하는 연산. [Monad](#monad)의 핵심 연산. [For Comprehension](#for-comprehension)으로 우아하게 표현 가능.

**For Comprehension**
: [flatMap](#flatmap), `map`, `withFilter`의 조합을 우아하게 표현하는 문법적 설탕. [Option](#optiona), [Future](#futuret), [Either](#eitherl-r) 등에 사용. → [For Comprehension 상세](../concepts/for-comprehensions/)

**Functor**
: `map` 연산을 가진 [타입 클래스](#type-class). 컨테이너 내의 값을 변환. [Applicative](#applicative)와 [Monad](#monad)의 기반. → [함수형 패턴](../concepts/functional-patterns/)

**Future[T]**
: 아직 완료되지 않은 비동기 계산을 나타내는 타입. [ExecutionContext](#executioncontext) 필요. [For Comprehension](#for-comprehension)으로 순차 실행 가능. → [동시성](../concepts/concurrency/)

#### G

**Given (Scala 3)**
: [타입 클래스](#type-class) 인스턴스를 정의하는 키워드. Scala 2의 [implicit](#implicit-scala-2) `val`을 대체. → [Scala 2 vs 3 비교](../examples/scala2-vs-scala3/)

#### H

**Higher-Order Function**
: 함수를 인자로 받거나 함수를 반환하는 함수. [map](#flatmap), `filter`, `fold` 등. → [고차 함수 상세](../concepts/higher-order-functions/)

**Higher-Kinded Type**
: 타입 생성자를 인자로 받는 타입. `F[_]` 형태. [Functor](#functor), [Monad](#monad) 정의에 필수. → [고급 타입 시스템](../concepts/type-system-advanced/)

#### I

**Immutable (불변)**
: 생성 후 상태를 변경할 수 없는 것. Scala는 불변성을 권장. [Case Class](#case-class), [val](#val), 불변 [컬렉션](../concepts/collections/) 사용.

**Implicit (Scala 2)**
: 암시적 값, 매개변수, 변환을 정의하는 키워드. Scala 3에서는 [given](#given-scala-3)/[using](#using-scala-3)으로 대체. → [암시적 변환 상세](../concepts/implicits/)

**inline (Scala 3)**
: 컴파일 타임에 코드를 인라인하여 런타임 오버헤드를 제거하는 키워드. `inline def`, `inline val`, `inline if` 등으로 사용. → [매크로와 메타프로그래밍](../../concepts/macros-metaprogramming/)

**Intersection Type (&)**
: 여러 타입을 모두 만족하는 타입. `A & B`. [Union Type](#union-type-)과 반대. → [고급 타입 시스템](../concepts/type-system-advanced/)

{{% notice style="note" title="E-I 핵심 포인트" %}}
- **Either/Option**: null 대신 사용, 에러 처리의 함수형 방식
- **flatMap**: Monad의 핵심, for comprehension으로 우아하게 표현
- **given/using** (Scala 3): `implicit`을 대체하는 명확한 문법
{{% /notice %}}

#### L

**Lazy val**
: 처음 접근할 때까지 초기화가 지연되는 값. 비용이 큰 초기화에 유용. → [기초 문법](../concepts/basics/)

**Linearization (선형화)**
: 다중 트레이트 상속 시 메서드 호출 순서를 결정하는 알고리즘. 오른쪽에서 왼쪽으로 트레이트가 적용됨. → [클래스와 객체](../../concepts/classes-objects/)

**List**
: 불변 연결 리스트. 앞에서 추가/삭제가 O(1)로 빠르지만 인덱스 접근은 O(n). `::` 연산자로 요소 추가, `:::` 또는 `++`로 리스트 연결. → [컬렉션](../../concepts/collections/)

**Lower Bound (하한 경계)**
: 타입 매개변수가 특정 타입의 상위 타입이어야 함을 지정. `A >: B` 형태. 공변 타입에서 메서드 매개변수를 다룰 때 사용. → [제네릭](../../concepts/generics/)

#### M

**Macro (매크로)**
: 컴파일 타임에 코드를 생성하는 기능. Scala 3에서는 `${ }` 스플라이스와 `'{ }` 인용, `scala.quoted` API 사용. 보일러플레이트 코드 생성에 유용. → [매크로와 메타프로그래밍](../../concepts/macros-metaprogramming/)

**Match Type (Scala 3)**
: 타입 수준에서 패턴 매칭을 수행하는 기능. 입력 타입에 따라 다른 결과 타입을 계산. 재귀적 Match Types로 복잡한 타입 변환 가능. → [고급 타입 시스템](../../concepts/type-system-advanced/)

**Mixin**
: 여러 [Trait](#trait)를 조합하여 클래스를 구성하는 방식. `with` 키워드로 트레이트를 믹스인. 코드 재사용과 관심사 분리에 유용. → [클래스와 객체](../../concepts/classes-objects/)

**Match Expression**
: 값의 패턴에 따라 분기하는 표현식. `switch`의 강력한 버전. [Case Class](#case-class)와 [Sealed](#sealed) 트레이트에 최적화. → [패턴 매칭 상세](../concepts/pattern-matching/)

**Monad**
: [flatMap](#flatmap)과 `pure` 연산을 가진 [타입 클래스](#type-class). 순차적 효과 조합. [Option](#optiona), [Either](#eitherl-r), [Future](#futuret) 등이 Monad. → [함수형 패턴](../concepts/functional-patterns/)

#### O

**Object**
: 싱글톤 인스턴스를 정의하는 키워드. [Companion Object](#companion-object) 참고. → [클래스와 객체](../concepts/classes-objects/)

**Opaque Type (Scala 3)**
: 외부에서는 다른 타입으로 보이지만 내부에서는 기반 타입과 동일한 타입. 런타임 오버헤드 없는 타입 안전성. → [Scala 3 기능 비교](../appendix/version-comparison/)

**Option[A]**
: 값이 있거나(`Some`) 없음(`None`)을 나타내는 타입. `null` 대체. [flatMap](#flatmap)과 [For Comprehension](#for-comprehension)으로 안전하게 처리. → [기초 문법](../concepts/basics/)

{{% notice style="note" title="L-O 핵심 포인트" %}}
- **lazy val**: 비용이 큰 초기화를 첫 사용 시점까지 지연
- **Monad**: `flatMap` + `pure`, 순차적 효과 조합의 핵심 추상화
- **Option**: null 대신 `Some`/`None`으로 타입 안전한 처리
{{% /notice %}}

#### P

**Partial Function**
: 일부 입력에 대해서만 정의된 함수. [Pattern Matching](#pattern-matching)의 케이스와 같은 형태. `collect` 메서드에 활용. → [함수와 메서드](../concepts/functions-methods/)

**Pattern Matching**
: 값의 구조를 분석하고 데이터를 추출하는 기법. [Case Class](#case-class), [Sealed](#sealed) 트레이트와 함께 사용. → [패턴 매칭 상세](../concepts/pattern-matching/)

**Promise[T]**
: [Future](#futuret)를 직접 완료할 수 있게 해주는 타입. 콜백 기반 API 래핑에 사용. → [동시성](../concepts/concurrency/)

**Pure Function (순수 함수)**
: 동일한 입력에 대해 항상 동일한 출력을 반환하고 부수 효과가 없는 함수. [참조 투명성](#referential-transparency) 보장. 함수형 프로그래밍의 핵심. → [함수형 패턴](../../concepts/functional-patterns/)

#### R

**Range**
: 숫자 범위를 나타내는 특수 시퀀스. `1 to 10`, `1 until 10`, `1 to 10 by 2` 형태로 생성. 메모리에 모든 요소를 저장하지 않고 필요 시 계산. → [컬렉션](../../concepts/collections/)

**reduce**
: 컬렉션의 요소들을 하나의 값으로 축소하는 연산. [fold](#fold)와 달리 초기값 없이 첫 요소부터 시작. 빈 컬렉션에 사용 시 예외 발생. → [컬렉션](../../concepts/collections/)

**Referential Transparency**
: 표현식을 그 결과값으로 대체해도 프로그램 의미가 변하지 않는 속성. 순수 함수의 핵심 특성. [Immutable](#immutable-불변) 데이터와 관련. → [함수형 패턴](../concepts/functional-patterns/)

#### S

**Sealed**
: 같은 파일에서만 상속 가능하게 제한하는 수식어. [Pattern Matching](#pattern-matching) 완전성 검사에 사용. [ADT](#adt-algebraic-data-type) 정의에 필수. → [패턴 매칭](../concepts/pattern-matching/)

**Set**
: 중복이 없는 불변 컬렉션. 요소 포함 여부를 빠르게 확인 가능. `union`, `intersect`, `diff` 등 집합 연산 지원. → [컬렉션](../../concepts/collections/)

**Side Effect (부수 효과)**
: 함수가 값을 반환하는 것 외에 외부 상태를 변경하거나 관찰 가능한 동작을 수행하는 것. 콘솔 출력, 파일 I/O, 전역 변수 변경 등. 함수형 프로그래밍에서는 최소화 권장. → [함수형 패턴](../../concepts/functional-patterns/)

**Singleton Object**
: [Object](#object) 키워드로 정의된 유일한 인스턴스. [Companion Object](#companion-object) 참고.

**String Interpolation (문자열 보간)**
: 문자열 내에 변수나 표현식을 삽입하는 기능. `s"Hello, $name"`, `f"$pi%.2f"`, `raw"\d+"` 형태. → [기초 문법](../../concepts/basics/)

**summon (Scala 3)**
: 주어진 타입의 암시적 인스턴스를 가져오는 함수. [implicit](#implicit-scala-2) `implicitly`를 대체. → [타입 클래스](../concepts/type-classes/)

#### T

**Tail Recursion**
: 함수의 마지막 연산이 자기 자신 호출인 재귀. 스택 오버플로우 없이 최적화 가능. `@tailrec` 어노테이션으로 검증. → [함수와 메서드](../concepts/functions-methods/)

**Trait**
: Java의 인터페이스와 유사하지만 구현을 포함할 수 있는 타입. 믹스인 상속 지원. [Sealed](#sealed)와 함께 [ADT](#adt-algebraic-data-type) 구성. → [클래스와 객체](../concepts/classes-objects/)

**Try[T]**
: 예외를 던질 수 있는 계산의 결과를 담는 타입. `Success` 또는 `Failure`. [Either](#eitherl-r), [Option](#optiona)과 유사한 에러 처리 패턴. → [기초 문법](../concepts/basics/)

**Type Class**
: 기존 타입에 기능을 추가하는 패턴. Ad-hoc 다형성 구현. [Functor](#functor), [Monad](#monad) 등이 대표적. → [타입 클래스 상세](../concepts/type-classes/)

**Type Inference**
: 컴파일러가 타입을 자동으로 추론하는 기능. Scala의 강력한 타입 추론으로 보일러플레이트 감소. → [기초 문법](../concepts/basics/)

**Type Lambda (Scala 3)**
: 타입 생성자를 타입 매개변수로 전달할 수 있게 하는 기능. `[X] =>> F[X]` 형태. 고차 타입을 다룰 때 타입 매개변수 부분 적용에 유용. → [고급 타입 시스템](../../concepts/type-system-advanced/)

**transparent inline (Scala 3)**
: 반환 타입을 더 정밀하게 추론하는 [inline](#inline-scala-3). 각 호출 지점에서 정확한 타입이 추론됨. 타입별 최적화에 사용. → [매크로와 메타프로그래밍](../../concepts/macros-metaprogramming/)

{{% notice style="note" title="P-T 핵심 포인트" %}}
- **Pattern Matching**: `match` 표현식으로 데이터 구조 분해 및 분기
- **Sealed**: 상속을 같은 파일로 제한, 패턴 매칭 완전성 검사
- **Type Class**: 기존 타입에 기능 추가, Ad-hoc 다형성 구현
{{% /notice %}}

#### U

**Union Type (|)**
: 여러 타입 중 하나를 나타내는 타입. `Int | String`. [Intersection Type](#intersection-type-)과 반대. Scala 3 전용. → [고급 타입 시스템](../concepts/type-system-advanced/)

**unapply**
: 패턴 매칭에서 객체를 분해하여 필드를 추출하는 메서드. [Case Class](#case-class)에서 자동 생성. 커스텀 추출자(Extractor) 정의에 사용. → [패턴 매칭](../../concepts/pattern-matching/)

**Upper Bound (상한 경계)**
: 타입 매개변수가 특정 타입의 하위 타입이어야 함을 지정. `A <: B` 형태. 타입 매개변수가 특정 메서드를 가짐을 보장. → [제네릭](../../concepts/generics/)

**Using (Scala 3)**
: 암시적 매개변수를 선언하는 키워드. [Implicit](#implicit-scala-2)을 대체. [Given](#given-scala-3)과 짝을 이룸. → [Scala 2 vs 3 비교](../examples/scala2-vs-scala3/)

#### V

**Vector**
: 랜덤 접근이 빠른 불변 시퀀스. 32진 트리 구조로 대부분의 연산이 사실상 상수 시간. [List](#list)보다 인덱스 접근이 필요한 경우에 적합. → [컬렉션](../../concepts/collections/)

**val**
: [Immutable](#immutable-불변) 값을 선언하는 키워드. [var](#var)와 비교. Scala에서 기본 선택.

**var**
: 가변 변수를 선언하는 키워드. [val](#val)보다 권장되지 않음. → [기초 문법](../concepts/basics/)

**Variance (변성)**
: 타입 매개변수의 서브타이핑 관계. 공변(`+A`), 반공변(`-A`), 무공변. 컬렉션 설계에 중요. → [변성 상세](../concepts/variance/)

#### Y

**yield**
: [For Comprehension](#for-comprehension)에서 값을 생성하는 키워드. `map` 호출로 변환됨. → [For Comprehension 상세](../concepts/for-comprehensions/)

{{% notice style="note" title="U-Y 핵심 포인트" %}}
- **Union Type** (`|`): Scala 3 전용, `Either` 대신 간결한 타입 표현
- **val/var**: `val`(불변) 우선, `var`(가변)는 최소화
- **Variance**: 공변(`+A`), 반공변(`-A`)으로 타입 관계 정의
{{% /notice %}}

#### Z

**ZIO**
: 효과 시스템과 의존성 주입을 통합한 라이브러리. `ZIO[R, E, A]` 타입으로 환경, 에러, 결과를 명시. [Cats Effect](#cats-effect)와 함께 [Future](#futuret)의 대안으로 사용. → [함수형 패턴](../../concepts/functional-patterns/)

---

#### 다음 단계

- [개념 이해](../concepts/) - Scala 핵심 개념
- [실습 예제](../examples/) - 코드로 배우기
- [Spark 통합](../examples/spark-integration/) - 빅데이터 처리
- [참고 자료](references/) - 도서, 강좌, 커뮤니티
- [FAQ](faq/) - 자주 묻는 질문
