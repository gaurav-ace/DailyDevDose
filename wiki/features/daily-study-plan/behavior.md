---
title: Study plan progression rules
feature: daily-study-plan
type: behavior
summary: TrackProgress stores an index and doneOn date. Today's item advances the day after it was ticked; Skip and Previous move without marking done; the track wraps at the end.
keywords:
  - currentIndex
  - toggled
  - moved
  - doneOn
  - toggleTrack
  - moveTrack
  - todayItems
  - HeadlinesWorker
aliases:
  - skip a day
  - tick done
  - next lesson
questions:
  - When does the next item appear?
  - What happens if I miss a day?
  - What do Skip and Previous do?
  - Why does the widget still show yesterday's item after midnight?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Curriculum.kt
---

# Study plan progression rules

## Study plan rules for today's item

Each track stores `TrackProgress(index, doneOn)`. `Curriculum.currentIndex()` computes today's item whenever it's needed:

- If `doneOn` is set and isn't today, the item shown is `index + 1`.
- Otherwise the item shown is `index`.
- The result wraps with `mod` over the track's size.

No midnight job moves anything. The next item appears because the date changed.

## Study plan actions

| Action | Function | Result |
|---|---|---|
| Tick | `toggled` | Saves the current index with `doneOn = today` |
| Untick the same day | `toggled` | Clears `doneOn` |
| Skip or Previous | `moved` | Moves by one and clears `doneOn`, so nothing counts as done |

A missed day changes nothing, because an unticked item just stays.

## Study plan notifications

- **Morning:** the scheduled `HeadlinesWorker` run posts "Today's plan" on its first attempt, before fetching headlines. That worker requires network, so with no connection the plan notification waits as well.
- **Evening:** `NudgeWorker` lists unfinished plan items along with unfinished goals, and posts nothing if all are done.

## Study plan edge cases

- `today()` uses the device's local date, so changing the date or time zone changes what counts as today.
- The widget can show the old item for up to about an hour after midnight, because it refreshes hourly. Opening the app shows the new one straight away.
