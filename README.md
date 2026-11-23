# JEE Gem 💎

An offline-first study app for JEE aspirants: the official syllabus, a daily plan built from your weakest subject, a focus timer, and a revision chain that does not forget.

No accounts, no ads, no internet permission.

## Status

| Milestone | What | State |
|---|---|---|
| v0.1 | Compose shell, 3-tab prototype, CI APK | ✅ |
| **v0.2** | **Official 54-unit syllabus, storage, real planner, focus timer, revision queue, streak/heatmap, onboarding, themes** | 🚧 in progress |
| v0.3 | Revision confidence ratings, mistake notebook, Main-pattern test log, deeper analytics | ⬜ |
| v0.4 | Reminders, backup/restore, widget | ⬜ |
| v0.5 | Flashcards, formula decks | ⬜ |

## What v0.2 does

- **Onboarding** — target attempt (2027/2028/2029) + honest daily study hours. 2027 dates are loaded from official notices; unknown years say so instead of inventing dates.
- **Syllabus** — 54 official NTA units (Physics 20, Chemistry 20, Maths 14) with 200+ study topics, status chips, filters (to start / in progress / due / high yield) and per-subject coverage.
- **Today** — 3–5 targets chosen by weakest subject coverage, priority and revisions that are due; nothing reshuffles randomly.
- **Focus timer** — per-target countdown, pause/resume, logs real study minutes and flips a topic to *Learning*.
- **Revision** — spaced review at +3, +7 and +21 days, with a "due now" queue and a 7-day outlook.
- **Progress** — 12-week heatmap, streak, weekly hours, subject coverage and mastery bars, exam timeline countdown.
- **Themes** — dark by default, AMOLED black, light and system options.

## Exam facts baked in

Verified against NTA and IIT Delhi notices on 23 Nov 2026:

| Milestone | Date |
|---|---|
| JEE Main 2027 Session 1 | 20–24 and 28 January 2027 |
| JEE Main 2027 Session 2 | 2–7 April 2027 |
| JEE Advanced 2027 | 23 May 2027 |

Paper 1 pattern: 75 questions, 300 marks, 25 per subject (20 MCQ + 5 numerical), +4 / −1, 3 hours.

## Stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- minSdk 26, targetSdk 35
- Storage: DataStore with kotlinx.serialization JSON — one file, no Room/KSP
- Syllabus bundled at `app/src/main/assets/syllabus.json`

## Layout

```
app/src/main/java/app/jeegem/
  MainActivity.kt              entry point
  data/                         syllabus loader, app state, exam calendar, DataStore repository
  domain/Planner.kt            daily plan, revision ladder, coverage maths (+ unit tests)
  ui/AppViewModel.kt           state, timer, actions
  ui/JeeGemApp.kt              onboarding gate, navigation, timer/settings sheets
  ui/screens/                  Today, Syllabus, Revision, Progress, onboarding, settings
  ui/components/               rings, heatmap, chips, section headers
app/src/test/java/app/jeegem/  planner and review scheduling tests
```

## Build

CI builds and tests on every push. `jee-gem-debug` artifact carries the APK; a `v*` tag builds a release and attaches it to a GitHub Release.

```
gradle :app:assembleDebug        # APK
gradle :app:testDebugUnitTest    # planner + scheduling tests
```

### Signing

Every build is signed with one permanent keystore so APKs install as updates over each other. Secrets: `JEE_GEM_KEYSTORE_BASE64`, `JEE_GEM_STORE_PASSWORD`, `JEE_GEM_KEY_ALIAS`, `JEE_GEM_KEY_PASSWORD`. Without them, the build falls back to the throwaway debug key.

## Honesty rules

- No predicted ranks or percentiles — only measurable things (coverage, revision debt, logged hours).
- Priority badges are approximate, based on past-paper trends, and labelled as such.
- No bundled PYQs; copyrighted material stays out.
- Exam dates are stamped with a verification date and can be edited in Settings.
