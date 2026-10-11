package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.example.data.repository.UserPreferences
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerType
import com.example.receiver.PrayerAlarmReceiver
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object NotificationScheduler {

    const val CHANNEL_PRAYER_ID = "channel_vaktija_prayers"
    const val CHANNEL_REMINDER_ID = "channel_vaktija_reminders"

    private val SARAJEVO_ZONE = ZoneId.of("Europe/Sarajevo")

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            // 1. Exact Prayer Notification Channel
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "Vremena namaza",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tačna obavijest pri nastupanju namaskog vakta"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(defaultSound, audioAttributes)
            }

            // 2. Pre-prayer Reminder Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Podsjetnik prije namaza",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Obavijest nekoliko minuta prije nastupanja namaza"
                enableVibration(true)
                setSound(defaultSound, audioAttributes)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Schedules alarms for all enabled prayers for the given schedule.
     */
    fun scheduleUpcomingPrayers(
        context: Context,
        schedule: DailyPrayerSchedule,
        prefs: UserPreferences
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if exact alarms can be scheduled on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("NotificationScheduler", "Exact alarms permission not granted by user")
            }
        }

        val nowInSarajevo = ZonedDateTime.now(SARAJEVO_ZONE)

        for (prayer in schedule.prayers) {
            val isEnabled = prefs.prayerNotifications[prayer.type] ?: true
            if (!isEnabled) {
                cancelAlarm(context, prayer.type, isReminder = false)
                cancelAlarm(context, prayer.type, isReminder = true)
                continue
            }

            val prayerDateTime = schedule.date.atTime(prayer.time).atZone(SARAJEVO_ZONE)

            // 1. Exact prayer alarm
            if (prayerDateTime.isAfter(nowInSarajevo)) {
                val triggerEpochMs = prayerDateTime.toInstant().toEpochMilli()
                setExactAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    triggerEpochMs = triggerEpochMs,
                    prayerType = prayer.type,
                    isReminder = false,
                    minutesBefore = 0,
                    locationName = schedule.locationName
                )
            }

            // 2. "X minutes before" reminder alarm
            if (prefs.minutesBefore > 0) {
                val reminderDateTime = prayerDateTime.minusMinutes(prefs.minutesBefore.toLong())
                if (reminderDateTime.isAfter(nowInSarajevo)) {
                    val reminderEpochMs = reminderDateTime.toInstant().toEpochMilli()
                    setExactAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        triggerEpochMs = reminderEpochMs,
                        prayerType = prayer.type,
                        isReminder = true,
                        minutesBefore = prefs.minutesBefore,
                        locationName = schedule.locationName
                    )
                }
            } else {
                cancelAlarm(context, prayer.type, isReminder = true)
            }
        }
    }

    private fun setExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerEpochMs: Long,
        prayerType: PrayerType,
        isReminder: Boolean,
        minutesBefore: Int,
        locationName: String
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, prayerType.id)
            putExtra(PrayerAlarmReceiver.EXTRA_IS_REMINDER, isReminder)
            putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
            putExtra(PrayerAlarmReceiver.EXTRA_LOCATION_NAME, locationName)
        }

        val requestCode = prayerType.id * 100 + (if (isReminder) 1 else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
            Log.d("NotificationScheduler", "Scheduled ${prayerType.bosnianName} (reminder=$isReminder) at $triggerEpochMs")
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "SecurityException scheduling exact alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, prayerType: PrayerType, isReminder: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val requestCode = prayerType.id * 100 + (if (isReminder) 1 else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
