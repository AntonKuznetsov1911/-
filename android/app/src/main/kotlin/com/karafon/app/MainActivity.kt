package com.karafon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.karafon.app.core.audio.AudioEngine
import com.karafon.app.core.permissions.PermissionManager
import com.karafon.app.core.recording.RecordingManager
import com.karafon.app.ui.screens.MainScreen
import com.karafon.app.ui.theme.KarafonTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    private lateinit var recordingManager: RecordingManager
    private lateinit var permissionManager: PermissionManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted
        } else {
            // Handle permission denial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers
        audioEngine = AudioEngine(this)
        recordingManager = RecordingManager(this)
        permissionManager = PermissionManager(this)

        // Request permissions
        requestPermissions()

        setContent {
            KarafonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KarafonApp(
                        audioEngine = audioEngine,
                        recordingManager = recordingManager
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val missingPermissions = permissionManager.getMissingPermissions()
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop audio when app goes to background
        if (audioEngine.isRecording.value) {
            audioEngine.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
        recordingManager.release()
    }
}

@Composable
fun KarafonApp(
    audioEngine: AudioEngine,
    recordingManager: RecordingManager
) {
    MainScreen(
        audioEngine = audioEngine,
        recordingManager = recordingManager
    )
}
