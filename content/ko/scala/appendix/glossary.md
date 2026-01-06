---
title: 용어 사전
weight: 1
---

Scala 핵심 용어를 알파벳 순으로 정리합니다.

## A

**ADT (Algebraic Data Type)**
: 대수적 데이터 타입. `sealed trait`과 `case class`로 정의되는 합 타입과 곱 타입의 조합. Scala 3에서는 `enum`으로 더 간단히 정의 가능.

**Applicative**
: 독립적인 효과를 결합하는 타입 클래스. `pure`와 `ap` 연산을 제공.

**apply 메서드**
: 객체를 함수처럼 호출할 수 있게 하는 특별한 메서드. `obj(args)`는 `obj.apply(args)`로 해석됨.

## C

**Case Class**
: 불변 데이터를 위한 특별한 클래스. `equals`, `hashCode`, `copy`, `unapply` 등이 자동 생성됨.

**Companion Object**
: 클래스와 같은 이름을 가진 싱글톤 객체. 클래스의 `private` 멤버에 접근 가능.

**Context Bound**
: `def f[A: Ordering]`처럼 타입 클래스 인스턴스 존재를 요구하는 문법.

**Currying (커링)**
: 여러 인자를 받는 함수를 단일 인자 함수의 체인으로 변환하는 기법.

## E

**Either[L, R]**
: 두 가지 타입 중 하나의 값을 담는 타입. 보통 `Left`는 실패, `Right`는 성공.

**Extension Method**
: 기존 타입에 새 메서드를 추가하는 기법. Scala 3에서는 `extension` 키워드 사용.

**ExecutionContext**
: `Future` 실행을 위한 스레드 풀을 제공하는 컨텍스트.

## F

**flatMap**
: 컨테이너 내의 값을 변환하고 결과를 평탄화하는 연산. Monad의 핵심 연산.

**For Comprehension**
: `flatMap`, `map`, `withFilter`의 조합을 우아하게 표현하는 문법적 설탕.

**Functor**
: `map` 연산을 가진 타입 클래스. 컨테이너 내의 값을 변환.

**Future[T]**
: 아직 완료되지 않은 비동기 계산을 나타내는 타입.

## G

**Given (Scala 3)**
: 타입 클래스 인스턴스를 정의하는 키워드. Scala 2의 `implicit val`을 대체.

## H

**Higher-Order Function**
: 함수를 인자로 받거나 함수를 반환하는 함수.

**Higher-Kinded Type**
: 타입 생성자를 인자로 받는 타입. `F[_]` 형태.

## I

**Immutable (불변)**
: 생성 후 상태를 변경할 수 없는 것. Scala는 불변성을 권장.

**Implicit (Scala 2)**
: 암시적 값, 매개변수, 변환을 정의하는 키워드.

**Intersection Type (&)**
: 여러 타입을 모두 만족하는 타입. `A & B`.

## L

**Lazy val**
: 처음 접근할 때까지 초기화가 지연되는 값.

## M

**Match Expression**
: 값의 패턴에 따라 분기하는 표현식. `switch`의 강력한 버전.

**Monad**
: `flatMap`과 `pure` 연산을 가진 타입 클래스. 순차적 효과 조합.

## O

**Object**
: 싱글톤 인스턴스를 정의하는 키워드.

**Opaque Type (Scala 3)**
: 외부에서는 다른 타입으로 보이지만 내부에서는 기반 타입과 동일한 타입.

**Option[A]**
: 값이 있거나(`Some`) 없음(`None`)을 나타내는 타입. `null` 대체.

## P

**Partial Function**
: 일부 입력에 대해서만 정의된 함수.

**Pattern Matching**
: 값의 구조를 분석하고 데이터를 추출하는 기법.

**Promise[T]**
: `Future`를 직접 완료할 수 있게 해주는 타입.

## R

**Referential Transparency**
: 표현식을 그 결과값으로 대체해도 프로그램 의미가 변하지 않는 속성.

## S

**Sealed**
: 같은 파일에서만 상속 가능하게 제한하는 수식어. 패턴 매칭 완전성 검사에 사용.

**Singleton Object**
: `object` 키워드로 정의된 유일한 인스턴스.

**summon (Scala 3)**
: 주어진 타입의 암시적 인스턴스를 가져오는 함수. `implicitly`를 대체.

## T

**Tail Recursion**
: 함수의 마지막 연산이 자기 자신 호출인 재귀. 스택 오버플로우 없이 최적화 가능.

**Trait**
: Java의 인터페이스와 유사하지만 구현을 포함할 수 있는 타입.

**Try[T]**
: 예외를 던질 수 있는 계산의 결과를 담는 타입. `Success` 또는 `Failure`.

**Type Class**
: 기존 타입에 기능을 추가하는 패턴. Ad-hoc 다형성 구현.

**Type Inference**
: 컴파일러가 타입을 자동으로 추론하는 기능.

## U

**Union Type (|)**
: 여러 타입 중 하나를 나타내는 타입. `Int | String`.

**Using (Scala 3)**
: 암시적 매개변수를 선언하는 키워드. `implicit`을 대체.

## V

**val**
: 불변 값을 선언하는 키워드.

**var**
: 가변 변수를 선언하는 키워드.

**Variance (변성)**
: 타입 매개변수의 서브타이핑 관계. 공변(+), 반공변(-), 무공변.

## Y

**yield**
: for comprehension에서 값을 생성하는 키워드.
