# RFC: Analytics & Reporting Bounded Context

## Status
Proposed - Initial Draft (we can postpone detailed discussion until after expert feedback on the other core BCs)

## Context
Reporting is currently handled via simple pivots, but coupled with submissions. The vision needs independent analytics for scalable, ad-hoc reporting.

## Problem
- Reporting logic tied to operational data, limiting flexibility.
- Hard to add complex analytics without affecting core.

## Proposal
Own pivots, materialized views, and reporting. Subscribes to submission events; uses read models.

### Core Responsibilities
- Handle pivots and reporting.

### Key Entities/Models
- Pivot tables/materialized views.

### Boundaries & Dependencies
- **Inbound**: Events from Submission BC.
- **Outbound**: None (purely analytical).
- **Flexibility**: Meta-based report configs.

### Integration with Dynamic Entities
- Reports can include dynamic entities via read models.

## Benefits
- Independent scaling for analytics.
- Flexible ad-hoc reporting.

## Risks
- Data consistency in read models.

## Open Questions
- Supporting real-time vs. batch reporting?

## Next Steps
- Prototype pivot generation.</content>
<parameter name="filePath">/home/hamza/data-run-api/docs/rfc-analytics-bc.md