package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerItem
import com.example.model.PrayerType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Entity(tableName = "prayer_cache")
data class PrayerEntity(
    @PrimaryKey
    val key: String, // format: "$locationId-$dateString" e.g. "106-2026-10-04"
    val locationId: Int,
    val locationName: String,
    val dateString: String, // "yyyy-MM-dd"
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toDailyPrayerSchedule(): DailyPrayerSchedule {
        val date = LocalDate.parse(dateString)
        val prayers = listOf(
            PrayerItem(PrayerType.FAJR, parseTime(fajr), fajr),
            PrayerItem(PrayerType.SUNRISE, parseTime(sunrise), sunrise),
            PrayerItem(PrayerType.DHUHR, parseTime(dhuhr), dhuhr),
            PrayerItem(PrayerType.ASR, parseTime(asr), asr),
            PrayerItem(PrayerType.MAGHRIB, parseTime(maghrib), maghrib),
            PrayerItem(PrayerType.ISHA, parseTime(isha), isha)
        )
        return DailyPrayerSchedule(
            locationId = locationId,
            locationName = locationName,
            date = date,
            prayers = prayers,
            isApproximate = false,
            sourceNote = "vaktija.ba (keš)"
        )
    }

    private fun parseTime(s: String): LocalTime {
        val parts = s.split(":")
        val h = parts[0].trim().toInt()
        val m = parts[1].trim().toInt()
        return LocalTime.of(h, m)
    }

    companion object {
        fun makeKey(locationId: Int, date: LocalDate): String =
            "$locationId-${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"

        fun fromDailyPrayerSchedule(schedule: DailyPrayerSchedule): PrayerEntity {
            val dateStr = schedule.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            return PrayerEntity(
                key = "${schedule.locationId}-$dateStr",
                locationId = schedule.locationId,
                locationName = schedule.locationName,
                dateString = dateStr,
                fajr = schedule.fajr.formattedTime,
                sunrise = schedule.sunrise.formattedTime,
                dhuhr = schedule.dhuhr.formattedTime,
                asr = schedule.asr.formattedTime,
                maghrib = schedule.maghrib.formattedTime,
                isha = schedule.isha.formattedTime
            )
        }
    }
}
