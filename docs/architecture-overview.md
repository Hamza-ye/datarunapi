# Architecture Overview

## Overview
This document outlines the proposed Bounded Contexts (BCs) for refactoring the data collection platform into a modular, maintainable architecture. Based on DDD principles, it separates concerns into 9 BCs, enabling flexibility, scalability, and incremental evolution toward the future vision of core tools + domain overlays.

## Key Principles
- **Modularity**: Each BC is independently deployable, with clear boundaries.
- **Event-Driven Coupling**: Loose coupling via events/APIs; no shared databases.
- **Meta-Based Flexibility**: JSON configs for dynamic rules and relationships.
- **Scalability**: Supports microservices evolution; CQRS for read/write separation.

---

## Target Architecture (full vision)

The full target architecture comprises 9 Bounded Contexts. Not all of these are being built now — see [Phase 1 Scope](#phase-1-scope) below for what is currently active.

### Proposed Bounded Contexts
1. **IAM BC**: User identity and access.
2. **Org Structure BC**: Hierarchical governance.
3. **Team & Collaboration BC**: Team management.
4. **Form Template Design BC**: Schema definitions.
5. **Campaign & Assignment Planning BC**: Planning logic.
6. **Data Collection & Submission BC**: Runtime execution.
7. **Reference Data & Options BC**: Canonical data.
8. **Analytics & Reporting BC**: Pivots and reports.
9. **Domain Entity Registry BC**: Dynamic entity management.

### Benefits
- Addresses current difficulties (e.g., scattered logic, tight coupling).
- Enables dynamic entities as subjects in data.
- Scales with future requirements via overlays.

---

## Phase 1 Scope

Phase 1 focuses on building the minimal core that unblocks the future vision. The goal is to validate BC boundaries with real implementation slices before expanding.

### Active BCs (Phase 1)
- **Org Structure BC** — isolate hierarchy ownership, publish read-only APIs (RFC accepted, [ADR-002](adrs/20260320-002-org-structure-bc-centralized-api.md), [initial contract](process/org-structure-bc-initial-contract.md))
- **Policy BC** — derive access rules from planning/team/org intent (RFC accepted, [ADR-001](adrs/20260320-001-policy-bc-derived-overlay.md))

### Structural decision
- **Modular monolith** with `legacy-core` isolation and new BC modules beside it ([ADR-003](adrs/20260321-003-modular-monolith-legacy-core-and-bc-modules.md), [isolation addendum](rfcs/rfc-addendum-legacy-core-isolation-plan.md))

### Deferred BCs
The following BCs have placeholder RFCs and will be elaborated once core BCs are validated:
- IAM, Team & Collaboration, Form Template Design, Data Collection, Reference Data, Analytics, Domain Entity Registry

---

## Related Docs
- [Current System Overview](current-system-overview.md) — implementation-level current state
- [ADR Index](adrs/README.md) — accepted architectural decisions
- [Docs Structure & Workflow](process/docs-structure.md) — how to navigate and update these docs
- [RFCs](rfcs/) — all proposals and evolution plans