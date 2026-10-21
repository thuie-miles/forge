# AGENTS.md

Rules for coding agents (Claude Code, Codex and others) working in this repo. `CLAUDE.md` points here.

## What this repo is

One Android app per repo, made for the owner to install on their phone. Every push to `main` makes GitHub Actions
build a debug APK and put it on the release tagged `latest`, so the download link never changes:

```
https://github.com/<owner>/<repo>/releases/download/latest/<APK_NAME>
```

A request usually arrives as one sentence, like "make me a habit tracker". Turn it into a working app in this repo,
make sure it builds, push it, and report the link.

This repo is also a GitHub template. If it still holds the Forge placeholder (package `com.seyta.forge`, app name
"Forge") and you were asked for a different app, first do
[Starting a new app from the template](#starting-a-new-app-from-the-template).

## Where things live

| Path | What it holds |
|---|---|
| `app/` | The only Gradle module. Don't add more. |
| `app/src/main/java/<package>/MainActivity.kt` | The only Activity. It just sets the theme and the top-level screen with `setContent`. |
| `app/src/main/java/<package>/ui/<feature>/` | One folder per screen: `<Feature>Screen.kt` (composables) and `<Feature>ViewModel.kt`. |
| `app/src/main/java/<package>/ui/theme/` | The Material 3 theme. |
| `app/src/main/java/<package>/data/` | Stored data and its classes, only once the app keeps data. |
| `app/src/main/res/` | Strings (`values/strings.xml`), the window theme, the launcher icon. |
| `app/build.gradle.kts` | App id, SDK levels, version, signing, dependencies. |
| `gradle/libs.versions.toml` | Every library and plugin version. |
| `.github/workflows/build.yml` | The CI build and the `latest` release. |
| `README.md` | How the owner uses all this. Keep it true when you change how things work. |

`<package>` is the folder form of `namespace` in `app/build.gradle.kts`, e.g. `com/seyta/forge`. Kotlin files go
under `src/main/java/` (Android's default).

## How apps are built here

- **UI:** Jetpack Compose with Material 3, inside the one Activity. For more than one screen, use Navigation Compose
  (`androidx.navigation:navigation-compose`).
- **State:** each screen's state lives in a ViewModel (`androidx.lifecycle:lifecycle-viewmodel-compose`), exposed as a
  `StateFlow` and read with `collectAsStateWithLifecycle()` (`androidx.lifecycle:lifecycle-runtime-compose`).
- **Saving data:** only when the app has to remember something after it closes.
  - Settings and a few small values: DataStore Preferences (`androidx.datastore:datastore-preferences`).
  - Lists of records (habits, entries, notes): Room, with its compiler through the KSP plugin
    (`com.google.devtools.ksp`). Not kapt: it doesn't work with AGP 9's built-in Kotlin.
- **Libraries:** add one only when the app needs it, and prefer AndroidX. Put it in `gradle/libs.versions.toml` at its
  current stable version (look it up, e.g. on https://maven.google.com; don't guess). No Firebase, sign-in, accounts,
  analytics, ads or crash reporting unless the owner asks.
- **Permissions:** ask the owner before adding any `<uses-permission>`, including INTERNET, notifications, camera,
  location and storage: name the permission, say what it's for, and wait for a yes. If no one can answer (a run with
  no chat), build without it and say in your report what it would add. Libraries can bring permissions in through
  their own manifests, so after adding one, check the merged manifest:
  `grep -rh "uses-permission" app/build/intermediates/merged_manifest*`
  (`DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, from androidx.core, is expected).
- **Text:** anything the user reads goes in `res/values/strings.xml`.
- **Vague requests:** build the smallest app that does what was asked, make sensible choices, and list them in your
  report.

## Don't change these

- **Signing.** Builds are debug-signed with `app/debug.keystore`, which is committed on purpose: every build must be
  signed with the same key, or the phone refuses to install it over the previous one ("App not installed"). Don't
  replace, regenerate or delete it, and don't add release signing, other keystores or secrets.
- **versionCode.** It comes from the CI run number (`GITHUB_RUN_NUMBER`), so every build is an upgrade of the last.
  Don't hardcode it. `versionName` is `0.1.<run number>`; change the `0.1` only when the owner asks.
- **The workflow's file name.** Keep `.github/workflows/build.yml`. GitHub counts runs per workflow file, so a new name
  restarts the count at 1, and the phone won't install a lower versionCode over a higher one.
- **The shape of the build.** One module, debug builds only: no product flavors and no release build in CI.

## Before you finish: build, push, report

1. **Build:** `./gradlew assembleDebug` (Windows: `.\gradlew.bat assembleDebug`). Fix every error and build again
   until it passes. Don't fix an error by removing a feature or switching off a check. If this machine has no Android
   SDK (no `ANDROID_HOME`, and no `sdk.dir` in `local.properties`), the CI build is your build: push, then follow
   step 3 until it passes.
2. **Commit and push** to `main`, with a message that says in plain words what changed.
3. **Watch the CI run** for your commit until it's green:
   ```sh
   for i in $(seq 30); do   # a run takes a few seconds to appear after a push
     run=$(gh run list --commit "$(git rev-parse HEAD)" --json databaseId -q '.[0].databaseId')
     [ -n "$run" ] && break; sleep 5
   done
   gh run watch "$run" --exit-status
   gh run view "$run" --log-failed   # when it failed: read, fix, commit, push, watch again
   ```
4. **Report** the download link, `https://github.com/<owner>/<repo>/releases/download/latest/<APK_NAME>`
   (`gh repo view --json nameWithOwner -q .nameWithOwner` gives `<owner>/<repo>`; `APK_NAME` is set at the top of
   `.github/workflows/build.yml`), what the app does, what you assumed, and anything left for later.

## Starting a new app from the template

A repo made with "Use this template" starts as a copy of Forge. Rename it before building anything, so the new app
installs next to the others instead of over Forge:

1. Pick a short lowercase name, e.g. `habits`. The package becomes `com.seyta.habits` (keep the `com.seyta.` prefix).
2. `app/build.gradle.kts`: set `namespace` and `applicationId` to the new package.
3. Move the Kotlin files from `app/src/main/java/com/seyta/forge/` to the new package's folder, and update every
   `package` and `import` line.
4. `app/src/main/res/values/strings.xml`: set `app_name` to the app's display name.
5. Rename the theme: `ForgeTheme` in `ui/theme/Theme.kt`, and `Theme.Forge` in `res/values/themes.xml` and
   `AndroidManifest.xml`.
6. `settings.gradle.kts`: set `rootProject.name`.
7. `.github/workflows/build.yml`: set `APK_NAME` to `<name>-debug.apk`.
8. `README.md`: swap Forge's name, description and download link for the new app's.
9. Optional: a new launcher icon: the shape in `res/drawable/ic_launcher_foreground.xml`, the color
   `ic_launcher_background` in `res/values/colors.xml`.

Then build the app that was asked for. The new repo's CI makes its own `latest` release, and the publish step removes
the leftover `forge-debug.apk` from it.
