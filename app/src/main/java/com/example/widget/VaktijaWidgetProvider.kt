package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PreferencesRepository
import com.example.model.LocationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class VaktijaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager? = null,
            appWidgetIds: IntArray? = null
        ) {
            val manager = appWidgetManager ?: AppWidgetManager.getInstance(context)
            val ids = appWidgetIds ?: manager.getAppWidgetIds(
                ComponentName(context, VaktijaWidgetProvider::class.java)
            )

            if (ids.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                val prefsRepo = PreferencesRepository(context)
                val prayerRepo = PrayerRepository(context)
                val prefs = prefsRepo.userPreferencesFlow.value
                val today = LocalDate.now()
                val schedule = prayerRepo.getPrayerSchedule(today, prefs.locationId)
                val location = LocationDatabase.getById(prefs.locationId)

                val sarajevoZone = ZoneId.of("Europe/Sarajevo")
                val now = ZonedDateTime.now(sarajevoZone)

                val nextPrayer = schedule.getNextPrayer(now) ?: schedule.fajr
                val prayerDateTime = if (nextPrayer.time.isBefore(now.toLocalTime())) {
                    today.plusDays(1).atTime(nextPrayer.time).atZone(sarajevoZone)
                } else {
                    today.atTime(nextPrayer.time).atZone(sarajevoZone)
                }

                val duration = Duration.between(now, prayerDateTime)
                val hours = duration.toHours()
                val minutes = (duration.toMinutes() % 60)
                val remainingStr = if (hours > 0) "za ${hours}h ${minutes}m" else "za ${minutes}m"

                val allTimesStr = "Zora ${schedule.fajr.formattedTime} • Podne ${schedule.dhuhr.formattedTime} • Akšam ${schedule.maghrib.formattedTime}"

                for (appWidgetId in ids) {
                    val views = RemoteViews(context.packageName, R.layout.widget_vaktija).apply {
                        val locLabel = if (location.id == LocationDatabase.DEFAULT_LOCATION_ID) {
                            "Džemat Oskova (Živinice)"
                        } else {
                            location.name
                        }
                        setTextViewText(R.id.widget_location, locLabel)
                        setTextViewText(R.id.widget_next_name, "Sljedeći namaz: ${nextPrayer.type.bosnianName}")
                        setTextViewText(R.id.widget_next_time, nextPrayer.formattedTime)
                        setTextViewText(R.id.widget_remaining, remainingStr)
                        setTextViewText(R.id.widget_all_times, allTimesStr)

                        // Open app on click
                        val openAppIntent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            openAppIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    }

                    manager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
