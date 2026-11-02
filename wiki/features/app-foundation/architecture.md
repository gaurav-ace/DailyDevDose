---
title: DevAgent architecture
feature: app-foundation
type: architecture
summary: UI, workers, alarm receivers and widget share one in-process AgentRepository StateFlow. WorkManager runs batched jobs, AlarmManager runs reminders, Glance renders the widget.
keywords:
  - AgentRepository
  - StateFlow
  - WorkManager
  - AlarmManager
  - Glance
  - Compose
  - AgentScheduler
  - KEEP
  - CANCEL_AND_REENQUEUE
aliases:
  - how the app is built
  - background jobs
questions:
  - How do the UI and workers share data?
  - Which background jobs does the app schedule?
  - Why do morning jobs run an hour off after a clock change?
sources:
  - app/src/main/java/com/gaurav/devagent/work/AgentScheduler.kt
  - app/src/main/java/com/gaurav/devagent/data/AgentRepository.kt
  - app/build.gradle.kts
---

# DevAgent architecture

## DevAgent components

| Concern | Mechanism | File |
|---|---|---|
| State | One JSON file behind a `StateFlow` | `data/AgentRepository.kt` |
| Scheduled work | WorkManager | `work/AgentScheduler.kt`, `work/Workers.kt` |
| Timed reminders | `AlarmManager` exact alarms | `reminders/Reminders.kt` |
| Widget | Jetpack Glance | `widget/AgentWidget.kt` |
| App UI | Jetpack Compose, Material 3 | `ui/` |
| Notifications | Four channels | `notify/Notifier.kt` |
| Network | Blocking `HttpURLConnection` | `news/Http.kt` |

## DevAgent background job flow

`AgentScheduler.scheduleAll()` registers three unique periodic jobs:

| Job name | Worker | Period | Needs network |
|---|---|---|---|
| `headlines-daily` | `HeadlinesWorker` | 24 h, first run at `headlineHour` | Yes |
| `goals-nudge` | `NudgeWorker` | 24 h, first run at `nudgeHour` | No |
| `checks-periodic` | `ChecksWorker` | `checkIntervalMinutes`, at least 15 | Yes |

App start and reboot use the `KEEP` policy, so existing schedules stay. Changing a time or interval in Settings uses `CANCEL_AND_REENQUEUE`. One-off runs use `headlines-now` and `checks-now`.

## DevAgent architecture dependencies

Compose BOM 2024.12.01, WorkManager 2.10.0, Glance 1.1.1, kotlinx-serialization-json 1.7.3. No third-party HTTP or XML library: `HttpURLConnection` and Android's `XmlPullParser` do that work.

## DevAgent architecture invariants

- Every state change goes through `AgentRepository.update()` or `edit()`, which serialize writes with a mutex and save the file.
- Every entry point that can start without the UI (workers, receivers, widget actions) calls `AgentRepository.init(context)` first, and `init` is idempotent.
- Time-of-day jobs repeat every fixed 24 hours, so they shift by an hour when the clocks change, until rescheduled.
