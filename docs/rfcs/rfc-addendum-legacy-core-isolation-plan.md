# RFC Addendum: Legacy Core Isolation Plan

## Status
Proposed - Exploration Addendum

## Context
The current application contains mixed responsibilities and implicit coupling across domains. The target architecture is bounded-context oriented, but immediate large-scale migration planning is intentionally deferred.

## Addendum Goal
Define a practical near-term isolation strategy that allows new BC modules to evolve safely while legacy behavior remains available.

## Scope
- Organize current messy code into a single `legacy-core` module boundary.
- Build new BC modules beside `legacy-core` in the same application.
- Use explicit adapters between new BC modules and `legacy-core`.

## Non-Goals (for now)
- No full cutover migration runbook.
- No one-shot migration schedule.
- No final downtime/freeze choreography.

## Proposed Isolation Rules
- New features must be implemented in new BC modules, not in `legacy-core`.
- `legacy-core` is read-through or adapter-backed for transitional use.
- Direct cross-BC data access is not allowed; use module contracts.
- Keep changes to `legacy-core` minimal and stabilization-focused.

## Initial Module Shape
- `legacy-core` (contains current code under isolation boundary)
- `policy-bc`
- `org-structure-bc`
- Additional BC modules as defined by RFCs

## Acceptance Signals
- At least two BC flows execute end-to-end without direct legacy coupling.
- Adapters are explicit and test-covered.
- `legacy-core` receives no net-new feature growth.

## Related
- [20260321-003-modular-monolith-legacy-core-and-bc-modules.md](20260321-003-modular-monolith-legacy-core-and-bc-modules.md)
- [../rfcs/rfc-policy-bc.md](../rfcs/rfc-policy-bc.md)
- [../rfcs/rfc-org-structure-bc.md](../rfcs/rfc-org-structure-bc.md)

---

*Date: 2026-03-21*  
*Authors: Domain Architecture Team*  
*Reviewers: Development Team*