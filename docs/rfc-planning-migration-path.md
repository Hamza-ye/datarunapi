# RFC: Planning BC Migration Path (from current Assignment/Team/Activity model)

## Purpose
This document describes a **practical incremental migration path** from the current `assignment/activity/team` model to the proposed **Planning & Execution Orchestration** bounded context described in `rfc-campaign-planning-bc.md`.

It is designed to:
1. Avoid ripping the system apart (low risk)
2. Preserve existing behavior during migration
3. Allow the new Planning BC to be built and validated incrementally
4. Enable eventual deprecation of the old "linking" model once the new model is in place

---

## 1. Current model (what we have today)

### Key tables / concepts
- `assignment` (id, uid, team_id, activity_id, org_unit_id, forms, created_date)
- `activity` (groups assignments)
- `team` / `team_user` / `team_managed_teams`

### How it's used today
- Used for **access control** (who can view/submit which forms)
- Used for **mobile sync** (the mobile client downloads config based on team/assignment scope)
- Used as **implicit planning** (assignments are treated as “what should happen”)

The model is essentially a set of join tables that are weaponized for both access control and execution, which creates hidden coupling and makes the system brittle.

---

## 2. Target model (Planning BC artifacts)
See `docs/rfc-campaign-planning-bc.md` for full domain model. Key artifacts:
- `Plan` (campaign, program)
- `WorkPackage` / `Phase`
- `WorkItem` / `Task` / `Activity` (planned work)
- `Allocation` / `Commitment` (who is expected to do it)
- `Scope` / `Target` (where it applies)
- `SuccessCriteria` / `Outcome` (what success looks like)
- `Constraint` / `Policy` (rules)
- `ProgressSnapshot` / `PlanReport` (read model)

---

## 3. Mapping (old → new)

### 3.1 `assignment` → `WorkItem` + `Allocation`
- `assignment.uid` → `workItemUid` (optional, or use new UUID)
- `assignment.team_id` → `Allocation.assigneeType=TEAM + assigneeUid=teamUid`
- `assignment.activity_id` → `WorkItem.workPackageUid` (or `WorkItem.type` / `WorkPackage` linkage)
- `assignment.org_unit_id` → `Scope` (or `WorkItem.scopeUid`)
- `assignment.forms` → link between `WorkItem` and `Template` (naming this `workItem.templateUid` or `workItem.formUid`)

> Note: If `assignment` currently holds the only notion of “what to submit”, the new model makes that explicit as a `WorkItem`.

### 3.2 `activity` → `WorkPackage` / `Phase`
- Keep the concept of “grouping” but make it explicit as a planning artifact with lifecycle and status.
- If `activity` is lightweight, it can be renamed to `WorkPackage` and extended with dates, state, dependencies.

### 3.3 `team` → `Assignee` (via Allocation) + Team BC ownership
- Keep `team` as a team concept in the Team/Collaboration BC; do not use it as a planning artifact.
- Planning model references teams only as allocation targets.

### 3.4 `UserExecutionContext` (access cache) → derived projection / policy input
- Continue to use `user_execution_context` as a read-model to support fast filtering.
- Populate it from both:
  - Planning intent (allocations/plan scope)
  - Execution evidence (submissions, progress)
- Eventually, move to a policy layer (RBAC/OPA) that uses planning intent to authorize.

---

## 4. Migration approach (incremental)

### Phase 0: Stabilize the current model (baseline)
- Ensure existing access filtering and mobile sync continue working.
- Add instrumentation to understand how often `assignment/activity/team` actually change.

### Phase 1: Introduce Planning BC read model (no behavior change)
1. Create new schema/tables for Planning BC artifacts (`plan`, `work_package`, `work_item`, `allocation`, etc.).
2. Build a thin service/module that can create these artifacts (API only).
3. Populate the new model for a small set of plans (manual or via migration script).
4. Emit planning intent events from the new service (e.g., `WorkItemCreated`).

### Phase 2: Start writing planning intent (collaboration + reporting)
1. Build a simple reporting projection that consumes planning events and produces progress snapshots.
2. Validate that the planning model can represent real scenarios (use real feature requests / cases).
3. Use this to build dashboards (QA / product validation) — no execution dependency.

### Phase 3: Reference Planning from Execution (soft link)
1. Allow `DataSubmission` to optionally include `workItemUid`.
2. When submissions arrive, emit an event `SubmissionCreated` containing `workItemUid` (if present).
3. Have the planning/reporting projection consume those events to update progress.
4. Keep existing access filtering (assignment/team) untouched during this phase.

### Phase 4: Replace access filtering with planning-derived policy
1. Implement a policy evaluation layer (RBAC/OPA) that answers: “Can user U act on entity E?” based on:
   - Planning allocations (intent)
   - Current plan/workitem status
   - User identity/role
2. Use `UserExecutionContext` as a cache for policy decisions or as a read-model.
3. Gradually switch filtering from hard-coded filters over `assignment` to policy checks.

### Phase 5: Deprecate old model
1. Once planning intent + policy fully cover existing behavior, stop writing to `assignment/activity/team` for new work.
2. Leave the DB as read-only for historical/legacy access, or migrate data into Planning BC and remove redundancy.

---

## 5. Notes on naming and abstraction
- **“Assignment”** is a poor name if it’s just a join table. In the new model it should be called **`Allocation`** or **`Commitment`** and should have a lifecycle.
- **“Activity”** should be a real planning concept (work package, phase, etc.) with a status and timeline.
- Avoid reusing old names for new semantics — choose vocabulary that reflects intent.

---

## 6. Risks & mitigation
- **Risk:** Planning model becomes another ad-hoc model that doesn’t reflect reality.
  - **Mitigation:** Validate with real planning stories and product owners before building (see next steps).

- **Risk:** Execution teams resist adding `workItemUid` to submissions.
  - **Mitigation:** Make it optional, and allow plan linking post-hoc.

- **Risk:** Dual writes (old model + new model) become a maintenance burden.
  - **Mitigation:** Treat the old model as read-only after Phase 4; use migration scripts to backfill.

---

## 7. Next actions (suggested)
1. Pick 1–2 real planning scenarios (e.g., “weekly field survey campaign” + “ad-hoc emergency response”) and write them down as user stories.
2. Validate that the proposed planning artifacts cover those stories.
3. Implement the minimal Planning BC schema + events for one story.
4. Build a small report that shows plan progress (proof of concept).

---

## 8. Where this doc lives
This document is intentionally a companion to `docs/rfc-campaign-planning-bc.md` and is meant to be updated as the migration evolves.
