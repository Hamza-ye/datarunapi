# Reference Scenario: Monthly Inventory Reporting

> **Status:** Frozen — modeled against [Architecture Baseline v1](baseline-v1.md)
> **Purpose:** End-to-end proof that the baseline constructs can model a real operational flow without introducing new architecture.

---

## 1. Scenario Summary

200 health facilities in a district submit monthly stock reports. Each facility has a pharmacist responsible for reporting. On the 5th of every month, a stock report form covering ~50 item types must be submitted. A district supervisor reviews and approves each submission. Late submissions (after a 10-day grace period) are flagged. Results feed into LMIS / supply chain decisions.

---

## 2. Construct Mapping

| Construct | In this scenario |
|---|---|
| **Entity** | Facility (hierarchical, attributes: has_warehouse, district, level). Item (flat, attributes: category, unit_of_measure). User (flat). |
| **Capture** | FormTemplate `inventory_report_v3` with fields: item_reference, opening_balance, quantity_received, quantity_dispensed, closing_balance, stock_out_days. Submission = one filled form per facility per month. |
| **Entity Reference** | Submission → target facility. Form field `item_reference` → item entity. FlowTask → facility. FlowTask → assigned pharmacist. |
| **State** | FlowTask lifecycle: `pending → submitted → approved/rejected`. Rejected can return to `submitted` after revision. |
| **Event** | `flow_instance.created`, `flow_task.submitted`, `flow_task.approved`, `flow_task.rejected`, `flow_task.late`. |
| **Flow** | FlowDefinition `monthly_inventory_report`. FlowInstance = one per month. FlowTask = one per facility. FlowProgress = aggregate completeness. |

---

## 3. Flow Definition

```
FlowDefinition: monthly_inventory_report
├── capture: inventory_report_v3
├── lifecycle (StateDefinition):
│   ├── states: [pending, submitted, approved, rejected]
│   ├── initial: pending
│   ├── terminal: [approved, rejected]
│   └── transitions:
│       ├── pending → submitted    (guard: submission complete)
│       ├── submitted → approved   (guard: role = district_supervisor)
│       ├── submitted → rejected   (guard: role = district_supervisor)
│       └── rejected → submitted   (guard: submission revised)
├── schedule: RECURRING, MONTHLY, day 5, grace period 10 days
├── scope: entityType = facility, filter = has_warehouse = true
└── actors:
    ├── reporter: PER_SCOPE_ENTITY (one pharmacist per facility)
    └── reviewer: PER_FLOW (one district supervisor)
```

---

## 4. Runtime Walkthrough

### Phase 1: Flow instance creation

```
March 1, 2026:
  Platform evaluates schedule → creates FlowInstance "monthly_inventory_report/2026-03"
  Platform evaluates scope → snapshot: 200 facilities with has_warehouse = true
  Platform creates 200 FlowTasks:
    - task[i].targetEntity = facility[i]
    - task[i].assignedActors = {reporter: facility[i].pharmacist}
    - task[i].state = "pending"
    - task[i].dueDate = 2026-03-05
  Event: {type: "flow_instance.created", subject: "flow/monthly_inventory_report/2026-03"}
```

### Phase 2: Mobile submission

```
March 3:
  Pharmacist opens mobile app
  App calls: GET /flows/my-tasks?period=2026-03
  Returns: [{task: task_123, form: inventory_report_v3, facility: "Kabale HC III", due: "2026-03-05"}]

  Pharmacist fills form: 50 line items (one per tracked item)
  App calls: POST /submissions
    body: {formVersion: v3, entityRefs: [{role: "target", uid: "facility_kabale"}], values: {...}}
  
  FlowTask.submission = new submission
  FlowTask.state: pending → submitted
  Event: {type: "flow_task.submitted", subject: "task_123", actor: "user_pharmacist_1"}
```

### Phase 3: Supervisor review

