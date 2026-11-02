---
title: Command-line Android toolchain
feature: app-foundation
type: design
status: implemented
summary: Build with JDK 17, Android SDK 35, Gradle 8.11.1, AGP 8.7.3 and Kotlin 2.0.21 from WSL in ~/android-dev, because Android Studio wasn't installed at the start.
keywords:
  - Temurin
  - JDK 17
  - AGP
  - Gradle
  - Kotlin
  - android-dev
  - cmdline-tools
aliases:
  - no Android Studio
  - toolchain
  - setup
questions:
  - Why is there no Android Studio project setup?
  - Which tool versions does the build use?
sources:
  - build.sh
  - build.gradle.kts
  - gradle/wrapper/gradle-wrapper.properties
related:
  - app-foundation/operations
---

# Command-line Android toolchain

## Summary

DevAgent is developed and built entirely from the command line in WSL, with no Android Studio.

## Context

The project began with Android Studio not installed and neither Java nor the Android SDK on the machine. Sudo wasn't available without a password, so everything had to install in the home directory. This comes from the development session of 2026-10-24.

## Decision

Install Temurin JDK 17, the Android command-line SDK (platform and build-tools 35) and Gradle 8.11.1 under `~/android-dev`, and add them to `~/.bashrc`. The project uses `./gradlew`, with AGP 8.7.3 and Kotlin 2.0.21, wrapped by `build.sh`. These versions were picked as proven and stable.

## Alternatives considered

- **Android Studio:** not installed at the start, and installing it wasn't weighed. The request was for help with setup and development without it being there.

## Consequences

- There is no emulator here unless the user joins the `kvm` group, so the phone has been the only way to test.
- There's no IDE preview or layout tooling.
- The build output is a debug APK. Publishing to the Play Store would need signing work first.

## Open questions

None.

## History

- 2026-10-24: Toolchain chosen and installed.
