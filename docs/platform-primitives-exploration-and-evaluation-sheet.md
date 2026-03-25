
# 📝 Platform Primitives Exploration & Evaluation Sheet

## Context

The platform provides a robust set of general-purpose tools and primitives that satisfy the majority of common domain needs (~70%), without embedding domain-specific semantics into the core. Optional extensions—such as overlays, bounded contexts, or utility modules—can cover specialized domain requirements (~20–30%), while integrating seamlessly to provide a uniform experience. The goal is an incremental, composable, and flexible platform that adapts to evolving domain needs.

---

## Evolutionary Architecture

In contrast to traditional up-front, heavy-weight enterprise architectural designs, we recommend adopting evolutionary architecture. It provides the benefits of enterprise architecture without the problems caused by trying to accurately predict the future. Instead of guessing how components will be re-used, evolutionary architecture supports adaptability, using proper abstractions, database migrations, test suites, continuous integration and refactoring to harvest re-use as it occurs within a system. The driving technical requirements for a system should be identified early to ensure they are properly handled in subsequent designs and implementations. We advocate delaying decisions to the latest responsible moment, which might in fact be up-front for some decisions. 

### Conceptual Map

```
+-------------------------------------------------------------+
|                         Platform Core                       |
|-------------------------------------------------------------|
| - General-purpose primitives                                |
| - Core tools supporting most domains (~70%)                 |
| - Observability, fitness functions, stability checks        |
+-------------------------------------------------------------+
            ^                  ^                    ^
            |                  |                    |
            |                  |                    |
+-----------+----------+   +-----+------+     +------+-------+
|  Overlay / Extension |   | BC/Utility |     |  Optional UX |
| - Domain-specific or |   | Supporting |     |   modules /  |
|   specialized logic  |   | optional   |     | dashboards   |
| - Incrementally added|   | primitives |     |              |
| - Integrates w/ core |   | Enhances   |     |              |
+----------------------+   +------------+     +--------------+
            ^                  ^                    ^
            |                  |                    |
       Evolves independently, feedback-driven,  guided by platform goals
```

### Key Concepts Shown

1. **Core remains agnostic and generic**

   * Provides primitives and tools
   * Supports most domains but does **not bake in domain semantics**

2. **Overlays / BCs / Utilities are optional extensions**

   * Can be added incrementally
   * Provide specialized functionality
   * Plug into core **without breaking uniform experience**

3. **Evolutionary Flow**

   * Each layer can **evolve independently**
   * Changes guided by **fitness functions** (e.g., reliability, traceability)
   * Feedback loops ensure **incremental improvement**

4. **Decision Points are open**

   * Diagram does **not imply which primitives or overlays are mandatory**
   * Leaves space for experts to propose, score, and prioritize

---

## Platform Ambition Statement

### Purpose

The platform exists to let people build and run real operational flows — without reinventing the same foundation every time.

This means a structured process where work is:
- defined against real-world entities,
- assigned to people or teams,
- captured through forms and submissions,
- tracked over time,
- and kept traceable and reusable.

The platform is not "a data collection tool." It is a set of composable tools and shared patterns that support end-to-end operational processes across different domains — inventory reporting, health facility registers, LMIS flows, campaign execution, assignment tracking — without hard-coding any single domain's logic into the core.

### Core Guarantees

These properties must hold regardless of how the platform evolves. They serve as fitness functions — if any of these break, the platform has failed.

1. **Identity stability**
   Entities — whether they are actors (users, teams) or subjects in data (facilities, warehouses, org units, items) — must carry stable, globally unique identifiers that persist across time, environments, and system changes.

2. **Data traceability**
   Every submission or action must remain traceable to who did it, when, under what context, and against which entity. This traceability must survive system evolution.

3. **Core generality**
   The core must support new domains without baking domain-specific logic into the foundation. Domain semantics belong in extensions, not in the core.

4. **Uniform experience**
   The platform must feel like one system. This means:
   - Uniform API conventions and query model for common operations.
   - Consistent identity model across all entity types.
   - Consistent way to query status, progress, and history.
   - Consistent sync contract for mobile/offline clients.
   
   Where a domain requires specialized UX (e.g., a campaign war-room dashboard), it may have a purpose-built interface — but data produced by that interface must still be queryable and reportable through the common platform model.

5. **Non-breaking evolution**
   Adding a new domain capability (e.g., campaign planning) must not break existing working flows (e.g., inventory reporting, facility registry). Extensions compose onto the core; they do not rewrite it.

6. **Pattern reusability**
   Common operational patterns — assignment, scheduling, state tracking, timing, linking — should be shared across domains, not rebuilt differently each time.

### Explicit Non-Goals

The platform must NOT become:

