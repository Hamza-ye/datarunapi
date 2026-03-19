# RFC: Planning & Execution Orchestration Bounded Context

## Status
Proposed - Initial Draft

## Context
The current system models planning artifacts (`assignment`, `activity`, `team`) primarily as database link tables used for both access control and execution behavior. This causes tight coupling, hidden rules, and brittle behavior when plans change.

A proper Planning Bounded Context (BC) is focused on **intent**, **accountability**, and **governance**. It captures the decision to do work (why, what, where, by whom, when), emits that intent, and exposes it for downstream systems to act on or report against.

## Problem
- **Artifacts are treated as technical joins**, not as meaningful planning objects, so they lack lifecycle, accountability, and expressiveness.
- **Planning and execution are fused**, causing execution logic to change unexpectedly when planning metadata changes.
- **Intent is not explicit**, making it hard to track progress, audit decisions, or answer the question “what was the plan?”
- **Access control logic is scattered** (filters, denormalized caches) rather than built from explicit planning intent.

## Vision
A real Planning BC is a first-class domain that:
- models **intent and commitment**, not just relationships
- provides **rich artifacts** that can be used for reporting, auditing, and governance
- publishes an **event stream of intent** that other BCs can consume
- allows execution to run with or without a plan
- supports progress tracking and measurement without coupling to execution

---

## Core Domain Artifacts (Suggested)

### `Plan` (Campaign / Program)
**Purpose:** Captures the overall intent, goals, and boundaries of a coordinated effort.

Key fields (example):
- `planUid`, `code`, `name`, `description`
- `ownerUserUid`, `sponsor`, `stakeholders`
- `startDate`, `endDate`, `status` (`DRAFT`, `PUBLISHED`, `ACTIVE`, `COMPLETED`, `CANCELLED`)
- `scope` (reference to Scope)
- `successCriteria` (links to SuccessCriteria)

### `WorkPackage` / `Phase`
**Purpose:** Groups related work within a plan (phases, waves, work streams).

Key fields:
- `workPackageUid`, `planUid`, `name`, `description`
- `startDate`, `endDate`, `status`
- `dependencies` (other work packages)

### `WorkItem` / `Task` / `Activity`
**Purpose:** A discrete unit of planned work expected to be carried out.

Key fields:
- `workItemUid`, `workPackageUid`, `name`, `description`
- `type` (e.g., `SURVEY`, `INSPECTION`, `TRAINING`)
- `expectedEffort`, `dueDate`, `status` (`PLANNED`, `READY`, `IN_PROGRESS`, `DONE`, `BLOCKED`)
- `requiredCapabilities` (e.g., `ENUMERATOR`, `SUPERVISOR`)
- `successCriteriaUid` (optional)

### `Allocation` / `Commitment`
**Purpose:** Represents an explicit commitment of a role/person/team to a work item.

Key fields:
- `allocationUid`, `workItemUid`, `assigneeType` (`USER`, `TEAM`, `ROLE`), `assigneeUid`
- `allocationType` (`PRIMARY`, `SECONDARY`, `REVIEWER`)
- `assignedAt`, `assignedBy`, `status` (`ASSIGNED`, `ACCEPTED`, `DECLINED`, `REVOKED`)

### `Scope` / `Target`
**Purpose:** Defines the domain (org units, geography, segments) to which the plan applies.

Key fields:
- `scopeUid`, `type` (`ORG_UNIT`, `REGION`, `SEGMENT`, `CUSTOM`)
- `definition` (list of IDs or rule expression)

### `SuccessCriteria` / `Outcome`
**Purpose:** Defines how success is measured (for reporting and closure).

Key fields:
- `successCriteriaUid`, `planUid`, `description`
- `metric` (optional), `targetValue`, `threshold`

### `Constraint` / `Policy`
**Purpose:** Encodes non-functional rules that must be enforced (e.g., “must be done by certified user”, “max 5 per day”).

Key fields:
- `constraintUid`, `type` (`ROLE_REQUIRED`, `MAX_PER_DAY`, etc.)
- `expression` (structured or JSON rule)
- `appliesTo` (work items, allocations, scope)

### `Progress Snapshot` / `PlanReport` (read model)
**Purpose:** A derived view that answers “how are we doing?” without coupling to execution.

Key fields:
- `planUid`, `asOf`, `workItemsPlanned`, `workItemsCompleted`, `completionPct`
- `openAllocations`, `overdueItems`, `riskFlags`

