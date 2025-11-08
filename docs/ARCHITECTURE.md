# Архитектура приложения Карафон

## 🏗️ Общая архитектура

Карафон состоит из двух нативных приложений (iOS и Android), использующих общую архитектурную концепцию, но платформо-специфичную реализацию.

## 📱 iOS (Swift + SwiftUI)

### Структура компонентов

```
┌─────────────────────────────────────────┐
│           SwiftUI Views                 │
│  (MainView, SettingsView, EffectsView)  │
└──────────────┬──────────────────────────┘
               │ @EnvironmentObject
               ↓
┌─────────────────────────────────────────┐
│         AudioEngine (ObservableObject)  │
│  - @Published properties                │
│  - State management                     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│        AVAudioEngine (iOS SDK)          │
│  - AVAudioInputNode (microphone)        │
│  - AVAudioMixerNode                     │
│  - AVAudioUnitReverb                    │
│  - AVAudioUnitDelay                     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│      AVAudioSession (iOS SDK)           │
│  - Audio route management               │
│  - Sample rate: 48 kHz                  │
│  - Buffer: 5 ms                         │
└─────────────────────────────────────────┘
```

### Ключевые классы

#### AudioEngine.swift
```swift
class AudioEngine: ObservableObject {
    // State
    @Published var isRecording: Bool
    @Published var microphoneVolume: Float
    @Published var currentInputLevel: Float

    // Audio components
    private let engine: AVAudioEngine
    private let inputNode: AVAudioInputNode
    private let mainMixer: AVAudioMixerNode

    // Effects
    private let reverbEffect: AVAudioUnitReverb
    private let delayEffect: AVAudioUnitDelay
}
```

#### Поток данных
1. **Вход**: AVAudioInputNode → микрофон устройства
2. **Обработка**: EQ → Delay → Reverb → Mixer
3. **Выход**: Mixer → Аудиоустройство (Bluetooth/Speaker)

## 🤖 Android (Kotlin + Jetpack Compose)

### Структура компонентов

```
┌─────────────────────────────────────────┐
│       Jetpack Compose UI                │
│  (MainScreen, SettingsScreen, etc.)     │
└──────────────┬──────────────────────────┘
               │ StateFlow
               ↓
┌─────────────────────────────────────────┐
│          AudioEngine (Kotlin)           │
│  - StateFlow properties                 │
│  - Coroutines for processing            │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│     AudioRecord / AudioTrack (SDK)      │
│  - AudioRecord: микрофон                │
│  - AudioTrack: вывод звука              │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│        AudioEffects (Kotlin)            │
│  - Reverb (multiple delays)             │
│  - Echo (circular buffer)               │
└─────────────────────────────────────────┘
```

### Ключевые классы

#### AudioEngine.kt
```kotlin
class AudioEngine(context: Context) {
    // State flows
    val isRecording: StateFlow<Boolean>
    val microphoneVolume: StateFlow<Float>
    val currentInputLevel: StateFlow<Float>

    // Audio components
    private var audioRecord: AudioRecord?
    private var audioTrack: AudioTrack?

    // Processing
    private var audioJob: Job?
    private val audioEffects: AudioEffects
}
```

#### AudioEffects.kt
```kotlin
class AudioEffects {
    // Echo parameters
    private val echoBuffer: ShortArray
    private var echoBufferIndex: Int

    // Reverb parameters
    private val reverbDelays: IntArray
    private val reverbBuffers: Array<ShortArray>

    fun applyEffects(buffer: ShortArray, size: Int)
}
```

#### Поток данных
1. **Вход**: AudioRecord → микрофон (PCM 16-bit)
2. **Обработка**:
   - Применение громкости
   - AudioEffects (echo + reverb)
   - Нормализация
3. **Выход**: AudioTrack → устройство

## 🎵 Аудио обработка

### Цепь обработки (iOS)

```
Input → [EQ] → [Delay] → [Reverb] → [Mixer] → Output
         ↓       ↓         ↓          ↓
      bypass  bypass   bypass   volume control
```

### Цепь обработки (Android)

```
Input → [Volume] → [Echo Buffer] → [Reverb Taps] → Output
         ↓              ↓                ↓
      0.0-1.0    circular buffer   4x delays
```

## ⚙️ Параметры аудио

