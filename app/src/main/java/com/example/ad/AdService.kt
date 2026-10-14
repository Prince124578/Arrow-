package com.example.ad

/**
 * Optional ad-service abstraction layer.
 * Keeps the game completely functional and offline-first without requiring any ad network SDK.
 */
interface AdService {
    fun isRewardedAdReady(): Boolean
    fun showRewardedAd(onRewardEarned: () -> Unit)
}

class NoOpAdService : AdService {
    override fun isRewardedAdReady(): Boolean = false
    override fun showRewardedAd(onRewardEarned: () -> Unit) {
        // No-op fallback - in offline mode, players continue directly without ads
        onRewardEarned()
    }
}
