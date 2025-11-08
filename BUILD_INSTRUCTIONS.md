# Инструкции по сборке Карафон

## iOS

### Требования
- macOS 13.0+
- Xcode 14.0+
- iOS 13.0+ device или simulator
- Apple Developer Account (для установки на физическое устройство)

### Сборка и запуск

#### Способ 1: Через Xcode (рекомендуется)

1. Откройте терминал и перейдите в директорию iOS:
```bash
cd ios
```

2. Откройте проект в Xcode:
```bash
open Karafon.xcodeproj
```

3. В Xcode:
   - Выберите целевое устройство (симулятор или физическое устройство)
   - Нажмите кнопку Run (⌘R) или Product → Run

#### Способ 2: Через командную строку

```bash
cd ios

# Для симулятора
xcodebuild -project Karafon.xcodeproj \
  -scheme Karafon \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 14' \
  build

# Для физического устройства
xcodebuild -project Karafon.xcodeproj \
  -scheme Karafon \
  -sdk iphoneos \
  -configuration Release \
  build
```

### Важные замечания для iOS

1. **Разрешения**: Приложение запрашивает доступ к микрофону и Bluetooth при первом запуске

2. **Тестирование микрофона**: Микрофон не работает на симуляторе. Для полноценного тестирования используйте физическое устройство

3. **Code Signing**: Для установки на физическое устройство необходимо:
   - Открыть проект в Xcode
   - Выбрать `Karafon` target
   - Перейти в `Signing & Capabilities`
   - Выбрать свою команду разработчика

4. **Задержка звука**: Для минимальной задержки рекомендуется использовать проводные наушники или AirPods

---

## Android

### Требования
- Android Studio Giraffe (2023.2.1)+
- Android SDK 34
- JDK 17+
- Android устройство с Android 8.0+ (API 26+) или эмулятор

### Сборка и запуск

#### Способ 1: Через Android Studio (рекомендуется)

1. Откройте Android Studio

2. Выберите "Open" и откройте папку `android/`

3. Дождитесь синхронизации Gradle (первый раз может занять несколько минут)

4. Подключите Android устройство или запустите эмулятор

5. Нажмите кнопку Run (▶️) или используйте `Run → Run 'app'`

#### Способ 2: Через командную строку

```bash
cd android

# Сборка debug версии
./gradlew assembleDebug

# Установка на подключенное устройство
./gradlew installDebug

# Запуск на устройстве
adb shell am start -n com.karafon.app/.MainActivity

# Сборка release версии
./gradlew assembleRelease
```

### Важные замечания для Android

1. **Разрешения**: Приложение автоматически запрашивает:
   - `RECORD_AUDIO` - доступ к микрофону
   - `BLUETOOTH_CONNECT` (Android 12+) - подключение к Bluetooth устройствам
   - `MODIFY_AUDIO_SETTINGS` - изменение настроек аудио

2. **Тестирование**: Эмулятор поддерживает микрофон, но для лучшего опыта используйте физическое устройство

3. **Bluetooth**: На эмуляторе Bluetooth может работать некорректно. Для тестирования Bluetooth функций используйте физическое устройство

4. **Производительность**: Для минимальной задержки:
   - Закройте другие приложения
   - Используйте Bluetooth 5.0+ устройства
   - Отключите звуковые эффекты в настройках

---

## Сборка обеих платформ

### Полная сборка проекта

```bash
# Из корневой директории проекта

# iOS
cd ios && xcodebuild -project Karafon.xcodeproj -scheme Karafon -sdk iphonesimulator build && cd ..

# Android
cd android && ./gradlew assembleDebug && cd ..
```

---

## Решение проблем

### iOS

**Проблема**: `Error: Command PhaseScriptExecution failed`
- Решение: Очистите build folder: Product → Clean Build Folder (⌘⇧K)

**Проблема**: `Signing for "Karafon" requires a development team`
- Решение: Выберите свою команду в настройках Signing & Capabilities

**Проблема**: Микрофон не работает на симуляторе
- Это ожидаемое поведение. Используйте физическое устройство

### Android

**Проблема**: `INSTALL_FAILED_INSUFFICIENT_STORAGE`
- Решение: Освободите место на устройстве или увеличьте размер эмулятора

**Проблема**: Gradle sync failed
- Решение: `File → Invalidate Caches / Restart`

**Проблема**: Permission denied (Microphone)
- Решение: Проверьте настройки приложения в системных настройках

---

## Производственная сборка

### iOS (App Store)

1. В Xcode выберите схему "Release"
2. Product → Archive
3. В Organizer выберите архив и нажмите "Distribute App"
4. Следуйте инструкциям для загрузки в App Store Connect

### Android (Google Play)

1. Создайте keystore для подписи:
```bash
keytool -genkey -v -keystore karafon.keystore -alias karafon -keyalg RSA -keysize 2048 -validity 10000
```

2. Обновите `android/app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("path/to/karafon.keystore")
            storePassword "your-password"
            keyAlias "karafon"
            keyPassword "your-password"
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

3. Соберите release AAB:
```bash
./gradlew bundleRelease
```

4. Загрузите `app/build/outputs/bundle/release/app-release.aab` в Google Play Console

---

## Дополнительная информация

### Требуемые разрешения

**iOS (Info.plist)**:
- `NSMicrophoneUsageDescription`
- `NSBluetoothAlwaysUsageDescription`
- `NSLocalNetworkUsageDescription`

**Android (AndroidManifest.xml)**:
- `RECORD_AUDIO`
- `BLUETOOTH_CONNECT`
- `MODIFY_AUDIO_SETTINGS`

### Зависимости

Все зависимости управляются автоматически:
- iOS: Xcode автоматически связывает системные фреймворки
- Android: Gradle автоматически скачивает зависимости

### Тестирование

Запустите тесты:

```bash
# iOS
cd ios && xcodebuild test -project Karafon.xcodeproj -scheme Karafon -destination 'platform=iOS Simulator,name=iPhone 14'

# Android
cd android && ./gradlew test
```

---

## Контакты поддержки

Если возникли проблемы:
1. Проверьте логи (Xcode Console / Android Logcat)
2. Убедитесь, что все разрешения предоставлены
3. Проверьте, что устройство поддерживает минимальные требования
4. Создайте issue в репозитории с детальным описанием проблемы

---

**Успешной сборки! 🎤**
