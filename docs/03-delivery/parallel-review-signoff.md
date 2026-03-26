# Parallel Expert Review — Architecture Baseline v1 & Monthly Inventory Reference Scenario

> **Date:** 2025-03-25  
> **Inputs:** [baseline-v1.md](../02-baseline/baseline-v1.md), [ref-monthly-inventory.md](../06-scenarios/ref-monthly-inventory.md), [platform-ambition.md](../01-vision/platform-ambition.md), [legacy-boundary-strategy.md](../02-baseline/legacy-boundary-strategy.md), [open-decisions-log.md](../02-baseline/open-decisions-log.md)

---

## 1. Software Architect — Baseline Coherence Check

**Verdict: Baseline is coherent enough to freeze. No redlines found.**

| Item | Status | Notes |
|------|--------|-------|
| Six constructs are orthogonal | ✅ Frozen | No overlap. Each has a clear single responsibility. |
| Flow ↔ Entity decoupling via Events | ✅ Frozen | Clean. Prevents the dependency tangle. |
| State as universal primitive | ✅ Frozen | One mechanism for FlowTask lifecycle AND entity lifecycle. Elegant. |
| Entity Reference as cross-cutting link | ✅ Frozen | Avoids foreign-key coupling between construct tables. |
| "Patterns not constructs" for assignment/schedule/scope | ✅ Frozen | Good discipline. Avoids premature reification. |
| Scope snapshot at FlowInstance creation | ✅ Frozen | Denominator stability is critical for progress tracking. |

### Open but NOT blocking

| Item | Assessment |
|------|------------|
| **Repeatable form sections** (OD-005 adjacent) | The scenario uses one submission with 50 repeated sections. FormTemplate must support repeatable sections with per-section entity references. This is a *capture template design detail*, not an architecture gap. Decide during Capture module implementation. **Later.** |
| **StateDefinition reusability model** | A StateDefinition is described as "reusable" but the scenario shows it embedded in FlowDefinition. Clarify whether StateDefinitions are globally registered (sharable across flows) or inline per FlowDefinition. Low risk either way — the constructs are valid in both interpretations. **Open, not blocking.** |
| **FlowProgress as stored vs computed** | The scenario shows `GET /progress` returning aggregated counts. Is FlowProgress a stored materialized view or computed on read? For 200 tasks it doesn't matter. For 50,000 tasks (campaign scenario) it will. Flag for implementation. **Open, not blocking.** |

### Potential contradiction (minor)

| Item | Assessment |
|------|------------|
| **OD-007 vs legacy-boundary-strategy** | OD-007 says module structure is "open." But `legacy-boundary-strategy.md` already commits to a 5-module Maven layout (`legacy`, `platform-core`, `platform-capture`, `platform-flow`, `app`). Recommend closing OD-007 — the decision has been made. **Housekeeping, not blocking.** |

---

## 2. Backend Architect — Minimal Contract Surface for Monthly Inventory

### Core Module (`platform-core`)

```
Entity
  id: UUID (PK)
  uid: String (unique, immutable)
  code: String (unique per type)
  name: String
  typeId: FK → EntityType
  attributes: JSONB
  createdAt, updatedAt: Timestamp

EntityType
  id: UUID (PK)
  code: String (unique) — e.g. "facility", "item", "user"
  structure: Enum [FLAT, HIERARCHICAL, GROUPED]
  attributeSchema: JSONB (optional — defines valid attribute keys/types)

EntityReference
  id: UUID (PK)
  sourceType: String — e.g. "submission", "flow_task"
  sourceId: UUID
  targetEntityId: FK → Entity
  role: String — e.g. "target", "item_reference", "assigned_actor"

StateDefinition
  id: UUID (PK)
  code: String (unique)
  states: JSONB — [{name, terminal: bool}]
  transitions: JSONB — [{from, to, guards:[]}]
  initialState: String

StateInstance
  id: UUID (PK)
  definitionId: FK → StateDefinition
  subjectType: String — e.g. "flow_task", "entity"
  subjectId: UUID
  currentState: String
  updatedAt: Timestamp

Event
  id: UUID (PK)
  type: String — e.g. "flow_task.submitted"
  subjectType: String
  subjectId: UUID
  actorId: UUID
  timestamp: Timestamp
  correlationId: UUID (nullable)
  payload: JSONB
```

