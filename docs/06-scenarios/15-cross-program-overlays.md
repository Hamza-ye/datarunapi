# Scenario 15 — Cross-Program Overlays

**Scenario:**

- A single flow (e.g., monthly inventory) produces submissions that multiple ministries consume simultaneously.
- Each ministry defines its own completeness targets, grace periods, and escalation paths.
- Reporting pivots differ per consumer (e.g., MoH wants facility-type rollups, MoF wants commodity cost coverage, donors want geographic slices).
- Data products (dashboards, exports, APIs) must partition access by role while sharing the same underlying submissions/events.
- Late or missing data in one overlay must not block sign-off for another overlay, yet discrepancies must be traceable back to the same FlowTasks.

**What this introduces:**

- multi-consumer overlays on top of a shared flow dataset
- role-partitioned reporting surfaces with differing completeness logic
- derived data products / pivots driven by Events and FlowTask state
- overlay-specific SLAs and escalation hooks referencing the same submissions
- audit trails that correlate overlay decisions back to core flow artifacts