- A complete, fixed business system for any single domain (not a full LMIS, not a full campaign system, not a full inventory system)
- A place where every special-case rule gets baked into the core
- So abstract that nothing concrete can be built on it
- So generic that it loses practical usefulness
- So rigid that every new domain must fit one predefined shape

Domain-specific systems (campaigns, LMIS, inventory management) are built *on top of* the platform by composing its shared patterns — they are not *part of* the platform itself.

### Extensibility Model

The platform uses a three-layer composition model:

```
Core        → provides: identity, capture, events, stable references
Patterns    → provide: assignment, scheduling, state, scope, linking
Domains     → compose patterns into: campaigns, LMIS, inventory, etc.
```

**Core** is general-purpose, domain-agnostic, owns the primitives.

**Patterns** are reusable building blocks for common operational concerns. They are composable primitives, not opinionated templates — meaning they can be combined in different ways for different domains. A task pattern, a schedule pattern, and a scope pattern can be composed independently.

**Domains** are specific solutions assembled from patterns. They add the domain-specific 20–30%: specialized validation, domain rules, purpose-built UX, integration hooks. They do not rewrite the core or reimagine shared patterns.

> Extensibility means: adding the missing 20–30% for a domain, without rewriting the 70% that is common.

**Criterion for shared patterns:** if three or more domains need the same capability (e.g., recurring schedules, approval workflows, supervisor review), it is a candidate for a shared pattern rather than domain-specific code.

### Configuration Principle

Configuration is a first-class concern. Setting up a new domain flow (a campaign, an inventory cycle, a facility register) should feel like configuring a system, not building one from scratch.

The method of configuration (UI, structured config files, API, or a combination) is not yet decided. But the principle is locked: the platform must support declarative setup of operational flows without requiring custom code for common patterns.

### Deferred Decisions

The following are deliberately not decided yet. They will be resolved at the last responsible moment, informed by real usage and validated slices:

- **Specific pattern inventory.** Which shared patterns the platform provides (assignment, scheduling, state machine, scope, etc.) will be confirmed through real domain composition, not pre-designed.
- **Configuration mechanism.** Whether config is JSON files, admin UI, API-driven, or a hybrid.
- **Event infrastructure.** How events are propagated (in-process, message bus, outbox). To be decided when the integration model demands it.
- **Entity resolution architecture.** How entities register, resolve, and expose themselves for form references and reporting. The need is clear; the specific pattern is not locked.
- **Module boundaries.** Whether patterns and domains are Java packages, Maven modules, or something else. To be decided when the first domain composition is implemented.
- **Mobile sync contract.** How the mobile client builds its execution context from the new model. To be decided as part of the first end-to-end slice.

### First Validation Slice

The platform model is proven when one real operational flow works end-to-end using core primitives and shared patterns:

- Define the flow (e.g., monthly inventory reporting OR a campaign cycle)
- Attach it to real-world entities (facilities, teams)
- Assign responsibilities
- Capture submissions
- Track status and completion
- Report results

This must demonstrate that core primitives are useful, shared patterns are composable, extensions do not rewrite the core, and the system supports a real process without becoming a mess.

---

## Architectural Constructs Exploration

This section explores candidate constructs across the three layers (Core, Patterns, Domains), how they interact, and the trade-offs between flexibility and control.

> **Status:** This is an exploration — nothing here is locked. The purpose is to weigh candidates, surface trade-offs, and identify what needs real validation.

---

### Layer 1: Core Primitive Candidates

These are the foundational building blocks. They must be domain-agnostic, own stable contracts, and work without any pattern or domain overlay being present.

| Candidate | What it does | Why core | Risk if deferred |
|---|---|---|---|
| **Entity** | Anything the system knows about: a facility, a warehouse, a team, a user, an item, an org unit. Carries stable identity `(id, uid, code, name, type)`. Can be both a subject in data and an actor. | Every domain references entities. Without a shared entity model, each domain reinvents identity — killing uniformity and reporting. | Domains build their own incompatible entity models; cross-domain reporting requires ETL glue. |
| **Form / Template** | Defines what data to capture. Versioned. Meta-driven (fields, sections, validation rules as config). | Data capture is the platform's primary function. Everything else — assignment, tracking, reporting — depends on having structured capture. | Already exists and is strong. No risk. |
| **Submission / Capture** | A filled form instance. Linked to a template version and to contextual entities (who, where, when, what). | The atomic unit of data in the system. Without it, there is no platform. | Already exists. No risk. |
| **Entity Reference** | A form field type that points to any registered entity. Resolved at capture time, stored as `(entity_type, entity_uid)`. | Forms need to reference real-world things (pick a facility, select an item, choose a team). Without a generic reference mechanism, each entity type requires custom form logic. | Forms can only reference hardcoded entity types. Adding a new entity type requires code changes. |
| **Event** | Something happened: a submission was created, an entity was updated, a state changed. The audit and integration backbone. | Traceability is a core guarantee. Events are how the system records what happened and how other layers react. | Traceability becomes after-the-fact reconstruction from database diffs instead of first-class records. |
| **Identity** | User accounts, authentication, basic profile. Who is interacting with the system. | Every operation needs an authenticated actor. | Cannot defer — system cannot function without it. |

