package app.jeegem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.jeegem.data.ExamCalendar
import app.jeegem.data.Settings
import app.jeegem.data.ThemeChoice

@Composable
fun SettingsDialog(
    settings: Settings,
    onSave: (Settings) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
) {
    var minutes by remember { mutableIntStateOf(settings.dailyMinutes) }
    var year by remember { mutableIntStateOf(settings.targetYear) }
    var theme by remember { mutableStateOf(settings.theme) }
    var confirmReset by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ThemeChoice.DARK to "Dark",
                        ThemeChoice.AMOLED to "AMOLED",
                        ThemeChoice.LIGHT to "Light",
                        ThemeChoice.SYSTEM to "System",
                    ).forEach { (choice, label) ->
                        FilterChip(
                            selected = theme == choice,
                            onClick = {
                                theme = choice
                                onSave(settings.copy(theme = choice, dailyMinutes = minutes, targetYear = year))
                            },
                            label = { Text(label) },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Daily target",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${minutes / 60}h ${minutes % 60}m",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = {
                        minutes = (it / 30).toInt() * 30
                        onSave(settings.copy(dailyMinutes = minutes, theme = theme, targetYear = year))
                    },
                    valueRange = 60f..600f,
                    steps = 17,
                )

                Text(
                    text = "Target attempt",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2027, 2028, 2029).forEach { option ->
                        FilterChip(
                            selected = year == option,
                            onClick = {
                                year = option
                                onSave(settings.copy(targetYear = option, dailyMinutes = minutes, theme = theme))
                            },
                            label = { Text("$option") },
                        )
                    }
                }
                Text(
                    text = if (ExamCalendar.hasKnownDates(year)) {
                        "Official 2027 dates are loaded: Main S1 20 Jan, Main S2 2 Apr, Advanced 23 May."
                    } else {
                        "No official dates for $year yet — they'll be added once NTA announces them."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )

                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Erase all study data", color = MaterialTheme.colorScheme.error)
                }
                Text(
                    text = "Deletes chapter statuses and sessions on this device. There is no undo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )

                TextButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Text("Close")
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Erase everything?") },
            text = { Text("All chapter statuses and logged study time will be deleted from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                        onClose()
                    },
                ) {
                    Text("Erase", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}
