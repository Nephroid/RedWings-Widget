# Red Wings Widget

Detroit Red Wings NHL schedule widget + companion app: next-game countdown, last result,
and division standings on your home screen and in a small app. Deliberately tight in scope —
no AI, no weather, no roster bloat.

Schedule and standings data comes from the public NHL API
(`https://api-web.nhle.com`, e.g. club schedule for team `DET` and the current standings
endpoint). The app caches responses in Room so the widget stays useful offline.

## Official Red Wings Jersey Themes

The widget and companion app include 5 authentic Red Wings jersey themes, reflecting historic sweaters across franchise history:

1. **🏛️ Heritage 1926**: Inaugural Detroit Cougars & 2014 Winter Classic vintage cream (`#F7F3EA`), deep ink (`#1A1210`), and classic red trim.
2. **🔴 Home Red**: Little Caesars Arena home sweater with rich hockey red gradient, white piping border, and crisp pure white typography.
3. **⚪ Away White**: Road white sweater with clean ice-white background, Red Wings Red border, and high-contrast obsidian black text.
4. **⚫ Reverse Retro 2.0**: 2022 Adidas alternate black sweater with deep matte black base, vibrant red piping, and pure white text.
5. **⭐ Stadium Series**: 2016 Coors Field special alternate with brushed slate base, dual silver/red borders, and crisp white typography.

### Dark Theme Text Legibility & WCAG AAA
All dark themes (Home Red, Reverse Retro 2.0, Stadium Series) use **100% pure white (`#FFFFFF`)** text for titles, countdowns, and opponent matchups. This permanently resolves legacy low-contrast red-on-black text issues and eliminates chromostereopsis, delivering contrast ratios up to **19.7:1** and achieving **WCAG 2.1 Level AAA** compliance. For in-depth color tokens and contrast tables, see [docs/JERSEY_THEMES.md](docs/JERSEY_THEMES.md).

### Widget & App Theme Synchronization
Theme selection is synchronized between the home screen widget and the companion app via shared preferences (`RedWingsPrefs`, key `widget_theme_index`):
- Tap the theme toggle chip directly on the widget to cycle through all 5 authentic jersey styles.
- The companion app (`AppJersey` / `JerseyPalette`) and home screen widget (`WidgetTheme`) stay aligned to the same jersey style.

## Build

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`. Versioned copies can be
produced with `./gradlew packageVersionedApk` (version comes from `version.properties`).

## Add the Widget

1. Install the debug (or release) APK on your device.
2. Long-press the home screen → Widgets → **Red Wings Schedule Widget**.
3. Tap the theme chip at the top-right of the widget at any time to toggle jersey styles.
4. Resize as desired; the widget refreshes on boot, app update, periodic alarms, and each data sync.

## Versioning

CalVer: `version.properties` holds `VERSION_MAJOR=YY`, `VERSION_MINOR=M`,
`VERSION_PATCH=D`, `VERSION_BUILD=N` (e.g. `26.9.29.2`), so releases sort chronologically.
`versionName` is `YY.M.D.N` (e.g. `26.9.29.2`) and `versionCode` is derived arithmetically
in `app/build.gradle.kts` (`versionCode = 26092902`).
