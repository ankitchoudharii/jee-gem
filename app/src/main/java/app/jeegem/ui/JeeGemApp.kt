package app.jeegem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.jeegem.data.Chapter
import app.jeegem.data.ChapterStatus
import app.jeegem.ui.screens.OnboardingScreen
import app.jeegem.ui.screens.ProgressScreen
import app.jeegem.ui.screens.RevisionScreen
import app.jeegem.ui.screens.SettingsDialog
import app.jeegem.ui.screens.SyllabusScreen
import app.jeegem.ui.screens.TimerSheet
import app.jeegem.ui.screens.TodayScreen
import app.jeegem.ui.theme.JeeGemTheme

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Filled.Home),
    Syllabus("syllabus", "Syllabus", Icons.Filled.List),
    Revision("revision", "Revision", Icons.Filled.Refresh),
    Progress("progress", "Progress", Icons.Filled.Star),
}

@Composable
fun JeeGemApp(vm: AppViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val timer by vm.timer.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    JeeGemTheme(choice = state.settings.theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                !state.ready -> LoadingScreen()

                state.showOnboarding -> OnboardingScreen(
                    onDone = { year, minutes -> vm.completeOnboarding(year, minutes) },
                )

                else -> MainScaffold(
                    state = state,
                    onStart = vm::startTimer,
                    onComplete = vm::completeTarget,
                    onCycleStatus = { chapter ->
                        val current = state.progress[chapter.id]?.status ?: ChapterStatus.NOT_STARTED
                        vm.setChapterStatus(chapter.id, current.next())
                    },
                    onRevised = vm::markRevised,
                    onOpenSettings = { showSettings = true },
                )
            }

            if (timer.active) {
                TimerSheet(
                    state = timer,
                    onToggle = vm::toggleTimer,
                    onFinish = vm::finishTimer,
                    onDismiss = vm::dismissTimer,
                )
            }

            if (showSettings) {
                SettingsDialog(
                    settings = state.settings,
                    onSave = vm::updateSettings,
                    onReset = vm::resetAll,
                    onClose = { showSettings = false },
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "JEE Gem",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun MainScaffold(
    state: UiState,
    onStart: (PlanTarget) -> Unit,
    onComplete: (PlanTarget) -> Unit,
    onCycleStatus: (Chapter) -> Unit,
    onRevised: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Dest.Today.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Dest.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Dest.Today.route) { TodayScreen(state, onStart, onComplete) }
            composable(Dest.Syllabus.route) { SyllabusScreen(state, onCycleStatus) }
            composable(Dest.Revision.route) { RevisionScreen(state, onRevised) }
            composable(Dest.Progress.route) { ProgressScreen(state, onOpenSettings) }
        }
    }
}

private fun ChapterStatus.next(): ChapterStatus = when (this) {
    ChapterStatus.NOT_STARTED -> ChapterStatus.LEARNING
    ChapterStatus.LEARNING -> ChapterStatus.REVISED
    ChapterStatus.REVISED -> ChapterStatus.MASTERED
    ChapterStatus.MASTERED -> ChapterStatus.NOT_STARTED
}
