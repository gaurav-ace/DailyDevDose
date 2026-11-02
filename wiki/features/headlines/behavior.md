---
title: Headline fetching and selection
feature: headlines
type: behavior
summary: Feeds load in parallel. Items are filtered to unseen, then to the last 3 days, 14 days or anything. Retries 3 times if no feed responds. Remembers the last 300 URLs shown.
keywords:
  - pickTop
  - classify
  - matchesWord
  - seenHeadlineUrls
  - parseFeed
  - Result.retry
  - XmlPullParser
  - MIXED
aliases:
  - dedupe
  - no repeats
  - feed failure
  - late headlines
questions:
  - How are the three headlines chosen?
  - How does a Mixed feed decide what to keep?
  - What happens when feeds can't be reached?
  - Why did my headlines arrive late?
sources:
  - app/src/main/java/com/gaurav/devagent/news/HeadlineEngine.kt
  - app/src/main/java/com/gaurav/devagent/work/Workers.kt
---

# Headline fetching and selection

## Headline fetch flow

`HeadlineEngine.fetch()` loads all feeds in parallel. A feed that fails or returns a non-2xx code counts as failed and the others carry on.

## Headline classification rules

Feeds tagged `AI` or `SYSTEM_DESIGN` keep that topic for every item. For a `MIXED` feed, each title is matched as whole words, case-insensitively, against the AI keywords first and then the system design keywords. A title matching neither is dropped.

## Headline picking order

`pickTop()` removes URLs already seen and duplicates, then uses the newest of the first non-empty group: the last 3 days, the last 14 days, or everything. It then picks:

1. The newest AI item.
2. The newest system design item.
3. The newest item from a source not picked yet.
4. Any newest remaining items, if still short of three.

## Headline failure handling

| Situation | Result |
|---|---|
| No items, and at least one feed failed | Status "Couldn't reach any feed — will retry". Retries up to 3 times, then fails. |
| Nothing new matches | Status "No new matching headlines right now". The old headlines stay. |
| Some feeds failed but items were found | Status "N feed(s) failed". Headlines are updated. |

## Headline deduplication

The picked URLs join `seenHeadlineUrls`, which keeps the newest 300. An article can only reappear once it has dropped out of that list.

## Headline timing

WorkManager batches work to save battery, so "8:00" can arrive a few minutes late. The job repeats every fixed 24 hours, so it shifts an hour after a clock change until the time is re-saved in Settings.
