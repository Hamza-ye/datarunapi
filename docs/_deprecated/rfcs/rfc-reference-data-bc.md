# RFC: Reference Data & Options Bounded Context

## Status
Proposed — Placeholder (deferred until core BCs are validated)

## Context
Options and reference data are scattered. The vision needs a centralized hub for canonical data.

## Problem
- No clear ownership of reference entities.
- Hard to add custom options.

## Proposal
Own options and reference data. Provides read-only APIs.

### Core Responsibilities
- Manage canonical options and references.

### Key Entities/Models
- `option_set`, `option_value`.

### Boundaries & Dependencies
- **Inbound**: None.
- **Outbound**: APIs for lookups.
- **Flexibility**: Meta-config for hierarchies.

### Integration with Dynamic Entities
- Can host dynamic entities as options.

## Benefits
- Centralized reference management.
- Independent updates.

## Risks
- Performance for large option sets.

## Open Questions
- Supporting dynamic option hierarchies?

## Next Steps
- Design lookup APIs.

---

## Enforced Usage Rules
- **Can use:** option_set, option_value
- **Cannot use:** Scattered options in other tables
- **Temporary adapter allowance:** Allow read-only access to legacy options during migration