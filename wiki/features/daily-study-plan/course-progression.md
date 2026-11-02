---
title: Advance only when an item is done
feature: daily-study-plan
type: design
status: implemented
summary: Items stay until ticked and the next appears the following day, so skipped days never skip content, because the plan is meant to work as a course rather than a daily feed.
keywords:
  - TrackProgress
  - doneOn
  - currentIndex
aliases:
  - pick up where you left off
  - course mode
questions:
  - Why doesn't the plan move on each day by itself?
  - Why only one item per day in each track?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Curriculum.kt
related:
  - daily-study-plan/behavior
---

# Advance only when an item is done

## Summary

A plan item stays as today's item until it is ticked done. The next item appears the day after, so missing a day loses no content.

## Context

The plan was requested as "a course to teach everyday something", aimed at a beginner. A course builds on earlier lessons, so a calendar-driven schedule would skip material whenever a day was missed. From the development session of 2026-10-26.

## Decision

Store the position per track as `TrackProgress(index, doneOn)`. Derive today's item from those two values and today's date, without any scheduled job. Advance only after a done item's date has passed. **Skip** and **Previous** let the user move by hand, without marking anything done.

## Alternatives considered

None recorded.

## Consequences

- At most one item per track can be done each day.
- Falling behind never creates a backlog, but the course only moves when items get ticked.
- Because nothing is scheduled, the rule needs no background work to stay correct.

## Open questions

None.

## History

- 2026-10-26: Decided when the study plan was added.
