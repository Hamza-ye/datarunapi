# Architecture Exploration Record

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---

## Core Primitive Candidates (as explored)

These were the foundational building blocks evaluated. They had to be domain-agnostic, own stable contracts, and work without any pattern or domain overlay being present.

| Candidate | What it does | Why core | Risk if deferred |
|---|---|---|---|
| **Entity** | Anything the system knows about: a facility, a warehouse, a team, a user, an item, an org unit. Carries stable identity `(id, uid, code, name, type)`. | Every domain references entities. Without a shared entity model, each domain reinvents identity. | Domains build incompatible entity models; cross-domain reporting requires ETL glue. |
| **Form / Template** | Defines what data to capture. Versioned. Meta-driven (fields, sections, validation rules as config). | Data capture is the platform's primary function. | Already exists and is strong. |
| **Submission / Capture** | A filled form instance. Linked to a template version and to contextual entities (who, where, when, what). | The atomic unit of data in the system. | Already exists. |
| **Entity Reference** | A form field type that points to any registered entity. Resolved at capture time, stored as `(entity_type, entity_uid)`. | Forms need to reference real-world things without custom logic per entity type. | Adding a new entity type requires code changes. |
| **Event** | Something happened: a submission was created, an entity was updated, a state changed. The audit and integration backbone. | Traceability is a core guarantee. | Traceability becomes after-the-fact reconstruction. |
| **Identity** | User accounts, authentication, basic profile. | Every operation needs an authenticated actor. | Cannot defer. |

> **Resolution:** All six became first-class constructs in the baseline. Identity is handled through the Entity construct (users are entities). See [baseline-v1.md](baseline-v1.md#1-first-class-constructs-6).

---

## Shared Pattern Candidates (as explored)

Reusable operational building blocks evaluated for the layer between core and domain. Criterion: if 3+ domains need it, it's a pattern candidate.

| Candidate | What it does | Domains that use it |
|---|---|---|
| **Assignment** | Links a responsibility to an actor for a scope of work. | Campaigns, Inventory, Registry |
| **Schedule** | Defines when something should happen — recurring, one-time, or triggered. | Inventory (monthly), LMIS (weekly), Campaigns (window) |
| **State / Lifecycle** | Tracks progression through stages (planned → active → submitted → verified). | Campaigns, Inventory, Registry |
| **Scope** | Defines what entities something applies to. Can be explicit list or rule-based. | Campaigns, Inventory, LMIS |
| **Approval / Review** | One actor submits, another reviews and approves or rejects. | Inventory, Registry |
| **Grouping / Hierarchy** | Organize entities into trees, groups, or sets. | Org units, item categorization |

> **Resolution:** State became a first-class construct. Assignment, Schedule, Scope, and Approval became patterns inside Flow. Hierarchy became an EntityType structure config. See [baseline-v1.md](baseline-v1.md#2-what-is-not-a-separate-construct).

---

## Interaction Models (as evaluated)

Three models for how layers communicate:

### Model A: Direct Composition
Domain calls Pattern APIs directly, Pattern calls Core APIs directly. Synchronous.

- **Pro:** Easiest to build, debug, reason about.
- **Con:** Compile-time coupling. Pattern changes ripple to domains.
- **Mitigation:** Enforce layer boundaries with ArchUnit.

### Model B: Event-Driven Reaction
Core emits events, Patterns react, Domains react to pattern events. Asynchronous.

- **Pro:** Maximum decoupling. Layers evolve independently.
- **Con:** Harder to debug. Eventual consistency. Requires event infrastructure.
- **Mitigation:** Small event vocabulary. Correlation IDs. Start with in-process synchronous events.

### Model C: Configuration-Driven Interpretation
Domain defines a config (JSON/YAML), platform interprets and wires patterns together.

- **Pro:** Maximum flexibility. Non-developers can configure flows.
- **Con:** Hardest to build. Requires a config interpretation engine.
- **Mitigation:** Start code-driven, extract config only after 3+ domains prove the commonalities.

> **Resolution:** Start with Model A (direct composition in code), use Model B selectively (events for audit, cross-cutting concerns). Model C deferred until real evidence shows what's configurable. See [open-decisions-log.md](open-decisions-log.md#od-001-configuration-mechanism).

---

## Flexibility vs. Control Spectrum

```
← More Control                                    More Flexibility →

Hardcoded logic    Code composition    Config-driven    Full DSL/low-code
(one way to do it) (patterns in code)  (declarative)    (users define flows)

Risk: rigidity     Risk: coupling      Risk: complexity  Risk: building a platform
                                                         for building platforms
```

**Decision:** Composable primitives (code composition), with the door open to config-driven later. This sits in the sweet spot — flexible enough to support multiple domains, controlled enough that behavior is traceable and debuggable.
