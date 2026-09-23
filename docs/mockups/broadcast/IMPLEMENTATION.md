# Broadcast — Implementation Spec (Scoreboard Broadcast)

Direction: TV sports-ticker energy. Bold red header band, white body, black
score-bug blocks, thick black dividers, uppercase condensed type, HOME/AWAY
pill, tight leaderboard with points emphasized. Flat + geometric so it maps
1:1 to RemoteViews.

Mockup: `broadcast.svg` (720×460, 3×2 widget).

## 1. Colors (hex)

| Token | Hex | Usage |
|---|---|---|
| `wings_red` | `#CE1126` | Header band, FINAL badge, DET accent bar, HOME pill fill, footer top rule |
| `ice_white` | `#FFFFFF` | Card body, header text, bug text |
| `bug_black` | `#111111` | Score bug bg, countdown chip, standings bar, footer ticker, card stroke, dividers |
| `det_tint` | `#FDECEF` | Highlighted DET leaderboard row bg |
| `body_gray` | `#555555` | Secondary labels (date, venue city, PUCK DROP IN, LAST GAME) |
| `record_gray` | `#333333` | W–L–OTL records (non-DET) |
| `rank_gray` | `#999999` | Rank numbers 1–3, 5 |
| `rule_gray` | `#E5E5E5` | Thin 1dp row separators inside leaderboard |
| `bug_divider` | `#888888` | En-dash / vertical divider inside score bug |
| `overlay_white_25` | `#FFFFFF` @ 25% | Header speed-line |
| `overlay_white_14` | `#FFFFFF` @ 14% | Header diagonal slashes |

No gradients, no shadows, no transparency except the two header slash
overlays (omit overlays on-device if 9-patch cost matters — visual is fine
without them).

## 2. Text sizes (sp)

System fonts only. All labels uppercase. Use `sans-serif-condensed` where
available, fallback `sans-serif`. Fake-condensed effect via `letterSpacing`
reduction — no custom font files.

| Element | sp | Style | Color |
|---|---|---|---|
| Header eyebrow `DETROIT` | 12sp | bold, tracking +0.25em | white |
| Header title `RED WINGS` | 26sp | black/900, condensed | white |
| Header chip `NHL • ATLANTIC / 4TH • 63 PTS` | 10sp | black/900 | white on black |
| Section label `NEXT GAME • FRI FEB 14` | 10sp | black, tracking +0.15em | wings_red |
| Matchup `VS. TORONTO MAPLE LEAFS` | 18sp | black/900, condensed, maxLines 1, ellipsize end | bug_black |
| Matchup sub `7:00 PM ET • TNT` | 11sp | bold | body_gray |
| Label `PUCK DROP IN` / `LAST GAME` | 9sp | black, tracking +0.15em | body_gray |
| Countdown `1d 04h 22m` | 17sp | black/900, tabular | white on black |
| Bug `FINAL` badge | 11sp | black/900 | white on red |
| Bug score `DET 4 – MTL 2` | 15sp | black/900 | white (`–` in bug_divider) |
| Bug result `W • HOME` | 11sp | bold | white |
| Venue `LITTLE CAESARS ARENA` | 10sp | black/900 | bug_black |
| Venue city `DETROIT, MI` | 10sp | bold | body_gray |
| Pill `HOME` / `AWAY` | 10sp / 9sp | black/900 | white-on-red / black-on-white |
| Standings bar `ATLANTIC LEADERBOARD` | 9sp | black, tracking +0.15em | white on black |
| Standings bar `W – L – OTL • PTS` | 9sp | black | white on black |
| Row rank | 10sp | black | rank_gray (DET: wings_red) |
| Row abbrev `TOR / DET ◀` | 10sp | black/900 | bug_black (DET: wings_red) |
| Row record | 10sp | bold | record_gray (DET: bug_black black) |
| Row points | 12sp | black/900 | bug_black (DET: white-on-black chip) |
| Footer ticker `DET 4TH ATL • …` | 9sp | black, tracking +0.08em, single line | white on black |

Countdown + points should use tabular figures if available
(`android:fontFeatureSettings="tnum"` is ignored on some launchers — keep
right-alignment as the real guarantee).

## 3. Layout structure (RemoteViews-safe)

Overall: `LinearLayout` vertical, background `@drawable/widget_bg_broadcast`
(white rounded rect, 14dp radius, 2dp black stroke). Padding 0 — dividers are
full-bleed Views.

