package com.example.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Enumeration of the 6 Islamic daily prayer times in Bosnia and Herzegovina.
 */
enum class PrayerType(
    val id: Int,
    val bosnianName: String,
    val arabicName: String,
    val description: String
) {
    FAJR(0, "Zora", "الفجر", "Vrijeme od zore do izlaska sunca"),
    SUNRISE(1, "Izlazak sunca", "الشروق", "Vrijeme izlaska sunca"),
    DHUHR(2, "Podne", "الظهر", "Početak podnevskog namaza"),
    ASR(3, "Ikindija", "العصر", "Početak poslijepodnevnog namaza"),
    MAGHRIB(4, "Akšam", "المغرب", "Vrijeme zalaska sunca i iftara"),
    ISHA(5, "Jacija", "العشاء", "Početak noćnog namaza");

    companion object {
        fun fromId(id: Int): PrayerType = entries.find { it.id == id } ?: DHUHR
    }
}

/**
 * Single prayer entry with exact time and date.
 */
data class PrayerItem(
    val type: PrayerType,
    val time: LocalTime,
    val formattedTime: String // e.g. "05:12" or "12:35"
)

/**
 * Complete day prayer schedule with validation and status calculation.
 */
data class DailyPrayerSchedule(
    val locationId: Int,
    val locationName: String,
    val date: LocalDate,
    val prayers: List<PrayerItem>,
    val isApproximate: Boolean = false,
    val sourceNote: String = "vaktija.ba"
) {
    init {
        require(prayers.size == 6) { "Dnevna vaktija mora imati tačno 6 vremena namaza." }
    }

    val fajr: PrayerItem get() = prayers[0]
    val sunrise: PrayerItem get() = prayers[1]
    val dhuhr: PrayerItem get() = prayers[2]
    val asr: PrayerItem get() = prayers[3]
    val maghrib: PrayerItem get() = prayers[4]
    val isha: PrayerItem get() = prayers[5]

    /**
     * Determines which prayer is the next upcoming prayer based on current time in Europe/Sarajevo.
     */
    fun getNextPrayer(now: ZonedDateTime): PrayerItem? {
        val todayTime = now.toLocalTime()
        return prayers.firstOrNull { it.time.isAfter(todayTime) }
    }

    /**
     * Formatted gregorian date string in Bosnian, e.g. "Nedjelja, 11. oktobar 2026."
     */
    fun formattedDateBosnian(): String {
        val days = arrayOf("Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja")
        val months = arrayOf(
            "januar", "februar", "mart", "april", "maj", "juni",
            "juli", "august", "septembar", "oktobar", "novembar", "decembar"
        )
        val dayName = days[date.dayOfWeek.value - 1]
        val monthName = months[date.monthValue - 1]
        return "$dayName, ${date.dayOfMonth}. $monthName ${date.year}."
    }
}
