//
//  SettingsView.swift
//  Karafon
//
//  Settings screen for device selection and app configuration
//

import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var audioEngine: AudioEngine

    @State private var selectedTheme = "dark"
    @State private var duetModeEnabled = false

    var body: some View {
        NavigationView {
            ZStack {
                Color(red: 0.05, green: 0.05, blue: 0.1)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 30) {

                        // Audio Device Section
                        SettingsSection(title: "🔊 Аудиоустройство") {
                            AudioDeviceRow(
                                deviceName: audioEngine.currentAudioRoute,
                                isConnected: true
                            )

                            if audioEngine.isBluetoothConnected {
                                InfoRow(
                                    icon: "checkmark.circle.fill",
                                    text: "Bluetooth подключен",
                                    color: .green
                                )
                            }
                        }

                        // Theme Section
                        SettingsSection(title: "🎨 Внешний вид") {
                            Picker("Тема", selection: $selectedTheme) {
                                Text("Светлая").tag("light")
                                Text("Тёмная").tag("dark")
                                Text("Системная").tag("system")
                            }
                            .pickerStyle(.segmented)
                        }

                        // Duet Mode Section
                        SettingsSection(title: "👥 Режим дуэта") {
                            Toggle("Спой вместе", isOn: $duetModeEnabled)
                                .tint(.pink)

                            if duetModeEnabled {
                                Text("Подключите второй телефон через Bluetooth для пения вдвоём")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                        }

                        // Audio Settings Section
                        SettingsSection(title: "🎧 Настройки звука") {
                            VStack(alignment: .leading, spacing: 16) {
                                HStack {
                                    Text("Частота дискретизации")
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("48 kHz")
                                        .foregroundColor(.gray)
                                }

                                HStack {
                                    Text("Размер буфера")
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("5 ms")
                                        .foregroundColor(.gray)
                                }

                                Toggle("Низкая задержка", isOn: .constant(true))
                                    .tint(.pink)
                                    .disabled(true)
                            }
                        }

                        // About Section
                        SettingsSection(title: "ℹ️ О приложении") {
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Text("Версия")
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("1.0.0 (MVP)")
                                        .foregroundColor(.gray)
                                }

                                HStack {
                                    Text("Сборка")
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("1")
                                        .foregroundColor(.gray)
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Настройки")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Готово") {
                        dismiss()
                    }
                    .foregroundColor(.pink)
                }
            }
        }
    }
}

// MARK: - Settings Section

struct SettingsSection<Content: View>: View {
    let title: String
    let content: Content

    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .foregroundColor(.white)

            VStack(spacing: 12) {
                content
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.white.opacity(0.1))
            )
        }
    }
}

// MARK: - Audio Device Row

struct AudioDeviceRow: View {
    let deviceName: String
    let isConnected: Bool

    var body: some View {
        HStack {
            Image(systemName: "speaker.wave.2.fill")
                .foregroundColor(.pink)

            VStack(alignment: .leading) {
                Text(deviceName)
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .medium))

                Text(isConnected ? "Подключено" : "Не подключено")
                    .font(.caption)
                    .foregroundColor(isConnected ? .green : .gray)
            }

            Spacer()
        }
    }
}

// MARK: - Info Row

struct InfoRow: View {
    let icon: String
    let text: String
    let color: Color

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
            Text(text)
                .foregroundColor(.white)
                .font(.system(size: 14))
            Spacer()
        }
    }
}

// MARK: - Preview

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
            .environmentObject(AudioEngine())
    }
}
