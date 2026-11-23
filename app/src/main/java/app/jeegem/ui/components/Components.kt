package app.jeegem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.jeegem.data.ChapterStatus
import app.jeegem.ui.DayActivity
import app.jeegem.ui.theme.GemBlue
import app.jeegem.ui.theme.GemGold
import app.jeegem.ui.theme.GemMint

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun statusLabel(status: ChapterStatus): String = when (status) {
    ChapterStatus.NOT_STARTED -> "Not started"
    ChapterStatus.LEARNING -> "Learning"
    ChapterStatus.REVISED -> "Revised"
    ChapterStatus.MASTERED -> "Mastered"
}

fun statusColor(status: ChapterStatus): Color = when (status) {
    ChapterStatus.NOT_STARTED -> Color(0xFF7C8BA1)
    ChapterStatus.LEARNING -> GemBlue
    ChapterStatus.REVISED -> GemGold
    ChapterStatus.MASTERED -> GemMint
}

@Composable
fun StatusChip(status: ChapterStatus, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f),
    ) {
        Text(
            text = statusLabel(status),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (label, color) = when (priority) {
        "high" -> "HIGH" to GemGold
        "medium" -> "MED" to GemBlue
        else -> "LOW" to Color(0xFF7C8BA1)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun SubjectDot(hex: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(RoundedCornerShape(50))
            .background(app.jeegem.ui.theme.subjectColor(hex)),
    )
}

@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    caption: String,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Int = 104,
    stroke: Float = 10f,
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val strokeWidth = stroke
            val inset = strokeWidth / 2f
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    this.size.width - strokeWidth,
                    this.size.height - strokeWidth,
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(
                    this.size.width - strokeWidth,
                    this.size.height - strokeWidth,
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ActivityHeatmap(activity: List<DayActivity>, modifier: Modifier = Modifier) {
    val max = activity.maxOfOrNull { it.minutes }?.coerceAtLeast(1) ?: 1
    val weeks = activity.chunked(7)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { day ->
                    val intensity = (day.minutes.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                    val color = when {
                        day.minutes == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f + 0.75f * intensity)
                    }
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun EmptyHint(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DividerDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(3.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
    )
}

@Composable
fun WideDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    )
}

@Composable
fun HSpace(width: Int) {
    Spacer(Modifier.width(width.dp))
}

@Composable
fun VSpace(height: Int) {
    Spacer(Modifier.height(height.dp))
}
