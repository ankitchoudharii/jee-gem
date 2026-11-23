package app.jeegem.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val GemGreen = Color(0xFF12A87A)
val GemMint = Color(0xFF7FE3B8)
val GemGold = Color(0xFFE2A93B)
val GemBlue = Color(0xFF3B6FE0)

private val LightColors = lightColorScheme(
    primary = GemGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F5E6),
    onPrimaryContainer = Color(0xFF04301F),
    secondary = GemBlue,
    onSecondary = Color.White,
    tertiary = GemGold,
)

private val DarkColors = darkColorScheme(
    primary = GemMint,
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF0B5945),
    onPrimaryContainer = Color(0xFFC7F7E3),
    secondary = Color(0xFF9DB8FF),
    tertiary = Color(0xFFF3C766),
    background = Color(0xFF0B1220),
    surface = Color(0xFF111A2B),
    surfaceVariant = Color(0xFF1B2638),
)

@Composable
fun JeeGemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
