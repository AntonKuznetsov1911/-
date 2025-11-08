//
//  AudioEngine.swift
//  Karafon
//
//  Core audio engine for real-time microphone input and output
//

import AVFoundation
import Combine

/// Main audio engine class that handles microphone input, audio effects, and output
class AudioEngine: ObservableObject {

    // MARK: - Published Properties

    @Published var isRecording: Bool = false
    @Published var microphoneVolume: Float = 0.8
    @Published var musicVolume: Float = 0.7
    @Published var currentInputLevel: Float = 0.0
    @Published var isEffectsEnabled: Bool = false

    // MARK: - Private Properties

    private let engine = AVAudioEngine()
    private let inputNode: AVAudioInputNode
    private let mainMixer: AVAudioMixerNode
    private var audioSession = AVAudioSession.sharedInstance()

    // Audio effects
    private let reverbEffect = AVAudioUnitReverb()
    private let delayEffect = AVAudioUnitDelay()
    private let eqEffect = AVAudioUnitEQ(numberOfBands: 10)

    // Audio format
    private let sampleRate: Double = 48000.0
    private let preferredIOBufferDuration: TimeInterval = 0.005 // 5ms for low latency

    private var levelTimer: Timer?

    // MARK: - Initialization

    init() {
        self.inputNode = engine.inputNode
        self.mainMixer = engine.mainMixerNode

        setupAudioSession()
        setupAudioEngine()
    }

    deinit {
        stop()
    }

    // MARK: - Audio Session Setup

    private func setupAudioSession() {
        do {
            // Configure audio session for minimal latency
            try audioSession.setCategory(.playAndRecord,
                                        mode: .default,
                                        options: [.defaultToSpeaker, .allowBluetooth])

            // Set preferred sample rate and buffer duration for low latency
            try audioSession.setPreferredSampleRate(sampleRate)
            try audioSession.setPreferredIOBufferDuration(preferredIOBufferDuration)

            try audioSession.setActive(true)

            print("✅ Audio session configured successfully")
            print("📊 Sample rate: \(audioSession.sampleRate) Hz")
            print("⚡ IO buffer duration: \(audioSession.ioBufferDuration * 1000) ms")

        } catch {
            print("❌ Failed to setup audio session: \(error.localizedDescription)")
        }
    }

    // MARK: - Audio Engine Setup

    private func setupAudioEngine() {
        let inputFormat = inputNode.outputFormat(forBus: 0)
        let outputFormat = AVAudioFormat(
            commonFormat: .pcmFormatFloat32,
            sampleRate: sampleRate,
            channels: 1,
            interleaved: false
        )!

        // Configure reverb effect
        reverbEffect.loadFactoryPreset(.mediumHall)
        reverbEffect.wetDryMix = 30.0

        // Configure delay effect (echo)
        delayEffect.delayTime = 0.3
        delayEffect.feedback = 40.0
        delayEffect.wetDryMix = 25.0

        // Attach effects nodes
        engine.attach(reverbEffect)
        engine.attach(delayEffect)
        engine.attach(eqEffect)

        // Connect microphone input -> effects chain -> output
        engine.connect(inputNode, to: eqEffect, format: inputFormat)
        engine.connect(eqEffect, to: delayEffect, format: inputFormat)
        engine.connect(delayEffect, to: reverbEffect, format: inputFormat)
        engine.connect(reverbEffect, to: mainMixer, format: inputFormat)

        // Install tap to monitor input levels
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: inputFormat) { [weak self] buffer, _ in
            self?.updateInputLevel(buffer: buffer)
        }

        print("✅ Audio engine configured successfully")
    }

    // MARK: - Public Methods

    /// Start audio engine and microphone monitoring
    func start() {
        guard !isRecording else { return }

        do {
            if !engine.isRunning {
                try engine.start()
            }

            isRecording = true
            startLevelMonitoring()

            print("🎤 Audio engine started")

        } catch {
            print("❌ Failed to start audio engine: \(error.localizedDescription)")
        }
    }

    /// Stop audio engine
    func stop() {
        guard isRecording else { return }

        engine.stop()
        isRecording = false
        stopLevelMonitoring()

        print("⏹️ Audio engine stopped")
    }

    /// Update microphone volume
    func setMicrophoneVolume(_ volume: Float) {
        microphoneVolume = volume
        mainMixer.outputVolume = volume
    }

    /// Update music volume (this would control system audio if we had access)
    func setMusicVolume(_ volume: Float) {
        musicVolume = volume
        // Note: Controlling system-wide music volume requires additional implementation
        // This is a placeholder for future integration
    }

    /// Toggle audio effects (reverb, echo)
    func toggleEffects(_ enabled: Bool) {
        isEffectsEnabled = enabled

        if enabled {
            reverbEffect.bypass = false
            delayEffect.bypass = false
        } else {
            reverbEffect.bypass = true
            delayEffect.bypass = true
        }
    }

    /// Set reverb intensity (0.0 - 100.0)
    func setReverbIntensity(_ intensity: Float) {
        reverbEffect.wetDryMix = intensity
    }

    /// Set echo/delay intensity (0.0 - 100.0)
    func setEchoIntensity(_ intensity: Float) {
        delayEffect.wetDryMix = intensity
    }

    // MARK: - Private Methods

    private func updateInputLevel(buffer: AVAudioPCMBuffer) {
        guard let channelData = buffer.floatChannelData else { return }

        let channelDataValue = channelData.pointee
        let channelDataValueArray = stride(from: 0, to: Int(buffer.frameLength), by: buffer.stride)
            .map { channelDataValue[$0] }

        let rms = sqrt(channelDataValueArray.map { $0 * $0 }.reduce(0, +) / Float(buffer.frameLength))
        let avgPower = 20 * log10(rms)
        let normalizedPower = max(0, min(1, (avgPower + 50) / 50))

        DispatchQueue.main.async { [weak self] in
            self?.currentInputLevel = normalizedPower
        }
    }

    private func startLevelMonitoring() {
        levelTimer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true) { [weak self] _ in
            // Level updates happen in the tap callback
        }
    }

    private func stopLevelMonitoring() {
        levelTimer?.invalidate()
        levelTimer = nil
        currentInputLevel = 0.0
    }
}

// MARK: - Audio Session Extensions

extension AudioEngine {

    /// Get current audio route (speakers, headphones, bluetooth, etc.)
    var currentAudioRoute: String {
        let currentRoute = audioSession.currentRoute
        return currentRoute.outputs.first?.portName ?? "Unknown"
    }

    /// Check if bluetooth device is connected
    var isBluetoothConnected: Bool {
        let outputs = audioSession.currentRoute.outputs
        return outputs.contains { $0.portType == .bluetoothA2DP || $0.portType == .bluetoothHFP }
    }
}
