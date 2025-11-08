package com.karafon.app.core.audio

import kotlin.math.min

/**
 * Audio effects processor - reverb and echo/delay
 */
class AudioEffects {

    companion object {
        private const val SAMPLE_RATE = 48000
        private const val MAX_DELAY_MS = 500
        private const val MAX_DELAY_SAMPLES = (SAMPLE_RATE * MAX_DELAY_MS) / 1000
    }

    // Echo/Delay parameters
    private var echoIntensity = 0.25f
    private var echoDelayMs = 300
    private val echoBuffer = ShortArray(MAX_DELAY_SAMPLES)
    private var echoBufferIndex = 0

    // Reverb parameters
    private var reverbIntensity = 0.3f
    private val reverbDelays = intArrayOf(37, 87, 181, 271) // Prime numbers for better reverb
    private val reverbBuffers = Array(reverbDelays.size) { ShortArray(MAX_DELAY_SAMPLES) }
    private val reverbIndices = IntArray(reverbDelays.size)

    /**
     * Apply all enabled effects to the audio buffer
     */
    fun applyEffects(buffer: ShortArray, size: Int) {
        applyEcho(buffer, size)
        applyReverb(buffer, size)
    }

    /**
     * Apply echo/delay effect
     */
    private fun applyEcho(buffer: ShortArray, size: Int) {
        val delaySamples = (SAMPLE_RATE * echoDelayMs) / 1000

        for (i in 0 until size) {
            val delayedSample = echoBuffer[echoBufferIndex]
            val mixedSample = buffer[i] + (delayedSample * echoIntensity).toInt()

            // Store current sample for future echo
            echoBuffer[echoBufferIndex] = buffer[i]

            // Clamp to prevent overflow
            buffer[i] = mixedSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            // Advance circular buffer
            echoBufferIndex = (echoBufferIndex + 1) % delaySamples
        }
    }

    /**
     * Apply reverb effect (multiple short delays)
     */
    private fun applyReverb(buffer: ShortArray, size: Int) {
        for (i in 0 until size) {
            var reverbSum = 0

            // Sum all reverb taps
            for (tap in reverbDelays.indices) {
                val delaySamples = (SAMPLE_RATE * reverbDelays[tap]) / 1000
                val delayedSample = reverbBuffers[tap][reverbIndices[tap]]
                reverbSum += delayedSample

                // Store current sample
                reverbBuffers[tap][reverbIndices[tap]] = buffer[i]

                // Advance circular buffer
                reverbIndices[tap] = (reverbIndices[tap] + 1) % delaySamples
            }

            // Mix reverb with original signal
            val reverbAvg = reverbSum / reverbDelays.size
            val mixedSample = buffer[i] + (reverbAvg * reverbIntensity).toInt()

            buffer[i] = mixedSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /**
     * Set echo intensity (0.0 - 100.0)
     */
    fun setEchoIntensity(intensity: Float) {
        echoIntensity = (intensity / 100f).coerceIn(0f, 1f)
    }

    /**
     * Set reverb intensity (0.0 - 100.0)
     */
    fun setReverbIntensity(intensity: Float) {
        reverbIntensity = (intensity / 100f).coerceIn(0f, 1f)
    }

    /**
     * Reset all effect buffers
     */
    fun reset() {
        echoBuffer.fill(0)
        echoBufferIndex = 0

        reverbBuffers.forEach { it.fill(0) }
        reverbIndices.fill(0)
    }

    /**
     * Release resources
     */
    fun release() {
        reset()
    }
}
