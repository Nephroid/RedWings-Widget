# Official Detroit Red Wings Jersey Themes & Accessibility Specification

This document details the 5 official jersey themes implemented across the Detroit Red Wings Android Home Screen Widget and Companion App, their historical origins, design tokens, contrast ratios, and synchronization mechanics.

---

## 1. The 5 Official Jersey Themes

| Theme ID | Name | Chip Label | Era / Inspiration | Base Colors | Accent Colors |
| :---: | :--- | :---: | :--- | :--- | :--- |
| **0** | **Heritage 1926** | `🏛️ HERITAGE` | 1926 Detroit Cougars & 2014 Winter Classic | Vintage Cream (`#F7F3EA`), Ink (`#1A1210`) | Red Wings Red (`#CE1126`) |
| **1** | **Home Red** | `🔴 HOME` | Little Caesars Arena Official Home Sweater | Hockey Red Gradient (`#B30C20`–`#7A0614`) | Crisp White (`#FFFFFF`), Gold (`#FFD54F`) |
| **2** | **Away White** | `⚪ AWAY` | Official Road White Sweater | Ice White (`#FFFFFF`), Obsidian (`#111111`) | Red Wings Red (`#CE1126`) |
| **3** | **Reverse Retro 2.0** | `⚫ RETRO` | 2022 Adidas Alternate Black Sweater | Deep Matte Black (`#0C0D0E`) | Pure White (`#FFFFFF`), Red (`#FF3B30`) |
| **4** | **Stadium Series** | `⭐ SPECIAL` | 2016 Coors Field Special Alternate | Brushed Slate (`#1A1D24` / `#15181E`) | Silver (`#A2AAAD`), Red (`#FF2D20`) |

---

### Theme 0: Heritage 1926 (`HERITAGE`)
- **Historical Inspiration**: Commemorates the Detroit Cougars' inaugural 1926 season and the iconic 2014 NHL Winter Classic at Michigan Stadium.
- **Visual Aesthetic**: Textured vintage cream paper with a dual ink keyline and Red Wings red inner boundary.
- **Palette Tokens**:
  - Background: `#F7F3EA` (Layered cream with `#1A1210` ink border)
  - Primary / Opponent Text: `#1A1210` (Deep vintage ink)
  - Countdown & Accent: `#CE1126` (Heritage red)
  - Division Subtext: `#5A4A33` (Warm vintage tobacco brown)
  - Badge Chip: Red badge with cream text (`#F7F3EA`)

### Theme 1: Home Red (`HOME`)
- **Historical Inspiration**: The timeless red sweater worn at Little Caesars Arena, featuring iconic white piping.
- **Visual Aesthetic**: Rich radial hockey red gradient enclosed by a crisp 1.5dp pure white piping stroke.
- **Palette Tokens**:
  - Background: Radial gradient from `#B30C20` (center) to `#7A0614` (edges)
  - Primary / Countdown / Opponent Text: `#FFFFFF` (Crisp pure white)
  - Subtext & Team Names: `#F0F0F0` / `#E0E0E0`
  - Standings & DET Rank Highlight: `#FFD54F` (Arena gold)
  - Wild Card Back (WCGB): `#FF8A80`
  - Badge Chip: White badge with deep crimson text (`#9E0B1D`)

### Theme 2: Away White (`AWAY`)
- **Historical Inspiration**: The classic Detroit road sweater worn for away games across the NHL.
- **Visual Aesthetic**: Clean ice white base surrounded by a bold 1.5dp Red Wings Red border stroke.
- **Palette Tokens**:
  - Background: `#FFFFFF` (Ice white)
  - Primary & Opponent Text: `#111111` (Obsidian black)
  - Countdown & Dividers: `#CE1126` (Red Wings red)
  - Subtext: `#2E2E2E`
  - Division Teams: `#3D3D3D`
  - Badge Chip: Wings Red badge with pure white text (`#FFFFFF`)

### Theme 3: Reverse Retro 2.0 (`REVERSE_RETRO`)
- **Historical Inspiration**: The 2022 Adidas Reverse Retro 2.0 sweater honoring Detroit's 1991 Barber Pole heritage with a modern black sweater execution.
- **Visual Aesthetic**: Deep matte black base framed with a high-voltage red border stroke.
- **Palette Tokens**:
  - Background: `#0C0D0E` (Matte black)
  - Primary / Countdown / Opponent Text: `#FFFFFF` (Pure white)
  - Secondary / Subtext: `#E4E4E7` / `#D4D4D8`
  - Piping & Divider: `#FF3B30` (Vibrant red)
  - Standings & Rank Highlight: `#FFD54F` (Gold)
  - Badge Chip: Electric red badge with pure white text (`#FFFFFF`)

### Theme 4: Stadium Series (`STADIUM_SERIES`)
- **Historical Inspiration**: The 2016 outdoor NHL Stadium Series game at Coors Field against the Colorado Avalanche.
- **Visual Aesthetic**: Modern brushed charcoal slate base with dual-layer border: metallic silver outer stroke and stadium red inner keyline.
- **Palette Tokens**:
  - Background: `#1A1D24` / `#15181E` (Brushed slate)
  - Primary / Countdown / Opponent Text: `#FFFFFF` (Pure white)
  - Secondary & Division Text: `#D4D8E0` / `#C8CCD4`
  - Accent Red: `#FF2D20`
  - Outer Stroke: `#A2AAAD` (Metallic silver, 1.5dp)
  - Standings Highlight: `#FFD54F` (Gold)
  - Badge Chip: Stadium red badge with pure white text (`#FFFFFF`)

---

## 2. Permanent Resolution of Dark Theme Legibility

