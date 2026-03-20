# Documentation Structure and Workflow

This document explains the repository documentation structure and how developers / AI agents should use it.

## Tree

- `docs/architecture-overview.md`: high-level architecture, domains, and design principles.
- `docs/current-system-overview.md`: implementation-level current state, core entities, and technical context.
- `docs/rfcs/`: all bounded context RFCs and evolution proposals.
  - `rfc-analytics-bc.md`
  - `rfc-campaign-planning-bc.md`
  - `...`
- `docs/adrs/`: architectural decision records (to be created per decision). 
- `docs/process/`: workflow and governance guides like this one.

## Where to author

1. New domain or behavior proposal → create a new RFC in `docs/rfcs/` (copy template from existing files).
2. Decision closure → move key design statement to `docs/adrs/` with a short-summary + rationale.
3. Tracking / transition notes → keep in `docs/process/` and reference RFC/ADR.

## Cross-links

- Each RFC should include a "Status" section and link to related ADRs in `docs/adrs/`.
- Each ADR should include source RFC references and any implementation paths.

## AI Agent behavior

- Agents should first read `docs/process/docs-structure.md` and `docs/current-system-overview.md` before applying changes.
- For modifications, make non-breaking edits and respect existing folder structure.

## Best practices

- Keep RFCs lightweight and focused on intent and boundaries.
- Keep ADRs deterministic, naming: `YYYYMMDD-xxx-title.md`.
- Avoid mixing implementation language in RFC; use architectural notation.
