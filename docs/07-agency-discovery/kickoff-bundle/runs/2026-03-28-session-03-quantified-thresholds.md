# Session 03 Quantified Thresholds

## Purpose

Define numeric go/hold/rollback thresholds for the contrarian lane introduced in Session 02.
These thresholds are designed for Minimal-tier evidence and should be reviewed every 2 weeks.

## Evaluation Window

- Primary cadence: bi-weekly
- Formal decision points: Day 45, Day 60, Day 90
- Data scope: pilot lane and core lane side-by-side where possible

## Decision States

- Proceed: all critical thresholds are Green, and at most one non-critical metric is Amber.
- Hold: one critical metric is Amber or two non-critical metrics are Amber.
- Rollback: any critical metric is Red, or two consecutive Hold decisions occur.

## Metric Thresholds

### 1. Audit Reconstruction Success Rate (Critical)

Definition:
Percentage of sampled critical operational paths where the system can reconstruct who did what, when, and in what context without manual correction.

Formula:
Audit Reconstruction Success Rate = successful_reconstructions / sampled_paths * 100

Thresholds:
- Green: >= 98%
- Amber: 95% to < 98%
- Red: < 95%

### 2. Compatibility Non-Breaking Pass Rate (Critical)

Definition:
Percentage of compatibility test cases passed across existing flows after each release.

Formula:
Compatibility Pass Rate = passed_compatibility_tests / total_compatibility_tests * 100

Thresholds:
- Green: >= 99%
- Amber: 98% to < 99%
- Red: < 98%

### 3. Expected-vs-Actual Reporting Accuracy (Critical)

Definition:
Agreement rate between authoritative source records and supervisory reporting views for expected/actual, pending, late, and approved counts.

Formula:
Reporting Accuracy = matching_report_cells / sampled_report_cells * 100

Thresholds:
- Green: >= 98%
- Amber: 97% to < 98%
- Red: < 97%

### 4. Delivery Predictability (Critical)

Definition:
Percentage of committed sprint outcomes delivered on time and in scope.

Formula:
Delivery Predictability = delivered_commitments / total_commitments * 100

Thresholds:
- Green: >= 85%
- Amber: 70% to < 85%
- Red: < 70%

Additional rule:
Two consecutive Amber periods on this metric trigger automatic Rollback review.

### 5. Guardrail Breach Count (Critical)

Definition:
Count of unresolved breaches of non-negotiable guardrails in the evaluation window.

Thresholds:
- Green: 0 unresolved breaches
- Amber: 1 unresolved breach, remediated within 5 working days
- Red: > 1 unresolved breach OR any unresolved breach older than 5 working days

### 6. Supervisor Active Usage (Non-Critical)

Definition:
Share of pilot supervisors actively using the contrarian lane views at least once per week.

Formula:
Supervisor Active Usage = weekly_active_supervisors / target_supervisors * 100

Thresholds:
- Green: >= 70%
- Amber: 60% to < 70%
- Red: < 60%

### 7. Intervention Lead-Time Improvement (Non-Critical)

Definition:
Median improvement in time between risk emergence and supervisor intervention compared to baseline.

Formula:
Lead-Time Improvement = (baseline_median - current_median) / baseline_median * 100

Thresholds:
- Green: >= 20%
- Amber: 10% to < 20%
- Red: < 10%

### 8. Team Cognitive Load Index (Non-Critical)

Definition:
Composite score from weekly team pulse (1-5), incident interrupts, and context-switch count.
Higher is worse.

Normalized index range: 0 to 100.

Thresholds:
- Green: <= 45
- Amber: > 45 and <= 60
- Red: > 60

## Composite Decision Logic

1. If any Critical metric is Red => Rollback.
2. If Guardrail Breach Count is Red => immediate Rollback review, no waiting period.
3. If two or more Critical metrics are Amber in the same period => Hold.
4. If one Critical metric is Amber for two consecutive periods => Rollback review.
5. If all Critical metrics are Green but two or more Non-Critical are Red => Hold and run corrective plan.
6. Proceed only when all Critical are Green and no more than one Non-Critical is Amber.

## Day-45, Day-60, Day-90 Gates

### Day 45 Gate
Must-have:
- Audit Reconstruction >= 98%
- Reporting Accuracy >= 98%
- Guardrail Breach Count Green

Else: Hold (or Rollback if any Red critical).

### Day 60 Gate
Must-have:
- All Day 45 criteria
- Compatibility Pass Rate >= 99%
- Delivery Predictability >= 85%

Else: Hold with remediation plan.
Any Red critical: Rollback.

### Day 90 Gate
Must-have:
- All Day 60 criteria
- Supervisor Active Usage >= 70%
- Lead-Time Improvement >= 20%

If all pass: keep contrarian lane as controlled ongoing experiment.
If not: rollback to Session 01 sequencing and retain reusable instrumentation.

## Measurement Notes

- Sampling minimums:
  - Audit paths sampled per period: >= 50
  - Report cells sampled per period: >= 200
  - Compatibility tests per release: >= 100
- Baseline freeze date for comparisons: 2026-03-28 (Session 01 close)
- Metric owner roles:
  - Accountability metrics: Platform QA lead
  - Delivery metrics: Delivery manager
  - Usage and lead-time metrics: Product operations lead

## Change Control

Threshold changes require:
1. Written rationale tied to observed data.
2. Approval from orchestrator owner and one independent reviewer.
3. Versioned update in this file with date and reason.
