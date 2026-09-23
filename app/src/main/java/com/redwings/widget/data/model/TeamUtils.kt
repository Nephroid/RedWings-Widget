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

fun teamDisplayName(abbrev: String?): String {
    if (abbrev.isNullOrBlank()) return "Opponent"
    return NHL_TEAMS[abbrev.uppercase()] ?: abbrev.uppercase()
}

fun getTeamLogoUrl(abbrev: String?): String {
    val abbr = abbrev?.uppercase()?.takeIf { it.isNotBlank() } ?: "DET"
    val safe = if (NHL_TEAMS.containsKey(abbr)) abbr else "DET"
    return "https://assets.nhle.com/logos/nhl/svg/${safe}_light.svg"
}

fun getTeamLogoUrlFallback(abbrev: String?): String {
    val abbr = abbrev?.lowercase()?.takeIf { it.isNotBlank() } ?: "det"
    return "https://a.espncdn.com/i/teamlogos/nhl/500/$abbr.png"
}

fun formatOpponentPrefix(isHome: Boolean): String = if (isHome) "vs" else "at"
