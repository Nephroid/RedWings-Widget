# Material You Adaptive — Implementation Spec (Light)

> Direction: Google Pixel-native, Android 16 feel. Large rounded container in system
> dynamic color, tonal chips, medium-emphasis hierarchy, rounded-everything,
> no hard dividers (spacing does the work), friendly rounded sans, FAB-style refresh.

Mockup: `material-you.svg` (720×460px, 3×2 widget, **light** version).

## 1. Colors (hex + Material 3 dynamic-color roles)

All colors are flat fills. No gradients, no strokes < 1dp except chip outlines.

### Light theme (mocked)

| Usage | Hex | M3 dynamic role | RemoteViews usage |
|---|---|---|---|
| Widget container | `#FBEAEA` | `surfaceContainer` tinted by `primary` (red seed) | Root `LinearLayout` / `FrameLayout` background drawable |
| Container outline (subtle) | `#EAD0D0` | `outlineVariant` | `stroke` in `bg_widget_material_you.xml` (1dp) |
| Header chip bg | `#F3C4C4` | `primaryContainer` (red tonal) | `TextView` background pill drawable |
| Header chip text + dot | Text `#561010`, dot `#B3261E` | `onPrimaryContainer` / `primary` | `android:textColor`, dot via `ImageView` or nested `View` bg |
| Date / eyebrow labels | `#8A6E70` | `onSurfaceVariant` (medium emphasis) | `TextView` 60% emphasis |
| Matchup title | `#201A1B` | `onSurface` | Main `TextView` |
| Matchup subtitle / venue | `#74565A` / `#6E5659` | `onSurfaceVariant` | Secondary `TextView`s |
| Countdown numerals | `#211A1A` | `onSurface` (emphasized) | Large `TextView` |
| Countdown accent (`Thu`) | `#8C1D18` | `primary` (Red Wings red, darkened for contrast) | Small `TextView` |
| DET logo tint bg / glyph | `#F6C7C7` / `#8C1D18` | `primaryContainer` / `onPrimaryContainer` | Round `TextView` or `ImageView` bg + letter |
| TOR logo tint bg / glyph | `#D7E3FF` / `#0A305F` | `secondaryContainer` (blue override for opponent) / `onSecondaryContainer` | Same as above |
| Last-result chip bg / text | `#CFE8CF` / `#0F3710` | `secondaryContainer` (green tonal) / `onSecondaryContainer` | Pill `TextView` |
| HOME chip bg / stroke / text | `#FFFFFF` / `#E8CFCF` / `#5A1111` | `surface` / `outlineVariant` / `onSurface` | Pill with 1dp stroke drawable |
| Streak chip bg / text | `#F2D9D9` / `#5A1111` | `surfaceContainerHigh` / `onSurface` | Pill `TextView` |
| Standings card | `#FFF8F7` | `surfaceBright` / `surfaceContainerLow` (lighter than container) | Rounded-rect drawable on inner `LinearLayout` |
| WCGB chip bg / text | `#FFE0A3` / `#3A2800` | `tertiaryContainer` / `onTertiaryContainer` | Pill `TextView` |
| FAB refresh bg / glyph / ring | `#FFFFFF` / `#8C1D18` / `#E7C8C8` | `surface` / `primary` / `outlineVariant` | 44dp circle `ImageView` (`ic_refresh`) |
| Wallpaper behind widget (mock only) | `#E7EAF1`, blobs `#D8E2FF` / `#F9D2D2` | system wallpaper | Not shipped — mock presentation only |

### Dark theme (not mocked, for parity)

| Usage | Hex | M3 dynamic role |
|---|---|---|
| Widget container | `#3A2326` | `surfaceContainer` (dark red-gray) |
| Header chip bg / text | `#5C2526` / `#FFDAD4` | `primaryContainer` (dark) / `onPrimaryContainer` (dark) |
| Standings card | `#2E1F21` | `surfaceContainerHigh` (dark) |
| Body text | `#EDE0E1` | `onSurface` (dark) |
| Secondary text | `#B7A6A7` | `onSurfaceVariant` (dark) |
| WCGB chip | `#5A4100` bg / `#FFE0A3` text | `tertiaryContainer` (dark) / `onTertiaryContainer` (dark) |

On Android 12+ prefer real dynamic color (`@android:color/system_*` /
`?attr/colorPrimaryContainer`, etc.) with these hexes as `values/` fallback and
`values-night/` using the dark column.

## 2. Typography (sp, system fonts only)

No custom fonts. `fontFamily="sans-serif"` (Roboto / Google Sans fallback handles
Pixel rounding automatically). Use `sans-serif-medium` for 500 weights.

| Element | Size | Weight | Color | Notes |
|---|---|---|---|---|
| Header chip `RED WINGS` | 12–13sp | 700, `letterSpacing="0.12"` | `#561010` | All caps, pill |
| Date `WED · SEP 23` | 12sp | 500 | `#8A6E70` | Medium emphasis |
| Matchup `vs. Toronto Maple Leafs` | 16–17sp | 700 | `#201A1B` | `maxLines=1`, `ellipsize=end` |
| Matchup subline | 12sp | 400–500 | `#74565A` | 1 line |
| Eyebrow `NEXT FACEOFF IN` | 11sp | 700, `letterSpacing="0.14"` | `#8A6E70` | All caps |
| Countdown `1d 04h 22m` | 28–30sp | 800 (`sans-serif-medium` + `textStyle=bold`) | `#211A1A` | Tabular numerals (`fontFeatureSettings="tnum"` where safe; fallback: fixed format `1d 04h 22m`) |
| Venue line | 12–13sp | 500 | `#6E5659` | `maxLines=1`, `ellipsize=end` |
| Chips (`W 4–2 at MTL`, `HOME`, `Streak W2`) | 12–13sp | 700 | per chip table | `singleLine=true` |
| Standings primary (`5th Atlantic`) | 13sp | 700 | `#201A1B` | — |
| Standings secondary (`28–22–6 · 62 pts`) | 12sp | 500 | `#74565A` | — |
| WCGB chip `WCGB +2` | 13sp | 800 | `#3A2800` | — |

