//
//  RecordingView.swift
//  Karafon
//
//  Screen for managing recordings
//

import SwiftUI

struct RecordingView: View {
    @EnvironmentObject var recordingManager: RecordingManager
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ZStack {
                Color(red: 0.05, green: 0.05, blue: 0.1)
                    .ignoresSafeArea()

                if recordingManager.recordings.isEmpty {
                    EmptyRecordingsView()
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            ForEach(recordingManager.recordings) { recording in
                                RecordingRow(recording: recording)
                                    .environmentObject(recordingManager)
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Записи")
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

struct EmptyRecordingsView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "music.note.list")
                .font(.system(size: 60))
                .foregroundColor(.gray)

            Text("Нет записей")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.white)

            Text("Ваши записи будут отображаться здесь")
                .font(.body)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct RecordingRow: View {
    let recording: Recording
    @EnvironmentObject var recordingManager: RecordingManager
    @State private var showShareSheet = false
    @State private var showDeleteAlert = false

    var body: some View {
        HStack(spacing: 16) {
            // Icon
            ZStack {
                Circle()
                    .fill(Color.pink.opacity(0.2))
                    .frame(width: 50, height: 50)

                Image(systemName: "music.note")
                    .foregroundColor(.pink)
                    .font(.system(size: 20))
            }

            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(recording.title)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)

                HStack {
                    Text(recording.formattedDate)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)

                    Text("•")
                        .foregroundColor(.gray)

                    Text(recording.formattedDuration)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                }
            }

            Spacer()

            // Actions
            HStack(spacing: 16) {
                Button(action: { recordingManager.playRecording(recording) }) {
                    Image(systemName: "play.fill")
                        .foregroundColor(.white)
                        .font(.system(size: 18))
                }

                Button(action: { showShareSheet = true }) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(.white)
                        .font(.system(size: 18))
                }

                Button(action: { showDeleteAlert = true }) {
                    Image(systemName: "trash")
                        .foregroundColor(.red)
                        .font(.system(size: 18))
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white.opacity(0.1))
        )
        .sheet(isPresented: $showShareSheet) {
            ActivityViewController(activityItems: [recordingManager.shareRecording(recording)])
        }
        .alert("Удалить запись?", isPresented: $showDeleteAlert) {
            Button("Отмена", role: .cancel) { }
            Button("Удалить", role: .destructive) {
                recordingManager.deleteRecording(recording)
            }
        } message: {
            Text("Это действие нельзя отменить")
        }
    }
}

// MARK: - Activity View Controller (for sharing)

struct ActivityViewController: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - Preview

struct RecordingView_Previews: PreviewProvider {
    static var previews: some View {
        RecordingView()
            .environmentObject(RecordingManager())
    }
}
