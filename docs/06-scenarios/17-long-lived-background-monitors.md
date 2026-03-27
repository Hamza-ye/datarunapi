# Scenario 17 — Long-Lived Background Monitors

**Scenario:**

- Continuous cold-chain telemetry streams (temperature, humidity, door sensors) arrive 24/7 from IoT gateways.
- Instead of discrete submissions, the system ingests time-series packets that must be validated, stored, and linked to entities.
- Threshold breaches spawn investigation FlowTasks automatically, while normal data simply accumulates for analytics.
- Operators need rolling windows (last 2 hours, last 7 days) and anomaly summaries, not just end-of-period completeness reports.
- Data retention, compression, and replay matter because investigations may look back weeks to reconstruct conditions.

**What this introduces:**

- streaming / ingestion bridge feeding the Capture + Event models
- hybrid capture modes (continuous telemetry + discrete investigation submissions)
- auto-generated FlowTasks triggered by telemetry-derived events
- long-lived state tracking with sliding-window evaluations and alerts
- storage + archival policies tuned for high-volume time-series data linked to entities
