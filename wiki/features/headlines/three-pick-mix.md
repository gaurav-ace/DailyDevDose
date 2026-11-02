---
title: One AI, one system design, one other
feature: headlines
type: design
status: implemented
summary: Three daily picks are balanced across topics and sources, with Hacker News and Pragmatic Engineer keyword-filtered because they cover many topics beyond AI and system design.
keywords:
  - pickTop
  - Topic.MIXED
  - matchesWord
  - keywords
aliases:
  - topic balance
questions:
  - Why are headlines split by topic?
  - Why are some feeds filtered by keywords?
sources:
  - app/src/main/java/com/gaurav/devagent/news/HeadlineEngine.kt
related:
  - headlines/behavior
---

# One AI, one system design, one other

## Summary

The app shows exactly three headlines a day: one AI, one system design, and one from a different source. General-purpose feeds only contribute titles matching keywords.

## Context

The request was for "top 3 headlines related to AI/system-design articles daily". Two useful feeds, Hacker News and Pragmatic Engineer, cover many unrelated topics. From the development session of 2026-10-24.

## Decision

Tag each feed `AI`, `SYSTEM_DESIGN` or `MIXED`. Items from `MIXED` feeds are kept only if their title matches a keyword. Pick by topic first, then favour a different source, and never repeat an article. Keywords match whole words, so "ai" doesn't match inside "email".

## Alternatives considered

None recorded.

## Consequences

- Topics stay balanced even when one feed publishes much more than the others.
- Keyword matching only sees titles, so it can miss relevant articles and keep irrelevant ones.
- If no fresh item exists for a topic, the remaining slots are filled from anything available.

## Open questions

None.

## History

- 2026-10-24: Decided when headlines were added.
