# System Goals & Boundaries

> **Status:** Foundational — defines what the platform is NOT and how it extends.

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

**Core** is general-purpose, domain-agnostic, owns the six first-class constructs (see [baseline-v1.md](../02-baseline/baseline-v1.md)).

**Patterns** are reusable building blocks for common operational concerns. They live inside Flows as composable aspects — not as independent modules.

**Domains** are specific solutions assembled from patterns. They add the domain-specific 20–30%: specialized validation, domain rules, purpose-built UX, integration hooks. They do not rewrite the core or reimagine shared patterns.

> Extensibility means: adding the missing 20–30% for a domain, without rewriting the 70% that is common.

---

## Configuration Principle

Configuration is a first-class concern. Setting up a new domain flow (a campaign, an inventory cycle, a facility register) should feel like configuring a system, not building one from scratch.

The method of configuration (UI, structured config files, API, or a combination) is not yet decided (see [open-decisions-log.md](../02-baseline/open-decisions-log.md#od-001-configuration-mechanism)). But the principle is locked: the platform must support declarative setup of operational flows without requiring custom code for common patterns.

---

## Evolutionary Architecture Principle

The platform uses evolutionary architecture: decisions are deferred to the last responsible moment, validated through real usage, and protected by fitness functions (the core guarantees in [platform-ambition.md](platform-ambition.md)). Instead of predicting how components will be reused, the platform supports adaptability through proper abstractions, test suites, boundary enforcement, and incremental refactoring.
