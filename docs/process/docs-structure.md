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
  - `README.md`: ADR index (status, date, and source RFC traceability).
- `docs/process/`: workflow and governance guides like this one.
  - `module-ownership-map.md`: module owners, allowed dependencies, and transition rules.
  - `boundary-pr-review-checklist.md`: PR gate checklist for boundary safety.
- `.github/PULL_REQUEST_TEMPLATE.md`: default PR template enforcing boundary review evidence.

## Where to author

1. New domain or behavior proposal → create a new RFC in `docs/rfcs/` (copy template from existing files).
2. Decision closure → move key design statement to `docs/adrs/` with a short-summary + rationale.
   Update `docs/adrs/README.md` with the new decision row.
3. Tracking / transition notes → keep in `docs/process/` and reference RFC/ADR.
  Use `module-ownership-map.md` and `boundary-pr-review-checklist.md` as operational controls.
  Use `.github/PULL_REQUEST_TEMPLATE.md` to enforce the minimum review evidence in pull requests.

### Decision small-step flow

1. Validate the RFC has clear boundaries and enforced usage rules.
2. Capture one locked decision per ADR (avoid bundling unrelated decisions).
3. Add implementation paths and consequences in the ADR.
4. Sync statuses and links across RFC and ADR index.
5. Keep unresolved design questions in RFCs, not in accepted ADRs.

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
- Keep process documents normative: document rules, roles, and workflow; avoid review commentary or status conclusions in governance docs.