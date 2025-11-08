package com.karafon.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.karafon.app.core.audio.AudioEngine

data class EffectPreset(
    val name: String,
    val icon: ImageVector,
    val reverbIntensity: Float,
    val echoIntensity: Float
)

@Composable
fun EffectsScreen(
    audioEngine: AudioEngine,
    onDismiss: () -> Unit
) {
    val isEffectsEnabled by audioEngine.isEffectsEnabled.collectAsState()
    var reverbIntensity by remember { mutableStateOf(30f) }
    var echoIntensity by remember { mutableStateOf(25f) }

    val presets = listOf(
        EffectPreset("Чисто", Icons.Default.Clear, 0f, 0f),
        EffectPreset("Зал", Icons.Default.Home, 50f, 20f),
        EffectPreset("Стадион", Icons.Default.Place, 80f, 40f),
        EffectPreset("Космос", Icons.Default.Star, 100f, 60f)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A1E),
                            Color(0xFF16213E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Эффекты",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color(0xFFE91E63)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Master Toggle
                    EffectsToggle(
                        isEnabled = isEffectsEnabled,
                        onClick = {
                            audioEngine.toggleEffects(!isEffectsEnabled)
                        }
                    )

                    // Effects Controls
                    AnimatedVisibility(
                        visible = isEffectsEnabled,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Reverb Control
                            EffectControl(
                                title = "🎪 Реверберация",
                                description = "Эффект зала и пространства",
                                value = reverbIntensity,
                                onValueChange = {
                                    reverbIntensity = it
                                    audioEngine.setReverbIntensity(it)
                                }
                            )

                            // Echo Control
                            EffectControl(
                                title = "🔁 Эхо",
                                description = "Задержка и повторение звука",
                                value = echoIntensity,
                                onValueChange = {
                                    echoIntensity = it
                                    audioEngine.setEchoIntensity(it)
                                }
                            )

                            // Presets
                            EffectPresets(
                                presets = presets,
                                onPresetSelect = { preset ->
                                    reverbIntensity = preset.reverbIntensity
                                    echoIntensity = preset.echoIntensity
                                    audioEngine.setReverbIntensity(preset.reverbIntensity)
                                    audioEngine.setEchoIntensity(preset.echoIntensity)
                                }
                            )
                        }
                    }

                    // Info Section
                    InfoCard()
                }
            }
        }
    }
}

@Composable
fun EffectsToggle(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Звуковые эффекты",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEnabled) "Включено" else "Выключено",
                color = if (isEnabled) Color(0xFF4CAF50) else Color.Gray,
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isEnabled) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.Done else Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun EffectControl(
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${value.toInt()}%",
                color = Color(0xFFE91E63),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE91E63),
                activeTrackColor = Color(0xFFE91E63),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun EffectPresets(
    presets: List<EffectPreset>,
    onPresetSelect: (EffectPreset) -> Unit
) {
    Column {
        Text(
            text = "🎭 Пресеты",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presets) { preset ->
                PresetCard(
                    preset = preset,
                    onClick = { onPresetSelect(preset) }
                )
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: EffectPreset,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE91E63).copy(alpha = 0.6f),
                            Color(0xFF9C27B0).copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.name,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = preset.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF2196F3).copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )

        Column {
            Text(
                text = "Совет",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Эффекты могут увеличить задержку звука на 10-20 мс. Для минимальной задержки рекомендуем отключить эффекты.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
