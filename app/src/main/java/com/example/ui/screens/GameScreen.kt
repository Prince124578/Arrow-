package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.Arrow
import com.example.model.PuzzleBoard
import com.example.ui.components.ArrowBoardCanvas
import com.example.ui.components.BottomControls
import com.example.ui.components.GeometricBackground
import com.example.ui.components.HeaderBar
import com.example.ui.dialogs.CompletionDialog
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.PauseDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.DeepNavy

@Composable
fun GameScreen(
    board: PuzzleBoard,
    activeArrows: List<Arrow>,
    livesRemaining: Int,
    coins: Int,
    canUndo: Boolean,
    animatingArrow: Arrow?,
    exitProgress: Float,
    blockedArrowId: String?,
    shakeProgress: Float,
    hintArrowId: String?,
    showCompletionDialog: Boolean,
    showGameOverDialog: Boolean,
    showPauseDialog: Boolean,
    showSettingsDialog: Boolean,
    completionStars: Int,
    completionCoinsEarned: Int,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    reducedMotion: Boolean,
    onArrowTapped: (Arrow) -> Unit,
    onHintClick: () -> Unit,
    onRestartClick: () -> Unit,
    onUndoClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onNextLevelClick: () -> Unit,
    onHomeClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back navigation: open pause dialog or return to home
    BackHandler {
        if (!showCompletionDialog && !showGameOverDialog) {
            onPauseClick()
        } else {
            onHomeClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        GeometricBackground(reducedMotion = reducedMotion)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header Bar
            HeaderBar(
                levelNumber = board.levelNumber,
                levelTitle = board.title,
                livesRemaining = livesRemaining,
                coins = coins,
                onPauseClick = onPauseClick
            )

            // 2. Center Puzzle Board Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                ArrowBoardCanvas(
                    board = board,
                    activeArrows = activeArrows,
                    animatingArrow = animatingArrow,
                    exitProgress = exitProgress,
                    blockedArrowId = blockedArrowId,
                    shakeProgress = shakeProgress,
                    hintArrowId = hintArrowId,
                    onArrowTapped = onArrowTapped
                )
            }

            // 3. Bottom Controls
            BottomControls(
                arrowsRemaining = activeArrows.size,
                totalArrows = board.arrows.size,
                canUndo = canUndo,
                coins = coins,
                hintCost = 20,
                onHintClick = onHintClick,
                onRestartClick = onRestartClick,
                onUndoClick = onUndoClick
            )
        }

        // --- Dialogs & Overlays ---

        if (showPauseDialog) {
            PauseDialog(
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                onResume = onResumeClick,
                onRestart = {
                    onResumeClick()
                    onRestartClick()
                },
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics,
                onHome = onHomeClick
            )
        }

        if (showCompletionDialog) {
            CompletionDialog(
                levelNumber = board.levelNumber,
                stars = completionStars,
                coinsEarned = completionCoinsEarned,
                arrowsCleared = board.arrows.size,
                onNextLevel = onNextLevelClick,
                onReplay = onRestartClick,
                onHome = onHomeClick
            )
        }

        if (showGameOverDialog) {
            GameOverDialog(
                onRetry = onRestartClick,
                onHome = onHomeClick
            )
        }
    }
}
