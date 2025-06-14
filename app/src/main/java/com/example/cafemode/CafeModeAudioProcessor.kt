package com.example.cafemode

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.PresetReverb
import android.util.Log
import kotlin.math.*

class CafeModeAudioProcessor(effectType: String) {
    private var equalizer: Equalizer? = null
    private var reverb: EnvironmentalReverb? = null
    private var presetReverb: PresetReverb? = null

    // Café mode parameters
    private var intensity = 0.5f
    private var spatialWidth = 0.5f
    private var isEnabled = false

    // Audio processing parameters
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // Reverb parameters for café simulation
    private val cafeReverbSettings = mapOf(
        "roomSize" to 0.3f,      // Medium room size
        "damping" to 0.6f,       // Moderate damping
        "wetLevel" to 0.2f,      // Subtle reverb mix
        "dryLevel" to 0.8f,      // Keep original signal prominent
        "predelay" to 20f        // Short predelay for intimacy
    )

    init {
        initializeEffects()
    }

    private fun initializeEffects() {
        try {
            // Initialize Equalizer for frequency shaping
            equalizer = Equalizer(0, 0).apply {
                enabled = false
                setupCafeEQ()
            }

            // Initialize Environmental Reverb for spatial processing
            reverb = EnvironmentalReverb(0, 0).apply {
                enabled = false
                setupCafeReverb()
            }

            // Backup preset reverb
            presetReverb = PresetReverb(0, 0).apply {
                enabled = false
                preset = PresetReverb.PRESET_SMALLROOM
            }

        } catch (e: Exception) {
            Log.e("CafeModeProcessor", "Error initializing audio effects", e)
        }
    }

    private fun setupCafeEQ() {
        equalizer?.let { eq ->
            val numBands = eq.numberOfBands.toInt()

            for (i in 0 until numBands) {
                val centerFreq = eq.getCenterFreq(i.toShort())
                val bandLevel = calculateCafeBandLevel(centerFreq)
                eq.setBandLevel(i.toShort(), bandLevel)
            }
        }
    }

    private fun calculateCafeBandLevel(frequency: Int): Short {
        // Sony-style café mode EQ curve
        val freqHz = frequency / 1000.0

        val level = when {
            // Sub-bass rolloff (simulate distance)
            freqHz < 0.08 -> -800 // -8dB
            freqHz < 0.2 -> -400   // -4dB

            // Gentle low-mid boost for warmth
            freqHz < 0.5 -> 0
            freqHz < 1.0 -> 200    // +2dB

            // Presence range slight cut for smoothness
            freqHz < 3.0 -> 100    // +1dB
            freqHz < 5.0 -> -100   // -1dB

            // High frequency air absorption simulation
            freqHz < 8.0 -> -200   // -2dB
            freqHz < 12.0 -> -400  // -4dB
            else -> -600           // -6dB
        }

        return (level * intensity).toInt().toShort()
    }

    private fun setupCafeReverb() {
        reverb?.let { rv ->
            try {
                // Room size - medium café space
                rv.roomSize = (cafeReverbSettings["roomSize"]!! * 1000).toInt().toShort()

                // Damping - simulate soft furnishings
                rv.damping = (cafeReverbSettings["damping"]!! * 1000).toInt().toShort()

                // Wet/Dry mix
                rv.reverbLevel = (cafeReverbSettings["wetLevel"]!! * intensity * 1000).toInt().toShort()
                rv.roomLevel = (cafeReverbSettings["dryLevel"]!! * 1000).toInt().toShort()

                // Pre-delay for intimacy
                rv.reflectionsDelay = cafeReverbSettings["predelay"]!!.toInt()

                // Additional café-specific settings
                rv.density = (700 * intensity).toInt().toShort()      // Moderate density
                rv.diffusion = (800 * intensity).toInt().toShort()    // Good diffusion
                rv.reflectionsLevel = (-300 * intensity).toInt().toShort() // Subtle early reflections

            } catch (e: Exception) {
                Log.e("CafeModeProcessor", "Error setting up reverb", e)
            }
        }
    }

    fun enable() {
        if (!isEnabled) {
            try {
                equalizer?.enabled = true
                reverb?.enabled = true
                isEnabled = true
                Log.d("CafeModeProcessor", "Café mode enabled")
            } catch (e: Exception) {
                Log.e("CafeModeProcessor", "Error enabling café mode", e)
            }
        }
    }

    fun disable() {
        if (isEnabled) {
            try {
                equalizer?.enabled = false
                reverb?.enabled = false
                presetReverb?.enabled = false
                isEnabled = false
                Log.d("CafeModeProcessor", "Café mode disabled")
            } catch (e: Exception) {
                Log.e("CafeModeProcessor", "Error disabling café mode", e)
            }
        }
    }

    fun updateIntensity(newIntensity: Float) {
        intensity = newIntensity.coerceIn(0f, 1f)
        if (isEnabled) {
            setupCafeEQ()
            setupCafeReverb()
        }
    }

    fun updateSpatialWidth(newWidth: Float) {
        spatialWidth = newWidth.coerceIn(0f, 1f)
        // Adjust stereo width and reverb spread
        reverb?.let { rv ->
            try {
                val spreadFactor = (spatialWidth * 500).toInt().toShort()
                rv.diffusion = spreadFactor
            } catch (e: Exception) {
                Log.e("CafeModeProcessor", "Error updating spatial width", e)
            }
        }
    }

    fun release() {
        try {
            disable()
            equalizer?.release()
            reverb?.release()
            presetReverb?.release()
        } catch (e: Exception) {
            Log.e("CafeModeProcessor", "Error releasing audio processor", e)
        }
    }

    // Advanced spatial processing function
    private fun processSpatialAudio(inputBuffer: FloatArray): FloatArray {
        val outputBuffer = inputBuffer.copyOf()

        // Apply Haas effect for distance simulation
        val haasDelay = (spatialWidth * 15).toInt() // Up to 15ms delay
        if (haasDelay > 0) {
            applyHaasEffect(outputBuffer, haasDelay)
        }

        // Apply binaural processing for spatial width
        applyBinauralProcessing(outputBuffer)

        // Simulate air absorption
        applyAirAbsorption(outputBuffer)

        return outputBuffer
    }

    private fun applyHaasEffect(buffer: FloatArray, delaySamples: Int) {
        // Simple implementation of Haas effect
        // This creates the perception of spatial separation
        for (i in delaySamples until buffer.size step 2) {
            buffer[i] = 0.7f * buffer[i] + 0.3f * buffer[i - delaySamples]
        }
    }

    private fun applyBinauralProcessing(buffer: FloatArray) {
        // Simplified binaural processing for stereo width
        for (i in 0 until buffer.size - 1 step 2) {
            val left = buffer[i]
            val right = buffer[i + 1]

            val mid = (left + right) * 0.5f
            val side = (left - right) * spatialWidth

            buffer[i] = mid + side
            buffer[i + 1] = mid - side
        }
    }

    private fun applyAirAbsorption(buffer: FloatArray) {
        // High-frequency rolloff to simulate distance
        val cutoffFactor = 0.95f + (intensity * 0.04f)
        var previousSample = 0f

        for (i in buffer.indices) {
            buffer[i] = buffer[i] * cutoffFactor + previousSample * (1f - cutoffFactor)
            previousSample = buffer[i]
        }
    }
}