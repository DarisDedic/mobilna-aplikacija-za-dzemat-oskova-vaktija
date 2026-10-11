package com.example.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical solar calculation for Islamic prayer times used as fallback
 * when neither network nor cached times from vaktija.ba are available.
 */
object AstronomicalPrayerCalculator {

    private val SARAJEVO_ZONE = ZoneId.of("Europe/Sarajevo")

    fun calculate(date: LocalDate, location: LocationInfo): DailyPrayerSchedule {
        val lat = location.latitude
        val lon = location.longitude

        val julianDay = toJulianDay(date)
        val d = julianDay - 2451545.0

        // Mean anomaly of the Sun
        val g = fixAngle(357.529 + 0.98560028 * d)
        // Mean longitude of the Sun
        val q = fixAngle(280.459 + 0.98564736 * d)
        // Geocentric apparent ecliptic longitude
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        // Mean obliquity of the ecliptic
        val e = 23.439 - 0.00000036 * d
        // Sun's declination
        val sinDec = sin(Math.toRadians(e)) * sin(Math.toRadians(l))
        val dec = Math.toDegrees(asin(sinDec))

        // Right ascension
        val ra = fixAngle(Math.toDegrees(atan(cos(Math.toRadians(e)) * tan(Math.toRadians(l)))) )
        // Equation of Time (in hours)
        var eqt = (q / 15.0) - (ra / 15.0)
        while (eqt > 1) eqt -= 24
        while (eqt < -1) eqt += 24

        // Solar noon in UTC hours
        val noonUtc = 12.0 - (lon / 15.0) - eqt

        // Solar depression angles:
        // Fajr (Zora): 18.0 deg
        val fajrUtc = noonUtc - hourAngle(18.0, lat, dec) / 15.0
        // Sunrise (Izlazak): 0.833 deg
        val sunriseUtc = noonUtc - hourAngle(0.833, lat, dec) / 15.0
        // Sunset (Akšam): 0.833 deg
        val sunsetUtc = noonUtc + hourAngle(0.833, lat, dec) / 15.0
        // Isha (Jacija): 17.0 deg
        val ishaUtc = noonUtc + hourAngle(17.0, lat, dec) / 15.0
        // Asr (Ikindija): standard shadow factor 1
        val asrAngle = -Math.toDegrees(atan(1.0 + tan(Math.toRadians(Math.abs(lat - dec)))) )
        val asrUtc = noonUtc + hourAngle(-asrAngle, lat, dec) / 15.0

        // Convert UTC hours to local time in Europe/Sarajevo
        val zdt = date.atStartOfDay(SARAJEVO_ZONE)
        val offsetHours = zdt.offset.totalSeconds / 3600.0

        val fajrTime = hoursToLocalTime(fajrUtc + offsetHours)
        val sunriseTime = hoursToLocalTime(sunriseUtc + offsetHours)
        val dhuhrTime = hoursToLocalTime(noonUtc + offsetHours)
        val asrTime = hoursToLocalTime(asrUtc + offsetHours)
        val maghribTime = hoursToLocalTime(sunsetUtc + offsetHours)
        val ishaTime = hoursToLocalTime(ishaUtc + offsetHours)

        val prayers = listOf(
            PrayerItem(PrayerType.FAJR, fajrTime, formatTime(fajrTime)),
            PrayerItem(PrayerType.SUNRISE, sunriseTime, formatTime(sunriseTime)),
            PrayerItem(PrayerType.DHUHR, dhuhrTime, formatTime(dhuhrTime)),
            PrayerItem(PrayerType.ASR, asrTime, formatTime(asrTime)),
            PrayerItem(PrayerType.MAGHRIB, maghribTime, formatTime(maghribTime)),
            PrayerItem(PrayerType.ISHA, ishaTime, formatTime(ishaTime))
        )

        return DailyPrayerSchedule(
            locationId = location.id,
            locationName = location.name,
            date = date,
            prayers = prayers,
            isApproximate = true,
            sourceNote = "Približni astronomski proračun"
        )
    }

    private fun hourAngle(alpha: Double, lat: Double, dec: Double): Double {
        val cosHourAngle = (-sin(Math.toRadians(alpha)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(dec))) /
                (cos(Math.toRadians(lat)) * cos(Math.toRadians(dec)))
        val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun toJulianDay(date: LocalDate): Double {
        var year = date.year
        var month = date.monthValue
        val day = date.dayOfMonth
        if (month <= 2) {
            year -= 1
            month += 12
        }
        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5
    }

    private fun hoursToLocalTime(hours: Double): LocalTime {
        var h = hours % 24.0
        if (h < 0) h += 24.0
        val hour = h.toInt().coerceIn(0, 23)
        val minutes = ((h - hour) * 60).toInt().coerceIn(0, 59)
        return LocalTime.of(hour, minutes)
    }

    private fun formatTime(t: LocalTime): String {
        return String.format(java.util.Locale.US, "%02d:%02d", t.hour, t.minute)
    }
}
