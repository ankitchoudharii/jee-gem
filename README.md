# JEE Gem 💎

An offline-first study app for JEE aspirants: syllabus tracking, a daily plan, and revision scheduling. No accounts, no internet permission, no nonsense.

## Status

| Milestone | What | State |
|---|---|---|
| M0 | Project skeleton, navigation, CI APK build | 🚧 in progress |
| M1 | Onboarding, syllabus map, chapter status | ⬜ |
| M2 | Daily plan generation, check-off, streak | ⬜ |
| M3 | Coverage dashboard, revision queue | ⬜ |
| M4 | Settings, JSON backup/restore, icon, release signing | ⬜ |
| M5 | PYQ / mock test engine, formula flashcards | ⬜ |

## Stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- minSdk 26, targetSdk 35
- Storage: DataStore + kotlinx.serialization JSON (fully offline, no Room/KSP)
- Syllabus tree bundled at `app/src/main/assets/syllabus.json` (85 chapters)

## Layout

```
app/src/main/java/app/jeegem/
  MainActivity.kt          entry point
  ui/JeeGemApp.kt          bottom nav + nav host
  ui/HomeScreens.kt        Today / Syllabus / Progress
  ui/theme/Theme.kt        colors and Material 3 theme
app/src/main/assets/syllabus.json
.github/workflows/android.yml
```

## Build

CI builds the debug APK on every push and uploads it as the `jee-gem-debug` artifact. Pushing a `v*` tag builds a release APK and attaches it to a GitHub Release.

Local build (Linux/macOS with JDK 17 + Android SDK):

```
./gradlew :app:assembleDebug
```

### Signing

Every build is signed with one permanent keystore so APKs install as updates over each other. The keystore lives in Actions secrets, never in the repo:

| Secret | Meaning |
|---|---|
| `JEE_GEM_KEYSTORE_BASE64` | base64 of the `.jks` file |
| `JEE_GEM_STORE_PASSWORD` | keystore password |
| `JEE_GEM_KEY_ALIAS` | key alias |
| `JEE_GEM_KEY_PASSWORD` | key password |

Without those secrets the build falls back to the throwaway debug key.

## Roadmap notes

- Planner picks 3–5 daily targets from lowest-coverage chapters plus revisions that are due.
- Marking a chapter as *Learning* schedules revisions at +3, +7 and +21 days.
- All state stays on device; backup/restore will be a JSON export/import in M4.
