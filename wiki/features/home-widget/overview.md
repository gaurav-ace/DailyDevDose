---
title: Home-screen widget
feature: home-widget
type: overview
status: implemented
summary: Jetpack Glance widget, about 60% opaque, showing today's plan, goals and top 3 reads. Checkboxes update state and titles open links. Refreshes on changes and hourly.
keywords:
  - AgentWidget
  - AgentWidgetReceiver
  - Glance
  - GlanceAppWidget
  - WidgetContent
  - ToggleGoalAction
  - ToggleTrackAction
aliases:
  - home screen
  - app widget
questions:
  - What does the widget show?
  - Which actions work on the widget?
sources:
  - app/src/main/java/com/gaurav/devagent/widget/AgentWidget.kt
  - app/src/main/res/xml/agent_widget_info.xml
---

# Home-screen widget

## Purpose of the home-screen widget

The widget puts the day's work on the home screen without opening the app, which was one of the original requests.

## Home-screen widget capabilities

- Header with "Today's plan" and a done count. Tapping it opens the app.
- One row per plan track, with a checkbox, and a title that opens the link.
- "My goals", with checkboxes.
- "Top reads", where tapping a headline opens the article.

## Home-screen widget entry points in code

- `widget/AgentWidget.kt`: `AgentWidget`, `AgentWidgetReceiver`, `ToggleGoalAction`, `ToggleTrackAction`, `WidgetContent`.
- `res/xml/agent_widget_info.xml`: size and refresh settings.
- `data/AgentRepository.kt`: `update()` refreshes the widget after state changes.

## Home-screen widget status and limits

- Goals can't be added or deleted on the widget, and **Previous** and **Skip** are only in the app.
- The transparency level is set in code, so changing it means rebuilding.
