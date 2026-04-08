---
title: "DDD Debate: Proponents vs Skeptics"
description: "A summary of the debate between positive and negative perspectives on DDD adoption."
weight: 4
lastmod: "2026-01-16"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
skip_evaluation: true
---

# DDD Debate: Proponents vs Skeptics

> Two senior developers debate Domain-Driven Design (DDD).

{{< callout type="info" title="About This Document" >}}
This document records a debate between two senior developers with different perspectives on DDD. Rather than presenting a definitive answer, its purpose is to help you develop a <strong>balanced view of DDD through diverse perspectives</strong>.
{{< /callout >}}

**Participants:**
- **Proponent (Pro-DDD)**: A senior developer who advocates for DDD
- **Skeptic (Anti-DDD)**: A senior developer who is skeptical of DDD

**Date:** 2026-01-17

---

## Debate Begins

### Skeptic's Opening

> Honestly, whenever I see people praising DDD, I worry. The theory sounds great. "Design centered on business logic," "unify language with domain experts" — who would disagree?
>
> But in practice, most DDD I have witnessed was a <strong>'festival of unnecessary complexity'</strong>.
>
> * Spending all their time breaking simple CRUD into complex Aggregates and Value Objects,
> * Writing thousands of lines of meaningless DTO conversion code just to follow Layered Architecture — it was truly frustrating.
> * In the end, you end up working at <strong>'the speed of maintaining the architecture'</strong> rather than the speed of the business. That is the trap of DDD, in my opinion.
>
> Creating business value should come first, not treating Eric Evans' book like a bible and making code look pretty — that is not our job, is it?

---

### Proponent's First Response

Welcome, pragmatic senior developer.

I have seen many of the cases you describe. And honestly, **that is not DDD**.

#### Core Rebuttal: DDD Is Not "Applying Tactical Patterns"

Eric Evans' book is over 500 pages, yet all people remember are a few <strong>tactical patterns</strong> like Aggregate, Repository, and Value Object. The <strong>strategic design</strong> that takes up more than half the book — Bounded Context, Context Mapping, Ubiquitous Language — gets ignored.

Let me address your points one by one:

**1. "Breaking simple CRUD into Aggregates"**

This is not a problem with DDD but a problem with <strong>judgment in application</strong>. The DDD community clearly states "use Transaction Script for Simple Domains." Applying DDD everywhere is not DDD — it is <strong>Cargo Cult</strong>.

**2. "Thousands of lines of DTO conversion code"**

Layered Architecture is not a mandatory element of DDD. Eric Evans himself recommended Hexagonal Architecture more, and nowadays many teams use Clean Architecture or Vertical Slice. DTO hell is the result of <strong>poor architecture choices</strong>, not DDD.

**3. "Working at the speed of maintaining the architecture"**

This is precisely <strong>not doing DDD</strong>. The core of DDD is "code follows business changes," but if the architecture is blocking business, that is a failed design.

#### My Question

> "It could have been done with simple CRUD"

Is this premise really correct?

In my experience, I have seen many systems that started as CRUD but became <strong>monsters with dozens of business rules</strong> two years later. By then, refactoring is already too late, and inevitably someone says "it is legacy" and suggests rewriting from scratch.

DDD is about <strong>selectively applying it to core domains where complexity is expected</strong>. It is not about using it everywhere.

---

### Skeptic's First Rebuttal

> A "No True Scotsman" argument, I see. Let me offer three rebuttals.

**1. The difficulty of the methodology itself is a flaw**

If 10 teams try DDD and 9 fail, that is not evidence of developer incompetence — it is evidence that the methodology is disconnected from reality.

**2. A 3,000-line monster vs a maze of 300 pieces**

A 3,000-line class is at least easy to 'find things in.' A system split up with DDD requires navigating dozens of files to understand a single piece of logic. This is not 'removal of complexity' but merely 'distribution of complexity.'

**3. Strategic design is 'politics,' not 'technology'**

In reality, how many organizations have time for domain experts to unify terminology with developers? Eventually developers divide Contexts based on imagination, and later it becomes the even bigger disaster of distributed transactions.

> **Key question**: Is the complexity of maintaining DDD — its abstractions and distributed environment — not greater than the complexity DDD claims to solve?

---

### Proponent's Second Response

#### I Will Concede What Should Be Conceded

