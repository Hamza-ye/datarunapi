# End-to-End Scenario Modeling: Hybrid Architecture

> **Status:** Historical — this documents the exploration that led to [baseline-v1.md](baseline-v1.md).
> **Note:** All open questions listed here have been resolved. See the baseline for final decisions.

---
2
> **Purpose:** Validate the hybrid architecture by modeling two real scenarios through concrete constructs, defined at Java interface level. This is where ambiguities either resolve or reveal genuine problems.

---

## Scenarios Selected

| Scenario | Tests | Why selected |
|---|---|---|
| **A: Monthly Facility Inventory Reporting** | Flow, all core primitives, Assignment, Schedule, State, Scope, Approval | The most representative operational flow. Combines periodic scheduling, entity-linked capture, assignment, approval, and completeness tracking. |
| **B: Facility Registry with Lifecycle** | Entity + Capabilities (State), without any flow | Tests that entity-centric concerns work independently of flows. |
| **Interaction** | Flow references entities managed by capabilities | Facilities from the registry (Scenario B) participate in the reporting flow (Scenario A). Tests that the two sides of the architecture connect cleanly. |

---

## First-Class Constructs: Java Interface Definitions

### 1. Entity

The foundational identity primitive. Everything in the platform is an entity or references one.

```java
/**
 * Core identity primitive.
 * An Entity is anything the platform tracks — a facility, user, team, item, org unit.
 * Entities carry stable identity and typed attributes.
 */
public interface Entity {
    
    ULID getId();              // Internal, immutable
    String getUid();           // Business key, stable across environments
    String getCode();          // Human-readable short identifier
    String getName();          // Display name
    EntityType getType();      // What kind of entity (facility, item, team, etc.)
    
    Map<String, Object> getAttributes();   // Type-specific attributes (JSON-backed)
    
    Instant getCreatedAt();
    Instant getUpdatedAt();
}

/**
 * Defines a kind of entity and its structural constraints.
 * This is where "org units are hierarchical" or "options are flat lists" is expressed.
 */
public interface EntityType {
    
    String getCode();           // e.g., "facility", "item", "org_unit"
    String getName();
    StructureType getStructure();  // FLAT, HIERARCHICAL, GROUPED
    
    // Attribute schema — what attributes entities of this type carry
    List<AttributeDefinition> getAttributeSchema();
    
    // Constraints — expressed as config, not code
    // e.g., {"hierarchical": {"maxDepth": 5, "requireParent": true}}
    Map<String, Object> getConstraints();
}

enum StructureType {
    FLAT,           // Simple list (options, items)
    HIERARCHICAL,   // Tree structure (org units, locations)
    GROUPED         // Belongs to groups/sets (item categories)
}
```

### 2. Capture (Form + Submission)

The platform's primary operational function.

```java
/**
 * Defines what data to capture.
 * Versioned — a template evolves over time, submissions link to a specific version.
 */
public interface FormTemplate {
    
    ULID getId();
    String getUid();
    String getCode();
    String getName();
    
    int getCurrentVersion();
    FormVersion getVersion(int version);
}

public interface FormVersion {
    
    int getVersion();
    List<FormField> getFields();
    List<FormSection> getSections();
    List<ValidationRule> getValidationRules();
    
    Instant getCreatedAt();
    boolean isActive();
}

/**
 * A single field in a form.
 * Fields can be simple values or entity references.
 */
public interface FormField {
    
    String getCode();
    String getLabel();
    FieldType getType();      // TEXT, NUMBER, DATE, BOOLEAN, ENTITY_REFERENCE, ...
    boolean isRequired();
    Map<String, Object> getConfig();  // Type-specific config
    
    // For ENTITY_REFERENCE fields:
    // config contains: {"entityType": "facility", "filter": "..."}
}

/**
 * A filled form instance.
 * Linked to a form version and to contextual entities.
 */
public interface Submission {
    
    ULID getId();
    String getUid();
    
    FormVersion getFormVersion();
    
    // Who submitted
    Entity getSubmittedBy();        // The actor
    Instant getSubmittedAt();
    
    // Context — what entities this submission relates to
    List<EntityReference> getEntityReferences();
    
    // The actual data
    Map<String, Object> getValues();
    
    // State (uses the State primitive)
    StateInstance getState();
}
```

### 3. Entity Reference

The linking mechanism between capture and entities, and between any constructs.

