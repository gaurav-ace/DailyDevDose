---
title: Periodic checks
feature: periodic-checks
type: overview
status: implemented
summary: Background URL watching with three alert types (site down, page contains text, page changed), run every 15 minutes to 6 hours and notifying only when the state changes.
keywords:
  - ChecksWorker
  - Check
  - CheckType
  - SITE_DOWN
  - CONTAINS_TEXT
  - PAGE_CHANGED
  - ChecksScreen
aliases:
  - monitoring
  - uptime check
  - website watcher
  - page watcher
questions:
  - What are periodic checks for?
  - What can a check watch for?
  - Why doesn't a check see text that's visible in my browser?
sources:
  - app/src/main/java/com/gaurav/devagent/work/Workers.kt
  - app/src/main/java/com/gaurav/devagent/ui/ChecksScreen.kt
---

# Periodic checks

## Purpose of periodic checks

Checks watch a web page in the background so nobody has to keep opening it. Each has a URL and a condition, and a notification comes only when something changes. Examples include a deployed side project going down, a careers page showing a job title, a product page showing "In stock", or an exam results page changing.

## Periodic check types

| `CheckType` | Notifies when |
|---|---|
| `SITE_DOWN` | The site stops responding or returns an error, and again when it recovers |
| `CONTAINS_TEXT` | The text appears on the page |
| `PAGE_CHANGED` | The page's visible text changes |

## Periodic check entry points in code

- `work/Workers.kt`: `ChecksWorker`, with `evaluate()` holding the rules.
- `ui/ChecksScreen.kt`: list, add dialog, and "Run now" and "Run all now".
- `work/AgentScheduler.kt`: `checks-periodic` and `runChecksNow`.
- `data/Models.kt`: `Check`, `CheckType`.

## Periodic check status and limits

- Reads raw HTML only and doesn't run JavaScript, so script-rendered pages may not contain the text a browser shows.
- Pages behind a login don't work.
- `PAGE_CHANGED` fires too often on pages with timestamps, ads or rotating content.
- See the known problems in `behavior.md`.
