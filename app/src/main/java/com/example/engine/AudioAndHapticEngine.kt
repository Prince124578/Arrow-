package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioAndHapticEngine(private val context: Context) {

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playTap() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 580f, durationMs = 35, volume = 0.4f)
        }
    }

    fun playArrowEscape() {
        if (!soundEnabled) return
        scope.launch {
            playSweepTone(startFreq = 440f, endFreq = 880f, durationMs = 120, volume = 0.55f)
        }
    }

    fun playBlocked() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 140f, durationMs = 70, volume = 0.6f)
        }
    }

    fun playHint() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 659.25f, durationMs = 70, volume = 0.5f)
            playTone(frequency = 880f, durationMs = 100, volume = 0.5f)
        }
    }

    fun playCoin() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 987.77f, durationMs = 50, volume = 0.45f)
            playTone(frequency = 1318.51f, durationMs = 80, volume = 0.5f)
        }
    }

    fun playLevelWin() {
        if (!soundEnabled) return
        scope.launch {
            val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (note in notes) {
                playTone(frequency = note, durationMs = 80, volume = 0.6f)
            }
        }
    }

    // --- Haptic Feedback ---

    fun vibrateTap() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    fun vibrateBlocked() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 40, 45)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) {}
    }

    fun vibrateWin() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 50, 40, 50, 80)
                val amplitudes = intArrayOf(0, 150, 0, 180, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    // --- Pure Waveform Synthesizers ---

    private fun playTone(frequency: Float, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val rawSine = sin(2.0 * PI * frequency * time)
                // Window envelope for zero clicking
                val envelope = sin(PI * i / numSamples)
                val sample = (rawSine * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {}
    }

    private fun playSweepTone(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val fraction = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * fraction
                val time = i.toDouble() / sampleRate
                val rawSine = sin(2.0 * PI * currentFreq * time)
                val envelope = sin(PI * i / numSamples)
                val sample = (rawSine * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {}
    }
}
