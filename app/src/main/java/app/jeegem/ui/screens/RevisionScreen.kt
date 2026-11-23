package app.jeegem.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.jeegem.data.Chapter
import app.jeegem.ui.UiState
import app.jeegem.ui.components.EmptyHint
import app.jeegem.ui.components.SectionHeader
import app.jeegem.ui.components.SubjectDot
import app.jeegem.ui.components.VSpace
import java.time.temporal.ChronoUnit

@Composable
fun RevisionScreen(
    state: UiState,
    onRevised: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Revision",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Spaced review at 3, 7 and 21 days after you study a topic.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        VSpace(22)
        SectionHeader(title = "Due now", trailing = "${state.reviewsDue.size} topics")

        VSpace(8)
        if (state.reviewsDue.isEmpty()) {
            EmptyHint(
                title = "Nothing due",
                body = "Study a topic from Today and its first revision will appear here in 3 days.",
            )
        } else {
            state.reviewsDue.forEach { chapter ->
                RevisionCard(chapter = chapter, dueText = "Due now", onRevised = onRevised)
                VSpace(8)
            }
        }

        if (state.reviewsUpcoming.isNotEmpty()) {
            VSpace(18)
            SectionHeader(title = "Next 7 days", trailing = "${state.reviewsUpcoming.size} topics")
            VSpace(8)
            state.reviewsUpcoming.take(12).forEach { upcoming ->
                val days = ChronoUnit.DAYS.between(state.today, upcoming.review.dueDate).coerceAtLeast(0)
                RevisionCard(
                    chapter = upcoming.chapter,
                    dueText = "in ${days}d",
                    onRevised = onRevised,
                )
                VSpace(8)
            }
        }
    }
}

@Composable
private fun RevisionCard(
    chapter: Chapter,
    dueText: String,
    onRevised: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubjectDot(chapter.subjectTint)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${chapter.subjectName} · ${chapter.unitName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(onClick = { onRevised(chapter.id) }) {
                Text("Revised")
            }
        }
    }
}