The "No True Scotsman" criticism is partly valid. The DDD community has been evasive about failure cases by saying "that was not real DDD." And the steep learning curve is also true.

But I disagree with the three rebuttals.

#### 1. On "Methodology Difficulty Is a Flaw"

By that logic, <strong>Git, Kubernetes, and functional programming are all flawed</strong>. How many teams in practice use Git properly? Most are stuck in an infinite loop of `git pull` -> conflict -> `git reset --hard`.

The value of a tool is not "is it easy" but <strong>"does the benefit of using it properly outweigh the learning cost"</strong>.

The issue is knowing <strong>when and where</strong> to apply DDD. Not every project needs DDD. For systems where complex business logic is a core competitive advantage — finance, logistics, reservation systems — DDD more than repays the learning cost.

#### 2. On "3,000 Lines vs 300 Files"

This is a <strong>false dichotomy</strong>.

First, "easy to find things in 3,000 lines" is true <strong>only the first time you look</strong>. What about when fixing bugs inside it?

```java
// A common pattern in a 3,000-line service
public void processOrder(Order order) {
    // ... 500 lines of validation logic ...
    // ... 800 lines of discount calculation ...
    // ... 400 lines of inventory processing ...
    // ... 300 lines of payment integration ...
    // ... Which part do I fix? What is the impact on the rest?
}
```

<strong>You cannot determine the impact scope</strong>. Have you never experienced fixing discount logic and breaking inventory processing?

In contrast, well-designed DDD is not 300 files. It is <strong>a few cohesive Aggregates</strong>:

```text
Order (Aggregate Root)
├── OrderItem (Entity)
├── Discount (Value Object)
└── All related logic exists only within this boundary

Inventory (Separate Aggregate)
└── Communicates with Order only through domain events
```

If you fix discount logic, you only need to assess impact within the Order Aggregate. <strong>This is not distribution of complexity but isolation of complexity</strong>.

#### 3. On "Strategic Design Is Politics"

**That is exactly right. And that is the point.**

The most important lesson DDD teaches is that "things that look like technical problems are actually <strong>organizational/communication problems</strong>."

No time for domain experts to talk? That is not DDD's problem — it is an <strong>organizational problem</strong>. No methodology will produce good software in that state.

DDD at least <strong>makes the problem explicit</strong>. It makes you recognize that "we are writing code without understanding the domain." Without awareness, there is no improvement.

#### My Answer on the Total Amount of Complexity

You need to distinguish between Fred Brooks' <strong>Essential Complexity</strong> and <strong>Accidental Complexity</strong>.

- **Essential Complexity**: The complexity of the business itself. Unavoidable.
- **Accidental Complexity**: Complexity from our technology/design choices.

A 3,000-line service <strong>hides</strong> Essential Complexity. Business rules are scattered somewhere in the code, but you do not know where.

DDD <strong>makes Essential Complexity explicit</strong>. Aggregate boundaries, domain events, Value Objects — these are signposts saying "business rules live here."

> **My counter-question**:
>
> You said KISS and YAGNI are sufficient, but how do those principles determine <strong>"what is simple"</strong> and <strong>"what is needed"</strong>?
>
> That judgment is based on <strong>deep understanding of the domain</strong>. DDD is the methodology for gaining that understanding.
>
> If you advocate KISS without understanding the domain, you end up with <strong>"a design that looks simple but is wrong."</strong> When requirements change later, it is a full rewrite.

---

### Skeptic's Second Rebuttal

**1. The comparison with Git/Kubernetes is a 'category error'**

Git is a tool with deterministic results. DDD is a philosophy where 5 developers fight for 5 days over the single question "what is an Aggregate?" This <strong>'cost of subjective interpretation'</strong> is fundamentally different from tool proficiency.

**2. 'Isolation of complexity' brings a 'tax of technical debt'**

The moment you split Order and Inventory into Aggregates, what could have been a single DB transaction now requires Domain Events, Outbox Pattern, and Saga. This is the ultimate form of Accidental Complexity.

**3. Diagnosing "organizational problems" is irresponsible**

As senior developers, we should find the best solution within the given environment, not just blame the environment.

**Answer on KISS/YAGNI judgment criteria:**
- The data schema is the domain
- The measure of simplicity is 'the propagation scope of changes'

