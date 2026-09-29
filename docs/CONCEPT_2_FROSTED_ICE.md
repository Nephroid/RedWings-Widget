# Detroit Red Wings Widget — Concept 2: "The Frosted Ice & Home Red Sweater (Modern Editorial Glass)"

**Concept Artist**: Concept Artist 2  
**Target Form Factor**: Android Home Screen Widget (16:9 / 3:2 aspect ratio, scalable 4x2 to 5x3 grid)  
**Design Paradigm**: Frosted Acrylic Ice Glassmorphism × Detroit Red Wings Home Red Sweater Stripe × Premium Sports Editorial Typography  
**Mockup Asset**: `mockups/concept_2_frosted_ice_editorial.png`

---

## 1. Design Philosophy & Aesthetic Blueprint

Concept 2 reconciles two tactile hockey truths: the crisp, sub-zero precision of arena ice and the iconic warmth of Detroit's sacred red sweater.

1. **Frosted Acrylic Ice Substrate**:
   - The body of the widget is formed from an ultra-modern translucent ice glass sheet (`rgba(255, 255, 255, 0.72)` over dark mode or frosted acrylic `backdrop-filter: blur(28px) saturate(180%)`).
   - Micro-texture: Faint, authentic Zamboni skate etchings and sub-surface light refractions etched across the surface, giving it physical depth without degrading text legibility.
   - Beveled Glass Rim: A 1.5dp inner specular white perimeter stroke (`rgba(255, 255, 255, 0.85)` at top/left light source, falling to `0.30` on bottom/right) simulating real beveled ice slab thickness.

2. **Home Red Sweater Stripe Banner**:
   - A bold Detroit Red Wings Home Red (`#C8102E` / `#B30C20`) header band spanning the top rim of the widget.
   - Flanked by crisp dual pure white piping bands inspired directly by the sleeve and hem striping of the official Detroit home sweater.
   - Debossed tactile red texture with the iconic Winged Wheel crest in high-definition enamel white and crisp editorial typography.

3. **Modern Editorial Typography (Apple Sports & The Athletic Inspiration)**:
   - Clean, geometric grotesque type scale (`SF Pro Display` / `Inter` / `Roboto Flex`).
   - Strict optical alignment, generous breathing room, high contrast ratios (obsidian black `#0F172A` on ice, pure white `#FFFFFF` on red sweater banners and highlighted badge pills).
   - High legibility WCAG 2.1 AAA compliance across all lighting environments.

---

## 2. Layout Specification & Visual Regions

The widget layout is organized vertically into four harmonious visual zones:

```
┌────────────────────────────────────────────────────────────────────────┐
│ [LOGO]  DETROIT RED WINGS                          [SWEATER STRIPES]   │ <- Region 1: Sweater Ribbon
├────────────────────────────────────────────────────────────────────────┤
│    AWAY           │          SATURDAY • 7:00 PM           │    HOME    │
│   [TOR]    TOR    │                 [@]                   │    DET [W] │ <- Region 2: Matchup Hero
│           20-8-6  │             02 : 14 : 22              │   22-7-5   │
├───────────────────┴───────────────────────────────────────┴────────────┤
│                    [ Little Caesars Arena • Detroit ]                  │ <- Region 3: Venue Glass Pill
├────────────────────────────────────────────────────────────────────────┤
│       PLAYOFF SEEDS (1–4)        │         IN THE HUNT (5–8)           │
│  1 BOS   2 FLA   3 TOR  [4 DET]  │   5 TBL   6 OTT   7 BUF   8 MTL     │ <- Region 4: Split Standings
└────────────────────────────────────────────────────────────────────────┘
```

### Region 1: The Home Red Sweater Ribbon Header (Height: ~18%)
- **Background**: Solid Red Wings Home Red (`#C8102E`) horizontal banner anchored along the top rounded squircle corners.
- **Accents**: Dual horizontal racing/sweater stripes in crisp ice white (`#FFFFFF`, 2dp height each) running symmetrically across both sides.
- **Elements**:
  - **Left**: Official Red Wings Winged Wheel insignia badge in embossed enamel white.
  - **Center**: `DETROIT RED WINGS` in all-caps, tracked bold grotesque (`letter-spacing: 0.10em`, color: `#FFFFFF`, weight: 800).
  - **Clash Indicator**: An embossed red circular puck badge with a crisp `@` symbol at the ribbon's bottom center vertex, visually anchoring down into the matchup area.

### Region 2: Matchup Hero & Puck-Drop Countdown (Height: ~45%)
- **Background**: Semi-transparent frosted ice sheet with subtle skate blade textures.
- **Away Team (Left Column)**:
  - Top label: `AWAY` micro-tag (font-size 9sp, font-weight 700, color `#64748B`, uppercase).
  - Club Crest: Toronto Maple Leafs blue crest (or current opponent) with optical drop shadow.
  - Abbreviation: `TOR` in obsidian black (`#0F172A`, 22sp, font-weight 800).
  - Record: `20-8-6` (tabular numerals, 11sp, font-weight 600, `#475569`).
- **Center Duel & Countdown Anchor (Middle Column)**:
  - Matchup Schedule: `SATURDAY • 7:00 PM` (11sp, bold uppercase, `#0F172A`, centered).
  - Puck-Drop Countdown Clock:
    - Digits: `02 : 14 : 22` rendered in vibrant Detroit Red (`#C8102E`) with slight inner warmth and clean glyph spacing.
    - Prominent scale (26sp bold condensed), instantly communicating remaining game prep time to the user.