### Capture Module (`platform-capture`)

```
FormTemplate
  id: UUID (PK)
  code: String (unique) — e.g. "inventory_report"
  latestVersionId: FK → FormVersion (nullable)

FormVersion
  id: UUID (PK)
  templateId: FK → FormTemplate
  version: Integer
  fields: JSONB — ordered list of field definitions
  createdAt: Timestamp

Submission
  id: UUID (PK)
  uid: String (unique)
  formVersionId: FK → FormVersion
  values: JSONB
  submittedBy: UUID
  submittedAt: Timestamp
```

### Flow Module (`platform-flow`)

```
FlowDefinition
  id: UUID (PK)
  code: String (unique) — e.g. "monthly_inventory_report"
  captureTemplateId: FK → FormTemplate
  lifecycleId: FK → StateDefinition
  schedule: JSONB — {type, frequency, dayOfMonth, gracePeriodDays}
  scope: JSONB — {entityTypeCode, filter}
  actors: JSONB — {reporter: {assignment}, reviewer: {assignment}}

FlowInstance
  id: UUID (PK)
  definitionId: FK → FlowDefinition
  period: String — "2026-03"
  state: String — "active" | "closed"
  createdAt: Timestamp

FlowTask
  id: UUID (PK)
  instanceId: FK → FlowInstance
  targetEntityId: FK → Entity
  submissionId: FK → Submission (nullable)
  dueDate: Date
  isLate: Boolean (default false)

FlowProgress (view or materialized — TBD)
  instanceId, total, submitted, approved, rejected, pending, late, completionRate
```

### Minimal API for the scenario

| Verb | Path | Module |
|------|------|--------|
| `POST` | `/entities` | core |
| `GET` | `/entities/:uid` | core |
| `PATCH` | `/entities/:uid` | core |
| `POST` | `/submissions` | capture |
| `GET` | `/submissions/:uid` | capture |
| `GET` | `/flows` | flow |
| `GET` | `/flows/:code/:period` | flow |
| `GET` | `/flows/:code/:period/progress` | flow |
| `GET` | `/flows/:code/:period/tasks` | flow |
| `POST` | `/flows/tasks/:id/transition` | flow |
| `GET` | `/flows/my-tasks` | flow |
| `GET` | `/events?subject=:uid` | core |

> [!IMPORTANT]
> **Dependency direction is strict**: `flow → capture → core`. No reverse imports. ArchUnit enforces this from Day 1.

---

## 3. Reality Checker — Does the Scenario Survive Real Usage?

**Assessment: B+ — solid modeling, with 5 real-world edge cases that need answers before implementation, none of which break the architecture.**

### Edge cases flagged

| # | Edge Case | Risk | Verdict |
|---|-----------|------|---------|
| 1 | **Pharmacist transfer mid-cycle** — Pharmacist A leaves facility on March 10. Pharmacist B takes over. Who owns the FlowTask? | Medium | FlowTask has `assignedActors`. Need a re-assignment operation (admin action → Event). The baseline supports this — just needs a defined process. **Open, not blocking.** |
| 2 | **Partial submission / save-as-draft** — Pharmacist fills 30 of 50 items and loses connectivity. What state is the task? | Medium | The scenario jumps from `pending` to `submitted`. Need an intermediate state or the mobile app holds the draft locally until complete. Recommend: **drafts are client-side only; `submitted` means complete.** If server-side drafts are needed, it's just adding a `draft` state. **Open, not blocking.** |
| 3 | **Resubmission after rejection** — The scenario says `rejected → submitted` on revision. But what happens to the *old* submission? Is it replaced or versioned? | Medium | The baseline says "one FlowTask = one primary submission." On resubmission, either: (a) old submission is immutable and a new one replaces the reference, or (b) submission values are updated in place. Option (a) preserves audit trail. **Recommend (a). Open, not blocking.** |
| 4 | **Scope amendment after creation** — Baseline says scope amendments are explicit and auditable. But the scenario doesn't show the API for this. | Low | Need: `POST /flows/:code/:period/scope-amendments {action: "add"|"remove", entityId}`. Not complex, just needs a defined endpoint. **Later.** |
| 5 | **Concurrent approval conflict** — Two supervisors approve/reject the same task simultaneously. | Low | State transition guards + optimistic locking on `StateInstance.updatedAt` handle this. Standard. **Frozen (implicit in State construct).** |

