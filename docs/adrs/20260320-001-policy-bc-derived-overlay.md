# ADR-001: Policy BC as Derived Overlay with Precedence Model

## Status
Accepted

## Context
The current system relies on implicit access rules scattered across team memberships, assignments, and org units. The future vision requires a dedicated Policy BC to own explicit governance rules, constraints, and overlays for domain-specific access control, building on top of IAM's identity foundation.

The problem includes entangled access control logic with business entities, no clear separation of identity from policy, and difficulty adding dynamic permissions without side effects. Legacy patterns like team.form_permissions create brittle, implicit policies.

## Decision
We will implement the Policy BC as a derived overlay on IAM, owning explicit policy rules, constraints, and governance. It consumes IAM events and provides APIs for policy evaluation.

Key aspects:
- **Role**: Derived; interprets Planning, Team, and Org intent, but does not own source data.
- **Conflict Resolution**: Deterministic; explicit precedence model (e.g., most-specific-wins).
- **Migration Strategy**: Isolated adapters allowed; no direct legacy inference in new model.

## Consequences
- **Positive**: Decouples policy from identity and business logic, enables flexible governance, supports overlays for complex access scenarios.
- **Negative**: Migration challenges from implicit legacy policies, potential performance impacts for real-time evaluation.
- **Risks**: Need to handle policy conflicts or hierarchies, integration with external policy engines.

## Implementation Paths
- policy evaluation API contract (to be defined in the application service layer)
- policy rule precedence enforcement (most-specific-wins)
- migration adapters for read-only legacy policy ingestion

## Related
- RFC: Policy & Access Control Bounded Context ([../rfcs/rfc-policy-bc.md](../rfcs/rfc-policy-bc.md))
- RFC: IAM Bounded Context ([../rfcs/rfc-iam-bc.md](../rfcs/rfc-iam-bc.md))

---

*Date: 2026-03-20*  
*Deciders: Domain Architecture Team*  
*Consulted: Development Team, Product Owners*