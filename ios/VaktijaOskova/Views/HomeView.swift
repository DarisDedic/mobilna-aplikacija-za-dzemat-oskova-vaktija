import SwiftUI

struct HomeView: View {
    @EnvironmentObject var vm: MainViewModel

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Header Bar
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            HStack {
                                Text("Vaktija")
                                    .font(.title2.bold())
                                    .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                                Text("Džemat Oskova")
                                    .font(.caption.bold())
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color(red: 0.02, green: 0.47, blue: 0.34).opacity(0.25))
                                    .cornerRadius(6)
                            }
                            Text(vm.locationName)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                    }
                    .padding(.horizontal)

                    // Hero Next Prayer Countdown Card
                    if let next = vm.nextPrayer {
                        VStack(spacing: 8) {
                            HStack {
                                Text("SLJEDEĆI NAMAZ")
                                    .font(.caption.bold())
                                    .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                                Spacer()
                                Text(next.type.arabicName)
                                    .font(.title3)
                                    .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                            }

                            HStack(alignment: .firstTextBaseline) {
                                Text(next.type.bosnianName)
                                    .font(.system(size: 28, weight: .bold))
                                Spacer()
                                Text(next.formattedTime)
                                    .font(.system(size: 24, weight: .semibold))
                                    .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                            }

                            Text(vm.countdownString)
                                .font(.system(size: 46, weight: .bold, design: .monospaced))
                                .padding(.vertical, 6)

                            Text("preostalo vrijeme do vakta")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding(18)
                        .background(
                            LinearGradient(
                                colors: [Color(red: 0.02, green: 0.31, blue: 0.23), Color(red: 0.04, green: 0.18, blue: 0.14)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .cornerRadius(18)
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(Color(red: 0.98, green: 0.75, blue: 0.14), lineWidth: 1.5)
                        )
                        .padding(.horizontal)
                    }

                    // 6 Prayer Times Card
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Dnevna vaktija (6 namaskih vremena)")
                            .font(.headline)
                            .padding(.bottom, 4)

                        if let sched = vm.todaySchedule {
                            ForEach(sched.prayers) { prayer in
                                let isNext = vm.nextPrayer?.type == prayer.type
                                HStack {
                                    Image(systemName: isNext ? "bell.fill" : "bell")
                                        .foregroundColor(isNext ? Color(red: 0.98, green: 0.75, blue: 0.14) : .secondary)

                                    VStack(alignment: .leading) {
                                        Text(prayer.type.bosnianName)
                                            .font(.body.weight(isNext ? .bold : .medium))
                                        if isNext {
                                            Text("Sljedeći namaz")
                                                .font(.caption2.bold())
                                                .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                                        }
                                    }

                                    Spacer()

                                    Text(prayer.type.arabicName)
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                        .padding(.trailing, 8)

                                    Text(prayer.formattedTime)
                                        .font(.title3.bold())
                                        .foregroundColor(isNext ? Color(red: 0.98, green: 0.75, blue: 0.14) : Color(red: 0.02, green: 0.65, blue: 0.45))
                                }
                                .padding(12)
                                .background(isNext ? Color(red: 0.02, green: 0.47, blue: 0.34).opacity(0.3) : Color.gray.opacity(0.1))
                                .cornerRadius(12)
                            }
                        }
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(18)
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationBarHidden(true)
        }
    }
}
