package com.redwings.widget.widget

import com.redwings.widget.R

/**
 * 5 official Detroit Red Wings jersey themes:
 * 0. Heritage 1926: Vintage cream & red (1926 Cougars / Winter Classic).
 * 1. Home Red: Official home red sweater with crisp white piping & text.
 * 2. Away White: Official road white sweater with wings red accents.
 * 3. Reverse Retro: 2022 Adidas black sweater with ultra-high contrast pure white text.
 * 4. Stadium Series: 2016 Coors Field special alternate with silver & stadium red trim.
 */
enum class WidgetTheme(
    val id: Int,
    val displayName: String,
    val buttonLabel: String,
    val isMaterialYou: Boolean,
    val bgDrawableRes: Int,
    val tagDrawableRes: Int,
    val titleColorRes: Int,
    val countdownColorRes: Int,
    val opponentColorRes: Int,
    val dividerColorRes: Int,
    val subColorRes: Int,
    val highlightHex: String?,
    val standingColorRes: Int,
    val teamColorRes: Int,
    val teamDetRes: Int,
    val wcgbColorRes: Int,
    val tagTextColorRes: Int,
    val headerDrawableRes: Int = R.drawable.widget_header_ribbon,
    val matchupCardDrawableRes: Int = R.drawable.widget_matchup_frosted_bg,
    val standingsCardDrawableRes: Int = R.drawable.widget_standings_frosted_bg,
    val venuePillDrawableRes: Int = R.drawable.widget_venue_pill,
    val detHighlightPillRes: Int = R.drawable.widget_det_highlight_pill,
    val matchupBadgeTextColorRes: Int = R.color.redwings_white,
    val stripeColorRes: Int = R.color.redwings_white
) {
    HERITAGE(
        id = 0, displayName = "Heritage 1926", buttonLabel = "🏛️ HERITAGE",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_heritage,
        tagDrawableRes = R.drawable.widget_tag_heritage,
        titleColorRes = R.color.widget_heritage_title,
        countdownColorRes = R.color.widget_heritage_countdown,
        opponentColorRes = R.color.widget_heritage_opponent,
        dividerColorRes = R.color.widget_heritage_divider,
        subColorRes = R.color.widget_heritage_sub,
        highlightHex = "#CE1126",
        standingColorRes = R.color.widget_heritage_standing,
        teamColorRes = R.color.widget_heritage_team,
        teamDetRes = R.color.widget_heritage_team_det,
        wcgbColorRes = R.color.widget_heritage_wcgb,
        tagTextColorRes = R.color.widget_heritage_tag_text,
        headerDrawableRes = R.drawable.widget_header_heritage,
        matchupCardDrawableRes = R.drawable.widget_matchup_heritage_bg,
        standingsCardDrawableRes = R.drawable.widget_standings_heritage_bg,
        venuePillDrawableRes = R.drawable.widget_venue_pill_heritage,
        detHighlightPillRes = R.drawable.widget_det_highlight_heritage,
        matchupBadgeTextColorRes = R.color.redwings_white,
        stripeColorRes = R.color.widget_heritage_title
    ),
    HOME(
        id = 1, displayName = "Home Red", buttonLabel = "🔴 HOME",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_home,
        tagDrawableRes = R.drawable.widget_tag_home,
        titleColorRes = R.color.widget_home_title,
        countdownColorRes = R.color.widget_home_countdown,
        opponentColorRes = R.color.widget_home_opponent,
        dividerColorRes = R.color.widget_home_divider,
        subColorRes = R.color.widget_home_sub,
        highlightHex = "#FFFFFF",
        standingColorRes = R.color.widget_home_standing,
        teamColorRes = R.color.widget_home_team,
        teamDetRes = R.color.widget_home_team_det,
        wcgbColorRes = R.color.widget_home_wcgb,
        tagTextColorRes = R.color.widget_home_tag_text,
        headerDrawableRes = R.drawable.widget_header_ribbon,
        matchupCardDrawableRes = R.drawable.widget_matchup_frosted_bg,
        standingsCardDrawableRes = R.drawable.widget_standings_frosted_bg,
        venuePillDrawableRes = R.drawable.widget_venue_pill,
        detHighlightPillRes = R.drawable.widget_det_highlight_pill,
        matchupBadgeTextColorRes = R.color.redwings_white,
        stripeColorRes = R.color.redwings_white
    ),
    AWAY(
        id = 2, displayName = "Away White", buttonLabel = "⚪ AWAY",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_away,
        tagDrawableRes = R.drawable.widget_tag_away,
        titleColorRes = R.color.widget_away_title,
        countdownColorRes = R.color.widget_away_countdown,
        opponentColorRes = R.color.widget_away_opponent,
        dividerColorRes = R.color.widget_away_divider,
        subColorRes = R.color.widget_away_sub,
        highlightHex = "#CE1126",
        standingColorRes = R.color.widget_away_standing,
        teamColorRes = R.color.widget_away_team,
        teamDetRes = R.color.widget_away_team_det,
        wcgbColorRes = R.color.widget_away_wcgb,
        tagTextColorRes = R.color.widget_away_tag_text,
        headerDrawableRes = R.drawable.widget_header_ribbon,
        matchupCardDrawableRes = R.drawable.widget_matchup_away_bg,
        standingsCardDrawableRes = R.drawable.widget_standings_away_bg,
        venuePillDrawableRes = R.drawable.widget_venue_pill_away,
        detHighlightPillRes = R.drawable.widget_det_highlight_pill,
        matchupBadgeTextColorRes = R.color.redwings_white,
        stripeColorRes = R.color.redwings_white
    ),
    REVERSE_RETRO(
        id = 3, displayName = "Reverse Retro", buttonLabel = "⚫ RETRO",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_reverse_retro,
        tagDrawableRes = R.drawable.widget_tag_reverse_retro,
        titleColorRes = R.color.widget_retro_title,
        countdownColorRes = R.color.widget_retro_countdown,
        opponentColorRes = R.color.widget_retro_opponent,
        dividerColorRes = R.color.widget_retro_divider,
        subColorRes = R.color.widget_retro_sub,
        highlightHex = "#FF3B30",
        standingColorRes = R.color.widget_retro_standing,
        teamColorRes = R.color.widget_retro_team,
        teamDetRes = R.color.widget_retro_team_det,
        wcgbColorRes = R.color.widget_retro_wcgb,
        tagTextColorRes = R.color.widget_retro_tag_text,
        headerDrawableRes = R.drawable.widget_header_ribbon,
        matchupCardDrawableRes = R.drawable.widget_matchup_dark_bg,
        standingsCardDrawableRes = R.drawable.widget_standings_dark_bg,
        venuePillDrawableRes = R.drawable.widget_venue_pill_dark,
        detHighlightPillRes = R.drawable.widget_det_highlight_pill,
        matchupBadgeTextColorRes = R.color.redwings_white,
        stripeColorRes = R.color.redwings_white
    ),
    STADIUM_SERIES(
        id = 4, displayName = "Stadium Series", buttonLabel = "⭐ SPECIAL",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_stadium_series,
        tagDrawableRes = R.drawable.widget_tag_stadium_series,
        titleColorRes = R.color.widget_stadium_title,
        countdownColorRes = R.color.widget_stadium_countdown,
        opponentColorRes = R.color.widget_stadium_opponent,
        dividerColorRes = R.color.widget_stadium_divider,
        subColorRes = R.color.widget_stadium_sub,
        highlightHex = "#FF2D20",
        standingColorRes = R.color.widget_stadium_standing,
        teamColorRes = R.color.widget_stadium_team,
        teamDetRes = R.color.widget_stadium_team_det,
        wcgbColorRes = R.color.widget_stadium_wcgb,
        tagTextColorRes = R.color.widget_stadium_tag_text,
        headerDrawableRes = R.drawable.widget_header_ribbon,
        matchupCardDrawableRes = R.drawable.widget_matchup_dark_bg,
        standingsCardDrawableRes = R.drawable.widget_standings_dark_bg,
        venuePillDrawableRes = R.drawable.widget_venue_pill_dark,
        detHighlightPillRes = R.drawable.widget_det_highlight_pill,
        matchupBadgeTextColorRes = R.color.redwings_white,
        stripeColorRes = R.color.redwings_white
    );

    companion object {
        fun fromIndex(index: Int): WidgetTheme {
            val all = values()
            return all[((index % all.size) + all.size) % all.size]
        }
    }
}
