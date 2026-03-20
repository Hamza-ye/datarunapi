# RFC: Team & Collaboration Bounded Context

## Status
Proposed - Initial Draft (we can postpone detailed discussion until after expert feedback on the other core BCs)

## Context
Teams are currently coupled with assignments and org units, leading to implicit access rules. The future vision requires flexible collaboration tools as core, with overlays for domain-specific roles.

## Problem
- Team logic is scattered across assignments and user management.
- Hard to add dynamic permissions or cross-team workflows.

## Proposal
Own teams, memberships, and collaborative roles. Depends on IAM for users; publishes events for changes.

### Core Responsibilities
- Manage teams, user memberships, and managed hierarchies.
- Handle team-based permissions.

### Key Entities/Models
- `team`, `team_user`, `team_managed_teams`.

### Boundaries & Dependencies
- **Inbound**: IAM BC for user data.
- **Outbound**: Events like "team membership changed".
- **Flexibility**: Meta-config for permissions (JSON rules).

### Integration with Dynamic Entities
- Teams can be subjects in data; dynamic entities can extend team attributes.

## Benefits
- Decouples collaboration from operations.
- Enables flexible team dynamics.

## Risks
- Transition from current implicit model.

## Open Questions
- How to handle overlapping team memberships?

## Next Steps
- Prototype permission rules.</content>
<parameter name="filePath">/home/hamza/data-run-api/docs/rfc-team-collaboration-bc.md