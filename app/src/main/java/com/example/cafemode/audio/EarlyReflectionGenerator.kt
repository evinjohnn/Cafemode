package com.example.cafemode.audio

class EarlyReflectionGenerator {
    private val reflectionDelays = intArrayOf(15, 23, 31, 47, 63, 79) // milliseconds converted to samples
    private val reflectionGains = floatArrayOf(0.3f, 0.25f, 0.2f, 0.15f, 0.1f, 0.05f)
    private val delayBuffers = Array(reflectionDelays.size) { FloatArray(8192) }
    private val bufferIndices = IntArray(reflectionDelays.size)

    fun processEarlyReflections(
        leftChannel: FloatArray,
        rightChannel: FloatArray,
        roomSize: Float = 0.5f
    ) {
        val scaledGains = reflectionGains.map { it * roomSize }.toFloatArray()

        for (i in leftChannel.indices) {
            var reflectionSumLeft = 0f
            var reflectionSumRight = 0f

            // Generate early reflections
            for (j in reflectionDelays.indices) {
                val delayBuffer = delayBuffers[j]
                val delayIndex = bufferIndices[j]

                // Get delayed sample
                val delayedSample = delayBuffer[delayIndex]

                // Add to reflection sum
                reflectionSumLeft += delayedSample * scaledGains[j]
                reflectionSumRight += delayedSample * scaledGains[j] * 0.9f // Slight stereo difference

                // Store current sample in delay buffer
                delayBuffer[delayIndex] = (leftChannel[i] + rightChannel[i]) * 0.5f

                // Update buffer index
                bufferIndices[j] = (delayIndex + 1) % delayBuffer.size
            }

            // Mix reflections with original signal
            leftChannel[i] += reflectionSumLeft * 0.15f
            rightChannel[i] += reflectionSumRight * 0.15f
        }
    }
}
