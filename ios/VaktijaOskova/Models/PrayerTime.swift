import Foundation

enum PrayerType: Int, CaseIterable, Identifiable {
    case fajr = 0
    case sunrise = 1
    case dhuhr = 2
    case asr = 3
    case maghrib = 4
    case isha = 5

    var id: Int { rawValue }

    var bosnianName: String {
        switch self {
        case .fajr: return "Zora"
        case .sunrise: return "Izlazak sunca"
        case .dhuhr: return "Podne"
        case .asr: return "Ikindija"
        case .maghrib: return "Akšam"
        case .isha: return "Jacija"
        }
    }

    var arabicName: String {
        switch self {
        case .fajr: return "الفجر"
        case .sunrise: return "الشروق"
        case .dhuhr: return "الظهر"
        case .asr: return "العصر"
        case .maghrib: return "المغرب"
        case .isha: return "العشاء"
        }
    }
}

struct PrayerItem: Identifiable {
    let id = UUID()
    let type: PrayerType
    let time: Date
    let formattedTime: String
}

struct DailyPrayerSchedule: Identifiable {
    let id = UUID()
    let locationId: Int
    let locationName: String
    let date: Date
    let prayers: [PrayerItem]
    let isApproximate: Bool
    let sourceNote: String

    var fajr: PrayerItem { prayers[0] }
    var sunrise: PrayerItem { prayers[1] }
    var dhuhr: PrayerItem { prayers[2] }
    var asr: PrayerItem { prayers[3] }
    var maghrib: PrayerItem { prayers[4] }
    var isha: PrayerItem { prayers[5] }

    func nextPrayer(now: Date) -> PrayerItem? {
        return prayers.first { $0.time > now }
    }
}

// Validator matching requirements: exactly 6 times, strictly ascending order
struct PrayerValidator {
    static func validate(locationId: Int, locationName: String, date: Date, vakat: [String]) -> DailyPrayerSchedule? {
        guard vakat.count == 6 else { return nil }

        let calendar = Calendar.current
        var times: [Date] = []
        var formatted: [String] = []

        for raw in vakat {
            let parts = raw.split(separator: ":")
            guard parts.count == 2,
                  let hour = Int(parts[0].trimmingCharacters(in: .whitespaces)),
                  let minute = Int(parts[1].trimmingCharacters(in: .whitespaces)),
                  hour >= 0, hour < 24, minute >= 0, minute < 60 else {
                return nil
            }

            var components = calendar.dateComponents([.year, .month, .day], from: date)
            components.hour = hour
            components.minute = minute
            components.second = 0
            components.timeZone = TimeZone(identifier: "Europe/Sarajevo")

            guard let prayerDate = calendar.date(from: components) else { return nil }
            times.append(prayerDate)
            formatted.append(String(format: "%02d:%02d", hour, minute))
        }

        // Strictly ascending order check
        for i in 0..<times.count - 1 {
            if times[i] >= times[i + 1] {
                return nil
            }
        }

        let items = times.indices.map { idx in
            PrayerItem(
                type: PrayerType(rawValue: idx) ?? .dhuhr,
                time: times[idx],
                formattedTime: formatted[idx]
            )
        }

        return DailyPrayerSchedule(
            locationId = locationId,
            locationName = locationName,
            date: date,
            prayers: items,
            isApproximate = false,
            sourceNote = "vaktija.ba"
        )
    }
}
