import SwiftUI
import UserNotifications

@main
struct VaktijaOskovaApp: App {
    @StateObject private var viewModel = MainViewModel()

    init() {
        requestNotificationPermission()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
                .preferredColorScheme(viewModel.colorScheme)
        }
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            if granted {
                print("Dozvola za obavijesti odobrena na iOS-u.")
            }
        }
    }
}
