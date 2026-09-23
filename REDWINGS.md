# REDWINGS.md — Engineering Rules

- Keep classes small: no god classes over ~300 lines. Split data / ui / widget concerns.
- `RemoteViews` layouts: never use `<View>`. Use only `FrameLayout` / `LinearLayout` /
  `TextView` / `ImageView` and other RemoteViews-safe widgets.
- Coroutines: do I/O on `Dispatchers.IO`, expose state via `StateFlow`, and launch UI
  work in `viewModelScope` only — never `GlobalScope`.
- Room is the single source of truth: network responses go through the repository into
  `AppDatabase` first; UI and widget read from the database / repository flows.
- Test with `./gradlew testDebugUnitTest` (Robolectric + Roborazzi screenshot tests).
