# Legacy Boundary Strategy

> **Status:** Decision record — 2026-03-25
> **Context:** The baseline architecture (v1) defines 6 new first-class constructs. The legacy codebase has 15+ packages with domain-specific logic tangled into one Spring Boot module. This document decides how legacy and new code coexist.

---

## The Problem

The legacy code (`org.nmcpye.datarun`) has:

```
acl/         analytics/    aop/          assignment/    config/
datacapture/ iam/          jooq/         jpa/           management/
outbox/      party/        sharedkernal/ template/      web/
```

Key entanglement points:
- `jpa/orgunit` — org unit hierarchy (will become Entity + EntityType)
- `jpa/option` — reference data (will become Entity)
- `jpa/accessfilter` — access control (cross-cutting, entangled with everything)
- `template/` — form templates (will become Capture)
- `datacapture/` — submissions (will become Capture)
- `assignment/` — activity/project assignments (will become Flow pattern)
- `party/` — party model (will become Entity)
- `sharedkernal/` — UID generation, enums, common utilities

**The risk:** If new code imports from legacy, legacy shapes leak into the new constructs. Every `import org.nmcpye.datarun.jpa.orgunit.*` in new code is a dependency arrow pointing the wrong way.

---

## Decision: Build in Parallel, Hard Boundary

### The strategy: Side-by-side modules, one-way bridge

```
datarunapi/                          (root — parent pom)
├── legacy/                          (renamed from current src/)
│   └── src/main/java/org/nmcpye/datarun/...
│   └── pom.xml                      (existing dependencies)
│
├── platform-core/                   (NEW — Entity, State, Event, EntityRef)
│   └── src/main/java/org/nmcpye/platform/core/...
│   └── pom.xml                      (minimal: spring-data, postgres)
│
├── platform-capture/                (NEW — FormTemplate, Submission)
│   └── src/main/java/org/nmcpye/platform/capture/...
│   └── pom.xml                      (depends on: platform-core)
│
├── platform-flow/                   (NEW — FlowDefinition, FlowInstance, FlowTask)
│   └── src/main/java/org/nmcpye/platform/flow/...
│   └── pom.xml                      (depends on: platform-core, platform-capture)
│
├── app/                             (Application assembly — boots everything)
│   └── src/main/java/org/nmcpye/datarun/DataRunApiApp.java
│   └── pom.xml                      (depends on: legacy, platform-*)
│
└── pom.xml                          (parent — manages versions, shared config)
```

### Rules

| Rule | Enforcement |
|---|---|
| **New modules MUST NOT import from `legacy/`** | ArchUnit test: `noClasses().that().resideInAPackage("org.nmcpye.platform..").should().dependOnClassesThat().resideInAPackage("org.nmcpye.datarun..")` |
| **Legacy CAN import from new modules** | One-way bridge. Legacy can start using new Entity/Capture constructs when ready. |
| **Shared infrastructure goes in `platform-core`** | UID generation, base entity classes, common annotations |
| **Legacy continues to compile and run** | No breaking changes to legacy. It just moves to its own module. |
| **Database is shared** | Both legacy and new modules use the same PostgreSQL database. New tables have a `platform_` prefix. Legacy tables stay as they are. |
| **Liquibase changelogs are split** | Legacy keeps its changelogs. New modules add theirs under `platform/changelog/`. The `app` module includes both. |

### Why this approach

| Alternative | Why not |
|---|---|
| **Edit legacy in place** | The old abstractions will shape every decision. You'll keep bending new constructs to fit legacy table structures. This is the mud you want to avoid. |
| **Full rewrite, delete legacy** | Legacy is deployed, serving users. You can't stop it cold. Side-by-side lets you migrate incrementally when you're ready — not now. |
| **Feature flags / same module** | Package boundaries within one module are not enforceable. A single `import` statement and the boundary is gone. Maven module boundaries are real walls. |
| **Separate repository** | Over-isolation. You lose shared database access, shared Spring context, and the ability to bridge later. One repo, multiple modules is the right balance. |

---

## What Changes NOW

### Step 1: Convert to multi-module Maven project

Move all existing source to a `legacy/` module. Create empty `platform-core/`, `platform-capture/`, `platform-flow/` modules. Wire them in a parent pom. The app still boots from `app/` and includes all modules.

**Legacy behavior is 100% preserved.** Nothing changes for users. The app runs exactly as before.

### Step 2: Set up ArchUnit boundary tests

Add tests that enforce the one-way dependency rule. If new code ever imports from legacy, the build fails.

### Step 3: Start building new constructs in `platform-core`

First construct: `Entity` + `EntityType` + `StateDefinition` + `StateInstance` + `Event`. Clean. No legacy references.

### Step 4: Bridge later, only when needed

When the new Entity model is proven, create a thin bridge (in `app/` or a `bridge/` module) that maps legacy org units to new Entity format. This bridge is:
- Optional
- Not in `platform-core`
- Something you write when you're ready, not something that shapes the architecture

---

## Database Boundary

```
Legacy tables:          New tables:
  jhi_user                platform_entity
  jhi_authority            platform_entity_type
  drun_org_unit            platform_form_template
  drun_option              platform_form_version
  drun_option_set          platform_submission
  drun_data_template       platform_state_definition
  drun_data_element        platform_state_instance
  drun_assignment          platform_event
  drun_activity            platform_flow_definition
  drun_project             platform_flow_instance
  ...                      platform_flow_task
```

Both live in the same database. New tables use `platform_` prefix. No foreign keys between legacy and new tables. If a bridge needs to map legacy org units to new entities, it reads from both and reconciles — it doesn't create cross-table foreign keys.

---

## What You Should Stop Doing

1. **Stop editing legacy domain classes** for new requirements. If it works, leave it.
2. **Stop trying to make legacy classes fit the new architecture.** They won't. That's fine.
3. **Stop worrying about migration right now.** Migration is a future bridge concern, not a current architecture concern.

## What You Should Start Doing

1. **Build new constructs as new code** in `platform-core`, `platform-capture`, `platform-flow`.
2. **Write tests for the new modules** independently. They should compile and test without legacy.
3. **Use the baseline** to guide what to build — Entity, Capture, State, Event, EntityReference, Flow.
