# Scenario Envelope

## Purpose

Define what scenario surface agents should plan for in discovery, while avoiding unnecessary complexity in the first pass.

## Tier 1: Minimal (Default)

Use these scenarios first:
- [00-basic-structured-capture.md](../06-scenarios/00-basic-structured-capture.md)
- [01-entity-linked-capture.md](../06-scenarios/01-entity-linked-capture.md)
- [02-periodic-reporting.md](../06-scenarios/02-periodic-reporting.md)
- [ref-monthly-inventory.md](../06-scenarios/ref-monthly-inventory.md)

This tier validates:
- structured data capture,
- durable entity linkage,
- recurring expected-vs-actual tracking,
- review and late-handling patterns in one end-to-end reference.

## Tier 2: Core Expansion

Add only if first-pass plans under-specify operational workflow behavior:
- [03-user-based-assignment.md](../06-scenarios/03-user-based-assignment.md)
- [04-supervisor-review.md](../06-scenarios/04-supervisor-review.md)
- [06-entity-registry-lifecycle.md](../06-scenarios/06-entity-registry-lifecycle.md)

This tier strengthens:
- actor responsibility and handoff clarity,
- review depth and governance patterns,
- long-lived entity lifecycle planning.

## Tier 3: Stress Envelope

Add only after baseline-aligned plans are stable:
- [11-multi-step-approval.md](../06-scenarios/11-multi-step-approval.md)
- [12-event-triggered-actions.md](../06-scenarios/12-event-triggered-actions.md)
- [16-emergency-rapid-response.md](../06-scenarios/16-emergency-rapid-response.md)
- [18-advanced-analytics-derived-flows.md](../06-scenarios/18-advanced-analytics-derived-flows.md)

This tier tests:
- chained approvals,
- event-triggered behavior,
- fast-changing and high-pressure operating conditions,
- analytics-driven operational adaptation.
