# Architecture Stress-Test: Alpha vs. Beta vs. Gamma

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---

> **Purpose:** Run 15 real-world scenarios through three architecture candidates to reveal where each holds, strains, or breaks — without picking a winner prematurely.

**Models under test:**
- **Alpha:** Thin Core, Fat Patterns (independent modules composed in code)
- **Beta:** Core with Capabilities (entities declare behaviors, platform activates them)
- **Gamma:** Flow-Centric (the composition itself is a first-class construct)

---

## Scenario 0: Basic Structured Data Capture

*User fills a form → validated → stored → viewable.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Natural | ✅ Natural | ⚠️ Overhead |
| **How** | Core only. Form + Submission. No patterns needed. | Core only. No capabilities needed. | Must decide: is this a "flow" or a raw capture? If everything must be a flow, forced abstraction. |

**What this reveals:**
All three handle pure capture fine, but Gamma already has a question: **does a simple form submission need a flow?** If yes, you have overhead for trivial cases. If no, you have two paths (flow vs. raw capture), which is fine but must be an explicit design decision.

**Pressure:** Gamma needs a "flow-less" path for simple capture. Otherwise trivial use cases pay a tax.

---

## Scenario 1: Entity-Linked Data Capture

*Form filled for a specific entity → submission linked → viewable per entity over time.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Natural | ✅ Natural | ⚠️ Slight overhead |
| **How** | Core: Entity + Entity Reference field type + Submission. | Core: Entity exists with identity. Capture links via reference. | Same question: is "capture linked to an entity" a flow or just core? |

**What this reveals:**
Entity Reference as a core primitive works identically in Alpha and Beta. Gamma again must decide whether entity-linked capture is "a flow" or "just capture with context." The scenario suggests Entity Reference belongs in Core, not in Patterns or Flow.

**Pressure:** None on Alpha/Beta. Gamma must be comfortable with a core capture path that bypasses flows.

---

## Scenario 2: Periodic Reporting

*Entities must submit reports on a recurring basis → track completeness → flag late/missing.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Good | ✅ Good | ✅ Natural |
| **How** | Schedule pattern + Scope pattern + Submission tracking. Domain wires them. | Entity type declares `schedulable(monthly)` + `scopeable(facility)`. Platform generates expected submissions. | Define a "Periodic Report" flow with schedule, scope, and completeness tracking built in. |
| **Easy** | Each pattern is independent. Schedule is reusable for other scenarios. | Declaring periodicity on an entity type is clean. | Querying "completeness of monthly reporting" is trivial — it's a flow property. |
| **Awkward** | "What's the completeness rate?" requires joining Schedule + Scope + Submissions manually. No unified place to ask. | Capability engine must generate "expected submission" records. Who owns those? The schedulable capability? | Works perfectly. This IS what flows are for. |

**What this reveals:**
Gamma shines here. "Monthly inventory report" IS a flow — it has a schedule, scope, expected submissions, and completeness. Alpha handles it but fragments the status across modules. Beta handles it but must define who generates and tracks the "expected vs. actual" — does the schedulable capability own that, or is it a separate concern?

**Pressure:** Alpha lacks a natural home for "aggregate status." Beta's capabilities need to produce trackable artifacts (expected submissions). Gamma is the strongest model here.

---

## Scenario 3: User-Based Assignment

*Reporting responsibility assigned to a user → tracked per user → missing submissions traced to user.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Natural | ✅ Natural | ✅ Natural |
| **How** | Assignment pattern: `(actor=user, scope=entity, role=reporter)` | Entity/process declares `assignable(role=reporter, per=entity)` | Flow has an `actors` section: `reporter: user, per facility` |
| **Easy** | Assignment is a simple join table — lightweight. | Declaring assignment as a capability is clean. | "Who is assigned to this flow?" is a first-class query. |
| **Awkward** | "What are all assignments for user X across all contexts?" requires querying Assignment module directly — works but not unified with scope/schedule. | Same capability question: who resolves the assignment? Is it the platform or the domain? | Works well. Assignment is a natural flow property. |

**What this reveals:**
All three handle assignment well. The difference is where "assigned to" lives:
- Alpha: standalone module, queried independently
- Beta: capability on the entity/process, resolved by the platform
- Gamma: property of the flow

