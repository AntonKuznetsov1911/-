//
//  MainView.swift
//  Karafon
//
//  Main screen with microphone control and volume sliders
//

import SwiftUI

struct MainView: View {
    @EnvironmentObject var audioEngine: AudioEngine
    @State private var showSettings = false
    @State private var showEffects = false

    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(red: 0.1, green: 0.1, blue: 0.3),
                        Color(red: 0.2, green: 0.1, blue: 0.4)
                    ]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                VStack(spacing: 40) {

                    // Header
                    Text("Карафон")
                        .font(.system(size: 44, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                        .padding(.top, 40)

                    Spacer()

                    // Audio level indicator
                    AudioLevelIndicator(level: audioEngine.currentInputLevel)
                        .frame(height: 100)
                        .padding(.horizontal, 40)

                    // Main sing button
                    SingButton(isRecording: audioEngine.isRecording) {
                        if audioEngine.isRecording {
                            audioEngine.stop()
                        } else {
                            audioEngine.start()
                        }
                    }
                    .padding(.vertical, 20)

                    // Volume controls
                    VStack(spacing: 30) {
                        VolumeSlider(
                            title: "🎤 Микрофон",
                            value: $audioEngine.microphoneVolume,
                            onChange: { value in
                                audioEngine.setMicrophoneVolume(value)
                            }
                        )

                        VolumeSlider(
                            title: "🎵 Музыка",
                            value: $audioEngine.musicVolume,
                            onChange: { value in
                                audioEngine.setMusicVolume(value)
                            }
                        )
                    }
                    .padding(.horizontal, 30)

                    Spacer()

                    // Bottom controls
                    HStack(spacing: 60) {
                        // Effects button
                        ControlButton(icon: "waveform.circle.fill", title: "Эффекты") {
                            showEffects.toggle()
                        }

                        // Settings button
                        ControlButton(icon: "gearshape.fill", title: "Настройки") {
                            showSettings.toggle()
                        }
                    }
                    .padding(.bottom, 30)
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView()
                    .environmentObject(audioEngine)
            }
            .sheet(isPresented: $showEffects) {
                EffectsView()
                    .environmentObject(audioEngine)
            }
        }
    }
}

// MARK: - Sing Button

struct SingButton: View {
    let isRecording: Bool
    let action: () -> Void

    @State private var isPressed = false

    var body: some View {
        Button(action: action) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: isRecording ?
                                [Color.red.opacity(0.8), Color.red] :
                                [Color.pink.opacity(0.8), Color.purple]
                            ),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 160, height: 160)
                    .shadow(color: isRecording ? .red.opacity(0.5) : .purple.opacity(0.5),
                           radius: 20, x: 0, y: 10)

                VStack(spacing: 8) {
                    Image(systemName: isRecording ? "stop.fill" : "mic.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.white)

                    Text(isRecording ? "Стоп" : "Петь")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
        }
        .scaleEffect(isPressed ? 0.95 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}

// MARK: - Volume Slider

struct VolumeSlider: View {
    let title: String
    @Binding var value: Float
    let onChange: (Float) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)

                Spacer()

                Text("\(Int(value * 100))%")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white.opacity(0.8))
            }

            Slider(value: Binding(
                get: { Double(value) },
                set: { newValue in
                    value = Float(newValue)
                    onChange(Float(newValue))
                }
            ), in: 0...1)
                .accentColor(.pink)
        }
    }
}

// MARK: - Audio Level Indicator

struct AudioLevelIndicator: View {
    let level: Float

    var body: some View {
        GeometryReader { geometry in
            HStack(spacing: 4) {
                ForEach(0..<20) { index in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(barColor(for: index))
                        .opacity(level > Float(index) / 20.0 ? 1.0 : 0.3)
                        .animation(.easeInOut(duration: 0.1), value: level)
                }
            }
        }
    }

    private func barColor(for index: Int) -> Color {
        if index < 12 {
            return .green
        } else if index < 16 {
            return .yellow
        } else {
            return .red
        }
    }
}

// MARK: - Control Button

struct ControlButton: View {
    let icon: String
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 28))
                    .foregroundColor(.white)

                Text(title)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white.opacity(0.9))
            }
        }
    }
}

// MARK: - Preview

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
            .environmentObject(AudioEngine())
    }
}
