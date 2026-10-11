package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PreferencesRepository
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast action: $action")

        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED
        )

        if (action in validActions) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsRepo = PreferencesRepository(context)
                    val prayerRepo = PrayerRepository(context)
                    val prefs = prefsRepo.userPreferencesFlow.value

                    // Re-schedule for today and tomorrow
                    val todaySchedule = prayerRepo.getPrayerSchedule(LocalDate.now(), prefs.locationId)
                    NotificationScheduler.scheduleUpcomingPrayers(context, todaySchedule, prefs)

                    val tomorrowSchedule = prayerRepo.getPrayerSchedule(LocalDate.now().plusDays(1), prefs.locationId)
                    NotificationScheduler.scheduleUpcomingPrayers(context, tomorrowSchedule, prefs)

                    Log.i("BootReceiver", "Alarms successfully rescheduled on $action")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
