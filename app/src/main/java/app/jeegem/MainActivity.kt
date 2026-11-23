package app.jeegem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.jeegem.ui.JeeGemApp
import app.jeegem.ui.theme.JeeGemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JeeGemTheme {
                JeeGemApp()
            }
        }
    }
}
