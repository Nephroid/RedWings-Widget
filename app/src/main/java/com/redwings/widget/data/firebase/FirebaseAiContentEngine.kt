package com.redwings.widget.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.redwings.widget.data.model.GameStarUi
import com.redwings.widget.data.model.TeamLeaderPlayerUi
import com.redwings.widget.data.model.TeamLeadersUi
import com.redwings.widget.data.repository.HockeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class GameIntelligenceState(
    val matchupStoryline: String = "",
    val keyBattle: String = "",
    val dynamicRecapPill: String = "",
    val starCommentaries: Map<Int, String> = emptyMap(),
    val milestonePacing: Map<Int, String> = emptyMap()
)

class FirebaseAiContentEngine(
    private val context: Context,
    private val remoteConfigManager: RemoteConfigManager? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val prefs = context.getSharedPreferences(HockeyRepository.PREFS, Context.MODE_PRIVATE)
    private val _intelligenceState = MutableStateFlow(loadCachedIntelligence())
    val intelligenceState: StateFlow<GameIntelligenceState> = _intelligenceState.asStateFlow()

    private fun loadCachedIntelligence(): GameIntelligenceState {
        return GameIntelligenceState(
            matchupStoryline = prefs.getString("ai_matchup_storyline", "") ?: "",
            keyBattle = prefs.getString("ai_key_battle", "") ?: "",
            dynamicRecapPill = prefs.getString("ai_recap_pill", "") ?: "",
            starCommentaries = loadStarCommentaries(),
            milestonePacing = loadMilestonePacing()
        )
    }

    private fun loadStarCommentaries(): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        for (star in 1..3) {
            val text = prefs.getString("ai_star_${star}_commentary", null)
            if (!text.isNullOrBlank()) {
                map[star] = text
            }
        }
        return map
    }

    private fun loadMilestonePacing(): Map<Int, String> {
        val count = prefs.getInt("ai_pacing_count", 0)
        val map = mutableMapOf<Int, String>()
        for (i in 0 until count) {
            val pid = prefs.getInt("ai_pacing_id_$i", -1)
            val text = prefs.getString("ai_pacing_text_$i", null)
            if (pid != -1 && !text.isNullOrBlank()) {
                map[pid] = text
            }
        }
        return map
    }

    /**
     * Generates or fetches pre-game matchup preview.
     * Guaranteed zero UI latency: immediately populates local heuristics, then refines with Gemini in background.
     */
    fun refreshPreGameIntelligence(
        opponentName: String,
        opponentAbbrev: String,
        isHome: Boolean,
        detRecord: String,
        oppRecord: String
    ) {
        val cachedOpponent = prefs.getString("ai_matchup_opponent_abbrev", "") ?: ""
        val isNewOpponent = !cachedOpponent.equals(opponentAbbrev, ignoreCase = true)

        // Step 1: Immediate local heuristic preview
        val heuristicStoryline = generateLocalMatchupStoryline(opponentName, opponentAbbrev, isHome, detRecord, oppRecord)
        val heuristicBattle = generateLocalKeyBattle(opponentAbbrev)

        if (isNewOpponent || _intelligenceState.value.matchupStoryline.isBlank()) {
            val updated = _intelligenceState.value.copy(
                matchupStoryline = heuristicStoryline,
                keyBattle = heuristicBattle
            )
            _intelligenceState.value = updated
            savePreGamePrefs(opponentAbbrev, heuristicStoryline, heuristicBattle)
        }

        // Step 2: Background Gemini AI generation
        scope.launch {
            try {
                val modelName = "gemini-2.5-flash"
                val model = Firebase.ai.generativeModel(modelName)
                val phase = getSeasonPhase()
                val prompt = """
                    You are a sharp, analytical Detroit Red Wings hockey beat writer.
                    Write a punchy 1-sentence pre-game storyline and identify 1 key player battle for:
                    Opponent: $opponentName ($opponentAbbrev)
                    Location: ${if (isHome) "Home at Little Caesars Arena" else "Away at $opponentName"}
                    Detroit record: $detRecord, Opponent record: $oppRecord.
                    Season Phase: $phase
                    
                    Format your response EXACTLY as:
                    STORYLINE: <1 sentence storyline reflecting the season phase and stakes>
                    BATTLE: <Player vs Player>
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text.orEmpty()
                if (text.isNotBlank()) {
                    var aiStoryline = heuristicStoryline
                    var aiBattle = heuristicBattle
                    text.lines().forEach { line ->
                        when {
                            line.startsWith("STORYLINE:", ignoreCase = true) -> {
                                aiStoryline = line.substringAfter(":").trim()
                            }
                            line.startsWith("BATTLE:", ignoreCase = true) -> {
                                aiBattle = line.substringAfter(":").removePrefix("Key Battle:").removePrefix("•").trim()
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _intelligenceState.value = _intelligenceState.value.copy(
                            matchupStoryline = aiStoryline,
                            keyBattle = aiBattle
                        )
                    }
                    savePreGamePrefs(opponentAbbrev, aiStoryline, aiBattle)
                    Log.d("FirebaseAiContentEngine", "Generated AI pre-game preview: $aiStoryline")
                }
            } catch (t: Throwable) {
                Log.w("FirebaseAiContentEngine", "AI generation fallback to heuristics: ${t.message}")
            }
        }
    }

    /**
     * Generates or fetches post-game 3-Stars insights and dynamic recap pill.
     */
    fun refreshPostGameIntelligence(
        wingsScore: Int,
        oppScore: Int,
        oppAbbrev: String,
        stars: List<GameStarUi>
    ) {
        val heuristicPill = generateLocalRecapPill(wingsScore, oppScore)
        val heuristicCommentaries = generateLocalStarCommentaries(stars)

        val updated = _intelligenceState.value.copy(
            dynamicRecapPill = heuristicPill,
            starCommentaries = heuristicCommentaries
        )
        _intelligenceState.value = updated
        savePostGamePrefs(heuristicPill, heuristicCommentaries)

        scope.launch {
            try {
                val model = Firebase.ai.generativeModel("gemini-2.5-flash")
                val starLines = stars.joinToString("; ") { "★${it.star} ${it.name} (${it.teamAbbrev}, ${it.statLine})" }
                val prompt = """
                    You are a Detroit Red Wings beat reporter.
                    Game: DET $wingsScore - $oppAbbrev $oppScore.
                    3 Stars: $starLines.
                    Generate:
                    1. A punchy 2-3 word RECAP_TAG (e.g. 'Overtime Thriller', 'Third-Period Surge', 'Clinical Road Win').
                    2. A 1-sentence insight for each of the 3 stars.
                    
                    CRITICAL: Avoid generic clichés like 'electrifying presence', 'stood tall in the crease', or 'valuable performance'. Be specific to each player's actual in-game hockey contribution and stat line.
                    
                    Format:
                    TAG: <2-3 words>
                    STAR1: <1 sentence>
                    STAR2: <1 sentence>
                    STAR3: <1 sentence>
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text.orEmpty()
                if (text.isNotBlank()) {
                    var pill = heuristicPill
                    val commentaries = heuristicCommentaries.toMutableMap()
                    text.lines().forEach { line ->
                        when {
                            line.startsWith("TAG:", ignoreCase = true) -> pill = line.substringAfter(":").trim()
                            line.startsWith("STAR1:", ignoreCase = true) -> commentaries[1] = line.substringAfter(":").trim()
                            line.startsWith("STAR2:", ignoreCase = true) -> commentaries[2] = line.substringAfter(":").trim()
                            line.startsWith("STAR3:", ignoreCase = true) -> commentaries[3] = line.substringAfter(":").trim()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _intelligenceState.value = _intelligenceState.value.copy(
                            dynamicRecapPill = pill,
                            starCommentaries = commentaries
                        )
                    }
                    savePostGamePrefs(pill, commentaries)
                }
            } catch (t: Throwable) {
                Log.w("FirebaseAiContentEngine", "AI post-game fallback: ${t.message}")
            }
        }
    }

    /**
     * Calculates 82-game pacing projections for DRW Season Leaders.
     */
    fun calculatePacingProjections(leaders: TeamLeadersUi): Map<Int, String> {
        val pacing = mutableMapOf<Int, String>()
        val allSkaters = (leaders.topGoals + leaders.topAssists + leaders.topPoints).distinctBy { it.playerId }
        for (player in allSkaters) {
            val gp = extractGamesPlayed(player.secondaryStat)
            val goals = extractGoals(player.secondaryStat, player.primaryStat)
            val assists = extractAssists(player.secondaryStat, player.primaryStat)
            val points = extractPoints(player.secondaryStat, player.primaryStat)

            if (gp > 0) {
                val paceGoals = (goals * 82.0 / gp).roundToInt()
                val pacePoints = (points * 82.0 / gp).roundToInt()
                val paceAssists = (assists * 82.0 / gp).roundToInt()

                val projection = when {
                    pacePoints >= 80 -> "82-Game Pace: $paceGoals G • $paceAssists A • $pacePoints PTS (Elite Milestone)"
                    paceGoals >= 30 -> "82-Game Pace: $paceGoals G (30-Goal Pace) • $pacePoints PTS"
                    else -> "82-Game Pace: $paceGoals G • $paceAssists A • $pacePoints PTS"
                }
                pacing[player.playerId] = projection
            }
        }
        _intelligenceState.value = _intelligenceState.value.copy(milestonePacing = pacing)
        savePacingPrefs(pacing)
        return pacing
    }

    private fun extractGamesPlayed(stat: String): Int {
        val match = Regex("(\\d+)\\s*GP").find(stat)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 70
    }

    private fun extractGoals(sec: String, pri: String): Int {
        val match = Regex("(\\d+)\\s*G").find(sec) ?: Regex("(\\d+)\\s*G").find(pri)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 20
    }

    private fun extractAssists(sec: String, pri: String): Int {
        val match = Regex("(\\d+)\\s*A").find(sec) ?: Regex("(\\d+)\\s*A").find(pri)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 30
    }

    private fun extractPoints(sec: String, pri: String): Int {
        val match = Regex("(\\d+)\\s*PTS").find(sec) ?: Regex("(\\d+)\\s*PTS").find(pri)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 50
    }

    enum class SeasonPhase {
        EARLY_SEASON,
        WINTER_GRIND,
        PLAYOFF_CHASE,
        OFF_SEASON
    }

    private fun getSeasonPhase(): SeasonPhase {
        val calendar = java.util.Calendar.getInstance()
        return when (calendar.get(java.util.Calendar.MONTH)) {
            java.util.Calendar.OCTOBER, java.util.Calendar.NOVEMBER -> SeasonPhase.EARLY_SEASON
            java.util.Calendar.DECEMBER, java.util.Calendar.JANUARY -> SeasonPhase.WINTER_GRIND
            java.util.Calendar.FEBRUARY, java.util.Calendar.MARCH, java.util.Calendar.APRIL -> SeasonPhase.PLAYOFF_CHASE
            else -> SeasonPhase.OFF_SEASON
        }
    }

    private fun generateLocalMatchupStoryline(name: String, abbr: String, isHome: Boolean, detRecord: String, oppRecord: String): String {
        val venue = if (isHome) "at Little Caesars Arena" else "on the road"
        val detRecClean = detRecord.trim().takeIf { it.isNotEmpty() && it != "--" }
        val oppRecClean = oppRecord.trim().takeIf { it.isNotEmpty() && it != "--" }
        val detText = if (detRecClean != null) "Detroit ($detRecClean)" else "Detroit"
        val oppText = if (oppRecClean != null) "$name ($oppRecClean)" else name

        val isAtlantic = abbr.uppercase() in listOf("BOS", "TOR", "TBL", "FLA", "MTL", "OTT", "BUF")
        val isOriginalSix = abbr.uppercase() in listOf("BOS", "CHI", "MTL", "NYR", "TOR")
        val phase = getSeasonPhase()

        return when {
            isAtlantic && phase == SeasonPhase.PLAYOFF_CHASE ->
                "High-stakes Atlantic Division clash $venue as $detText battles $oppText with massive four-point wild card implications."
            isAtlantic && phase == SeasonPhase.WINTER_GRIND ->
                "Intense Atlantic Division showdown $venue as $detText looks to out-grit rival $oppText through the grueling mid-season stretch."
            isAtlantic ->
                "Crucial Atlantic Division test $venue as $detText sets the early division pace against $oppText."
            isOriginalSix ->
                "Historic Original Six rivalry takes center stage $venue as $detText clashes with $oppText."
            phase == SeasonPhase.PLAYOFF_CHASE ->
                "Every regulation point is critical $venue as $detText pushes for playoff position against cross-conference challenger $oppText."
            phase == SeasonPhase.WINTER_GRIND ->
                "The Red Wings take the ice $venue, seeking a gritty statement win and two key points against $oppText."
            else ->
                "$detText faces off $venue against $oppText, looking to establish aggressive 5-on-5 tempo from the opening puck drop."
        }
    }

    private fun generateLocalKeyBattle(oppAbbr: String): String {
        return when (oppAbbr.uppercase()) {
            // Atlantic Division
            "TOR" -> "Larkin vs. Matthews"
            "BOS" -> "Seider vs. Pastrnak"
            "TBL" -> "Raymond vs. Kucherov"
            "FLA" -> "Seider vs. Barkov"
            "OTT" -> "DeBrincat vs. Tkachuk"
            "MTL" -> "Larkin vs. Suzuki"
            "BUF" -> "Larkin vs. Thompson"
            // Metropolitan Division
            "NYR" -> "Seider vs. Panarin"
            "CAR" -> "Larkin vs. Aho"
            "NJD" -> "Raymond vs. J. Hughes"
            "NYI" -> "Larkin vs. Barzal"
            "PIT" -> "Kane vs. Crosby"
            "WSH" -> "Seider vs. Ovechkin"
            "PHI" -> "Raymond vs. Konecny"
            "CBJ" -> "Larkin vs. Fantilli"
            // Central Division
            "COL" -> "Larkin vs. MacKinnon"
            "DAL" -> "Larkin vs. Robertson"
            "WPG" -> "Seider vs. Connor"
            "MIN" -> "Seider vs. Kaprizov"
            "NSH" -> "Larkin vs. Forsberg"
            "STL" -> "Raymond vs. Thomas"
            "CHI" -> "Kane vs. Bedard"
            "UTA" -> "Larkin vs. Keller"
            // Pacific Division
            "EDM" -> "Seider vs. McDavid"
            "VAN" -> "Seider vs. Q. Hughes"
            "VGK" -> "Larkin vs. Eichel"
            "LAK" -> "Larkin vs. Kopitar"
            "CGY" -> "Raymond vs. Kadri"
            "SEA" -> "Larkin vs. Beniers"
            "ANA" -> "Raymond vs. Carlsson"
            "SJS" -> "Seider vs. Celebrini"
            else -> "Top Line Execution vs. Opponent Defense"
        }
    }

    private fun generateLocalRecapPill(wingsScore: Int, oppScore: Int): String {
        return when {
            wingsScore > oppScore && wingsScore - oppScore == 1 -> "One-Goal Nail-Biter"
            wingsScore > oppScore && wingsScore >= 5 -> "Offensive Explosion"
            wingsScore > oppScore -> "Decisive Victory"
            oppScore - wingsScore == 1 -> "Hard-Fought Battle"
            else -> "Tough Divisional Test"
        }
    }

    private fun generateLocalStarCommentaries(stars: List<GameStarUi>): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        val usedPhrases = mutableSetOf<String>()

        stars.forEach { star ->
            val isGoalie = star.position.equals("G", ignoreCase = true) || star.statLine.contains("SV")
            val isDefense = star.position.equals("D", ignoreCase = true)
            val goals = Regex("(\\d+)\\s*G").find(star.statLine)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val assists = Regex("(\\d+)\\s*A").find(star.statLine)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val saves = Regex("(\\d+)\\s*SV").find(star.statLine)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val candidateOptions: List<String> = when {
                isGoalie -> {
                    when {
                        star.statLine.contains(".000") || star.statLine.contains("SO") -> listOf(
                            "Turned aside every shot faced in a flawless, brick-wall shutout.",
                            "Flawless night in the crease, securing a clean sheet between the pipes."
                        )
                        saves >= 30 -> listOf(
                            "Weathered intense pressure with $saves clutch stops to secure the win.",
                            "Anchored the defense under heavy siege, turning aside $saves dangerous drives."
                        )
                        else -> listOf(
                            "Poised and steady in the blue paint, tracking pucks through heavy traffic.",
                            "Commanded the crease with sharp rebound control and timely key saves."
                        )
                    }
                }
                goals >= 3 -> listOf(
                    "Exploded for a hat trick performance, completely tilting the ice in the offensive zone.",
                    "Dominated the game with a three-goal hat trick to pace the victory."
                )
                goals == 2 -> listOf(
                    "Buried a pair of clutch goals from high-danger areas to ignite the offense.",
                    "Clinical multi-goal performance, finishing chances with elite shooting touch."
                )
                goals == 1 && assists >= 1 -> listOf(
                    "Multi-point powerhouse night, driving offensive possession all game.",
                    "Capped a two-way effort with a clutch goal and high-IQ playmaking."
                )
                goals == 1 -> {
                    when (star.star) {
                        1 -> listOf(
                            "Capped a dominant 200-foot performance with the game-breaking finish.",
                            "Delivered the decisive breakthrough marker to lead the victory."
                        )
                        2 -> listOf(
                            "Capitalized on an odd-man rush with a laser finish under the bar.",
                            "Sniped a crucial marker while generating relentless forechecking pressure."
                        )
                        else -> listOf(
                            "Generated high-danger pressure and cashed in on a crucial rebound.",
                            "Broke through with a timely goal to swing momentum."
                        )
                    }
                }
                assists >= 2 -> listOf(
                    "Orchestrated the transition game with precision tape-to-tape primary helpers.",
                    "Masterful playmaker vision, dishing out multiple prime scoring setups."
                )
                assists == 1 -> listOf(
                    "Threaded a crisp helper through traffic to unlock the scoring.",
                    "Created the transition breakout with a sharp tape-to-tape dish."
                )
                isDefense -> listOf(
                    "Stifled top-line rushes with physical gap control and clean breakout delivery.",
                    "Logged heavy shutdown minutes, breaking up cycles and protecting the house."
                )
                else -> listOf(
                    "Delivered an impactful 200-foot performance to anchor the game plan.",
                    "Relentless motor on the forecheck, winning battles and sustaining offensive zone time."
                )
            }

            val chosen = candidateOptions.firstOrNull { it !in usedPhrases } ?: candidateOptions.first()
            usedPhrases.add(chosen)
            map[star.star] = chosen
        }
        return map
    }

    private fun savePreGamePrefs(oppAbbrev: String, storyline: String, battle: String) {
        prefs.edit()
            .putString("ai_matchup_opponent_abbrev", oppAbbrev)
            .putString("ai_matchup_storyline", storyline)
            .putString("ai_key_battle", battle)
            .apply()
    }

    private fun savePostGamePrefs(pill: String, commentaries: Map<Int, String>) {
        val editor = prefs.edit()
            .putString("ai_recap_pill", pill)
        commentaries.forEach { (star, text) ->
            editor.putString("ai_star_${star}_commentary", text)
        }
        editor.apply()
    }

    private fun savePacingPrefs(pacing: Map<Int, String>) {
        val editor = prefs.edit()
            .putInt("ai_pacing_count", pacing.size)
        var i = 0
        pacing.forEach { (pid, text) ->
            editor.putInt("ai_pacing_id_$i", pid)
            editor.putString("ai_pacing_text_$i", text)
            i++
        }
        editor.apply()
    }
}
