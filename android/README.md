# Karafon Android

Android приложение "Карафон" на Kotlin + Jetpack Compose.

## 🛠️ Требования

- Android Studio Giraffe (2023.2.1)+
- Android SDK 34
- Kotlin 1.9.20+
- Минимальная версия Android: 8.0 (API 26)

## 📦 Структура проекта

```
app/src/main/
├── kotlin/com/karafon/app/
│   ├── MainActivity.kt              # Точка входа приложения
│   ├── core/
│   │   └── audio/
│   │       ├── AudioEngine.kt       # Аудио движок (AudioRecord/AudioTrack)
│   │       └── AudioEffects.kt      # Звуковые эффекты
│   └── ui/
│       ├── screens/
│       │   ├── MainScreen.kt        # Главный экран
│       │   ├── SettingsScreen.kt    # Настройки
│       │   └── EffectsScreen.kt     # Экран эффектов
│       └── theme/
│           ├── Theme.kt             # Тема Material 3
│           └── Type.kt              # Типография
├── res/
│   └── values/
│       ├── strings.xml              # Строковые ресурсы
│       └── themes.xml               # Темы приложения
└── AndroidManifest.xml              # Манифест с разрешениями
```

## 🎯 Ключевые компоненты

### AudioEngine
Основной класс для работы с аудио:
- Захват звука с микрофона (AudioRecord)
- Обработка в реальном времени с минимальной задержкой
- Применение звуковых эффектов (AudioEffects)
- Вывод на устройство (AudioTrack)
- Поддержка Bluetooth, проводных наушников, динамиков

### AudioEffects
Процессор звуковых эффектов:
- **Эхо/Delay**: настраиваемая задержка звука
- **Реверберация**: множественные короткие задержки для эффекта зала
- Circular buffer для эффективной обработки

### UI компоненты
**MainScreen** - главный экран приложения:
- Кнопка "Петь/Стоп"
- Индикатор уровня микрофона (20 полос)
- Ползунки громкости микрофона и музыки
- Кнопки доступа к настройкам и эффектам

**SettingsScreen** - экран настроек:
- Информация о подключенном аудиоустройстве
- Режим дуэта "Спой вместе"
- Параметры аудио
- О приложении

**EffectsScreen** - управление эффектами:
- Главный переключатель эффектов
- Ползунки реверберации и эха
- Пресеты: Чисто, Зал, Стадион, Космос

## 🚀 Сборка и запуск

### Через Android Studio:
1. Откройте проект в Android Studio
2. Синхронизируйте Gradle (`File > Sync Project with Gradle Files`)
3. Подключите устройство или запустите эмулятор
4. Нажмите `Run` (▶️)

### Через командную строку:
```bash
# Сборка debug версии
./gradlew assembleDebug

# Установка на подключенное устройство
./gradlew installDebug

# Сборка release версии
./gradlew assembleRelease
```

## ⚙️ Конфигурация

### Разрешения (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### Настройки аудио (AudioEngine.kt)
```kotlin
private const val SAMPLE_RATE = 48000  // 48 kHz
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
private const val BUFFER_SIZE_MULTIPLIER = 1  // Минимальная задержка
```

## 🎤 Аудио архитектура

```
Микрофон (AudioRecord)
    ↓
Буферизация (ShortArray)
    ↓
Применение громкости
    ↓
Звуковые эффекты (AudioEffects)
    ├── Эхо (Delay)
    └── Реверберация (Multiple delays)
    ↓
Вывод (AudioTrack)
    ↓
Устройство (Bluetooth/Speaker/Headphones)
```

## 📊 Производительность

- **Частота дискретизации**: 48 kHz
- **Формат**: PCM 16-bit
- **Задержка**: ~5-10 мс (без эффектов)
- **Задержка с эффектами**: ~15-30 мс
- **Потребление**: оптимизировано для работы в фоне

## 🎨 UI/UX особенности

- **Material Design 3**
- **Темная тема** с градиентами
- **Анимации**: плавные переходы и жесты
- **Адаптивность**: поддержка различных размеров экранов
- **Русская локализация**

## 🔧 Зависимости

```gradle
// Jetpack Compose
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'

// ViewModel и State
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'androidx.lifecycle:lifecycle-runtime-compose'

// Корутины
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android'
```

## 🐛 Отладка

### Логи AudioEngine:
```bash
adb logcat | grep AudioEngine
```

### Проверка аудио устройств:
```bash
adb shell dumpsys audio
```

### Мониторинг производительности:
```bash
adb shell top | grep karafon
```

## 📝 Известные ограничения

- Управление громкостью системной музыки требует дополнительной интеграции
- Режим дуэта требует реализации сетевого протокола
- Некоторые Bluetooth устройства могут иметь повышенную задержку

## 🔐 Безопасность

- Запрос разрешений во время выполнения (runtime permissions)
- Нет сохранения персональных данных
- Локальная обработка аудио (без передачи в сеть)

## 📄 Лицензия

Все права защищены © 2025