---

## Domain Events (Intent Stream)
Planning BC publishes intent events that downstream systems consume. Examples:

- `PlanCreated`, `PlanPublished`, `PlanCancelled`, `PlanCompleted`
- `WorkPackageCreated`, `WorkPackageStarted`, `WorkPackageCompleted`
- `WorkItemCreated`, `WorkItemAssigned`, `WorkItemStarted`, `WorkItemCompleted`, `WorkItemBlocked`
- `AllocationCreated`, `AllocationRevoked`, `AllocationAccepted`, `AllocationDeclined`
- `ScopeUpdated`, `SuccessCriteriaUpdated`, `ConstraintUpdated`

These events represent the “plan as it was intended” and are separate from execution results.

---

## Boundaries & Dependencies

### Inbound
- **Org Structure BC** (for org unit references)
- **Team/Collaboration BC** (for team membership and structure)
- **Identity/IAM BC** (for user/role/claims)
- **Templates/Forms BC** (for linking work items to forms)

### Outbound
- **Execution/Data Collection BC** (receives intent events; may optionally link executions to work items)
- **Reporting/Analytics BC** (consumes events to produce progress snapshots)
- **Policy/Access layer** (derives authorization decisions from planning intent)

### Decoupling principle
The Planning BC does **not** own execution data (submissions, task completion logs). It provides intent and optional correlation identifiers (e.g., `workItemUid`) that execution systems can reference.

---

## Plan authoring API (minimal sketch)

The Planning BC exposes a lightweight REST surface for authoring intent and a corresponding event stream for downstream consumers. The HTTP API is ultimately a convenient UX layer; the canonical intent record is captured in the domain model and snapshot events.

### Core REST endpoints (intent write APIs)
These endpoints are the minimal set required for creating and evolving plans, work packages, work items, and allocations.

#### Create a new plan
`POST /plans`

Request
```json
{
  "code": "2026-STATE-HEALTH-CAMPAIGN",
  "name": "State Health Survey Campaign",
  "description": "Collect health survey data across all districts.",
  "ownerUserUid": "user-123",
  "startDate": "2026-05-01",
  "endDate": "2026-09-30",
  "scope": {
    "type": "ORG_UNIT",
    "definition": ["org-789", "org-456"]
  },
  "successCriteria": [
    {"description": "Survey coverage >= 80% of targets", "metric": "coverage", "targetValue": 0.8}
  ]
}
```

Success
```json
{
  "planUid": "plan-001",
  "status": "DRAFT",
  "createdAt": "2026-03-19T12:00:00Z"
}
```

#### Publish a plan (move from draft to active)
`PATCH /plans/{planUid}`

Request
```json
{
  "status": "PUBLISHED"
}
```

#### Add a work package (phase) to a plan
`POST /plans/{planUid}/work-packages`

Request
```json
{
  "name": "Phase 1 – Enumerator Training",
  "description": "Train enumerators and supervisors in Week 1.",
  "startDate": "2026-05-01",
  "endDate": "2026-05-07"
}
```

Success
```json
{
  "workPackageUid": "wp-123",
  "planUid": "plan-001"
}
```

#### Create a work item
`POST /work-packages/{workPackageUid}/work-items`

Request
```json
{
  "name": "Conduct household survey",
  "description": "Visit assigned households and record responses.",
  "type": "SURVEY",
  "dueDate": "2026-06-10",
  "requiredCapabilities": ["ENUMERATOR"],
  "expectedEffortHours": 4
}
```

Success
```json
{
  "workItemUid": "wi-456",
  "workPackageUid": "wp-123"
}
```

#### Allocate a work item
`POST /work-items/{workItemUid}/allocations`

Request (team allocation)
```json
{
  "assigneeType": "TEAM",
  "assigneeUid": "team-33",
  "allocationType": "PRIMARY",
  "assignedBy": "user-123",
  "notes": "Primary field execution team"
}
```

Success
```json
{
  "allocationUid": "alloc-999",
  "workItemUid": "wi-456",
  "status": "ASSIGNED"
}
```

#### Update work item status (optionally by plan author)
`PATCH /work-items/{workItemUid}`

Request
```json
{
  "status": "READY"
}
```

This endpoint supports plan-driven state changes (e.g., moving work items from PLANNED → READY → BLOCKED) and triggers intent events.

---

## Event stream (intent payloads)
The Planning BC emits intent events that downstream systems ingest. Each event includes a `correlationId` and `origin` metadata to support traceability.

