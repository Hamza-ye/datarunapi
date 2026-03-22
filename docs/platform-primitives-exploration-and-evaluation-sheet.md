
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
| - General-purpose primitives                                 |
| - Core tools supporting most domains (~70%)                 |
| - Observability, fitness functions, stability checks       |
+-------------------------------------------------------------+
            ^                  ^                 ^
            |                  |                 |
            |                  |                 |
+-----------+---------+  +-----+-------+  +------+--------+
|  Overlay / Extension |  |  BC / Utility |  | Optional UX  |
| - Domain-specific or |  |  Supporting  |  | modules /    |
|   specialized logic  |  |  optional   |  | dashboards   |
| - Incrementally added|  |  primitives |  |              |
| - Integrates w/ core |  |  Enhances   |  |              |
+---------------------+  +-------------+  +--------------+
            ^                  ^                 ^
            |                  |                 |
       Evolves independently, feedback-driven, guided by platform goals
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