> **Counter-question**: Is it justified to triple the architecture complexity to solve business logic complexity? Can you definitively say that a system with 5 classes and 3 events is easier to maintain than 3,000 lines of procedural code?

---

### Proponent's Third Response

#### On the Distributed Transaction Problem — I Concede

When you mention Saga and Outbox Pattern, it is <strong>half right and half a misconception</strong>.

The correct part first: when combining DDD with microservices, many teams misunderstand Aggregate boundary = service boundary. Then, as you describe, a simple transaction becomes a distributed systems challenge. **This is indeed a common mistake, and some blame lies with DDD community overhype.**

But there is a misconception:

```java
// Aggregate separation != service separation != DB separation

// Aggregate boundaries are meaningful even within the same DB and same service
@Transactional
public void placeOrder(OrderCommand cmd) {
    Order order = Order.create(cmd);           // Order Aggregate
    inventory.reserve(order.getItems());       // Inventory Aggregate
    // Both Aggregates can be manipulated within the same transaction
    orderRepository.save(order);
    inventoryRepository.save(inventory);
}
```

Aggregate boundaries mean <strong>"these objects must always maintain consistency together"</strong>, not <strong>"they must be separated into different services"</strong>.

Eventual Consistency is needed when <strong>the business is inherently asynchronous</strong>. "Deduct inventory after payment completes" is inherently asynchronous. Forcing it into a single transaction means a payment system failure would paralyze the inventory system.

#### On the Git vs DDD "Category Error"

That they are different categories is correct. But <strong>is "5 people arguing for 5 days" a bad thing?</strong>

The essence of that debate is "what is Order," "does the discount policy belong to Order or is it a separate Policy." This is <strong>a process that deepens domain understanding</strong>.

What happens when you develop without DDD? The first developer just throws everything into `OrderService` as they see fit. Quick, without debate. Then 6 months later, a second developer creates `DiscountService` wondering "why is this logic here?" A year later, `OrderService`, `DiscountService`, `OrderDiscountHelper`, `DiscountCalculator` proliferate, and nobody understands the full picture.

<strong>5 days of debate</strong> is an investment that prevents <strong>1 year of chaos</strong>.

#### On "Data Schema Is the Domain"

Here is where I fundamentally disagree.

Data models and domain models are different:

| Perspective | Data Model | Domain Model |
|------------|-----------|-------------|
| Concern | Storage, normalization, query performance | Business rules, invariants |
| Example | `discount_amount` column in `orders` table | `Order.applyDiscount(policy)` method |
| Change Reason | Query optimization, index addition | Business rule change |

You said "with a DB schema, 80% of business rules are clear," but that is only true for <strong>CRUD apps</strong>.

"If the customer is VIP, it is Friday, and there are 3 or more items from a specific category in the cart, apply an additional 5% discount"

Where is this rule visible in the DB schema? It would be stored as JSON in a `discount_rules` table. And where is the logic to interpret it? Hidden somewhere in a 3,000-line service.

#### Answer to the "3x Complexity" Question

Let me reframe the question:

> "5 classes, 3 events vs 3,000 lines of procedural code — which is easier to maintain?"

<strong>In the short term</strong>, the 3,000 lines are easier. I concede that.

But <strong>in the long term</strong>, you need to ask:

1. **Testability**: Have you ever written unit tests for a 3,000-line service? You need DB mocking, external API mocking, dozens of setups for a single test. You end up not testing at all.

2. **Fear of change**: When modifying discount logic in 3,000 lines, are you confident "this change will not affect anything else"? If not, you <strong>avoid making changes</strong>. That is the beginning of legacy.

3. **Onboarding**: When a new developer joins, how long does it take to understand the 3,000-line service? Is "just follow the code" the best you can do?

5 classes and 3 events each have <strong>clear responsibilities</strong>. You can tell a new developer "discounts are in `DiscountPolicy`, order creation is in `Order.create()`, inventory integration is in the `InventoryReserved` event."

#### My Final Question

> **You said "find the best within the given environment." I agree.**
>
> But is the standard for "best" <strong>short-term productivity</strong> or <strong>long-term maintainability</strong>?
>
> For a startup MVP, 3,000 lines are fine. Validate the market quickly and throw it away.
>
> But would you make the same choice for a <strong>core system that will run for 5+ years</strong>?
>
> DDD is an investment not for <strong>"fast now"</strong> but for <strong>"still changeable 2 years from now."</strong>
> If the project does not need that investment, I would not recommend DDD either.

