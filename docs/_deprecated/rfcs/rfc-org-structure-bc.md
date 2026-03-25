# RFC: Organizational Structure Bounded Context

## Status
Accepted - Decision Captured

## Related ADRs and contracts 
- [../adrs/20260320-002-org-structure-bc-centralized-api.md](../adrs/20260320-002-org-structure-bc-centralized-api.md)
- Initial contracts ([../rfcs/org-structure-bc-initial-contract.md](../process/org-structure-bc-initial-contract.md))

## Context
The system implements hierarchical org units similar to DHIS2, but they're tightly coupled with assignments and teams. The future vision needs a stable governance foundation for hierarchical data, reusable across domains.

## Problem
- Org hierarchies are implicit in assignments, leading to scattered filtering logic.
- Changes to org structure unintentionally affect user access and submissions.
- Limited to predefined levels; hard to add custom hierarchies.

## Proposal
Isolate org structure into its own BC, owning hierarchies and groupings. It publishes events for changes and provides read-only APIs for queries. No inbound dependencies.

### Core Responsibilities
- Manage org unit trees, groups, and sets.
- Support hierarchical queries and validations.

### Key Entities/Models
- `org_unit` (id, uid, code, name, path, level, parent_id, ...)
- `org_unit_group`, `org_unit_group_members`, `org_unit_groupset`, etc.

### Boundaries & Dependencies
- **Inbound**: None.
- **Outbound**: APIs for hierarchy data; events like "org unit hierarchy changed".
- **Flexibility**: Meta-based rules for dynamic group membership.

### Integration with Dynamic Entities
- Org units can be subjects in data; dynamic entities can reference hierarchies via APIs.

## Benefits
- Decouples governance from operations.
- Enables flexible org structures without affecting submissions.

## Risks
- Migration of existing implicit relationships.
- Performance for deep hierarchies.

## Open Questions
- How to handle org unit renames across references?
- Support for graph-based hierarchies?

## Next Steps
- Review with domain experts.
- Prototype hierarchy APIs.

---

## Enforced Usage Rules
- **Can use:** org_unit, org_unit_group, org_unit_group_members, org_unit_groupset
- **Cannot use:** Implicit org hierarchies in assignments
- **Temporary adapter allowance:** Allow read-only access to legacy org data during migration