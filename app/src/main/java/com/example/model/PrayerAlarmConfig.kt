package com.example.model

import com.example.audio.PrayerSoundManager
import java.time.LocalTime

enum class AlarmTargetPrayer(
    val id: String,
    val title: String,
    val subtitle: String,
    val prayerType: PrayerType?,
    val defaultMinutesBefore: Int
) {
    FAJR(
        id = "fajr",
        title = "Sabahski budilnik (Zora)",
        subtitle = "Glavno buđenje na sabahski vakat",
        prayerType = PrayerType.FAJR,
        defaultMinutesBefore = 15
    ),
    TAHAJJUD(
        id = "tahajjud",
        title = "Tehedždžud & Sehur",
        subtitle = "Noćni namaz i priprema za post prije sabaha",
        prayerType = PrayerType.FAJR,
        defaultMinutesBefore = 45
    ),
    DHUHR(
        id = "dhuhr",
        title = "Podne namaz",
        subtitle = "Poziv na podnevski namaz",
        prayerType = PrayerType.DHUHR,
        defaultMinutesBefore = 0
    ),
    ASR(
        id = "asr",
        title = "Ikindija namaz",
        subtitle = "Poziv na ikindijski namaz",
        prayerType = PrayerType.ASR,
        defaultMinutesBefore = 0
    ),
    MAGHRIB(
        id = "maghrib",
        title = "Akšam namaz (Iftar)",
        subtitle = "Poziv na akšam i vrijeme iftara",
        prayerType = PrayerType.MAGHRIB,
        defaultMinutesBefore = 0
    ),
    ISHA(
        id = "isha",
        title = "Jacija namaz",
        subtitle = "Poziv na noćni jacija namaz",
        prayerType = PrayerType.ISHA,
        defaultMinutesBefore = 0
    );

    companion object {
        fun fromId(id: String): AlarmTargetPrayer =
            entries.firstOrNull { it.id == id } ?: FAJR
    }
}

data class SingleAlarmSetting(
    val target: AlarmTargetPrayer,
    val isEnabled: Boolean,
    val minutesBefore: Int // 0 = at exact time, >0 = X minutes before
)

data class PrayerAlarmConfig(
    val alarms: Map<AlarmTargetPrayer, SingleAlarmSetting> = AlarmTargetPrayer.entries.associateWith {
        SingleAlarmSetting(
            target = it,
            isEnabled = (it == AlarmTargetPrayer.FAJR), // Sabah enabled by default
            minutesBefore = it.defaultMinutesBefore
        )
    },
    val soundId: String = PrayerSoundManager.SOUND_EZAN,
    val vibrate: Boolean = true,
    val volume: Float = 1.0f,
    val snoozeMinutes: Int = 5
)
