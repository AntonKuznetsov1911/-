package com.karafon.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karafon.app.core.audio.AudioEngine
import com.karafon.app.core.recording.RecordingManager

@Composable
fun MainScreen(
    audioEngine: AudioEngine,
    recordingManager: RecordingManager
) {
    val isRecording by audioEngine.isRecording.collectAsState()
    val micVolume by audioEngine.microphoneVolume.collectAsState()
    val musicVolume by audioEngine.musicVolume.collectAsState()
    val inputLevel by audioEngine.currentInputLevel.collectAsState()
    val isRecordingAudio by recordingManager.isRecording.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showEffects by remember { mutableStateOf(false) }
    var showRecordings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Карафон",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Audio level indicator
            AudioLevelIndicator(level = inputLevel)

            Spacer(modifier = Modifier.height(40.dp))

            // Main sing button
            SingButton(
                isRecording = isRecording,
                onClick = {
                    if (isRecording) {
                        audioEngine.stop()
                    } else {
                        audioEngine.start()
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Volume controls
            VolumeSlider(
                title = "🎤 Микрофон",
                value = micVolume,
                onValueChange = { audioEngine.setMicrophoneVolume(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            VolumeSlider(
                title = "🎵 Музыка",
                value = musicVolume,
                onValueChange = { audioEngine.setMusicVolume(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlButton(
                    icon = Icons.Filled.List,
                    title = "Записи",
                    onClick = { showRecordings = true }
                )

                ControlButton(
                    icon = Icons.Filled.Star,
                    title = "Эффекты",
                    onClick = { showEffects = true }
                )

                ControlButton(
                    icon = Icons.Filled.Settings,
                    title = "Настройки",
                    onClick = { showSettings = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Record button in top right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            RecordButton(
                isRecording = isRecordingAudio,
                onClick = {
                    if (isRecordingAudio) {
                        recordingManager.stopRecording()
                    } else {
                        recordingManager.startRecording()
                    }
                }
            )
        }
    }

    // Modals
    if (showSettings) {
        SettingsScreen(
            audioEngine = audioEngine,
            onDismiss = { showSettings = false }
        )
    }

    if (showEffects) {
        EffectsScreen(
            audioEngine = audioEngine,
            onDismiss = { showEffects = false }
        )
    }

    if (showRecordings) {
        RecordingScreen(
            recordingManager = recordingManager,
            onDismiss = { showRecordings = false }
        )
    }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isRecording) Color.Red else Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun SingButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = if (isRecording) {
                        listOf(Color(0xFFE53935), Color(0xFFD32F2F))
                    } else {
                        listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                    }
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Close else Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRecording) "Стоп" else "Петь",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VolumeSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(value * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE91E63),
                activeTrackColor = Color(0xFFE91E63),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AudioLevelIndicator(level: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(20) { index ->
            val threshold = index / 20f
            val isActive = level > threshold

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            !isActive -> Color.White.copy(alpha = 0.2f)
                            index < 12 -> Color(0xFF4CAF50)
                            index < 16 -> Color(0xFFFFEB3B)
                            else -> Color(0xFFF44336)
                        }
                    )
            )
        }
    }
}

@Composable
fun ControlButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
