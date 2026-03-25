# Open Decisions Log

> **Status:** Living document — tracks intentionally deferred decisions.
> **Related:** [Architecture Baseline v1](baseline-v1.md)

---

## Purpose

These items are **not problems**. They are decisions deliberately left open because:
- insufficient evidence exists to decide now, or
- the decision has no impact on the frozen baseline, or
- deciding now would over-commit without real usage feedback.

Each item has a **trigger** — the condition under which it should be decided.

---

## Open Items

### OD-001: Configuration mechanism

**What:** How FlowDefinitions, EntityTypes, and StateDefinitions are created and managed — Java code, JSON/YAML config files, admin UI, API-driven, or a hybrid.

**Why open:** No impact on the construct shapes. The interfaces work regardless of whether the definitions are built in code or loaded from config.

**Trigger:** Decide when the first FlowDefinition is implemented end-to-end and you see what the natural configuration surface is.

---

### OD-002: Reaction layer formalization

**What:** Whether event-condition-action triads become a first-class platform concern or remain domain code.

**Why open:** The need is clear (follow-ups, escalations, late notifications), but the shape is not. Formalizing too early risks building a hidden workflow engine.

**Boundary (already decided):** Reactions must be stateless, single-step, deterministic. Multi-step orchestration is a Flow, not a Reaction.

**Trigger:** Decide after 3+ domains implement event-triggered actions and common patterns emerge.

---

### OD-003: Event infrastructure

**What:** How events are propagated — in-process synchronous (Spring ApplicationEventPublisher), async, outbox pattern, message broker.

**Why open:** In-process synchronous events are sufficient for a modular monolith. The infrastructure decision becomes relevant only when distribution, replay, or external consumers are needed.

**Trigger:** Decide when either (a) event volume requires async processing, or (b) an external system needs to consume platform events.

---

### OD-004: Dynamic scope / filter expressions

**What:** Whether scope can be a live query (dynamic filter evaluated at runtime) or only explicit entity lists.

**Why open:** Explicit lists cover the inventory scenario. Dynamic filters are needed for Scenario 10 (dynamic targeting) which is not in the first implementation slice.

**Workaround:** Domain code can update entity attributes based on past submissions, and scope reads those attributes. No architecture change needed.

**Trigger:** Decide when a domain requires scope that cannot be expressed as entity attributes.

---

### OD-005: FlowTask multi-capture

**What:** Whether a FlowTask can bind to multiple capture templates (e.g., pre-visit form AND visit report) or only one primary.

**Why open:** One primary submission per FlowTask works for inventory reporting. Multi-capture may be needed for supervision visits.

**Workaround:** Supplementary captures are standalone submissions linked via EntityReference to the same target entity. The FlowTask tracks only the primary.

**Trigger:** Decide when a domain requires supplementary captures that must be tracked as part of the FlowTask lifecycle (not just linked submissions).

---

### OD-006: Flow nesting / phases

**What:** Whether a FlowDefinition can have phases (sub-flows) or stages, or whether phases are separate FlowDefinitions that share a scope.

**Why open:** Monthly reporting has one phase. Campaigns have multiple (preparation, execution, follow-up). The right model depends on how phases interact.

**Trigger:** Decide when the first campaign-like scenario is implemented.

---

### OD-007: Module and package structure

**What:** Whether constructs map to Java packages, Maven modules, or another boundary mechanism. How `legacy-core` and new modules coexist.

**Why open:** No impact on construct design. Important for build and deployment, but a separate concern.

**Trigger:** Decide at the start of implementation, when the project structure is set up.

---

### OD-008: Mobile sync contract

**What:** How the mobile client builds its execution context — what to sync, when, in what format.

**Why open:** The API surface for flows (`GET /flows/my-tasks`) suggests a natural sync boundary, but the offline/sync strategy is a separate design concern.

**Trigger:** Decide when mobile implementation begins.

---

### OD-009: Entity capability declarations

**What:** Whether EntityTypes should formally declare capabilities (stateful, versionable, etc.) or whether capabilities are implicit (an entity has a StateInstance attached → it's stateful).

**Why open:** Implicit capabilities work for now. Explicit declarations add documentation value but also ceremony.

**Trigger:** Decide if entity type management becomes complex enough that an admin needs to understand "what can this entity type do?"
