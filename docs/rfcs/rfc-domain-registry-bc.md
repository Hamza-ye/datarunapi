# RFC: Domain Entity Registry Bounded Context

## Status
Proposed - Initial Draft

## Context
The platform needs to support dynamic domain entities (e.g., patients, assets) as subjects in data, plugged in easily. Current BCs handle static entities; a registry is needed for extensibility.

## Problem
- No centralized way to define/manage dynamic entities.
- Hard to integrate new entities without code changes.

## Proposal
Centralize meta-schemas for dynamic entities. Acts as a hub for discovery and integration.

### Core Responsibilities
- Manage JSON schemas for dynamic entities.
- Publish events for entity changes.

### Key Entities/Models
- Entity definitions (JSONB schemas).

### Boundaries & Dependencies
- **Inbound**: None (standalone registry).
- **Outbound**: Events/APIs for entity lookups.
- **Flexibility**: Extensible schemas for custom attributes.

### Integration with Dynamic Entities
- Core of dynamic entity support; other BCs reference via registry.

## Benefits
- Easy plugging of new entities.
- Maintains modularity.

## Risks
- Complexity in schema versioning.

## Open Questions
- How to handle entity relationships?

## Next Steps
- Prototype schema registry.</content>
<parameter name="filePath">/home/hamza/data-run-api/docs/rfc-domain-registry-bc.md