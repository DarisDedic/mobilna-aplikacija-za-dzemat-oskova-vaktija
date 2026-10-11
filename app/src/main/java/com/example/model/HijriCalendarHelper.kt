package com.example.model

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

data class HijriDateInfo(
    val day: Int,
    val month: Int,
    val monthNameBosnian: String,
    val year: Int,
    val isRamadan: Boolean,
    val ramadanDay: Int?
) {
    fun formatBosnian(): String {
        return "$day. $monthNameBosnian $year. h.g."
    }
}

object HijriCalendarHelper {

    val HIJRI_MONTH_NAMES_BOSNIAN = listOf(
        "Muharrem",
        "Safer",
        "Rebiu-l-evvel",
        "Rebiu-l-ahir",
        "Džumade-l-ula",
        "Džumade-l-uhra",
        "Redžeb",
        "Ša'ban",
        "Ramazan",
        "Ševval",
        "Zu-l-ka'de",
        "Zu-l-hidždže"
    )

    fun getHijriDate(gregorianDate: LocalDate, dayOffset: Int = 0): HijriDateInfo {
        val adjustedDate = gregorianDate.plusDays(dayOffset.toLong())
        val hijrahDate = HijrahChronology.INSTANCE.date(adjustedDate)

        val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
        val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
        val year = hijrahDate.get(ChronoField.YEAR)

        val monthName = HIJRI_MONTH_NAMES_BOSNIAN.getOrElse(month - 1) { "Mjesec $month" }
        val isRamadan = (month == 9)
        val ramadanDay = if (isRamadan) day else null

        return HijriDateInfo(
            day = day,
            month = month,
            monthNameBosnian = monthName,
            year = year,
            isRamadan = isRamadan,
            ramadanDay = ramadanDay
        )
    }

