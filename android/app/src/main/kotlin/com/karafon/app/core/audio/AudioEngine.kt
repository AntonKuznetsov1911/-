package com.karafon.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Core audio engine for real-time microphone input and output
 * Uses AudioRecord and AudioTrack for low-latency audio processing
 */
class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioEngine"

        // Audio configuration for minimal latency
        private const val SAMPLE_RATE = 48000
        private const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        // Buffer size for low latency (5ms at 48kHz)
        private const val BUFFER_SIZE_MULTIPLIER = 1
    }

    // State flows
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _microphoneVolume = MutableStateFlow(0.8f)
    val microphoneVolume: StateFlow<Float> = _microphoneVolume.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.7f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _currentInputLevel = MutableStateFlow(0f)
    val currentInputLevel: StateFlow<Float> = _currentInputLevel.asStateFlow()

    private val _isEffectsEnabled = MutableStateFlow(false)
    val isEffectsEnabled: StateFlow<Boolean> = _isEffectsEnabled.asStateFlow()

    private val _currentAudioRoute = MutableStateFlow("Unknown")
    val currentAudioRoute: StateFlow<String> = _currentAudioRoute.asStateFlow()

    // Audio components
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Processing
    private var audioJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.IO)

    // Effects
    private val audioEffects = AudioEffects()

    // Buffer
    private var bufferSize: Int = 0

    init {
        calculateBufferSize()
        updateAudioRoute()
    }

    /**
     * Calculate optimal buffer size for low latency
     */
    private fun calculateBufferSize() {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG_IN,
            AUDIO_FORMAT
        )

        bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER

        Log.d(TAG, "Buffer size: $bufferSize samples")
        Log.d(TAG, "Latency: ${(bufferSize.toFloat() / SAMPLE_RATE * 1000)} ms")
    }

    /**
     * Start audio engine and microphone monitoring
     */
    fun start() {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return
        }

        try {
            // Create AudioRecord for microphone input
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT,
                bufferSize
            )

            // Create AudioTrack for output
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG_OUT)
                        .setEncoding(AUDIO_FORMAT)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // Start recording and playback
            audioRecord?.startRecording()
            audioTrack?.play()

            _isRecording.value = true

            // Start audio processing loop
            startAudioProcessing()

            Log.d(TAG, "✅ Audio engine started")
            Log.d(TAG, "📊 Sample rate: $SAMPLE_RATE Hz")
            Log.d(TAG, "⚡ Buffer size: $bufferSize samples")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start audio engine", e)
            stop()
        }
    }

    /**
     * Stop audio engine
     */
    fun stop() {
        if (!_isRecording.value) {
            return
        }

        audioJob?.cancel()
        audioJob = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        _isRecording.value = false
        _currentInputLevel.value = 0f

        Log.d(TAG, "⏹️ Audio engine stopped")
    }

    /**
     * Audio processing loop - reads from microphone and writes to output
     */
    private fun startAudioProcessing() {
        audioJob = audioScope.launch {
            val buffer = ShortArray(bufferSize)
            val outputBuffer = ShortArray(bufferSize)

            while (_isRecording.value) {
                // Read from microphone
                val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: -1

                if (readResult > 0) {
                    // Calculate input level
                    updateInputLevel(buffer, readResult)

                    // Apply volume
                    applyVolume(buffer, outputBuffer, readResult, _microphoneVolume.value)

                    // Apply effects if enabled
                    if (_isEffectsEnabled.value) {
                        audioEffects.applyEffects(outputBuffer, readResult)
                    }

                    // Write to output (speaker/headphones/bluetooth)
                    audioTrack?.write(outputBuffer, 0, readResult)
                }
            }
        }
    }

    /**
     * Calculate and update input level (for visualization)
     */
    private fun updateInputLevel(buffer: ShortArray, size: Int) {
        var sum = 0.0
        for (i in 0 until size) {
            val sample = buffer[i].toFloat() / Short.MAX_VALUE
            sum += sample * sample
        }

        val rms = sqrt(sum / size)
        val db = 20 * log10(rms + 0.0001)
        val normalizedLevel = ((db + 50) / 50).coerceIn(0f, 1f)

        _currentInputLevel.value = normalizedLevel
    }

    /**
     * Apply volume gain to audio buffer
     */
    private fun applyVolume(input: ShortArray, output: ShortArray, size: Int, volume: Float) {
        for (i in 0 until size) {
            val sample = input[i] * volume
            output[i] = sample.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
        }
    }

    /**
     * Set microphone volume (0.0 - 1.0)
     */
    fun setMicrophoneVolume(volume: Float) {
        _microphoneVolume.value = volume.coerceIn(0f, 1f)
    }

    /**
     * Set music volume (0.0 - 1.0)
     */
    fun setMusicVolume(volume: Float) {
        _musicVolume.value = volume.coerceIn(0f, 1f)
        // Note: Controlling system-wide music volume requires additional implementation
    }

    /**
     * Toggle audio effects
     */
    fun toggleEffects(enabled: Boolean) {
        _isEffectsEnabled.value = enabled
        if (!enabled) {
            audioEffects.reset()
        }
    }

    /**
     * Set reverb intensity (0.0 - 100.0)
     */
    fun setReverbIntensity(intensity: Float) {
        audioEffects.setReverbIntensity(intensity)
    }

    /**
     * Set echo intensity (0.0 - 100.0)
     */
    fun setEchoIntensity(intensity: Float) {
        audioEffects.setEchoIntensity(intensity)
    }

    /**
     * Update current audio route (speakers, headphones, bluetooth, etc.)
     */
    private fun updateAudioRoute() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val route = when {
            devices.any { it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP } -> "Bluetooth"
            devices.any { it.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES } -> "Headphones"
            devices.any { it.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET } -> "Headset"
            else -> "Speaker"
        }
        _currentAudioRoute.value = route
    }

    /**
     * Check if bluetooth device is connected
     */
    fun isBluetoothConnected(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any {
            it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
    }

    /**
     * Cleanup resources
     */
    fun release() {
        stop()
        audioEffects.release()
    }
}
