package app.jeegem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.jeegem.data.Chapter
import app.jeegem.data.ChapterStatus
import app.jeegem.data.UnitPack
import app.jeegem.ui.SubjectSummary
import app.jeegem.ui.UiState
import app.jeegem.ui.components.PriorityBadge
import app.jeegem.ui.components.StatusChip
import app.jeegem.ui.components.SubjectDot
import app.jeegem.ui.theme.subjectColor

private enum class SyllabusFilter(val label: String) {
    ALL("All"),
    NOT_STARTED("To start"),
    IN_PROGRESS("In progress"),
    DUE("Due"),
    HIGH("High yield"),
}

@Composable
fun SyllabusScreen(
    state: UiState,
    onCycleStatus: (Chapter) -> Unit,
) {
    var filterName by rememberSaveable { mutableStateOf(SyllabusFilter.ALL.name) }
    val filter = SyllabusFilter.valueOf(filterName)
    var expandedUnits by remember { mutableStateOf(setOf<String>()) }

    val byUnit = remember(state.chapters) { state.chapters.groupBy { it.unitId } }
    val dueIds = remember(state.reviewsDue) { state.reviewsDue.map { it.id }.toSet() }

    fun statusOf(chapter: Chapter): ChapterStatus =
        state.progress[chapter.id]?.status ?: ChapterStatus.NOT_STARTED

    fun isVisible(chapter: Chapter): Boolean = when (filter) {
        SyllabusFilter.ALL -> true
        SyllabusFilter.NOT_STARTED -> statusOf(chapter) == ChapterStatus.NOT_STARTED
        SyllabusFilter.IN_PROGRESS -> statusOf(chapter) == ChapterStatus.LEARNING ||
            statusOf(chapter) == ChapterStatus.REVISED
        SyllabusFilter.DUE -> chapter.id in dueIds
        SyllabusFilter.HIGH -> chapter.priority == "high"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "Syllabus",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "54 official units · ${state.totalTopics} topics · ${state.examName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SyllabusFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filterName = option.name },
                    label = { Text(option.label) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        ) {
            state.subjects.forEach { subject ->
                item(key = "subject-${subject.id}") {
                    SubjectHeader(subject)
                    Spacer(Modifier.height(6.dp))
                }

                subject.pack.units.forEach { unit ->
                    val topics = byUnit[unit.id].orEmpty()
                    val visible = topics.filter { isVisible(it) }
                    if (visible.isEmpty()) return@forEach

                    val expanded = unit.id in expandedUnits
                    item(key = "unit-${unit.id}") {
                        UnitHeader(
                            unit = unit,
                            startedCount = topics.count {
                                (state.progress[it.id]?.status ?: ChapterStatus.NOT_STARTED) !=
                                    ChapterStatus.NOT_STARTED
                            },
                            total = topics.size,
                            expanded = expanded,
                            tint = subjectColor(subject.tint),
                            onToggle = {
                                expandedUnits = if (expanded) {
                                    expandedUnits - unit.id
                                } else {
                                    expandedUnits + unit.id
                                }
                            },
                        )
                    }

                    if (expanded) {
                        items(visible, key = { it.id }) { chapter ->
                            TopicRow(
                                chapter = chapter,
                                status = statusOf(chapter),
                                onClick = { onCycleStatus(chapter) },
                            )
                        }
                    }
                }
                item(key = "gap-${subject.id}") {
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun SubjectHeader(subject: SubjectSummary) {
    val tint = subjectColor(subject.tint)
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectDot(subject.tint)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "${subject.startedTopics}/${subject.totalTopics} started · ${subject.masteredTopics} mastered",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { subject.coverage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = tint,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun UnitHeader(
    unit: UnitPack,
    startedCount: Int,
    total: Int,
    expanded: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (unit.priority == "high") {
                    Spacer(Modifier.width(8.dp))
                    PriorityBadge(unit.priority)
                }
            }
            Text(
                text = "$startedCount of $total topics started",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (startedCount == total && total > 0) {
            Text(
                text = "done",
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse unit" else "Expand unit",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

@Composable
private fun TopicRow(
    chapter: Chapter,
    status: ChapterStatus,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = chapter.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )
        StatusChip(
            status = status,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = 8.dp),
        )
    }
}
