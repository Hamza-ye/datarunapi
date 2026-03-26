# Implementation Playbook — From Frozen Baseline to Running Code

> **Created:** 2026-03-25
> **Prerequisite:** [parallel-review-signoff.md](parallel-review-signoff.md) accepted. Baseline v1 frozen. OD-007 closed.

---

## How to use this playbook

Each step below is **one focused session** (one conversation or working block). They are ordered — do not skip ahead. Each step produces a concrete deliverable that the next step depends on.

The key discipline: **each step has a done-criteria. Do not move on until it's met.**

---

## Step 0 — Resolve micro-decisions before writing code

**Goal:** Close the 3 open questions the Reality Checker flagged that touch Slice 1 implementation.

**What to decide:**

| # | Question | Recommended answer | Decide now / later |
|---|----------|-------------------|-------------------|
| 1 | **Resubmission after rejection** — is the old submission replaced or versioned? | New immutable submission replaces the FlowTask reference. Old submission stays in the DB (audit trail). | **Now** — affects Submission + FlowTask contract. |
| 2 | **Draft handling** — does the server know about drafts? | No. Drafts are client-side only. `POST /submissions` means "complete." | **Now** — affects state machine design. |
| 3 | **Re-assignment** — can a FlowTask's assigned actor change mid-cycle? | Yes, via admin action. `PATCH /flows/tasks/:id {assignedActors}` + Event. | **Now** — affects FlowTask API surface. |

**Deliverable:** Add these 3 decisions as a short addendum to [baseline-v1.md](../02-baseline/baseline-v1.md) or as closed items in the open decisions log.

**Done-criteria:** All 3 questions have a written answer in docs. No ambiguity for the developer.

---

## Step 1 — Maven scaffold + boundary enforcement ✅

**Goal:** Create the multi-module project structure defined in [legacy-boundary-strategy.md](../02-baseline/legacy-boundary-strategy.md).

**Deliverable:** Working multi-module Maven build. Green ArchUnit tests. Legacy behavior unchanged.

**Done-criteria:** `mvn clean verify` passes. ArchUnit test exists and would fail if a forbidden import is added.

**Walkthrough:** [001-maven-scaffold.md](walkthroughs/001-maven-scaffold.md)

---

## Step 2 — Core module: Entity + EntityType + Event

**Goal:** Implement the identity and audit backbone.

**Request:**
> In `platform-core`, implement Entity, EntityType, EntityReference, and Event. Use the schema from the parallel review's Backend Architect section. Include: JPA entities, Liquibase migrations (`platform_` prefix), Spring Data repositories, REST endpoints (`POST/GET/PATCH /entities`, `GET /events`). Use the API Tester payloads from the parallel review as test fixtures. Add structured JSON logging (SRE requirement).

**Deliverable:** Working CRUD for entities. Events append on every mutation. Integration tests using the facility/item payloads from the review.

**Done-criteria:** Can create a facility entity, create an item entity, query both, and see Events recorded. All via REST.

---

## Step 3 — Core module: State machine

**Goal:** Implement the universal state primitive.

**Request:**
> In `platform-core`, implement StateDefinition and StateInstance. StateDefinition stores states, transitions, guards (role-based, condition-based). StateInstance tracks current state for any subject (entity or flow task). Implement `POST /entities/:uid/transition` for entity state changes. Include optimistic locking on StateInstance (SRE requirement). Guard evaluation must emit Events on success and on guard rejection.

**Deliverable:** Working state machine attached to entities. Transition with guards. Events on every transition.

**Done-criteria:** Can define a state machine, attach it to an entity, transition through states, get rejected by a guard, and see all Events.

---

## Step 4 — Capture module: FormTemplate + Submission

**Goal:** Implement the data capture backbone.

