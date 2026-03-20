# RFC: User Identity & Access Management (IAM) Bounded Context

## Status
Proposed - Initial Draft

## Context
The current system manages users, user groups, and basic access control implicitly through team memberships and assignments. This leads to scattered logic and tight coupling, making it hard to evolve authentication, roles, or integrate external IDPs. The future vision requires a stable, reusable IAM foundation for 70-80% of needs, with overlays for domain-specific access.

## Problem
- User management is entangled with business logic (e.g., teams).
- No clear separation of identity from access rules.
- Hard to add features like OAuth2 integration or dynamic roles without side effects.

## Proposal
Introduce an IAM BC as a core tool, owning user lifecycle, authentication, and basic permissions. It exposes APIs for user lookup and publishes events for changes. Dependencies are minimal: provides to other BCs, consumes none.

### Core Responsibilities
- Manage user accounts, groups, and authentication.
- Handle basic access control (roles, permissions).
- Support external integrations (e.g., OAuth2).

### Key Entities/Models
- `user` (id, uid, code, login, firstname, created_date, ...)
- `user_group` (id, uid, code, name, created_date, ...)
- `user_group_users` (group_id, user_id)

### Boundaries & Dependencies
- **Inbound**: None (standalone for identity).
- **Outbound**: APIs for user data; events like "user deactivated".
- **Anti-Corruption Layer**: Translates external auth to internal models.

### Flexibility Hooks
- Meta-config for roles (JSON rules defining permissions like "can access if in group X").
- Extensible for custom auth providers via plugins.

### Integration with Dynamic Entities
- Can reference users as subjects in data via APIs.
- Registry BC can extend user attributes dynamically.

## Benefits
- Decouples identity from business logic.
- Enables independent scaling and evolution.
- Supports future overlays (e.g., domain-specific roles).

## Risks
- Initial migration from current implicit model.
- Event consistency for real-time access checks.

## Open Questions
- How to handle legacy team-based permissions during transition?
- Integration with external IDPs?

## Next Steps
- Gather feedback from experts.
- Prototype API contracts.

---

## Enforced Usage Rules
- **Can use:** user, user_group, user_group_users; APIs for user lookup; events for changes; meta-config for roles (JSON rules); extensible auth providers
- **Cannot use:** Implicit user management entangled with business logic (e.g., teams); deprecated team.form_permissions for access control; scattered access rules in assignments
- **Temporary adapter allowance:** Allow read-only access to legacy team-based permissions during migration; use UserExecutionContext and AccessFilter classes for transitional access control</content>
<parameter name="filePath">/home/hamza/data-run-api/docs/rfc-iam-bc.md