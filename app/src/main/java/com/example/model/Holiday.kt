package com.example.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class Holiday(
    val id: String,
    val name: String,
    val description: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriMonthName: String,
    val isNightEve: Boolean, // True for Lejletu-r-regaib, Mi'radž, Berat, Kadr (counts down to previous day Maghrib)
    val targetDate: LocalDate,
    val targetTime: LocalTime = LocalTime.MIDNIGHT
) {
    fun remainingTimeDescription(now: ZonedDateTime, maghribTime: LocalTime? = null): String {
        val targetZone = ZoneId.of("Europe/Sarajevo")
        val nowInTarget = now.withZoneSameInstant(targetZone)
        
        val targetDateTime = if (isNightEve) {
            val eveMaghrib = maghribTime ?: LocalTime.of(18, 0)
            targetDate.minusDays(1).atTime(eveMaghrib).atZone(targetZone)
        } else {
            targetDate.atStartOfDay(targetZone)
        }

        if (nowInTarget.isAfter(targetDateTime)) {
            val endOfHoliday = targetDate.plusDays(1).atStartOfDay(targetZone)
            return if (nowInTarget.isBefore(endOfHoliday)) {
                "Danas"
            } else {
                "Prošlo"
            }
        }

        val totalSeconds = ChronoUnit.SECONDS.between(nowInTarget, targetDateTime)
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            days > 0 -> "za $days d $hours h"
            hours > 0 -> "za $hours h $minutes m"
            else -> "za $minutes min"
        }
    }
}
