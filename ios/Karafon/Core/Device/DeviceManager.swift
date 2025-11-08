//
//  DeviceManager.swift
//  Karafon
//
//  Device and audio route management
//

import AVFoundation
import Combine

/// Manages audio device connections and routes
class DeviceManager: ObservableObject {

    @Published var currentRoute: AudioRoute = .speaker
    @Published var availableDevices: [AudioDevice] = []
    @Published var isBluetoothAvailable: Bool = false

    private let audioSession = AVAudioSession.sharedInstance()
    private var cancellables = Set<AnyCancellable>()

    init() {
        setupNotifications()
        updateCurrentRoute()
        updateAvailableDevices()
    }

    // MARK: - Setup

    private func setupNotifications() {
        NotificationCenter.default.publisher(for: AVAudioSession.routeChangeNotification)
            .sink { [weak self] _ in
                self?.handleRouteChange()
            }
            .store(in: &cancellables)
    }

    // MARK: - Route Management

    private func handleRouteChange() {
        updateCurrentRoute()
        updateAvailableDevices()
    }

    private func updateCurrentRoute() {
        let outputs = audioSession.currentRoute.outputs

        if let output = outputs.first {
            currentRoute = AudioRoute(from: output.portType)
        } else {
            currentRoute = .speaker
        }

        isBluetoothAvailable = outputs.contains { output in
            output.portType == .bluetoothA2DP || output.portType == .bluetoothHFP || output.portType == .bluetoothLE
        }
    }

    private func updateAvailableDevices() {
        var devices: [AudioDevice] = []

        // Built-in speaker
        devices.append(AudioDevice(name: "iPhone Speaker", type: .speaker, isActive: currentRoute == .speaker))

        // Check for headphones
        let outputs = audioSession.currentRoute.outputs
        for output in outputs {
            let device = AudioDevice(
                name: output.portName,
                type: AudioRoute(from: output.portType),
                isActive: true
            )
            devices.append(device)
        }

        availableDevices = devices
    }

    // MARK: - Device Selection

    func selectDevice(_ device: AudioDevice) {
        // Note: Direct device selection is limited on iOS
        // The system automatically routes to connected devices
        print("Selected device: \(device.name)")
    }

    // MARK: - Bluetooth

    func enableBluetooth() {
        do {
            try audioSession.setCategory(.playAndRecord,
                                        mode: .default,
                                        options: [.allowBluetooth, .allowBluetoothA2DP])
            try audioSession.setActive(true)
            print("✅ Bluetooth enabled")
        } catch {
            print("❌ Failed to enable Bluetooth: \(error)")
        }
    }
}

// MARK: - Models

enum AudioRoute {
    case speaker
    case headphones
    case bluetooth
    case airPlay
    case aux
    case unknown

    init(from portType: AVAudioSession.Port) {
        switch portType {
        case .builtInSpeaker:
            self = .speaker
        case .headphones, .headsetMic:
            self = .headphones
        case .bluetoothA2DP, .bluetoothHFP, .bluetoothLE:
            self = .bluetooth
        case .airPlay:
            self = .airPlay
        case .lineOut:
            self = .aux
        default:
            self = .unknown
        }
    }

    var displayName: String {
        switch self {
        case .speaker: return "Speaker"
        case .headphones: return "Headphones"
        case .bluetooth: return "Bluetooth"
        case .airPlay: return "AirPlay"
        case .aux: return "AUX"
        case .unknown: return "Unknown"
        }
    }

    var icon: String {
        switch self {
        case .speaker: return "speaker.wave.2.fill"
        case .headphones: return "headphones"
        case .bluetooth: return "antenna.radiowaves.left.and.right"
        case .airPlay: return "airplayaudio"
        case .aux: return "cable.connector"
        case .unknown: return "speaker.slash.fill"
        }
    }
}

struct AudioDevice: Identifiable {
    let id = UUID()
    let name: String
    let type: AudioRoute
    let isActive: Bool
}
