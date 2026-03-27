# Scenario 16 — Emergency / Rapid-Response Flows

**Scenario:**

- An unexpected outbreak or recall forces immediate data capture and coordination.
- Scope changes hourly as new facilities, border points, or shipments are added or removed.
- Tasks need skip-level approvals (regional lead can approve on behalf of district) and guard overrides under predefined emergency rules.
- Notifications must fire in near-real time to SMS/email/voice channels with throttling to avoid alert storms.
- Historical submissions remain relevant for situational awareness, but responders prioritize the latest state without waiting for the regular reporting cadence.

**What this introduces:**

- real-time scope amendment and re-evaluation of FlowInstances
- bursty event volumes that demand asynchronous delivery and back-pressure handling
- emergency guard policies (override paths, temporary roles, condensed approvals)
- high-frequency notification fan-out tied to state transitions
- operational dashboards that reconcile rapid changes with underlying audit history