Assignment is one of the simplest patterns. It doesn't differentiate the models much.

---

## Scenario 4: Supervisor Review & Approval

*Submission → reviewer approves/rejects → status tracked → delays visible.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Good | ✅ Good | ✅ Good |
| **How** | State pattern (submitted→approved/rejected) + Assignment (reviewer role) | Entity declares `stateful` + `approvable`. Platform manages transitions. | Flow lifecycle includes approval stages. |
| **Easy** | State + Assignment compose cleanly. | Declaring approvability is expressive. | Approval is just part of the flow lifecycle. |
| **Awkward** | "Show me all pending approvals for supervisor X" requires joining State + Assignment. | The `approvable` capability — is it a distinct capability or just a state transition with a role guard? Feels like it should be state + assignment composed, not a separate thing. | Works well. |

**What this reveals:**
In Beta, "approvable" might not be a separate capability — it's really `stateful` + `assignable` with a specific configuration. This suggests **capabilities should be composable themselves**, not a flat list. Or: reduce the number of capabilities and let their configuration handle approval.

**Pressure:** Beta's capability list can grow unbounded if every pattern combination becomes its own capability. Better to have fewer, more configurable capabilities.

---

## Scenario 5: Supervision / Audit Visits

*Users visit entities periodically → complete checklists → findings trigger follow-ups → performance tracked.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Good | ✅ Good, slight fuzzy | ✅ Strong |
| **How** | Schedule + Scope + Assignment + Capture (checklist form). Events trigger follow-up. | Entity declares `schedulable`, `assignable`, `capturable`. But: where do follow-up triggers live? | "Supervision Visit" flow with schedule, scope, actors, capture, and event hooks for follow-ups. |
| **Easy** | Patterns compose. Event-driven follow-ups use the Event primitive. | Clean declaration. | Follow-up triggers are natural flow events. "Visit completed → check findings → if threshold exceeded → create follow-up task." |
| **Awkward** | Follow-up trigger: which pattern owns "if finding X → create task Y"? It's cross-pattern logic. Where does it live? | Same question, amplified: does the capability engine handle conditional logic? That's a significant expansion. | Flow must support event hooks — "on event X, do Y." This makes the Flow construct more complex but more useful. |

**What this reveals:**
**Follow-up triggers are the first construct that doesn't fit neatly into any model.** They require:
1. Evaluating a condition on submitted data
2. Creating a new task/action in response
3. Linking the follow-up to the original finding

In Alpha, this is domain glue code. In Beta, it's either a new capability (`triggerable`) or domain code. In Gamma, it's a flow event hook.

**Missing construct surfaced:** An **action/reaction** mechanism — "when X happens, evaluate Y, trigger Z." This is related to Scenario 12 (Event-Triggered Actions) but appears here first.

---

## Scenario 6: Entity Registry with Lifecycle

*Entities created/updated/deactivated → some updates need approval → changes tracked → periodic verification.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Strained | ✅ Strong | ⚠️ Forced |
| **How** | Entity (core) + State pattern (lifecycle) + Approval. But the entity IS the subject being managed, not a separate process operating on entities. | Entity type declares `stateful(active, inactive, pending)` + `approvable` + `versionable`. The entity manages itself. | A "Registry" flow? The lifecycle of an entity IS the "process" — but it doesn't have a schedule, scope, or assignments in the traditional sense. |
| **Easy** | State and Approval patterns work for the lifecycle. | Very natural. An entity with lifecycle capability just works. | — |
| **Awkward** | The entity itself has state transitions — but in Alpha, state is an external pattern applied TO something. Is the entity its own subject? Who owns the state — the entity or the State module? | Clean. This is exactly what capabilities are for. | The entity lifecycle doesn't naturally "look like a flow." A flow implies a process with a beginning/end. An entity registry is a standing, ongoing process. Forcing it into a flow is awkward. |

**What this reveals:**
This is the first scenario where **Beta clearly outperforms** the others. Entity lifecycle management is exactly "an entity that has capabilities" — it IS stateful, it IS versionable, it IS approvable. It doesn't need external patterns applied to it (Alpha) or a flow wrapping it (Gamma).

**Pressure on Alpha:** State as an external module that's applied to entities creates an awkward ownership question — does the Entity own its state, or does the State module own the entity's state?

