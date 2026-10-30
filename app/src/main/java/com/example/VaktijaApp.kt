package com.example

import android.app.Application
import android.util.Log
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PreferencesRepository
import com.example.notification.NotificationScheduler
import com.example.widget.VaktijaWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class VaktijaApp : Application() {

    lateinit var prayerRepository: PrayerRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var updateManager: com.example.updater.UpdateManager
        private set

    override fun onCreate() {
        super.onCreate()

        prayerRepository = PrayerRepository(this)
        preferencesRepository = PreferencesRepository(this)
        updateManager = com.example.updater.UpdateManager(this)

        // 1. Initialize notification channels
        NotificationScheduler.createNotificationChannels(this)
        com.example.alarm.PrayerWakeAlarmScheduler.createAlarmNotificationChannel(this)

        // 2. Background check and cache priming
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = preferencesRepository.userPreferencesFlow.value
                val schedule = prayerRepository.getPrayerSchedule(LocalDate.now(), prefs.locationId)
                val tomorrowSchedule = prayerRepository.getPrayerSchedule(LocalDate.now().plusDays(1), prefs.locationId)
                NotificationScheduler.scheduleUpcomingPrayers(this@VaktijaApp, schedule, prefs)
                com.example.alarm.PrayerWakeAlarmScheduler.scheduleAllAlarms(
                    this@VaktijaApp,
                    schedule,
                    tomorrowSchedule,
                    prefs.prayerAlarmConfig
                )

                // Update widgets
                VaktijaWidgetProvider.updateAllWidgets(this@VaktijaApp)

                // Ensure 60+ days are cached
                prayerRepository.prefetch60Days(prefs.locationId)
            } catch (e: Exception) {
                Log.w("VaktijaApp", "Initial warmup background task: ${e.message}")
            }
        }
    }
}
