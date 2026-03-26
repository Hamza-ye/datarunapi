# Architecture Modeling - Architectural Design Thinking

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---

Good. Let me walk through the constructs and layers by putting them under pressure with real scenarios. The goal is to find where the current model holds, where it breaks, and what alternatives exist.

I'll present **three genuinely different architectural models**, run the **same scenario through each**, and surface the trade-offs. Then you react.

---

## The scenario we'll use to test each model

**Monthly inventory reporting at health facilities:**
- 200 health facilities in a district
- Each facility has a pharmacist responsible for stock reporting
- On the 5th of every month, a stock report form must be submitted
- Covers ~50 item types (medicines, supplies)
- A district supervisor reviews and approves each submission
- Late submissions are flagged
- Results feed into LMIS/supply chain decisions

This is a good test because it touches every construct: entities, forms, assignment, scheduling, state, scope, approval, and hierarchy.

---

## Architecture Alpha: Thin Core, Fat Patterns

Core owns almost nothing — just identity, raw capture, and events. All operational intelligence lives in the patterns layer.

```
┌─────────────────────────────────────┐
│              Domain                  │
│  (Inventory overlay — thin composer) │
│  - wires patterns together in code   │
│  - adds inventory-specific rules     │
└──────────┬───────────┬──────────────┘
           │           │
    ┌──────▼───┐  ┌────▼─────┐  ┌──────────┐  ┌────────┐
    │Assignment│  │ Schedule │  │  State   │  │ Scope  │  ...
    │ (module) │  │ (module) │  │ (module) │  │(module)│
    └──────┬───┘  └────┬─────┘  └────┬─────┘  └───┬────┘
           │           │             │             │
┌──────────▼───────────▼─────────────▼─────────────▼──┐
│                     Core                             │
│  Entity (uid, code, name, type — identity only)      │
│  Capture (form + submission — raw ingestion)         │
│  Events (something happened — audit backbone)        │
│  Identity (users, auth)                              │
└──────────────────────────────────────────────────────┘
```

**How inventory works in Alpha:**

```java
// Domain layer: InventoryFlowSetup.java
// This is code composition — domain wires patterns together

scope = Scope.define(entityType("facility"), filter("has_warehouse"));
schedule = Schedule.recurring(MONTHLY, dayOfMonth(5));
assignment = Assignment.forEachInScope(scope, role("facility_pharmacist"));
lifecycle = State.define("draft", "submitted", "approved")
    .transition("draft→submitted", requires(submission))
    .transition("submitted→approved", requires(role("district_supervisor")));
```

**Strengths:**
- Patterns are independent modules — you can use Schedule without Assignment, State without Scope
- Adding a new pattern doesn't touch core or existing patterns
- Domain logic is explicit code — easy to trace and debug

**Weaknesses:**
- The domain layer has to know how to wire everything together — every domain reimplements the wiring
- No "flow" concept — the composition exists only in code, not as a queryable/trackable thing
- "What is the status of inventory reporting for district X?" requires querying across 4 pattern modules

**Control level:** High. Every composition is visible code.
**Flexibility level:** High for patterns, medium for domains (they have to wire things manually).

---

## Architecture Beta: Core with Capabilities

Instead of independent pattern modules, entities and processes **declare capabilities**. "This entity is schedulable." "This process is assignable." The platform activates the right behavior.

```
┌─────────────────────────────────────────┐
│              Domain                      │
│  (Inventory overlay)                     │
│  - defines entity types + capabilities   │
│  - adds domain-specific rules            │
└──────────┬──────────────────────────────┘
           │
┌──────────▼──────────────────────────────┐
│          Capability Contracts            │
│                                          │
│  Schedulable  │  Assignable  │ Stateful  │
│  Scopeable    │  Approvable  │ Groupable │
│                                          │
│  (attached to entities/processes)        │
└──────────┬──────────────────────────────┘
           │
┌──────────▼──────────────────────────────┐
│               Core                       │
│  Entity (with type + declared caps)      │
│  Capture (form + submission)             │
│  Events + Identity                       │
└──────────────────────────────────────────┘
```

**How inventory works in Beta:**

```yaml
# Entity type definition — config, not code
entity_type: "inventory_reporting_flow"
structure: scoped_process
capabilities:
  schedulable:
    period: monthly
    day: 5
  scopeable:
    target_entity_type: facility
    filter: has_warehouse
  assignable:
    role: facility_pharmacist
    per: scope_entity     # one assignment per facility
  stateful:
    states: [draft, submitted, approved]
    transitions:
      - from: draft, to: submitted, requires: submission_complete
      - from: submitted, to: approved, requires: role(district_supervisor)
  approvable:
    reviewer_role: district_supervisor
```

**Strengths:**
- Declarative — you describe what something IS, not how to wire it
- Capabilities are standardized contracts — the platform knows how to query "all schedulable things" or "all stateful things"
- "What is the status of inventory reporting?" is answerable — the platform knows the entity has state capability and can query it
- Adding a new capability doesn't break existing entity types

