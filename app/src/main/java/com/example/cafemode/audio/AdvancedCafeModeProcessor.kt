package com.example.cafemode.audio

class AdvancedCafeModeProcessor {
    private val binauralProcessor = BinauralProcessor()
    private val airAbsorptionFilter = AirAbsorptionFilter()
    private val earlyReflectionGenerator = EarlyReflectionGenerator()
    private val spatialWidener = SpatialWidener()

    private var intensity = 0.5f
    private var spatialWidth = 0.5f
    private var roomSize = 0.3f
    private var distance = 5f

    fun processAudioBuffer(audioBuffer: FloatArray): FloatArray {
        if (audioBuffer.size % 2 != 0) return audioBuffer

        val leftChannel = FloatArray(audioBuffer.size / 2)
        val rightChannel = FloatArray(audioBuffer.size / 2)

        // Deinterleave stereo audio
        for (i in leftChannel.indices) {
            leftChannel[i] = audioBuffer[i * 2]
            rightChannel[i] = audioBuffer[i * 2 + 1]
        }

        // Apply café mode processing chain
        processAudioChain(leftChannel, rightChannel)

        // Reinterleave processed audio
        val processedBuffer = FloatArray(audioBuffer.size)
        for (i in leftChannel.indices) {
            processedBuffer[i * 2] = leftChannel[i]
            processedBuffer[i * 2 + 1] = rightChannel[i]
        }

        return processedBuffer
    }

    private fun processAudioChain(left: FloatArray, right: FloatArray) {
        // 1. Apply binaural processing for distance perception
        binauralProcessor.processBinaural(left, right, distance)

        // 2. Generate early reflections for room simulation
        earlyReflectionGenerator.processEarlyReflections(left, right, roomSize)

        // 3. Apply air absorption for realistic distance filtering
        airAbsorptionFilter.processAirAbsorption(left, right, distance)

        // 4. Widen stereo field for immersive experience
        spatialWidener.widenStereoField(left, right, spatialWidth)

        // 5. Apply final intensity scaling
        for (i in left.indices) {
            left[i] *= intensity
            right[i] *= intensity
        }
    }

    fun updateParameters(newIntensity: Float, newSpatialWidth: Float, newRoomSize: Float = 0.3f) {
        intensity = newIntensity.coerceIn(0f, 1f)
        spatialWidth = newSpatialWidth.coerceIn(0f, 1f)
        roomSize = newRoomSize.coerceIn(0.1f, 1f)
        distance = 3f + (intensity * 7f) // Distance varies from 3m to 10m
    }
}
