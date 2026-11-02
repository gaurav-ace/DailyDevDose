---
title: Periodic checks learnings
feature: periodic-checks
type: learning
summary: Lessons from building periodic checks: what they suit, script-rendered pages, and noisy pages.
---

# Periodic checks learnings

## 2026-10-26: What are periodic checks for?

They watch a page in the background and notify only when something changes. Good uses are a deployed side project going down, a careers page showing a job title, a product page showing "In stock", a releases page showing a version, or a results page changing.

**How found:** question

## 2026-10-26: Checks see raw HTML, not what the browser shows

No JavaScript runs, so pages that load content with scripts, including many job boards, may not contain the text visible in a browser. Pages behind a login don't work either. Try a simple static page first if a check seems wrong.

**How found:** discovery

## 2026-10-26: Page changed is noisy on busy pages

Timestamps, ads and rotating content change the visible text, so these pages count as changed on nearly every check. It works best on simple, mostly static pages.

**How found:** discovery
