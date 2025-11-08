//
//  RecordingManager.swift
//  Karafon
//
//  Recording functionality for saving performances
//

import AVFoundation
import Combine

/// Manages audio recording and exporting
class RecordingManager: NSObject, ObservableObject {

    @Published var isRecording: Bool = false
    @Published var recordingDuration: TimeInterval = 0
    @Published var recordings: [Recording] = []

    private var audioRecorder: AVAudioRecorder?
    private var recordingTimer: Timer?
    private var currentRecordingURL: URL?

    private let audioSession = AVAudioSession.sharedInstance()

    override init() {
        super.init()
        loadRecordings()
    }

    // MARK: - Recording

    /// Start recording
    func startRecording() {
        guard !isRecording else { return }

        // Setup audio session for recording
        do {
            try audioSession.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetooth])
            try audioSession.setActive(true)
        } catch {
            print("❌ Failed to setup audio session for recording: \(error)")
            return
        }

        // Create recording URL
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd_HH-mm-ss"
        let filename = "Karafon_\(dateFormatter.string(from: Date())).m4a"
        currentRecordingURL = documentsPath.appendingPathComponent(filename)

        // Configure recording settings
        let settings: [String: Any] = [
            AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
            AVSampleRateKey: 48000.0,
            AVNumberOfChannelsKey: 2,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]

        // Create and start recorder
        do {
            guard let url = currentRecordingURL else { return }
            audioRecorder = try AVAudioRecorder(url: url, settings: settings)
            audioRecorder?.delegate = self
            audioRecorder?.prepareToRecord()
            audioRecorder?.record()

            isRecording = true
            recordingDuration = 0

            // Start timer
            recordingTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
                self?.updateRecordingDuration()
            }

            print("🔴 Recording started: \(filename)")

        } catch {
            print("❌ Failed to start recording: \(error)")
        }
    }

    /// Stop recording
    func stopRecording() {
        guard isRecording else { return }

        audioRecorder?.stop()
        recordingTimer?.invalidate()
        recordingTimer = nil

        isRecording = false

        // Save recording metadata
        if let url = currentRecordingURL {
            let recording = Recording(
                url: url,
                date: Date(),
                duration: recordingDuration
            )
            recordings.insert(recording, at: 0)
            saveRecordings()
        }

        currentRecordingURL = nil
        recordingDuration = 0

        print("⏹️ Recording stopped")
    }

    private func updateRecordingDuration() {
        guard let recorder = audioRecorder, recorder.isRecording else { return }
        recordingDuration = recorder.currentTime
    }

    // MARK: - Playback

    func playRecording(_ recording: Recording) {
        // TODO: Implement playback
        print("▶️ Playing recording: \(recording.title)")
    }

    // MARK: - Delete

    func deleteRecording(_ recording: Recording) {
        // Remove file
        try? FileManager.default.removeItem(at: recording.url)

        // Remove from list
        recordings.removeAll { $0.id == recording.id }
        saveRecordings()

        print("🗑️ Deleted recording: \(recording.title)")
    }

    // MARK: - Export

    func shareRecording(_ recording: Recording) -> URL {
        return recording.url
    }

    // MARK: - Persistence

    private func loadRecordings() {
        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]

        do {
            let files = try FileManager.default.contentsOfDirectory(at: documentsPath, includingPropertiesForKeys: [.creationDateKey], options: [.skipsHiddenFiles])

            let audioFiles = files.filter { $0.pathExtension == "m4a" }

            recordings = audioFiles.compactMap { url -> Recording? in
                guard let attributes = try? FileManager.default.attributesOfItem(atPath: url.path),
                      let creationDate = attributes[.creationDate] as? Date else {
                    return nil
                }

                let asset = AVAsset(url: url)
                let duration = CMTimeGetSeconds(asset.duration)

                return Recording(url: url, date: creationDate, duration: duration)
            }.sorted { $0.date > $1.date }

        } catch {
            print("❌ Failed to load recordings: \(error)")
        }
    }

    private func saveRecordings() {
        // Recordings are automatically persisted as files
        // This method is here for future metadata storage if needed
    }
}

// MARK: - AVAudioRecorderDelegate

extension RecordingManager: AVAudioRecorderDelegate {
    func audioRecorderDidFinishRecording(_ recorder: AVAudioRecorder, successfully flag: Bool) {
        if flag {
            print("✅ Recording saved successfully")
        } else {
            print("❌ Recording failed")
        }
    }

    func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder, error: Error?) {
        print("❌ Recording encode error: \(error?.localizedDescription ?? "unknown")")
    }
}

// MARK: - Recording Model

struct Recording: Identifiable {
    let id = UUID()
    let url: URL
    let date: Date
    let duration: TimeInterval

    var title: String {
        url.deletingPathExtension().lastPathComponent
    }

    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }

    var formattedDuration: String {
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}
