---
title: Project learnings
feature: project
type: learning
summary: Lessons across DevAgent: FlowRow opt-in, no emulator in WSL without kvm, saved state overriding code defaults.
---

# Project learnings

## 2026-10-26: Changing a default in code doesn't reach existing installs

State is saved in full, including defaults, so saved values win over `DEFAULT_GOALS` and `DEFAULT_FEEDS`. Dropping starter goals left the old ones on the installed app until they were deleted by hand. Any default change needs a migration if it has to reach existing users.

**How found:** discovery

## 2026-10-24: FlowRow needs a project-wide opt-in

The first build failed with three errors, all from `FlowRow` being marked experimental. `app/build.gradle.kts` passes `-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi` to the compiler to fix them.

**How found:** bug

## 2026-10-24: A headless emulator in WSL needs the kvm group

`/dev/kvm` exists, but the user must be in the `kvm` group, and adding them needs sudo. Run `sudo usermod -aG kvm $USER`, then `wsl --shutdown` from Windows. Without this, a real phone is the only way to run the app.

**How found:** discovery
