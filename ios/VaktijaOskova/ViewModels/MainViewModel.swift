import SwiftUI
import Combine

final class MainViewModel: ObservableObject {
    @Published var selectedDate = Date()
    @Published var todaySchedule: DailyPrayerSchedule?
    @Published var nextPrayer: PrayerItem?
    @Published var countdownString: String = "00:00:00"
    @Published var locationName: String = "Živinice (Džemat Oskova)"
    @Published var locationId: Int = 106
    @Published var isDarkTheme: Bool = true
    @Published var minutesBefore: Int = 0
    @Published var enabledPrayers: [PrayerType: Bool] = [
        .fajr: true, .sunrise: true, .dhuhr: true, .asr: true, .maghrib: true, .isha: true
    ]

    var colorScheme: ColorScheme? {
        isDarkTheme ? .dark : .light
    }

    private var timer: AnyCancellable?

    init() {
        loadMockData()
        startTimer()
    }

    func loadMockData() {
        // Load initial prayer schedule for location 106
        let times = ["5:12", "6:41", "12:35", "15:46", "18:27", "19:45"]
        todaySchedule = PrayerValidator.validate(
            locationId: 106,
            locationName: "Živinice",
            date: Date(),
            vakat: times
        )
        updateTick()
    }

    private func startTimer() {
        timer = Timer.publish(every: 1.0, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.updateTick()
            }
    }

    private func updateTick() {
        guard let sched = todaySchedule else { return }
        let now = Date()
        let next = sched.nextPrayer(now: now) ?? sched.fajr
        nextPrayer = next

        var target = next.time
        if target < now {
            target = Calendar.current.date(byAdding: .day, value: 1, to: target) ?? target
        }

        let diff = max(0, Int(target.timeIntervalSince(now)))
        let hrs = diff / 3600
        let mins = (diff % 3600) / 60
        let secs = diff % 60
        countdownString = String(format: "%02d:%02d:%02d", hrs, mins, secs)
    }
}
