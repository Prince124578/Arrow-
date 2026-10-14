package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GamePreferences
import com.example.data.GameSettings
import com.example.data.GameStatistics
import com.example.engine.AudioAndHapticEngine
import com.example.engine.CollisionEngine
import com.example.engine.PuzzleGenerator
import com.example.model.Arrow
import com.example.model.PuzzleBoard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GameScreenState {
    HOME,
    GAMEPLAY,
    LEVEL_SELECT,
    DAILY_PUZZLE
}

data class GameUiState(
    val screen: GameScreenState = GameScreenState.HOME,
    val currentLevel: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val isDailyPuzzle: Boolean = false,
    val board: PuzzleBoard? = null,
    val activeArrows: List<Arrow> = emptyList(),
    val livesRemaining: Int = 3,
    val coins: Int = 100,
    val canUndo: Boolean = false,
    val animatingArrow: Arrow? = null,
    val exitProgress: Float = 0f,
    val blockedArrowId: String? = null,
    val shakeProgress: Float = 0f,
    val hintArrowId: String? = null,
    val isBusy: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val showPauseDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showStatsDialog: Boolean = false,
    val completionStars: Int = 3,
    val completionCoinsEarned: Int = 0,
    val arrowsClearedCount: Int = 0,
    val isDailyCompletedToday: Boolean = false,
    val dailyStreak: Int = 0,
    val settings: GameSettings = GameSettings()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GamePreferences(application)
    val audioEngine = AudioAndHapticEngine(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<Arrow>()
    private var animationJob: Job? = null

    init {
        audioEngine.soundEnabled = prefs.soundEnabled
        audioEngine.hapticsEnabled = prefs.hapticsEnabled
        refreshFromPrefs()
    }

    private fun refreshFromPrefs() {
        val todayKey = getTodayDateKey()
        val isDailyDone = (prefs.dailyLastCompletedDate == todayKey)
        _uiState.update {
            it.copy(
                currentLevel = prefs.currentLevel,
                highestUnlockedLevel = prefs.highestUnlockedLevel,
                coins = prefs.coins,
                isDailyCompletedToday = isDailyDone,
                dailyStreak = prefs.dailyStreak,
                settings = prefs.getSettings()
            )
        }
    }

    private fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    // --- Screen Navigation ---

    fun navigateTo(screen: GameScreenState) {
        audioEngine.playTap()
        _uiState.update { it.copy(screen = screen) }
    }

    fun startLevel(levelNumber: Int) {
        audioEngine.playTap()
        val board = PuzzleGenerator.getLevel(levelNumber)
        undoStack.clear()
        _uiState.update {
            it.copy(
                screen = GameScreenState.GAMEPLAY,
                currentLevel = levelNumber,
                isDailyPuzzle = false,
                board = board,
                activeArrows = board.arrows,
                livesRemaining = 3,
                canUndo = false,
                animatingArrow = null,
                blockedArrowId = null,
                hintArrowId = null,
                isBusy = false,
                showCompletionDialog = false,
                showGameOverDialog = false,
                showPauseDialog = false,
                arrowsClearedCount = 0
            )
        }
    }

    fun startDailyPuzzle() {
        audioEngine.playTap()
        val todayKey = getTodayDateKey()
        val board = PuzzleGenerator.getDailyPuzzle(todayKey)
        undoStack.clear()
        _uiState.update {
            it.copy(
                screen = GameScreenState.GAMEPLAY,
                isDailyPuzzle = true,
                board = board,
                activeArrows = board.arrows,
                livesRemaining = 3,
                canUndo = false,
                animatingArrow = null,
                blockedArrowId = null,
                hintArrowId = null,
                isBusy = false,
                showCompletionDialog = false,
                showGameOverDialog = false,
                showPauseDialog = false,
                arrowsClearedCount = 0
            )
        }
    }

    // --- Gameplay Core ---

    fun onArrowTapped(arrow: Arrow) {
        val state = _uiState.value
        val board = state.board ?: return
        if (state.isBusy || state.showCompletionDialog || state.showGameOverDialog || state.showPauseDialog) return

        val canEscape = CollisionEngine.canArrowEscape(
            arrow = arrow,
            activeArrows = state.activeArrows,
            boardWidth = board.width,
            boardHeight = board.height
        )

        if (canEscape) {
            handleArrowEscape(arrow, board)
        } else {
            handleArrowBlocked(arrow)
        }
    }

    private fun handleArrowEscape(arrow: Arrow, board: PuzzleBoard) {
        audioEngine.playArrowEscape()
        audioEngine.vibrateTap()

        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    animatingArrow = arrow,
                    exitProgress = 0f,
                    activeArrows = it.activeArrows.filter { a -> a.id != arrow.id }
                )
            }

            // Animate arrow sliding out
            val durationMs = if (_uiState.value.settings.reducedMotion) 80 else 240
            val steps = 12
            val stepTime = durationMs / steps
            for (step in 1..steps) {
                delay(stepTime.toLong())
                _uiState.update { it.copy(exitProgress = step.toFloat() / steps) }
            }

            // Add to undo history
            undoStack.add(arrow)
            val remainingArrows = _uiState.value.activeArrows
            val clearedCount = _uiState.value.arrowsClearedCount + 1

            _uiState.update {
                it.copy(
                    isBusy = false,
                    animatingArrow = null,
                    exitProgress = 0f,
                    canUndo = undoStack.isNotEmpty(),
                    hintArrowId = if (it.hintArrowId == arrow.id) null else it.hintArrowId,
                    arrowsClearedCount = clearedCount
                )
            }

            // Check Level Completion
            if (remainingArrows.isEmpty()) {
                handleLevelComplete(board)
            }
        }
    }

    private fun handleArrowBlocked(arrow: Arrow) {
        audioEngine.playBlocked()
        audioEngine.vibrateBlocked()

        prefs.totalMistakes += 1

        val currentLives = _uiState.value.livesRemaining - 1

        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    blockedArrowId = arrow.id,
                    shakeProgress = 0f,
                    livesRemaining = currentLives.coerceAtLeast(0)
                )
            }

            val steps = 10
            for (step in 1..steps) {
                delay(24)
                _uiState.update { it.copy(shakeProgress = step.toFloat() / steps) }
            }

            _uiState.update {
                it.copy(
                    isBusy = false,
                    blockedArrowId = null,
                    shakeProgress = 0f
                )
            }

            if (currentLives <= 0) {
                _uiState.update { it.copy(showGameOverDialog = true) }
            }
        }
    }

    private fun handleLevelComplete(board: PuzzleBoard) {
        audioEngine.playLevelWin()
        audioEngine.vibrateWin()

        val state = _uiState.value
        val stars = when (state.livesRemaining) {
            3 -> 3
            2 -> 2
            else -> 1
        }

        var rewardCoins = 0
        if (state.isDailyPuzzle) {
            rewardCoins = 50
            prefs.addCoins(rewardCoins)
            val todayKey = getTodayDateKey()
            if (prefs.dailyLastCompletedDate != todayKey) {
                prefs.dailyLastCompletedDate = todayKey
                prefs.dailyStreak += 1
            }
        } else {
            val wasFirstTime = prefs.markLevelCompleted(
                level = board.levelNumber,
                stars = stars,
                arrowsCleared = board.arrows.size
            )
            rewardCoins = if (wasFirstTime) 30 else 10
            prefs.addCoins(rewardCoins)
            if (board.levelNumber >= prefs.currentLevel) {
                prefs.currentLevel = board.levelNumber + 1
            }
        }

        _uiState.update {
            it.copy(
                coins = prefs.coins,
                highestUnlockedLevel = prefs.highestUnlockedLevel,
                currentLevel = prefs.currentLevel,
                dailyStreak = prefs.dailyStreak,
                isDailyCompletedToday = (prefs.dailyLastCompletedDate == getTodayDateKey()),
                showCompletionDialog = true,
                completionStars = stars,
                completionCoinsEarned = rewardCoins
            )
        }
    }

    // --- Controls ---

    fun onHintClicked() {
        val state = _uiState.value
        val board = state.board ?: return
        if (state.isBusy) return

        val cost = 20
        if (prefs.coins >= cost) {
            prefs.deductCoins(cost)
        } // Allow hint even if coins are low for generous assistance

        audioEngine.playHint()
        audioEngine.vibrateTap()

        prefs.totalHintsUsed += 1

        val escapable = CollisionEngine.findEscapableArrows(
            activeArrows = state.activeArrows,
            boardWidth = board.width,
            boardHeight = board.height
        )

        val target = escapable.firstOrNull()
        _uiState.update {
            it.copy(
                coins = prefs.coins,
                hintArrowId = target?.id
            )
        }
    }

    fun onUndoClicked() {
        if (undoStack.isEmpty() || _uiState.value.isBusy) return
        val lastArrow = undoStack.removeAt(undoStack.size - 1)
        audioEngine.playTap()

        _uiState.update {
            it.copy(
                activeArrows = it.activeArrows + lastArrow,
                canUndo = undoStack.isNotEmpty(),
                hintArrowId = null
            )
        }
    }

    fun onRestartLevel() {
        audioEngine.playTap()
        val board = _uiState.value.board ?: return
        undoStack.clear()
        _uiState.update {
            it.copy(
                activeArrows = board.arrows,
                livesRemaining = 3,
                canUndo = false,
                animatingArrow = null,
                blockedArrowId = null,
                hintArrowId = null,
                isBusy = false,
                showCompletionDialog = false,
                showGameOverDialog = false,
                showPauseDialog = false
            )
        }
    }

    fun onNextLevel() {
        audioEngine.playTap()
        val nextLevel = _uiState.value.currentLevel
        startLevel(nextLevel)
    }

    // --- Dialogs ---

    fun setPauseDialogVisible(visible: Boolean) {
        audioEngine.playTap()
        _uiState.update { it.copy(showPauseDialog = visible) }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        audioEngine.playTap()
        _uiState.update { it.copy(showSettingsDialog = visible) }
    }

    fun setStatsDialogVisible(visible: Boolean) {
        audioEngine.playTap()
        _uiState.update { it.copy(showStatsDialog = visible) }
    }

    // --- Settings Toggles ---

    fun toggleSound() {
        val newValue = !prefs.soundEnabled
        prefs.soundEnabled = newValue
        audioEngine.soundEnabled = newValue
        if (newValue) audioEngine.playTap()
        _uiState.update { it.copy(settings = prefs.getSettings()) }
    }

    fun toggleHaptics() {
        val newValue = !prefs.hapticsEnabled
        prefs.hapticsEnabled = newValue
        audioEngine.hapticsEnabled = newValue
        if (newValue) audioEngine.vibrateTap()
        _uiState.update { it.copy(settings = prefs.getSettings()) }
    }

    fun toggleReducedMotion() {
        val newValue = !prefs.reducedMotion
        prefs.reducedMotion = newValue
        _uiState.update { it.copy(settings = prefs.getSettings()) }
    }

    fun resetAllProgress() {
        prefs.resetAllProgress()
        undoStack.clear()
        refreshFromPrefs()
        _uiState.update {
            it.copy(
                screen = GameScreenState.HOME,
                board = null,
                activeArrows = emptyList(),
                showCompletionDialog = false,
                showGameOverDialog = false,
                showPauseDialog = false,
                showSettingsDialog = false
            )
        }
    }

    fun getStarsForLevel(level: Int): Int {
        return prefs.getStarsForLevel(level)
    }

    fun getStatistics(): GameStatistics {
        return prefs.getStatistics()
    }
}
