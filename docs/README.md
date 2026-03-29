# Documentation

## Quick Start

| I want to… | Go to |
|------------|-------|
| Know where the project is right now | [03-delivery/status.md](03-delivery/status.md) |
| Understand the implementation roadmap | [03-delivery/implementation-playbook.md](03-delivery/implementation-playbook.md) |
| Read the frozen architecture baseline | [02-baseline/baseline-v1.md](02-baseline/baseline-v1.md) |
| Understand why the platform exists | [01-vision/platform-ambition.md](01-vision/platform-ambition.md) |
| See intentionally deferred decisions | [02-baseline/open-decisions-log.md](02-baseline/open-decisions-log.md) |
| Understand the module structure | [02-baseline/legacy-boundary-strategy.md](02-baseline/legacy-boundary-strategy.md) |
| Browse architecture stress-test scenarios | [06-scenarios/](06-scenarios/README.md) |

---

## Directory Structure

```
docs/
├── 01-vision/        ← WHY: platform purpose, guarantees, principles
├── 02-baseline/      ← WHAT: frozen architecture, open decisions
├── 03-delivery/      ← HOW: playbook, progress, walkthroughs
├── 04-decisions/     ← WHY X: implementation-time ADRs
├── 05-discussion/    ← ARCHIVE: historical exploration discussions
└── 06-scenarios/     ← PRESSURE TESTS: architecture validation scenarios
```

---

## Update Rules

| Tier | When to update | Who | Review? |
|------|---------------|-----|---------|
| `01-vision/` | Only if the platform's purpose fundamentally changes | Architect + team lead | Yes — consensus |
| `02-baseline/` | Only via formal revision (v2, v3…). `open-decisions-log.md` updates when a decision is opened or closed | Architect | Baseline: yes. OD-log: no |
| `03-delivery/status.md` | After every step completion | Whoever completes the step | No — factual |
| `03-delivery/walkthroughs/` | One new file per completed step | Whoever completes the step | No |
| `03-delivery/playbook` | Only if steps are reordered, added, or scoped out | Architect | Yes |
| `04-decisions/` | When a non-trivial implementation choice is made | Developer making the choice | No |
| `05-discussion/` | Never (archived) | — | — |
| `06-scenarios/` | When a new scenario is identified or refined | Architect | Yes |

---

## For Agents / Automated Tools

Load in this order (stop when you have enough context):

1. `03-delivery/status.md` — where we are now
2. `03-delivery/implementation-playbook.md` — what's next
3. `02-baseline/baseline-v1.md` — what we're building
4. `02-baseline/open-decisions-log.md` — what's intentionally deferred
5. `02-baseline/legacy-boundary-strategy.md` — module structure

---

## Archive

### Discussion

Historical discussions that led to the frozen baseline and signoffs.

Located in [`05-discussion/`](05-discussion/).
