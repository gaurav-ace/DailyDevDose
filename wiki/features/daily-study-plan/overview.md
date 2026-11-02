---
title: Daily study plan
feature: daily-study-plan
type: overview
status: implemented
summary: Three fixed tracks (132 LeetCode DSA problems, 40 system design items, 52 beginner AI lessons). One linked item per track each day, advancing only once ticked done.
keywords:
  - Curriculum
  - Track
  - Lesson
  - DailyItem
  - TrackProgress
  - PlanCard
  - NeetCode
  - LeetCode
aliases:
  - course
  - study track
  - daily todo
  - DSA question
  - AI course
questions:
  - What does the daily study plan contain?
  - How many items does each track have?
  - What happens at the end of a track?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Curriculum.kt
  - app/src/main/java/com/gaurav/devagent/ui/TodayScreen.kt
---

# Daily study plan

## Purpose of the daily study plan

The plan gives one thing to study per track each day, directly on the widget and the Today screen, and works like a course. It was requested with one DSA question link, one system design article or task, and an AI topic taught to a beginner, covering concepts like LLMs, agentic AI, RAG and vector databases.

## Study plan tracks

| Track | Items | Content |
|---|---|---|
| `DSA` | 132 | Free LeetCode problems in NeetCode-150 order, grouped by pattern, easy to hard within each group |
| `SYSTEM_DESIGN` | 40 | Fundamentals, building blocks, reliability, then full design problems |
| `AI` | 52 | Six modules: foundations, how LLMs work, using LLMs, RAG and vector databases, agentic AI, production and papers |

## Study plan entry points in code

- `data/Curriculum.kt`: all content, plus `currentIndex`, `todayItems`, `toggled` and `moved`.
- `data/AgentRepository.kt`: `toggleTrack`, `moveTrack`.
- `ui/TodayScreen.kt`: `PlanCard`, with **Open**, **Previous** and **Skip**.
- `widget/AgentWidget.kt`: compact plan rows with a checkbox and link.
- `work/Workers.kt`: the morning "Today's plan" notification and the evening nudge.

## Study plan status and limits

- Content is compiled in, so changing it means editing `Curriculum.kt` and rebuilding.
- A track wraps back to its first item after the last one.
- The link checker used to verify URLs wasn't added to the repo, so links aren't re-checked and can rot.
