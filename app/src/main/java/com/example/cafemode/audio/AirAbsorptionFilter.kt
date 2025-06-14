package com.example.cafemode.audio

import kotlin.math.*

class AirAbsorptionFilter(private val sampleRate: Int = 44100) {
    private var previousLeft = 0f
    private var previousRight = 0f

    fun processAirAbsorption(
        leftChannel: FloatArray,
        rightChannel: FloatArray,
        distance: Float = 5f
    ) {
        // Calculate absorption coefficient based on distance
        val absorptionCoeff = calculateAbsorptionCoeff(distance)

        for (i in leftChannel.indices) {
            // High-frequency rolloff simulation
            leftChannel[i] = applyAbsorption(leftChannel[i], previousLeft, absorptionCoeff)
            rightChannel[i] = applyAbsorption(rightChannel[i], previousRight, absorptionCoeff)

            previousLeft = leftChannel[i]
            previousRight = rightChannel[i]
        }
    }

    private fun calculateAbsorptionCoeff(distance: Float): Float {
        // Frequency-dependent absorption increases with distance
        return (0.98f - (distance * 0.002f)).coerceIn(0.85f, 0.98f)
    }

    private fun applyAbsorption(current: Float, previous: Float, coeff: Float): Float {
        return current * coeff + previous * (1f - coeff)
    }
}
