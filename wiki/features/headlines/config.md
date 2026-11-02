---
title: Headline feeds and keywords
feature: headlines
type: config
summary: Six default feeds: ByteByteGo, InfoQ Architecture, Hugging Face, Simon Willison, plus Hacker News and Pragmatic Engineer as Mixed. Feeds and both keyword lists are editable in Settings.
keywords:
  - DEFAULT_FEEDS
  - DEFAULT_AI_KEYWORDS
  - DEFAULT_SD_KEYWORDS
  - hnrss
  - ByteByteGo
  - InfoQ
  - Hugging Face
aliases:
  - sources
  - add a feed
  - news sources
questions:
  - Which feeds does the app use by default?
  - How do I add a feed or change the keywords?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Models.kt
---

# Headline feeds and keywords

## Default headline feeds

| Feed | Topic |
|---|---|
| ByteByteGo | System design |
| InfoQ Architecture | System design |
| Hugging Face | AI |
| Simon Willison | AI |
| Hacker News (`hnrss.org/best`) | Mixed |
| Pragmatic Engineer | Mixed |

URLs are in `DEFAULT_FEEDS` in `data/Models.kt`. Candidate feeds were checked before being added, during the session of 2026-10-24.

## Headline settings

Settings can add or remove feeds, with a topic for each, and edit the AI and system design keyword lists. Keywords only affect `MIXED` feeds. Existing installs keep their saved lists, so changing the defaults in code doesn't reach them.
