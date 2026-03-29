# Guardrails And Non-Goals

## Non-Negotiable Guardrails

1. Identity must remain stable.
Every tracked subject and actor must keep a durable identity that survives growth and change.

2. Platform records must remain traceable.
Actions and submitted data must be explainable by actor, time, and context.

3. Core capabilities must stay domain-agnostic.
Domain-specific semantics belong in overlays, not in shared foundations.

4. Product growth must be non-breaking.
Adding one domain capability must not regress existing operational flows.

5. Operational concerns must stay composable.
Assignment, scheduling, review, and progress are reusable patterns, not isolated one-off inventions per domain.

6. Process and subject lifecycles must remain decoupled.
Interactions should happen through explicit events and contracts, not hidden coupling.
