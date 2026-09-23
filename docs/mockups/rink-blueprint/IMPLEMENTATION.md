# Rink Blueprint — Implementation Spec (RemoteViews-safe)

Design direction: hockey-rink diagram / blueprint. Deep ice-navy field, thin
ice-blue line art as background geometry, white text, Wings red reserved
exclusively for (1) the countdown and (2) the DET standings-row highlight so
both pop. Monospace tabular numerals throughout for the technical feel.

Mockup: `rink-blueprint.svg` (720×460, matches a ~3×2 widget aspect).

## 1. Colors (hex, flat — no gradients)

| Token | Hex | Usage |
|---|---|---|
| Ice-navy bg | `#0A1A2F` | Widget background, logo-circle inverse fill |
| Ice-blue line/text-dim | `#7FB3D5` | Rink line art, labels (`NEXT FACEOFF`, venue, `LAST SHIFT:`, table headers), hairline dividers, outer border, logo strokes |
| White | `#FFFFFF` | Primary text (matchup, table rows, countdown numerals), DET logo disc |
| Wings red (POP ONLY) | `#CE1126` | Countdown chip background + DET row background. **Do not use red anywhere else** (no red rink lines, no red logos, no red WCGB value) |

Contrast note: `#CE1126` text directly on `#0A1A2F` is ~2.7:1 (fails small
text), so red is always used as a **filled chip/row background with white
text on top** (white on `#CE1126` ≈ 5.9:1). This is why the mockup shows a
red rounded chip behind `1d 04h 22m` and a red row behind `DET` — never bare
red text on navy except large countdown if artificially brightened (don't).

Text opacities on navy: labels `#7FB3D5` at 100% (12–19sp sizes); hairlines
at 35%; background rink art at 12–22% (baked into PNG, below).

## 2. Text sizes (sp) + styles

System fonts only — no custom fonts in RemoteViews. Use
`android:fontFamily="monospace"` for every text element (tabular numerals
come free with the monospace fallback: `Droid Sans Mono` → `Courier New`
chain in the mockup). All sizes in `sp`, single line, `ellipsize="end"`.

| Element | Size | Style / color |
|---|---|---|
| Tech header (`DET ▸ RINK.BLUEPRINT / V1`, `FIG. 03 — 3×2`) | 10sp | monospace, `#7FB3D5`, `letterSpacing="0.12"` |
| `NEXT FACEOFF` label | 11sp | monospace bold, `#7FB3D5`, `letterSpacing="0.25"` |
| Countdown `1d 04h 22m` | 30–32sp | monospace bold, `#FFFFFF` on `#CE1126` chip, `letterSpacing="0.02"` |
| Matchup `vs. Toronto Maple Leafs` | 16sp | sans or monospace bold, `#FFFFFF` |
| Logo letters `D` / `T` | 12sp | bold; `D` = `#0A1A2F` on white disc, `T` = `#7FB3D5` on navy disc |
| Venue `LITTLE CAESARS ARENA — 7:00 PM ET` | 11sp | monospace, `#7FB3D5`, `letterSpacing="0.08"` |
| `LAST SHIFT:` label / `W 4-2 at MTL` value | 11sp / 12sp | label monospace `#7FB3D5`; value monospace bold `#FFFFFF` |
| `STANDINGS // ATLANTIC` / `TAB. 01` | 10sp | monospace, `#7FB3D5`, `letterSpacing="0.15"` |
| Table column headers (`# TEAM GP PTS`) | 10sp | monospace, `#7FB3D5` @ 75% alpha |
| Table rows | 12sp | monospace, `#FFFFFF`; DET row bold `#FFFFFF` on `#CE1126` |
| `WCGB: 4` | 12sp | monospace bold, `#FFFFFF` inside ice-blue outline chip (NOT red) |
| Footer datum line | 8–9sp | monospace, `#7FB3D5` @ 60% |

Line discipline: countdown exactly `"%dd %02dh %02dm"` so column width never
jitters; scores/standings always zero-padded or fixed-width via monospace.

## 3. Background PNG spec (line art MUST be pre-rendered)

RemoteViews cannot draw vector rink geometry at runtime — bake it.

- **What:** single opaque PNG: navy field `#0A1A2F` + outer 2px border
  `#7FB3D5` @ ~55% + rink diagram (outer rounded-rect rink outline, 2 blue
  lines, dashed center line, center circle + dot, 4 faceoff circles + dots +
  hash marks, 2 crease arcs, 2 goal lines) stroked `#7FB3D5` at 12–22%
  opacity, 1.5–2.5px at xxxhdpi. Rounded corners 16dp (drawn into the PNG).
- **Contents map to SVG:** delete everything except the backing `<rect>`s
  and the `FAINT RINK LINE-ART` `<g>` group in `rink-blueprint.svg`, export
  at each density, flatten (no transparency — avoids overdraw seams).
- **Dimensions (3×2 cells ≈ 180dp × 110dp):**

| Density | Scale | PNG px |
|---|---|---|
| mdpi | ×1 | 180 × 110 |
| hdpi | ×1.5 | 270 × 165 |
| xhdpi | ×2 | 360 × 220 |
| xxhdpi | ×3 | 540 × 330 |
| xxxhdpi | ×4 | **720 × 440** (master; mockup is 720×460 including caption margin — crop 10px top/bottom) |

  Place under `res/drawable-<density>/widget_bg_rink.png` (or one
  `drawable-nodpi/widget_bg_rink.png` at 720×440 if APK size matters).
  Reference as `android:background="@drawable/widget_bg_rink"` on the root
  layout — never as a remote `ImageView` bitmap (saves the RemoteViews
  bitmap budget).
- **Red exclusion:** the PNG contains zero red pixels. Red chips (countdown,
  DET row) are separate `shape` drawables layered on top so they stay crisp.

Supporting drawables (all `shape`, no 9-patch needed):
- `countdown_chip.xml`: `solid #CE1126`, `corners 6dp`.
- `det_row_bg.xml`: `solid #CE1126`, `corners 4dp`.
- `wcgb_chip.xml`: `stroke 1dp #7FB3D5`, `corners 6dp`, transparent fill.
- `logo_disc_white.xml` / `logo_disc_navy.xml`: `oval` 40dp, white/navy fill
  + `stroke 1dp #7FB3D5`. (Deliberately single-color stencil — full team
  logos would break the two-red-only rule; swap in real logos only if the
  palette constraint is lifted.)

## 4. Layout structure (flat + geometric)

`res/layout/widget_rink.xml` — root `LinearLayout` vertical, background PNG.
`android:minWidth="180dp"`, `minHeight="110dp"`, `targetCellWidth="3"`,
`targetCellHeight="2"` (Android 12+). Max ~3 nesting levels.

```
LinearLayout (vertical, @drawable/widget_bg_rink, padding 12dp)
├── LinearLayout (horizontal, height wrap)            # tech header
│   ├── TextView (tech_left, weight 1)               # DET ▸ RINK.BLUEPRINT
│   └── TextView (tech_right)                        # FIG. 03 — 3×2
├── View (hairline, 1dp, #7FB3D5 @35%)
├── LinearLayout (horizontal)                        # body
│   ├── LinearLayout (vertical, weight 1.15)         # LEFT: faceoff block
│   │   ├── TextView (next_faceoff_label)
│   │   ├── FrameLayout (@drawable/countdown_chip, padding 6dp/2dp)
│   │   │   └── TextView (countdown)                 # 1d 04h 22m
│   │   ├── LinearLayout (horizontal)                # matchup row
│   │   │   ├── ImageView (logo_det, 20dp oval)      # static disc drawable
│   │   │   ├── ImageView (logo_opp, 20dp oval)
│   │   │   └── TextView (matchup)
│   │   ├── TextView (venue)
│   │   └── LinearLayout (horizontal)                # last shift
│   │       ├── TextView (last_shift_label)
│   │       └── TextView (last_shift_value)
│   └── LinearLayout (vertical, weight 1)            # RIGHT: standings
│       ├── LinearLayout (horizontal)                # STANDINGS // TAB.01
│       ├── LinearLayout (horizontal) ×4–5           # header + rows;
│       │   └── TextViews (# / TEAM / GP / PTS)      # DET row root bg det_row_bg
│       └── TextView (wcgb, @drawable/wcgb_chip)     # WCGB: 4
└── (optional) TextView (footer datum, gone if height < 110dp)
```

Row pattern per standings entry: one horizontal `LinearLayout` with fixed
widths (`#` 24dp, `TEAM` weight 1, `GP` 36dp right-aligned, `PTS` 40dp
right-aligned) — fixed widths + monospace keep columns tabular without a
`TableLayout`. DET row sets `background="@drawable/det_row_bg"`.

## 5. RemoteViews-safe notes

- **Widgets only:** `TextView`, `ImageView` (static discs only),
  `LinearLayout`/`FrameLayout`, hairline `View`. No `ConstraintLayout`,
  no custom views, no custom fonts, no shadows/elevation, no gradients.
- **Updates:** single `RemoteViews` pass; set text via `setTextViewText`,
  toggle DET highlight by `setInt(rowId, "setBackgroundResource",
  R.drawable.det_row_bg)`. Never send the bg PNG through `setImageViewBitmap`.
- **Dark-only design:** background is opaque navy — force
  `android:background` on root (ignore system light/dark); test with themed
  icons off.
- **Resize:** for heights < 110dp set footer datum + venue to `View.GONE`;
  keep countdown + matchup + 3 standings rows minimum. Ellipsize matchup at
  one line — never wrap (wrap breaks the blueprint grid).
- **No red leaks:** lint check — the only `#CE1126` references in
  `res/` must be `countdown_chip.xml` and `det_row_bg.xml`.
- **Export path:** `rink-blueprint.svg` → hide all non-background layers →
  export at table in §3 → `optipng` → drop into `res/drawable-*`.
```

