---
title: DevAgent settings and permissions
feature: app-foundation
type: config
summary: Settings tab controls headline hour (8), nudge hour (20), check interval (60 min), feeds and keywords. Manifest permissions cover network, notifications, boot and exact alarms.
keywords:
  - Settings
  - headlineHour
  - nudgeHour
  - checkIntervalMinutes
  - POST_NOTIFICATIONS
  - USE_EXACT_ALARM
  - minSdk
  - CH_REMINDERS
aliases:
  - preferences
  - permissions
  - notification channels
questions:
  - What are the default times for headlines and the nudge?
  - Which Android permissions does the app use?
sources:
  - app/src/main/java/com/gaurav/devagent/ui/SettingsScreen.kt
  - app/src/main/AndroidManifest.xml
---

# DevAgent settings and permissions

## DevAgent user settings and defaults

Stored in `Settings` in `data/Models.kt` and edited on the Settings tab:

| Setting | Default | Effect of changing it |
|---|---|---|
| `headlineHour` | 8 | Reschedules jobs |
| `nudgeHour` | 20 | Reschedules jobs |
| `checkIntervalMinutes` | 60 | Reschedules jobs |
| `feeds` | 6 feeds | Used on the next fetch |
| `aiKeywords`, `systemDesignKeywords` | See `Models.kt` | Used on the next fetch |

The Reliability section has a **Battery settings** button and a **Test notification** button.

## DevAgent Android permissions

| Permission | Used for |
|---|---|
| `INTERNET` | Feeds and checks |
| `POST_NOTIFICATIONS` | Requested at app start on Android 13+ |
| `RECEIVE_BOOT_COMPLETED` | Re-arming reminders and jobs after a reboot |
| `SCHEDULE_EXACT_ALARM` (up to SDK 32), `USE_EXACT_ALARM` | Exact reminders |

## DevAgent build targets

`minSdk` 26, `targetSdk` and `compileSdk` 35, Java 17. Dynamic color applies on Android 12+. `android:usesCleartextTraffic` isn't set.

## DevAgent notification channels

| Channel id | Used by | Importance |
|---|---|---|
| `reminders` | Task reminders, test notification | High |
| `checks` | Check alerts | High |
| `headlines` | Daily headlines | Default |
| `goals` | Today's plan, evening nudge | Default |
