---
title: Task reminders
feature: task-reminders
type: overview
status: implemented
summary: One-off or daily task notifications at an exact time via AlarmManager, surviving reboots through BootReceiver. Missed one-off reminders show as Missed at the next start.
keywords:
  - Reminder
  - ReminderScheduler
  - ReminderReceiver
  - BootReceiver
  - RemindersScreen
  - AlarmManager
aliases:
  - alarm
  - task notification
  - notify me at
questions:
  - How do reminders work?
  - Do reminders survive a reboot?
sources:
  - app/src/main/java/com/gaurav/devagent/reminders/Reminders.kt
  - app/src/main/java/com/gaurav/devagent/ui/RemindersScreen.kt
---

# Task reminders

## Purpose of task reminders

Reminders notify about a task at a specific time. This is the "notifications when needed for any particular task" part of the original request.

## Task reminder capabilities

- One-off or daily, with a title and optional note.
- Turn on or off, or delete, from the Reminders tab.
- Restored after a reboot or app update.

## Task reminder entry points in code

- `reminders/Reminders.kt`: `ReminderScheduler`, `ReminderReceiver`, `BootReceiver`.
- `ui/RemindersScreen.kt`: the list and add dialog.
- `ui/MainActivity.kt`: re-arms all alarms at each app start.

## Task reminder status and limits

- Daily reminders drift by an hour after a clock change. See `behavior.md`.
- There are no weekly or custom repeats.
