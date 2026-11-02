---
title: Reminder scheduling rules
feature: task-reminders
type: behavior
summary: setExactAndAllowWhileIdle when allowed, else inexact. Daily reminders add fixed 24-hour steps, so they drift an hour after clock changes. rescheduleAll runs on boot and app start.
keywords:
  - setExactAndAllowWhileIdle
  - canScheduleExactAlarms
  - nextOccurrence
  - rescheduleAll
  - BOOT_COMPLETED
  - MY_PACKAGE_REPLACED
  - DAY_MS
aliases:
  - daylight saving
  - DST
  - missed reminder
  - alarm drift
questions:
  - Why is my daily reminder an hour off after the clocks change?
  - What happens to a reminder missed while the phone was off?
  - What happens if exact alarms aren't allowed?
sources:
  - app/src/main/java/com/gaurav/devagent/reminders/Reminders.kt
---

# Reminder scheduling rules

## Reminder firing flow

`ReminderReceiver` looks up the reminder, posts the notification on the high-importance `reminders` channel, and then either moves a daily reminder to its next occurrence and re-arms it, or disables a one-off.

## Reminder alarm precision

`ReminderScheduler.schedule()` uses `setExactAndAllowWhileIdle` on Android below 12, or when `canScheduleExactAlarms()` is true. Otherwise it falls back to the inexact `setAndAllowWhileIdle`, with no message to the user.

## Reminder recovery after reboot

`rescheduleAll()` runs from `BootReceiver` (`BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`) and from each `MainActivity` start:

- A daily reminder moves to its next future time.
- A one-off whose time passed is disabled and shown as "Missed: ...".
- All reminders are then scheduled again.

## Daily reminder drift after a clock change

`nextOccurrence()` adds whole 24-hour periods to the stored timestamp instead of using the local time of day. After a daylight-saving change, a daily reminder fires an hour off and stays that way. This comes from reading the code and hasn't been reproduced on a device.

## Reminder edge cases

- The add dialog rejects a one-off time in the past.
- A one-off whose time has passed has its switch disabled.