**Pressure on Gamma:** Not everything is a flow. Entity lifecycle is a standing concern, not a process with phases. Gamma must support non-flow use cases or admit it doesn't cover everything.

---

## Scenario 7: Resource Distribution & Acknowledgment

*Items move across levels → transfers recorded → receipt confirmed → discrepancies reported → history traceable.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Awkward | ⚠️ Awkward | ⚠️ Possible but strained |
| **How** | What pattern handles a transfer? Assignment? No — it's not "someone is assigned." State? Each transfer has a state (sent, received, disputed), but there are multiple transfers in a chain. | Entity declares `transferable`? That's a new capability. And each transfer step is itself a capture event with a state. | A "Distribution" flow? But the flow spans multiple actors and multiple steps, with the same resources passing through different hands. |
| **Easy** | — | — | — |
| **Awkward** | A transfer is a multi-step capture chain: send(entity_A → entity_B, quantity) → confirm(entity_B received) → discrepancy_report(if mismatch). No existing pattern covers this naturally. | Transfer isn't like other capabilities. It involves TWO entities (sender + receiver), not one entity with a behavior. The capability model (one entity declares behaviors) doesn't naturally model interactions between entities. | A flow can model "distribution from A through B to C" — but each step might be a sub-flow, and tracking "where are the resources right now" requires aggregating across steps. |

**What this reveals:**
**This scenario exposes a gap in all three models.** The issue:

1. **Transfer is relational** — it involves two parties, not one entity. Alpha's patterns, Beta's capabilities, and Gamma's flows are all entity-centric or process-centric, not interaction-centric.
2. **The chain creates implicit state** — "where are resources now?" is not a property of any single entity or step. It's a derived aggregate across the chain.
3. **Discrepancy handling** is an exception flow — what happens when the receiver says "I got 80, not 100"?

**Missing construct surfaced:** A **transfer/handoff** pattern — something that models the interaction between two parties around a resource. This might be a core concern or a shared pattern, but it doesn't exist yet.

**Alternatively:** Model each transfer as a Submission (a "transfer form") with entity references to sender, receiver, and items. The transfer chain is just a series of linked submissions. This is simpler but makes "where are resources now?" a query over submissions, not a first-class status.

---

## Scenario 8: Case Management / Follow-Up Tracking

*Cases opened → move through states → multiple actions on same case → responsibility shifts → all actions traceable.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Strained | ✅ Good | ⚠️ Possible but heavy |
| **How** | State pattern + Assignment + Capture (for each action). But a case is a long-lived entity with evolving state, not a single-pass process. | Entity type `case` declares `stateful`, `assignable`, `capturable`. Multiple captures attach to the same entity. Assignment can change. | A "Case" flow? The case IS the flow. But cases are long-lived, open-ended, and don't follow a fixed schedule. |
| **Easy** | Individual patterns work. | Clean. The case entity manages its own lifecycle. | — |
| **Awkward** | Who owns the case? Is it an entity (core) or a process (patterns)? The case is both — it's a thing AND it has a process. Assignment changes over time — the Assignment pattern must support reassignment, not just initial assignment. | Works well. The case is an entity with capabilities. Multiple captures attach to it over time. | Cases don't have a schedule or a scope in the way flows expect. They're opened by events and closed by resolution, not by time windows. Gamma's flow construct must be very flexible to accommodate this shape. |

**What this reveals:**
Beta handles case management most naturally — a case is an entity with lifecycle capabilities. Alpha works but the entity/process duality is awkward (is the case a core Entity or a patterns-layer thing?). Gamma can model it as a flow, but the flow construct must support open-ended, event-driven processes with no fixed timeline — which stretches the "flow" metaphor.

**Pressure on Gamma:** Flows need to support open-ended lifecycle processes (no schedule, no scope, event-driven) without the flow becoming a catch-all container. If everything is a flow, the word loses meaning.

---

## Scenario 9: Time-Bound Coordinated Operation (Campaign-like)

