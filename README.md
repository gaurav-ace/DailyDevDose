# DevAgent

A personal Android agent for your phone:

- **Daily study plan** with three tracks that work like a course. An item stays until you tick it done, then the next one appears the following day. Content lives in `data/Curriculum.kt`.
  - **DSA**: 132 free LeetCode problems in NeetCode-150 order, grouped by pattern.
  - **System design**: 40 items, going from fundamentals to building blocks to reliability to full design problems.
  - **AI course for beginners**: 52 lessons in 6 modules. Foundations → how LLMs work → using LLMs → RAG and vector DBs → agents and MCP → production and papers.
- **Your own daily goals** (optional extras). They reset every day, and you can tick them off in the app or on the home-screen widget.
- **Top 3 headlines** each morning from AI and system design RSS feeds: one AI, one system design, and one more from a third source. You don't see the same article twice.
- **Periodic checks** on URLs: site down/up, page contains some text, or page content changed.
- **Task reminders** at an exact time, one-off or daily, and restored after a reboot.
- **Evening nudge** listing the goals you haven't finished yet.
- **Home-screen widget** with goal checkboxes and today's headlines.

## Toolchain (no Android Studio)

Installed under `~/android-dev` in WSL, and added to `~/.bashrc`:

| Tool | Location |
|---|---|
| JDK 17 (Temurin) | `~/android-dev/jdk` |
| Android SDK (platform 35, build-tools 35, platform-tools/adb) | `~/android-dev/sdk` |
| Gradle 8.11.1 (the project uses `./gradlew`) | `~/android-dev/gradle-8.11.1` |

## Build

```bash
cd /mnt/d/android_projects/DevAgent
./build.sh            # -> D:\android_projects\DevAgent.apk
./build.sh install    # build + adb install to a connected phone
```

## Install on your phone

**Option A: copy the APK.** Send `D:\android_projects\DevAgent.apk` to your phone (USB, Drive, Telegram…), open it, and allow "Install unknown apps" when asked.

**Option B: wireless adb from WSL (Android 11+).** This is best while you're developing.
1. On the phone, go to Settings → About → tap *Build number* 7× to enable Developer options.
2. Turn on Developer options → *Wireless debugging* → *Pair device with pairing code*.
3. In WSL:
   ```bash
   adb pair <phone-ip>:<pair-port>      # enter the 6-digit code
   adb connect <phone-ip>:<port>        # port shown on the Wireless debugging screen
   ./build.sh install
   ```
4. Read logs with `adb logcat | grep -i devagent`.

## After installing

1. Open the app and allow notifications.
2. Long-press the home screen → Widgets → DevAgent → drag it onto the screen.
3. In Settings, tap **Battery settings** and set DevAgent to *Unrestricted*. Xiaomi, Samsung and OnePlus phones often delay or kill background work otherwise.
4. Tap **Test notification** to confirm notifications arrive.

## How it works

| Feature | Mechanism | File |
|---|---|---|
| Storage | One JSON file + `StateFlow`, shared by UI, workers and widget | `data/AgentRepository.kt` |
| Headlines | `HeadlinesWorker`, daily at the configured hour (WorkManager), RSS/Atom parsing + keyword classification | `news/HeadlineEngine.kt`, `work/Workers.kt` |
| Checks | `ChecksWorker`, every 15 min–6 h; notifies only on state transitions | `work/Workers.kt` |
| Goal nudge | `NudgeWorker`, daily in the evening | `work/Workers.kt` |
| Reminders | `AlarmManager` exact alarms + `BootReceiver` | `reminders/Reminders.kt` |
| Widget | Jetpack Glance | `widget/AgentWidget.kt` |
| UI | Jetpack Compose, Material 3 | `ui/` |

Feeds and keywords can be edited in Settings. A feed tagged *Mixed* (Hacker News, for example) only contributes headlines whose titles match the AI or system design keywords.

Because WorkManager batches jobs to save battery, the "8:00" headlines may arrive a few minutes late. Reminders use exact alarms and fire on time.
