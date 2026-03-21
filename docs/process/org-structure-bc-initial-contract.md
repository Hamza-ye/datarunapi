# Org Structure BC Initial Contract

This document defines the minimum public contract for the first implementation phase of `org-structure-bc`.

## Purpose

- Provide a stable read-oriented contract for early BC adoption.
- Support thin validating slices without over-designing the full domain surface.
- Prevent direct reads from legacy org hierarchy structures outside approved adapters.

## Contract Scope (Phase 1)

Read-only queries only:

- get org unit by id
- get org unit by code
- get parent chain for an org unit
- get descendants for an org unit
- validate whether an org unit exists

Out of scope for Phase 1:

- create/update/delete org units
- group and group-set mutation APIs
- bulk synchronization APIs
- graph or non-tree hierarchy support

## Public Query Contract

### 1. Get Org Unit By Id

Input:
- `orgUnitId`

Output:
- `OrgUnitView`

### 2. Get Org Unit By Code

Input:
- `orgUnitCode`

Output:
- `OrgUnitView`

### 3. Get Parent Chain

Input:
- `orgUnitId`

Output:
- ordered list of `OrgUnitView` from immediate parent to root

### 4. Get Descendants

Input:
- `orgUnitId`
- optional `maxDepth`

Output:
- ordered list of `OrgUnitView`

### 5. Exists Check

Input:
- `orgUnitId` or `orgUnitCode`

Output:
- boolean

## Shared Response Shape

`OrgUnitView`

- `id`
- `uid`
- `code`
- `name`
- `path`
- `level`
- `parentId`
- `active`

Notes:
- `path` is the authoritative hierarchy locator for Phase 1.
- Response shape is intentionally minimal and read-focused.

## Contract Rules

- Consumers must use this contract, not legacy org tables or internal package access.
- Contract is BC-public and implementation-private.
- New fields may be added only if required by an active slice.
- Breaking changes require BC owner approval and documentation update.

## Adapter Rule

If Phase 1 reads are still sourced from legacy data, they must pass through an explicit adapter owned by `org-structure-bc`. Consumers must not know whether the source is legacy-backed or native.

## Initial Validation Scenarios

- Resolve org unit by code for a planning flow.
- Resolve parent chain for access or filtering logic.
- Resolve descendants for scoped query evaluation.

## Open Questions

- Should `active` be mandatory in all response shapes?
- Do we need pagination for descendants in Phase 1?
- When should group membership queries enter the public contract?

## Related

- [../rfcs/rfc-org-structure-bc.md](../rfcs/rfc-org-structure-bc.md)
- [../adrs/20260320-002-org-structure-bc-centralized-api.md](../adrs/20260320-002-org-structure-bc-centralized-api.md)
- [module-ownership-map.md](module-ownership-map.md)
- [boundary-pr-review-checklist.md](boundary-pr-review-checklist.md)

---

*Date: 2026-03-21*  
*Owner: Domain Structure Team*  
*Status: Draft for implementation kickoff*