---

### Skeptic's Third Rebuttal

**1. "5 days of debate" becomes a "shackle" 6 months later**

Business is a living organism. Boundaries defined over 5 days become 'the hardest wall to change' when a new business model arrives 6 months later. Simple code can be deleted and rewritten. A 'solid castle' built with DDD requires reconstruction from the foundation.

**2. The illusion of testability**

DDD also drains energy on 'framework tests' — Aggregate test setup, event publication verification, etc. It would be more economical to extract just the complex parts from the 3,000 lines into static functions and test those.

**3. 'Data schema' is reality, 'domain model' is virtual**

Data is ultimately stored in the DB. Is splitting it into 5 SELECTs to respect Aggregate boundaries efficient? When performance optimization is needed, you inevitably regress to Native Queries.

> **Final question**: 'Separation of concerns' and 'appropriate abstraction' alone produce good code — why must you convert to the specific religion of 'DDD'? Can you not ensure long-term maintainability without DDD?

---

### Proponent's Fourth Response (Final)

#### Let me be clear first: I am not a "DDD fundamentalist"

Throughout this debate, I have emphasized <strong>"selective application"</strong>. I have never said to use DDD on every project. In fact, I said "use Transaction Script for Simple Domains."

So let me re-read the question:

> "'Separation of concerns' and 'appropriate abstraction' alone produce good code — why must you convert to the specific religion of DDD?"

**I agree.** Doing 'separation of concerns' and 'appropriate abstraction' well does produce good code.

But let me ask one thing:

> **Where do you get the criteria for "appropriate abstraction"?**

#### DDD Is Not a "Religion" but a "Lens"

The core of DDD is not Aggregate, Repository, or Factory. Those are <strong>tactical patterns</strong> and are optional.

The true core of DDD is:

1. **Ubiquitous Language**: Developers and business use the same terminology
2. **Bounded Context**: The same word can have different meanings depending on context
3. **Domain-centric thinking**: Understand the business problem first, not the technology

These are <strong>ways of thinking, not code patterns</strong>. You can apply them without using Aggregates.

```java
// Code applying DDD thinking without tactical patterns
public class OrderService {
    // If the team agreed to say 'receive order' instead of 'create order'
    public Order receiveOrder(OrderRequest request) {  // Not createOrder
        // Reflecting business terminology in code
    }
}
```

#### My Thoughts on Three Rebuttals

**1. "5 days of debate becomes a shackle"**

Partially correct. But <strong>only if you treat the debate results as "immutable truth"</strong>.

Good DDD teams view Bounded Contexts as <strong>"snapshots of current understanding"</strong>. When business changes, Contexts change too. The problem is not DDD but <strong>the mindset of "once decided, it can never change"</strong>.

**2. "The illusion of testability"**

I agree to an extent. Testing an entire Aggregate can involve complex setup.

But the testability I am talking about is this:

```java
// To test this in a 3,000-line service?
// -> DB mocking, external API mocking, countless dependencies...

// Extract it as a Value Object?
Money discountedPrice = originalPrice.applyDiscount(discountRate);
// -> Pure function, no dependencies, 3 lines of test
```

You said "extract just the complex parts as static functions" — <strong>that is exactly a Value Object</strong>. Just different names, same concept.

**3. "Data schema is reality"**

It is true that you regress to Native Queries for performance optimization. That is why <strong>CQRS</strong> was created:

- **Command (write)**: Protect business rules with the domain model
- **Query (read)**: Optimize performance with Native Queries

Separating the two lets you have both "domain model purity" and "query performance." DDD does not say "solve everything with Aggregates."

#### Conclusion: Where We Agree

| Topic | Skeptic | Proponent (Me) |
|-------|---------|----------------|
| DDD for every project? | Against | **Against** |
| Tactical pattern misuse? | A problem | **A problem** |
| Separation of concerns is important? | Agree | **Agree** |
| Understanding business comes first? | Agree | **Agree** |
| Simplicity is important? | Agree | **Agree** |

What we actually disagree on is <strong>one thing</strong>:

> **"In core systems with high domain complexity, do DDD's strategic/tactical tools provide long-term value?"**