### What would actually break it

Nothing in the current scenario breaks the baseline. The risk is **Scenario 9 (campaigns)**, which requires flow phases — and that is correctly deferred to OD-006.

---

## 4. SRE — Operational Baseline & Early Failure Modes

### SLOs for Day 1

| Signal | SLI | Target | Rationale |
|--------|-----|--------|-----------|
| **Availability** | `count(2xx+4xx) / count(total)` | 99.5% (30d) | Modular monolith, single DB — 99.5% is achievable and honest. |
| **Latency** | `p95(GET /flows/my-tasks)` | < 500ms | Mobile clients on variable networks. This is the critical path. |
| **Latency** | `p95(POST /submissions)` | < 1000ms | Includes JSONB write + event emit + state transition. |
| **Correctness** | FlowProgress accuracy | 100% | If progress numbers are wrong, the entire dashboard is meaningless. |

### Observability — what to instrument from Day 1

| Layer | What to measure | Why |
|-------|----------------|-----|
| **API** | Request latency histograms by endpoint, error rate by status code | Golden signals |
| **State transitions** | Transition count per state pair, guard rejection count | Detects stuck tasks early |
| **Flow instance creation** | Task count per instance, scope evaluation duration | Detects scope explosions |
| **Event table** | Insert rate, table size growth | Events are append-only — unbounded growth risk |
| **Database** | Connection pool utilization, slow query log (>100ms) | Shared DB = shared risk |

### Early failure modes

| Failure | Likelihood | Impact | Mitigation |
|---------|------------|--------|------------|
| **Event table unbounded growth** | High | DB slowdown over months | Partition by month. Archive policy. Decide retention window early. **Blocking for prod — not blocking for first build.** |
| **FlowProgress computation at scale** | Medium | Slow dashboard | Start computed. Add materialized view trigger when it hurts. **Open, not blocking.** |
| **Scope evaluation timeout** | Low (200 entities) | Flow instance creation fails | Fine for 200. Warning: Scenario 9 may have 10,000+ entities. Index `EntityType + filter attributes`. **Later.** |
| **Shared DB contention** | Medium | Legacy and new modules compete for connections | Separate connection pools per module. Configure in `app/`. **Blocking for first build.** |
| **State transition race conditions** | Low | Data corruption | Optimistic locking on `StateInstance`. **Implement from Day 1.** |

### Operational checklist for first deployment

- [ ] Structured JSON logging from Day 1 (not println)
- [ ] Health check endpoint (`/actuator/health`) covering DB connectivity
- [ ] Connection pool monitoring (HikariCP metrics exposed)
- [ ] Event table partitioning strategy documented
- [ ] Liquibase migration tested forward and backward

---

## 5. Project Shepherd — Smallest Useful Delivery Slice

### Slice 1: "One facility submits one inventory report that a supervisor can approve"

This slice proves the entire vertical — Entity, Capture, State, Event, Flow — with minimal scope.

#### Deliverables (ordered)

| # | Deliverable | Module | Depends on | Estimated effort |
|---|------------|--------|------------|-----------------|
| 1 | Multi-module Maven scaffold + ArchUnit boundary tests | all | — | 1–2 days |
| 2 | Entity + EntityType CRUD (facility, item) | `platform-core` | #1 | 2–3 days |
| 3 | StateDefinition + StateInstance (create, transition with guards) | `platform-core` | #1 | 2–3 days |
| 4 | Event (append + query by subject) | `platform-core` | #1 | 1–2 days |
| 5 | FormTemplate + FormVersion + Submission (create, read) | `platform-capture` | #2 | 3–4 days |
| 6 | FlowDefinition + FlowInstance + FlowTask + transition API | `platform-flow` | #2, #3, #5 | 4–5 days |
| 7 | `GET /flows/my-tasks` (mobile entry point) | `platform-flow` | #6 | 1 day |
| 8 | FlowProgress (computed query) | `platform-flow` | #6 | 1 day |
| 9 | Integration test: full scenario walkthrough | `app` | all | 2–3 days |

