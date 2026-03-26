# final pass before freezing

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---

### 1. Is the hybrid now stable enough to finalize?

**Yes, with 2 adjustments** (both decisions, not redesigns).

---

### 2. What still needs a decision now?

#### Decision 1: Scope timing → **Fixed at creation, with explicit amendments**

Scope should be **snapshotted when a FlowInstance is created**. The 200 facilities in scope at March 1 are the 200 facilities for the March cycle. Period.

Why fixed:
- Completeness tracking requires a stable denominator. If scope shifts mid-cycle, "completion rate" becomes meaningless.
- Mobile sync is clean — sync once at period start, no surprises mid-cycle.
- Auditability — the scope is a traceable snapshot, not a moving target.

But allow **explicit scope amendments**: an admin can manually add or remove an entity from a running FlowInstance. This is an auditable action (produces an Event), not an automatic recalculation. New entity activated on March 10? Admin adds it to the March scope. Facility suspended on March 8? Admin removes its FlowTask or flags it.

This keeps scope predictable while allowing real-world exceptions.

#### Decision 2: FlowTask granularity → **One task = one target entity × one occurrence**

The atomic unit of trackable work is:

```
FlowTask = target entity + assigned actor(s) + time window + one primary submission
```

This holds across scenarios:

| Scenario              | FlowTask =                                           |
| --------------------- | ---------------------------------------------------- |
| Inventory reporting   | facility × pharmacist × month                        |
| Campaign              | village × team × phase                               |
| Supervision visit     | facility × supervisor × quarter                      |
| Resource distribution | destination_entity × logistics_team × transfer_batch |

**One FlowTask binds to one primary submission** (the capture template from FlowDefinition). If a domain needs supplementary captures (e.g., a pre-visit form AND a visit report), the supplementary ones are standalone submissions linked to the same target entity via EntityReference. The FlowTask tracks only the primary.

This keeps FlowTask simple: one target, one assignment, one submission to track, one state to query.

#### Decision 3 (deferrable): Reaction boundary → **Decide after first flow is implemented**

The reaction layer is NOT needed for the first implementation slice. Simple event listeners in domain code handle the immediate cases (late task notification, completeness recalculation).

When formalized later, the boundary should be: **reactions are stateless, single-step, deterministic.**

| IN | OUT |
|---|---|
| One event → evaluate condition → one action | Multi-step orchestration ("do A, wait for B, decide C") |
| `on flow_task.late → notify_reviewer` | Complex cross-entity joins |
| `on submission.value('stock_out') > 14 → create_follow_up` | Maintaining state between reactions |
| Deterministic, can be tested as pure functions | Anything that is itself a process |

If something needs multi-step orchestration, it's a Flow, not a Reaction.

**This decision can be deferred** because event listeners work fine until you have 3+ reactions that share a pattern.

---

### 3. What should be frozen now?

**First-class constructs (6):**
- Entity (with EntityType carrying structural constraints as config)
- Capture (FormTemplate → FormVersion → Submission)
- Entity Reference (typed, resolvable links)
- State (universal state machine: definition + instance + transition guards)
- Event (immutable audit record with correlation)
- Flow (FlowDefinition → FlowInstance → FlowTask → FlowProgress)

**Terminology:**
- "Entity" — anything with stable identity (not "resource," "object," or "record")
- "Submission" — a filled form (not "response," "entry," or "data point")
- "Flow" — a process-centric operation (not "workflow," "pipeline," or "process") — "workflow" implies BPMN-level complexity we don't want
- "FlowTask" — the atomic unit of work within a flow (not "activity," "assignment," or "job")
- "State" — lifecycle stages with guarded transitions (not "status" — status is informal, state is formal)

**What is definitively NOT a separate construct:**
- Assignment → pattern inside Flow (ActorRole + FlowTask.assignedActors)
- Schedule → pattern inside Flow (FlowDefinition.schedule)
- Scope → pattern inside Flow (FlowDefinition.scope), snapshotted at FlowInstance creation
- Approval → composition of State transitions with role guards
- Transfer → Flow where capture template includes sender/receiver/items
- Hierarchy → EntityType.structure config (HIERARCHICAL, FLAT, GROUPED)
- Reaction → deferred; use event listeners for now

---

### 4. What should remain flexible for later?

- **Reaction layer formalization** — use event listeners, extract pattern after 3+ cases
- **Scope rules vs. static lists** — start with explicit entity lists; filter expressions come when you need dynamic scoping
- **Multi-capture FlowTasks** — start with one primary submission per task; add supplementary capture linking if domains demand it
- **FlowTask nesting** — campaigns may want phases as sub-flows. Defer until the first campaign implementation reveals whether FlowDefinition stages or nested FlowInstances are the right model
- **Entity capability declarations** — currently, entity lifecycle is just State attached to an entity. If entity types need richer declarative behaviors, formalize Capabilities then
- **Configuration mechanism** — how FlowDefinitions are created (Java code, JSON config, admin UI) stays open

---

### 5. What scenario would still break this architecture?

**Scenario that strains but holds:** Resource distribution (14). A distribution flow works as: FlowDefinition scoped to destination entities, each FlowTask is a transfer step, the submission captures receipt confirmation with sender reference and item quantities. "Where are resources now?" is derived from FlowTask states (shipping → confirmed). This is queryable but not a first-class aggregate. It works, but if distribution tracking becomes critical at scale, **transfer/chain status** may need to become a first-class concern. This is a known future pressure point, not a blocker.

**Scenario that would actually require a design extension:** A feedback loop scenario (10 — dynamic targeting) where **task results change the scope of the SAME flow's next cycle.** Example: "facilities that reported stock-outs last month should submit weekly instead of monthly." This requires reading past submissions to compute a new scope for the next FlowInstance, with different schedule per entity. The current model evaluates scope from entity attributes — not from submission history. 

To handle it, you'd either:
- (A) Write domain code that updates entity attributes based on past submissions, and scope reads those attributes (works, no architecture change)
- (B) Allow scope filters to query submission history (heavier, crosses the capture ↔ flow boundary)

**Option A works within the current architecture.** It means "entity attributes are the single source of truth for scope decisions" — which is a good principle. The domain code that updates attributes from submissions is domain logic, not a platform concern.

---

### Summary

| Question                   | Answer                                                                                                              |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Stable enough to finalize? | **Yes, with scope timing and task granularity decided**                                                             |
| What must be decided now?  | Fixed scope with amendments, one task = one entity × one occurrence                                                 |
| What should be frozen?     | 6 constructs, terminology, what's NOT a construct                                                                   |
| What stays flexible?       | Reaction layer, config mechanism, multi-capture, nesting                                                            |
| What would break it?       | Dynamic targeting feedback loops — but solvable through entity attribute updates (Option A), no architecture change |

**This is ready to freeze into architecture documents and move toward implementation design.**