I believe <strong>"yes."</strong> Selectively.
The pragmatic senior developer seems to believe <strong>"fundamentals alone are sufficient."</strong>

#### My Final Proposal

Instead of viewing DDD as a "religion," how about seeing it as a <strong>"toolbox"</strong>?

- Ubiquitous Language? -> Use it if it helps team communication
- Bounded Context? -> Use it if it helps set system boundaries
- Aggregate? -> Use it if transaction boundaries are complex
- Value Object? -> Use it if it helps express immutable data (you probably already do)

<strong>It is not a binary choice of using all or none.</strong>

I am also wary of people who shout "you must do DDD!" But dismissing "DDD is useless!" is equally dangerous.

> <strong>A true senior does not discriminate against tools. They simply choose the right tool for the situation.</strong>

---

### Skeptic's Final Closing

#### 1. Beware the Trap of "Naming Things"

What could simply be "reflect business terms well in code" — the moment you attach the grand name "Ubiquitous Language," team members fixate on form over substance. Good naming, separation of responsibilities, low coupling — these existed before DDD.

#### 2. 'Tools' should be lightweight to be tools

Value Objects are 'good abstractions,' and Bounded Contexts are 'well-defined module boundaries.' Rather than selling these as a set under the name 'DDD,' we should return them to the domain of <strong>'engineering common sense'</strong>.

#### 3. On 'The Role of a Senior'

> "A true senior <strong>calculates the 'cost'</strong> a tool will bring first."

The possibility of change 2 years from now is important, but deploying today to keep the business alive is a senior's first duty.

#### Closing

Though our methodologies differ, it was a productive session confirming we share the same <strong>passion for "wanting to build better software."</strong>

---

### Proponent's Final Closing

#### Agreement and Rebuttal on "Naming Things"

Correct. Names often obscure the essence. "Ubiquitous Language" sounds grand, but it is ultimately <strong>"use the same words in code that you use in specs."</strong>

But names also have value. When there is a <strong>shared vocabulary</strong>, communication between teams speeds up. When someone says "just split it into a Bounded Context," a 30-minute explanation becomes 30 seconds among people who know DDD.

The problem is not the names but <strong>blind faith in names</strong>.

#### I Fully Agree That "You Calculate Costs First"

I also agree that "deploying today to keep the business alive" is the priority.

Just one addition:

> <strong>You should also calculate whether "today's fast deployment" creates "tomorrow's slow deployment."</strong>

Calculating both is the role of a senior, in my opinion.

#### What I Gained from This Debate

1. Confirmed again that the DDD community's <strong>overhype</strong> has backfired in the field
2. The expression <strong>"fundamentals"</strong> can sometimes convey the same values more effectively than "DDD"
3. <strong>The importance of cost calculation</strong> — you should not ignore short-term costs by only looking at long-term benefits

#### Closing

If the pragmatic senior developer serves as the <strong>"safety mechanism watching the blade,"</strong> I will continue serving as the <strong>"eyes that recognize when the blade is needed."</strong>

Ultimately, we both aim for the same goal: <strong>"helping the team build good software."</strong> Stepping back from the methodology debate, when different perspectives coexist in a team, more balanced decisions become possible.

Thank you for your effort. It was a productive debate.

---

## Debate Summary

### Key Points of Contention

| Topic | Skeptic (Pragmatist) | Proponent (DDD Advocate) |
|-------|---------------------|--------------------------|
| Essence of DDD | A methodology that induces excessive complexity | A lens for domain-centric thinking |
| Tactical patterns | Mostly unnecessary, fundamentals suffice | Selectively useful |
| Learning cost | A flaw of the methodology | Worth the investment for complex domains |
| Long-term maintenance | Simple code is more advantageous | DDD provides changeability |

### Points of Agreement

1. **DDD should not be applied to every project**
2. **Misuse of tactical patterns is a problem**
3. **Separation of concerns and business understanding are key**
4. **Tools should be chosen to fit the situation**
5. **Both short-term costs and long-term benefits must be calculated**

### Remaining Differences

> "In core systems with high domain complexity, do DDD's strategic/tactical tools provide additional value beyond 'fundamentals'?"

- **Skeptic**: Fundamentals (separation of concerns, good abstraction) are sufficient
- **Proponent**: For complex domains, DDD tools provide additional value

---

*Debate concluded: 2026-01-17*
