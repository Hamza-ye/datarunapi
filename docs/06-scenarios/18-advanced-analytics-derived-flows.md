# Scenario 18 — Advanced Analytics / Derived Flows

**Scenario:**

- A data warehouse or analytics service continuously evaluates submissions and telemetry to detect anomalies (e.g., sudden stockouts, correlated temperature excursions, statistical fraud signals).
- When a pattern is detected, it auto-triggers a new FlowDefinition instance (campaign, investigation, recall) targeting the affected entities without human scheduling.
- Derived flows inherit context (entities, submissions, events) from the source anomaly so responders can trace why the campaign exists.
- Closing the derived flow feeds back into analytics, updating models or suppressing duplicate triggers.
- Governance requires a clear audit chain: which rule fired, which data set it relied on, and what follow-up actions occurred.

**What this introduces:**

- reaction layer rules that emit Events capable of instantiating flows automatically
- analytics-to-platform handshake (data warehouse feedback loop)
- anomaly payloads embedded in FlowDefinition/Instance metadata for traceability
- suppression / deduplication logic for auto-triggered campaigns
- policy controls around automated launches and audit evidence for reviewers
