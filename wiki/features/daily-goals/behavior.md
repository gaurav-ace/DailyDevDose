---
title: Daily goal reset and evening nudge
feature: daily-goals
type: behavior
summary: A goal counts as done only while doneOn equals today, so no reset job exists. NudgeWorker runs daily at nudgeHour (default 20:00) and lists unfinished plan items and goals.
keywords:
  - isDoneToday
  - doneOn
  - NudgeWorker
  - goalsNudge
  - nudgeHour
aliases:
  - reset at midnight
  - evening nudge
questions:
  - How do goals reset each day?
  - When does the evening nudge fire, and what does it list?
sources:
  - app/src/main/java/com/gaurav/devagent/work/Workers.kt
  - app/src/main/java/com/gaurav/devagent/data/Models.kt
---

# Daily goal reset and evening nudge

## Daily goal reset rule

`Goal.isDoneToday` compares `doneOn` with today's date. Nothing clears the ticks: they stop counting when the date changes.

## Evening nudge flow

`NudgeWorker` runs every 24 hours, first at `nudgeHour`, and needs no network. It builds one line per unfinished plan item and goal, posts "N items left today" on the `goals` channel, and refreshes the widget. If nothing is left, no notification is posted.

## Daily goal edge cases

- Empty categories are hidden on the Today screen. When there are no goals at all, it shows "Tap + to add your own daily goals".
- The Today screen's progress count and the widget's count both include plan items as well as goals.
- New installs start with one goal, "Ship 1 commit to a side project". Installs from before 2026-10-26 keep the original starter goals until deleted.
- The nudge repeats every fixed 24 hours, so it shifts by an hour after a clock change until the time is re-saved in Settings.
