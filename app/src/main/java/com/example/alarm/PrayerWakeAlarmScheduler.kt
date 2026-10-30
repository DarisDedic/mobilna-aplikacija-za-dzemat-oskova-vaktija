package com.example.alarm

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
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AlarmTargetPrayer
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerAlarmConfig
import com.example.model.PrayerType
import com.example.receiver.PrayerWakeAlarmReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object PrayerWakeAlarmScheduler {

    const val CHANNEL_WAKE_ALARM_ID = "channel_vaktija_wake_alarm"
    const val NOTIFICATION_RINGING_ID = 9999
    private val SARAJEVO_ZONE = ZoneId.of("Europe/Sarajevo")

    fun createAlarmNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                CHANNEL_WAKE_ALARM_ID,
                "Zov na Sedždu (Budilnik)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Glasni alarm i buđenje za namaze i sabah"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    /**
     * Schedules the next active alarm clock events for enabled prayers.
     */
    fun scheduleAllAlarms(
        context: Context,
        schedule: DailyPrayerSchedule,
        tomorrowSchedule: DailyPrayerSchedule?,
        config: PrayerAlarmConfig
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = ZonedDateTime.now(SARAJEVO_ZONE)

        for (target in AlarmTargetPrayer.entries) {
            val setting = config.alarms[target] ?: continue
            val requestCode = 5000 + target.ordinal

            if (!setting.isEnabled) {
                cancelAlarm(context, requestCode)
                continue
            }

            val prayerTime: LocalTime = when (target) {
                AlarmTargetPrayer.FAJR -> schedule.fajr.time
                AlarmTargetPrayer.TAHAJJUD -> schedule.fajr.time
                AlarmTargetPrayer.DHUHR -> schedule.dhuhr.time
                AlarmTargetPrayer.ASR -> schedule.asr.time
                AlarmTargetPrayer.MAGHRIB -> schedule.maghrib.time
                AlarmTargetPrayer.ISHA -> schedule.isha.time
            }

            // Calculate alarm time today
            var targetDateTime = schedule.date
                .atTime(prayerTime)
                .minusMinutes(setting.minutesBefore.toLong())
                .atZone(SARAJEVO_ZONE)

            // If time has passed today, schedule for tomorrow
            if (!targetDateTime.isAfter(now)) {
                val tomorrowTime: LocalTime = if (tomorrowSchedule != null) {
                    when (target) {
                        AlarmTargetPrayer.FAJR -> tomorrowSchedule.fajr.time
                        AlarmTargetPrayer.TAHAJJUD -> tomorrowSchedule.fajr.time
                        AlarmTargetPrayer.DHUHR -> tomorrowSchedule.dhuhr.time
                        AlarmTargetPrayer.ASR -> tomorrowSchedule.asr.time
                        AlarmTargetPrayer.MAGHRIB -> tomorrowSchedule.maghrib.time
                        AlarmTargetPrayer.ISHA -> tomorrowSchedule.isha.time
                    }
                } else {
                    prayerTime
                }

                targetDateTime = schedule.date.plusDays(1)
                    .atTime(tomorrowTime)
                    .minusMinutes(setting.minutesBefore.toLong())
                    .atZone(SARAJEVO_ZONE)
            }

            val triggerEpochMs = targetDateTime.toInstant().toEpochMilli()
            setAlarmClock(
                context = context,
                alarmManager = alarmManager,
                triggerEpochMs = triggerEpochMs,
                requestCode = requestCode,
                target = target,
                soundId = config.soundId,
                vibrate = config.vibrate
            )
        }
    }

    /**
     * Schedules a quick test alarm to ring after specified seconds.
     */
    fun scheduleTestAlarm(context: Context, seconds: Int = 5, soundId: String, vibrate: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerEpochMs = System.currentTimeMillis() + (seconds * 1000L)
        val requestCode = 9991

        setAlarmClock(
            context = context,
            alarmManager = alarmManager,
            triggerEpochMs = triggerEpochMs,
            requestCode = requestCode,
            target = AlarmTargetPrayer.FAJR,
            soundId = soundId,
            vibrate = vibrate
        )
    }

    /**
     * Schedules a snooze alarm.
     */
    fun scheduleSnooze(context: Context, minutes: Int = 5) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerEpochMs = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val requestCode = 9992

        val intent = Intent(context, PrayerWakeAlarmReceiver::class.java).apply {
            action = PrayerWakeAlarmReceiver.ACTION_WAKE_ALARM
            putExtra(PrayerWakeAlarmReceiver.EXTRA_TARGET_ID, AlarmTargetPrayer.FAJR.id)
            putExtra(PrayerWakeAlarmReceiver.EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clockInfo = AlarmManager.AlarmClockInfo(triggerEpochMs, showPendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)
    }

    private fun setAlarmClock(
        context: Context,
        alarmManager: AlarmManager,
        triggerEpochMs: Long,
        requestCode: Int,
        target: AlarmTargetPrayer,
        soundId: String,
        vibrate: Boolean
    ) {
        val intent = Intent(context, PrayerWakeAlarmReceiver::class.java).apply {
            action = PrayerWakeAlarmReceiver.ACTION_WAKE_ALARM
            putExtra(PrayerWakeAlarmReceiver.EXTRA_TARGET_ID, target.id)
            putExtra(PrayerWakeAlarmReceiver.EXTRA_SOUND_ID, soundId)
            putExtra(PrayerWakeAlarmReceiver.EXTRA_VIBRATE, vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val clockInfo = AlarmManager.AlarmClockInfo(triggerEpochMs, showPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
            Log.d("PrayerWakeAlarm", "Scheduled alarm ${target.title} at $triggerEpochMs")
        } catch (e: SecurityException) {
            Log.w("PrayerWakeAlarm", "Permission required for exact alarm: ${e.message}")
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } catch (e2: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerWakeAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAlarmRingingNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_RINGING_ID)
    }
}
