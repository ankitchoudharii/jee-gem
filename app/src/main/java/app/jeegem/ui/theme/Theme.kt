package app.jeegem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.jeegem.data.ThemeChoice

val GemMint = Color(0xFF4ADE9F)
val GemBlue = Color(0xFF4C8DFF)
val GemGold = Color(0xFFF0B429)
val GemRose = Color(0xFFFF6B8A)

private val DarkColors = darkColorScheme(
    primary = GemMint,
    onPrimary = Color(0xFF04231A),
    primaryContainer = Color(0xFF0C4A38),
    onPrimaryContainer = Color(0xFFB9F5DC),
    secondary = GemBlue,
    onSecondary = Color(0xFF061626),
    secondaryContainer = Color(0xFF1B3A66),
    onSecondaryContainer = Color(0xFFD5E4FF),
    tertiary = GemGold,
    onTertiary = Color(0xFF2A1C00),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE8EDF5),
    surface = Color(0xFF121A29),
    onSurface = Color(0xFFE8EDF5),
    surfaceVariant = Color(0xFF1B2638),
    onSurfaceVariant = Color(0xFF9FB0C6),
    outline = Color(0xFF2C3B52),
    error = Color(0xFFFF8A9B),
)

private val AmoledColors = darkColorScheme(
    primary = GemMint,
    onPrimary = Color(0xFF04231A),
    primaryContainer = Color(0xFF0C4A38),
    onPrimaryContainer = Color(0xFFB9F5DC),
    secondary = GemBlue,
    onSecondary = Color(0xFF061626),
    secondaryContainer = Color(0xFF1B3A66),
    onSecondaryContainer = Color(0xFFD5E4FF),
    tertiary = GemGold,
    onTertiary = Color(0xFF2A1C00),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE8EDF5),
    surface = Color(0xFF0A0A0C),
    onSurface = Color(0xFFE8EDF5),
    surfaceVariant = Color(0xFF15151A),
    onSurfaceVariant = Color(0xFF9AA3AF),
    outline = Color(0xFF232329),
    error = Color(0xFFFF8A9B),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E8C66),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F5E7),
    onPrimaryContainer = Color(0xFF04291C),
    secondary = Color(0xFF2F6BD8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7FF),
    onSecondaryContainer = Color(0xFF0B2350),
    tertiary = Color(0xFFB07B00),
    background = Color(0xFFF4F7F5),
    onBackground = Color(0xFF141A1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141A1F),
    surfaceVariant = Color(0xFFE7EDEA),
    onSurfaceVariant = Color(0xFF53625C),
    outline = Color(0xFFC6D2CC),
)

@Composable
fun JeeGemTheme(
    choice: ThemeChoice = ThemeChoice.DARK,
    content: @Composable () -> Unit,
) {
    val scheme = when {
        choice == ThemeChoice.AMOLED -> AmoledColors
        choice == ThemeChoice.LIGHT -> LightColors
        choice == ThemeChoice.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
        else -> DarkColors
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

private val tintCache = mutableMapOf<String, Color>()

fun subjectColor(hex: String): Color = tintCache.getOrPut(hex) {
    runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(GemBlue)
}
