---
title: Building and running DevAgent
feature: app-foundation
type: operations
summary: Build with ./build.sh from WSL (debug APK copied to D:\android_projects), install with ./build.sh install over adb. Battery Unrestricted is needed on Xiaomi, Samsung and OnePlus.
keywords:
  - build.sh
  - gradlew
  - adb
  - APK
  - logcat
  - Unrestricted
  - kvm
aliases:
  - install
  - deploy
  - battery optimization
questions:
  - How do I build and install the app?
  - Why are background jobs late on my phone?
  - How do I run an emulator in WSL?
sources:
  - build.sh
  - README.md
---

# Building and running DevAgent

## Build and install DevAgent

```bash
./build.sh            # builds, then copies the APK to ../DevAgent.apk
./build.sh install    # also runs adb install -r on a connected phone
```

`build.sh` defaults `JAVA_HOME` and `ANDROID_HOME` to `~/android-dev/jdk` and `~/android-dev/sdk`. The first build took about 7 minutes and later ones about a minute. Installing over the old APK keeps user data. The README covers wireless adb, and logs are at `adb logcat | grep -i devagent`.

## First-run setup on a phone

1. Allow notifications.
2. Long-press the home screen, choose Widgets, then DevAgent.
3. In Settings, tap **Battery settings** and set DevAgent to Unrestricted.
4. Tap **Test notification**.

## Troubleshooting DevAgent background work

| Symptom | Cause | Fix |
|---|---|---|
| Checks or headlines run late or not at all | Some phones (Xiaomi, Samsung, OnePlus) delay or kill background work | Set battery usage to Unrestricted |
| 8:00 headlines arrive a few minutes late | WorkManager batches jobs to save battery. This is expected. | None needed |
| Times are an hour off after a clock change | Jobs and daily reminders repeat every fixed 24 hours | Re-save the time in Settings; see `task-reminders` for reminders |
| No emulator available in WSL | `/dev/kvm` needs the `kvm` group, which needs sudo | `sudo usermod -aG kvm $USER`, then `wsl --shutdown` from Windows |

## DevAgent monitoring

There is none beyond `adb logcat` and the status text on the Today and Checks screens.
