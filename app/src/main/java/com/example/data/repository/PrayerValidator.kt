package com.example.data.repository

import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerItem
import com.example.model.PrayerType
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object PrayerValidator {

    /**
     * Validates that the list contains exactly 6 times, all valid HH:mm or H:mm format,
     * and strictly ascending throughout the 24-hour day.
     * Returns a valid DailyPrayerSchedule or null if invalid.
     */
    fun validateAndParse(
        locationId: Int,
        locationName: String,
        date: LocalDate,
        vakatStrings: List<String>?,
        sourceNote: String = "vaktija.ba"
    ): DailyPrayerSchedule? {
        if (vakatStrings == null || vakatStrings.size != 6) {
            return null
        }

        val parsedTimes = mutableListOf<LocalTime>()
        for (raw in vakatStrings) {
            val trimmed = raw.trim()
            val time = parseSingleTime(trimmed) ?: return null
            parsedTimes.add(time)
        }

        // Check strictly ascending order:
        // Zora < Izlazak < Podne < Ikindija < Akšam < Jacija
        for (i in 0 until parsedTimes.size - 1) {
            if (!parsedTimes[i].isBefore(parsedTimes[i + 1])) {
                return null // Rejected: not strictly ascending
            }
        }

        val items = parsedTimes.mapIndexed { index, time ->
            PrayerItem(
                type = PrayerType.fromId(index),
                time = time,
                formattedTime = formatTime(time)
            )
        }

        return DailyPrayerSchedule(
            locationId = locationId,
            locationName = locationName,
            date = date,
            prayers = items,
            isApproximate = false,
            sourceNote = sourceNote
        )
    }

    private fun parseSingleTime(str: String): LocalTime? {
        return try {
            val parts = str.split(":")
            if (parts.size != 2) return null
            val hour = parts[0].trim().toInt()
            val minute = parts[1].trim().toInt()
            if (hour in 0..23 && minute in 0..59) {
                LocalTime.of(hour, minute)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun formatTime(t: LocalTime): String {
        return String.format(Locale.US, "%02d:%02d", t.hour, t.minute)
    }
}
