---
title: Daily goals
feature: daily-goals
type: overview
status: implemented
summary: User-defined recurring goals in Coding, System Design and AI categories. Ticks clear by date each day. One starter goal, add and delete only, no editing.
keywords:
  - Goal
  - GoalCategory
  - DEFAULT_GOALS
  - toggleGoal
  - GoalRow
  - AddGoalDialog
  - NudgeWorker
aliases:
  - todo
  - habits
  - my goals
  - evening reminder
questions:
  - What are daily goals, and how do they differ from the study plan?
  - Can a goal be edited?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Models.kt
  - app/src/main/java/com/gaurav/devagent/ui/TodayScreen.kt
related:
  - daily-study-plan/overview
---

# Daily goals

## Purpose of daily goals

Daily goals are the user's own recurring tasks, kept alongside the study plan. They were part of the original request, and became optional extras once the study plan covered DSA, system design and AI.

## Daily goal capabilities

- Add a goal with a category (`CODING`, `SYSTEM_DESIGN`, `AI`) on the Today screen.
- Tick it in the app or on the widget.
- Delete it in the app.
- Get an evening nudge listing what's unfinished.

## Daily goal entry points in code

- `data/Models.kt`: `Goal`, `GoalCategory`, `DEFAULT_GOALS`.
- `data/AgentRepository.kt`: `toggleGoal`.
- `ui/TodayScreen.kt`: `GoalRow`, `AddGoalDialog`.
- `widget/AgentWidget.kt`: `ToggleGoalAction`.
- `work/Workers.kt`: `NudgeWorker`.

## Daily goal status and limits

- Goal text can't be edited. To change one, delete it and add a new one.
- There's no history or streak.
