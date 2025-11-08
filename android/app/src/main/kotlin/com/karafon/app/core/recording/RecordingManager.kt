package com.karafon.app.core.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages audio recording functionality
 */
class RecordingManager(private val context: Context) {

    companion object {
        private const val TAG = "RecordingManager"
    }

    // State
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _recordings = MutableStateFlow<List<Recording>>(emptyList())
    val recordings: StateFlow<List<Recording>> = _recordings.asStateFlow()

    // Recording
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingJob: Job? = null
    private val recordingScope = CoroutineScope(Dispatchers.Main)

    init {
        loadRecordings()
    }

    /**
     * Start recording
     */
    fun startRecording() {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return
        }

        try {
            // Create output file
            val recordingsDir = File(context.filesDir, "recordings")
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val filename = "Karafon_${dateFormat.format(Date())}.m4a"
            currentRecordingFile = File(recordingsDir, filename)

            // Setup MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(48000)
                setAudioEncodingBitRate(192000)
                setOutputFile(currentRecordingFile?.absolutePath)

                prepare()
                start()
            }

            _isRecording.value = true
            _recordingDuration.value = 0

            // Start duration timer
            recordingJob = recordingScope.launch {
                while (_isRecording.value) {
                    delay(100)
                    _recordingDuration.value += 100
                }
            }

            Log.d(TAG, "🔴 Recording started: $filename")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
            stopRecording()
        }
    }

    /**
     * Stop recording
     */
    fun stopRecording() {
        if (!_isRecording.value) {
            return
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            recordingJob?.cancel()
            recordingJob = null

            _isRecording.value = false

            // Add to recordings list
            currentRecordingFile?.let { file ->
                if (file.exists()) {
                    val recording = Recording(
                        file = file,
                        date = Date(),
                        duration = _recordingDuration.value
                    )
                    _recordings.value = listOf(recording) + _recordings.value
                }
            }

            currentRecordingFile = null
            _recordingDuration.value = 0

            Log.d(TAG, "⏹️ Recording stopped")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to stop recording", e)
        }
    }

    /**
     * Delete recording
     */
    fun deleteRecording(recording: Recording) {
        try {
            recording.file.delete()
            _recordings.value = _recordings.value.filter { it.id != recording.id }
            Log.d(TAG, "🗑️ Deleted recording: ${recording.title}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete recording", e)
        }
    }

    /**
     * Load existing recordings from storage
     */
    private fun loadRecordings() {
        try {
            val recordingsDir = File(context.filesDir, "recordings")
            if (!recordingsDir.exists()) {
                return
            }

            val files = recordingsDir.listFiles { file ->
                file.extension == "m4a"
            } ?: emptyArray()

            _recordings.value = files.map { file ->
                Recording(
                    file = file,
                    date = Date(file.lastModified()),
                    duration = 0 // Would need MediaMetadataRetriever to get actual duration
                )
            }.sortedByDescending { it.date }

            Log.d(TAG, "Loaded ${files.size} recordings")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load recordings", e)
        }
    }

    /**
     * Release resources
     */
    fun release() {
        if (_isRecording.value) {
            stopRecording()
        }
    }
}

/**
 * Recording data class
 */
data class Recording(
    val id: String = UUID.randomUUID().toString(),
    val file: File,
    val date: Date,
    val duration: Long // in milliseconds
) {
    val title: String
        get() = file.nameWithoutExtension

    val formattedDate: String
        get() {
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return format.format(date)
        }

    val formattedDuration: String
        get() {
            val seconds = (duration / 1000).toInt()
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }
}
