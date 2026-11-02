---
title: Daily headlines
feature: headlines
type: overview
status: implemented
summary: Three articles each morning from RSS and Atom feeds: one AI, one system design, one from another source, never repeated. Shown in the app, widget and a notification.
keywords:
  - HeadlineEngine
  - HeadlinesWorker
  - Headline
  - Feed
  - Topic
  - RSS
  - Atom
  - HeadlineCard
aliases:
  - news
  - articles
  - top reads
  - top 3
questions:
  - Where do the daily headlines come from?
  - Can headlines be refreshed by hand?
sources:
  - app/src/main/java/com/gaurav/devagent/news/HeadlineEngine.kt
  - app/src/main/java/com/gaurav/devagent/news/Http.kt
---

# Daily headlines

## Purpose of daily headlines

Each morning the app picks three fresh articles on AI and system design, so there's something current to read alongside the study plan.

## Headline capabilities

- Scheduled fetch at `headlineHour`, 8:00 by default, with a notification.
- Manual refresh on the Today screen, without a notification.
- Automatic refresh at app start if none were fetched today, without a notification.
- Tapping a headline on the widget opens the article.
- Feeds and keywords are editable in Settings.

## Headline entry points in code

- `news/HeadlineEngine.kt`: fetching, parsing, classifying and picking.
- `news/Http.kt`: the HTTP client, which caps responses at 3 million characters.
- `work/Workers.kt`: `HeadlinesWorker`.
- `ui/TodayScreen.kt`: `HeadlineCard`, status and refresh.

## Headline status and limits

- The parser only reads titles, links and dates. There are no summaries.
- A headline with an unreadable date counts as published at time 0, so it's picked last.
- The app keeps the previous three if no new ones match.
