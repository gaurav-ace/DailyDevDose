---
title: Exact alarms for reminders, WorkManager for the rest
feature: task-reminders
type: design
status: implemented
summary: Reminders use AlarmManager exact alarms because they must fire on time. Headlines, checks and the nudge use WorkManager and accept batching delays of a few minutes.
keywords:
  - USE_EXACT_ALARM
  - SCHEDULE_EXACT_ALARM
  - setExactAndAllowWhileIdle
  - WorkManager
aliases:
  - AlarmManager vs WorkManager
questions:
  - Why do reminders use AlarmManager instead of WorkManager?
sources:
  - app/src/main/java/com/gaurav/devagent/reminders/Reminders.kt
  - app/src/main/AndroidManifest.xml
related:
  - task-reminders/behavior
---

# Exact alarms for reminders, WorkManager for the rest

## Summary

Reminders use `AlarmManager`. All other scheduled work uses WorkManager.

## Context

The README and the development session of 2026-10-24 record that WorkManager batches jobs to save battery, so "8:00" work can arrive minutes late, while a reminder for a task has to fire at its set time.

## Decision

Use `setExactAndAllowWhileIdle` for reminders, along with `USE_EXACT_ALARM` and, up to SDK 32, `SCHEDULE_EXACT_ALARM`. Because alarms don't survive a reboot, re-arm them with `BootReceiver`, and at each app start. Use WorkManager for headlines, checks and the nudge, where a small delay is acceptable and network constraints help.

## Alternatives considered

- **WorkManager for reminders as well:** its batching can't guarantee an exact time.

## Consequences

- Reminders need their own recovery after a reboot, which WorkManager handles itself.
- Exact alarm permissions are needed, with an inexact fallback when they aren't granted.

## Open questions

None.

## History

- 2026-10-24: Decided when reminders were added.
