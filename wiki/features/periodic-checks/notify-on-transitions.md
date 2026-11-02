---
title: Notify on state changes only
feature: periodic-checks
type: design
status: implemented
summary: Checks store an alerting flag and notify only when the state flips, so a site that stays down produces one alert per outage rather than one per run.
keywords:
  - alerting
  - lastHash
  - transition
aliases:
  - alert fatigue
  - notification spam
questions:
  - Why do I only get one notification while a site is down?
sources:
  - app/src/main/java/com/gaurav/devagent/work/Workers.kt
  - app/src/main/java/com/gaurav/devagent/data/Models.kt
related:
  - periodic-checks/behavior
---

# Notify on state changes only

## Summary

A check notifies when its state changes, not every time a condition holds.

## Context

Checks run as often as every 15 minutes. Notifying on each run while a site was down, or while text remained on a page, would send repeated alerts for the same event. The rule was described in the development session of 2026-10-24, and the code comment on `Check.alerting` records it.

## Decision

Store `alerting` on each check, true while the alert condition holds, and compare each result with it. `PAGE_CHANGED` uses `lastHash` for the same purpose.

## Alternatives considered

None recorded.

## Consequences

- One outage produces one "down" alert and one "back up" alert.
- If that single notification is missed, nothing repeats it.
- A brief flip in a result counts as a real change, which is why error pages cause the false alerts listed under the behavior page.

## Open questions

- Should error responses be excluded from `CONTAINS_TEXT` and `PAGE_CHANGED` evaluation?

## History

- 2026-10-24: Decided when checks were added.
