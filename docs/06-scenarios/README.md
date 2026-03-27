# Scenarios

> **Purpose:** Architecture pressure-test scenarios. These are NOT initial scope — they validate that the architectural direction supports future needs without reworking the foundation.

## Ordering Rationale

The scenarios follow a natural progression:

**Simple capture** → **Structured operations** → **Workflows** → **Coordination** → **Dynamic systems**

This allows incremental reasoning — detecting where architecture would break without jumping to complex solutions too early.

---

## Index

| # | Scenario | Key concepts introduced |
|---|----------|------------------------|
| [00](00-basic-structured-capture.md) | Basic Structured Data Capture | schema, submission, validation |
| [01](01-entity-linked-capture.md) | Entity-Linked Data Capture | entity reference, identity |
| [02](02-periodic-reporting.md) | Periodic Reporting | periodicity, completeness tracking |
| [03](03-user-based-assignment.md) | User-Based Assignment | assignment, responsibility |
| [04](04-supervisor-review.md) | Supervisor Review & Approval | state transitions, role-based actions |
| [05](05-supervision-audit-visits.md) | Supervision / Audit Visits | scheduled tasks, follow-up triggers |
| [06](06-entity-registry-lifecycle.md) | Entity Registry with Lifecycle | lifecycle, versioning |
| [07](07-resource-distribution.md) | Resource Distribution & Acknowledgment | hierarchical flows, event chaining |
| [08](08-case-management.md) | Case Management / Follow-Up | long-lived entities, actor handoffs |
| [09](09-coordinated-campaign.md) | Coordinated Campaign | planning vs execution, time windows |
| [10](10-dynamic-targeting.md) | Dynamic Targeting | conditional logic, feedback loops |
| [11](11-multi-step-approval.md) | Multi-Step Approval | state machines, chained approvals |
| [12](12-event-triggered-actions.md) | Event-Triggered Actions | event-driven behavior, rule evaluation |
| [13](13-cross-flow-linking.md) | Cross-Flow Linking | reference linking, consistency |
| [14](14-multi-level-distribution.md) | Multi-Level Resource Distribution | chain of custody, step-level tracking |
| [15](15-cross-program-overlays.md) | Cross-Program Overlays | multi-consumer reporting overlays |
| [16](16-emergency-rapid-response.md) | Emergency / Rapid-Response Flows | real-time scope changes, bursty events |
| [17](17-long-lived-background-monitors.md) | Long-Lived Background Monitors | streaming ingestion, telemetry-driven tasks |
| [18](18-advanced-analytics-derived-flows.md) | Advanced Analytics / Derived Flows | reaction-triggered campaigns |

## Reference Scenarios

| Scenario | Status | Purpose |
|----------|--------|---------|
| [Monthly Inventory](ref-monthly-inventory.md) | Active — Slice 1 | Full end-to-end proof against baseline v1 |