*Coordinated operation → phases → teams assigned to scopes → time windows → progress tracked → delays flagged → supervisor intervention.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Good | ✅ Good | ✅ Strongest |
| **How** | Schedule + Scope + Assignment + State, all composed in domain code. Phases are sub-scopes with their own states. | Campaign entity with `schedulable`, `scopeable`, `assignable`, `stateful`. Phases may be sub-entities. | "Campaign" is THE example of a flow. Phases are sub-flows or flow stages. |
| **Easy** | Patterns compose well — campaigns use all of them. | Clean declaration. | This is Gamma's home turf. Flow with phases, scoped assignments, time windows, progress tracking. |
| **Awkward** | "Campaign progress" = aggregate across scope × assignment × state × submissions. Alpha must build this aggregation as domain code. | Phases as sub-entities with their own capabilities — does the capability engine support nesting? | Works perfectly — but this is the easy case for Gamma. The question is whether the flow construct, designed to handle this case well, also handles the simpler and weirder cases. |

**What this reveals:**
This is Gamma's strongest scenario. Campaigns are exactly what flows model. But we already knew that — the question is not whether Gamma handles campaigns, but whether the flow abstraction generalizes without becoming too heavy.

**No new pressure.** All models handle this, with varying amounts of domain glue code.

---

## Scenario 10: Dynamic Targeting / Eligibility-Based Tasks

*Tasks generated for entities meeting conditions → conditions change → tasks appear/disappear → results feed back into targeting.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Needs rule engine | ⚠️ Needs rule evaluation | ⚠️ Needs dynamic scope |
| **How** | A new pattern: conditional scoping. Scope evaluates a rule engine at runtime instead of static entity list. | `Scopeable` capability could support dynamic filters. But who evaluates the filter? When? | Flow scope can be dynamic — evaluated at runtime. But feedback loops (results affect future targeting) require event processing. |
| **Awkward** | Where does the rule engine live? Is it a core primitive (used by Scope) or a separate pattern? | Filter evaluation is a runtime concern — it needs access to current entity state. This is heavier than "declare a capability." | Dynamic scope is fine. Feedback loops (result → re-evaluate scope → new tasks) require event-driven reaction — Flows must support this or delegate to events. |

**What this reveals:**
**All three models need something they don't currently have: a rule/condition evaluation mechanism.** Dynamic targeting isn't just scope — it's "scope that re-evaluates based on changing conditions." This is closer to a reactive system.

**Missing construct surfaced:** A **rule/condition evaluator** — something that can express "entities where attribute X > threshold Y and last_submission is older than 30 days." Not a full rule engine, but a structured condition evaluator.

**This is important because it determines how scope works.** If scope is always a static list, dynamic targeting requires external orchestration. If scope can be a live query, it's much more powerful but also more complex.

---

## Scenario 11: Multi-Step Approval Workflow

*Submission → multiple approval levels → different roles per stage → auditable → delays visible.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Good (State handles it) | ✅ Good | ✅ Good |
| **How** | State pattern with chained states: submitted → level_1_approved → level_2_approved → final. Each transition has a role guard. | `Stateful` with multi-step config. | Flow lifecycle with approval stages. |
| **Easy** | State machine handles arbitrary chains. Role guards enforce who can transition. | Clean declaration if the stateful capability supports multi-stage. | Flow stages naturally model approval levels. |
| **Awkward** | Tracking "where is this submission in the approval chain, and who's blocking it?" requires combining State current-position with Assignment at-each-level. | Does the `stateful` capability support role-per-transition natively? Or is it "stateful + assignable per state"? | Works cleanly. Each approval level is a flow stage with an assigned reviewer. |

**What this reveals:**
All three handle this well if the state/lifecycle mechanism supports role-guarded transitions. The differentiation is minor. This scenario confirms that **state machine + role-based transition guards** is a critical capability in whatever model we choose.

---

## Scenario 12: Event-Triggered Actions

*Data/events trigger follow-ups → rule evaluation → actions across users → escalation.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Needs reaction layer | ⚠️ Needs reaction mechanism | ✅ Good if flows support hooks |
| **How** | Events (core) + a new "Reaction" pattern: (on event X, if condition Y, do action Z). | Entities could declare `triggerable` — but triggers cross entity boundaries. A threshold on submission entity creates a task for a different user/entity. | Flow event hooks: "on submission_complete, evaluate X, trigger Y." |
| **Easy** | Events exist. Reactions are a natural pattern extension. | — | Flow hooks are a natural extension. |
| **Awkward** | The Reaction pattern becomes a mini rule engine. Complexity lives here. | Triggers cross entity boundaries — not a single-entity capability. This is a system-level concern, not an entity behavior. | Flow hooks work for within-flow events. Cross-flow triggers (event in flow A triggers action in flow B) need something else. |

