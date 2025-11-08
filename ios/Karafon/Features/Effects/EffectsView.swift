//
//  EffectsView.swift
//  Karafon
//
//  Audio effects control screen (reverb, echo, etc.)
//

import SwiftUI

struct EffectsView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var audioEngine: AudioEngine

    @State private var reverbIntensity: Float = 30.0
    @State private var echoIntensity: Float = 25.0

    var body: some View {
        NavigationView {
            ZStack {
                Color(red: 0.05, green: 0.05, blue: 0.1)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 30) {

                        // Master Effects Toggle
                        EffectsToggle(isEnabled: audioEngine.isEffectsEnabled) {
                            audioEngine.toggleEffects(!audioEngine.isEffectsEnabled)
                        }
                        .padding()

                        // Effects Controls
                        if audioEngine.isEffectsEnabled {
                            VStack(spacing: 25) {

                                // Reverb Control
                                EffectControl(
                                    title: "🎪 Реверберация",
                                    description: "Эффект зала и пространства",
                                    value: $reverbIntensity,
                                    range: 0...100,
                                    onChange: { value in
                                        audioEngine.setReverbIntensity(value)
                                    }
                                )

                                // Echo Control
                                EffectControl(
                                    title: "🔁 Эхо",
                                    description: "Задержка и повторение звука",
                                    value: $echoIntensity,
                                    range: 0...100,
                                    onChange: { value in
                                        audioEngine.setEchoIntensity(value)
                                    }
                                )

                                // Presets
                                EffectPresets(
                                    onPresetSelect: { preset in
                                        applyPreset(preset)
                                    }
                                )
                            }
                            .padding()
                            .transition(.opacity.combined(with: .scale))
                        }

                        // Info Section
                        InfoSection()
                            .padding()
                    }
                }
            }
            .navigationTitle("Эффекты")
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

    private func applyPreset(_ preset: EffectPreset) {
        withAnimation {
            reverbIntensity = preset.reverb
            echoIntensity = preset.echo
            audioEngine.setReverbIntensity(preset.reverb)
            audioEngine.setEchoIntensity(preset.echo)
        }
    }
}

// MARK: - Effects Toggle

struct EffectsToggle: View {
    let isEnabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Звуковые эффекты")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)

                    Text(isEnabled ? "Включено" : "Выключено")
                        .font(.system(size: 14))
                        .foregroundColor(isEnabled ? .green : .gray)
                }

                Spacer()

                ZStack {
                    Circle()
                        .fill(isEnabled ? Color.green : Color.gray.opacity(0.3))
                        .frame(width: 60, height: 60)

                    Image(systemName: isEnabled ? "waveform" : "waveform.slash")
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white.opacity(0.1))
            )
        }
    }
}

// MARK: - Effect Control

struct EffectControl: View {
    let title: String
    let description: String
    @Binding var value: Float
    let range: ClosedRange<Float>
    let onChange: (Float) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)

                    Text(description)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                }

                Spacer()

                Text("\(Int(value))%")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.pink)
                    .frame(width: 50)
            }

            Slider(value: Binding(
                get: { Double(value) },
                set: { newValue in
                    value = Float(newValue)
                    onChange(Float(newValue))
                }
            ), in: Double(range.lowerBound)...Double(range.upperBound))
                .accentColor(.pink)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white.opacity(0.1))
        )
    }
}

// MARK: - Effect Presets

struct EffectPreset {
    let name: String
    let icon: String
    let reverb: Float
    let echo: Float
}

struct EffectPresets: View {
    let onPresetSelect: (EffectPreset) -> Void

    let presets = [
        EffectPreset(name: "Чисто", icon: "speaker.fill", reverb: 0, echo: 0),
        EffectPreset(name: "Зал", icon: "building.columns.fill", reverb: 50, echo: 20),
        EffectPreset(name: "Стадион", icon: "sportscourt.fill", reverb: 80, echo: 40),
        EffectPreset(name: "Космос", icon: "sparkles", reverb: 100, echo: 60)
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("🎭 Пресеты")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(presets, id: \.name) { preset in
                        PresetButton(preset: preset) {
                            onPresetSelect(preset)
                        }
                    }
                }
            }
        }
    }
}

struct PresetButton: View {
    let preset: EffectPreset
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color.pink.opacity(0.6),
                                    Color.purple.opacity(0.6)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 60, height: 60)

                    Image(systemName: preset.icon)
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                }

                Text(preset.name)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
            }
            .frame(width: 80)
        }
    }
}

// MARK: - Info Section

struct InfoSection: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(.blue)
                Text("Совет")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
            }

            Text("Эффекты могут увеличить задержку звука на 10-20 мс. Для минимальной задержки рекомендуем отключить эффекты.")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.blue.opacity(0.1))
        )
    }
}

// MARK: - Preview

struct EffectsView_Previews: PreviewProvider {
    static var previews: some View {
        EffectsView()
            .environmentObject(AudioEngine())
    }
}
