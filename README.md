# Forge

A pipeline for Android apps you never build by hand: a coding agent writes the app, GitHub builds it, and you install
it on your phone from one link that never changes.

Forge is the placeholder app that proves the pipeline works: one screen with its name and version. This repo is also
a template: every new app gets its own repo made from it (see [Start a new app](#start-a-new-app)).

**Download on your phone:** https://github.com/thuie-miles/forge/releases/download/latest/forge-debug.apk

## Install it on your phone

The first time:

1. On the phone, open the download link in Chrome. If Chrome warns that the file might be harmful, tap
   **Download anyway**.
2. Open the downloaded file. Android says Chrome isn't allowed to install unknown apps: tap **Settings**, turn on
   **Allow from this source**, and go back. You only do this once. (The switch lives under Settings → Apps → Special
   app access → Install unknown apps → Chrome; the names vary a little between phone makers.)
3. Tap **Install**. If Play Protect warns that it doesn't recognise the app, tap **More details → Install anyway**:
   it's your own app.

After that, each new build installs the same way: open the same link, open the file, tap **Update**. The app keeps
its data.

If Android says **App not installed**, the copy on the phone was either signed with a different key (for example,
built in Android Studio on another computer) or is newer than the download. Uninstall the app once, then install it
from the link.

## How it works

```
you or an agent
   │  git push to main
   ▼
GitHub Actions  (.github/workflows/build.yml)
   ├─ build    JDK 17 + Android SDK + Gradle cache → ./gradlew assembleDebug → forge-debug.apk
   │           (also kept as a workflow artifact on the run's page)
   └─ publish  moves the "latest" tag to this commit and swaps the new APK onto the "latest" release
                 │
                 ▼
https://github.com/thuie-miles/forge/releases/download/latest/forge-debug.apk  →  your phone
```

- **When it runs:** on every push to `main`, or by hand: Actions tab → Build APK → Run workflow. A build takes about a
  minute when Gradle's cache is warm, and about three when it isn't; watch it on the Actions tab, or with
  `gh run watch`.
- **Version numbers:** every run gets the next number (1, 2, 3, ...). The app uses it as its version code, an internal
  number that has to go up for Android to accept an update, and shows it on screen as version 0.1.<number>.
- **Signing:** Android only installs an update that is signed with the same key as the app already on the phone. So
  the debug key is part of the repo (`app/debug.keystore`) and every build uses it. It's a debug key with the standard
  password `android`, not a secret. The flip side: anyone could sign an APK that installs over this app, so only
  install APKs from your own link.
- **No secrets to set up:** the workflow updates the release with GitHub's built-in token, so a fresh repo works as is.

## Start a new app

1. Make a repo from this template and clone it:
   ```sh
   gh repo create habits --template thuie-miles/forge --public --clone
   ```
   Or on GitHub: **Use this template → Create a new repository**.
2. Open Claude Code or Codex in the new folder and ask for the app, e.g. "make me a habit tracker".
3. The agent follows [AGENTS.md](AGENTS.md): it renames Forge to the new app, builds it, pushes, and reports the new
   link, e.g. `https://github.com/thuie-miles/habits/releases/download/latest/habits-debug.apk`.

Each app gets its own package name (`com.seyta.<name>`), so the apps sit side by side on the phone instead of
replacing each other.

## Build on your computer (optional)

You need JDK 17 or newer and the Android SDK; Android Studio installs both. Then, in this folder:

```sh
./gradlew assembleDebug     # Windows: .\gradlew.bat assembleDebug
```

The APK appears at `app/build/outputs/apk/debug/app-debug.apk`. A local build has version code 1, so a phone that
already has a CI build won't take it as an update.

## What's in here

| Path | What it is |
|---|---|
| `app/` | The app: Kotlin and Jetpack Compose, one module, one Activity |
| `app/debug.keystore` | The shared debug signing key (see Signing above) |
| `gradle/libs.versions.toml` | Every library and plugin version |
| `.github/workflows/build.yml` | The build and the `latest` release |
| `AGENTS.md` | Rules for coding agents working here; `CLAUDE.md` points to it |
