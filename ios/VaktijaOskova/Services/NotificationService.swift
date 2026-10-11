import Foundation
import UserNotifications

final class NotificationService {
    static let shared = NotificationService()

    private init() {}

    /// iOS has a system limit of 64 local notifications.
    /// This method schedules a rolling window of upcoming prayers (7-10 days ahead)
    /// and should be refreshed whenever the app opens or background refresh fires.
    func scheduleRollingWindow(schedules: [DailyPrayerSchedule], enabledPrayers: [PrayerType: Bool], minutesBefore: Int) {
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()

        var scheduledCount = 0
        let maxAllowed = 60 // Leave 4 slots buffer under the 64 limit
        let now = Date()

        for schedule in schedules {
            for prayer in schedule.prayers {
                guard scheduledCount < maxAllowed else { return }
                guard enabledPrayers[prayer.type] ?? true else { continue }

                // 1. Exact Prayer Notification
                if prayer.time > now {
                    let content = UNMutableNotificationContent()
                    content.title = "Vrijeme je za \(prayer.type.bosnianName)"
                    content.body = "Nastupilo je vrijeme za \(prayer.type.bosnianName) namaz (\(schedule.locationName)). \(prayer.type.arabicName)"
                    content.sound = UNNotificationSound.default

                    let triggerDate = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: prayer.time)
                    let trigger = UNCalendarNotificationTrigger(dateMatching: triggerDate, repeats: false)

                    let request = UNNotificationRequest(
                        identifier: "prayer_\(prayer.type.rawValue)_\(prayer.time.timeIntervalSince1970)",
                        content: content,
                        trigger: trigger
                    )
                    center.add(request)
                    scheduledCount += 1
                }

                // 2. Reminder "X minutes before"
                if minutesBefore > 0 && scheduledCount < maxAllowed {
                    let reminderTime = prayer.time.addingTimeInterval(-Double(minutesBefore * 60))
                    if reminderTime > now {
                        let content = UNMutableNotificationContent()
                        content.title = "Podsjetnik: \(prayer.type.bosnianName) (\(minutesBefore) min)"
                        content.body = "Uskoro nastupa \(prayer.type.bosnianName) namaz (\(schedule.locationName))."
                        content.sound = UNNotificationSound.default

                        let triggerDate = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: reminderTime)
                        let trigger = UNCalendarNotificationTrigger(dateMatching: triggerDate, repeats: false)

                        let request = UNNotificationRequest(
                            identifier: "reminder_\(prayer.type.rawValue)_\(reminderTime.timeIntervalSince1970)",
                            content: content,
                            trigger: trigger
                        )
                        center.add(request)
                        scheduledCount += 1
                    }
                }
            }
        }
    }
}
