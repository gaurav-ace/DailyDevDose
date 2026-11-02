---
title: Widget refresh and interactions
feature: home-widget
type: behavior
summary: AgentWidget collects the repository StateFlow. Repository updates call updateAll, and the system refreshes it hourly, so the new day's plan can appear up to an hour after midnight.
keywords:
  - updateAll
  - updatePeriodMillis
  - refreshWidget
  - actionRunCallback
  - LazyColumn
aliases:
  - widget not updating
  - stale widget
questions:
  - Why does the widget still show yesterday's plan after midnight?
  - When does the widget refresh?
sources:
  - app/src/main/java/com/gaurav/devagent/widget/AgentWidget.kt
  - app/src/main/java/com/gaurav/devagent/data/AgentRepository.kt
---

# Widget refresh and interactions

## Widget refresh rules

`AgentWidget` collects `AgentRepository.state`. It redraws when:

- `AgentRepository.update()` runs, unless called with `refreshWidget = false`. Reminder and check updates and settings saves skip the refresh, since the widget doesn't show them.
- `HeadlinesWorker` or `NudgeWorker` runs.
- The system's hourly update runs (`updatePeriodMillis` is 3600000).

Nothing fires at midnight, so the next day's plan can take up to about an hour to appear.

## Widget tap actions

| Tap | Result |
|---|---|
| Header | Opens `MainActivity` |
| Plan checkbox | `ToggleTrackAction`, then `toggleTrack` |
| Plan title | Opens the lesson URL |
| Goal checkbox | `ToggleGoalAction`, then `toggleGoal` |
| Headline | Opens the article URL |

## Widget edge cases

With no headlines it shows "No headlines yet — open the app to refresh". The widget actions call `AgentRepository.init()` first, because they can run when the app isn't already in memory.
