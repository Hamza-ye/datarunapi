# ADR-002: Org Structure BC as Centralized API Contract

## Status
Accepted

## Context
The system implements hierarchical org units similar to DHIS2, but they're tightly coupled with assignments and teams. The future vision needs a stable governance foundation for hierarchical data, reusable across domains.

The problem includes implicit org hierarchies in assignments leading to scattered filtering logic, changes affecting user access and submissions unintentionally, and limitations to predefined levels making custom hierarchies hard.

## Decision
We will isolate org structure into its own BC as a centralized API contract, owning hierarchies and groupings. It provides read-only APIs for queries and publishes events for changes. No inbound dependencies.

Key aspects:
- **Role**: Centralized API contract for org unit trees, groups, and sets.
- **Responsibilities**: Manage hierarchical queries, validations, and support for dynamic group membership via meta-based rules.
- **Integration**: Org units can be subjects in data; dynamic entities can reference hierarchies via APIs.

## Consequences
- **Positive**: Decouples governance from operations, enables flexible org structures without affecting submissions.
- **Negative**: Migration of existing implicit relationships, performance concerns for deep hierarchies.
- **Risks**: Handling org unit renames across references, support for graph-based hierarchies.

## Implementation Paths
- org hierarchy read API contract (query and validation semantics)
- org change event publication contract
- migration adapters for read-only legacy org hierarchy ingestion

## Related
- RFC: Organizational Structure Bounded Context ([../rfcs/rfc-org-structure-bc.md](../rfcs/rfc-org-structure-bc.md))
- RFC: Reference Data Bounded Context ([../rfcs/rfc-reference-data-bc.md](../rfcs/rfc-reference-data-bc.md))

---

*Date: 2026-03-20*  
*Deciders: Domain Architecture Team*  
*Consulted: Development Team, Product Owners*