package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LocationDatabase
import com.example.model.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val DEFAULT_GITHUB_REPO = "DarisDedic/mobilna-aplikacija-za-dzemat-Oskova"
const val OLD_DEFAULT_GITHUB_REPO = "asif-omerbegovic/vaktija-oskova"
const val PREVIOUS_TEMP_GITHUB_REPO = "DarisDedic/mobilna-aplikacija-za-dzemat-oskova-vaktija"

data class UserPreferences(
    val locationId: Int = LocationDatabase.DEFAULT_LOCATION_ID,
    val prayerNotifications: Map<PrayerType, Boolean> = PrayerType.entries.associateWith { true },
    val minutesBefore: Int = 0, // 0, 5, 10, 15, 30
    val soundType: String = "SYSTEM", // "SYSTEM", "CHIME", "SILENT"
    val themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    val largeText: Boolean = false,
    val is24HourFormat: Boolean = true,
    val hijriOffset: Int = 0, // -1, 0, +1
    val jumuahCustomTime: String? = null, // null for vaktija dhuhr, or "12:00", "13:00"
    val imsakMinutesBefore: Int = 15, // minutes before Fajr for sehur/imsak alert
    val kazaFajr: Int = 0,
    val kazaDhuhr: Int = 0,
    val kazaAsr: Int = 0,
    val kazaMaghrib: Int = 0,
    val kazaIsha: Int = 0,
    val kazaWitr: Int = 0,
    val silentModeEnabled: Boolean = false,
    val silentModeDurationMinutes: Int = 20,
    val teravihMinutesAfterIsha: Int = 15,
    val autoCheckUpdates: Boolean = true,
    val githubRepo: String = DEFAULT_GITHUB_REPO,
    val prayerAlarmConfig: com.example.model.PrayerAlarmConfig = com.example.model.PrayerAlarmConfig()
)

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vaktija_preferences", Context.MODE_PRIVATE)

    private val _userPreferencesFlow = MutableStateFlow(loadPreferences())
    val userPreferencesFlow: StateFlow<UserPreferences> = _userPreferencesFlow.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val locationId = prefs.getInt(KEY_LOCATION_ID, LocationDatabase.DEFAULT_LOCATION_ID)
        val notifMap = mutableMapOf<PrayerType, Boolean>()
        for (type in PrayerType.entries) {
            notifMap[type] = prefs.getBoolean(KEY_NOTIF_PREFIX + type.name, true)
        }
        val minutesBefore = prefs.getInt(KEY_MINUTES_BEFORE, 0)
        val soundType = prefs.getString(KEY_SOUND_TYPE, "SYSTEM") ?: "SYSTEM"
        val themeMode = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        val largeText = prefs.getBoolean(KEY_LARGE_TEXT, false)
        val is24h = prefs.getBoolean(KEY_24H, true)
        val hijriOffset = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        val jumuahTime = prefs.getString(KEY_JUMUAH_TIME, null)
        val imsakBefore = prefs.getInt(KEY_IMSAK_BEFORE, 15)
        val kFajr = prefs.getInt(KEY_KAZA_FAJR, 0)
        val kDhuhr = prefs.getInt(KEY_KAZA_DHUHR, 0)
        val kAsr = prefs.getInt(KEY_KAZA_ASR, 0)
        val kMaghrib = prefs.getInt(KEY_KAZA_MAGHRIB, 0)
        val kIsha = prefs.getInt(KEY_KAZA_ISHA, 0)
        val kWitr = prefs.getInt(KEY_KAZA_WITR, 0)
        val silentEnabled = prefs.getBoolean(KEY_SILENT_ENABLED, false)
        val silentDuration = prefs.getInt(KEY_SILENT_DURATION, 20)
        val teravihAfter = prefs.getInt(KEY_TERAVIH_AFTER, 15)
        val autoUpdates = prefs.getBoolean(KEY_AUTO_UPDATES, true)
        val rawRepo = prefs.getString(KEY_GITHUB_REPO, DEFAULT_GITHUB_REPO) ?: DEFAULT_GITHUB_REPO
        val repo = if (rawRepo.isBlank() || rawRepo == OLD_DEFAULT_GITHUB_REPO || rawRepo == PREVIOUS_TEMP_GITHUB_REPO) {
            DEFAULT_GITHUB_REPO
        } else {
            rawRepo
        }

        // Load Prayer Alarm ("Zov na Sedždu") settings
        val alarmMap = mutableMapOf<com.example.model.AlarmTargetPrayer, com.example.model.SingleAlarmSetting>()
        for (target in com.example.model.AlarmTargetPrayer.entries) {
            val isEnabled = prefs.getBoolean(
                KEY_ALARM_ENABLED_PREFIX + target.id,
                target == com.example.model.AlarmTargetPrayer.FAJR
            )
            val minBefore = prefs.getInt(
                KEY_ALARM_MINUTES_PREFIX + target.id,
                target.defaultMinutesBefore
            )
            alarmMap[target] = com.example.model.SingleAlarmSetting(
                target = target,
                isEnabled = isEnabled,
                minutesBefore = minBefore
            )
        }
        val alarmSoundId = prefs.getString(KEY_ALARM_SOUND_ID, com.example.audio.PrayerSoundManager.SOUND_EZAN)
            ?: com.example.audio.PrayerSoundManager.SOUND_EZAN
        val alarmVibrate = prefs.getBoolean(KEY_ALARM_VIBRATE, true)
        val alarmVolume = prefs.getFloat(KEY_ALARM_VOLUME, 1.0f)
        val alarmSnooze = prefs.getInt(KEY_ALARM_SNOOZE, 5)

        val alarmConfig = com.example.model.PrayerAlarmConfig(
            alarms = alarmMap,
            soundId = alarmSoundId,
            vibrate = alarmVibrate,
            volume = alarmVolume,
            snoozeMinutes = alarmSnooze
        )

        return UserPreferences(
            locationId = locationId,
            prayerNotifications = notifMap,
            minutesBefore = minutesBefore,
            soundType = soundType,
            themeMode = themeMode,
            largeText = largeText,
            is24HourFormat = is24h,
            hijriOffset = hijriOffset,
            jumuahCustomTime = jumuahTime,
            imsakMinutesBefore = imsakBefore,
            kazaFajr = kFajr,
            kazaDhuhr = kDhuhr,
            kazaAsr = kAsr,
            kazaMaghrib = kMaghrib,
            kazaIsha = kIsha,
            kazaWitr = kWitr,
            silentModeEnabled = silentEnabled,
            silentModeDurationMinutes = silentDuration,
            teravihMinutesAfterIsha = teravihAfter,
            autoCheckUpdates = autoUpdates,
            githubRepo = repo,
            prayerAlarmConfig = alarmConfig
        )
    }

    fun setLocationId(id: Int) {
        prefs.edit().putInt(KEY_LOCATION_ID, id).apply()
        updateState()
    }

    fun setPrayerNotification(type: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_PREFIX + type.name, enabled).apply()
        updateState()
    }

    fun setMinutesBefore(minutes: Int) {
        prefs.edit().putInt(KEY_MINUTES_BEFORE, minutes).apply()
        updateState()
    }

    fun setSoundType(type: String) {
        prefs.edit().putString(KEY_SOUND_TYPE, type).apply()
        updateState()
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        updateState()
    }

    fun setLargeText(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
        updateState()
    }

    fun setIs24HourFormat(is24h: Boolean) {
        prefs.edit().putBoolean(KEY_24H, is24h).apply()
        updateState()
    }

    fun setHijriOffset(offset: Int) {
        prefs.edit().putInt(KEY_HIJRI_OFFSET, offset).apply()
        updateState()
    }

    fun setJumuahCustomTime(time: String?) {
        prefs.edit().putString(KEY_JUMUAH_TIME, time).apply()
        updateState()
    }

    fun setImsakMinutesBefore(minutes: Int) {
        prefs.edit().putInt(KEY_IMSAK_BEFORE, minutes).apply()
        updateState()
    }

    fun updateKaza(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, witr: Int) {
        prefs.edit()
            .putInt(KEY_KAZA_FAJR, fajr.coerceAtLeast(0))
            .putInt(KEY_KAZA_DHUHR, dhuhr.coerceAtLeast(0))
            .putInt(KEY_KAZA_ASR, asr.coerceAtLeast(0))
            .putInt(KEY_KAZA_MAGHRIB, maghrib.coerceAtLeast(0))
            .putInt(KEY_KAZA_ISHA, isha.coerceAtLeast(0))
            .putInt(KEY_KAZA_WITR, witr.coerceAtLeast(0))
            .apply()
        updateState()
    }

    fun setSilentModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SILENT_ENABLED, enabled).apply()
        updateState()
    }

    fun setSilentModeDuration(minutes: Int) {
        prefs.edit().putInt(KEY_SILENT_DURATION, minutes).apply()
        updateState()
    }

    fun setTeravihMinutesAfterIsha(minutes: Int) {
        prefs.edit().putInt(KEY_TERAVIH_AFTER, minutes).apply()
        updateState()
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_UPDATES, enabled).apply()
        updateState()
    }

    fun setGithubRepo(repo: String) {
        prefs.edit().putString(KEY_GITHUB_REPO, repo.trim()).apply()
        updateState()
    }

    fun togglePrayerAlarm(target: com.example.model.AlarmTargetPrayer, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALARM_ENABLED_PREFIX + target.id, enabled).apply()
        updateState()
    }

    fun setPrayerAlarmMinutesBefore(target: com.example.model.AlarmTargetPrayer, minutes: Int) {
        prefs.edit().putInt(KEY_ALARM_MINUTES_PREFIX + target.id, minutes).apply()
        updateState()
    }

    fun setPrayerAlarmSound(soundId: String) {
        prefs.edit().putString(KEY_ALARM_SOUND_ID, soundId).apply()
        updateState()
    }

    fun setPrayerAlarmVibrate(vibrate: Boolean) {
        prefs.edit().putBoolean(KEY_ALARM_VIBRATE, vibrate).apply()
        updateState()
    }

    fun setPrayerAlarmVolume(volume: Float) {
        prefs.edit().putFloat(KEY_ALARM_VOLUME, volume.coerceIn(0.1f, 1.0f)).apply()
        updateState()
    }

    fun setPrayerAlarmSnooze(minutes: Int) {
        prefs.edit().putInt(KEY_ALARM_SNOOZE, minutes.coerceIn(1, 30)).apply()
        updateState()
    }

    private fun updateState() {
        _userPreferencesFlow.value = loadPreferences()
    }

    companion object {
        private const val KEY_LOCATION_ID = "location_id"
        private const val KEY_NOTIF_PREFIX = "notif_"
        private const val KEY_MINUTES_BEFORE = "minutes_before"
        private const val KEY_SOUND_TYPE = "sound_type"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LARGE_TEXT = "large_text"
        private const val KEY_24H = "is_24h"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"
        private const val KEY_JUMUAH_TIME = "jumuah_time"
        private const val KEY_IMSAK_BEFORE = "imsak_before"
        private const val KEY_KAZA_FAJR = "kaza_fajr"
        private const val KEY_KAZA_DHUHR = "kaza_dhuhr"
        private const val KEY_KAZA_ASR = "kaza_asr"
        private const val KEY_KAZA_MAGHRIB = "kaza_maghrib"
        private const val KEY_KAZA_ISHA = "kaza_isha"
        private const val KEY_KAZA_WITR = "kaza_witr"
        private const val KEY_SILENT_ENABLED = "silent_enabled"
        private const val KEY_SILENT_DURATION = "silent_duration"
        private const val KEY_TERAVIH_AFTER = "teravih_after"
        private const val KEY_AUTO_UPDATES = "auto_updates"
        private const val KEY_GITHUB_REPO = "github_repo"

        // Alarm "Zov na Sedždu" keys
        private const val KEY_ALARM_ENABLED_PREFIX = "alarm_enabled_"
        private const val KEY_ALARM_MINUTES_PREFIX = "alarm_minutes_"
        private const val KEY_ALARM_SOUND_ID = "alarm_sound_id"
        private const val KEY_ALARM_VIBRATE = "alarm_vibrate"
        private const val KEY_ALARM_VOLUME = "alarm_volume"
        private const val KEY_ALARM_SNOOZE = "alarm_snooze"
    }
}
