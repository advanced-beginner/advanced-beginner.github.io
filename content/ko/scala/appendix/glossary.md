---
lastmod: "2026-01-08"
title: 용어 사전
weight: 1
---

Scala 핵심 용어를 알파벳 순으로 정리합니다. 상세 설명은 [개념 이해](../../concepts/) 섹션을 참고하세요.

## A

**ADT (Algebraic Data Type)**
: 대수적 데이터 타입. `sealed trait`과 [Case Class](#case-class)로 정의되는 합 타입과 곱 타입의 조합. Scala 3에서는 `enum`으로 더 간단히 정의 가능. → [패턴 매칭](../../concepts/pattern-matching/)과 함께 사용

**Applicative**
: 독립적인 효과를 결합하는 [타입 클래스](#type-class). `pure`와 `ap` 연산을 제공. [Functor](#functor)보다 강력하고 [Monad](#monad)보다 약함. → [함수형 패턴](../../concepts/functional-patterns/)

**apply 메서드**
: 객체를 함수처럼 호출할 수 있게 하는 특별한 메서드. `obj(args)`는 `obj.apply(args)`로 해석됨. [Companion Object](#companion-object)에서 팩토리 메서드로 자주 사용.

## C

**Case Class**
: 불변 데이터를 위한 특별한 클래스. `equals`, `hashCode`, `copy`, `unapply` 등이 자동 생성됨. [Pattern Matching](#pattern-matching)과 함께 사용. → [Case Classes 상세](../../concepts/case-classes/)

**Companion Object**
: 클래스와 같은 이름을 가진 싱글톤 객체. 클래스의 `private` 멤버에 접근 가능. [apply 메서드](#apply-메서드)로 팩토리 패턴 구현. → [클래스와 객체](../../concepts/classes-objects/)

**Context Bound**
: `def f[A: Ordering]`처럼 [타입 클래스](#type-class) 인스턴스 존재를 요구하는 문법. → [암시적 변환](../../concepts/implicits/)

**Currying (커링)**
: 여러 인자를 받는 함수를 단일 인자 함수의 체인으로 변환하는 기법. [Higher-Order Function](#higher-order-function)과 함께 사용. → [함수와 메서드](../../concepts/functions-methods/)

## E

**Either[L, R]**
: 두 가지 타입 중 하나의 값을 담는 타입. 보통 `Left`는 실패, `Right`는 성공. [Option](#optiona)과 유사하나 실패 사유를 담을 수 있음. [flatMap](#flatmap)으로 체이닝 가능.

**Extension Method**
: 기존 타입에 새 메서드를 추가하는 기법. Scala 3에서는 `extension` 키워드 사용. [Type Class](#type-class) 구현에 활용. → [Scala 3 기능 비교](../../appendix/version-comparison/)

**ExecutionContext**
: [Future](#futuret) 실행을 위한 스레드 풀을 제공하는 컨텍스트. → [동시성](../../concepts/concurrency/)

## F

**flatMap**
: 컨테이너 내의 값을 변환하고 결과를 평탄화하는 연산. [Monad](#monad)의 핵심 연산. [For Comprehension](#for-comprehension)으로 우아하게 표현 가능.

**For Comprehension**
: [flatMap](#flatmap), `map`, `withFilter`의 조합을 우아하게 표현하는 문법적 설탕. [Option](#optiona), [Future](#futuret), [Either](#eitherl-r) 등에 사용. → [For Comprehension 상세](../../concepts/for-comprehensions/)

**Functor**
: `map` 연산을 가진 [타입 클래스](#type-class). 컨테이너 내의 값을 변환. [Applicative](#applicative)와 [Monad](#monad)의 기반. → [함수형 패턴](../../concepts/functional-patterns/)

**Future[T]**
: 아직 완료되지 않은 비동기 계산을 나타내는 타입. [ExecutionContext](#executioncontext) 필요. [For Comprehension](#for-comprehension)으로 순차 실행 가능. → [동시성](../../concepts/concurrency/)

## G

**Given (Scala 3)**
: [타입 클래스](#type-class) 인스턴스를 정의하는 키워드. Scala 2의 [implicit](#implicit-scala-2) `val`을 대체. → [Scala 2 vs 3 비교](../../examples/scala2-vs-scala3/)

## H

**Higher-Order Function**
: 함수를 인자로 받거나 함수를 반환하는 함수. [map](#flatmap), `filter`, `fold` 등. → [고차 함수 상세](../../concepts/higher-order-functions/)

**Higher-Kinded Type**
: 타입 생성자를 인자로 받는 타입. `F[_]` 형태. [Functor](#functor), [Monad](#monad) 정의에 필수. → [고급 타입 시스템](../../concepts/type-system-advanced/)

## I

**Immutable (불변)**
: 생성 후 상태를 변경할 수 없는 것. Scala는 불변성을 권장. [Case Class](#case-class), [val](#val), 불변 [컬렉션](../../concepts/collections/) 사용.

**Implicit (Scala 2)**
: 암시적 값, 매개변수, 변환을 정의하는 키워드. Scala 3에서는 [given](#given-scala-3)/[using](#using-scala-3)으로 대체. → [암시적 변환 상세](../../concepts/implicits/)

**Intersection Type (&)**
: 여러 타입을 모두 만족하는 타입. `A & B`. [Union Type](#union-type-)과 반대. → [고급 타입 시스템](../../concepts/type-system-advanced/)

## L

**Lazy val**
: 처음 접근할 때까지 초기화가 지연되는 값. 비용이 큰 초기화에 유용. → [기초 문법](../../concepts/basics/)

## M

**Match Expression**
: 값의 패턴에 따라 분기하는 표현식. `switch`의 강력한 버전. [Case Class](#case-class)와 [Sealed](#sealed) 트레이트에 최적화. → [패턴 매칭 상세](../../concepts/pattern-matching/)

**Monad**
: [flatMap](#flatmap)과 `pure` 연산을 가진 [타입 클래스](#type-class). 순차적 효과 조합. [Option](#optiona), [Either](#eitherl-r), [Future](#futuret) 등이 Monad. → [함수형 패턴](../../concepts/functional-patterns/)

## O

**Object**
: 싱글톤 인스턴스를 정의하는 키워드. [Companion Object](#companion-object) 참고. → [클래스와 객체](../../concepts/classes-objects/)

**Opaque Type (Scala 3)**
: 외부에서는 다른 타입으로 보이지만 내부에서는 기반 타입과 동일한 타입. 런타임 오버헤드 없는 타입 안전성. → [Scala 3 기능 비교](../../appendix/version-comparison/)

**Option[A]**
: 값이 있거나(`Some`) 없음(`None`)을 나타내는 타입. `null` 대체. [flatMap](#flatmap)과 [For Comprehension](#for-comprehension)으로 안전하게 처리. → [기초 문법](../../concepts/basics/)

## P

**Partial Function**
: 일부 입력에 대해서만 정의된 함수. [Pattern Matching](#pattern-matching)의 케이스와 같은 형태. `collect` 메서드에 활용. → [함수와 메서드](../../concepts/functions-methods/)

**Pattern Matching**
: 값의 구조를 분석하고 데이터를 추출하는 기법. [Case Class](#case-class), [Sealed](#sealed) 트레이트와 함께 사용. → [패턴 매칭 상세](../../concepts/pattern-matching/)

**Promise[T]**
: [Future](#futuret)를 직접 완료할 수 있게 해주는 타입. 콜백 기반 API 래핑에 사용. → [동시성](../../concepts/concurrency/)

## R

**Referential Transparency**
: 표현식을 그 결과값으로 대체해도 프로그램 의미가 변하지 않는 속성. 순수 함수의 핵심 특성. [Immutable](#immutable-불변) 데이터와 관련. → [함수형 패턴](../../concepts/functional-patterns/)

## S

**Sealed**
: 같은 파일에서만 상속 가능하게 제한하는 수식어. [Pattern Matching](#pattern-matching) 완전성 검사에 사용. [ADT](#adt-algebraic-data-type) 정의에 필수. → [패턴 매칭](../../concepts/pattern-matching/)

**Singleton Object**
: [Object](#object) 키워드로 정의된 유일한 인스턴스. [Companion Object](#companion-object) 참고.

**summon (Scala 3)**
: 주어진 타입의 암시적 인스턴스를 가져오는 함수. [implicit](#implicit-scala-2) `implicitly`를 대체. → [타입 클래스](../../concepts/type-classes/)

## T

**Tail Recursion**
: 함수의 마지막 연산이 자기 자신 호출인 재귀. 스택 오버플로우 없이 최적화 가능. `@tailrec` 어노테이션으로 검증. → [함수와 메서드](../../concepts/functions-methods/)

**Trait**
: Java의 인터페이스와 유사하지만 구현을 포함할 수 있는 타입. 믹스인 상속 지원. [Sealed](#sealed)와 함께 [ADT](#adt-algebraic-data-type) 구성. → [클래스와 객체](../../concepts/classes-objects/)

**Try[T]**
: 예외를 던질 수 있는 계산의 결과를 담는 타입. `Success` 또는 `Failure`. [Either](#eitherl-r), [Option](#optiona)과 유사한 에러 처리 패턴. → [기초 문법](../../concepts/basics/)

**Type Class**
: 기존 타입에 기능을 추가하는 패턴. Ad-hoc 다형성 구현. [Functor](#functor), [Monad](#monad) 등이 대표적. → [타입 클래스 상세](../../concepts/type-classes/)

**Type Inference**
: 컴파일러가 타입을 자동으로 추론하는 기능. Scala의 강력한 타입 추론으로 보일러플레이트 감소. → [기초 문법](../../concepts/basics/)

## U

**Union Type (|)**
: 여러 타입 중 하나를 나타내는 타입. `Int | String`. [Intersection Type](#intersection-type-)과 반대. Scala 3 전용. → [고급 타입 시스템](../../concepts/type-system-advanced/)

**Using (Scala 3)**
: 암시적 매개변수를 선언하는 키워드. [Implicit](#implicit-scala-2)을 대체. [Given](#given-scala-3)과 짝을 이룸. → [Scala 2 vs 3 비교](../../examples/scala2-vs-scala3/)

## V

**val**
: [Immutable](#immutable-불변) 값을 선언하는 키워드. [var](#var)와 비교. Scala에서 기본 선택.

**var**
: 가변 변수를 선언하는 키워드. [val](#val)보다 권장되지 않음. → [기초 문법](../../concepts/basics/)

**Variance (변성)**
: 타입 매개변수의 서브타이핑 관계. 공변(`+A`), 반공변(`-A`), 무공변. 컬렉션 설계에 중요. → [변성 상세](../../concepts/variance/)

## Y

**yield**
: [For Comprehension](#for-comprehension)에서 값을 생성하는 키워드. `map` 호출로 변환됨. → [For Comprehension 상세](../../concepts/for-comprehensions/)

---

## 다음 단계

- [개념 이해](../../concepts/) - Scala 핵심 개념
- [실습 예제](../../examples/) - 코드로 배우기
- [Spark 통합](../../examples/spark-integration/) - 빅데이터 처리
- [참고 자료](../references/) - 도서, 강좌, 커뮤니티
- [FAQ](../faq/) - 자주 묻는 질문