**Total estimate: 3–4 weeks for one developer.**

> [!TIP]
> **#1 is the unlock.** Nothing else starts until the Maven scaffold and boundary tests are green. Prioritize this above all.

#### What Slice 1 intentionally excludes
- Automatic FlowInstance creation from schedule (manual trigger is fine for Slice 1)
- Late-task detection (cron job / scheduled task — Slice 2)
- Scope amendments
- Mobile sync contract
- Repeatable form sections (use hardcoded 50-field form for now)
- Legacy bridge

#### Slice 2 (follows Slice 1)
- Schedule-triggered FlowInstance creation
- Late-task detection + `isLate` flagging
- Scope amendment API
- Repeatable form sections in FormTemplate

---

## 6. UX Researcher — User Journey Shape

### Primary actors and their journeys

#### Pharmacist (reporter) — mobile-first

```
Monthly cycle:
  [Notification] → Open app → See "My Tasks" → Tap task → Fill form (50 items)
  → Review summary → Submit → See confirmation with state "submitted"
  
  If rejected:
  → See rejection with comment → Edit specific items → Resubmit
```

**Key UX concerns:**
- **50-item form is the hardest UX problem.** Scrolling through 50 line items is brutal on mobile. Needs: section grouping by category, progress indicator, save-as-you-go (client-side).
- **Offline-first is non-negotiable.** Pharmacists are in low-connectivity areas. The form must work offline and sync when connected.
- **"My Tasks" must be the landing screen.** One tap from app open to the current task. `GET /flows/my-tasks` is the right API shape for this.

#### Supervisor (reviewer) — desktop/tablet

```
During review window:
  Open dashboard → See progress (145/200 submitted) → Filter by "submitted"
  → Open task → Review submission data → Approve or Reject with comment
  
  For monitoring:
  → See completeness heatmap by facility → Identify late facilities
  → See trend (this month vs last 3 months)
```

**Key UX concerns:**
- **Bulk operations matter.** Approving 145 tasks one-by-one is painful. Consider bulk-approve for tasks passing validation rules.
- **Progress dashboard is the killer feature.** The `FlowProgress` API supporting breakdowns by state is exactly what this screen needs.
- **Rejection must require a comment.** The transition guard should enforce `comment required` on `submitted → rejected`.

#### Admin — configuration

```
Setup (once or rarely):
  Define entities (facilities, items) → Define form template → Define flow
  → Assign pharmacists to facilities → Trigger first flow instance
```

**Key UX concern:**
- OD-001 (configuration mechanism) matters here. For Slice 1, API or seed scripts are fine. For real operations, a configuration UI is mandatory.

### Late-task handling

```
Grace period expires →
  System flags tasks as late →
  Supervisor sees late count in dashboard →
  (Future: Notification sent to pharmacist and supervisor)
```

**Observation:** The scenario handles late detection but has no notification mechanism. This aligns with OD-002 (Reaction layer). For Slice 1, the dashboard showing late count is sufficient. Notifications are Slice 2+.

---

## 7. API Tester — Sample Data Shapes & Example Payloads

### Entity creation

```json
// POST /entities
// Create a facility
{
  "uid": "fac_kabale_hc3",
  "code": "KBL-HC3",
  "name": "Kabale Health Centre III",
  "typeCode": "facility",
  "attributes": {
    "has_warehouse": true,
    "district": "Kabale",
    "level": 3
  }
}
// → 201 Created {id: "uuid", uid: "fac_kabale_hc3", ...}

// Create an item
{
  "uid": "item_amox_250",
  "code": "AMOX-250",
  "name": "Amoxicillin 250mg",
  "typeCode": "item",
  "attributes": {
    "category": "antibiotic",
    "unit_of_measure": "tablet"
  }
}
```

### Submission