```java
/**
 * A typed, stable reference from one construct to an entity.
 * Used in: form fields, flow scope, submission context.
 */
public interface EntityReference {
    
    String getReferenceRole();     // e.g., "target_facility", "reporter", "sender"
    EntityType getEntityType();    // What kind of entity
    String getEntityUid();         // Which specific entity
    
    // Resolution — the reference can be resolved to a full entity
    Entity resolve();
}
```

### 4. State

A universal state machine primitive. Used by flows, entities, and submissions.

```java
/**
 * Defines a state machine — states, transitions, and guards.
 * This is the DEFINITION, not an instance.
 */
public interface StateDefinition {
    
    String getCode();          // e.g., "reporting_lifecycle", "entity_lifecycle"
    List<String> getStates();  // e.g., ["draft", "submitted", "approved", "rejected"]
    String getInitialState();
    List<String> getTerminalStates();  // e.g., ["approved", "rejected"]
    
    List<StateTransition> getTransitions();
}

public interface StateTransition {
    
    String getFromState();
    String getToState();
    
    // Guards — conditions that must be met for this transition
    List<TransitionGuard> getGuards();
}

/**
 * A guard on a state transition.
 * Can be role-based, condition-based, or both.
 */
public interface TransitionGuard {
    
    GuardType getType();
    
    // For ROLE_REQUIRED: which role must the actor have?
    String getRequiredRole();      // e.g., "district_supervisor"
    
    // For CONDITION: what condition must be true?
    String getConditionExpression(); // e.g., "submission.isComplete()"
}

enum GuardType {
    ROLE_REQUIRED,    // Actor must have a specific role
    CONDITION,        // A condition expression must evaluate to true
    BOTH              // Role AND condition
}

/**
 * A live instance of a state machine — the actual current state of something.
 * Attached to: an entity, a submission, or a flow instance.
 */
public interface StateInstance {
    
    StateDefinition getDefinition();
    String getCurrentState();
    
    List<StateTransition> getAvailableTransitions(Actor actor);
    
    // Transition — returns the event that was produced
    Event transition(String toState, Actor actor);
    
    // History
    List<StateChange> getHistory();
}

public interface StateChange {
    String getFromState();
    String getToState();
    Actor getActor();
    Instant getTimestamp();
    String getComment();
}
```

### 5. Event

The audit and integration backbone.

```java
/**
 * Something that happened in the platform.
 * Immutable. Traceable. The backbone for audit, reactions, and integration.
 */
public interface Event {
    
    ULID getId();
    String getType();              // e.g., "submission.created", "state.transitioned", "entity.updated"
    Instant getOccurredAt();
    
    // Who caused it
    Actor getActor();
    
    // What it relates to (entity, submission, flow, etc.)
    String getSubjectType();       // "entity", "submission", "flow"
    String getSubjectUid();
    
    // Event-specific data
    Map<String, Object> getPayload();
    
    // Correlation — links related events across flows/entities
    String getCorrelationId();
}
```

### 6. Flow

The process-centric organizing construct. A Flow is a first-class, trackable, queryable operational process.