Line heights: default (`lineSpacingMultiplier="1.0"`); add `4dp` bottom padding under
eyebrow and `2dp` under countdown to get the airy rhythm.

## 3. Corner radii, elevation, spacing

- Widget container: `28dp` radius (`<corners android:radius="28dp"/>`), 1dp
  `outlineVariant` stroke, `2dp` outer margin for launcher padding.
- Header / result / WCGB chips: full pill `20dp` radius (half of 36–40dp height).
- Standings card: `24dp` radius (rounded-everything rule; mock uses `rx=24`).
- Logo tints: full circle (`oval` shape, `48–56dp` diameter, 3dp white ring via
  nested padding — white ring is a second oval behind, not a stroke, for RemoteViews safety).
- FAB refresh: `22dp` radius circle (44dp touch target), white fill, no elevation
  (RemoteViews ignores `elevation`; fake depth with 1dp `outlineVariant` ring only).
- No dividers: separate blocks with `12–16dp` vertical gaps and `8dp` horizontal
  gaps between chips. Standings internals use `16dp` gaps, never `<View>` lines.
- Padding: `16dp` outer container padding, `12dp` inner standings-card padding.

## 4. Layout structure (RemoteViews-safe)

Root must be `FrameLayout` or `LinearLayout` only (no `ConstraintLayout`,
no `CardView`, no custom views).

```
FrameLayout (widget root, bg_widget_material_you)
└─ LinearLayout (vertical, 16dp padding, 12dp gaps)
   ├─ LinearLayout (horizontal, header row)
   │  ├─ TextView (RED WINGS pill, bg_chip_header)
   │  ├─ Space / weight
   │  ├─ TextView (date, transparent)
   │  └─ ImageView (refresh FAB, bg_fab_circle + ic_refresh)
   ├─ LinearLayout (horizontal, matchup row, 12dp gap)
   │  ├─ FrameLayout (DET tint oval + centered TextView "D")
   │  ├─ FrameLayout (TOR tint oval + centered TextView "T", -8dp overlap margin)
   │  └─ LinearLayout (vertical)
   │     ├─ TextView (vs. Toronto Maple Leafs)
   │     └─ TextView (subline)
   ├─ LinearLayout (vertical, countdown block)
   │  ├─ TextView (NEXT FACEOFF IN eyebrow)
   │  ├─ LinearLayout (horizontal: countdown + Thu accent)
   │  └─ TextView (venue line)
   ├─ LinearLayout (horizontal, chips row, 8dp gaps)
   │  ├─ TextView (W 4–2 at MTL, bg_chip_result)
   │  ├─ TextView (HOME, bg_chip_home)
   │  └─ TextView (Streak W2, bg_chip_streak)
   └─ LinearLayout (horizontal, standings card bg_standings_card)
      ├─ LinearLayout (vertical: 5th Atlantic + record)
      ├─ Space (weight=1)
      ├─ LinearLayout (vertical: Playoff race + pts out)
      └─ TextView (WCGB +2, bg_chip_wcgb)
```

Drawables needed (all `<shape>` XML, solid + corners, no gradients):
`bg_widget_material_you.xml` (28dp, `#FBEAEA` + 1dp `#EAD0D0`),
`bg_chip_header.xml`, `bg_chip_result.xml`, `bg_chip_home.xml`,
`bg_chip_streak.xml`, `bg_chip_wcgb.xml` (all pill 20dp),
`bg_standings_card.xml` (24dp, `#FFF8F7`),
`bg_logo_det.xml` / `bg_logo_tor.xml` (oval),
`bg_fab_circle.xml` (oval, `#FFFFFF` + 1dp ring).

Sizing target: 3×2 cell (~680×420 in the 720×460 mock incl. launcher margin).
Keep total height `wrap_content` with `minHeight` so 3×2 and 3×3 both work.

## 5. RemoteViews-safe notes

- Only `TextView`, `ImageView`, `LinearLayout`, `FrameLayout` (+ `Space`/`View`
  for gaps). Verified against structure above — no `ConstraintLayout`, dividers,
  or custom fonts.
- Flat colors + `<shape>` rounded rects/ovals only. No gradients, blurs, or
  `elevation` — the SVG drop shadow is presentation-only, do not ship it.
- Update text via `RemoteViews.setTextViewText()`; tint via
  `setInt(viewId, "setBackgroundResource", R.drawable.…)` per light/dark,
  or `setColorStateList` only on API 31+ guarded paths.
- Refresh glyph: static `ImageView` (`android:src="@drawable/ic_refresh"`)
  inside a `FrameLayout` circle with a `PendingIntent` — no animation in widgets.
- Contrast checked (light): `#561010` on `#F3C4C4` ≈ 7.2:1; `#0F3710` on
  `#CFE8CF` ≈ 8:1; `#3A2800` on `#FFE0A3` ≈ 8.5:1; body `#201A1B` on `#FBEAEA`
  ≈ 14:1. Opponent blue `#0A305F` on `#D7E3FF` ≈ 9:1.
- Dark mode: swap to dark column; never reuse light chip fills on `#3A2326`.
- Keep all strings ≤ 1 line with `ellipsize="end"` so Toronto-length names and
  `1d 04h 22m` never push the standings card off a 3×2 cell.
