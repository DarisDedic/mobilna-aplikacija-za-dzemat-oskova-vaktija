package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PreferencesRepository
import com.example.model.LocationDatabase
import com.example.model.PrayerType
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getIntExtra(EXTRA_PRAYER_ID, -1)
        val isReminder = intent.getBooleanExtra(EXTRA_IS_REMINDER, false)
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Džemat Oskova"

        if (prayerId == -1) return
        val prayerType = PrayerType.fromId(prayerId)

        showNotification(context, prayerType, isReminder, minutesBefore, locationName)

        // Reschedule upcoming prayers in background
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = PreferencesRepository(context)
                val prayerRepo = PrayerRepository(context)
                val prefs = prefsRepo.userPreferencesFlow.value
                val schedule = prayerRepo.getPrayerSchedule(LocalDate.now(), prefs.locationId)
                NotificationScheduler.scheduleUpcomingPrayers(context, schedule, prefs)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        prayer: PrayerType,
        isReminder: Boolean,
        minutesBefore: Int,
        locationName: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (isReminder) {
            NotificationScheduler.CHANNEL_REMINDER_ID
        } else {
            NotificationScheduler.CHANNEL_PRAYER_ID
        }

        val title = if (isReminder) {
            "Podsjetnik: ${prayer.bosnianName} ($minutesBefore min)"
        } else {
            "Vrijeme je za ${prayer.bosnianName}"
        }

        val content = if (isReminder) {
            "Uskoro nastupa ${prayer.bosnianName} namaz ($locationName)."
        } else {
            "Nastupilo je vrijeme za ${prayer.bosnianName} namaz ($locationName). ${prayer.arabicName}"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayer.id * 10 + (if (isReminder) 1 else 0),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))

        val notificationId = 1000 + prayer.id + (if (isReminder) 100 else 0)
        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_IS_REMINDER = "extra_is_reminder"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
    }
}
