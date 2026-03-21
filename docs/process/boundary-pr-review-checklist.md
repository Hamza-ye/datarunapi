# Boundary PR Review Checklist

Use this checklist for every PR that touches more than one module.

## Required Checks

- [ ] Change belongs to the module it is implemented in.
- [ ] No direct cross-module internal access was introduced.
- [ ] Interactions use public contracts, events, or adapters.
- [ ] No net-new feature logic was added to legacy-core.
- [ ] Contract changes are documented and versioned.
- [ ] Tests cover adapter behavior and boundary assumptions.
- [ ] Module owner(s) approved the change.

## Legacy-Core Specific Checks

- [ ] Change is stabilization, compatibility, or extraction only.
- [ ] No new business capability was embedded in legacy-core.
- [ ] A corresponding BC implementation path exists or is tracked.

## Evidence to Include in PR Description

- Modules affected
- Contract or adapter touched
- Risk level (Low, Medium, High)
- Rollback scope
- Follow-up tasks (if any)

## Rejection Conditions

Reject or request changes if any of the following are true:

- Hidden coupling is introduced.
- A BC feature is implemented in legacy-core.
- A public contract is changed without owner approval.
- Boundary tests are missing for adapter or contract behavior.

---

*Date: 2026-03-21*  
*Maintainers: Domain Architecture Team*