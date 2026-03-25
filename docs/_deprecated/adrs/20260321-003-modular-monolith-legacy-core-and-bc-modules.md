# ADR-003: Modular Monolith with Legacy-Core Module and New BC Modules

## Status
Accepted

## Context
The team is transitioning from a legacy monolith to bounded contexts. BC boundaries are not yet fully validated in production, and the team wants to avoid over-investing in migration and distributed-system complexity at this stage.

A direction is needed now to structure implementation safely while preserving delivery speed.

## Decision
Adopt a modular monolith architecture in the current application with:
- one isolated `legacy-core` module for existing code,
- new BC modules implemented beside it,
- explicit adapters between BC modules and `legacy-core`,
- monorepo and single deployable runtime for this phase.

Key directives:
- New features are implemented in BC modules only.
- `legacy-core` is treated as transitional and stabilized, not expanded.
- Cross-module communication must use module contracts (APIs, events, adapters), not direct internal coupling.
- Microservice extraction is deferred until BC contracts and operational needs are proven.

## Consequences
- **Positive**: Lower transition risk, faster iteration, cleaner BC boundary discovery, simpler operations during early architecture evolution.
- **Negative**: Legacy code remains present longer; strict module governance is required to avoid boundary erosion.
- **Risks**: Team may drift back into legacy coupling if enforcement is weak.

## Implementation Paths
- Define module boundaries in package structure and ownership mapping.
- Introduce adapter interfaces between BC modules and `legacy-core`.
- Route selected BC flows end-to-end through new modules.
- Add tests that fail on prohibited cross-module direct access patterns.

## Related
- Addendum: [20260321-rfc-addendum-legacy-core-isolation-plan.md](20260321-rfc-addendum-legacy-core-isolation-plan.md)
- RFC: Policy BC ([../rfcs/rfc-policy-bc.md](../rfcs/rfc-policy-bc.md))
- RFC: Org Structure BC ([../rfcs/rfc-org-structure-bc.md](../rfcs/rfc-org-structure-bc.md))

---

*Date: 2026-03-21*  
*Deciders: Domain Architecture Team*  
*Consulted: Development Team, Product Owners*