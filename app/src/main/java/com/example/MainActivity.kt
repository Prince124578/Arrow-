package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.StatsDialog
import com.example.ui.screens.DailyPuzzleScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreenState
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepNavy
                ) {
                    VectorExitApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VectorExitApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.screen) {
        GameScreenState.HOME -> {
            HomeScreen(
                currentLevel = uiState.currentLevel,
                coins = uiState.coins,
                isDailyCompleted = uiState.isDailyCompletedToday,
                soundEnabled = uiState.settings.soundEnabled,
                reducedMotion = uiState.settings.reducedMotion,
                onPlayClick = { viewModel.startLevel(uiState.currentLevel) },
                onLevelSelectClick = { viewModel.navigateTo(GameScreenState.LEVEL_SELECT) },
                onDailyPuzzleClick = { viewModel.navigateTo(GameScreenState.DAILY_PUZZLE) },
                onStatsClick = { viewModel.setStatsDialogVisible(true) },
                onToggleSound = { viewModel.toggleSound() },
                onSettingsClick = { viewModel.setSettingsDialogVisible(true) }
            )

            if (uiState.showSettingsDialog) {
                SettingsDialog(
                    soundEnabled = uiState.settings.soundEnabled,
                    hapticsEnabled = uiState.settings.hapticsEnabled,
                    reducedMotion = uiState.settings.reducedMotion,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onToggleReducedMotion = { viewModel.toggleReducedMotion() },
                    onResetProgress = { viewModel.resetAllProgress() },
                    onDismiss = { viewModel.setSettingsDialogVisible(false) }
                )
            }

            if (uiState.showStatsDialog) {
                StatsDialog(
                    stats = viewModel.getStatistics(),
                    currentLevel = uiState.currentLevel,
                    coins = uiState.coins,
                    onDismiss = { viewModel.setStatsDialogVisible(false) }
                )
            }
        }

        GameScreenState.GAMEPLAY -> {
            val board = uiState.board
            if (board != null) {
                GameScreen(
                    board = board,
                    activeArrows = uiState.activeArrows,
                    livesRemaining = uiState.livesRemaining,
                    coins = uiState.coins,
                    canUndo = uiState.canUndo,
                    animatingArrow = uiState.animatingArrow,
                    exitProgress = uiState.exitProgress,
                    blockedArrowId = uiState.blockedArrowId,
                    shakeProgress = uiState.shakeProgress,
                    hintArrowId = uiState.hintArrowId,
                    showCompletionDialog = uiState.showCompletionDialog,
                    showGameOverDialog = uiState.showGameOverDialog,
                    showPauseDialog = uiState.showPauseDialog,
                    showSettingsDialog = uiState.showSettingsDialog,
                    completionStars = uiState.completionStars,
                    completionCoinsEarned = uiState.completionCoinsEarned,
                    soundEnabled = uiState.settings.soundEnabled,
                    hapticsEnabled = uiState.settings.hapticsEnabled,
                    reducedMotion = uiState.settings.reducedMotion,
                    onArrowTapped = { viewModel.onArrowTapped(it) },
                    onHintClick = { viewModel.onHintClicked() },
                    onRestartClick = { viewModel.onRestartLevel() },
                    onUndoClick = { viewModel.onUndoClicked() },
                    onPauseClick = { viewModel.setPauseDialogVisible(true) },
                    onResumeClick = { viewModel.setPauseDialogVisible(false) },
                    onNextLevelClick = {
                        if (uiState.isDailyPuzzle) {
                            viewModel.navigateTo(GameScreenState.HOME)
                        } else {
                            viewModel.onNextLevel()
                        }
                    },
                    onHomeClick = { viewModel.navigateTo(GameScreenState.HOME) },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onToggleReducedMotion = { viewModel.toggleReducedMotion() },
                    onResetProgress = { viewModel.resetAllProgress() }
                )
            }
        }

        GameScreenState.LEVEL_SELECT -> {
            LevelSelectScreen(
                highestUnlockedLevel = uiState.highestUnlockedLevel,
                coins = uiState.coins,
                getStarsForLevel = { viewModel.getStarsForLevel(it) },
                onLevelSelected = { level -> viewModel.startLevel(level) },
                onBack = { viewModel.navigateTo(GameScreenState.HOME) }
            )
        }

        GameScreenState.DAILY_PUZZLE -> {
            DailyPuzzleScreen(
                isCompletedToday = uiState.isDailyCompletedToday,
                dailyStreak = uiState.dailyStreak,
                coins = uiState.coins,
                onStartDaily = { viewModel.startDailyPuzzle() },
                onBack = { viewModel.navigateTo(GameScreenState.HOME) }
            )
        }
    }
}