```
LinearLayout (vertical, white bg)
├─ LinearLayout header (horizontal, wings_red, 52dp, padding 12dp/8dp)
│  ├─ LinearLayout (vertical, weight 1)
│  │  ├─ TextView DETROIT (12sp)
│  │  └─ TextView RED WINGS (26sp)
│  └─ LinearLayout chip (vertical, bug_black, 4dp radius, padding 6dp)
│     ├─ TextView NHL • ATLANTIC (10sp, centered)
│     └─ TextView ● 4TH • 63 PTS (10sp, centered)
├─ View divider (h 3dp, bug_black, full width)
├─ LinearLayout next-game (horizontal, padding 12dp/8dp)
│  ├─ LinearLayout (vertical, weight 1)
│  │  ├─ TextView NEXT GAME • DATE (10sp, wings_red)
│  │  ├─ TextView VS. … (18sp, single line)
│  │  └─ TextView time • TV (11sp)
│  └─ LinearLayout countdown (vertical, gravity end)
│     ├─ TextView PUCK DROP IN (9sp)
│     └─ TextView 1d 04h 22m (bug_black bg, 4dp radius, padding 8x4dp)
├─ View divider (h 2dp, bug_black)
├─ LinearLayout last-game (vertical, padding 12dp/6dp)
│  ├─ TextView LAST GAME (9sp)
│  └─ LinearLayout bug (horizontal, bug_black, 4dp radius)
│     ├─ TextView FINAL (wings_red bg, padding 8x4dp)
│     ├─ TextView DET 4 – MTL 2 (weight 1, centered)
│     └─ TextView W • HOME (padding end 8dp)
├─ View divider (h 2dp, bug_black)
├─ LinearLayout venue (horizontal, padding 12dp/6dp)
│  ├─ TextView LITTLE CAESARS ARENA (weight 0) + TextView DETROIT, MI
│  └─ Space (weight 1)
│  ├─ TextView HOME (wings_red pill, 13dp radius)
│  └─ TextView AWAY (white pill, 1dp black stroke — see notes)
├─ View divider (h 2dp, bug_black)
├─ LinearLayout standings-bar (horizontal, bug_black, padding 12x4dp)
│  ├─ TextView ATLANTIC LEADERBOARD (weight 1, 9sp)
│  └─ TextView W – L – OTL • PTS (9sp)
├─ LinearLayout rows (vertical)
│  ├─ ×4 normal row: horizontal, padding 12x2dp + 1dp rule_gray divider View
│  │  rank (24dp) / abbrev (weight 1) / record (wrap) / points (48dp, end)
│  └─ ×1 DET row: same + det_tint bg + 3dp wings_red left View + points chip
│     (bug_black 2dp radius bg)
└─ TextView footer ticker (bug_black bg, red 2dp top border via layered
   drawable, centered, single line, 9sp)
```

Heights: header 52dp, next-game ~56dp, bug ~30dp, venue ~24dp,
standings-bar 16dp, each row ~18dp, footer 16dp → fits 3×2 (≈220dp tall)
without scrolling. If launcher squeezes, allow venue city to `gone` first,
then bug `W • HOME` to `gone`.

## 4. RemoteViews-safe notes

- Only `TextView / ImageView / LinearLayout / FrameLayout / View` (+
  `Space`). No ConstraintLayout, no custom views, no webviews.
- Dividers are plain `View` with fixed `layout_height` (2–3dp) and solid
  `background` color — reliable in RemoteViews.
- Rounded rects + strokes + solid fills only. All as XML
  `shape android:shape="rectangle"` drawables with `<corners>` + `<solid>` +
  `<stroke>`. No gradients, no ripples, no elevation.
- No custom fonts: `android:fontFamily="sans-serif-condensed"` for header /
  matchup / bug score, `sans-serif-medium` or `sans-serif-black` where OEM
  supports it; always provide `sans-serif` fallback. Uppercase via strings
  (don't rely on `textAllCaps` alone across launchers).
- `letterSpacing` (tracking) is API 21+ — safe, but keep legibility at 0 if
  ignored.
- Single-line enforcement: `maxLines="1"`, `ellipsize="end"` on matchup,
  venue, footer ticker.
- Click: single `setOnClickPendingIntent` on root opens app; no per-row
  clicks (widget collection views add complexity — avoid `ListView`/
  `GridView`; 5 static rows updated via `setTextViewText` are cheaper).
- Dark-mode launcher tint: force explicit `textColor` + `background` on every
  node (never rely on `?attr`), otherwise white body flips.
- Update cadence: countdown text updates every 15–30 min via WorkManager;
  compute `1d 04h 22m` string in code, push with `setTextViewText`.

## 5. PNG / drawable asset needs

Goal: zero PNGs — everything is XML. Needed drawables (all `res/drawable/`):

- `widget_bg_broadcast.xml` — white, 14dp corners, 2dp `#111111` stroke.
- `chip_black_4.xml` — `#111111`, 4dp corners (countdown, DET points).
- `chip_black_6.xml` — `#111111`, 6dp corners (header chip).
- `badge_red_4.xml` — `#CE1126`, 4dp corners (FINAL badge; left-side rounding
  only achieved by overlapping a 4dp black rect — or accept uniform radius).
- `pill_home.xml` — `#CE1126`, 13dp corners (HOME).
- `pill_away.xml` — `#FFFFFF`, 13dp corners, 1dp `#111111` stroke (AWAY).
- `bar_det_accent.xml` — `#CE1126` plain rect, 3dp wide (DET row left bar).
- `divider_black.xml` — `#111111` plain rect (reused at 2dp/3dp heights).
- `divider_rule.xml` — `#E5E5E5` plain rect, 1dp (leaderboard separators).
- `footer_bg.xml` — layer-list: `#111111` rect + 2dp `#CE1126` top line item.

Optional (only if design QA wants it): 1× `ic_wings_simple.png` (white,
24dp, mdpi–xxxhdpi) for header left of DETROIT — SVG mockup omits the logo
intentionally so layout works even without the asset.

States: HOME pill = red fill active; AWAY game swaps fills (AWAY becomes red
fill + white text, HOME becomes white + black stroke). Two pre-built pill
drawables cover both states — toggle via `setInt(view, "setBackgroundResource",
R.drawable.…)`.