```json
// POST /submissions
{
  "formVersionCode": "inventory_report_v3",
  "entityRefs": [
    {"role": "target", "entityUid": "fac_kabale_hc3"}
  ],
  "values": {
    "sections": [
      {
        "itemRef": "item_amox_250",
        "opening_balance": 500,
        "quantity_received": 200,
        "quantity_dispensed": 350,
        "closing_balance": 350,
        "stock_out_days": 0
      },
      {
        "itemRef": "item_para_500",
        "opening_balance": 1000,
        "quantity_received": 0,
        "quantity_dispensed": 800,
        "closing_balance": 200,
        "stock_out_days": 3
      }
    ]
  }
}
// → 201 Created {id: "uuid", uid: "sub_...", submittedAt: "2026-03-03T10:15:00Z"}
```

### Flow task query (mobile)

```json
// GET /flows/my-tasks?period=2026-03
// → 200 OK
[
  {
    "taskId": "task_123",
    "flowCode": "monthly_inventory_report",
    "period": "2026-03",
    "targetEntity": {
      "uid": "fac_kabale_hc3",
      "name": "Kabale Health Centre III"
    },
    "formCode": "inventory_report_v3",
    "formVersion": 3,
    "currentState": "pending",
    "dueDate": "2026-03-05",
    "isLate": false,
    "submissionUid": null
  }
]
```

### State transition

```json
// POST /flows/tasks/task_123/transition
// Supervisor approves
{
  "toState": "approved"
}
// → 200 OK {taskId: "task_123", previousState: "submitted", currentState: "approved"}

// Supervisor rejects
{
  "toState": "rejected",
  "comment": "Closing balance for Amoxicillin doesn't match (500+200-350 ≠ 350)"
}
// → 200 OK {taskId: "task_456", previousState: "submitted", currentState: "rejected"}
```

### Flow progress

```json
// GET /flows/monthly_inventory_report/2026-03/progress
// → 200 OK
{
  "flowCode": "monthly_inventory_report",
  "period": "2026-03",
  "total": 200,
  "byState": {
    "pending": 55,
    "submitted": 15,
    "approved": 125,
    "rejected": 5
  },
  "late": 40,
  "completionRate": 0.725
}
```

### Event query

```json
// GET /events?subject=task_123
// → 200 OK
[
  {
    "id": "evt_001",
    "type": "flow_task.submitted",
    "subjectId": "task_123",
    "actorId": "user_pharmacist_1",
    "timestamp": "2026-03-03T10:15:00Z",
    "correlationId": "flow_2026_03",
    "payload": {"submissionUid": "sub_abc"}
  },
  {
    "id": "evt_002",
    "type": "flow_task.approved",
    "subjectId": "task_123",
    "actorId": "user_supervisor_1",
    "timestamp": "2026-03-08T14:30:00Z",
    "correlationId": "flow_2026_03",
    "payload": {}
  }
]
```

### Validation error shape

```json
// POST /submissions (invalid)
// → 400 Bad Request
{
  "error": "VALIDATION_FAILED",
  "message": "Submission validation failed",
  "details": [
    {"field": "sections[0].closing_balance", "rule": "required", "message": "Closing balance is required"},
    {"field": "entityRefs", "rule": "min_count", "message": "At least one entity reference with role 'target' is required"}
  ]
}
```

---

## Summary Matrix

| Expert | Verdict | Blocking items | Key recommendation |
|--------|---------|---------------|-------------------|
| **Software Architect** | ✅ Coherent, freeze confirmed | None | Close OD-007 (already decided). |
| **Backend Architect** | ✅ Contract surface defined | None | Enforce `flow → capture → core` dependency from Day 1. |
| **Reality Checker** | ✅ B+ — solid | None | Define re-assignment and resubmission semantics before Slice 1. |
| **SRE** | ✅ Baseline viable | 2 items for first build: shared DB connection pools, event table partitioning strategy | Structured logging + health checks from Day 1. |
| **Project Shepherd** | ✅ Slice defined | Maven scaffold (#1) unlocks everything | 3–4 weeks for Slice 1 with one developer. |
| **UX Researcher** | ✅ Journey mapped | None | 50-item mobile form UX is the hardest design problem — address early. |
| **API Tester** | ✅ Payloads concrete | None | Use these payloads as integration test fixtures. |

> [!IMPORTANT]
> **No blocking architecture issues found.** The baseline is coherent and the reference scenario validates all six constructs. Move to implementation design.