**What this reveals:**
**Event-triggered actions are a cross-cutting concern, not an entity behavior or a flow feature.** They involve:
1. Listening to events (from any source)
2. Evaluating conditions
3. Producing actions (which may target different entities/flows)

This is closer to a platform-level **reaction/automation layer** than a pattern or capability.\

**Missing construct surfaced:** A **reaction layer** — lightweight event-condition-action triads that operate at the platform level, not inside any single entity or flow.

---

## Scenario 13: Cross-Flow / Cross-Domain Linking

*Data from one flow referenced in another → linked submissions/tasks/entities → changes propagate without tight coupling.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ✅ Possible via Entity References | ✅ Possible via shared identity | ⚠️ Awkward if flows are isolated |
| **How** | Core Entity References link arbitrary entities/submissions. Events notify interested parties. | Entities have stable identity. Any flow/domain can reference any entity. Changes propagate via events. | Flows are self-contained. Cross-flow linking requires flows to reference external entities — which they can do via entity references. But: does a flow know about another flow? |
| **Easy** | Entity references are core. No special mechanism needed. | Same — shared identity enables loose linking. | Entity-level linking works. |
| **Awkward** | "Show me all flows/submissions that reference entity X" is a query — enabled by entity references, but someone must build the aggregation. | Same as Alpha. | If flows are isolated (each flow manages its own scope, state, assignments), cross-flow relationships are external. This is fine architecturally (loose coupling), but makes "holistic view of entity X across all flows" a reporting/aggregation concern, not a flow feature. |

**What this reveals:**
Cross-domain linking works in all models **as long as Entity + Entity Reference is a core primitive.** The key insight: **identity and referenceability are the glue.** No model needs special cross-linking machinery if the entity model is solid.

**Pressure:** This validates Entity + Entity Reference as core primitives. Without them being in core, cross-flow linking becomes hard in every model.

---

## Scenario 14: Resource Distribution Across Multiple Levels

*Central → regional → local → entity. Transfers at each step. Confirmation, discrepancies, partial deliveries. Full traceability.*

| | Alpha | Beta | Gamma |
|---|---|---|---|
| **Fit** | ⚠️ Strained | ⚠️ Strained | ⚠️ Possible with sub-flows |
| **How** | Each transfer = Submission (transfer form) + Entity References (sender, receiver, items) + State (sent, received, disputed). Chain = series of linked submissions. | Entities involved declare capabilities — but the transfer itself isn't a single entity. It's an interaction. | A "Distribution" flow with stages per level. Each stage is a sub-flow: prepare → send → confirm → resolve_discrepancies. |
| **Easy** | Modeling each transfer step as a form submission is simple. | — | Flows with stages map to distribution levels. |
| **Awkward** | "Where are resources right now?" requires walking the submission chain and computing current location. Not a first-class answer. | Same problem as Scenario 7: transfer is **between** entities, not a property of one entity. Capabilities are entity-centric. | "Where are resources now?" could be a flow status query — IF the flow tracks aggregate state across its stages. This requires the flow to maintain derived state, not just lifecycle state. |

**What this reveals:**
This re-confirms the gap found in Scenario 7. **Multi-party, multi-step interactions are the hardest construct for all three models.** The transfer chain pattern has specific needs:

1. **Relational:** Involves two parties per step
2. **Sequential:** Each step depends on the previous
3. **Stateful at the chain level:** "Where is it now?" is an aggregate across all steps
4. **Exception-aware:** Partial deliveries, discrepancies, follow-ups

**Possible resolution:** Model transfers as linked submissions (each transfer is a form submission that references sender, receiver, previous transfer, and items). The "current location" query is a traversal over the submission chain. This avoids a new first-class construct but makes the query more complex.

**Alternative:** Introduce a **Transfer** pattern — a specialized capture type that explicitly models "from → to" with confirmation semantics. This is more structured but adds a new construct.

---

