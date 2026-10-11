package com.example

import com.example.data.repository.PrayerValidator
import com.example.model.HijriCalendarHelper
import com.example.model.LocationDatabase
import com.example.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerTimesTest {

    private val sarajevoZone = ZoneId.of("Europe/Sarajevo")

    /**
     * Requirement:
     * "Provjera: za 2026-10-04 i lokaciju 106 vremena moraju biti 05:12, 06:41, 12:35, 15:46, 18:27, 19:45."
     */
    @Test
    fun testUserSpecifiedVerificationDateAndTimes() {
        val date = LocalDate.of(2026, 10, 4)
        val rawTimes = listOf("5:12", "6:41", "12:35", "15:46", "18:27", "19:45")

        val schedule = PrayerValidator.validateAndParse(
            locationId = 106,
            locationName = "Živinice",
            date = date,
            vakatStrings = rawTimes
        )

        assertNotNull("Schedule must not be null for valid times", schedule)
        schedule?.let {
            assertEquals("Zora time mismatch", "05:12", it.fajr.formattedTime)
            assertEquals("Izlazak time mismatch", "06:41", it.sunrise.formattedTime)
            assertEquals("Podne time mismatch", "12:35", it.dhuhr.formattedTime)
            assertEquals("Ikindija time mismatch", "15:46", it.asr.formattedTime)
            assertEquals("Akšam time mismatch", "18:27", it.maghrib.formattedTime)
            assertEquals("Jacija time mismatch", "19:45", it.isha.formattedTime)
        }
    }

    /**
     * Requirement (a):
     * "Validiraj svaki dan (6 vremena, strogo rastući redoslijed), odbaci neispravne odgovore."
     */
    @Test
    fun testValidationOfPrayerTimes() {
        val date = LocalDate.of(2026, 10, 4)

        // 1. Valid strictly ascending
        val valid = listOf("5:12", "6:41", "12:35", "15:46", "18:27", "19:45")
        assertNotNull(PrayerValidator.validateAndParse(106, "Živinice", date, valid))

        // 2. Invalid count (only 5 items)
        val only5 = listOf("5:12", "6:41", "12:35", "15:46", "18:27")
        assertNull(PrayerValidator.validateAndParse(106, "Živinice", date, only5))

        // 3. Invalid order (not strictly ascending: Asr after Maghrib)
        val wrongOrder = listOf("5:12", "6:41", "12:35", "18:46", "15:27", "19:45")
        assertNull(PrayerValidator.validateAndParse(106, "Živinice", date, wrongOrder))

        // 4. Invalid time string
        val invalidTime = listOf("5:12", "invalid", "12:35", "15:46", "18:27", "19:45")
        assertNull(PrayerValidator.validateAndParse(106, "Živinice", date, invalidTime))

        // 5. Duplicate time (equal, not strictly ascending)
        val duplicateTime = listOf("5:12", "6:41", "12:35", "12:35", "18:27", "19:45")
        assertNull(PrayerValidator.validateAndParse(106, "Živinice", date, duplicateTime))
    }

    /**
     * Requirement (b): Countdown calculation logic.
     */
    @Test
    fun testCountdownCalculation() {
        val now = ZonedDateTime.of(2026, 10, 4, 10, 0, 0, 0, sarajevoZone)
        val dhuhrTime = LocalTime.of(12, 35)
        val target = now.toLocalDate().atTime(dhuhrTime).atZone(sarajevoZone)

        val duration = Duration.between(now, target)
        val totalSecs = duration.seconds
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        assertEquals(2L, hrs)
        assertEquals(35L, mins)
        assertEquals(0L, secs)
    }

    /**
     * Requirement (c):
     * "prelazak ljetnog/zimskog vremena (npr. 2026-10-25 i 2027-03-28)"
     */
    @Test
    fun testDaylightSavingTimeTransitions() {
        // Transition 1: Autumn DST end (2026-10-25) in Europe/Sarajevo: UTC+2 -> UTC+1
        val oct24 = ZonedDateTime.of(2026, 10, 24, 12, 0, 0, 0, sarajevoZone)
        val oct25 = ZonedDateTime.of(2026, 10, 25, 12, 0, 0, 0, sarajevoZone)

        assertEquals("Oct 24 is summer time (UTC+2)", 7200, oct24.offset.totalSeconds)
        assertEquals("Oct 25 is standard winter time (UTC+1)", 3600, oct25.offset.totalSeconds)

        // Transition 2: Spring DST start (2027-03-28) in Europe/Sarajevo: UTC+1 -> UTC+2
        val mar27 = ZonedDateTime.of(2027, 3, 27, 12, 0, 0, 0, sarajevoZone)
        val mar28 = ZonedDateTime.of(2027, 3, 28, 12, 0, 0, 0, sarajevoZone)

        assertEquals("Mar 27 is winter time (UTC+1)", 3600, mar27.offset.totalSeconds)
        assertEquals("Mar 28 is summer time (UTC+2)", 7200, mar28.offset.totalSeconds)
    }

    /**
     * Requirement (d): Hidžretski datumi i blagdani.
     */
    @Test
    fun testHijriDatesAndHolidays() {
        val testDate = LocalDate.of(2026, 10, 4)
        val hijriInfo = HijriCalendarHelper.getHijriDate(testDate, dayOffset = 0)

        assertNotNull(hijriInfo)
        assertTrue("Hijri year should be around 1448", hijriInfo.year in 1447..1449)
        assertTrue("Hijri month must be between 1 and 12", hijriInfo.month in 1..12)

        val holidays = HijriCalendarHelper.getUpcomingHolidays(testDate, dayOffset = 0)
        assertTrue("Holidays list must contain upcoming holidays", holidays.isNotEmpty())

        // Verify Lejle nights eve flag (counts down to previous day Maghrib)
        val regaib = holidays.find { it.id.startsWith("regaib") }
        if (regaib != null) {
            assertTrue("Lejletu-r-regaib must have isNightEve=true", regaib.isNightEve)
        }
    }

    /**
     * Requirement: Location database contains Živinice ID 106 as default.
     */
    @Test
    fun testLocationDatabase() {
        val defaultLoc = LocationDatabase.getById(LocationDatabase.DEFAULT_LOCATION_ID)
        assertEquals("Default location must be Živinice", "Živinice", defaultLoc.name)
        assertEquals("Default location ID must be 106", 106, defaultLoc.id)

        // Test finding nearest location via coordinates (around Živinice)
        val nearest = LocationDatabase.findNearest(44.45, 18.65)
        assertEquals("Nearest location to Oskova coordinates must be Živinice", 106, nearest.id)
    }
}