#### Entity: the construct that needs the most discussion

Entities have a dual nature: they can be **subjects** (something you collect data about) and **actors** (something that performs work). A facility submits data AND is a data point in reports. A team member fills forms AND is referenced in assignments.

**Open question:** Should entity carry **type-specific structure as config**?

Example: an entity of type `org_unit` declares `"structure": "hierarchical"` in its type definition, and the platform enforces parent-child constraints. An entity of type `option` declares `"structure": "flat_list"`. The platform doesn't need to know what an "org unit" is — it knows how to enforce "hierarchical."

This is the difference between:
- **Core knows entity types** (org unit BC, team BC, reference data BC — the current RFC approach)
- **Core knows entity structures** (hierarchical, flat, grouped — and entity types are config on top)

Both work. The second gives more flexibility but requires a solid constraint/validation engine.

---

### Layer 2: Shared Pattern Candidates

These are reusable operational building blocks. They sit between core and domain — common enough to share, specific enough to be useful. The criterion: if 3+ domains need it, it's a pattern candidate.

| Candidate | What it does | Domains that use it | Composable with |
|---|---|---|---|
| **Assignment** | Links a responsibility to an actor (person, team, role) for a scope of work. Answers "who should do what." | Campaigns (team → targets), Inventory (facility staff → monthly report), Registry (supervisor → review) | Scope, Schedule, State |
| **Schedule** | Defines when something should happen. Recurring (monthly, weekly), one-time (campaign window), or triggered (on event). | Inventory (monthly cycle), LMIS (weekly), Campaigns (6-day window), Registry (on-demand) | Assignment, State |
| **State / Lifecycle** | Tracks progression through stages. E.g., planned → active → submitted → verified → closed. Configurable per use case. | Campaigns (planned → executing → reconciling), Inventory (open → submitted → approved), Registry (pending → approved) | Assignment, Schedule |
| **Scope** | Defines what entities something applies to (which facilities, which region, which item categories). Can be explicit list or rule-based. | Campaigns (target org units), Inventory (facilities with warehouses), LMIS (all health facilities in district X) | Assignment, Schedule |
| **Approval / Review** | One actor submits, another reviews and approves or rejects. Optionally with comments and revision cycle. | Inventory (supervisor approval), Registry (new facility review), some campaign flows | State, Assignment |
| **Grouping / Hierarchy** | Organize entities into trees, groups, or sets. Traversable for scoping and filtering. | Org unit hierarchy, item categorization, regional groupings | Scope, Entity |

#### How patterns compose — example

A **monthly inventory report** is:
```
Schedule(monthly, day=5)
  + Scope(entity_type=facility, filter=has_warehouse)
  + Assignment(actor=facility_pharmacist, role=reporter)
  + State(draft → submitted → approved)
  + Approval(reviewer=district_supervisor)
```

A **6-day mass campaign** is:
```
Schedule(one_time, start=jun_1, end=jun_6)
  + Scope(entity_type=org_unit, filter=district_X_villages)
  + Assignment(actor=campaign_team_A, role=executor)
  + State(planned → active → completed → reconciled)
```

Same patterns, different composition. Neither requires the other to exist.

---

### Layer 3: Domain / Overlay Candidates

These are not built yet. They emerge when a real use case requires more than what patterns provide.

| Candidate | What it adds beyond patterns | Why it's an overlay, not core |
|---|---|---|
| **Campaign Management** | Campaign-specific logic: target coverage calculations, medicine consumption aggregation, temporary team/warehouse spin-up, cross-campaign reconciliation. | Not every domain runs campaigns. The patterns (schedule, scope, assignment) cover the 70%. The campaign overlay adds the domain-specific 30%. |
| **LMIS / Inventory** | Stock tracking, consumption calculations, stock-out alerts, supply chain flow (regional WH → facility). | Inventory semantics (stock in, stock out, balance) are domain-specific. The capture and scheduling are reusable. |
| **Facility Registry** | Registry-specific workflows: registration, verification, status tracking, attribute standardization. | A registry is a specific use of entities + state + approval. The overlay adds registry governance rules. |
| **Planning & Tracking** | Rich planning artifacts (plans, phases, milestones, success criteria), progress dashboards, plan-vs-actual analysis. | Heavy planning logic is not needed by every domain. Simple domains use assignment + state directly. |
| **Analytics / Reporting** | Pivot tables, cross-domain aggregation, trend analysis, custom dashboards. | Reporting logic should not live in operational code. Separate concern, separate evolution. |

---

### Interaction Patterns

