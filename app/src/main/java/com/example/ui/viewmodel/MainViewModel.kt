package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.VaktijaApp
import com.example.data.repository.UserPreferences
import com.example.model.DailyPrayerSchedule
import com.example.model.HijriCalendarHelper
import com.example.model.HijriDateInfo
import com.example.model.Holiday
import com.example.model.LocationDatabase
import com.example.model.LocationInfo
import com.example.model.PrayerItem
import com.example.model.PrayerType
import com.example.notification.NotificationScheduler
import com.example.updater.UpdateInfo
import com.example.updater.UpdateState
import com.example.widget.VaktijaWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val app = application as VaktijaApp
    private val prayerRepo = app.prayerRepository
    private val prefsRepo = app.preferencesRepository

    private val SARAJEVO_ZONE = ZoneId.of("Europe/Sarajevo")

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _todaySchedule = MutableStateFlow<DailyPrayerSchedule?>(null)
    val todaySchedule: StateFlow<DailyPrayerSchedule?> = _todaySchedule.asStateFlow()

    private val _displaySchedule = MutableStateFlow<DailyPrayerSchedule?>(null)
    val displaySchedule: StateFlow<DailyPrayerSchedule?> = _displaySchedule.asStateFlow()

    private val _hijriInfo = MutableStateFlow<HijriDateInfo?>(null)
    val hijriInfo: StateFlow<HijriDateInfo?> = _hijriInfo.asStateFlow()

    private val _nextPrayer = MutableStateFlow<PrayerItem?>(null)
    val nextPrayer: StateFlow<PrayerItem?> = _nextPrayer.asStateFlow()

    private val _countdownString = MutableStateFlow("00:00:00")
    val countdownString: StateFlow<String> = _countdownString.asStateFlow()

    private val _nextHoliday = MutableStateFlow<Holiday?>(null)
    val nextHoliday: StateFlow<Holiday?> = _nextHoliday.asStateFlow()

    private val _holidaysList = MutableStateFlow<List<Holiday>>(emptyList())
    val holidaysList: StateFlow<List<Holiday>> = _holidaysList.asStateFlow()

    private val _cachedDaysCount = MutableStateFlow(0)
    val cachedDaysCount: StateFlow<Int> = _cachedDaysCount.asStateFlow()

    private val _isDifferentTimezone = MutableStateFlow(false)
    val isDifferentTimezone: StateFlow<Boolean> = _isDifferentTimezone.asStateFlow()

    private val _deviceZoneId = MutableStateFlow(ZoneId.systemDefault().id)
    val deviceZoneId: StateFlow<String> = _deviceZoneId.asStateFlow()

    // Ramadan countdowns
    private val _imsakCountdown = MutableStateFlow("")
    val imsakCountdown: StateFlow<String> = _imsakCountdown.asStateFlow()

    private val _iftarCountdown = MutableStateFlow("")
    val iftarCountdown: StateFlow<String> = _iftarCountdown.asStateFlow()

    // 30-day month schedules for Ramadan table
    private val _monthSchedules = MutableStateFlow<List<DailyPrayerSchedule>>(emptyList())
    val monthSchedules: StateFlow<List<DailyPrayerSchedule>> = _monthSchedules.asStateFlow()

    // Qibla Compass state
    private val _qiblaBearing = MutableStateFlow(0.0f) // Angle towards Kaaba relative to North
    val qiblaBearing: StateFlow<Float> = _qiblaBearing.asStateFlow()

    private val _deviceHeading = MutableStateFlow(0.0f) // Current device heading relative to North
    val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

    private val _sensorAccuracyWarning = MutableStateFlow(false)
    val sensorAccuracyWarning: StateFlow<Boolean> = _sensorAccuracyWarning.asStateFlow()

    val updateState: StateFlow<UpdateState> = app.updateManager.updateState

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null

    init {
        checkTimezone()
        observePreferences()
        startClockTicker()
        loadDataForSelectedDate()
        setupCompass()

        // Automatic update check if enabled in settings
        if (prefsRepo.userPreferencesFlow.value.autoCheckUpdates) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                val repo = userPreferences.value.githubRepo
                app.updateManager.checkForUpdates(repo, isManualCheck = false)
            }
        }
    }

    private fun checkTimezone() {
        val systemZone = ZoneId.systemDefault()
        _deviceZoneId.value = systemZone.id
        _isDifferentTimezone.value = (systemZone.id != SARAJEVO_ZONE.id && systemZone != SARAJEVO_ZONE)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.collect {
                loadDataForSelectedDate()
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadDataForSelectedDate()
    }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
        loadDataForSelectedDate()
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        loadDataForSelectedDate()
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        loadDataForSelectedDate()
    }

    fun loadDataForSelectedDate() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = userPreferences.value
            val date = _selectedDate.value
            val today = LocalDate.now()

            // 1. Load today's schedule for active prayer calculation
            val todaySched = prayerRepo.getPrayerSchedule(today, prefs.locationId)
            _todaySchedule.value = todaySched

            // 2. Load display schedule for the currently selected date
            val dispSched = if (date == today) todaySched else prayerRepo.getPrayerSchedule(date, prefs.locationId)
            _displaySchedule.value = dispSched

            // 3. Hijri Date & Holidays
            val hInfo = HijriCalendarHelper.getHijriDate(date, prefs.hijriOffset)
            _hijriInfo.value = hInfo

            val holidays = HijriCalendarHelper.getUpcomingHolidays(today, prefs.hijriOffset)
            _holidaysList.value = holidays
            _nextHoliday.value = holidays.firstOrNull()

            // 4. Cached days count
            _cachedDaysCount.value = prayerRepo.getCachedDaysCount(prefs.locationId)

            // 5. Update Qibla bearing for current location
            val loc = LocationDatabase.getById(prefs.locationId)
            _qiblaBearing.value = calculateQiblaBearing(loc.latitude, loc.longitude).toFloat()

            // 6. Reschedule alarms and update widgets
            NotificationScheduler.scheduleUpcomingPrayers(app, todaySched, prefs)
            VaktijaWidgetProvider.updateAllWidgets(app)
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                updateTick()
                delay(1000L)
            }
        }
    }

    private fun updateTick() {
        val sched = _todaySchedule.value ?: return
        val now = ZonedDateTime.now(SARAJEVO_ZONE)
        val today = now.toLocalDate()
        val currentTime = now.toLocalTime()

        // 1. Next prayer
        val next = sched.prayers.firstOrNull { it.time.isAfter(currentTime) } ?: sched.fajr
        _nextPrayer.value = next

        val targetDateTime = if (next.time.isBefore(currentTime)) {
            today.plusDays(1).atTime(next.time).atZone(SARAJEVO_ZONE)
        } else {
            today.atTime(next.time).atZone(SARAJEVO_ZONE)
        }

        val duration = Duration.between(now, targetDateTime)
        val totalSecs = duration.seconds.coerceAtLeast(0)
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        _countdownString.value = String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

        // 2. Ramadan countdowns (Imsak & Iftar)
        val prefs = userPreferences.value
        val imsakTime = sched.fajr.time.minusMinutes(prefs.imsakMinutesBefore.toLong())
        val iftarTime = sched.maghrib.time

        // Imsak countdown
        val imsakTarget = if (imsakTime.isBefore(currentTime)) {
            today.plusDays(1).atTime(imsakTime).atZone(SARAJEVO_ZONE)
        } else {
            today.atTime(imsakTime).atZone(SARAJEVO_ZONE)
        }
        val imsakDuration = Duration.between(now, imsakTarget)
        val imsakSecs = imsakDuration.seconds.coerceAtLeast(0)
        _imsakCountdown.value = formatDuration(imsakSecs)

        // Iftar countdown
        val iftarTarget = if (iftarTime.isBefore(currentTime)) {
            today.plusDays(1).atTime(iftarTime).atZone(SARAJEVO_ZONE)
        } else {
            today.atTime(iftarTime).atZone(SARAJEVO_ZONE)
        }
        val iftarDuration = Duration.between(now, iftarTarget)
        val iftarSecs = iftarDuration.seconds.coerceAtLeast(0)
        _iftarCountdown.value = formatDuration(iftarSecs)
    }

    private fun formatDuration(totalSeconds: Long): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    // Settings actions
    fun setLocation(locationId: Int) {
        prefsRepo.setLocationId(locationId)
        loadDataForSelectedDate()
    }

    fun setNearestLocation(lat: Double, lon: Double) {
        val nearest = LocationDatabase.findNearest(lat, lon)
        prefsRepo.setLocationId(nearest.id)
        loadDataForSelectedDate()
    }

    fun togglePrayerNotification(prayerType: PrayerType, enabled: Boolean) {
        prefsRepo.setPrayerNotification(prayerType, enabled)
    }

    fun setMinutesBefore(minutes: Int) {
        prefsRepo.setMinutesBefore(minutes)
    }

    fun setSoundType(type: String) {
        prefsRepo.setSoundType(type)
    }

    fun setThemeMode(mode: String) {
        prefsRepo.setThemeMode(mode)
    }

    fun setLargeText(enabled: Boolean) {
        prefsRepo.setLargeText(enabled)
    }

    fun setIs24HourFormat(is24h: Boolean) {
        prefsRepo.setIs24HourFormat(is24h)
    }

    fun setHijriOffset(offset: Int) {
        prefsRepo.setHijriOffset(offset)
    }

    fun setJumuahCustomTime(time: String?) {
        prefsRepo.setJumuahCustomTime(time)
    }

    fun setImsakMinutesBefore(minutes: Int) {
        prefsRepo.setImsakMinutesBefore(minutes)
    }

    fun updateKaza(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, witr: Int) {
        prefsRepo.updateKaza(fajr, dhuhr, asr, maghrib, isha, witr)
    }

    fun toggleSilentMode(enabled: Boolean) {
        prefsRepo.setSilentModeEnabled(enabled)
    }

    fun loadMonthSchedules(startDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = userPreferences.value
            val list = mutableListOf<DailyPrayerSchedule>()
            for (i in 0 until 30) {
                val d = startDate.plusDays(i.toLong())
                val s = prayerRepo.getPrayerSchedule(d, prefs.locationId)
                list.add(s)
            }
            _monthSchedules.value = list
        }
    }

    // Qibla calculation: Kaaba at lat 21.4225, lon 39.8262
    private fun calculateQiblaBearing(lat: Double, lon: Double): Double {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLon = Math.toRadians(39.8262)
        val userLat = Math.toRadians(lat)
        val userLon = Math.toRadians(lon)

        val dLon = kaabaLon - userLon
        val y = sin(dLon) * cos(kaabaLat)
        val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(dLon)
        var initialBearing = Math.toDegrees(atan2(y, x))
        initialBearing = (initialBearing + 360.0) % 360.0
        return initialBearing
    }

    private fun setupCompass() {
        try {
            sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            rotationSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (e: Exception) {
            Log.w("MainViewModel", "Compass setup error: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuthDegrees < 0) azimuthDegrees += 360.0f
            _deviceHeading.value = azimuthDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _sensorAccuracyWarning.value = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
    }

    // In-App Updater functions
    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            val repo = userPreferences.value.githubRepo
            app.updateManager.checkForUpdates(repo, isManualCheck = isManual)
        }
    }

    fun downloadAndInstallUpdate(context: Context, info: UpdateInfo) {
        viewModelScope.launch {
            app.updateManager.downloadAndInstall(context, info)
        }
    }

    fun retryInstall(context: Context, apkFile: java.io.File) {
        app.updateManager.promptInstall(context, apkFile)
    }

    fun dismissUpdateDialog() {
        app.updateManager.resetState()
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefsRepo.setAutoCheckUpdates(enabled)
    }

    fun setGithubRepo(repo: String) {
        prefsRepo.setGithubRepo(repo)
    }

    // "Zov na Sedždu" Prayer Alarm functions
    fun togglePrayerAlarm(target: com.example.model.AlarmTargetPrayer, enabled: Boolean) {
        prefsRepo.togglePrayerAlarm(target, enabled)
        rescheduleWakeAlarms()
    }

    fun setPrayerAlarmMinutesBefore(target: com.example.model.AlarmTargetPrayer, minutes: Int) {
        prefsRepo.setPrayerAlarmMinutesBefore(target, minutes)
        rescheduleWakeAlarms()
    }

    fun setPrayerAlarmSound(soundId: String) {
        prefsRepo.setPrayerAlarmSound(soundId)
        rescheduleWakeAlarms()
    }

    fun setPrayerAlarmVibrate(vibrate: Boolean) {
        prefsRepo.setPrayerAlarmVibrate(vibrate)
        rescheduleWakeAlarms()
    }

    fun setPrayerAlarmVolume(volume: Float) {
        prefsRepo.setPrayerAlarmVolume(volume)
        rescheduleWakeAlarms()
    }

    fun testAlarmNow(seconds: Int = 5) {
        val config = userPreferences.value.prayerAlarmConfig
        com.example.alarm.PrayerWakeAlarmScheduler.scheduleTestAlarm(
            context = app,
            seconds = seconds,
            soundId = config.soundId,
            vibrate = config.vibrate
        )
    }

    private fun rescheduleWakeAlarms() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = userPreferences.value
            val today = prayerRepo.getPrayerSchedule(LocalDate.now(), prefs.locationId)
            val tomorrow = prayerRepo.getPrayerSchedule(LocalDate.now().plusDays(1), prefs.locationId)
            com.example.alarm.PrayerWakeAlarmScheduler.scheduleAllAlarms(
                app,
                today,
                tomorrow,
                prefs.prayerAlarmConfig
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