```java
/**
 * DEFINITION of a flow — the template/blueprint.
 * Comparable to FormTemplate: the definition is reused, instances are created per period/occurrence.
 */
public interface FlowDefinition {
    
    ULID getId();
    String getUid();
    String getCode();           // e.g., "monthly_inventory_report"
    String getName();           // e.g., "Monthly Inventory Report"
    
    // What form to capture
    FormTemplate getCaptureTemplate();
    
    // Lifecycle — uses State primitive
    StateDefinition getLifecycleDefinition();
    
    // Schedule — when this flow runs (null for ad-hoc/event-triggered flows)
    ScheduleConfig getSchedule();
    
    // Scope — which entities this flow targets
    ScopeConfig getScope();
    
    // Actors — who participates, in what role
    List<ActorRole> getActorRoles();
}

/**
 * Schedule configuration — part of a flow definition.
 */
public interface ScheduleConfig {
    
    ScheduleType getType();     // RECURRING, ONE_TIME, EVENT_TRIGGERED
    
    // For RECURRING:
    Period getPeriod();          // DAILY, WEEKLY, MONTHLY, YEARLY
    int getDayOfPeriod();       // e.g., 5 (5th of month)
    
    // For ONE_TIME:
    Instant getStartDate();
    Instant getEndDate();
    
    // For late detection:
    Duration getGracePeriod();  // e.g., 10 days after due
}

/**
 * Scope configuration — which entities a flow targets.
 */
public interface ScopeConfig {
    
    EntityType getTargetEntityType();   // e.g., "facility"
    
    // Static list OR dynamic filter
    List<String> getExplicitEntityUids();   // null if using filter
    String getFilterExpression();            // e.g., "attributes.has_warehouse = true"
}

/**
 * Actor role in a flow — who does what.
 */
public interface ActorRole {
    
    String getRoleCode();       // e.g., "reporter", "reviewer"
    String getRoleLabel();      // e.g., "Facility Pharmacist", "District Supervisor"
    
    // How actors are assigned: per scope entity, per flow, manual, etc.
    AssignmentStrategy getAssignment();
}

enum AssignmentStrategy {
    PER_SCOPE_ENTITY,   // One actor per entity in scope (e.g., one pharmacist per facility)
    PER_FLOW,           // One actor for the entire flow (e.g., one campaign coordinator)
    MANUAL              // Assigned individually, not by rule
}

/**
 * A LIVE INSTANCE of a flow — one occurrence.
 * For a monthly report: one FlowInstance per month.
 * For a campaign: one FlowInstance per campaign.
 */
public interface FlowInstance {
    
    ULID getId();
    String getUid();
    
    FlowDefinition getDefinition();
    
    // Period (for recurring flows)
    Period getFlowPeriod();         // e.g., "2026-03" for March 2026
    
    // State (uses State primitive)
    StateInstance getState();       // e.g., "open", "closed", "reconciled"
    
    // The work items — one per entity in scope
    List<FlowTask> getTasks();
    
    // Aggregate status
    FlowProgress getProgress();
}

/**
 * A single unit of work within a flow instance — one entity's participation.
 * For inventory reporting: one FlowTask per facility per month.
 */
public interface FlowTask {
    
    ULID getId();
    
    // Which entity this task is for
    EntityReference getTargetEntity();  // e.g., facility X
    
    // Who is assigned
    Map<String, EntityReference> getAssignedActors();  // role → actor
    
    // State (uses State primitive)
    StateInstance getState();        // e.g., "pending", "submitted", "approved"
    
    // The submission (when captured)
    Submission getSubmission();      // null until submitted
    
    // Timing
    Instant getDueDate();
    boolean isLate();
}

/**
 * Aggregate progress of a flow instance.
 */
public interface FlowProgress {
    
    int getTotalTasks();
    int getCompletedTasks();
    int getPendingTasks();
    int getLateTasks();
    int getApprovedTasks();
    int getRejectedTasks();
    
    double getCompletionRate();     // completed / total
}
```

---

## Scenario A: Monthly Facility Inventory Reporting

### Setup (one-time configuration)

```
Step 1: Entity types exist
  → EntityType "facility" (HIERARCHICAL, attributes: [has_warehouse, district, level])
  → EntityType "item" (FLAT, attributes: [category, unit_of_measure])
  → EntityType "user" (FLAT)

Step 2: Form template exists
  → FormTemplate "inventory_report_v3"
    Fields:
      - item_reference (ENTITY_REFERENCE → entityType: "item")
      - opening_balance (NUMBER)
      - quantity_received (NUMBER)
      - quantity_dispensed (NUMBER)
      - closing_balance (NUMBER)
      - stock_out_days (NUMBER)

Step 3: Flow definition created
  → FlowDefinition "monthly_inventory_report"
    captureTemplate: inventory_report_v3
    lifecycle: StateDefinition {
      states: [pending, submitted, approved, rejected]
      initial: pending
      terminal: [approved, rejected]
      transitions:
        pending → submitted (guard: CONDITION, "submission.isComplete()")
        submitted → approved (guard: ROLE_REQUIRED, "district_supervisor")
        submitted → rejected (guard: ROLE_REQUIRED, "district_supervisor")
        rejected → submitted (guard: CONDITION, "submission.isRevised()")
    }
    schedule: RECURRING, MONTHLY, day 5, gracePeriod: 10 days
    scope: entityType "facility", filter "has_warehouse = true"
    actors:
      - reporter: PER_SCOPE_ENTITY  (one pharmacist per facility)
      - reviewer: PER_FLOW          (one district supervisor)
```

### Runtime (monthly cycle)

