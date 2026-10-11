import SwiftUI

struct HolidaysView: View {
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Mubarek dani i noći")) {
                    HolidayRow(name: "Nova hidžretska godina", desc: "1. Muharrem – Početak nove godine", date: "1. Muharrem", isNight: false)
                    HolidayRow(name: "Jevmu-Ašura", desc: "10. Muharrem – Dan spasa i milosti", date: "10. Muharrem", isNight: false)
                    HolidayRow(name: "Mevlud", desc: "12. Rebiu-l-evvel – Rođenje Poslanika a.s.", date: "12. Rebiu-l-evvel", isNight: false)
                    HolidayRow(name: "Lejletu-r-regaib", desc: "Noć želja – uoči prvog petka u Redžebu", date: "uoči petka u Redžebu", isNight: true)
                    HolidayRow(name: "Lejletu-l-mi'radž", desc: "27. Redžeb – Noć uzdignuća i propisivanja namaza", date: "26. na 27. Redžeb", isNight: true)
                    HolidayRow(name: "Lejletu-l-berat", desc: "15. Ša'ban – Noć oprosta i sudbine", date: "14. na 15. Ša'ban", isNight: true)
                    HolidayRow(name: "Početak Ramazana", desc: "1. Ramazan – Prvi dan posta", date: "1. Ramazan", isNight: false)
                    HolidayRow(name: "Lejletul-Bedr", desc: "17. Ramazan – Dan pobjede na Bedru", date: "17. Ramazan", isNight: true)
                    HolidayRow(name: "Lejletu-l-kadr", desc: "27. Ramazan – Noć bolja od 1000 mjeseci", date: "26. na 27. Ramazan", isNight: true)
                    HolidayRow(name: "Ramazanski bajram", desc: "1. Ševval – Praznik završetka posta", date: "1. Ševval", isNight: false)
                    HolidayRow(name: "Dan šehida", desc: "2. Ševval – Drugi dan Bajrama", date: "2. Ševval", isNight: false)
                    HolidayRow(name: "Dan Arefata", desc: "9. Zu-l-hidždže – Stajanje na Arefatu", date: "9. Zu-l-hidždže", isNight: false)
                    HolidayRow(name: "Kurban-bajram", desc: "10. Zu-l-hidždže – Hadžijski bajram", date: "10. Zu-l-hidždže", isNight: false)
                }
            }
            .navigationTitle("Blagdani")
        }
    }
}

struct HolidayRow: View {
    let name: String
    let desc: String
    let date: String
    let isNight: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(name)
                    .font(.headline)
                Spacer()
                if isNight {
                    Text("uoči akšama")
                        .font(.caption2.bold())
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.orange.opacity(0.2))
                        .cornerRadius(4)
                }
            }
            Text(desc)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(date)
                .font(.caption2.bold())
                .foregroundColor(Color(red: 0.02, green: 0.47, blue: 0.34))
        }
        .padding(.vertical, 4)
    }
}

struct QiblaView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Text("Pravac Kabe: Živinice")
                    .font(.title3.bold())
                Text("Azimut: 137° Jugoistok • Kaaba (21.4225° N, 39.8262° E)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                ZStack {
                    Circle()
                        .stroke(Color.green.opacity(0.3), lineWidth: 3)
                        .frame(width: 250, height: 250)

                    Image(systemName: "location.north.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                        .rotationEffect(.degrees(137))
                }

                Text("Usmjerite telefon vodoravno i kalibrišite u obliku broja 8.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                Spacer()
            }
            .padding(.top, 30)
            .navigationTitle("Kibla kompas")
        }
    }
}

struct DzematView: View {
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Džemat Oskova")
                            .font(.title2.bold())
                        Text("Medžlis Islamske zajednice Živinice")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(16)

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Džematske obavijesti")
                            .font(.headline)
                        Text("Trenutno nema novih objavljenih obavijesti džemata.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(16)
                }
                .padding()
            }
            .navigationTitle("Džemat Oskova")
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject var vm: MainViewModel

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Lokacija")) {
                    HStack {
                        Text("Odabrana lokacija")
                        Spacer()
                        Text("106 - Živinice")
                            .foregroundColor(.secondary)
                    }
                }

                Section(header: Text("Podsjetnik prije namaza")) {
                    Picker("Vrijeme podsjetnika", selection: $vm.minutesBefore) {
                        Text("Isključeno").tag(0)
                        Text("5 min").tag(5)
                        Text("10 min").tag(10)
                        Text("15 min").tag(15)
                        Text("30 min").tag(30)
                    }
                }

                Section(header: Text("Izgled")) {
                    Toggle("Tamna tema", isOn: $vm.isDarkTheme)
                }

                Section(header: Text("O aplikaciji")) {
                    HStack {
                        Text("Verzija")
                        Spacer()
                        Text("1.0.0 (iOS)")
                            .foregroundColor(.secondary)
                    }
                    Text("Podaci: vaktija.ba i lokalni keš")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Postavke")
        }
    }
}
