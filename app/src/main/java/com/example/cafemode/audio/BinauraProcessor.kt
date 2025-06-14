package com.example.cafemode.audio

class BinauraProcessor {

    // HRTF coefficients for basic binaural processing
    private val hrtfCoefficients = floatArrayOf(
        0.5f, 0.3f, 0.1f, -0.1f, -0.05f, 0.02f
    )

    fun processBinaural(leftChannel: FloatArray, rightChannel: FloatArray, distance: Float = 1.0f) {
        val delayFactor = (distance * 0.1f).coerceIn(0f, 0.5f)
        val attenuationFactor = 1.0f / (1.0f + distance * 0.3f)

        // Apply distance-based delay and attenuation
        for (i in leftChannel.indices) {
            leftChannel[i] *= attenuationFactor
            rightChannel[i] *= attenuationFactor
        }

        // Apply HRTF filtering for spatial realism
        applyHRTF(leftChannel, rightChannel)
    }

    private fun applyHRTF(left: FloatArray, right: FloatArray) {
        val filterLength = hrtfCoefficients.size

        for (i in filterLength until left.size) {
            var leftSum = 0f
            var rightSum = 0f

            for (j in hrtfCoefficients.indices) {
                leftSum += left[i - j] * hrtfCoefficients[j]
                rightSum += right[i - j] * hrtfCoefficients[j] * 0.9f // Slight asymmetry
            }

            left[i] = leftSum
            right[i] = rightSum
        }
    }
}