```
Month begins (March 2026):

1. Platform evaluates schedule → creates FlowInstance "monthly_inventory_report/2026-03"
   → State: "open"

2. Platform evaluates scope → finds 200 facilities matching "has_warehouse = true"

3. Platform creates 200 FlowTasks, one per facility
   → Each task: targetEntity=facility_X, state=pending, dueDate=2026-03-05
   → Each task: assignedActors = {reporter: facility_X_pharmacist}

4. Mobile sync: Pharmacist opens app
   → GET /flows/my-tasks?period=2026-03
   → Returns: [{flowTask: task_123, form: inventory_report_v3, facility: "Kabale HC III", due: "2026-03-05"}]

5. Pharmacist fills form, submits
   → POST /submissions {formVersion: v3, entityReferences: [{role: "target", uid: "facility_kabale"}], values: {...}}
   → FlowTask.submission = new submission
   → FlowTask.state transitions: pending → submitted
   → Event emitted: {type: "flow_task.submitted", subject: "flow_task_123", actor: "user_pharmacist_1"}

6. District supervisor reviews
   → GET /flows/monthly_inventory_report/2026-03/tasks?state=submitted
   → Reviews submission data
   → POST /flows/tasks/task_123/transition {toState: "approved"}
   → FlowTask.state transitions: submitted → approved
   → Event emitted: {type: "flow_task.approved", subject: "flow_task_123", actor: "user_supervisor_1"}

7. Progress query at any time:
   → GET /flows/monthly_inventory_report/2026-03/progress
   → Returns: {total: 200, completed: 145, pending: 40, late: 15, completionRate: 0.725}

8. Grace period expires (March 15):
   → Platform evaluates: tasks still in "pending" state → marked as late
   → Event emitted: {type: "flow_task.late", subject: "flow_task_456"}
   → (Future: Reaction layer could auto-notify supervisor)

9. End of cycle:
   → All tasks resolved OR month closes
   → FlowInstance state: "open" → "closed"
```

### What this proves about the architecture

- **Entity, Capture, State, Event, Entity Reference, Flow** — all 6 first-class constructs are used
- **Assignment** works as a pattern (property of FlowTask via ActorRole), not as a first-class construct
- **Schedule** works as a pattern (property of FlowDefinition), not independent
- **Scope** works as a pattern (property of FlowDefinition), evaluated at flow instance creation
- **Approval** works as State transitions with role guards — no separate Approval construct needed
- **FlowProgress** answers "what's the status?" as a first-class query
- **Mobile sync** is clean: "get my flow tasks" is one query

---

## Scenario B: Facility Registry with Lifecycle

### Setup

```
Step 1: Entity type with lifecycle
  → EntityType "facility"
    structure: HIERARCHICAL
    attributes: [name, code, level, district, has_warehouse, gps_coordinates, ...]
    
  → StateDefinition "entity_lifecycle"
    states: [draft, active, suspended, deactivated]
    initial: draft
    terminal: [deactivated]
    transitions:
      draft → active (guard: ROLE_REQUIRED, "registry_admin")
      active → suspended (guard: ROLE_REQUIRED, "registry_admin")
      suspended → active (guard: ROLE_REQUIRED, "registry_admin")
      active → deactivated (guard: ROLE_REQUIRED, "registry_admin")
      suspended → deactivated (guard: ROLE_REQUIRED, "registry_admin")
```

### Runtime

```
1. New facility registration
   → POST /entities {type: "facility", code: "KBL-HC3", name: "Kabale HC III", attributes: {...}}
   → Entity created with StateInstance: "draft"
   → Event: {type: "entity.created", subject: "entity_kbl_hc3"}

2. Admin reviews and activates
   → POST /entities/kbl_hc3/transition {toState: "active"}
   → State: draft → active
   → Event: {type: "entity.state_changed", subject: "entity_kbl_hc3", payload: {from: "draft", to: "active"}}

3. Facility now appears in flow scopes
   → FlowDefinition "monthly_inventory_report" has scope filter "has_warehouse = true"
   → Next flow instance creation evaluates scope → includes newly active facility
   (Only "active" entities are included in scope evaluation — "draft" and "suspended" are excluded)

4. Facility attribute update
   → PATCH /entities/kbl_hc3 {attributes: {has_warehouse: false}}
   → Event: {type: "entity.updated", subject: "entity_kbl_hc3"}
   → Next flow instance: facility excluded from inventory scope (no warehouse)

5. Facility suspension
   → POST /entities/kbl_hc3/transition {toState: "suspended"}
   → Facility excluded from all new flow scopes
   → Existing in-progress FlowTasks are NOT auto-cancelled (explicit decision needed)
   → Event: {type: "entity.state_changed", payload: {from: "active", to: "suspended"}}
```