## Synthesis: What Each Model Handles Well and Where It Struggles

### Alpha: Thin Core, Fat Patterns

| Strength zone | Strain zone |
|---|---|
| Scenarios 0-4: simple capture through assignment/approval | Scenario 6: entity lifecycle — who owns state? |
| Independent, testable patterns | Scenario 7/14: multi-party transfers |
| Maximum transparency — everything is code | Any "aggregate status" question (completeness, progress) |
| | "Show me everything about X" requires cross-module joins |

**Alpha's fundamental tension:** Patterns are independent, which is great for modularity — but real operations need **aggregated views** across patterns. Alpha doesn't have a natural home for "the combined status of this operational process."

### Beta: Core with Capabilities

| Strength zone | Strain zone |
|---|---|
| Scenario 6: entity lifecycle — its best case | Scenario 7/14: interactions between entities |
| Any single-entity behavior composition | Capability creep — tendency to add a new capability for each scenario |
| Clean declaration, less wiring code | Capability engine becomes complex runtime |
| | "Approvable" is really "stateful + assignable" — needs composable capabilities, not flat list |

**Beta's fundamental tension:** Capabilities work beautifully for single-entity behaviors but struggle with **inter-entity interactions** (transfers, handoffs) and **process-level concerns** (aggregate progress, campaign completeness). The entity is not always the right unit of behavior.

### Gamma: Flow-Centric

| Strength zone | Strain zone |
|---|---|
| Scenarios 2,3,5,9: periodic, assigned, tracked processes | Scenario 0,1: basic capture — overhead |
| "What's the status of X?" is always answerable | Scenario 6: entity lifecycle — not a flow |
| Mobile sync: "my flows and my tasks" | Scenario 8: open-ended cases stretch the metaphor |
| Campaign-like operations | Flow risks becoming a God object if it absorbs too many concerns |

**Gamma's fundamental tension:** Flows are the most user-friendly abstraction for operational processes, but **not everything is a flow.** Entity lifecycle, ad-hoc capture, and standing registries don't naturally fit. If Gamma adapts to handle them, the Flow construct becomes too broad. If it doesn't, you need a second organizing principle alongside flows.

---

## Constructs That Emerged as Missing

All three models lack these. They would be needed regardless of which model is chosen:

| Construct | Why needed | First surfaced in |
|---|---|---|
| **Rule/Condition Evaluator** | Dynamic scoping, trigger conditions, eligibility checks | Scenario 10 (Dynamic Targeting) |
| **Reaction/Automation Layer** | Event → condition → action triads, cross-entity and cross-flow | Scenario 5, 12 (Follow-ups, Event-Triggered) |
| **Transfer / Handoff** | Multi-party interactions with confirmation and exception handling | Scenario 7, 14 (Resource Distribution) |
| **Aggregate Status** | "What's the overall completeness/progress?" across a set of entities/submissions | Scenario 2, 9 (Periodic Reporting, Campaigns) |

---

## Where This Leaves the Architecture Choice

No single model covers every scenario cleanly. The question is: **which model is the best organizing principle** — the one that handles the most cases naturally and can be extended for the rest?

**The emerging shape is a hybrid, but not an unprincipled one:**

```
Core:       Entity, Capture, Events, Identity, Entity Reference
            (from Alpha — thin, stable, domain-agnostic)

Flows:      First-class construct for operational processes
            (from Gamma — schedule, scope, assignment, State, capture)
            Used for: periodic reporting, campaigns, supervision, distribution

Capabilities: On entities, for entity behaviors
            (from Beta — lifecycle, versioning, grouping, hierarchy)
            Used for: registries, entity management, standing concerns

Reaction:   Event → condition → action (cross-cutting)
            Used for: follow-ups, escalations, dynamic triggers
```

This is **not** three layers bolted together. It's a recognition that:
- **Flows** are the right shape for **process-centric** operations (things with schedules, scopes, actors, and lifecycle)
- **Capabilities** are the right shape for **entity-centric** concerns (things that entities inherently ARE)
- **Core** provides the primitives both use (identity, capture, events)
- **Reactions** handle the cross-cutting glue (triggers, automations, escalations)

> **This is still an exploration. The question is: does this hybrid shape feel right, or does it introduce too many organizing principles?**