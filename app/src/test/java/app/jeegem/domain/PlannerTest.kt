package app.jeegem.domain

import app.jeegem.data.Chapter
import app.jeegem.data.ChapterStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlannerTest {

    private val day = LocalDate.of(2026, 11, 23)

    private fun chapter(id: String, subjectId: String, priority: String = "high") =
        Chapter(
            id = id,
            subjectId = subjectId,
            subjectName = subjectId,
            subjectTint = "#4C8DFF",
            unitId = "$subjectId-unit",
            unitName = "Unit",
            name = "Chapter $id",
            priority = priority,
        )

    private val chapters = listOf(
        chapter("p1", "physics"),
        chapter("p2", "physics"),
        chapter("c1", "chemistry"),
        chapter("m1", "maths"),
        chapter("m2", "maths"),
    )

    @Test
    fun `revision ladder is three then seven then twenty-one days`() {
        assertEquals(day.plusDays(3), ReviewSchedule.next("p1", day, 0)?.dueDate)
        assertEquals(day.plusDays(7), ReviewSchedule.next("p1", day, 1)?.dueDate)
        assertEquals(day.plusDays(21), ReviewSchedule.next("p1", day, 2)?.dueDate)
        assertNull(ReviewSchedule.next("p1", day, 3))
    }

    @Test
    fun `reviews due today or earlier are planned first`() {
        val progress = mapOf("p1" to ChapterProgress("p1", ChapterStatus.LEARNING))
        val reviews = listOf(
            ReviewItem("c1", day.minusDays(2), 0),
            ReviewItem("m1", day.plusDays(4), 0),
        )

        val plan = Planner.plan(day, chapters, progress, reviews, minutesBudget = 600)

        assertEquals(TargetReason.REVIEW, plan.first().reason)
        assertEquals("c1", plan.first().chapterId)
        assertTrue(plan.none { it.chapterId == "m1" && it.reason == TargetReason.REVIEW })
    }

    @Test
    fun `weakest subject is offered new chapters first`() {
        val progress = mapOf(
            "m1" to ChapterProgress("m1", ChapterStatus.MASTERED),
            "m2" to ChapterProgress("m2", ChapterStatus.MASTERED),
            "p1" to ChapterProgress("p1", ChapterStatus.MASTERED),
        )

        val plan = Planner.plan(day, chapters, progress, emptyList(), minutesBudget = 600)
        val firstNew = plan.first { it.reason == TargetReason.NEW }

        assertEquals("c1", firstNew.chapterId)
    }

    @Test
    fun `high priority chapters come before low priority in the same subject`() {
        val subjectChapters = listOf(
            chapter("maths-low", "maths", "low"),
            chapter("maths-high", "maths", "high"),
        )

        val plan = Planner.plan(day, subjectChapters, emptyMap(), emptyList(), minutesBudget = 600)

        assertEquals("maths-high", plan.first().chapterId)
    }

    @Test
    fun `plan never exceeds the minute budget`() {
        val progress = mapOf(
            "p1" to ChapterProgress("p1", ChapterStatus.LEARNING),
            "p2" to ChapterProgress("p2", ChapterStatus.LEARNING),
        )
        val reviews = listOf(ReviewItem("c1", day, 0))

        val plan = Planner.plan(day, chapters, progress, reviews, minutesBudget = 60)

        assertTrue(plan.sumOf { it.minutes } <= 60)
        assertEquals(2, plan.size)
    }

    @Test
    fun `zero budget yields no targets`() {
        assertTrue(Planner.plan(day, chapters, emptyMap(), emptyList(), minutesBudget = 0).isEmpty())
    }

    @Test
    fun `no chapter is planned twice`() {
        val progress = mapOf("p1" to ChapterProgress("p1", ChapterStatus.LEARNING))
        val reviews = listOf(ReviewItem("p1", day, 0))

        val plan = Planner.plan(day, chapters, progress, reviews, minutesBudget = 600)

        assertEquals(plan.size, plan.map { it.chapterId }.toSet().size)
    }

    @Test
    fun `upcoming reviews follow the stored stage and skip mastered chapters`() {
        val progress = mapOf(
            "p1" to ChapterProgress("p1", ChapterStatus.LEARNING, day, 0),
            "p2" to ChapterProgress("p2", ChapterStatus.MASTERED, day, 3),
        )

        val reviews = Planner.upcomingReviews(chapters, progress)

        assertEquals(listOf("p1"), reviews.map { it.chapterId })
        assertEquals(day.plusDays(3), reviews.single().dueDate)
    }
}
