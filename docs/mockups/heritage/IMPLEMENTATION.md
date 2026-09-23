# Heritage Winged Wheel — Implementation Notes (3×2 widget)

Mockup: `heritage.svg` (720×460px, ~3:2 ratio of a 3×2 cell widget).

## Design intent
1926 Original Six vintage hockey program. Two-column "cover + ledger" layout —
left = masthead / matchup / countdown, right = Atlantic standings ledger —
separated by a vertical double rule. Deliberately NOT a stacked Tigers-clone card.

## Colors (flat only — RemoteViews-safe)
| Role | Hex | Usage |
|---|---|---|
| Paper cream | `#F5F1E6` | widget background, circle fills, text on dark |
| Wings red | `#CE1126` | keylines, motif, DET highlight, seals, countdown label, diamond |
| Ink brown-black | `#1A1210` | all body text, outer/double-rule borders, VS cartouche |
| Parchment shade | `#EAE2CC` | logo-circle inner fill, DET standings row fill (`#EADFC6` variant in mock) |
| Muted red tint | `#EFDCD0` (alt) | optional DET row wash if `#EADFC6` clashes with photos |

No gradients anywhere. Opacity is used in the SVG mock only for hairlines —
in XML use solid colors or pre-rendered PNGs.

## Typography (system fonts only)
RemoteViews allows `android:fontFamily="serif"` — use it for everything here.
Mock uses `Georgia, 'Palatino Linotype', 'Times New Roman', serif` to preview it.

| Element | sp | style | mock px @720w |
|---|---|---|---|
| `RED WINGS` masthead | 22sp | serif bold, letterSpacing ~0.05 | 40px |
| `DETROIT` eyebrow | 10sp | serif, letterSpacing 0.3 | 13px |
| `EST. 1926…` subline | 9sp | serif italic, letterSpacing 0.15 | 10.5px |
| Date tag `SAT · OCT 12…` | 10sp | serif, letterSpacing 0.15 | 12px |
| `NEXT OPPONENT` / `NEXT FACE-OFF IN` labels | 9sp | serif, letterSpacing 0.25, red for face-off | 10–10.5px |
| Matchup `vs. Toronto Maple Leafs` | 14sp | serif italic | 19px |
| Countdown `1d 04h 22m` | 32sp | serif bold | 44px |
| Venue `Little Caesars Arena • Home` | 11sp | serif italic | 13px |
| `Last: W 4-2 at MTL` | 11sp | serif | 12.5px |
| `ATLANTIC / DIVISION` header | 11sp / 9sp | serif bold, letterSpacing 0.2 | 12/10px |
| Standings rows (abbr/record/pts) | 11sp / 10sp / 11sp bold | serif | 12/11px |
| `WCGB: 4` | 13sp bold | serif | 16px |
| `GO WINGS` seal | 9sp bold | serif, letterSpacing 0.15 | 9px |

Letter-spacing: `android:letterSpacing="0.2"`–`"0.3"` on eyebrow labels only;
keep body at 0–0.05 so narrow widgets don't clip.

## Layout structure (RemoteViews XML)
```
FrameLayout (rounded-rect cream bg @28dp radius, padding 6dp)
└─ LinearLayout vertical
   ├─ View (outer border via bg drawable, 2dp ink stroke)
   └─ LinearLayout horizontal (weightSum 100, padding 8dp)
      ├─ LinearLayout vertical (weight 62) — LEFT
      │  ├─ LinearLayout horizontal: motif ImageView (24dp) + masthead TextViews
      │  ├─ TextView date tag (bordered pill bg drawable)
      │  ├─ View double-rule divider (bg drawable or 2 stacked 1dp Views + diamond ImageView)
      │  ├─ TextView NEXT OPPONENT label
      │  ├─ LinearLayout horizontal: DET logo ImageView (48dp circle)
      │  │    + VS TextView (ink bg) + TOR logo ImageView (48dp circle)
      │  ├─ TextView "vs. Toronto Maple Leafs" (italic)
      │  ├─ TextView NEXT FACE-OFF IN + TextView countdown (32sp bold, singleLine)
      │  ├─ TextView venue + LinearLayout last-result (W badge TextView + text)
      ├─ View vertical double rule (1dp + 1dp Views with 3dp gap)
      └─ LinearLayout vertical (weight 38) — RIGHT
         ├─ TextViews ATLANTIC / DIVISION + double-rule Views
         ├─ 5× LinearLayout horizontal rows (rank/abbr/record/pts TextViews,
         │   hairline View dividers; DET row has tinted bg + red left-bar View)
         ├─ LinearLayout WCGB box (bordered bg: ink label cell + value cell)
         └─ footer ornament (ImageView dot + TextViews + red seal TextView)
```

Fixed heights: prefer `wrap_content` + `weight` over absolute dp so 3×2
resize works. Countdown `android:singleLine="true"`, `ellipsize="none"`.

## RemoteViews-safe vs PNG
**Do directly in RemoteViews (safe):**
- All TextViews (system serif), flat `solid` color backgrounds, `stroke`
  rounded-rect / oval drawables for borders, pill, W badge, seal, WCGB box.
- Row dividers and vertical double rule as plain `View`s with flat bg colors.
- DET highlight row: `solid #EADFC6` bg + 4dp red `View` bar — no image needed.
- Team logo *frames* (red/ink double-ring circles) as `oval` shape drawables.

**Must be pre-rendered PNG (put in `res/drawable-nodpi/`):**
1. `heritage_wheel_motif.png` — the small winged-wheel emblem (wheel spokes +
   feather bars). Too fine for shape XML; render at 48/72/96dp, ink+red on
   transparent, used in masthead and footer dot.
2. `heritage_det_logo.png` + `heritage_tor_logo.png` — the two 96px circles
   *with artwork inside* (wing graphic, leaf graphic). Ring can stay XML,
   artwork cannot be vector in RemoteViews reliably → ship full-circle PNGs
   at 96×96 / 144×144 px so the double ring + art stay crisp.
3. `heritage_diamond_divider.9.png` (optional) — diamond + flanking double
   rules as a 9-patch, so hairlines don't alias on ldpi. Fallback: 3 Views
   (rule / diamond ImageView / rule) is fully safe without it.
4. Outer triple keyline (ink–ink–red rounded rect) — implement as one
   `layer-list` of 3 stroked rects if the launcher honors it; otherwise bake
   `heritage_frame.9.png` 9-patch for the 720×460 frame.

## Vintage details that survive RemoteViews
- Double rules everywhere (two 1dp Views, 3–4dp apart) instead of single lines.
- Letterspaced small-caps labels via `letterSpacing` + uppercase.
- Italic serif for matchup/venue (`textStyle="italic"` + `fontFamily="serif"`).
- Diamond `✦`/rotated-square ornament: use a tiny PNG or a rotated square
  TextView (`▪` rotated not possible) — PNG recommended.
- No shadows, no gradients, no custom fonts — period feel comes from rules,
  spacing, and the red/ink/cream palette, all flat-safe.

## Asset checklist
- [ ] `heritage_wheel_motif.png` (3 densities)
- [ ] `heritage_det_logo.png`, `heritage_tor_logo.png` (96px circles)
- [ ] `heritage_frame.9.png` or `layer-list` frame drawable
- [ ] `heritage_diamond.9.png` (optional divider)
- [ ] widget XML per structure above, all `fontFamily="serif"`
