---
title: DevAgent stored state
feature: app-foundation
type: data
summary: All state is one AgentState in filesDir/agent_state.json, written via temp file and rename. A file that fails to parse is silently replaced by defaults.
keywords:
  - AgentState
  - agent_state.json
  - ignoreUnknownKeys
  - kotlinx.serialization
  - DEFAULT_GOALS
  - runCatching
aliases:
  - database
  - persistence
  - saved data
questions:
  - Where is the app's data stored?
  - What happens if the state file is corrupt?
  - Is it safe to rename a field or enum in Models.kt?
sources:
  - app/src/main/java/com/gaurav/devagent/data/AgentRepository.kt
  - app/src/main/java/com/gaurav/devagent/data/Models.kt
---

# DevAgent stored state

## AgentState model

`data/Models.kt` defines `AgentState`: `goals`, `headlines`, `headlinesFetchedAt`, `headlineStatus`, `seenHeadlineUrls`, `reminders`, `checks`, `progress` (per `Track`) and `settings`.

## agent_state.json storage

State is saved to `agent_state.json` in the app's files directory. Each save writes `agent_state.json.tmp` and renames it over the original. `update()` skips the write when the state didn't change, and refreshes the widget unless called with `refreshWidget = false`.

Why JSON was chosen over a database: rationale not recorded.

## State file load failure and data loss

**`AgentRepository.init()` treats a parse failure the same as a missing file: it writes defaults over the file, with no backup and no error.** This was found by reading the code, not by seeing it happen.

The JSON reader uses `ignoreUnknownKeys` and defaults, so adding a field is safe. Renaming or removing a saved enum value, or changing a field's type, would fail parsing for existing installs and erase their goals, reminders, checks and study progress.

## Migrations for existing installs

There are none. Saved values win over code defaults, so changing `DEFAULT_GOALS` or `DEFAULT_FEEDS` only affects new installs. When the three original study-related default goals were dropped on 2026-10-26, existing installs kept them until deleted by hand.
