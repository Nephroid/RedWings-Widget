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
        // Step 1: Immediate local heuristic preview
        val heuristicStoryline = generateLocalMatchupStoryline(opponentName, opponentAbbrev, isHome, detRecord, oppRecord)
        val heuristicBattle = generateLocalKeyBattle(opponentAbbrev)

        if (_intelligenceState.value.matchupStoryline.isBlank()) {
            val updated = _intelligenceState.value.copy(
                matchupStoryline = heuristicStoryline,
                keyBattle = heuristicBattle
            )
            _intelligenceState.value = updated
            savePreGamePrefs(heuristicStoryline, heuristicBattle)
        }

        // Step 2: Background Gemini AI generation
        scope.launch {
            try {
                val modelName = "gemini-2.5-flash"
                val model = Firebase.ai.generativeModel(modelName)
                val prompt = """
                    You are a sharp, analytical Detroit Red Wings hockey beat writer.
                    Write a punchy 1-sentence pre-game storyline and identify 1 key player battle for:
                    Opponent: $opponentName ($opponentAbbrev)
                    Location: ${if (isHome) "Home at Little Caesars Arena" else "Away at $opponentName"}
                    Detroit record: $detRecord, Opponent record: $oppRecord.
                    
                    Format your response EXACTLY as:
                    STORYLINE: <1 sentence storyline>
                    BATTLE: <Key Battle: Player vs Player>
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
                                aiBattle = line.substringAfter(":").trim()
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _intelligenceState.value = _intelligenceState.value.copy(
                            matchupStoryline = aiStoryline,
                            keyBattle = aiBattle
                        )
                    }
                    savePreGamePrefs(aiStoryline, aiBattle)
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

    private fun generateLocalMatchupStoryline(name: String, abbr: String, isHome: Boolean, detRecord: String, oppRecord: String): String {
        val venue = if (isHome) "at Little Caesars Arena" else "on the road"
        return when (abbr) {
            "BOS", "TOR", "TBL", "FLA", "MTL", "OTT", "BUF" ->
                "Critical Atlantic Division battle $venue as Detroit ($detRecord) looks to secure crucial playoff positioning against $name ($oppRecord)."
            else ->
                "The Red Wings take the ice $venue, eyeing a statement win and two big points against $name."
        }
    }

    private fun generateLocalKeyBattle(oppAbbr: String): String {
        return when (oppAbbr) {
            "TOR" -> "Key Battle: Larkin vs. Matthews"
            "BOS" -> "Key Battle: Seider vs. Pastrnak"
            "TBL" -> "Key Battle: Raymond vs. Kucherov"
            "EDM" -> "Key Battle: Seider vs. McDavid"
            "COL" -> "Key Battle: Larkin vs. MacKinnon"
            else -> "Key Battle: Top line execution & special teams supremacy"
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
        stars.forEach { star ->
            val comment = when {
                star.position.equals("G", ignoreCase = true) || star.statLine.contains("SV") ->
                    "Stood tall in the crease, controlling rebounds and turning aside dangerous chances."
                star.statLine.contains("G") && !star.statLine.startsWith("0G") ->
                    "Electrifying presence on the ice, finishing crucial chances with elite scoring touch."
                star.statLine.contains("A") && !star.statLine.startsWith("0A") ->
                    "Controlled the pace with high-IQ puck distribution and playmaker vision."
                else ->
                    "Delivered an impactful 200-foot performance to anchor the game plan."
            }
            map[star.star] = comment
        }
        return map
    }

    private fun savePreGamePrefs(storyline: String, battle: String) {
        prefs.edit()
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