### iOS (AVAudioEngine)
- **Sample Rate**: 48000 Hz
- **Format**: Float32
- **Channels**: Mono/Stereo
- **Buffer**: 5 ms (preferredIOBufferDuration)
- **Latency**: < 100 ms

### Android (AudioRecord/AudioTrack)
- **Sample Rate**: 48000 Hz
- **Format**: PCM 16-bit
- **Channels**: Mono
- **Buffer**: minBufferSize × 1
- **Latency**: ~5-10 ms (без эффектов)

## 🎨 UI архитектура

### iOS (SwiftUI)
```swift
struct MainView: View {
    @EnvironmentObject var audioEngine: AudioEngine

    var body: some View {
        // Reactive UI based on @Published properties
    }
}
```

**Принципы**:
- Declarative UI
- Single source of truth (@EnvironmentObject)
- Automatic updates via Combine

### Android (Jetpack Compose)
```kotlin
@Composable
fun MainScreen(audioEngine: AudioEngine) {
    val isRecording by audioEngine.isRecording.collectAsState()

    // Compose UI that reacts to StateFlow
}
```

**Принципы**:
- Declarative UI
- State hoisting
- Reactive updates via StateFlow

## 🔄 Управление состоянием

### iOS
- **ObservableObject** для AudioEngine
- **@Published** для свойств
- **Combine** для реактивности
- **@EnvironmentObject** для передачи зависимостей

### Android
- **StateFlow** для состояния
- **Coroutines** для асинхронных операций
- **collectAsState()** для интеграции с Compose
- Dependency injection через конструктор

## 🎯 Оптимизация задержки

### Факторы влияющие на latency:

1. **Размер буфера**
   - Меньше = меньше задержка, больше нагрузка на CPU
   - iOS: `preferredIOBufferDuration = 0.005` (5ms)
   - Android: `minBufferSize × 1`

2. **Частота дискретизации**
   - 48 kHz - оптимальный баланс качества и производительности

3. **Эффекты**
   - Reverb добавляет ~10-15 ms
   - Echo добавляет ~5-10 ms
   - Можно отключить для минимальной задержки

4. **Bluetooth**
   - A2DP профиль: +100-200 ms
   - Рекомендация: проводное подключение для минимальной задержки

## 📊 Потоки выполнения

### iOS
- **Main Thread**: UI updates
- **Audio Thread**: AVAudioEngine автоматически
- **Timer**: мониторинг уровня микрофона

### Android
- **Main Thread**: UI (Compose)
- **IO Dispatcher**: аудио обработка (Coroutines)
- **AudioRecord/AudioTrack**: нативные потоки

## 🔐 Безопасность и разрешения

### iOS (Info.plist)
```xml
<key>NSMicrophoneUsageDescription</key>
<string>Для караоке нужен доступ к микрофону</string>

<key>NSBluetoothAlwaysUsageDescription</key>
<string>Для подключения к Bluetooth-колонкам</string>
```

### Android (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

**Runtime permissions**:
- Запрашиваются при первом запуске
- Graceful handling при отказе

## 🧪 Тестирование

### Unit Tests
- AudioEffects алгоритмы
- State management
- Бизнес-логика

### Integration Tests
- AudioEngine initialization
- Effects processing
- Volume controls

### UI Tests
- Навигация
- Взаимодействие с кнопками
- Проверка состояний

## 📈 Метрики производительности

### Целевые показатели:
- ✅ Latency < 100 ms (без эффектов: < 20 ms)
- ✅ CPU usage < 15%
- ✅ Memory < 50 MB
- ✅ No audio glitches
- ✅ Stable FPS (60)

## 🔮 Будущие улучшения

1. **Режим дуэта**
   - P2P соединение (Wi-Fi Direct / Bluetooth)
   - Синхронизация аудио потоков
   - Протокол обмена данными

2. **Запись**
   - AVAudioRecorder (iOS) / MediaRecorder (Android)
   - Микс голоса + музыки
   - Экспорт в MP4/MP3

3. **Дополнительные эффекты**
   - Pitch correction (автотюн)
   - Компрессор
   - Noise gate

4. **Оптимизация**
   - AAudio на Android (вместо AudioRecord/AudioTrack)
   - Metal/OpenCL для DSP
   - SIMD оптимизации
