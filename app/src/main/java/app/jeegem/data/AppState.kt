package app.jeegem.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class AppState(
    val settings: Settings = Settings(),
    val chapters: Map<String, ChapterState> = emptyMap(),
    val sessions: List<StudySession> = emptyList(),
)

@Serializable
data class Settings(
    val onboarded: Boolean = false,
    val targetYear: Int = 2027,
    val dailyMinutes: Int = 240,
    val theme: ThemeChoice = ThemeChoice.DARK,
    val customDates: Map<String, Long> = emptyMap(),
)

@Serializable
enum class ThemeChoice { DARK, AMOLED, LIGHT, SYSTEM }

@Serializable
enum class ChapterStatus { NOT_STARTED, LEARNING, REVISED, MASTERED }

@Serializable
data class ChapterState(
    val status: ChapterStatus = ChapterStatus.NOT_STARTED,
    val lastStudiedEpochDay: Long? = null,
    val revisionStage: Int = 0,
    val updatedAt: Long = 0L,
)

@Serializable
data class StudySession(
    val epochDay: Long,
    val minutes: Int,
    val subjectId: String,
    val chapterId: String? = null,
    val completedAtMillis: Long = 0L,
)

data class Milestone(val key: String, val label: String, val date: LocalDate)

object ExamCalendar {
    const val KEY_MAIN_1 = "main1"
    const val KEY_MAIN_2 = "main2"
    const val KEY_ADVANCED = "advanced"

    /** Verified against NTA and jeeadv.ac.in notices on 23 Nov 2026. */
    private val known: Map<Int, Map<String, LocalDate>> = mapOf(
        2027 to mapOf(
            KEY_MAIN_1 to LocalDate.of(2027, 1, 20),
            KEY_MAIN_2 to LocalDate.of(2027, 4, 2),
            KEY_ADVANCED to LocalDate.of(2027, 5, 23),
        ),
    )

    fun hasKnownDates(year: Int): Boolean = known.containsKey(year)

    fun milestones(year: Int, custom: Map<String, Long>): List<Milestone> {
        val defaults = known[year].orEmpty()
        return listOf(KEY_MAIN_1, KEY_MAIN_2, KEY_ADVANCED)
            .mapNotNull { key ->
                val date = custom[key]?.let { LocalDate.ofEpochDay(it) }
                    ?: defaults[key]
                    ?: return@mapNotNull null
                Milestone(key, labelFor(key), date)
            }
            .sortedBy { it.date }
    }

    fun labelFor(key: String): String = when (key) {
        KEY_MAIN_1 -> "JEE Main S1"
        KEY_MAIN_2 -> "JEE Main S2"
        KEY_ADVANCED -> "JEE Advanced"
        else -> key
    }
}