### `PlanCreated`
```json
{
  "eventType": "PlanCreated",
  "eventId": "evt-01",
  "timestamp": "2026-03-19T12:00:00Z",
  "payload": {
    "planUid": "plan-001",
    "code": "2026-STATE-HEALTH-CAMPAIGN",
    "name": "State Health Survey Campaign",
    "ownerUserUid": "user-123",
    "startDate": "2026-05-01",
    "endDate": "2026-09-30",
    "status": "DRAFT"
  },
  "metadata": {
    "origin": "planning-service",
    "correlationId": "req-abc"
  }
}
```

### `WorkItemCreated`
```json
{
  "eventType": "WorkItemCreated",
  "eventId": "evt-02",
  "timestamp": "2026-03-19T12:05:00Z",
  "payload": {
    "workItemUid": "wi-456",
    "workPackageUid": "wp-123",
    "name": "Conduct household survey",
    "type": "SURVEY",
    "dueDate": "2026-06-10",
    "status": "PLANNED"
  },
  "metadata": {
    "origin": "planning-service",
    "correlationId": "req-def"
  }
}
```

### `AllocationCreated`
```json
{
  "eventType": "AllocationCreated",
  "eventId": "evt-03",
  "timestamp": "2026-03-19T12:06:00Z",
  "payload": {
    "allocationUid": "alloc-999",
    "workItemUid": "wi-456",
    "assigneeType": "TEAM",
    "assigneeUid": "team-33",
    "status": "ASSIGNED"
  },
  "metadata": {
    "origin": "planning-service",
    "correlationId": "req-ghi"
  }
}
```

### `WorkItemStatusChanged`
```json
{
  "eventType": "WorkItemStatusChanged",
  "eventId": "evt-04",
  "timestamp": "2026-03-19T12:10:00Z",
  "payload": {
    "workItemUid": "wi-456",
    "previousStatus": "PLANNED",
    "newStatus": "READY"
  },
  "metadata": {
    "origin": "planning-service",
    "correlationId": "req-jkl"
  }
}
```

---

## Notes on API design
- **Idempotency**: All write endpoints should support an optional `Idempotency-Key` header to allow safe retries.
- **Audit & Traceability**: Each write request should record `createdBy`/`updatedBy` and emit an event with `correlationId`.
- **Command–Query separation**: Write APIs should be treated as commands (validate, persist, emit events), while read APIs should serve projections (progress snapshots, reports).
- **Validation**: Validate that referenced ids exist (e.g., `planUid`, `workPackageUid`) and enforce business rules (e.g., cannot publish a plan with no work items).
- **Event-first mindset**: Treat emitted events (e.g., `PlanCreated`, `AllocationCreated`) as the canonical record of intent; projections are derived artifacts.
- **Optional linkage to execution**: The API should not require execution systems to be aware of plan IDs; it should be optional for them to include `workItemUid` when writing submissions.

---

## Example Real-World Scenarios

### 1) Campaign + execution tracking
1. Planner defines a `Plan` with `WorkItems`.
2. Planner allocates tasks to a `Team` or `Role` via `Allocation`.
3. Field workers submit data; Data Collection BC emits a `SubmissionCreated` event with `workItemUid`.
4. Reporting BC correlates and updates plan progress.

### 2) Ad-hoc collection later linked to a plan
1. Field worker submits data without a plan.
2. Planner later creates a `Plan` and links those submissions to work items (via event or reconciliation tool).
3. Progress updates without breaking execution.

---

## Migration / Evolution Path (Incremental)
1. Implement the Planning BC domain model + event stream without changing submission tables.
2. Build read-model projections (progress snapshots) based on events.
3. Optionally allow data collection to reference `workItemUid` (soft link, not foreign key).
4. Iterate: add success criteria, constraints, and reporting layers.

---

## Open Questions
- What granularity of progress tracking is required (per plan, per work item, per team)?
- How should execution results be linked to planning intent (if at all)?
- Should planning support multi-party allocations with different roles/weights?
- What is the termination model for plans (automatic vs manual closure)?

---

## Benefits
- Clear separation of intent vs execution.
- Real artifacts for reporting and audit.
- Execution can run independently of planning.
- Planning becomes a stable, extensible domain.

---

## Next Steps
1. Validate this model against real planning stories from product/operations.
2. Refine artifact names to match ubiquitous language.
3. Prototype a minimal implementation (entities + events + projection) to validate the architecture.
