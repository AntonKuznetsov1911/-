# API документация Карафон

## AudioEngine API

### iOS (Swift)

#### Инициализация
```swift
let audioEngine = AudioEngine()
```

#### Методы

##### start()
Запускает аудио движок и начинает захват микрофона.
```swift
func start()
```

**Пример:**
```swift
audioEngine.start()
```

##### stop()
Останавливает аудио движок.
```swift
func stop()
```

**Пример:**
```swift
audioEngine.stop()
```

##### setMicrophoneVolume(_ volume: Float)
Устанавливает громкость микрофона.
```swift
func setMicrophoneVolume(_ volume: Float)
```

**Параметры:**
- `volume`: Float от 0.0 до 1.0

**Пример:**
```swift
audioEngine.setMicrophoneVolume(0.8)
```

##### setMusicVolume(_ volume: Float)
Устанавливает громкость музыки (плейсхолдер).
```swift
func setMusicVolume(_ volume: Float)
```

**Параметры:**
- `volume`: Float от 0.0 до 1.0

##### toggleEffects(_ enabled: Bool)
Включает/выключает звуковые эффекты.
```swift
func toggleEffects(_ enabled: Bool)
```

**Пример:**
```swift
audioEngine.toggleEffects(true)
```

##### setReverbIntensity(_ intensity: Float)
Устанавливает интенсивность реверберации.
```swift
func setReverbIntensity(_ intensity: Float)
```

**Параметры:**
- `intensity`: Float от 0.0 до 100.0

##### setEchoIntensity(_ intensity: Float)
Устанавливает интенсивность эха.
```swift
func setEchoIntensity(_ intensity: Float)
```

**Параметры:**
- `intensity`: Float от 0.0 до 100.0

#### Свойства

##### @Published isRecording: Bool
Статус записи (активен ли микрофон).

##### @Published microphoneVolume: Float
Текущая громкость микрофона (0.0 - 1.0).

##### @Published musicVolume: Float
Текущая громкость музыки (0.0 - 1.0).

##### @Published currentInputLevel: Float
Текущий уровень входного сигнала (0.0 - 1.0).

##### @Published isEffectsEnabled: Bool
Статус звуковых эффектов.

##### currentAudioRoute: String
Текущее аудиоустройство (Speaker, Bluetooth, Headphones).

##### isBluetoothConnected: Bool
Подключено ли Bluetooth устройство.

---

### Android (Kotlin)

#### Инициализация
```kotlin
val audioEngine = AudioEngine(context)
```

#### Методы

##### start()
Запускает аудио движок.
```kotlin
fun start()
```

**Пример:**
```kotlin
audioEngine.start()
```

##### stop()
Останавливает аудио движок.
```kotlin
fun stop()
```

##### setMicrophoneVolume(volume: Float)
Устанавливает громкость микрофона.
```kotlin
fun setMicrophoneVolume(volume: Float)
```

**Параметры:**
- `volume`: Float от 0.0 до 1.0

**Пример:**
```kotlin
audioEngine.setMicrophoneVolume(0.8f)
```

##### setMusicVolume(volume: Float)
Устанавливает громкость музыки (плейсхолдер).
```kotlin
fun setMusicVolume(volume: Float)
```

##### toggleEffects(enabled: Boolean)
Включает/выключает эффекты.
```kotlin
fun toggleEffects(enabled: Boolean)
```

**Пример:**
```kotlin
audioEngine.toggleEffects(true)
```

##### setReverbIntensity(intensity: Float)
Устанавливает интенсивность реверберации.
```kotlin
fun setReverbIntensity(intensity: Float)
```

**Параметры:**
- `intensity`: Float от 0.0 до 100.0

##### setEchoIntensity(intensity: Float)
Устанавливает интенсивность эха.
```kotlin
fun setEchoIntensity(intensity: Float)
```

##### release()
Освобождает ресурсы.
```kotlin
fun release()
```

**Пример:**
```kotlin
override fun onDestroy() {
    audioEngine.release()
}
```

#### Свойства (StateFlow)

##### isRecording: StateFlow<Boolean>
Статус записи.

**Использование в Compose:**
```kotlin
val isRecording by audioEngine.isRecording.collectAsState()
```

##### microphoneVolume: StateFlow<Float>
Громкость микрофона (0.0f - 1.0f).

