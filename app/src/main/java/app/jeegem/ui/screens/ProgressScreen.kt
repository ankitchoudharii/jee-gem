package app.jeegem.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.jeegem.data.Milestone
import app.jeegem.ui.SubjectSummary
import app.jeegem.ui.UiState
import app.jeegem.ui.components.ActivityHeatmap
import app.jeegem.ui.components.EmptyHint
import app.jeegem.ui.components.SectionHeader
import app.jeegem.ui.components.StatTile
import app.jeegem.ui.components.VSpace
import app.jeegem.ui.theme.subjectColor
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val milestoneFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@Composable
fun ProgressScreen(
    state: UiState,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${state.startedTopics} of ${state.totalTopics} topics touched · ${state.masteredTopics} mastered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        VSpace(18)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Last 12 weeks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${state.todayMinutes}m today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                VSpace(12)
                ActivityHeatmap(activity = state.activity)
            }
        }

        VSpace(12)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                value = "${state.streak}d",
                label = "Current streak",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "${state.weekMinutes / 60}h",
                label = "This week",
                modifier = Modifier.weight(1f),
            )
        }
        VSpace(10)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                value = "${(state.coverage * 100).toInt()}%",
                label = "Syllabus touched",
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.secondary,
            )
            StatTile(
                value = "${state.reviewsDue.size}",
                label = "Revisions due",
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.tertiary,
            )
        }

        VSpace(24)
        SectionHeader(title = "Subject coverage")
        VSpace(10)
        state.subjects.forEach { subject ->
            SubjectProgressCard(subject)
            VSpace(10)
        }

        VSpace(14)
        SectionHeader(title = "Exam timeline")
        VSpace(10)
        if (state.milestones.isEmpty()) {
            EmptyHint(
                title = "No dates yet",
                body = "Official dates for ${state.settings.targetYear} aren't out. Add them in Settings when NTA publishes the bulletin.",
            )
        } else {
            state.milestones.forEach { milestone ->
                MilestoneRow(milestone, state)
                VSpace(8)
            }
        }

        VSpace(18)
        Text(
            text = "Syllabus ${state.examName} · verified ${state.verifiedOn}. Priority badges are approximate, based on past-paper trends — not NTA data.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpace(24)
    }
}

@Composable
private fun SubjectProgressCard(subject: SubjectSummary) {
    val tint = subjectColor(subject.tint)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${subject.startedTopics}/${subject.totalTopics}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { subject.coverage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = tint,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { subject.mastery },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = tint.copy(alpha = 0.45f),
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${(subject.coverage * 100).toInt()}% started · ${subject.masteredTopics} mastered",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MilestoneRow(milestone: Milestone, state: UiState) {
    val days = ChronoUnit.DAYS.between(state.today, milestone.date)
    val isNext = state.nextMilestone?.key == milestone.key
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = milestone.date.format(milestoneFormat),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNext) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = when {
                    days < 0 -> "passed"
                    days == 0L -> "today"
                    else -> "$days days"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
            )
        }
    }
}
