# Red Wings Widget

Detroit Red Wings NHL schedule widget + companion app: next-game countdown, last result,
and division standings on your home screen and in a small app. Deliberately tight in scope —
no AI, no weather, no roster bloat.

Schedule and standings data comes from the public NHL API
(`https://api-web.nhle.com`, e.g. club schedule for team `DET` and the current standings
endpoint). The app caches responses in Room so the widget stays useful offline.

## Build

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`. Versioned copies can be
produced with `./gradlew packageVersionedApk` (version comes from `version.properties`).

## Add the widget

1. Install the debug (or release) APK on your device.
2. Long-press the home screen → Widgets → **Red Wings Schedule Widget**.
3. Resize as desired; the widget refreshes on boot, app update, and each data sync.

## Versioning

CalVer: `version.properties` holds `VERSION_MAJOR=YY`, `VERSION_MINOR=M`,
`VERSION_PATCH=D`, `VERSION_BUILD=N` (e.g. `26.9.23.1`), so releases sort with the date.
`versionName` is `YY.M.D.N` and `versionCode` is derived arithmetically in
`app/build.gradle.kts`.
