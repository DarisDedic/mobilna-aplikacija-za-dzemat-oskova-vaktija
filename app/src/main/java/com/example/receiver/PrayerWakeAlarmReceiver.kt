package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.alarm.AlarmActivity
import com.example.alarm.PrayerWakeAlarmScheduler
import com.example.audio.PrayerSoundManager
import com.example.model.AlarmTargetPrayer

class PrayerWakeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_WAKE_ALARM

        when (action) {
            ACTION_DISMISS_ALARM -> {
                PrayerSoundManager.stop()
                PrayerWakeAlarmScheduler.cancelAlarmRingingNotification(context)
                return
            }

            ACTION_SNOOZE_ALARM -> {
                PrayerSoundManager.stop()
                PrayerWakeAlarmScheduler.cancelAlarmRingingNotification(context)
                PrayerWakeAlarmScheduler.scheduleSnooze(context, minutes = 5)
                return
            }

            ACTION_WAKE_ALARM -> {
                handleAlarmTrigger(context, intent)
            }
        }
    }

    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        val targetId = intent.getStringExtra(EXTRA_TARGET_ID) ?: AlarmTargetPrayer.FAJR.id
        val target = AlarmTargetPrayer.fromId(targetId)
        val soundId = intent.getStringExtra(EXTRA_SOUND_ID) ?: PrayerSoundManager.SOUND_EZAN
        val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "vaktija:AlarmWakeLock"
        )
        wakeLock.acquire(3 * 60 * 1000L /* 3 minutes */)

        // 1. Intent to launch AlarmActivity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(AlarmActivity.EXTRA_ALARM_TITLE, target.title)
            putExtra(AlarmActivity.EXTRA_ALARM_SUBTITLE, target.subtitle)
            putExtra(AlarmActivity.EXTRA_SOUND_ID, soundId)
            putExtra(AlarmActivity.EXTRA_VIBRATE, vibrate)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            PrayerWakeAlarmScheduler.NOTIFICATION_RINGING_ID,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(context, PrayerWakeAlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, PrayerWakeAlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(
            context,
            PrayerWakeAlarmScheduler.CHANNEL_WAKE_ALARM_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Zov na Sedždu: ${target.title}")
            .setContentText("Vrijeme je za namaz! Dodirni za buđenje i isključenje.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(R.mipmap.ic_launcher, "ISKLJUČI", dismissPendingIntent)
            .addAction(R.mipmap.ic_launcher, "ODGODI 5 MIN", snoozePendingIntent)
            .build()

        notificationManager.notify(PrayerWakeAlarmScheduler.NOTIFICATION_RINGING_ID, notification)

        // Try direct launch of AlarmActivity as well
        try {
            context.startActivity(alarmIntent)
        } catch (_: Exception) {}
    }

    companion object {
        const val ACTION_WAKE_ALARM = "com.example.vaktija.ACTION_WAKE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.example.vaktija.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.vaktija.ACTION_SNOOZE_ALARM"
        const val EXTRA_TARGET_ID = "extra_target_id"
        const val EXTRA_SOUND_ID = "extra_sound_id"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}
