# Karafon iOS

iOS приложение "Карафон" на Swift + SwiftUI.

## 🛠️ Требования

- Xcode 14.0+
- iOS 13.0+
- Swift 5.9+

## 📦 Структура проекта

```
Karafon/
├── App/
│   └── KarafonApp.swift         # Точка входа приложения
├── Core/
│   └── Audio/
│       └── AudioEngine.swift    # Аудио движок (AVAudioEngine)
├── Features/
│   ├── Main/
│   │   └── MainView.swift       # Главный экран
│   ├── Settings/
│   │   └── SettingsView.swift   # Настройки
│   └── Effects/
│       └── EffectsView.swift    # Экран эффектов
└── Info.plist                   # Конфигурация приложения
```

## 🎯 Ключевые компоненты

### AudioEngine
Основной класс для работы с аудио:
- Захват звука с микрофона (AVAudioInputNode)
- Применение эффектов (реверберация, эхо)
- Вывод звука на устройство с минимальной задержкой (<100мс)
- Поддержка Bluetooth, AUX, встроенных динамиков

### Главный экран (MainView)
- Кнопка "Петь/Стоп"
- Индикатор уровня микрофона
- Ползунки громкости микрофона и музыки
- Кнопки настроек и эффектов

### Настройки (SettingsView)
- Выбор аудиоустройства
- Настройка темы оформления
- Режим дуэта "Спой вместе"
- Параметры аудио (частота дискретизации, размер буфера)

### Эффекты (EffectsView)
- Реверберация (эффект зала)
- Эхо (задержка звука)
- Пресеты эффектов (Чисто, Зал, Стадион, Космос)

## 🚀 Сборка и запуск

### Через Xcode:
1. Откройте `Karafon.xcodeproj` в Xcode
2. Выберите целевое устройство (симулятор или физическое устройство)
3. Нажмите ⌘R для запуска

### Через командную строку:
```bash
# Сборка для симулятора
xcodebuild -project Karafon.xcodeproj -scheme Karafon -sdk iphonesimulator

# Сборка для устройства
xcodebuild -project Karafon.xcodeproj -scheme Karafon -sdk iphoneos
```

## ⚙️ Конфигурация

### Разрешения (Info.plist)
- `NSMicrophoneUsageDescription` - доступ к микрофону
- `NSBluetoothAlwaysUsageDescription` - Bluetooth подключения
- `NSLocalNetworkUsageDescription` - локальная сеть для дуэта
- `UIBackgroundModes: audio` - работа в фоновом режиме

### Настройки аудио
- Частота дискретизации: 48 kHz
- Размер буфера: 5 ms (минимальная задержка)
- Формат: Float32, моно/стерео

## 🎤 Аудио архитектура

```
Микрофон (AVAudioInputNode)
    ↓
EQ эффект
    ↓
Delay (эхо)
    ↓
Reverb (реверберация)
    ↓
Main Mixer (AVAudioMixerNode)
    ↓
Выход (Bluetooth/Speaker)
```

## 📝 Заметки разработчика

### Оптимизация задержки:
1. Используется минимальный размер буфера (5ms)
2. AVAudioSession настроена на низкую задержку
3. Эффекты можно отключить для дальнейшего снижения latency

### Известные ограничения:
- На симуляторе микрофон может работать некорректно (требуется физическое устройство)
- Управление громкостью системной музыки требует дополнительной интеграции с медиаплеерами
- Режим дуэта требует реализации сетевого протокола синхронизации

## 🐛 Отладка

Для отладки аудио используйте:
```swift
print("Sample rate: \(audioSession.sampleRate)")
print("IO buffer duration: \(audioSession.ioBufferDuration * 1000) ms")
print("Current route: \(audioSession.currentRoute)")
```

## 📄 Лицензия

Все права защищены © 2025