- **Home Team (Right Column)**:
  - Top label: `HOME` micro-tag (font-size 9sp, font-weight 700, color `#C8102E`, uppercase).
  - Club Crest: Red Wings Winged Wheel in vibrant red and white.
  - Abbreviation: `DET` in obsidian black (`#0F172A`, 22sp, font-weight 800).
  - Record: `22-7-5` (11sp, font-weight 600, `#475569`).

### Region 3: Context / Venue Frosted Glass Pill (Height: ~10%)
- **Container**: Floating rounded pill (`corner-radius: 999dp`) with frosted white acrylic fill (`rgba(255, 255, 255, 0.85)`) and subtle border stroke (`rgba(255, 255, 255, 0.95)`).
- **Text**: `Little Caesars Arena` (or venue name + city), rendered in centered, high-contrast dark charcoal (`#1E293B`, 10sp, bold).

### Region 4: Atlantic Standings — 2-Column Split (Height: ~27%)
- **Container**: Contained within a secondary frosted tray (`corner-radius: 20dp`, soft inset shadow).
- **Header Structure**: Two distinct section headers:
  - Left Header: `PLAYOFF SEEDS (1–4)` in medium-bold editorial gray (`#334155`, 9sp).
  - Right Header: `IN THE HUNT (5–8)` in medium-bold editorial gray (`#334155`, 9sp).
  - Separator: A vertical hairline divider (`1dp`, translucent frost `#CBD5E1`) dividing column 1 and column 2.
- **Left Column (Playoff Seeds 1–4)**:
  - `1 BOS`: Rank + Team abbreviation with points or record.
  - `2 FLA`: Rank + Team abbreviation.
  - `3 TOR`: Rank + Team abbreviation.
  - **`4 DET` Highlight Badge**:
    - Wrapped in a glowing Detroit Red pill (`#C8102E`) with gentle crimson ambient blur glow (`box-shadow: 0 4px 14px rgba(200, 16, 46, 0.4)`).
    - Bold pure white typography: `4 DET` (`#FFFFFF`, weight: 800).
- **Right Column (In The Hunt 5–8)**:
  - `5 TBL`: Rank + Team abbreviation.
  - `6 OTT`: Rank + Team abbreviation.
  - `7 BUF`: Rank + Team abbreviation.
  - `8 MTL`: Rank + Team abbreviation.
  - Rendered in neutral high-contrast graphite (`#334155`, weight: 700).

---

## 3. Micro-Interactions & Tap Targets

1. **Header Ribbon Tap**:
   - Tapping the `DETROIT RED WINGS` banner launches the Detroit Red Wings official mobile team hub or companion app dashboard.
2. **Matchup Hero Tap**:
   - Tapping the `AWAY` column opens opponent roster, head-to-head history, and starting goalie preview.
   - Tapping the `HOME` column opens Red Wings injury report and line combinations.
   - Tapping the `02 : 14 : 22` countdown clock opens a quick modal with broadcast channels (`FDSN DET`, `97.1 The Ticket`), calendar sync, and ticket wallet pass.
3. **Standings Tray Tap**:
   - Tapping the `PLAYOFF SEEDS` or `IN THE HUNT` zones expands the full NHL Wild Card Race standings table with goal differential (`DIFF`), games in hand (`GP`), and regulation wins (`RW`).
   - Long-pressing the widget triggers the dynamic theme selector (switching smoothly between Home Red, Heritage 1926, Away White, Reverse Retro, and Stadium Series).

---

## 4. Text-to-Image Generation Prompt Specification

```text
Hyper-detailed high-fidelity standalone Android Home Screen widget UI mockup, aspect ratio 16:9, isolated widget card floating with gentle realistic drop shadow on a neutral minimal studio backdrop, absolutely NO phone bezels, NO smartphone frame, NO hands, NO desktop icons. Concept: 'The Frosted Ice & Home Red Sweater (Modern Editorial Glass)' for the Detroit Red Wings. Visual aesthetic features a luxurious frosted acrylic ice glassmorphic card with subtle skate etchings, translucent ice refraction, and crisp white beveled specular glass edges. Across the top is a bold Detroit Red Wings Home Red (#C8102E) ribbon sweater-stripe header with crisp dual white jersey stripes, the classic Detroit Winged Wheel logo, and clean uppercase typography 'DETROIT RED WINGS'. Upper matchup area features an editorial Apple Sports layout: on the left is the Away team with Toronto crest, 'TOR', and record; in the center is a glowing red '@' clash badge, 'SATURDAY • 7:00 PM', and a prominent high-contrast puck-drop countdown '02 : 14 : 22' with red numerals; on the right is the Home team with Detroit Red Wings winged wheel logo, 'DET', and record. Below is a frosted glass venue pill 'Little Caesars Arena'. The bottom section displays an Atlantic Division standings board split into two distinct columns: Left column 'PLAYOFF SEEDS (1-4)' with ranks 1 BOS, 2 FLA, 3 TOR, and rank 4 DET prominently highlighted in a glowing Red Wings red pill with bold white text; Right column 'IN THE HUNT (5-8)' with ranks 5 TBL, 6 OTT, 7 BUF, 8 MTL. Masterclass UI design, razor-sharp editorial typography, pristine contrast, 8k UI render.
```
