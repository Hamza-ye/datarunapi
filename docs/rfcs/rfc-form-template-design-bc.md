# RFC: Form Template Design Bounded Context

## Status
Proposed - Initial Draft

## Context
Templates are versioned but coupled with submissions. The vision needs schema evolution decoupled from runtime data.

## Problem
- Template changes affect submissions directly.
- No meta-based field types for dynamic references.

## Proposal
Own template lifecycle and versions. Standalone; publishes "template updated" events.

### Core Responsibilities
- Define and version form schemas.
- Support extensible field types.

### Key Entities/Models
- `data_template`, `TemplateVersion`, `TemplateElement`.

### Boundaries & Dependencies
- **Inbound**: None.
- **Outbound**: Events for schema changes.
- **Flexibility**: JSON schemas for fields/sections; dynamic validation rules.

### Integration with Dynamic Entities
- Fields can reference dynamic entities via meta-configs.

## Benefits
- Independent schema evolution.
- Highly flexible forms.

## Risks
- Client-side tree composition migration.

## Open Questions
- JSON Schema standard adoption?

## Next Steps
- Prototype schema validation.</content>
<parameter name="filePath">/home/hamza/data-run-api/docs/rfc-form-template-design-bc.md