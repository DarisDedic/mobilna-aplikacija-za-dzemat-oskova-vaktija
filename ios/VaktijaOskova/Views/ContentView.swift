import SwiftUI

struct ContentView: View {
    @EnvironmentObject var vm: MainViewModel
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tabItem {
                    Label("Vaktija", systemImage: "clock.fill")
                }
                .tag(0)

            HolidaysView()
                .tabItem {
                    Label("Blagdani", systemImage: "calendar")
                }
                .tag(1)

            QiblaView()
                .tabItem {
                    Label("Kibla", systemImage: "safari.fill")
                }
                .tag(2)

            DzematView()
                .tabItem {
                    Label("Džemat", systemImage: "building.columns.fill")
                }
                .tag(3)

            SettingsView()
                .tabItem {
                    Label("Postavke", systemImage: "gearshape.fill")
                }
                .tag(4)
        }
        .accentColor(Color(red: 0.02, green: 0.47, blue: 0.34)) // Emerald Green
    }
}
