package com.redwings.widget.widget

import com.redwings.widget.R

/**
 * 5 widget themes: 4 static + 1 Material You dynamic.
 *
 * Tight-drawables note: heritage has its own bg/tag XML (cream frame + red
 * seal); the other 4 entries share widget_bg_classic / widget_tag_classic and
 * differ via text colors + highlightHex. HERITAGE is first so index 0 (the
 * default) is the Heritage 1926 look.
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
    val tagTextColorRes: Int
) {
    HERITAGE(
        id = 0, displayName = "Heritage 1926", buttonLabel = "\uD83C\uDFA8 HERITAGE",
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
        tagTextColorRes = R.color.widget_heritage_tag_text
    ),
    CLASSIC_RED(
        id = 1, displayName = "Classic Red", buttonLabel = "\uD83C\uDFA8 CLASSIC",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_classic,
        tagDrawableRes = R.drawable.widget_tag_classic,
        titleColorRes = R.color.widget_classic_title,
        countdownColorRes = R.color.widget_classic_countdown,
        opponentColorRes = R.color.widget_classic_opponent,
        dividerColorRes = R.color.widget_classic_divider,
        subColorRes = R.color.widget_classic_sub,
        highlightHex = "#CE1126",
        standingColorRes = R.color.widget_classic_standing,
        teamColorRes = R.color.widget_classic_team,
        teamDetRes = R.color.widget_classic_team_det,
        wcgbColorRes = R.color.widget_classic_wcgb,
        tagTextColorRes = R.color.widget_classic_tag_text
    ),
    WHITEOUT(
        id = 2, displayName = "Motor City White", buttonLabel = "\uD83C\uDFA8 WHITEOUT",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_classic,
        tagDrawableRes = R.drawable.widget_tag_classic,
        titleColorRes = R.color.widget_whiteout_title,
        countdownColorRes = R.color.widget_whiteout_countdown,
        opponentColorRes = R.color.widget_whiteout_opponent,
        dividerColorRes = R.color.widget_whiteout_divider,
        subColorRes = R.color.widget_whiteout_sub,
        highlightHex = "#CE1126",
        standingColorRes = R.color.widget_whiteout_standing,
        teamColorRes = R.color.widget_whiteout_team,
        teamDetRes = R.color.widget_whiteout_team_det,
        wcgbColorRes = R.color.widget_whiteout_wcgb,
        tagTextColorRes = R.color.widget_whiteout_tag_text
    ),
    NIGHT(
        id = 3, displayName = "Night Black", buttonLabel = "\uD83C\uDFA8 NIGHT",
        isMaterialYou = false,
        bgDrawableRes = R.drawable.widget_bg_classic,
        tagDrawableRes = R.drawable.widget_tag_classic,
        titleColorRes = R.color.widget_night_title,
        countdownColorRes = R.color.widget_night_countdown,
        opponentColorRes = R.color.widget_night_opponent,
        dividerColorRes = R.color.widget_night_divider,
        subColorRes = R.color.widget_night_sub,
        highlightHex = "#E0322D",
        standingColorRes = R.color.widget_night_standing,
        teamColorRes = R.color.widget_night_team,
        teamDetRes = R.color.widget_night_team_det,
        wcgbColorRes = R.color.widget_night_wcgb,
        tagTextColorRes = R.color.widget_night_tag_text
    ),
    MY_DYNAMIC(
        id = 4, displayName = "Material You", buttonLabel = "\uD83C\uDFA8 DYNAMIC",
        isMaterialYou = true,
        bgDrawableRes = R.drawable.widget_bg_classic,
        tagDrawableRes = R.drawable.widget_tag_classic,
        titleColorRes = R.color.widget_my_title,
        countdownColorRes = R.color.widget_my_countdown,
        opponentColorRes = R.color.widget_my_opponent,
        dividerColorRes = R.color.widget_my_divider,
        subColorRes = R.color.widget_my_sub,
        highlightHex = null,
        standingColorRes = R.color.widget_my_standing,
        teamColorRes = R.color.widget_my_team,
        teamDetRes = R.color.widget_my_team_det,
        wcgbColorRes = R.color.widget_my_wcgb,
        tagTextColorRes = R.color.widget_my_tag_text
    );

    companion object {
        fun fromIndex(index: Int): WidgetTheme {
            val all = values()
            return all[((index % all.size) + all.size) % all.size]
        }
    }
}