```
March 8:
  Supervisor opens dashboard
  Calls: GET /flows/monthly_inventory_report/2026-03/tasks?state=submitted
  Returns: 145 tasks ready for review

  Reviews Kabale HC III submission
  Calls: POST /flows/tasks/task_123/transition {toState: "approved"}
  FlowTask.state: submitted → approved
  Event: {type: "flow_task.approved", subject: "task_123", actor: "user_supervisor_1"}

  Rejection case:
  Calls: POST /flows/tasks/task_456/transition {toState: "rejected", comment: "Closing balance does not match"}
  FlowTask.state: submitted → rejected
  Event: {type: "flow_task.rejected", subject: "task_456", actor: "user_supervisor_1"}

  Pharmacist revises and resubmits → FlowTask.state: rejected → submitted
```

### Phase 4: Late handling

```
March 15 (grace period expires):
  Platform evaluates: FlowTasks still in "pending" state
  40 tasks → marked as late (isLate = true)
  Event: {type: "flow_task.late", subject: "task_*"} for each late task

  (Future: Reaction could auto-notify supervisor or pharmacist)
```

### Phase 5: Progress tracking

```
Any time during the cycle:
  GET /flows/monthly_inventory_report/2026-03/progress
  Returns:
    {
      total: 200,
      submitted: 145,
      approved: 130,
      rejected: 5,
      pending: 55,
      late: 40,
      completionRate: 0.725
    }
```

### Phase 6: Cycle close

```
April 1:
  New FlowInstance "2026-04" created
  FlowInstance "2026-03" state → "closed"
  Unresolved tasks remain in their final state (auditable)
```

---

## 5. API / Contract Surface

```
Core:
  POST   /entities                         Create entity
  GET    /entities/:uid                    Read entity
  PATCH  /entities/:uid                    Update entity attributes
  POST   /entities/:uid/transition         State transition on entity

  POST   /submissions                      Create submission (with or without flow)
  GET    /submissions/:uid                 Read submission

Flow:
  GET    /flows                            List flow definitions
  GET    /flows/:code/:period              Get flow instance
  GET    /flows/:code/:period/progress     Aggregate progress
  GET    /flows/:code/:period/tasks        List tasks (filter by state, entity, actor)
  POST   /flows/tasks/:id/transition       State transition on task
  GET    /flows/my-tasks                   Tasks assigned to current user (mobile sync)

Events:
  GET    /events?subject=:uid              Event history for any subject
```

---

## 6. What Fits Cleanly

- **All six constructs are used.** None is idle or forced. Entity, Capture, Entity Reference, State, Event, and Flow each serve a distinct purpose.
- **State as a universal primitive works.** The same StateDefinition/StateInstance mechanism serves FlowTask lifecycle and could serve entity lifecycle. One construct, two applications.
- **FlowProgress answers the hardest question.** "What's the completeness rate for district X in March?" is a first-class query, not a join across four tables.
- **Mobile sync is clean.** `GET /flows/my-tasks` returns everything a pharmacist needs: task, form, target entity, due date, current state.
- **Approval needs no special construct.** It's a state transition (submitted → approved) with a role guard (district_supervisor). The State primitive handles it.
- **Entity Reference links everything without coupling.** Submission → facility, form field → item, FlowTask → pharmacist — all through the same mechanism.

---

## 7. What Still Feels Awkward

**One minor friction point, not a blocker:**

**Inventory items per submission.** The pharmacist submits one form covering ~50 items. In the current model, that's either:
- (a) One submission with 50 repeated sections (one section per item, each with a `item_reference` field), or
- (b) 50 separate submissions (one per item, each linked to the same facility and period)

Option (a) is cleaner for mobile (one form fill = one submission). It requires the FormTemplate to support **repeatable sections** — a form section that repeats once per referenced entity. This is a FormTemplate design detail, not an architecture change. The baseline handles it through form field configuration (`FieldType.ENTITY_REFERENCE` in a repeatable section).

**This is a capture template design question, not an architecture gap.** It does not require new constructs.
