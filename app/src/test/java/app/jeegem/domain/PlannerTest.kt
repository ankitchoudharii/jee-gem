package app.jeegem.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlannerTest {

    private val day = LocalDate.of(2026, 11, 23)

    private val chapters = listOf(
        Chapter("p1", "physics", "mech", "Kinematics"),
        Chapter("p2", "physics", "mech", "Laws of Motion"),
        Chapter("c1", "chemistry", "physical", "Mole Concept"),
        Chapter("m1", "maths", "algebra", "Quadratic Equations"),
        Chapter("m2", "maths", "algebra", "Complex Numbers"),
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
        assertTrue(plan.none { it.chapterId == "c1" && it.reason != TargetReason.REVIEW })
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
}
