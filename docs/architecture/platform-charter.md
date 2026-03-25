# Platform Charter

> **Status:** Active — foundational document.
> **Purpose:** Defines why the platform exists, what it guarantees, what it is not, and how it extends.

---

## Purpose

The platform exists to let people build and run real operational flows — without reinventing the same foundation every time.

This means a structured process where work is:
- defined against real-world entities,
- assigned to people or teams,
- captured through forms and submissions,
- tracked over time,
- and kept traceable and reusable.

The platform is not "a data collection tool." It is a set of composable tools and shared patterns that support end-to-end operational processes across different domains — inventory reporting, health facility registers, LMIS flows, campaign execution, assignment tracking — without hard-coding any single domain's logic into the core.

---

## Core Guarantees

These properties must hold regardless of how the platform evolves. They serve as fitness functions — if any of these break, the platform has failed.

1. **Identity stability**
   Entities — whether they are actors (users, teams) or subjects in data (facilities, warehouses, org units, items) — must carry stable, globally unique identifiers that persist across time, environments, and system changes.

2. **Data traceability**
   Every submission or action must remain traceable to who did it, when, under what context, and against which entity. This traceability must survive system evolution.

3. **Core generality**
   The core must support new domains without baking domain-specific logic into the foundation. Domain semantics belong in extensions, not in the core.

4. **Uniform experience**
   The platform must feel like one system:
   - Uniform API conventions and query model for common operations.
   - Consistent identity model across all entity types.
   - Consistent way to query status, progress, and history.
   - Consistent sync contract for mobile/offline clients.
   
   Where a domain requires specialized UX (e.g., a campaign war-room dashboard), it may have a purpose-built interface — but data produced by that interface must still be queryable and reportable through the common platform model.

5. **Non-breaking evolution**
   Adding a new domain capability (e.g., campaign planning) must not break existing working flows (e.g., inventory reporting, facility registry). Extensions compose onto the core; they do not rewrite it.

6. **Pattern reusability**
   Common operational patterns — assignment, scheduling, state tracking, timing, linking — should be shared across domains, not rebuilt differently each time.

---

## Explicit Non-Goals

The platform must NOT become:

- A complete, fixed business system for any single domain (not a full LMIS, not a full campaign system, not a full inventory system)
- A place where every special-case rule gets baked into the core
- So abstract that nothing concrete can be built on it
- So generic that it loses practical usefulness
- So rigid that every new domain must fit one predefined shape

Domain-specific systems (campaigns, LMIS, inventory management) are built *on top of* the platform by composing its shared patterns — they are not *part of* the platform itself.

---

## Extensibility Model

The platform uses a three-layer composition model:

```
Core        → provides: identity, capture, events, state, stable references
Patterns    → provide: assignment, scheduling, scope, linking (inside Flows)
Domains     → compose patterns into: campaigns, LMIS, inventory, etc.
```

**Core** is general-purpose, domain-agnostic, owns the six first-class constructs (see [baseline-v1.md](baseline-v1.md)).

**Patterns** are reusable building blocks for common operational concerns. They live inside Flows as composable aspects — not as independent modules.

**Domains** are specific solutions assembled from patterns. They add the domain-specific 20–30%: specialized validation, domain rules, purpose-built UX, integration hooks. They do not rewrite the core or reimagine shared patterns.

> Extensibility means: adding the missing 20–30% for a domain, without rewriting the 70% that is common.

---

## Configuration Principle

Configuration is a first-class concern. Setting up a new domain flow (a campaign, an inventory cycle, a facility register) should feel like configuring a system, not building one from scratch.

The method of configuration (UI, structured config files, API, or a combination) is not yet decided (see [open-decisions-log.md](open-decisions-log.md#od-001-configuration-mechanism)). But the principle is locked: the platform must support declarative setup of operational flows without requiring custom code for common patterns.

---

## Evolutionary Architecture Principle

The platform uses evolutionary architecture: decisions are deferred to the last responsible moment, validated through real usage, and protected by fitness functions (the core guarantees above). Instead of predicting how components will be reused, the platform supports adaptability through proper abstractions, test suites, boundary enforcement, and incremental refactoring.

---

## Related Documents

| Document | Purpose |
|---|---|
| [baseline-v1.md](baseline-v1.md) | What is built — the 6 constructs, frozen decisions |
| [open-decisions-log.md](open-decisions-log.md) | What is intentionally deferred |
| [legacy-boundary-strategy.md](legacy-boundary-strategy.md) | How legacy and new code coexist |
