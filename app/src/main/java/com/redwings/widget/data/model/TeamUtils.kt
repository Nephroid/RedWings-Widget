package com.redwings.widget.data.model

val NHL_TEAMS: Map<String, String> = mapOf(
    "ANA" to "Anaheim Ducks",
    "BOS" to "Boston Bruins",
    "BUF" to "Buffalo Sabres",
    "CAR" to "Carolina Hurricanes",
    "CBJ" to "Columbus Blue Jackets",
    "CGY" to "Calgary Flames",
    "CHI" to "Chicago Blackhawks",
    "COL" to "Colorado Avalanche",
    "DAL" to "Dallas Stars",
    "DET" to "Detroit Red Wings",
    "EDM" to "Edmonton Oilers",
    "FLA" to "Florida Panthers",
    "LAK" to "Los Angeles Kings",
    "MIN" to "Minnesota Wild",
    "MTL" to "Montreal Canadiens",
    "NSH" to "Nashville Predators",
    "NJD" to "New Jersey Devils",
    "NYI" to "New York Islanders",
    "NYR" to "New York Rangers",
    "OTT" to "Ottawa Senators",
    "PHI" to "Philadelphia Flyers",
    "PIT" to "Pittsburgh Penguins",
    "SEA" to "Seattle Kraken",
    "SJS" to "San Jose Sharks",
    "STL" to "St. Louis Blues",
    "TBL" to "Tampa Bay Lightning",
    "TOR" to "Toronto Maple Leafs",
    "UTA" to "Utah Mammoth",
    "VAN" to "Vancouver Canucks",
    "VGK" to "Vegas Golden Knights",
    "WPG" to "Winnipeg Jets",
    "WSH" to "Washington Capitals"
)

val ESPN_SLUG_MAP: Map<String, String> = mapOf(
    "LAK" to "la",
    "SJS" to "sj",
    "TBL" to "tb"
)

val NHL_TEAM_COLORS: Map<String, Long> = mapOf(
    "ANA" to 0xFFF47A38, // Ducks Orange
    "BOS" to 0xFFFFB81C, // Bruins Gold
    "BUF" to 0xFF003087, // Sabres Royal Blue
    "CAR" to 0xFFCC0000, // Canes Red
    "CBJ" to 0xFF002654, // Blue Jackets Navy
    "CGY" to 0xFFC8102E, // Flames Red
    "CHI" to 0xFFCF0A2C, // Blackhawks Red
    "COL" to 0xFF6F263D, // Avalanche Burgundy
    "DAL" to 0xFF006847, // Stars Victory Green
    "DET" to 0xFFCE1126, // Red Wings Crimson
    "EDM" to 0xFFFF4C00, // Oilers Orange
    "FLA" to 0xFF041E42, // Panthers Navy
    "LAK" to 0xFF111111, // Kings Black
    "MIN" to 0xFF154734, // Wild Forest Green
    "MTL" to 0xFFAF1E2D, // Canadiens Bleu Blanc Rouge
    "NSH" to 0xFFFFB81C, // Predators Gold
    "NJD" to 0xFFCE1126, // Devils Red
    "NYI" to 0xFF00539B, // Islanders Royal Blue
    "NYR" to 0xFF0038A8, // Rangers Blue
    "OTT" to 0xFFC52032, // Senators Red
    "PHI" to 0xFFF74902, // Flyers Orange
    "PIT" to 0xFFFCB514, // Penguins Gold
    "SEA" to 0xFF001628, // Kraken Deep Sea Blue
    "SJS" to 0xFF006D75, // Sharks Deep Pacific Teal
    "STL" to 0xFF002F87, // Blues Royal Blue
    "TBL" to 0xFF002868, // Lightning Tampa Bay Blue
    "TOR" to 0xFF00205B, // Maple Leafs Blue
    "UTA" to 0xFF010101, // Utah Black
    "VAN" to 0xFF00205B, // Canucks Blue
    "VGK" to 0xFFB4975A, // Golden Knights Gold
    "WPG" to 0xFF041E42, // Jets Polar Night Blue
    "WSH" to 0xFF041E42  // Capitals Navy
)

fun getTeamColor(abbrev: String?): Long {
    val upper = abbrev?.uppercase()?.takeIf { it.isNotBlank() } ?: "DET"
    return NHL_TEAM_COLORS[upper] ?: 0xFFCE1126L
}

fun teamDisplayName(abbrev: String?): String {
    if (abbrev.isNullOrBlank()) return "Opponent"
    return NHL_TEAMS[abbrev.uppercase()] ?: abbrev.uppercase()
}

fun getTeamLogoUrl(abbrev: String?): String {
    val upper = abbrev?.uppercase()?.takeIf { it.isNotBlank() } ?: "DET"
    val safe = ESPN_SLUG_MAP[upper] ?: if (NHL_TEAMS.containsKey(upper)) upper.lowercase() else "det"
    return "https://a.espncdn.com/i/teamlogos/nhl/500/$safe.png"
}

fun getTeamLogoUrlFallback(abbrev: String?): String {
    val upper = abbrev?.uppercase()?.takeIf { it.isNotBlank() } ?: "DET"
    val safe = if (NHL_TEAMS.containsKey(upper)) upper else "DET"
    return "https://assets.nhle.com/logos/nhl/svg/${safe}_light.svg"
}

fun formatOpponentPrefix(isHome: Boolean): String = if (isHome) "vs" else "at"

