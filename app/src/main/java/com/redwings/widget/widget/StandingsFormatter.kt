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

    private data class Row(val abbr: String, val wins: Int, val losses: Int, val otl: Int, val pts: Int)

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
        val m = Regex("([A-Za-z]{2,3}):?\\s*(\\d+)[\\-\\s]+(\\d+)(?:[\\-\\s]+(\\d+))?(?:\\s*\\((\\d+)\\s*pts?\\))?")
            .find(clean)
        return if (m != null) {
            val w = m.groupValues[2].toIntOrNull() ?: 0
            val l = m.groupValues[3].toIntOrNull() ?: 0
            val o = m.groupValues[4].toIntOrNull() ?: 0
            val p = m.groupValues[5].toIntOrNull() ?: (w * 2 + o)
            Row(m.groupValues[1].uppercase(), w, l, o, p)
        } else {
            Row(clean.take(3).uppercase(), 0, 0, 0, 0)
        }
    }

    /** Top-8 Atlantic rows like "1. BOS: 30-15-5 (68pts)"; DET row is bold. */
    fun formatAtlanticLine(rawPrefsString: String?): List<CharSequence> {
        val fallback = "BOS: 30-15-5 (70pts) • TOR: 28-16-6 (62pts) • FLA: 28-17-5 (61pts) • " +
            "TB: 26-18-6 (58pts) • DET: 25-20-5 (55pts) • MTL: 24-21-5 (53pts) • " +
            "OTT: 22-23-6 (50pts) • BUF: 20-26-6 (46pts)"
        val items = (rawPrefsString ?: fallback).split("•", ",")
            .map { it.trim() }.filter { it.isNotEmpty() }
        val rows = items.map(::parse)
            .sortedWith(compareByDescending<Row> { it.pts }.thenByDescending { it.wins })
            .take(8)
        return rows.mapIndexed { i, r ->
            val line = "${i + 1}. ${r.abbr}: ${r.wins}-${r.losses}-${r.otl} (${r.pts}pts)"
            if (r.abbr == "DET") spannedOf("<b>$line</b>") else line
        }
    }

    /** "PLAYOFF: IN" when in, else "WCGB: <back>". Bold+italic for the widget. */
    fun formatWcLine(wcBack: String, playoffIn: Boolean): CharSequence {
        val text = if (playoffIn) {
            "PLAYOFF: IN"
        } else {
            val clean = wcBack
                .replace(Regex("^WCGB:?\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("^WC:?\\s*", RegexOption.IGNORE_CASE), "")
                .replace("GB", "").trim()
            "WCGB: $clean"
        }
        return spannedOf("<b><i>$text</i></b>")
    }
}