**Weaknesses:**
- Requires a capability resolution engine — something that reads type definitions and activates behaviors
- The line between "capability" and "domain logic" is fuzzy — is "target coverage calculation" a capability or domain code?
- Risk of growing the capability vocabulary until it becomes a DSL
- Harder to trace behavior — what's actually running comes from config interpretation, not explicit code

**Control level:** Medium. Behavior is declared, not coded. Traceability depends on how well the capability engine logs.
**Flexibility level:** Very high. New capabilities can be added independently. New entity types compose existing capabilities declaratively.

---

## Architecture Gamma: Flow-Centric

The key insight here: the **composition of patterns into a working operational process is itself the main construct.** Instead of composing Assignment + Schedule + State independently, you define a **Flow** — a first-class thing with identity, lifecycle, and configuration.

```
┌──────────────────────────────────────────┐
│              Domain                       │
│  (Inventory overlay)                      │
│  - configures flow instances              │
│  - extends with domain-specific hooks     │
└──────────┬───────────────────────────────┘
           │
┌──────────▼───────────────────────────────┐
│               Flow                        │
│  (first-class construct)                  │
│                                           │
│  Has: schedule, scope, assignments,       │
│       lifecycle, capture config           │
│  IS:  queryable, trackable, configurable  │
│  Owns: its progress, its status, its data │
└──────────┬───────────────────────────────┘
           │
┌──────────▼───────────────────────────────┐
│               Core                        │
│  Entity + Capture + Events + Identity     │
└──────────────────────────────────────────┘
```

**How inventory works in Gamma:**

```
Flow: "Monthly Inventory Report"
  schedule:  monthly, day 5
  scope:     all facilities with warehouses in district X
  capture:   inventory_form_v3
  lifecycle: draft → submitted → approved
  actors:
    reporter:  facility_pharmacist (per facility)
    reviewer:  district_supervisor (per district)
  rules:
    late_after: 10 days past due date
    requires_approval: true
```

The Flow is a real thing in the system — it has a UID, it can be queried (`GET /flows/monthly-inventory/status`), it tracks progress (`180/200 facilities submitted`), and it owns the relationship between all its parts.

**Strengths:**
- Answers the hardest question naturally: "what is the status of X?" — because X is a thing, not a scattered composition
- Uniform experience — every domain creates flows, every flow is queryable the same way
- Mobile sync becomes simpler — sync "my flows" instead of "my assignments + my schedules + my state"
- Reporting is natural — flows aggregate their own data

**Weaknesses:**
- Less composable than Alpha — if a use case doesn't fit the "flow" shape, it's awkward
- What IS a flow? Is a one-off ad-hoc submission a "flow"? Is a standing facility registry a "flow"?
- The Flow construct must be flexible enough to absorb very different shapes without becoming a God object
- Risk of the Flow becoming a "do everything" abstraction

**Control level:** Medium-high. Flows are explicit and queryable, but their internal wiring is the platform's responsibility.
**Flexibility level:** Medium. Very flexible within the flow paradigm. Less flexible for things that don't fit it.

---

## The critical comparison

| Dimension                         | Alpha (Thin Core)                         | Beta (Capabilities)                      | Gamma (Flow-Centric)                 |
| --------------------------------- | ----------------------------------------- | ---------------------------------------- | ------------------------------------ |
| "What's the status of inventory?" | Hard — query across 4 modules             | Medium — query stateful capability       | Easy — `flow.status()`               |
| Ad-hoc one-off submission         | Easy — just use Capture directly          | Easy — entity without capabilities       | Awkward — is a one-off a "flow"?     |
| Adding a new domain               | Wire patterns in code (work per domain)   | Declare capabilities (config per domain) | Configure a flow (config per domain) |
| Mobile sync contract              | Complex — aggregate from multiple sources | Medium — sync by capability              | Simple — sync "my flows"             |
| Debugging "why did X happen?"     | Easy — trace code                         | Medium — trace config + engine           | Medium — trace flow engine           |
| Avoiding God objects              | Strong — everything is small              | Medium — capability engine grows         | Weak — Flow can become a God object  |
| Uniformity across domains         | Weak — each domain wires differently      | Strong — capabilities are standard       | Strong — all flows look the same     |

---

## Or — a hybrid?

These aren't mutually exclusive. A real architecture might be:

- **Core:** Entity + Capture + Events + Identity (from Alpha)
- **Flow** as a first-class construct (from Gamma) — but only for operational processes that have schedule/scope/assignment/state
- **Capabilities** on entities (from Beta) — entities can be "hierarchical," "referenceable," "groupable" independent of flows
- **Ad-hoc capture** remains possible without a flow — a simple form submission to the core, no flow overhead

This hybrid avoids the worst weakness of each model while keeping their best features.

What resonates? Where do you disagree or want to push deeper?