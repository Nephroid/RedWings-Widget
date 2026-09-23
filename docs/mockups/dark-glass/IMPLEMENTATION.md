# Dark Glass — "Midnight at the Joe" · Implementation Spec

Target: 3×2 home-screen widget (`red_wings_widget_layout` variant).
Aesthetic: premium sports watch-face. Near-black canvas, one dominant red
countdown, silver secondary text, hairline red dividers, generous whitespace.
Numbers-first: the countdown is the hero; everything else is quiet.

Mockup: `dark-glass.svg` (720×460, 3×2 proportion).

---

## 1. Colors (hex, flat — no gradients in RemoteViews)

| Token | Hex | Usage |
|---|---|---|
| `bg_widget` | `#0E0E12` | Widget card background (`android:background` drawable) |
| `bg_canvas` | `#08080B` | Outside-canvas in mockup only (wallpaper shows through on device) |
| `bg_chip` | `#1A1A21` | Last-result chip + highlighted DET standings row |
| `red_primary` | `#CE1126` | Countdown numerals, `W` letter, DET row border/left bar, top hairline, divider |
| `red_bright` | `#E82338` | Countdown foreground text (crisp layer over dimmer `#CE1126`; pure `#CE1126` at small sizes washes out on OLED — bright layer keeps legibility) |
| `red_dim` | `#CE1126` @ 40–45% | Glow underlay (mockup-only blur; on device use flat duplicate — see §5) |
| `silver` | `#A2AAAD` | Secondary text: header, units, date/channel, venue, non-DET standings rows |
| `silver_dim` | `#6E6E7A` | Tertiary: seconds, `ATLANTIC` / `WCGB` labels |
| `white` | `#FFFFFF` | Matchup `vs. Toronto…`, chip score, DET standings row |
| `line_dark` | `#26262E` | Header + standings hairlines; card outer stroke |
| `line_card` | `#2E2E38` | Chip stroke |
| `footer` | `#3A3A44` | `MIDNIGHT AT THE JOE` tag (drop on `xxhdpi` small widgets if tight) |
| `opp_navy` | `#0A2342` | Placeholder opponent circle (replace with real TOR PNG) |
| `divider_red` | `#CE1126` @ 65% | 1dp red divider under countdown |

Add to `res/values/colors_widget.xml`:
`widget_dark_bg #0E0E12`, `widget_dark_chip #1A1A21`,
`redwings_red #CE1126`, `redwings_red_bright #E82338`,
`silver #A2AAAD`, `silver_dim #6E6E7A`, `line_dark #26262E`.

## 2. Text sizes (sp) & styles — system fonts only

RemoteViews: no custom fonts. Use `sans-serif` / `sans-serif-medium`;
`bold` via `textStyle`. Letter-spacing via `letterSpacing` (API 21+, safe).

| Element | Size | Style / color | Notes |
|---|---|---|---|
| Header `RED WINGS • OCT 11` | 12sp, `letterSpacing 0.2` | 600 → `bold`, silver; date span white | `TextView`, single line, `ellipsize=end` |
| `NEXT GAME` + dot | 10sp, `letterSpacing 0.18` | bold, `silver_dim`; dot = 8dp `ImageView`/circle drawable red | Right-aligned in header `LinearLayout` |
| Countdown `1d 04:22` | **52–56sp** | `bold`, `red_bright` (`#E82338`) | Hero `TextView`, `gravity=center`, single line. Shrink to 44sp on `mdpi`/320dp-wide cells via `values-small` or `onUpdate` length check |
| Unit labels `DAYS HRS MIN` | 9sp, `letterSpacing 0.2` | silver | Three `TextView`s under countdown, or one centered string |
| Seconds `:18 SEC` | 11sp, `letterSpacing 0.25` | `silver_dim` | Optional — drop first if height-constrained |
| Matchup `vs. Toronto Maple Leafs` | 16sp | `bold`, white | `ellipsize=end`, `maxLines=1` |
| Date/channel `Sat • 7:00 PM ET • TNT` | 12sp | silver | Single line under matchup |
| Venue `Little Caesars Arena…` | 11sp | silver | Prepend `•` or small pin drawable; drop on short heights |
| Last-result chip `W 4–2 vs MTL` | 12sp | `W` red bold + rest white bold | Chip = `TextView` on rounded-rect drawable (see §3) |
| Section labels `ATLANTIC` / `WCGB +1.0` | 9sp, `letterSpacing 0.18` | bold, `silver_dim` | Row header |
| Standings rows | 12sp | silver; DET row white bold on `bg_chip` | Monospace-ish alignment via fixed format string (`%s  %s  %d PTS`), not tabs |
| Footer tag | 8sp | `footer` color | `gone` on small/resizable-min layouts |

Countdown format logic: `1d 04:22` (+ `:18` seconds line) when ≥24h;
`04:22:18` when <24h. Keep glyph count stable to avoid layout jump
(pad with leading zeros, fixed `minWidth` on countdown `TextView`).

## 3. Layout structure (RemoteViews-safe views only)

