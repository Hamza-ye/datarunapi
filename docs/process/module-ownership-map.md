# Module Ownership Map

This document defines ownership, allowed dependencies, and responsibilities for the modular-monolith transition phase.

## Purpose

- Keep boundaries explicit while legacy code is isolated.
- Prevent architecture drift during BC implementation.
- Make ownership and review responsibility unambiguous.

## Module Map

| Module | Primary Owner | Responsibility | Allowed Dependencies | Forbidden Dependencies |
|---|---|---|---|---|
| legacy-core | Legacy Stabilization Team | Transitional legacy behavior; no net-new features | Shared kernel only | Direct access to BC internals |
| policy-bc | Domain Policy Team | Policy rules, permission checks, constraints | Shared kernel, adapter interfaces, org-structure-bc public contracts | Direct calls into legacy-core internals |
| org-structure-bc | Domain Structure Team | Org units, hierarchy queries, grouping contracts | Shared kernel, adapter interfaces | Direct calls into legacy-core internals |
| shared-kernel | Platform Team | Stable cross-cutting primitives and shared abstractions | None | Domain-specific business logic |

## Ownership Rules

- Each module has one accountable owner team.
- Cross-module changes require review from both source and target owners.
- Public contracts are versioned and documented before use.

## Transition Rules

- New features are implemented in BC modules only.
- legacy-core changes are limited to stabilization or adapters.
- Adapters are explicit interfaces; no hidden dependency paths.

## Escalation

- Any proposed forbidden dependency requires architecture review and ADR update.

---

*Date: 2026-03-21*  
*Maintainers: Domain Architecture Team*