---
title: DevAgent app foundation
feature: app-foundation
type: overview
status: implemented
summary: Personal Android app: Compose UI with 4 tabs, one JSON state file, WorkManager and exact alarms, built without Android Studio. Lists the ideas never built.
keywords:
  - DevAgent
  - AgentApp
  - MainActivity
  - com.gaurav.devagent
  - Tab
aliases:
  - agent app
  - DailyDevDose
questions:
  - What does DevAgent do and which features does it have?
  - Where does the app start up?
  - Has the app been tested on a real device?
  - Which ideas were discussed but not built?
sources:
  - app/src/main/java/com/gaurav/devagent/AgentApp.kt
  - app/src/main/java/com/gaurav/devagent/ui/MainActivity.kt
  - README.md
---

# DevAgent app foundation

## Purpose of DevAgent

DevAgent is a single-user Android app that acts as a personal agent on the phone. It was requested with four capabilities: notifications for tasks, periodic checks on chosen items, a home-screen widget with daily learning goals, and three AI or system design headlines a day. A daily study plan was added later.

This feature covers what the others share. Each capability has its own wiki feature: `daily-study-plan`, `daily-goals`, `headlines`, `periodic-checks`, `task-reminders` and `home-widget`.

## DevAgent entry points in code

- `AgentApp.onCreate()`: loads state, creates notification channels, schedules background work.
- `ui/MainActivity.kt`: four tabs (Today, Reminders, Checks, Settings). Each start re-arms reminders, refreshes headlines if none were fetched today, and asks for notification permission on Android 13+.
- `data/AgentRepository.kt`: the single source of state.
- `work/AgentScheduler.kt`: all WorkManager scheduling.
- `notify/Notifier.kt`: all notifications.

## DevAgent status and project-wide limits

- **Device testing:** as of the last development session (2026-10-26) the app had only been compiled, never run on a phone or emulator by Claude. Later results on a real phone aren't recorded.
- **No automated tests:** the repo has no `app/src/test` or `app/src/androidTest`.
- **Debug builds only:** `build.sh` produces a debug APK, which can't go on the Play Store as it is.
- **Data-loss risk:** a state file that fails to parse is replaced by defaults. See `data.md`.

## Ideas discussed but not built

None of these exist in the code:

- Claude summaries of the three daily articles
- A goal streak counter
- Tracking progress through GitHub or LeetCode
- An "explain this lesson" button using Claude, which would need an API key
- Check types for a new GitHub release or a new blog post
