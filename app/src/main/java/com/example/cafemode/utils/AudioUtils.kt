package com.example.cafemode.utils

import android.media.AudioFormat
import android.media.AudioManager
import kotlin.math.*

object AudioUtils {
    const val SAMPLE_RATE = 44100
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    fun dbToLinear(db: Float): Float {
        return 10.0f.pow(db / 20.0f)
    }

    fun linearToDb(linear: Float): Float {
        return 20.0f * log10(linear.coerceAtLeast(0.000001f))
    }

    fun frequencyToMel(frequency: Float): Float {
        return 2595.0f * log10(1.0f + frequency / 700.0f)
    }

    fun melToFrequency(mel: Float): Float {
        return 700.0f * (10.0f.pow(mel / 2595.0f) - 1.0f)
    }

    fun normalizeAudio(samples: FloatArray): FloatArray {
        val maxValue = samples.maxByOrNull { abs(it) }?.let { abs(it) } ?: 1f
        if (maxValue == 0f) return samples

        val normalizedSamples = FloatArray(samples.size)
        val scaleFactor = 0.95f / maxValue // Leave some headroom

        for (i in samples.indices) {
            normalizedSamples[i] = samples[i] * scaleFactor
        }

        return normalizedSamples
    }

    fun fadeIn(samples: FloatArray, fadeSamples: Int) {
        val actualFadeSamples = minOf(fadeSamples, samples.size)
        for (i in 0 until actualFadeSamples) {
            val factor = i.toFloat() / actualFadeSamples.toFloat()
            samples[i] *= factor
        }
    }

    fun fadeOut(samples: FloatArray, fadeSamples: Int) {
        val actualFadeSamples = minOf(fadeSamples, samples.size)
        val startIndex = samples.size - actualFadeSamples

        for (i in 0 until actualFadeSamples) {
            val factor = (actualFadeSamples - i - 1).toFloat() / actualFadeSamples.toFloat()
            samples[startIndex + i] *= factor
        }
    }
}