**Request:**
> In `platform-capture`, implement FormTemplate, FormVersion, and Submission. FormVersion stores field definitions as JSONB. Submission stores values as JSONB with entity references. Implement `POST /submissions` and `GET /submissions/:uid`. Submission creation must emit an Event. Use the inventory report payload from the parallel review as the test fixture. EntityReference records are created for each `entityRefs` entry in the submission.

**Deliverable:** Can create a form template, create a versioned form, submit against it, and retrieve the submission.

**Done-criteria:** `POST /submissions` with the inventory report payload succeeds, creates EntityReference records, and emits an Event.

---

## Step 5 — Flow module: FlowDefinition + FlowInstance + FlowTask

**Goal:** Implement the process orchestration layer.

**Request:**
> In `platform-flow`, implement FlowDefinition, FlowInstance, FlowTask, and FlowProgress. FlowDefinition references a FormTemplate and a StateDefinition. Implement manual FlowInstance creation (no scheduler yet) that evaluates scope (entity query by type + attribute filter) and creates FlowTasks. Implement `POST /flows/tasks/:id/transition` using the State module. Implement `GET /flows/my-tasks`, `GET /flows/:code/:period/tasks`, `GET /flows/:code/:period/progress`. FlowProgress is computed on read (not materialized). On resubmission after rejection: new Submission replaces FlowTask.submissionId reference; old Submission is preserved.

**Deliverable:** Can create a flow instance, see generated tasks, submit against a task, transition task state, query progress.

**Done-criteria:** The full runtime walkthrough from the reference scenario (Phases 1–5) works end-to-end via REST calls.

---

## Step 6 — Integration test: full scenario walkthrough

**Goal:** Prove the entire vertical works as one system.

**Request:**
> In the `app` module, write an integration test that replays the Monthly Inventory Reference Scenario end-to-end: create entities (3 facilities, 5 items) → create form template → create flow definition → create flow instance (scope evaluates to 3 facilities) → submit for 2 facilities → approve 1, reject 1 → resubmit rejected → query progress → verify all Events. Use the exact payloads from the parallel review. Assert response shapes match the API Tester examples. Add the SRE operational checklist: health check endpoint, connection pool metrics exposed via Actuator.

**Deliverable:** Green integration test. Health endpoint works. Actuator metrics exposed.

**Done-criteria:** `mvn verify` runs the full scenario test and passes. `/actuator/health` returns UP.

---

## Step 7 — Slice 1 signoff

**Goal:** Confirm readiness before moving to Slice 2.

**Request:**
> Run the parallel review panel again (same 7 perspectives) against the actual running code. Focus on: Does the implementation match the contract surface? Are the SRE operational requirements met? Does the integration test cover the reference scenario completely? Flag anything that drifted from the baseline.

**Deliverable:** Signoff report or punch list for fixes.

---

## Anti-drift rules (enforce throughout)

| Rule | Why |
|------|-----|
| **No code before Step 1 is green** | Module boundaries prevent wrong-direction imports. |
| **Every module compiles independently** | `cd platform-core && mvn compile` must work without `platform-flow` on classpath. |
| **Every REST endpoint has a test using the review's payloads** | Payloads are the contract. If they drift, the contract drifts. |
| **Every mutation emits an Event** | If you skip events, traceability breaks silently. |
| **No new constructs or tables outside the review's schema** | Adding things is easy. The discipline is not adding things. |
| **Dependency direction enforced by ArchUnit** | `flow → capture → core` only. Build fails if violated. |

---

## What is NOT in this playbook (intentionally)

| Excluded | When |
|----------|------|
| Schedule-triggered FlowInstance creation | Slice 2 |
| Late-task detection + `isLate` flagging | Slice 2 |
| Scope amendment API | Slice 2 |
| Repeatable form sections | Slice 2 |
| Legacy bridge | After Slice 2 — when proven |
| Mobile sync contract | OD-008 — when mobile implementation starts |
| Configuration UI | OD-001 — after first flow is proven end-to-end |
| Reaction layer | OD-002 — after 3+ event patterns emerge |
