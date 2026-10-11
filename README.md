# Vaktija – Džemat Oskova

Mobilna aplikacija za prikaz tačnih vremena namaza, kalendar mubarek dana i noći, kibla kompas i obavijesti za **Džemat Oskova** (Medžlis Islamske zajednice Živinice) i sve gradove u Bosni i Hercegovini i Sandžaku.

---

## 1. Arhitektura i odluka o tehnologiji (Nativno vs. Flutter)

### Zašto nativno (Android Kotlin / iOS Swift) umjesto čistog cross-platforma?
U aplikacijama za vaktiju **najvažniji zahtjev je pouzdanost alarma**. Korisnik ne smije propustiti sabah ili iftar zato što je operativni sistem "uspavao" proces aplikacije.
1. **Android (Doze Mode i agresivni proizvođači):** 
   Na Android uređajima (posebno Samsung, Xiaomi, Huawei), pozadinski servisi se agresivno gase. Pouzdani alarmi zahtijevaju direktnu upotrebu `AlarmManager.setExactAndAllowWhileIdle()`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED` pri restartu telefona i direktne sistemske `AppWidget` / Jetpack Glance providere koji čitaju iz lokalne SQLite (Room) baze bez buđenja mrežnog steka. U Flutteru se za ove svrhe ionako moraju pisati nativni Kotlin moduli (Platform Channels), što stvara dodatni sloj složenosti.
2. **iOS (Apple ograničenja):** 
   Apple dozvoljava najviše 64 lokalno zakazane obavijesti po aplikaciji (`UNNotificationCenter`). To zahtijeva "klizeći prozor" (rolling window) koji se automatski obnavlja pri svakom pokretanju i kroz `BGAppRefreshTask`. Također, zvuk obavijesti na iOS-u je ograničen na maksimalno 30 sekundi.
3. **Preporuka:**
   - **Android:** Kotlin + Jetpack Compose + Room + AlarmManager + AppWidget (kompletno implementirano ovdje).
   - **iOS:** Swift + SwiftUI + SwiftData/CoreData + WidgetKit + UNNotificationCenter (potpuno identična logika i pravila podataka).

---

## 2. Pokretanje i testiranje na stvarnom uređaju

### A. Android (USB / Emulator)
1. Uključite **Developer Options** na telefonu:
   - Postavke -> O telefonu -> tapnite 7 puta na *Build Number*.
   - U *Opcije za programere* uključite **USB Debugging** (Otklanjanje grešaka putem USB-a).
2. Spojite telefon USB kablom i potvrdite autorizaciju na ekranu telefona.
3. Pokrenite aplikaciju:
   ```bash
   gradle installDebug
   ```
4. Za pokretanje svih automatizovanih testova (validacija, prelazak ljetnog/zimskog vremena, hidžretski datumi):
   ```bash
   gradle :app:testDebugUnitTest
   ```

### B. Direktan izvoz APK datoteke za džematlije (instalacija bez Google Play prodavnice)
Džemat Oskova može podijeliti aplikaciju direktno džematlijama preko džematske web stranice ili WhatsApp/Viber grupe:
1. Generisanje instalacionog APK paketa:
   ```bash
   gradle :app:assembleRelease
   # ili za brzi test:
   gradle :app:assembleDebug
   ```
   Generisani APK se nalazi na lokaciji:
   `app/build/outputs/apk/release/app-release.apk`
2. **Uputstvo za džematlije na telefonu:**
   - Preuzmite APK na telefon.
   - Kada telefon prikaže upozorenje *"Instalacija iz nepoznatih izvora"*, kliknite *Dozvoli za ovaj izvor* (Chrome/Files).
   - Nakon instalacije, u Postavkama telefona dodijelite dozvolu za **Obavijesti** i **Tačne alarme**.

---

## 3. Izrada produkcijskih paketa (AAB za Android, IPA za iOS)

### Android App Bundle (AAB za Google Play):
```bash
gradle :app:bundleRelease
```
Izlaz: `app/build/outputs/bundle/release/app-release.aab`

### iOS (IPA za App Store):
1. Otvorite iOS projekt u Xcode-u na macOS računaru.
2. Odaberite *Product -> Archive*.
3. U Organizeru odaberite *Distribute App -> App Store Connect -> Upload* (ili *Export IPA* za Ad-Hoc distribuciju).

---

## 4. Aktuelni uvjeti i naknade za objavu na Google Play i Apple App Store

### Google Play Console:
- **Jednokratna naknada:** 25 USD (plaća se jednom za registraciju developer računa).
- **Lični vs. Organizacijski račun:**
  - *Novi lični računi:* Za lične račune otvorene poslije novembra 2023. Google zahtijeva **obavezno zatvoreno testiranje sa najmanje 12 testera tokom 14 dana** prije puštanja na javnu produkciju.
  - *Organizacijski račun (Džemat / Pravno lice):* Nema uslova od 12 testera, ali zahtijeva D-U-N-S broj (Dun & Bradstreet) registrovane organizacije.
- **Dozvola SCHEDULE_EXACT_ALARM:** Google Play strogo kontroliše ovu dozvolu. Aplikacije za vaktiju i vjerske alarme spadaju pod dozvoljenu kategoriju (namazi se moraju obaviti u tačno određeni minut). U formularu deklaracije navodi se: *"Aplikacija zakazuje tačne alarme za obavljanje molitve (namaza) u fiksno astronomsko vrijeme."*

### Apple Developer Program:
- **Godišnja pretplata:** 99 USD / godišnje.
- **Uvjeti:** Za pravna lica (neprofitne organizacije / vjerske zajednice) potreban je D-U-N-S broj. U nekim zemljama neprofitne organizacije mogu zatražiti oslobađanje od naknade.

---

## 5. Vanjske adrese (Endpointi) koje aplikacija poziva

| Adresa | Namjena | Šta se desi ako adresa ne radi? |
|---|---|---|
| `https://api.vaktija.ba/vaktija/v1/{id}/{godina}/{mjesec}` | Mjesečno preuzimanje za 60+ dana unaprijed | Aplikacija nastavlja nesmetano raditi iz **lokalnog keša**. Ako nema ni keša ni mreže, automatski se aktivira **približni astronomski proračun** označen badge-om. |
| `https://api.vaktija.ba/vaktija/v1/{id}/{godina}/{mjesec}/{dan}` | Provjera pojedinačnog dana po potrebi | Koristi se lokalni keš; aktivira se eksponencijalni backoff (15 min do 6 h) bez opterećivanja servera. |
| `https://api.vaktija.ba/vaktija/v1/lokacije` | Spisak 116 lokacija u BiH i Sandžaku | Aplikacija ima ugrađenu **offline bazu svih 116 lokacija** sa koordinatama i ID-evima, tako da radi i bez mreže. |

