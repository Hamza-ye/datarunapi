# ADR Index

This index tracks accepted and proposed architecture decisions.

## Current ADRs

| ADR | Title | Status | Date | Source RFC |
|---|---|---|---|---|
| [20260320-001](20260320-001-policy-bc-derived-overlay.md) | Policy BC as Derived Overlay with Precedence Model | Accepted | 2026-03-20 | [rfc-policy-bc.md](../rfcs/rfc-policy-bc.md) |
| [20260320-002](20260320-002-org-structure-bc-centralized-api.md) | Org Structure BC as Centralized API Contract | Accepted | 2026-03-20 | [rfc-org-structure-bc.md](../rfcs/rfc-org-structure-bc.md) |

## Usage Rules

- Add a new row when an ADR file is created.
- Update status in this index if an ADR is superseded or deprecated.
- Keep source RFC links and ADR status aligned with each other.
- Keep file naming in the form `YYYYMMDD-xxx-title.md`.

## Status Lifecycle

- **Proposed**: Draft decision under review; not yet authoritative.
- **Accepted**: Approved decision; implementation must align unless superseded.
- **Superseded**: Replaced by a newer ADR; keep for historical traceability.
- **Deprecated**: Decision retained for context but no longer recommended for new work.

## Decision Micro-Checklist

1. Confirm RFC scope and boundaries are stable.
2. Create ADR from template and lock the decision statement.
3. Record consequences and implementation paths.
4. Add or update ADR row in this index.
5. Update source RFC status and backlink to the ADR.