### The Problem in Legacy Builds
In earlier versions, dark theme layouts (such as legacy Night Black or Game Day) used medium-red text (`#E0322D`) or muted gray (`#9E9E9E`) on dark backgrounds (`#0D0202` / `#140404`). This created two critical flaws:
1. **Low Contrast Ratio (< 3.8:1)**: Below the WCAG 2.1 AA minimum threshold of 4.5:1 for regular text, making game countdowns and division records difficult to read in direct sunlight or at low screen brightness.
2. **Chromostereopsis**: Red text on black backgrounds creates optical depth distortion where red wavelengths appear at different focal planes than the dark background, causing visual strain and poor readability for users with color vision deficiencies (e.g. protanopia or deuteranopia).

### Architectural Solution: WCAG AAA Pure White Standard
To resolve this permanently across all dark themes:
1. **Standardized `#FFFFFF` Typography**: All countdown timers, game matchup titles, and opponent headers use pure white (`#FFFFFF`).
2. **Luminance-Calibrated Secondary Layers**: Secondary labels and standings use high-luminance neutral grays (`#F0F0F0`, `#E4E4E7`, `#D4D8E0`) exceeding 11:1 contrast.
3. **Luminous Standings Accents**: Detroit rank highlights and division positions use gold (`#FFD54F`), achieving > 12:1 contrast against dark surfaces.
4. **Structural Red Piping**: Red is strictly confined to borders, dividers, and decorative badges where text legibility is never compromised.

---

## 3. WCAG 2.1 Contrast Ratio Verification

All color pairings meet or exceed **WCAG 2.1 Level AAA** requirements (minimum 7.0:1 for normal text, 4.5:1 for large/bold text):

| Theme | Text Element | Foreground | Background | Contrast Ratio | WCAG Rating |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Heritage 1926** | Title / Opponent | `#1A1210` | `#F7F3EA` | **16.5:1** | **AAA** |
| **Heritage 1926** | Countdown Red | `#CE1126` | `#F7F3EA` | **5.2:1** | **AAA (Large)** |
| **Home Red** | Title / Countdown / Opponent | `#FFFFFF` | `#7A0614` | **11.3:1** | **AAA** |
| **Home Red (App)** | Title / Card Text | `#FFFFFF` | `#160305` | **18.2:1** | **AAA** |
| **Home Red** | Standings Gold | `#FFD54F` | `#7A0614` | **8.1:1** | **AAA** |
| **Away White** | Title / Opponent | `#111111` | `#FFFFFF` | **19.0:1** | **AAA** |
| **Away White** | Subtext | `#2E2E2E` | `#FFFFFF` | **12.4:1** | **AAA** |
| **Reverse Retro 2.0** | Title / Countdown / Opponent | `#FFFFFF` | `#0C0D0E` | **19.7:1** | **AAA** |
| **Reverse Retro 2.0** | Subtext | `#E4E4E7` | `#0C0D0E` | **14.8:1** | **AAA** |
| **Reverse Retro 2.0** | Standings Gold | `#FFD54F` | `#0C0D0E` | **12.2:1** | **AAA** |
| **Stadium Series** | Title / Countdown / Opponent | `#FFFFFF` | `#1A1D24` | **16.5:1** | **AAA** |
| **Stadium Series** | Subtext | `#D4D8E0` | `#1A1D24` | **11.5:1** | **AAA** |
| **Stadium Series** | Standings Gold | `#FFD54F` | `#1A1D24` | **10.2:1** | **AAA** |

---

## 4. Theme Synchronization: Widget & Companion App

Theme selection is shared between the Android Home Screen Widget and the Jetpack Compose Companion App:

```
┌─────────────────────────────────┐
│     Home Screen Widget          │
│   (RedWingsWidgetProvider)      │
│   Tapping [🏛️ / 🔴 / ⚪ / ...]  │
└────────────────┬────────────────┘
                 │
                 ▼ Intent ACTION_TOGGLE_THEME
┌─────────────────────────────────┐
│  SharedPreferences:             │
│  "RedWingsPrefs"                │
│  Key: "widget_theme_index"      │
│  Values: 0, 1, 2, 3, 4          │
└────────────────┬────────────────┘
                 │
                 ▼ Reads stored index
┌─────────────────────────────────┐
│     Companion App UI            │
│   (AppJersey / JerseyPalette)   │
│   Renders aligned theme palette │
└─────────────────────────────────┘
```

### Shared Persistence
- **Storage**: `SharedPreferences` named `"RedWingsPrefs"`.
- **Preference Key**: `"widget_theme_index"` (Integer `0..4`).

### Enumeration Mapping
Both widget and app define 1:1 matching themes:
- Widget: `com.redwings.widget.widget.WidgetTheme`
- App: `com.redwings.widget.ui.theme.AppJersey` & `JerseyPalette`

```kotlin
// Index-to-Theme Mapping
0 -> Heritage 1926     (WidgetTheme.HERITAGE       <-> AppJersey.HERITAGE)
1 -> Home Red          (WidgetTheme.HOME           <-> AppJersey.HOME)
2 -> Away White        (WidgetTheme.AWAY           <-> AppJersey.AWAY)
3 -> Reverse Retro 2.0 (WidgetTheme.REVERSE_RETRO  <-> AppJersey.REVERSE_RETRO)
4 -> Stadium Series    (WidgetTheme.STADIUM_SERIES <-> AppJersey.STADIUM_SERIES)
```

### Interactive Cycling
- Tapping the theme toggle chip on the home screen widget dispatches `ACTION_TOGGLE_THEME`.
- The receiver calculates `nextIndex = (currentIndex + 1) % 5`, saves it to `widget_theme_index`, and refreshes all active widget instances.
- The companion app reads the same index from `RedWingsPrefs` to present an identical aesthetic experience.