---

## 6. Popis dozvola u aplikaciji i obrazloženje

| Dozvola | Tip | Zašto je potrebna |
|---|---|---|
| `POST_NOTIFICATIONS` | Runtime (Android 13+) | Za prikaz obavijesti i ezana pri nastupanju namaskog vakta. |
| `SCHEDULE_EXACT_ALARM` | Posebna | Za buđenje uređaja u tačnu minutu namaza (zaobilaženje Doze režima). |
| `RECEIVE_BOOT_COMPLETED` | Instalacijska | Da ponovo zakaže alarme nakon što se telefon restartuje ili ažurira sistem. |
| `VIBRATE` | Normalna | Vibracija uz zvučnu obavijest namaza. |
| `INTERNET` | Normalna | Preuzimanje službenih vaktova sa vaktija.ba i džematskih obavijesti. |
| `ACCESS_NETWORK_STATE` | Normalna | Provjera dostupnosti mreže prije slanja zahtjeva. |
| `ACCESS_FINE_LOCATION` / `COARSE_LOCATION` | Runtime (samo na zahtjev) | **Opcionalno**: koristi se isključivo ako korisnik sam pritisne *"Najbliža lokacija (GPS)"*. Koordinate se obrađuju samo lokalno. |

---

## 7. Provjera pravila vaktija.ba i autorskih prava na zvukove

1. **Uvjeti vaktija.ba API-ja:**
   - Maksimalno 1 zahtjev u sekundi (ugrađen mutex i rate limiter).
   - Na grešku ili HTTP 429 aplikacija čeka (15 min, 30 min, 1h ... do 6h) i služi podatke iz SQLite keša.
   - Aplikacija preuzima podatke mjesec unaprijed (3 poziva za 90 dana umjesto 90 poziva), što štedi serverske resurse vaktija.ba za 98%.
2. **Licence za zvukove ezana:**
   - Snimci ezana podliježu autorskim pravima. Aplikacija u MVP-u koristi provjerene sistemske tonove uređaja.
   - Za ezan Džemata Oskova preporučuje se snimiti vlastiti glas imama ili mujezina džemata u džamiji Oskova, čime džemat posjeduje 100% autorska prava.

---

## 8. Politika privatnosti (Bosanski jezik)

Aplikacija **Vaktija – Džemat Oskova** poštuje vašu privatnost:
- Nema registracije, korisničkih računa, unosa e-maila niti praćenja identiteta.
- Nema analitičkih alata (Google Analytics, Firebase Analytics, Meta Pixel) niti reklama.
- Lokacijski podaci (GPS) se koriste isključivo na uređaju za pronalazak najbliže općine i nikada se ne prenose na server.
- Svi podaci o postavkama i vaktovima čuvaju se lokalno na vašem telefonu.
