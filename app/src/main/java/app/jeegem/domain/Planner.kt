package app.jeegem.domain

import app.jeegem.data.Chapter
import app.jeegem.data.ChapterStatus
import java.time.LocalDate

data class ChapterProgress(
    val chapterId: String,
    val status: ChapterStatus = ChapterStatus.NOT_STARTED,
    val lastStudied: LocalDate? = null,
    val revisionStage: Int = 0,
)

data class ReviewItem(
    val chapterId: String,
    val dueDate: LocalDate,
    val stage: Int,
)

enum class TargetReason { REVIEW, CONTINUE, NEW }

data class DailyTarget(
    val chapterId: String,
    val reason: TargetReason,
    val minutes: Int,
)

object ReviewSchedule {
    val gapsDays = listOf(3L, 7L, 21L)

    fun next(chapterId: String, studiedOn: LocalDate, stage: Int): ReviewItem? {
        val gap = gapsDays.getOrNull(stage) ?: return null
        return ReviewItem(chapterId, studiedOn.plusDays(gap), stage)
    }
}

object Planner {
    private const val REVIEW_MINUTES = 20
    private const val CONTINUE_MINUTES = 30
    private const val NEW_MINUTES = 35
    private const val MAX_TARGETS = 5

    fun plan(
        day: LocalDate,
        chapters: List<Chapter>,
        progress: Map<String, ChapterProgress>,
        reviews: List<ReviewItem>,
        minutesBudget: Int,
    ): List<DailyTarget> {
        if (minutesBudget <= 0) return emptyList()

        val targets = mutableListOf<DailyTarget>()
        val known = chapters.map { it.id }.toSet()
        val taken = mutableSetOf<String>()
        var remaining = minutesBudget

        fun add(chapterId: String, reason: TargetReason, minutes: Int) {
            if (targets.size >= MAX_TARGETS) return
            if (chapterId in taken || chapterId !in known) return
            if (minutes > remaining) return
            targets += DailyTarget(chapterId, reason, minutes)
            taken += chapterId
            remaining -= minutes
        }

        reviews.asSequence()
            .filter { !it.dueDate.isAfter(day) }
            .sortedWith(compareBy({ it.dueDate }, { it.chapterId }))
            .forEach { add(it.chapterId, TargetReason.REVIEW, REVIEW_MINUTES) }

        chapters.asSequence()
            .filter { progress[it.id]?.status == ChapterStatus.LEARNING }
            .sortedWith(compareBy({ priorityRank(it.priority) }, { it.name }))
            .forEach { add(it.id, TargetReason.CONTINUE, CONTINUE_MINUTES) }

        val weakestFirst = subjectOrder(chapters, progress)
        chapters.asSequence()
            .filter { statusOf(it, progress) == ChapterStatus.NOT_STARTED }
            .sortedWith(
                compareBy(
                    { weakestFirst.indexOf(it.subjectId) },
                    { priorityRank(it.priority) },
                    { it.name },
                ),
            )
            .forEach { add(it.id, TargetReason.NEW, NEW_MINUTES) }

        return targets
    }

    /** Reviews that are scheduled for every started chapter, based on its stored ladder stage. */
    fun upcomingReviews(
        chapters: List<Chapter>,
        progress: Map<String, ChapterProgress>,
    ): List<ReviewItem> = chapters.mapNotNull { chapter ->
        val state = progress[chapter.id] ?: return@mapNotNull null
        val studied = state.lastStudied ?: return@mapNotNull null
        if (state.status == ChapterStatus.MASTERED) return@mapNotNull null
        val gap = ReviewSchedule.gapsDays.getOrNull(state.revisionStage) ?: return@mapNotNull null
        ReviewItem(chapter.id, studied.plusDays(gap), state.revisionStage)
    }

    fun statusOf(chapter: Chapter, progress: Map<String, ChapterProgress>): ChapterStatus =
        progress[chapter.id]?.status ?: ChapterStatus.NOT_STARTED

    /** Subjects ordered weakest coverage first; ties broken by subject id for determinism. */
    fun subjectOrder(
        chapters: List<Chapter>,
        progress: Map<String, ChapterProgress>,
    ): List<String> {
        val coverage = subjectCoverage(chapters, progress)
        return coverage.entries
            .sortedWith(compareBy({ it.value }, { it.key }))
            .map { it.key }
    }

    fun subjectCoverage(
        chapters: List<Chapter>,
        progress: Map<String, ChapterProgress>,
    ): Map<String, Float> {
        val totals = chapters.groupingBy { it.subjectId }.eachCount()
        val started = chapters
            .filter { statusOf(it, progress) != ChapterStatus.NOT_STARTED }
            .groupingBy { it.subjectId }
            .eachCount()
        return totals.mapValues { (subjectId, total) ->
            (started[subjectId] ?: 0).toFloat() / total.toFloat()
        }
    }

    fun subjectMastery(
        chapters: List<Chapter>,
        progress: Map<String, ChapterProgress>,
    ): Map<String, Float> {
        val totals = chapters.groupingBy { it.subjectId }.eachCount()
        val mastered = chapters
            .filter { statusOf(it, progress) == ChapterStatus.MASTERED }
            .groupingBy { it.subjectId }
            .eachCount()
        return totals.mapValues { (subjectId, total) ->
            (mastered[subjectId] ?: 0).toFloat() / total.toFloat()
        }
    }

    private fun priorityRank(priority: String): Int = when (priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }
}
