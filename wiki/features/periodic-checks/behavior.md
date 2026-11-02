---
title: Periodic check evaluation rules
feature: periodic-checks
type: behavior
summary: ChecksWorker evaluates each enabled check via HTTP GET, strips script and style tags, hashes visible text with SHA-256, and uses the alerting flag to notify on transitions.
keywords:
  - evaluate
  - visibleText
  - sha256
  - alerting
  - lastHash
  - runChecksNow
  - Http.get
  - cleartext
aliases:
  - false alert
  - site reported down
  - http url
questions:
  - Why did I get a page changed alert when nothing changed?
  - Why does a site show as down when it isn't?
  - How does the check decide a page changed?
  - Do disabled checks run?
sources:
  - app/src/main/java/com/gaurav/devagent/work/Workers.kt
  - app/src/main/java/com/gaurav/devagent/news/Http.kt
  - app/src/main/java/com/gaurav/devagent/work/AgentScheduler.kt
---

# Periodic check evaluation rules

## Periodic check evaluation per type

`ChecksWorker.evaluate()` runs one GET per check:

- **`SITE_DOWN`:** up means HTTP 200 to 399. Notifies on the change from up to down, and from down to up.
- **`CONTAINS_TEXT`:** a case-insensitive search of the visible text. Notifies when the text goes from absent to present. A failed request only records an error status.
- **`PAGE_CHANGED`:** compares a SHA-256 of the visible text with the last one. The first run saves a baseline without notifying. A failed request only records an error status.

Visible text comes from removing `script`, `style` and `noscript` blocks and all tags, then collapsing whitespace.

## Periodic check schedule

A new check runs once as soon as it's added. After that, `checks-periodic` runs at the interval from Settings (15, 30 or 60 minutes, or 3 or 6 hours), and needs network. "Run now" and "Run all now" use `runChecksNow`, which has no network requirement. Disabled checks are skipped by scheduled runs and "Run all now", but "Run now" on one check still runs it.

## Periodic check request limits

`Http.get` uses a 15-second connect timeout and a 20-second read timeout, sends a browser-like user agent, and reads at most 3 million characters.

## Known periodic check problems

These come from reading the code and haven't been reproduced on a device:

| Problem | Why it happens |
|---|---|
| A temporary 500 can raise a false "page changed" alert, and another when it recovers | `Http.get` returns error pages as a normal body, and `PAGE_CHANGED` doesn't check the status code |
| `CONTAINS_TEXT` can raise a repeat "found" alert | An error page without the text clears `alerting`, so the next good response alerts again. The same happens if the text really disappears and returns. |
| A manual run while offline reports the site as down | `runChecksNow` has no network requirement, unlike the scheduled job |
| `http://` URLs likely show as down | The add form accepts them, but `usesCleartextTraffic` isn't set and cleartext is blocked by default at this target SDK |
