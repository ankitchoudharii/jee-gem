package app.jeegem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jeegem.ui.TimerState
import app.jeegem.ui.components.ProgressRing
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
    state: TimerState,
    onToggle: () -> Unit,
    onFinish: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val elapsedFraction = if (state.totalSeconds == 0) 0f else {
        1f - state.remainingSeconds.toFloat() / state.totalSeconds
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (state.completed) "Session complete" else "Focus session",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )

            ProgressRing(
                progress = elapsedFraction,
                label = formatClock(state.remainingSeconds),
                caption = if (state.completed) "saved" else "left",
                size = 168,
                stroke = 14f,
            )

            Text(
                text = "${state.totalSeconds / 60} minute target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!state.completed) {
                    FilledTonalButton(
                        onClick = onToggle,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(if (state.running) "Pause" else "Resume")
                    }
                }
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(if (state.completed) "Done" else "Save & close")
                }
            }

            if (!state.completed) {
                TextButton(onClick = onDismiss) {
                    Text("Discard")
                }
            } else {
                Text(
                    text = "Logged as study time on this topic.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private fun formatClock(seconds: Int): String =
    String.format(Locale.ENGLISH, "%02d:%02d", seconds / 60, seconds % 60)