```
FrameLayout (root, padding 0 — launcher supplies margins)
└─ LinearLayout vertical (card)
   ├─ View (top hairline, 2dp × match_parent, red_primary)   [1]
   ├─ LinearLayout horizontal (header, 16dp pad L/R, 10dp top)
   │   ├─ TextView header (weight 1)
   │   ├─ ImageView statusDot (8dp circle drawable)
   │   └─ TextView NEXT GAME
   ├─ View (hairline 1dp, line_dark, 16dp side margins)
   ├─ TextView countdown (center, 8dp top margin)             [HERO]
   ├─ TextView units (center)
   ├─ TextView seconds (center, gone-if-tight)
   ├─ View (divider 1dp, divider_red, 16dp side margins)
   ├─ LinearLayout horizontal (matchup, 16dp pad)
   │   ├─ ImageView logoDet (40dp circle PNG)
   │   ├─ ImageView logoOpp (40dp circle PNG, -12dp overlap margin)
   │   ├─ LinearLayout vertical (weight 1, 12dp left margin)
   │   │   ├─ TextView matchup
   │   │   └─ TextView datetime
   │   └─ TextView chip (last result, rounded-rect bg drawable)
   ├─ TextView venue (16dp pad L/R)
   ├─ View (hairline 1dp, line_dark, 16dp side margins)
   └─ LinearLayout vertical (standings, 16dp pad, 6dp bottom)
       ├─ LinearLayout horizontal (ATLANTIC + WCGB)
       └─ LinearLayout horizontal (3 rows: TOR | DET-highlight | BOS)
```

- Only `LinearLayout`, `FrameLayout` (+ `RelativeLayout` if already used),
  `TextView`, `ImageView`, `View` (for hairlines). No `ConstraintLayout`,
  no custom views, no `elevation`, no gradients.
- Card background: rounded-rect drawable, **solid** `#0E0E12`, corners 28dp
  (mockup 28px @ 720px ≈ 28dp on `xhdpi` baseline — tune 24–28dp),
  stroke 1dp `line_dark`. One drawable file: `widget_bg_dark_glass.xml`.
- Chip background: rounded-rect drawable, solid `bg_chip`, corners 20dp
  (pill), stroke 1dp `line_card`: `widget_tag_dark_glass.xml`.
- DET highlight row: same chip drawable + 4dp red left bar — implement as
  horizontal `LinearLayout` with a 4dp-wide red `View` + `TextView`; or a
  `layer-list` drawable (red bar + chip bg). `layer-list` is RemoteViews-safe.
- All dimensions in `dp`; text in `sp`. Min widget size: keep existing
  `xml/` provider info (`minWidth 250dp`, `minHeight 180dp` approx for 3×2);
  add `targetCellWidth/Height 3/2` (API 31+).

## 4. RemoteViews-safe notes

- **No blur / glow / shadows.** `feGaussianBlur` in the SVG is mockup-only.
  `TextView` `shadowColor/shadowDx` is ignored in RemoteViews on most
  launchers — do not rely on it.
- **Fake the glow (pick one):**
  a) *Flat (recommended):* single countdown `TextView` in `red_bright`
     on near-black — contrast alone reads as "glow" on OLED. Zero extra cost.
  b) *Layered text:* stack two identical `TextView`s in a `FrameLayout` —
     bottom copy `red_primary` at larger size / offset by 1dp, top copy
     `red_bright`. Slight halo illusion, still 100% RemoteViews-safe.
  c) *PNG:* pre-render blurred red numerals 0–9 + `:` as drawables and
     compose with `ImageView`s. Best fidelity, highest maintenance — only
     if (a)/(b) rejected in review.
- No custom fonts: `Roboto` fallback is automatic (`sans-serif`). Do not
  reference font resources from the widget layout.
- Keep the layout depth ≤ ~8 nested layouts; avoid `weight` chains deeper
  than 2 levels for update performance (`onUpdate` + `notifyAppWidgetViewDataChanged` budget).
- All color/state changes go through `RemoteViews.setTextColor / setViewVisibility /
  setImageViewResource`; no programmatic drawables.
- Dark-only design: set `android:description` and widget preview accordingly;
  no light-mode variant needed for this direction.

## 5. PNG asset needs (everything else is XML shapes)

| Asset | Size / form | Notes |
|---|---|---|
| `logo_det_circle.png` | 96/144/192px (hdpi/xhdpi/xxhdpi), transparent circle | White winged-wheel on `#CE1126` disc, flat, no shadow. Reuse `ic_redwings_logo` artwork if clean at 40dp |
| `logo_opp_circle_*.png` or per-team set | Same sizes | Mockup navy `T` is a placeholder. Ideally reuse existing team-logo pack from `TeamUtils`/app module; fallback: generic navy disc with white monogram |
| `widget_preview_dark_glass.png` | 720×460 (matches this SVG export) | Launcher widget picker preview (`android:previewImage` / `previewLayout` API 31+) |
| *Optional* glow numerals | Only if option (c) chosen in §4 | Pre-blurred red digit PNGs; adds ~11 assets × densities — avoid unless required |
| Status dot | **No PNG** — 8dp oval shape drawable (`dot_red.xml`) | Solid `#CE1126`; halo ring in mockup is illustrative only |

Export the SVG at 720×460 for the preview asset; text in the PNG may use any
font, but the live widget stays system-font.

## 6. Files to create (for the implementing agent)

- `res/drawable/widget_bg_dark_glass.xml` — rounded rect, solid `#0E0E12`, 24–28dp corners, 1dp `#26262E` stroke.
- `res/drawable/widget_tag_dark_glass.xml` — pill, solid `#1A1A21`, 20dp corners, 1dp `#2E2E38` stroke.
- `res/drawable/dot_red.xml` — 8dp oval, `#CE1126`.
- `res/drawable/standings_det_highlight.xml` — `layer-list`: chip bg + 4dp red left bar (or build with nested layouts).
- `res/layout/red_wings_widget_dark_glass.xml` — layout per §3.
- `res/values/colors_widget.xml` — add tokens from §1.
- `WidgetBinder` / provider: bind countdown (`1d 04:22` / `04:22:18`), matchup,
  chip, 3-line Atlantic + WCGB; hide seconds/venue/footer when
  `AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT` < threshold.
