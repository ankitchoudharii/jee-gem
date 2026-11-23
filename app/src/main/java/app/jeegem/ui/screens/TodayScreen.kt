package app.jeegem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.jeegem.domain.TargetReason
import app.jeegem.ui.PlanTarget
import app.jeegem.ui.UiState
import app.jeegem.ui.components.EmptyHint
import app.jeegem.ui.components.PriorityBadge
import app.jeegem.ui.components.ProgressRing
import app.jeegem.ui.components.SectionHeader
import app.jeegem.ui.components.SubjectDot
import app.jeegem.ui.components.VSpace
import app.jeegem.ui.components.statusLabel
import app.jeegem.ui.theme.subjectColor
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)

@Composable
fun TodayScreen(
    state: UiState,
    onStart: (PlanTarget) -> Unit,
    onComplete: (PlanTarget) -> Unit,
) {
    val doneCount = state.targets.count { it.chapter.id in state.doneToday }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.today.format(dateFormat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.nextMilestone?.let { milestone ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "${state.daysToNext}d",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = milestone.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }

        VSpace(18)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressRing(
                    progress = if (state.settings.dailyMinutes == 0) 0f else {
                        state.todayMinutes.toFloat() / state.settings.dailyMinutes
                    },
                    label = "${state.todayMinutes}m",
                    caption = "of ${state.settings.dailyMinutes / 60}h",
                )
                Spacer(Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (state.streak > 0) "${state.streak}-day streak" else "Start your streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${state.weekMinutes / 60}h ${state.weekMinutes % 60}m this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${state.startedTopics} of ${state.totalTopics} topics touched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        VSpace(24)
        SectionHeader(title = "Today's plan", trailing = "$doneCount of ${state.targets.size} done")
        VSpace(10)

        if (state.targets.isEmpty()) {
            EmptyHint(
                title = "Nothing planned yet",
                body = "Add a couple of hours a day in Settings and JEE Gem will build a plan from your weakest subject.",
            )
        } else {
            state.targets.forEach { target ->
                TargetCard(
                    target = target,
                    done = target.chapter.id in state.doneToday,
                    onStart = { onStart(target) },
                    onComplete = { onComplete(target) },
                )
                VSpace(10)
            }
        }

        if (state.reviewsDue.isNotEmpty()) {
            VSpace(18)
            SectionHeader(title = "Revision due", trailing = "${state.reviewsDue.size} chapters")
            VSpace(8)
            state.reviewsDue.take(3).forEach { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SubjectDot(chapter.subjectTint)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${chapter.subjectName} · ${chapter.unitName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = statusLabel(app.jeegem.data.ChapterStatus.LEARNING),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetCard(
    target: PlanTarget,
    done: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit,
) {
    val accent = subjectColor(target.chapter.subjectTint)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (done) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.chapter.unitName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = target.chapter.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${target.minutes} min · ${reasonLabel(target.reason)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (target.chapter.priority == "high") {
                        Spacer(Modifier.width(8.dp))
                        PriorityBadge(target.chapter.priority)
                    }
                }
            }
            Checkbox(checked = done, onCheckedChange = { checked -> if (checked) onComplete() })
            TextButton(onClick = onStart, enabled = !done) {
                Text("Focus")
            }
        }
    }
}

private fun reasonLabel(reason: TargetReason): String = when (reason) {
    TargetReason.REVIEW -> "revision"
    TargetReason.CONTINUE -> "continue"
    TargetReason.NEW -> "new chapter"
}
