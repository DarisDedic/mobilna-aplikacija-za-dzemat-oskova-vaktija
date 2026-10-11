package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PrayerEntity
import com.example.data.remote.VaktijaApiService
import com.example.model.AstronomicalPrayerCalculator
import com.example.model.DailyPrayerSchedule
import com.example.model.LocationDatabase
import com.example.model.LocationInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class PrayerRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val prayerDao = db.prayerDao()

    private val apiMutex = Mutex()
    private var lastRequestTimestamp: Long = 0L

    // Backoff state: 15 min, 30 min, 1h, 2h, 4h, max 6h
    private var backoffAttemptCount = 0
    private var backoffUntilTimestamp: Long = 0L

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val apiService: VaktijaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.vaktija.ba/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VaktijaApiService::class.java)
    }

    /**
     * Gets prayer schedule for a specific date and location.
     * Order of resolution:
     * 1. Room local cache (instant, offline)
     * 2. Remote network API (if not in backoff)
     * 3. Astronomical solar approximation (guaranteed fallback)
     */
    suspend fun getPrayerSchedule(
        date: LocalDate,
        locationId: Int = LocationDatabase.DEFAULT_LOCATION_ID
    ): DailyPrayerSchedule = withContext(Dispatchers.IO) {
        val location = LocationDatabase.getById(locationId)
        val key = PrayerEntity.makeKey(locationId, date)

        // 1. Check local Room database
        val cached = prayerDao.getByKey(key)
        if (cached != null) {
            return@withContext cached.toDailyPrayerSchedule()
        }

        // 2. Try fetching from network if allowed by backoff
        val now = System.currentTimeMillis()
        if (now >= backoffUntilTimestamp) {
            val fetched = fetchDayFromNetwork(date, location)
            if (fetched != null) {
                prayerDao.insert(PrayerEntity.fromDailyPrayerSchedule(fetched))
                // Also trigger background pre-cache of 60+ days if cache is low
                triggerPrecacheIfNeeded(date, locationId)
                return@withContext fetched
            }
        }

        // 3. Fallback to astronomical calculation
        AstronomicalPrayerCalculator.calculate(date, location)
    }

    /**
     * Pre-fetches at least 60 days ahead (current month + next 2 months) into Room database.
     * Runs with rate-limiting (>= 1000ms between calls) and backoff handling.
     */
    suspend fun prefetch60Days(locationId: Int): Boolean = withContext(Dispatchers.IO) {
        val location = LocationDatabase.getById(locationId)
        val today = LocalDate.now()
        val currentYm = YearMonth.of(today.year, today.month)

        val monthsToFetch = listOf(
            currentYm,
            currentYm.plusMonths(1),
            currentYm.plusMonths(2)
        )

        var anySuccess = false
        for (ym in monthsToFetch) {
            val now = System.currentTimeMillis()
            if (now < backoffUntilTimestamp) {
                Log.d("PrayerRepo", "Network in backoff mode until $backoffUntilTimestamp")
                break
            }

            val schedules = fetchMonthFromNetwork(ym.year, ym.monthValue, location)
            if (schedules != null && schedules.isNotEmpty()) {
                val entities = schedules.map { PrayerEntity.fromDailyPrayerSchedule(it) }
                prayerDao.insertAll(entities)
                anySuccess = true
                resetBackoff()
            } else {
                registerFailure()
                break
            }
        }
        anySuccess
    }

    private suspend fun triggerPrecacheIfNeeded(today: LocalDate, locationId: Int) {
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val count = prayerDao.countFutureCachedDays(locationId, todayStr)
        if (count < 30) {
            try {
                prefetch60Days(locationId)
            } catch (e: Exception) {
                Log.w("PrayerRepo", "Background precache deferred: ${e.message}")
            }
        }
    }

    private suspend fun fetchDayFromNetwork(date: LocalDate, location: LocationInfo): DailyPrayerSchedule? {
        enforceRateLimit()
        return try {
            val response = apiService.getSpecificDay(location.id, date.year, date.monthValue, date.dayOfMonth)
            if (response.isSuccessful) {
                val body = response.body()
                resetBackoff()
                PrayerValidator.validateAndParse(
                    locationId = location.id,
                    locationName = location.name,
                    date = date,
                    vakatStrings = body?.vakat,
                    sourceNote = "vaktija.ba"
                )
            } else {
                handleHttpError(response.code())
                null
            }
        } catch (e: Exception) {
            Log.e("PrayerRepo", "fetchDay network failure: ${e.message}")
            registerFailure()
            null
        }
    }

    private suspend fun fetchMonthFromNetwork(
        year: Int,
        month: Int,
        location: LocationInfo
    ): List<DailyPrayerSchedule>? {
        enforceRateLimit()
        return try {
            val response = apiService.getMonth(location.id, year, month)
            if (response.isSuccessful) {
                val body = response.body()
                val daysList = body?.dan ?: return null
                val result = mutableListOf<DailyPrayerSchedule>()

                daysList.forEachIndexed { index, dayItem ->
                    val dayNum = index + 1
                    try {
                        val date = LocalDate.of(year, month, dayNum)
                        val validated = PrayerValidator.validateAndParse(
                            locationId = location.id,
                            locationName = location.name,
                            date = date,
                            vakatStrings = dayItem.vakat,
                            sourceNote = "vaktija.ba"
                        )
                        if (validated != null) {
                            result.add(validated)
                        }
                    } catch (_: Exception) {
                        // Skip invalid day
                    }
                }
                resetBackoff()
                result
            } else {
                handleHttpError(response.code())
                null
            }
        } catch (e: Exception) {
            Log.e("PrayerRepo", "fetchMonth network failure: ${e.message}")
            registerFailure()
            null
        }
    }

    /**
     * Enforces strict rate limit of at most 1 request per second.
     */
    private suspend fun enforceRateLimit() {
        apiMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestTimestamp
            if (elapsed < 1100L) {
                val waitTime = 1100L - elapsed
                delay(waitTime)
            }
            lastRequestTimestamp = System.currentTimeMillis()
        }
    }

    private fun handleHttpError(code: Int) {
        Log.w("PrayerRepo", "HTTP error received: $code")
        registerFailure()
    }

    /**
     * Exponential backoff on error or HTTP 429:
     * 15 min, 30 min, 1h, 2h, 4h, max 6h.
     */
    private fun registerFailure() {
        backoffAttemptCount++
        val backoffMinutes = when (backoffAttemptCount) {
            1 -> 15L
            2 -> 30L
            3 -> 60L
            4 -> 120L
            5 -> 240L
            else -> 360L // Max 6 hours
        }
        backoffUntilTimestamp = System.currentTimeMillis() + (backoffMinutes * 60 * 1000L)
        Log.w("PrayerRepo", "Backing off network requests for $backoffMinutes minutes (attempt #$backoffAttemptCount)")
    }

    private fun resetBackoff() {
        backoffAttemptCount = 0
        backoffUntilTimestamp = 0L
    }

    suspend fun getCachedDaysCount(locationId: Int): Int = withContext(Dispatchers.IO) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        prayerDao.countFutureCachedDays(locationId, todayStr)
    }
}
