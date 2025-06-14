package com.example.cafemode.audio

import kotlin.math.*

class SpatialWidener {
    private var allPassDelay = FloatArray(512)
    private var delayIndex = 0

    fun widenStereoField(
        leftChannel: FloatArray,
        rightChannel: FloatArray,
        width: Float = 0.5f
    ) {
        val widthFactor = width.coerceIn(0f, 1f)

        for (i in leftChannel.indices) {
            val mid = (leftChannel[i] + rightChannel[i]) * 0.5f
            val side = (leftChannel[i] - rightChannel[i]) * widthFactor

            // Apply all-pass filtering for more natural widening
            val processedSide = processAllPass(side)

            leftChannel[i] = mid + processedSide
            rightChannel[i] = mid - processedSide
        }
    }

    private fun processAllPass(input: Float): Float {
        val delayed = allPassDelay[delayIndex]
        allPassDelay[delayIndex] = input + delayed * 0.7f
        delayIndex = (delayIndex + 1) % allPassDelay.size
        return delayed - input * 0.7f
    }
}