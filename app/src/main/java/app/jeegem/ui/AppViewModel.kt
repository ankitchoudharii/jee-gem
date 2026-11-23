package app.jeegem.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.jeegem.data.AppState
import app.jeegem.data.Chapter
import app.jeegem.data.ChapterState
import app.jeegem.data.ChapterStatus
import app.jeegem.data.ExamCalendar
import app.jeegem.data.GemRepository
import app.jeegem.data.Milestone
import app.jeegem.data.Settings
import app.jeegem.data.StudySession
import app.jeegem.data.SubjectPack
import app.jeegem.data.SyllabusLoader
import app.jeegem.data.UnitPack
import app.jeegem.domain.ChapterProgress
import app.jeegem.domain.Planner
import app.jeegem.domain.ReviewItem
import app.jeegem.domain.TargetReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PlanTarget(
    val chapter: Chapter,
    val reason: TargetReason,
    val minutes: Int,
)

data class SubjectSummary(
    val id: String,
    val name: String,
    val tint: String,
    val units: List<UnitPack>,
    val pack: SubjectPack,
    val totalTopics: Int,
    val startedTopics: Int,
    val masteredTopics: Int,
) {
    val coverage: Float get() = if (totalTopics == 0) 0f else startedTopics.toFloat() / totalTopics
    val mastery: Float get() = if (totalTopics == 0) 0f else masteredTopics.toFloat() / totalTopics
}

data class DayActivity(val date: LocalDate, val minutes: Int)

data class UpcomingReview(val chapter: Chapter, val review: ReviewItem)

