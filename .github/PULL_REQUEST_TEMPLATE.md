## Summary

Describe the change in 2-5 lines.

## Modules Affected

- [ ] legacy-core
- [ ] policy-bc
- [ ] org-structure-bc
- [ ] shared-kernel
- [ ] other: <!-- specify -->

## Boundary Review Checklist

- [ ] Change belongs to the module it is implemented in.
- [ ] No direct cross-module internal access was introduced.
- [ ] Interactions use public contracts, events, or adapters.
- [ ] No net-new feature logic was added to legacy-core.
- [ ] Contract changes are documented and versioned.
- [ ] Tests cover adapter behavior and boundary assumptions.
- [ ] Module owner(s) approved the change.

## Legacy-Core Specific Checks

- [ ] This PR does not touch legacy-core.
- [ ] Or, if it does: the change is stabilization, compatibility, or extraction only.
- [ ] No new business capability was embedded in legacy-core.
- [ ] A corresponding BC implementation path exists or is tracked.

## Evidence

- Contract or adapter touched:
- Risk level: Low / Medium / High
- Rollback scope:
- Follow-up tasks:

## Notes

Reference: `docs/process/boundary-pr-review-checklist.md`