How do layers talk to each other? Three models to weigh:

#### Model A: Direct Composition

```
Domain calls Pattern APIs directly.
Pattern calls Core APIs directly.
Synchronous. Simple call chain.
```

**Pro:** Easiest to build, debug, and reason about. No infrastructure overhead. Works in a monolith.
**Con:** Creates compile-time coupling between layers. Harder to evolve independently. A pattern change can ripple to domains.
**Control:** High — you see every call path.
**Risk:** Coupling creep. Over time, domains start calling core directly, bypassing patterns.
**Mitigation:** Enforce layer boundaries through package structure and architecture tests (e.g., ArchUnit).

#### Model B: Event-Driven Reaction

```
Core emits events (submission created, entity updated).
Patterns react to events (schedule evaluates, state transitions).
Domains react to pattern events (campaign checks coverage).
Asynchronous. Decoupled.
```

**Pro:** Maximum decoupling. Layers can evolve independently. Easy to add new reactions without modifying emitters.
**Con:** Harder to debug (event flows are invisible). Eventual consistency. Requires event infrastructure decisions.
**Risk:** Event spaghetti — too many implicit reactions, nobody can trace why something happened.
**Mitigation:** Keep event vocabulary small and well-documented. Use correlation IDs. Start with in-process synchronous events (Spring `ApplicationEventPublisher`), not a message bus.

#### Model C: Configuration-Driven Interpretation

```
Domain defines a configuration (JSON/YAML).
Platform interprets the config and wires patterns together.
Core provides the runtime engine.
```

Example: a domain config says:
```json
{
  "flow": "monthly_inventory",
  "schedule": {"type": "recurring", "period": "monthly", "day": 5},
  "scope": {"entity_type": "facility", "filter": "has_warehouse"},
  "assignment": {"role": "facility_pharmacist"},
  "lifecycle": ["draft", "submitted", "approved"],
  "capture": {"template": "inventory_form_v3"}
}
```

**Pro:** Maximum flexibility. Non-developers can configure flows. New domains without new code.
**Con:** Hardest to build. Requires a configuration interpretation engine. Validation complexity. Debugging config errors is notoriously hard.
**Risk:** Becoming an accidental low-code platform — building a DSL/engine that is itself harder to maintain than the domains it replaced.
**Mitigation:** Start with code-driven composition (Model A), extract config-driven setup only after you have 3+ domains working and you see the real commonalities. Don't pre-build the config engine.

#### Recommended approach: Start A, earn C

```
Phase 1: Model A (direct composition)
  → Build the first domain slice by composing patterns in code
  → Validate that patterns are actually reusable
  → Learn what the natural configuration surface is

Phase 2: Model B (selective events)
  → Add events for cross-cutting concerns (audit, tracing, progress tracking)
  → Keep most composition synchronous
  → Events for integration, not orchestration

Phase 3: Model C (config-driven — if warranted)
  → Extract common flow definitions into config
  → Only after real evidence shows what's configurable
  → Never for control flow or complex validation
```

This isn't a locked recommendation — it's a starting position to react to.

---

### Flexibility vs. Control Spectrum

Every design choice sits on this spectrum:

```
← More Control                                    More Flexibility →

Hardcoded logic    Code composition    Config-driven    Full DSL/low-code
(one way to do it) (patterns in code)  (declarative)    (users define flows)

Risk: rigidity     Risk: coupling      Risk: complexity  Risk: building a platform
                                                         for building platforms
```

**Your stated position:** composable primitives (code composition), with the door open to config-driven later. This sits in the sweet spot — flexible enough to support multiple domains, controlled enough that you can trace behavior and debug it.

**"If control is manageable through a strategy, it doesn't mean we lost control."** This is the right frame. For each construct, the question is:

1. **What am I giving up?** (visibility, enforcement, simplicity)
2. **What am I gaining?** (flexibility, reuse, domain independence)
3. **Is the risk manageable?** (through tests, conventions, fitness functions — not hope)

---

### Open Questions for Discussion

1. **Entity type definitions: code or config?**
   Should "org_unit is hierarchical" be a Java class or a JSON type definition that the entity engine interprets?

2. **Pattern boundaries: are patterns modules or just conventions?**
   Is "Assignment" a separate Java package with its own contract? Or is it a set of classes that domains import and wire up themselves?

3. **Where does access control live?**
   Is it a core primitive (every operation goes through it), a pattern (composed into flows that need it), or a cross-cutting concern (interceptor/filter layer)? 

4. **Is there a "flow" construct?**
   The composition of patterns (schedule + scope + assignment + state + capture) into a working operational flow — is that itself a first-class concept with an identity and lifecycle? Or is it just the domain wiring code? 

5. **What is the minimum for the first validation slice?**
   Which of these candidates must be real (not stubbed) to prove the model works end-to-end?

