# RFC: Policy & Access Control Bounded Context

## Status
Accepted - Decision Captured

## Related ADRs
- [../adrs/20260320-001-policy-bc-derived-overlay.md](../adrs/20260320-001-policy-bc-derived-overlay.md)

## Context
The current system relies on implicit access rules scattered across team memberships, assignments, and org units. The future vision requires a dedicated Policy BC to own explicit governance rules, constraints, and overlays for domain-specific access control, building on top of IAM's identity foundation.

## Problem
- Access control logic is entangled with business entities (e.g., teams, assignments).
- No clear separation of identity (IAM) from policy (what you can do).
- Hard to add dynamic permissions, constraints, or multi-party workflows without side effects.
- Legacy patterns like team.form_permissions create brittle, implicit policies.

## Proposal
Introduce a Policy BC as an overlay on IAM, owning explicit policy rules, constraints, and governance. It consumes IAM events and provides APIs for policy evaluation. Dependencies: inbound from IAM, outbound to other BCs for policy enforcement.

### Core Responsibilities
- Manage explicit access policies, constraints, and governance rules.
- Evaluate permissions based on identity, context, and rules.
- Support dynamic overlays for domain-specific access (e.g., campaign constraints).

### Key Entities/Models
- `policy` (id, uid, code, name, type, rules (JSON), context)
- `constraint` (id, uid, code, name, expression, appliesTo)
- `permission` (id, uid, code, name, resource, action, conditions)

### Boundaries & Dependencies
- **Inbound**: IAM BC (user/group events for policy evaluation).
- **Outbound**: APIs for policy checks; events like "policy violated".
- **Flexibility**: Meta-config for rules (JSON expressions); extensible for custom constraints.

### Integration with Dynamic Entities
- Policies can reference dynamic entities for context-aware rules.
- Registry BC can extend policy attributes dynamically.

## Benefits
- Decouples policy from identity and business logic.
- Enables flexible, explicit governance without coupling.
- Supports overlays for complex access scenarios.

## Risks
- Migration from implicit legacy policies.
- Performance for real-time policy evaluation.

## Open Questions
- How to handle policy conflicts or hierarchies?
- Integration with external policy engines?

## Final Decisions (from transition phase)
- **Policy BC role:** derived; interprets Planning, Team, and Org intent, but does not own source data.
- **Conflict resolution:** deterministic; explicit precedence model (e.g., most-specific-wins).
- **Migration strategy:** isolated adapters allowed; no direct legacy inference in new model.

## Next Steps
- Prototype policy evaluation APIs.
- Define rule expression language.

---

## Enforced Usage Rules
- **Can use:** policy, constraint, permission; APIs for policy evaluation; events for violations; meta-config for rules (JSON expressions); extensible constraints
- **Cannot use:** Implicit access rules in teams/assignments; legacy team.form_permissions; scattered policy logic
- **Temporary adapter allowance:** Allow read-only access to legacy policies during migration; use transitional policy overlays