##### musicVolume: StateFlow<Float>
Громкость музыки (0.0f - 1.0f).

##### currentInputLevel: StateFlow<Float>
Уровень входного сигнала (0.0f - 1.0f).

##### isEffectsEnabled: StateFlow<Boolean>
Статус эффектов.

##### currentAudioRoute: StateFlow<String>
Текущее аудиоустройство.

#### Методы проверки

##### isBluetoothConnected(): Boolean
Проверяет подключение Bluetooth.
```kotlin
fun isBluetoothConnected(): Boolean
```

---

## AudioEffects API (Android)

### Методы

##### applyEffects(buffer: ShortArray, size: Int)
Применяет все активные эффекты к буферу.
```kotlin
fun applyEffects(buffer: ShortArray, size: Int)
```

##### setEchoIntensity(intensity: Float)
Устанавливает интенсивность эха.
```kotlin
fun setEchoIntensity(intensity: Float)
```

**Параметры:**
- `intensity`: Float от 0.0 до 100.0

##### setReverbIntensity(intensity: Float)
Устанавливает интенсивность реверберации.
```kotlin
fun setReverbIntensity(intensity: Float)
```

##### reset()
Сбрасывает все буферы эффектов.
```kotlin
fun reset()
```

##### release()
Освобождает ресурсы.
```kotlin
fun release()
```

---

## События и колбэки

### iOS
Использует Combine framework для реактивности:
```swift
audioEngine.$isRecording
    .sink { isRecording in
        print("Recording status: \(isRecording)")
    }
```

### Android
Использует Kotlin Flow:
```kotlin
lifecycleScope.launch {
    audioEngine.isRecording.collect { isRecording ->
        println("Recording status: $isRecording")
    }
}
```

---

## Константы

### Аудио параметры

#### iOS
```swift
private let sampleRate: Double = 48000.0
private let preferredIOBufferDuration: TimeInterval = 0.005 // 5ms
```

#### Android
```kotlin
private const val SAMPLE_RATE = 48000
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
private const val BUFFER_SIZE_MULTIPLIER = 1
```

---

## Коды ошибок

### iOS
Использует стандартные ошибки AVFoundation:
- `AVAudioSessionErrorCodeCannotStartPlaying`
- `AVAudioSessionErrorCodeCannotStartRecording`

### Android
Использует стандартные коды AudioRecord:
- `ERROR_INVALID_OPERATION`
- `ERROR_BAD_VALUE`
- `ERROR_DEAD_OBJECT`

---

## Примеры использования

### Базовое использование (iOS)
```swift
import SwiftUI

struct ContentView: View {
    @StateObject private var audioEngine = AudioEngine()

    var body: some View {
        VStack {
            Button(audioEngine.isRecording ? "Стоп" : "Петь") {
                if audioEngine.isRecording {
                    audioEngine.stop()
                } else {
                    audioEngine.start()
                }
            }

            Slider(value: $audioEngine.microphoneVolume)
        }
    }
}
```

### Базовое использование (Android)
```kotlin
@Composable
fun MainScreen(audioEngine: AudioEngine) {
    val isRecording by audioEngine.isRecording.collectAsState()
    val micVolume by audioEngine.microphoneVolume.collectAsState()

    Column {
        Button(onClick = {
            if (isRecording) audioEngine.stop()
            else audioEngine.start()
        }) {
            Text(if (isRecording) "Стоп" else "Петь")
        }

        Slider(
            value = micVolume,
            onValueChange = { audioEngine.setMicrophoneVolume(it) }
        )
    }
}
```

---

## Лучшие практики

1. **Всегда останавливайте аудио движок** при уходе с экрана:
   ```swift
   // iOS
   .onDisappear {
       audioEngine.stop()
   }
   ```
   ```kotlin
   // Android
   override fun onPause() {
       audioEngine.stop()
   }
   ```

2. **Запрашивайте разрешения** перед запуском:
   ```swift
   // iOS
   AVAudioSession.sharedInstance().requestRecordPermission { granted in }
   ```
   ```kotlin
   // Android
   requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO))
   ```

3. **Обрабатывайте изменения аудио роута**:
   - Отслеживайте подключение/отключение Bluetooth
   - Обновляйте UI соответственно

4. **Освобождайте ресурсы**:
   ```kotlin
   // Android
   override fun onDestroy() {
       audioEngine.release()
   }
   ```
