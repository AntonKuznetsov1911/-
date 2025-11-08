//
//  KarafonApp.swift
//  Karafon
//
//  Created by Karafon Team
//

import SwiftUI

@main
struct KarafonApp: App {
    @StateObject private var audioEngine = AudioEngine()

    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(audioEngine)
        }
    }
}
