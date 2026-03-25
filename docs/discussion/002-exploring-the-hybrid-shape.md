# A. Exploring the hybrid Shape

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---

### A. Verdict

**Finalize with adjustments.**

The hybrid shape is real and coherent. It's not "we'll use everything" — it's a recognition that the platform has two fundamentally different concerns (entity-centric and process-centric) and one organizing principle cannot cover both without becoming a God object. But three specific things need tightening before this becomes a committable architecture direction.

---

### B. What works

- **Core as identity + capture + events + references.** This survived every scenario. No scenario needed less, no scenario needed the core to know domain semantics. This is solid.
- **The separation between Flows (process-centric) and Capabilities (entity-centric).** These answer genuinely different questions. An entity CAN exist without being in any flow. A flow ALWAYS operates on entities. This is a clean, non-overlapping boundary.
- **Entity Reference as a core primitive.** Scenario 13 (cross-flow linking) confirmed: if entity identity and referenceability are in core, everything else can be loosely coupled through references. This is the glue that makes the whole platform cohere.
- **The three-layer extensibility model.** Core → Patterns/Flows/Capabilities → Domain overlays. The scenarios confirmed that ~70% of each scenario's needs are met by core + patterns, with domain-specific 30% on top.

---

### C. What still doesn't fit

**1. State/Lifecycle appears in too many places.**
State is used by flows (flow lifecycle: planned → active → completed) AND by entities (entity lifecycle: active → deactivated). Is this one construct used in two contexts, or two separate things? If one: who owns it — Core, Flows, or Capabilities? If two: how are they different?

**This matters because:** If a facility entity has a lifecycle state (active/inactive) AND participates in a flow that also has lifecycle state (reporting period open/closed), a mobile client or API consumer needs to know which "state" they're asking about. The answer must be unambiguous.

**My position:** State is a **core primitive** — a generic state machine that can be attached to anything (entity, flow, submission). Flows and Capabilities both USE it but don't OWN it. This prevents duplication and keeps state queryable uniformly.

**2. Case management sits exactly on the boundary.**
A case is a long-lived entity (entity-centric) with an evolving process (process-centric). Scenario 8 tested this and neither Flows nor Capabilities alone handled it cleanly. The hybrid helps — a case is an entity with lifecycle capabilities, and specific case workflows are flows that operate on case entities — but we need to be explicit: **can a flow be triggered by entity state changes?** If a case entity transitions to "escalated," does that trigger a "supervisor review" flow?

If yes, Flows and Capabilities interact through events, and the answer is clean. If no, case management requires a lot of domain glue code.

**3. The Reaction layer is important but its boundaries are undefined.**
Scenarios 5, 10, and 12 all need "when X happens, evaluate Y, do Z." But how complex is this?
- Simple: "on submission_complete, if value > threshold, create_task" — this is a structured triplet, lightweight
- Complex: "evaluate eligibility across 5 entity attributes, dynamically generate tasks, re-evaluate weekly" — this is a rule engine

The boundary between "structured reaction" and "rule engine" must be drawn. If we don't draw it, the reaction layer will creep into becoming a workflow engine.

---

### D. What must be first-class

| Construct | Why first-class | What it owns |
|---|---|---|
| **Entity** | Foundation of everything. Every scenario references entities. | Identity (uid, code, name), type, attributes, references |
| **Capture** (Form + Submission) | The platform's primary function. Core guarantee. | Template definition, versioning, submission storage, validation |
| **Event** | Traceability guarantee. Integration backbone. Ties Flows and Capabilities together. | "Something happened" records. Correlation. Audit trail. |
| **Entity Reference** | The glue between capture and entities, between flows and entities, between flows and flows. | Field type + resolution + stable linking |
| **State** | Used by flows, entities, submissions, and cases. Too fundamental to be owned by any one layer. | State definition, transitions, guards (role-based, condition-based) |
| **Flow** | The organizing construct for process-centric operations. Without it, "what's the status of X?" has no home. | Schedule, scope, assignment, lifecycle (uses State), progress, capture binding |

Six first-class constructs. Not three, not twelve. Each one owns a clearly different concern.

---

### E. What can stay as a pattern (not first-class yet)

| Construct | Why not first-class yet | When to promote |
|---|---|---|
| **Assignment** | It's a property of flows ("who does what") and a simple entity relationship. It doesn't need its own independent identity. | If assignment logic becomes complex enough to need its own lifecycle (e.g., reassignment workflows, availability tracking) |
| **Schedule** | It's a property of flows. A standalone schedule without a flow doesn't mean much. | If non-flow scheduled actions become common (e.g., scheduled data cleanup, automated report generation) |
| **Scope** | It's how flows define their target set. Not independently meaningful. | If scope rules become a shared, referenceable concern (e.g., "scope X is reused across 5 flows") |
| **Approval** | It's a composition of State (transition guard) + Assignment (reviewer). Not a separate construct. | If approval chains become complex enough to need their own audit trail distinct from state transitions |
| **Reaction/Automation** | The need is clear but the shape is not. Start with simple event listeners in domain code. Extract when you see the real patterns. | After 3+ domains implement event-triggered actions and the commonalities are visible |
| **Transfer/Handoff** | Can be modeled as linked submissions with entity references (sender, receiver, items). Works for basic cases. | If tracking "where are resources now?" as a first-class aggregate becomes critical and the submission-chain model is too slow or awkward |
| **Grouping/Hierarchy** | Entity structure config (hierarchical, flat, grouped). Part of entity type definition, not a separate construct. | If hierarchy traversal becomes a shared operational concern (not just for scoping, but for aggregation, delegation, etc.) |

---

### F. Final recommendation

**This direction is final enough to move forward — with one critical clarification before implementation.**

The hybrid of Core + Flows + Entity Capabilities is not a compromise; it's a genuine recognition that platforms serve two kinds of concerns that cannot be unified into one abstraction without that abstraction becoming too broad. Entity identity and behavior (capabilities) answer "what IS this thing?" Flows answer "what PROCESS operates on these things?" Core provides the shared primitives both use. This is stable.

The one clarification you need before moving to implementation design: **how Flows and Entity Capabilities interact.** Specifically: can entity state changes trigger flow creation or flow transitions? If yes (and I believe the answer should be yes, via events), then the architecture is complete — Flows, Capabilities, and Core are connected through the Event primitive, and Reactions are just a future formalization of that connection. If no, case management and dynamic targeting will require awkward workarounds.

I would not explore more alternative architectures. The stress test covered 15 scenarios, three models, and a hybrid. Further exploration at this level of abstraction will produce diminishing returns. The next valuable step is **modeling one real scenario end-to-end through the hybrid architecture** — defining the actual constructs (Entity definition, Flow definition, State definition) concretely enough that you could write a Java interface for each one. That's where the remaining ambiguities will resolve themselves or reveal genuine problems.