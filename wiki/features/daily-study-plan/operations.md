---
title: Editing the study plan
feature: daily-study-plan
type: operations
summary: Change lessons by editing Curriculum.kt and running build.sh. Reordering or removing items shifts saved index positions, since progress stores a position instead of a lesson id.
keywords:
  - Curriculum.kt
  - dsa()
  - Lesson
  - PRIMER
aliases:
  - add a lesson
  - change the course
questions:
  - How do I add or change a lesson?
  - Why did my track jump to a different lesson after an update?
sources:
  - app/src/main/java/com/gaurav/devagent/data/Curriculum.kt
---

# Editing the study plan

## Edit and test the curriculum

Edit the lists in `data/Curriculum.kt`, then run `./build.sh`. Use `dsa(topic, slug, title, difficulty)` for LeetCode problems and `Lesson(...)` for the others. `PRIMER`, `HELLO` and `AWS` hold shared base URLs. There's no link checker in the repo, so open new links by hand.

## Curriculum troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| After an update, a track shows a different lesson | Progress stores a position, not a lesson id, so inserting or removing earlier items shifts it | Add new items at the end of a track, or use **Previous** and **Skip** to get back |
| A link is dead | Links were verified once, on 2026-10-26 | Replace the URL and rebuild |

## Curriculum monitoring

There is none.
