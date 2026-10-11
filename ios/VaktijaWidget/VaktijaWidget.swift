import WidgetKit
import SwiftUI

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), nextPrayerName: "Podne", nextPrayerTime: "12:35", locationName: "Džemat Oskova")
    }

    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), nextPrayerName: "Podne", nextPrayerTime: "12:35", locationName: "Džemat Oskova")
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        let entry = SimpleEntry(date: Date(), nextPrayerName: "Podne", nextPrayerTime: "12:35", locationName: "Džemat Oskova")
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())!
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
        completion(timeline)
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let nextPrayerName: String
    let nextPrayerTime: String
    let locationName: String
}

struct VaktijaWidgetEntryView : View {
    var entry: Provider.Entry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(entry.locationName)
                    .font(.caption2.bold())
                    .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                Spacer()
                Text("Vaktija")
                    .font(.caption2)
                    .foregroundColor(.white.opacity(0.8))
            }

            Spacer()

            Text("Sljedeći namaz:")
                .font(.caption2)
                .foregroundColor(.white.opacity(0.8))

            Text(entry.nextPrayerName)
                .font(.headline.bold())
                .foregroundColor(.white)

            Text(entry.nextPrayerTime)
                .font(.system(size: 26, weight: .bold))
                .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
        }
        .padding(12)
        .background(
            LinearGradient(
                colors: [Color(red: 0.02, green: 0.31, blue: 0.23), Color(red: 0.04, green: 0.18, blue: 0.14)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

@main
struct VaktijaWidget: Widget {
    let kind: String = "VaktijaWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            VaktijaWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Vaktija Oskova")
        .description("Prikaz sljedećeg namaza i preostalog vremena.")
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular])
    }
}
