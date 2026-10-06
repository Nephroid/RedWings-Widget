package com.redwings.widget.widget

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * Pure formatter for Atlantic Division standings. No Context, no prefs —
 * provider reads prefs and passes raw strings in.
 *
 * Expected raw row: "BOS: 30-15-5 (68pts)" or "BOS 30-15-5". Rows separated
 * by "•" or ",". Returns up to 8 ranked rows; provider shows the first 5 in
 * team_1..team_5 and the WC line in team_6.
 */
object StandingsFormatter {

    private data class Row(
        val abbr: String,
        val wins: Int,
        val losses: Int,
        val otl: Int,
        val pts: Int,
        val goalDiff: Int = 0
    )

    @Suppress("DEPRECATION")
    private fun html(s: String): CharSequence =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(s)
        }

    private fun spannedOf(s: String): Spanned = html(s) as? Spanned ?: html(s) as Spanned

    private fun parse(rawItem: String): Row {
        val clean = rawItem.replace(Regex("^\\d+[\\.\\s]\\s*"), "").trim()
        val diffMatch = Regex("(?:\\[diff:?\\s*|diff:?\\s*)([+-]?\\d+)\\]?", RegexOption.IGNORE_CASE).find(clean)
        val goalDiff = diffMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val m = Regex("([A-Za-z]{2,3}):?\\s*(\\d+)[\\-\\s]+(\\d+)(?:[\\-\\s]+(\\d+))?(?:\\s*\\((\\d+)\\s*pts?\\))?")
            .find(clean)
        return if (m != null) {
            val w = m.groupValues[2].toIntOrNull() ?: 0
            val l = m.groupValues[3].toIntOrNull() ?: 0
            val o = m.groupValues[4].toIntOrNull() ?: 0
            val p = m.groupValues[5].toIntOrNull() ?: (w * 2 + o)
            Row(m.groupValues[1].uppercase(), w, l, o, p, goalDiff)
        } else {
            val m2 = Regex("([A-Za-z]{2,3}):?\\s*(\\d+)\\s*pts", RegexOption.IGNORE_CASE).find(clean)
            if (m2 != null) {
                val abbr = m2.groupValues[1].uppercase()
                val pts = m2.groupValues[2].toIntOrNull() ?: 0
                Row(abbr, 0, 0, 0, pts, goalDiff)
            } else {
                Row(clean.take(3).uppercase(), 0, 0, 0, 0, goalDiff)
            }
        }
    }

    /** Top-8 Atlantic rows like "1. BOS: 51-20-11"; DET row is bold. */
    fun formatAtlanticLine(rawPrefsString: String?): List<CharSequence> {
        val fallback = "BOS: 51-20-11 (113pts) • TOR: 46-26-10 (102pts) • FLA: 45-27-10 (100pts) • " +
            "DET: 42-30-10 (94pts) • TBL: 40-32-10 (90pts) • MTL: 37-36-9 (83pts) • " +
            "OTT: 34-39-9 (77pts) • BUF: 30-43-9 (69pts)"
        val validString = rawPrefsString?.takeIf {
            it.isNotBlank() && !it.contains("unavailable") && it.contains("•") && it.length > 30
        } ?: fallback
        val items = validString.split("•", ",")
            .map { it.trim() }.filter { it.isNotEmpty() }
        val parsedRows = items.map(::parse)
        val rows = if (parsedRows.size >= 4) {
            parsedRows
        } else {
            fallback.split("•").map { it.trim() }.filter { it.isNotEmpty() }.map(::parse)
        }.sortedWith(compareByDescending<Row> { it.pts }.thenByDescending { it.wins }).take(8)

        return rows.mapIndexed { i, r ->
            val recordStr = if (r.wins > 0 || r.losses > 0 || r.otl > 0) {
                "${r.wins}-${r.losses}-${r.otl}"
            } else {
                "${r.pts} pts"
            }
            val line = "${i + 1}. ${r.abbr}: $recordStr"
            if (r.abbr == "DET") spannedOf("<b>$line</b>") else line
        }
    }

    /** "CLINCHED" when in playoffs, else "X pts out" or "In playoff hunt". Bold+italic for widget. */
    fun formatPlayoffChase(ptsBack: String, playoffIn: Boolean): CharSequence {
        val text = when {
            playoffIn -> "CLINCHED"
            ptsBack.equals("IN", ignoreCase = true) -> "CLINCHED"
            ptsBack == "-" || ptsBack.isBlank() || ptsBack == "--" -> "In playoff hunt"
            else -> {
                val clean = ptsBack
                    .replace(Regex("^WCGB:?\\s*", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("^WC:?\\s*", RegexOption.IGNORE_CASE), "")
                    .replace("GB", "", ignoreCase = true).trim()
                if (clean.toIntOrNull() != null) "$clean pts out" else "$clean out"
            }
        }
        return spannedOf("<b><i>$text</i></b>")
    }

    /** Legacy alias for backwards compatibility with existing calls / tests. */
    fun formatWcLine(wcBack: String, playoffIn: Boolean): CharSequence =
        formatPlayoffChase(wcBack, playoffIn)

    /** Parses standings string into typed rows for Compose UI dashboard. */
    fun parseAtlanticRowsForUi(rawPrefsString: String?): List<com.redwings.widget.ui.StandingsRowUi> {
        val fallback = "BOS: 51-20-11 (113pts) • TOR: 46-26-10 (102pts) • FLA: 45-27-10 (100pts) • " +
            "DET: 42-30-10 (94pts) • TBL: 40-32-10 (90pts) • MTL: 37-36-9 (83pts) • " +
            "OTT: 34-39-9 (77pts) • BUF: 30-43-9 (69pts)"
        val validString = rawPrefsString?.takeIf {
            it.isNotBlank() && !it.contains("unavailable") && it.contains("•") && it.length > 30
        } ?: fallback
        val items = validString.split("•", ",")
            .map { it.trim() }.filter { it.isNotEmpty() }
        val parsedRows = items.map(::parse)
        val rows = if (parsedRows.size >= 4) {
            parsedRows
        } else {
            fallback.split("•").map { it.trim() }.filter { it.isNotEmpty() }.map(::parse)
        }.sortedWith(compareByDescending<Row> { it.pts }.thenByDescending { it.wins }).take(8)

        return rows.mapIndexed { i, r ->
            val gp = (r.wins + r.losses + r.otl)
            com.redwings.widget.ui.StandingsRowUi(
                rank = i + 1,
                teamAbbrev = r.abbr,
                gamesPlayed = if (gp > 0) gp else if (r.pts > 0) 1 else 0,
                wins = r.wins,
                losses = r.losses,
                otLosses = r.otl,
                points = r.pts,
                isRedWings = r.abbr == "DET",
                goalDiff = r.goalDiff
            )
        }
    }
}
