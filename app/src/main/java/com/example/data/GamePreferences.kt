package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class GameStatistics(
    val totalLevelsCompleted: Int = 0,
    val totalArrowsCleared: Int = 0,
    val totalHintsUsed: Int = 0,
    val totalMistakes: Int = 0,
    val dailyStreak: Int = 0
)

data class GameSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false
)

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vector_exit_game_prefs", Context.MODE_PRIVATE)

    var currentLevel: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCKED, 1)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_UNLOCKED, value).apply()

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 100) // Start with 100 bonus coins
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var dailyLastCompletedDate: String?
        get() = prefs.getString(KEY_DAILY_DATE, null)
        set(value) = prefs.edit().putString(KEY_DAILY_DATE, value).apply()

    var dailyStreak: Int
        get() = prefs.getInt(KEY_DAILY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_STREAK, value).apply()

    // Settings
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var reducedMotion: Boolean
        get() = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCED_MOTION, value).apply()

    // Statistics
    var totalLevelsCompleted: Int
        get() = prefs.getInt(KEY_STAT_LEVELS_COMPLETED, 0)
        set(value) = prefs.edit().putInt(KEY_STAT_LEVELS_COMPLETED, value).apply()

    var totalArrowsCleared: Int
        get() = prefs.getInt(KEY_STAT_ARROWS_CLEARED, 0)
        set(value) = prefs.edit().putInt(KEY_STAT_ARROWS_CLEARED, value).apply()

    var totalHintsUsed: Int
        get() = prefs.getInt(KEY_STAT_HINTS_USED, 0)
        set(value) = prefs.edit().putInt(KEY_STAT_HINTS_USED, value).apply()

    var totalMistakes: Int
        get() = prefs.getInt(KEY_STAT_MISTAKES, 0)
        set(value) = prefs.edit().putInt(KEY_STAT_MISTAKES, value).apply()

    fun getStarsForLevel(level: Int): Int {
        return prefs.getInt(KEY_STARS_PREFIX + level, 0)
    }

    fun setStarsForLevel(level: Int, stars: Int) {
        val existing = getStarsForLevel(level)
        if (stars > existing) {
            prefs.edit().putInt(KEY_STARS_PREFIX + level, stars).apply()
        }
    }

    fun isLevelCompleted(level: Int): Boolean {
        return getStarsForLevel(level) > 0
    }

    fun markLevelCompleted(level: Int, stars: Int, arrowsCleared: Int): Boolean {
        val wasFirstTime = !isLevelCompleted(level)
        setStarsForLevel(level, stars)

        if (level >= highestUnlockedLevel) {
            highestUnlockedLevel = level + 1
        }

        totalLevelsCompleted += 1
        totalArrowsCleared += arrowsCleared

        return wasFirstTime
    }

    fun addCoins(amount: Int) {
        if (amount > 0) {
            coins += amount
        }
    }

    fun deductCoins(amount: Int): Boolean {
        if (coins >= amount) {
            coins -= amount
            return true
        }
        return false
    }

    fun getStatistics(): GameStatistics {
        return GameStatistics(
            totalLevelsCompleted = totalLevelsCompleted,
            totalArrowsCleared = totalArrowsCleared,
            totalHintsUsed = totalHintsUsed,
            totalMistakes = totalMistakes,
            dailyStreak = dailyStreak
        )
    }

    fun getSettings(): GameSettings {
        return GameSettings(
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            reducedMotion = reducedMotion
        )
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        // restore starting coins
        coins = 100
        currentLevel = 1
        highestUnlockedLevel = 1
    }

    companion object {
        private const val KEY_CURRENT_LEVEL = "key_current_level"
        private const val KEY_HIGHEST_UNLOCKED = "key_highest_unlocked"
        private const val KEY_COINS = "key_coins"
        private const val KEY_DAILY_DATE = "key_daily_date"
        private const val KEY_DAILY_STREAK = "key_daily_streak"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"

        private const val KEY_STAT_LEVELS_COMPLETED = "key_stat_levels_completed"
        private const val KEY_STAT_ARROWS_CLEARED = "key_stat_arrows_cleared"
        private const val KEY_STAT_HINTS_USED = "key_stat_hints_used"
        private const val KEY_STAT_MISTAKES = "key_stat_mistakes"

        private const val KEY_STARS_PREFIX = "key_stars_level_"
    }
}