    /**
     * Finds Gregorian date for a specific Hijri day, month, and year.
     */
    fun findGregorianForHijri(hijriYear: Int, hijriMonth: Int, hijriDay: Int, dayOffset: Int = 0): LocalDate? {
        return try {
            val hijrahDate = HijrahChronology.INSTANCE.date(hijriYear, hijriMonth, hijriDay.coerceAtMost(29))
            LocalDate.from(hijrahDate).minusDays(dayOffset.toLong())
        } catch (_: Exception) {
            try {
                val hijrahDate = HijrahChronology.INSTANCE.date(hijriYear, hijriMonth, 29)
                LocalDate.from(hijrahDate).minusDays(dayOffset.toLong())
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Returns list of upcoming Islamic holidays and mubarek nights as required by the user:
     * 1. Nova hidžretska godina (1. Muharrem)
     * 2. Jevmu-Ašura (10. Muharrem)
     * 3. Mevlud (12. Rebiu-l-evvel)
     * 4. Lejletu-r-regaib (noć uoči prvog petka u Redžebu)
     * 5. Lejletu-l-mi'radž (26. na 27. Redžeb)
     * 6. Lejletu-l-berat (14. na 15. Ša'ban)
     * 7. Početak Ramazana (1. Ramazan)
     * 8. Dan pobjede na Bedru / Lejletul-Bedr (17. Ramazan)
     * 9. Lejletu-l-kadr (26. na 27. Ramazan)
     * 10. Ramazanski bajram (1. Ševval)
     * 11. Dan šehida (2. Ševval)
     * 12. Dan Arefata (9. Zu-l-hidždže)
     * 13. Kurban-bajram (10. Zu-l-hidždže)
     */
    fun getUpcomingHolidays(currentDate: LocalDate, dayOffset: Int = 0): List<Holiday> {
        val currentHijri = getHijriDate(currentDate, dayOffset)
        val yearsToCheck = listOf(currentHijri.year, currentHijri.year + 1)
        val result = mutableListOf<Holiday>()

        for (hYear in yearsToCheck) {
            // 1. Nova hidžretska godina
            findGregorianForHijri(hYear, 1, 1, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "nova_godina_$hYear",
                        name = "Nova hidžretska $hYear. godina",
                        description = "1. Muharrem – Prvi dan nove hidžretske godine",
                        hijriDay = 1,
                        hijriMonth = 1,
                        hijriMonthName = "Muharrem",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 2. Dan Ašure
            findGregorianForHijri(hYear, 1, 10, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "asura_$hYear",
                        name = "Jevmu-Ašura (Dan Ašure)",
                        description = "10. Muharrem – Dan oslobođenja i milosti",
                        hijriDay = 10,
                        hijriMonth = 1,
                        hijriMonthName = "Muharrem",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 3. Mevlud
            findGregorianForHijri(hYear, 3, 12, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "mevlud_$hYear",
                        name = "Mevlud (Rođenje Poslanika a.s.)",
                        description = "12. Rebiu-l-evvel – Dan rođenja Muhammeda a.s.",
                        hijriDay = 12,
                        hijriMonth = 3,
                        hijriMonthName = "Rebiu-l-evvel",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 4. Lejletu-r-regaib (noć uoči prvog petka u Redžebu)
            findGregorianForHijri(hYear, 7, 1, dayOffset)?.let { rajabFirst ->
                // find the first Friday of Rajab
                var cursor = rajabFirst
                while (cursor.dayOfWeek.value != 5 && ChronoUnit.DAYS.between(rajabFirst, cursor) < 7) {
                    cursor = cursor.plusDays(1)
                }
                result.add(
                    Holiday(
                        id = "regaib_$hYear",
                        name = "Lejletu-r-regaib",
                        description = "Noć želja i nade – uoči prvog petka u mjesecu Redžebu",
                        hijriDay = 1,
                        hijriMonth = 7,
                        hijriMonthName = "Redžeb",
                        isNightEve = true, // Countdown to Thursday Maghrib
                        targetDate = cursor
                    )
                )
            }

            // 5. Lejletu-l-mi'radž (26. na 27. Redžeb)
            findGregorianForHijri(hYear, 7, 27, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "miradz_$hYear",
                        name = "Lejletu-l-mi'radž",
                        description = "27. Redžeb – Noć uzdignuća Poslanika a.s. i propisivanja namaza",
                        hijriDay = 27,
                        hijriMonth = 7,
                        hijriMonthName = "Redžeb",
                        isNightEve = true, // Countdown to 26th evening Maghrib
                        targetDate = date
                    )
                )
            }

            // 6. Lejletu-l-berat (14. na 15. Ša'ban)
            findGregorianForHijri(hYear, 8, 15, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "berat_$hYear",
                        name = "Lejletu-l-berat",
                        description = "15. Ša'ban – Noć oprosta i sudbine",
                        hijriDay = 15,
                        hijriMonth = 8,
                        hijriMonthName = "Ša'ban",
                        isNightEve = true, // Countdown to 14th evening Maghrib
                        targetDate = date
                    )
                )
            }

            // 7. Početak Ramazana
            findGregorianForHijri(hYear, 9, 1, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "ramazan_start_$hYear",
                        name = "Prvi dan posta – Ramazan",
                        description = "1. Ramazan – Početak mubarek mjeseca posta",
                        hijriDay = 1,
                        hijriMonth = 9,
                        hijriMonthName = "Ramazan",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 8. Dan pobjede na Bedru / Lejletul-Bedr
            findGregorianForHijri(hYear, 9, 17, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "bedr_$hYear",
                        name = "Lejletul-Bedr (Dan pobjede na Bedru)",
                        description = "17. Ramazan – Sjećanje na bitku na Bedru",
                        hijriDay = 17,
                        hijriMonth = 9,
                        hijriMonthName = "Ramazan",
                        isNightEve = true,
                        targetDate = date
                    )
                )
            }

            // 9. Lejletu-l-kadr (26. na 27. Ramazan)
            findGregorianForHijri(hYear, 9, 27, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "kadr_$hYear",
                        name = "Lejletu-l-kadr",
                        description = "27. Ramazan – Noć objave Kur'ana, bolja od hiljadu mjeseci",
                        hijriDay = 27,
                        hijriMonth = 9,
                        hijriMonthName = "Ramazan",
                        isNightEve = true, // Countdown to 26th evening Maghrib
                        targetDate = date
                    )
                )
            }

            // 10. Ramazanski bajram
            findGregorianForHijri(hYear, 10, 1, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "ramazanski_bajram_$hYear",
                        name = "Ramazanski bajram (1. dan)",
                        description = "1. Ševval – Praznik završetka posta",
                        hijriDay = 1,
                        hijriMonth = 10,
                        hijriMonthName = "Ševval",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 11. Dan šehida (2. dan Ramazanskog bajrama)
            findGregorianForHijri(hYear, 10, 2, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "dan_sehida_$hYear",
                        name = "Dan šehida (2. dan Bajrama)",
                        description = "2. Ševval – Dan sjećanja na šehide",
                        hijriDay = 2,
                        hijriMonth = 10,
                        hijriMonthName = "Ševval",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 12. Dan Arefata
            findGregorianForHijri(hYear, 12, 9, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "arefat_$hYear",
                        name = "Dan Arefata",
                        description = "9. Zu-l-hidždže – Dan stajanja na Arefatu i uoči Kurban-bajrama",
                        hijriDay = 9,
                        hijriMonth = 12,
                        hijriMonthName = "Zu-l-hidždže",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }

            // 13. Kurban-bajram
            findGregorianForHijri(hYear, 12, 10, dayOffset)?.let { date ->
                result.add(
                    Holiday(
                        id = "kurban_bajram_$hYear",
                        name = "Kurban-bajram (1. dan)",
                        description = "10. Zu-l-hidždže – Hadžijski bajram i prinošenje kurbana",
                        hijriDay = 10,
                        hijriMonth = 12,
                        hijriMonthName = "Zu-l-hidždže",
                        isNightEve = false,
                        targetDate = date
                    )
                )
            }
        }

        // Return future holidays sorted by target date
        return result
            .filter { it.targetDate.isAfter(currentDate.minusDays(1)) }
            .sortedBy { it.targetDate }
    }
}
