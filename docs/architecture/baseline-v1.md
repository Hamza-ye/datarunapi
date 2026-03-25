# Architecture Baseline v1

> **Status:** Frozen — 2026-03-25
> **Scope:** Platform architecture direction for the new era. This baseline governs all implementation design going forward.

---

## Summary

The platform uses a hybrid architecture with **six first-class constructs** organized across two complementary organizing principles:

- **Core primitives** (Entity, Capture, Entity Reference, State, Event) — domain-agnostic, stable, shared by everything.
- **Flow** — the process-centric organizing construct for operational work.

Entity-centric concerns (lifecycle, structure) are handled through Core primitives directly (Entity + State). Process-centric concerns (scheduling, assignment, tracking, approval) are handled through Flow.

Flows and Entities interact through Events, not direct coupling.

```
┌─────────────────────────────────────────────────┐
│                  Domain Overlays                 │
│  (campaigns, LMIS, registry — compose patterns) │
└────────────────────┬────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │        Flow         │
          │  (process-centric)  │
          │  schedule, scope,   │
          │  assignment, tasks, │
          │  progress           │
          └──────────┬──────────┘
                     │ uses
┌────────────────────▼────────────────────────────┐
│                    Core                          │
│  Entity  │  Capture  │  State  │  Event  │  Ref │
│  (identity, forms, state machines, audit, links) │
└──────────────────────────────────────────────────┘
```

---

## Frozen Decisions

These are committed. They should be treated as the working baseline and not reopened.

### 1. First-class constructs (6)

| Construct | Responsibility |
|---|---|
| **Entity** | Stable identity for anything the platform tracks. Carries `(id, uid, code, name, type)` and typed attributes. EntityType defines structural constraints (hierarchical, flat, grouped) as config. |
| **Capture** | Form definition (FormTemplate → FormVersion → FormField) and submission (Submission). Versioned. Meta-driven. The platform's primary operational function. |
| **Entity Reference** | Typed, resolvable links between any constructs. A form field can reference any entity. A submission carries entity references as context. |
| **State** | Universal state machine. StateDefinition (states, transitions, guards) is reusable. StateInstance is attached to entities, submissions, or flow tasks. Transition guards are role-based, condition-based, or both. |
| **Event** | Immutable record of something that happened. Carries actor, subject, timestamp, payload, and correlation ID. The audit backbone and integration mechanism. |
| **Flow** | Process-centric operations. FlowDefinition (schedule, scope, capture template, actors, lifecycle). FlowInstance (one occurrence — e.g., one month). FlowTask (one target entity × one actor × one submission). FlowProgress (aggregate status). |

### 2. What is NOT a separate construct

| Concern | Where it lives | Why not separate |
|---|---|---|
| Assignment | Pattern inside Flow (ActorRole + FlowTask.assignedActors) | Only meaningful in the context of a flow or entity relationship |
| Schedule | Pattern inside Flow (FlowDefinition.schedule) | A schedule without a flow is just a timer — not a platform concern |
| Scope | Pattern inside Flow (FlowDefinition.scope) | Defines the target set for a flow instance |
| Approval | Composition of State transitions with role guards | Not a distinct mechanism — it's state + assignment |
| Hierarchy | EntityType.structure config (HIERARCHICAL) | A structural property of entity types, not a separate concern |
| Grouping | EntityType.structure config (GROUPED) | Same — entity type configuration |
| Transfer | Flow where capture template includes sender/receiver/items | No separate construct needed — a distribution flow with entity references |

### 3. Scope timing

Scope is **fixed when a FlowInstance is created**. The entities in scope for March are determined on March 1. The denominator does not change mid-cycle.

Exceptions are handled through **explicit scope amendments**: an admin adds or removes an entity from a running FlowInstance. This is an auditable action (produces an Event).

### 4. FlowTask granularity

One FlowTask = **one target entity × one assigned actor × one primary submission**.

- Inventory: facility × pharmacist × month
- Campaign: village × team × phase
- Supervision: facility × supervisor × quarter

One FlowTask binds to one primary capture template. Supplementary captures are standalone submissions linked to the same target entity via EntityReference.

### 5. Flow ↔ Entity interaction

Flows and Entity lifecycle are **independent**. They interact through **Events only**.

- Entity state changes (e.g., facility suspended) emit Events.
- Flows do not listen to entity events by default.
- Future Reactions can connect them (e.g., "on entity suspended, flag active FlowTasks").
- No direct coupling between Flow and Entity lifecycle.

### 6. Basic capture without Flow

A Submission can be created **without any Flow**. `POST /submissions` works as a core capability. Flows are optional process wrappers — they are not required for simple data capture.

### 7. Terminology

| Term | Meaning | Not called |
|---|---|---|
| Entity | Anything with stable identity | Resource, object, record |
| Submission | A filled form instance | Response, entry, data point |
| Flow | A process-centric operation | Workflow, pipeline, process |
| FlowTask | Atomic unit of work within a flow | Activity, assignment, job |
| State | Formal lifecycle stages with guarded transitions | Status |

---

## Things to Avoid

Do **not** reopen these unless a real-world scenario genuinely cannot be modeled:

1. **Do not add new first-class constructs** without evidence from a real implementation that the six are insufficient. Assignment, Schedule, Scope, and Approval are patterns — promoting them to constructs requires a concrete failure case, not theoretical preference.

2. **Do not introduce BPMN / workflow engine concepts.** The platform uses Flows with configurable state machines, not executable process definitions. If multi-step orchestration is needed, it's a new Flow, not a workflow engine.

3. **Do not couple Flows to Entity lifecycle.** Interactions go through Events. Direct coupling reintroduces the dependency problem we explicitly removed.

4. **Do not bake domain semantics into Core.** The Core does not know what a "campaign" or "inventory report" is. It knows Entities, Forms, Submissions, States, Events, and References. Domain meaning lives in overlays.

5. **Do not design the Reaction layer yet.** Use event listeners in domain code. Formalize Reactions only after 3+ validated patterns emerge.

6. **Do not reference the current system's structure.** This baseline is independent of the legacy codebase. Migration compatibility is a separate concern.

---

## Related Documents

| Document | Purpose |
|---|---|
| [Open Decisions Log](open-decisions-log.md) | What is intentionally still open |
| [Reference Scenario: Monthly Inventory](reference-scenario-monthly-inventory.md) | End-to-end model proving the baseline holds |
| [Platform Ambition Statement](../platform-primitives-exploration-and-evaluation-sheet.md) | Goals, fitness functions, and non-goals |
