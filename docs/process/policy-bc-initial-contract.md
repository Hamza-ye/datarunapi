# Policy BC Initial Contract

This document defines the minimum public contract for the first implementation phase of policy-bc.

## Purpose

- Provide a stable policy evaluation contract for early BC adoption.
- Keep policy behavior explicit and deterministic without over-designing full governance features.
- Prevent direct permission logic reads from legacy structures outside approved adapters.
- Phase 1 is intentionally unblocking-only until upstream source BC contracts are stable.

## Contract Scope (Phase 1)

Read-oriented evaluation only:

- evaluate permission decision for actor, action, and resource
- get applicable policy summaries for actor and context
- check policy conflict outcome using precedence rules

Out of scope for Phase 1:

- policy authoring or mutation endpoints
- advanced policy simulation tooling
- external policy-engine integration
- multi-tenant policy administration surface

## Dependency Gate

Broader policy capabilities remain lower priority until these contracts are stable and consumed through BC boundaries:

- org-structure-bc public read contracts
- iam-bc identity and group intent contracts
- planning/team intent contracts that policy interprets as derived input

Until those are stable, policy-bc should only implement the minimal deterministic blocking/evaluation surface defined in this document.

## Public Query Contract

### 1. Evaluate Permission

Input:
- actor id
- action
- resource type
- resource id
- optional context map

Output:
- PolicyDecisionView

### 2. Get Applicable Policies

Input:
- actor id
- optional scope context

Output:
- ordered list of PolicySummaryView

### 3. Evaluate Conflict Outcome

Input:
- list of candidate policy identifiers
- evaluation context

Output:
- ConflictResolutionView

## Shared Response Shapes

PolicyDecisionView

- decision (allow or deny)
- reason code
- matched policy id
- precedence rule applied

PolicySummaryView

- id
- code
- name
- type
- priority
- active

ConflictResolutionView

- winning policy id
- strategy (most-specific-wins)
- explanation

Notes:
- Decision outputs are deterministic for equivalent inputs.
- Precedence strategy is fixed for Phase 1 and aligned with ADR decisions.

## Contract Rules

- Consumers must use this contract, not legacy permission fields or team-based implicit rules.
- Contract is BC-public and implementation-private.
- New response fields may be added only for active implementation slices.
- Breaking changes require BC owner approval and documentation update.

## Adapter Rule

If Phase 1 policy reads still require legacy sources, they must pass through an explicit adapter owned by policy-bc. Consumers must not know whether evaluation is legacy-backed or native.

## Initial Validation Scenarios

- Evaluate whether an actor can perform an action on a resource.
- List policy summaries affecting a scoped operation.
- Resolve a deny-versus-allow conflict using precedence.

## Phase 2 Promotion Criteria

Move beyond this Phase 1 contract only when all of the following are true:

- At least one end-to-end consumer flow uses Evaluate Permission without direct legacy permission reads.
- Determinism is proven by repeatable equivalent-input/equivalent-output tests.
- Precedence conflict outcomes are validated against accepted ADR behavior.

## Open Questions

- Do we need an explain trace level beyond reason code and winning policy?
- Should policy summaries expose constraint metadata in Phase 1?
- When should policy mutation endpoints enter the public contract?

## Related

- [../rfcs/rfc-policy-bc.md](../rfcs/rfc-policy-bc.md)
- [../adrs/20260320-001-policy-bc-derived-overlay.md](../adrs/20260320-001-policy-bc-derived-overlay.md)
- [module-ownership-map.md](module-ownership-map.md)
- [boundary-pr-review-checklist.md](boundary-pr-review-checklist.md)

---

Date: 2026-03-21  
Owner: Domain Policy Team  
Status: Active Phase 1 Unblocking Contract