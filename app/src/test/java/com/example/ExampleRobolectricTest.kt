package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vector Exit", appName)
    }

    @Test
    fun testGamePreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)
        prefs.resetAllProgress()

        assertEquals(1, prefs.currentLevel)
        assertEquals(100, prefs.coins)

        prefs.addCoins(50)
        assertEquals(150, prefs.coins)

        assertTrue(prefs.deductCoins(20))
        assertEquals(130, prefs.coins)

        assertFalse(prefs.deductCoins(200)) // cannot become negative
        assertEquals(130, prefs.coins)

        // Level completion & stars
        assertFalse(prefs.isLevelCompleted(1))
        prefs.markLevelCompleted(level = 1, stars = 3, arrowsCleared = 4)
        assertTrue(prefs.isLevelCompleted(1))
        assertEquals(3, prefs.getStarsForLevel(1))
        assertEquals(2, prefs.highestUnlockedLevel)
    }
}