### What this proves about the architecture

- **Entity lifecycle works through State as a core primitive** — no "lifecycle capability" construct needed. State is attached directly to the entity.
- **No Flow is needed** for registry management. Entity + State covers it.
- **Entity state affects flow scope** — this is the interaction point. Scope evaluation respects entity state.

---

## Interaction Test: Flow ↔ Entity Capabilities

The key question: **how do entity state changes affect running flows?**

```
Scenario: Facility is suspended while a flow instance is active

Timeline:
  March 1: FlowInstance "monthly_inventory_report/2026-03" created
           → 200 facilities in scope, including Kabale HC III
           → FlowTask for Kabale HC III: state = pending

  March 8: Kabale HC III is suspended
           → Entity state: active → suspended
           → Event: {type: "entity.state_changed", subject: "entity_kbl_hc3"}

  Question: What happens to the FlowTask for Kabale HC III?
```

**Three options:**

| Option | Behavior | Trade-off |
|---|---|---|
| **A: Nothing** | FlowTask stays pending. Suspension doesn't affect in-flight tasks. | Simple. But task stays open for a suspended facility. |
| **B: Auto-cancel** | FlowTask is automatically cancelled. | Clean. But requires the Flow to listen to entity events — coupling. |
| **C: Flag for review** | FlowTask is flagged (e.g., state → "blocked"). Supervisor decides. | Most flexible. But adds complexity. |

**My recommendation: Option A for now, Option C as a future Reaction.**

Reasoning:
- Option A keeps Flow and Entity independent — no coupling between them
- The Reaction layer (when built) would handle this: `on entity.state_changed(suspended) → if entity has active FlowTasks → flag them`
- This validates the architecture: **Flows and Entity State interact through Events, not through direct coupling.** The Reaction layer is the mediator.

This is the concrete answer to "how do Flows and Capabilities interact?" They don't call each other. They produce Events. Future Reactions connect them.

---

## API Surface Summary

```
Core:
  GET/POST /entities                       — CRUD for entities
  GET/POST /entities/:uid/transition       — State transitions on entities
  GET/POST /submissions                    — CRUD for submissions

Flows:
  GET      /flows                          — List flow definitions
  GET      /flows/:code/instances          — List instances (e.g., per period)
  GET      /flows/:code/:period/progress   — Aggregate progress
  GET      /flows/:code/:period/tasks      — List tasks (filterable by state)
  POST     /flows/tasks/:id/transition     — State transition on a task
  GET      /flows/my-tasks                 — Tasks assigned to current user

Events:
  GET      /events?subject=:uid            — Event history for any subject

References:
  GET      /entities/:uid/references       — Everything that references entity X
```

---

## Ambiguities Resolved by This Modeling

| Question from earlier | Resolution |
|---|---|
| Is State one construct or two? | **One.** StateDefinition is reusable. StateInstance is attached to entities, submissions, and flow tasks uniformly. |
| Where does Assignment live? | **Pattern in Flow.** ActorRole defines assignment strategy. FlowTask carries assigned actors. No standalone Assignment module. |
| How do Flows and Capabilities interact? | **Through Events.** Entity state changes emit events. Flows (or future Reactions) can listen and respond. No direct coupling. |
| Does basic capture need a Flow? | **No.** POST /submissions works without any flow. Flows are optional process wrappers. |
| Who owns aggregate status? | **FlowInstance.** FlowProgress is a first-class query on the flow. |

## Remaining Open Points

1. **Scope evaluation timing.** Scope is evaluated when a FlowInstance is created. What if entities join or leave the scope mid-cycle? (e.g., new facility activated on March 10 — does it get a task for March?)

2. **FlowTask granularity.** In inventory reporting, one task = one facility × one period. In campaigns, one task might = one team × one day × one scope. The FlowTask construct must be flexible enough for both shapes.

3. **Reaction layer shape.** The modeling shows Events connecting Flows and Entities. The Reaction layer formalizes this connection. Its minimum viable shape: `(eventFilter, condition, action)` triplets. But this is deferred — event listeners in domain code work first.