data class UiState(
    val ready: Boolean = false,
    val settings: Settings = Settings(),
    val showOnboarding: Boolean = false,
    val examName: String = "",
    val verifiedOn: String = "",
    val subjects: List<SubjectSummary> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val progress: Map<String, ChapterState> = emptyMap(),
    val targets: List<PlanTarget> = emptyList(),
    val reviewsDue: List<Chapter> = emptyList(),
    val reviewsUpcoming: List<UpcomingReview> = emptyList(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val streak: Int = 0,
    val milestones: List<Milestone> = emptyList(),
    val nextMilestone: Milestone? = null,
    val daysToNext: Long = 0,
    val totalTopics: Int = 0,
    val startedTopics: Int = 0,
    val masteredTopics: Int = 0,
    val planMinutes: Int = 0,
    val doneToday: Set<String> = emptySet(),
    val activity: List<DayActivity> = emptyList(),
    val today: LocalDate = LocalDate.now(),
) {
    val coverage: Float get() = if (totalTopics == 0) 0f else startedTopics.toFloat() / totalTopics
}

data class TimerState(
    val active: Boolean = false,
    val completed: Boolean = false,
    val chapterId: String? = null,
    val title: String = "",
    val subjectId: String = "",
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val running: Boolean = false,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GemRepository(app)
    private val pack = SyllabusLoader.load(app)
    private val chapters = SyllabusLoader.flatten(pack)

    private val _timer = MutableStateFlow(TimerState())
    val timer: StateFlow<TimerState> = _timer.asStateFlow()
    private var timerJob: Job? = null

    val uiState: StateFlow<UiState> = repo.state
        .map { state -> buildUi(state) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private fun buildUi(state: AppState): UiState {
        val today = LocalDate.now()
        val settings = state.settings

        val domainProgress: Map<String, ChapterProgress> = state.chapters.mapValues { (id, saved) ->
            ChapterProgress(
                chapterId = id,
                status = saved.status,
                lastStudied = saved.lastStudiedEpochDay?.let { LocalDate.ofEpochDay(it) },
                revisionStage = saved.revisionStage,
            )
        }
        val byId = chapters.associateBy { it.id }

        val reviews = Planner.upcomingReviews(chapters, domainProgress)
        val plan = Planner.plan(
            day = today,
            chapters = chapters,
            progress = domainProgress,
            reviews = reviews,
            minutesBudget = settings.dailyMinutes,
        )

        val dueIds = reviews.filter { !it.dueDate.isAfter(today) }.map { it.chapterId }.toSet()
        val due = chapters.filter { it.id in dueIds }

        val upcoming = reviews
            .filter { it.dueDate.isAfter(today) && it.dueDate.isBefore(today.plusDays(8)) }
            .sortedWith(compareBy({ it.dueDate }, { it.chapterId }))
            .mapNotNull { review -> byId[review.chapterId]?.let { UpcomingReview(it, review) } }

        val todayEpoch = today.toEpochDay()
        val todayMinutes = state.sessions.filter { it.epochDay == todayEpoch }.sumOf { it.minutes }
        val weekStartEpoch = today.minusDays((today.dayOfWeek.value - 1).toLong()).toEpochDay()
        val weekMinutes = state.sessions.filter { it.epochDay >= weekStartEpoch }.sumOf { it.minutes }

        val activeDays = state.sessions.map { it.epochDay }.toSet()
        var streak = 0
        var cursor = if (todayEpoch in activeDays) today else today.minusDays(1)
        while (cursor.toEpochDay() in activeDays) {
            streak += 1
            cursor = cursor.minusDays(1)
        }

        val milestones = ExamCalendar.milestones(settings.targetYear, settings.customDates)
        val next = milestones.firstOrNull { !it.date.isBefore(today) }
        val daysToNext = next?.let { ChronoUnit.DAYS.between(today, it.date) } ?: 0

        val perDay = state.sessions.groupBy { it.epochDay }.mapValues { entry -> entry.value.sumOf { it.minutes } }
        val activity = (83 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            DayActivity(date, perDay[date.toEpochDay()] ?: 0)
        }

        val subjects = pack.subjects.map { subject ->
            val subjectChapters = chapters.filter { it.subjectId == subject.id }
            val started = subjectChapters.count {
                (state.chapters[it.id]?.status ?: ChapterStatus.NOT_STARTED) != ChapterStatus.NOT_STARTED
            }
            val mastered = subjectChapters.count { state.chapters[it.id]?.status == ChapterStatus.MASTERED }
            SubjectSummary(
                id = subject.id,
                name = subject.name,
                tint = subject.tint,
                units = subject.units,
                pack = subject,
                totalTopics = subjectChapters.size,
                startedTopics = started,
                masteredTopics = mastered,
            )
        }

        val startedTopics = subjects.sumOf { it.startedTopics }
        val masteredTopics = subjects.sumOf { it.masteredTopics }

        return UiState(
            ready = true,
            settings = settings,
            showOnboarding = !settings.onboarded,
            examName = pack.exam,
            verifiedOn = pack.verifiedOn,
            subjects = subjects,
            chapters = chapters,
            progress = state.chapters,
            targets = plan.mapNotNull { target ->
                byId[target.chapterId]?.let { PlanTarget(it, target.reason, target.minutes) }
            },
            reviewsDue = due,
            reviewsUpcoming = upcoming,
            todayMinutes = todayMinutes,
            weekMinutes = weekMinutes,
            streak = streak,
            milestones = milestones,
            nextMilestone = next,
            daysToNext = daysToNext,
            totalTopics = chapters.size,
            startedTopics = startedTopics,
            masteredTopics = masteredTopics,
            planMinutes = plan.sumOf { it.minutes },
            doneToday = state.sessions
                .filter { it.epochDay == todayEpoch }
                .mapNotNull { it.chapterId }
                .toSet(),
            activity = activity,
            today = today,
        )
    }

    fun completeOnboarding(year: Int, dailyMinutes: Int) {
        viewModelScope.launch {
            val current = repo.state.first().settings
            repo.saveSettings(current.copy(onboarded = true, targetYear = year, dailyMinutes = dailyMinutes))
        }
    }

    fun updateSettings(settings: Settings) {
        viewModelScope.launch { repo.saveSettings(settings) }
    }

    fun resetAll() {
        timerJob?.cancel()
        _timer.value = TimerState()
        viewModelScope.launch { repo.clearAll() }
    }

    fun setChapterStatus(chapterId: String, status: ChapterStatus) {
        viewModelScope.launch { repo.recordStatus(chapterId, status, LocalDate.now()) }
    }

    fun markRevised(chapterId: String) {
        viewModelScope.launch { repo.recordStatus(chapterId, ChapterStatus.REVISED, LocalDate.now()) }
    }

    fun completeTarget(target: PlanTarget) {
        viewModelScope.launch {
            repo.addSession(
                StudySession(
                    epochDay = LocalDate.now().toEpochDay(),
                    minutes = target.minutes,
                    subjectId = target.chapter.subjectId,
                    chapterId = target.chapter.id,
                    completedAtMillis = System.currentTimeMillis(),
                ),
            )
            val status = uiState.value.progress[target.chapter.id]?.status
            if (status == null || status == ChapterStatus.NOT_STARTED) {
                repo.recordStatus(target.chapter.id, ChapterStatus.LEARNING, LocalDate.now())
            }
        }
    }

    fun startTimer(target: PlanTarget) {
        timerJob?.cancel()
        val total = target.minutes * 60
        _timer.value = TimerState(
            active = true,
            chapterId = target.chapter.id,
            title = target.chapter.name,
            subjectId = target.chapter.subjectId,
            totalSeconds = total,
            remainingSeconds = total,
            running = true,
        )
        runTimer()
    }

    fun toggleTimer() {
        val current = _timer.value
        if (!current.active || current.completed) return
        if (current.running) {
            timerJob?.cancel()
            _timer.value = current.copy(running = false)
        } else {
            _timer.value = current.copy(running = true)
            runTimer()
        }
    }

    fun finishTimer() {
        val current = _timer.value
        timerJob?.cancel()
        if (!current.completed) {
            logTimerSession(current, partial = true)
        }
        _timer.value = TimerState()
    }

    fun dismissTimer() {
        timerJob?.cancel()
        _timer.value = TimerState()
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val current = _timer.value
                if (!current.running || current.completed) break
                val next = current.remainingSeconds - 1
                if (next <= 0) {
                    _timer.value = current.copy(remainingSeconds = 0, running = false, completed = true)
                    logTimerSession(current.copy(remainingSeconds = 0), partial = false)
                    break
                }
                _timer.value = current.copy(remainingSeconds = next)
            }
        }
    }

    private fun logTimerSession(state: TimerState, partial: Boolean) {
        val chapterId = state.chapterId ?: return
        val elapsedSeconds = state.totalSeconds - state.remainingSeconds
        val minutes = (elapsedSeconds / 60).coerceAtLeast(1)
        viewModelScope.launch {
            repo.addSession(
                StudySession(
                    epochDay = LocalDate.now().toEpochDay(),
                    minutes = minutes,
                    subjectId = state.subjectId,
                    chapterId = chapterId,
                    completedAtMillis = System.currentTimeMillis(),
                ),
            )
            val status = uiState.value.progress[chapterId]?.status
            if (status == null || status == ChapterStatus.NOT_STARTED) {
                repo.recordStatus(chapterId, ChapterStatus.LEARNING, LocalDate.now())
            }
            if (partial) _timer.value = TimerState()
        }
    }
}
