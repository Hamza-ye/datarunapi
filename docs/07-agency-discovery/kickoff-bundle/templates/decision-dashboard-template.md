# Decision Dashboard Template

## Header

- Review date:
- Review window:
- Session baseline reference:
- Scenario tier:
- Decision owner:
- Reviewer:

## Overall Decision

- State: Proceed | Hold | Rollback
- Rationale (3 lines max):
- Immediate actions:

## Metric Board

| Metric | Type | Current | Green | Amber | Red | Status | Trend | Owner | Notes |
|---|---|---:|---:|---:|---:|---|---|---|---|
| Audit Reconstruction Success Rate | Critical |  | >= 98% | 95-<98% | <95% |  |  |  |  |
| Compatibility Non-Breaking Pass Rate | Critical |  | >= 99% | 98-<99% | <98% |  |  |  |  |
| Expected-vs-Actual Reporting Accuracy | Critical |  | >= 98% | 97-<98% | <97% |  |  |  |  |
| Delivery Predictability | Critical |  | >= 85% | 70-<85% | <70% |  |  |  |  |
| Guardrail Breach Count (unresolved) | Critical |  | 0 | 1 <= 5 days | >1 or >5 days |  |  |  |  |
| Supervisor Active Usage | Non-Critical |  | >= 70% | 60-<70% | <60% |  |  |  |  |
| Intervention Lead-Time Improvement | Non-Critical |  | >= 20% | 10-<20% | <10% |  |  |  |  |
| Team Cognitive Load Index | Non-Critical |  | <= 45 | >45 and <=60 | >60 |  |  |  |  |

## Gate Checklist

### Day 45 Gate
- Audit Reconstruction >= 98%: Yes | No
- Reporting Accuracy >= 98%: Yes | No
- Guardrail Breach Count Green: Yes | No
- Gate result: Pass | Hold | Rollback

### Day 60 Gate
- Day 45 criteria passed: Yes | No
- Compatibility Pass Rate >= 99%: Yes | No
- Delivery Predictability >= 85%: Yes | No
- Gate result: Pass | Hold | Rollback

### Day 90 Gate
- Day 60 criteria passed: Yes | No
- Supervisor Active Usage >= 70%: Yes | No
- Lead-Time Improvement >= 20%: Yes | No
- Gate result: Pass | Hold | Rollback

## Composite Logic Check

- Any Critical Red present: Yes | No
- Two Critical Amber in same window: Yes | No
- One Critical Amber for two consecutive windows: Yes | No
- Two or more Non-Critical Red with all Critical Green: Yes | No
- Recommended state from logic: Proceed | Hold | Rollback

## Risk And Action Register

| Risk | Triggered By | Severity | Action Owner | Action | Due Date | Status |
|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |
|  |  |  |  |  |  |  |

## Decision Notes

- What changed since last review:
- What stayed stable:
- What will be tested next window:

## Sign-Off

- Decision owner sign-off:
- Reviewer sign-off:
- Date:
