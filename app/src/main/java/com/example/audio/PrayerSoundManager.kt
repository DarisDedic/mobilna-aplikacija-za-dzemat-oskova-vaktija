package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object PrayerSoundManager {

    const val SOUND_EZAN = "EZAN"
    const val SOUND_TEKBIR = "TEKBIR"
    const val SOUND_MEKAM_SABA = "MEKAM_SABA"
    const val SOUND_SYSTEM_ALARM = "SYSTEM_ALARM"
    const val SOUND_GENTLE_CHIME = "GENTLE_CHIME"

    data class SoundOption(
        val id: String,
        val displayName: String,
        val description: String
    )

    val AVAILABLE_SOUNDS = listOf(
        SoundOption(
            id = SOUND_EZAN,
            displayName = "Ezan melodija (Allahu Ekber)",
            description = "Tradicionalni melodijski poziv na sabahski namaz"
        ),
        SoundOption(
            id = SOUND_TEKBIR,
            displayName = "Tekbir (Allahu Ekber, La ilahe illallah)",
            description = "Dostojanstveni zvučni tekbirski tonovi"
        ),
        SoundOption(
            id = SOUND_MEKAM_SABA,
            displayName = "Mekam Saba (Miran jutarnji ton)",
            description = "Nježni orijentalni ton za ugodno buđenje"
        ),
        SoundOption(
            id = SOUND_SYSTEM_ALARM,
            displayName = "Sistemski glasni alarm",
            description = "Standardni budilnik sa vašeg uređaja"
        ),
        SoundOption(
            id = SOUND_GENTLE_CHIME,
            displayName = "Blagi ezanski gong",
            description = "Kratak i prijatan zvučni signal"
        )
    )

    private var currentRingtone: Ringtone? = null
    private var currentAudioTrack: AudioTrack? = null
    private var activeJob: Job? = null
    private var isCurrentlyPlaying = false

    /**
     * Start playing alarm audio continuously until stop() is called.
     */
    fun startAlarmPlayback(
        context: Context,
        soundId: String = SOUND_EZAN,
        volume: Float = 1.0f,
        loop: Boolean = true
    ) {
        stop()
        isCurrentlyPlaying = true

        activeJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                if (soundId == SOUND_SYSTEM_ALARM) {
                    playSystemAlarmRingtone(context, volume, loop)
                } else {
                    do {
                        playSynthesizedSound(soundId, volume, isAlarm = true)
                        if (loop && isActive) {
                            delay(1200)
                        }
                    } while (loop && isActive)
                }
            } catch (e: Exception) {
                Log.e("PrayerSoundManager", "Error in alarm playback: ${e.message}")
            }
        }
    }

    /**
     * Plays a short preview (e.g. 5-10 seconds) of the sound.
     */
    fun playPreview(
        context: Context,
        soundId: String,
        onFinished: (() -> Unit)? = null
    ) {
        stop()
        isCurrentlyPlaying = true

        activeJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                if (soundId == SOUND_SYSTEM_ALARM) {
                    playSystemAlarmRingtone(context, volume = 0.9f, loop = false)
                    delay(5000)
                    stop()
                } else {
                    playSynthesizedSound(soundId, volume = 0.9f, isAlarm = false)
                }
            } catch (e: Exception) {
                Log.e("PrayerSoundManager", "Error in preview: ${e.message}")
            } finally {
                Handler(Looper.getMainLooper()).post {
                    isCurrentlyPlaying = false
                    onFinished?.invoke()
                }
            }
        }
    }

    /**
     * Stops any currently playing alarm or preview sound.
     */
    fun stop() {
        activeJob?.cancel()
        activeJob = null
        isCurrentlyPlaying = false

        try {
            currentRingtone?.stop()
            currentRingtone = null
        } catch (e: Exception) {
            Log.w("PrayerSoundManager", "Error stopping ringtone: ${e.message}")
        }

        try {
            currentAudioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
            currentAudioTrack = null
        } catch (e: Exception) {
            Log.w("PrayerSoundManager", "Error releasing AudioTrack: ${e.message}")
        }
    }

    fun isPlaying(): Boolean = isCurrentlyPlaying

    private fun playSystemAlarmRingtone(context: Context, volume: Float, loop: Boolean) {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.volume = volume.coerceIn(0.1f, 1.0f)
            ringtone.isLooping = loop
        }
        currentRingtone = ringtone
        ringtone.play()
    }

    /**
     * Generates beautiful melodic acoustic tone synthesis with natural harmonic envelope
     * (Allahu Akbar, Saba scale, or Tekbir melody) directly into AudioTrack.
     */
    private fun playSynthesizedSound(soundId: String, volume: Float, isAlarm: Boolean) {
        val sampleRate = 44100
        val notes = when (soundId) {
            SOUND_EZAN -> listOf(
                // "Allahu Ekber" Adhan motif (Bayan/Hijaz/Saba flavour)
                Note(392.00, 700), // G4 (Al-)
                Note(466.16, 900), // Bb4 (-laahu)
                Note(523.25, 1200), // C5 (Ek-)
                Note(466.16, 600), // Bb4
                Note(392.00, 1100), // G4 (-ber)
                Note(0.0, 400),
                Note(392.00, 700), // G4
                Note(466.16, 900), // Bb4
                Note(523.25, 1200), // C5
                Note(466.16, 700), // Bb4
                Note(392.00, 1300), // G4
                Note(0.0, 500)
            )

            SOUND_TEKBIR -> listOf(
                // "Allahu Ekber, Allahu Ekber, La ilahe illallah"
                Note(440.00, 600), // A4
                Note(554.37, 750), // C#5
                Note(659.25, 900), // E5
                Note(554.37, 650), // C#5
                Note(440.00, 1000), // A4
                Note(0.0, 300),
                Note(493.88, 500), // B4
                Note(554.37, 600), // C#5
                Note(659.25, 900), // E5
                Note(554.37, 1200) // C#5
            )

            SOUND_MEKAM_SABA -> listOf(
                // Contemplative dawn Saba melody
                Note(293.66, 800), // D4
                Note(311.13, 700), // Eb4
                Note(349.23, 850), // F4
                Note(369.99, 700), // Gb4
                Note(440.00, 1100), // A4
                Note(369.99, 650),
                Note(349.23, 750),
                Note(311.13, 800),
                Note(293.66, 1400) // D4
            )

            SOUND_GENTLE_CHIME -> listOf(
                Note(523.25, 400), // C5
                Note(659.25, 450), // E5
                Note(783.99, 500), // G5
                Note(1046.50, 1100) // C6
            )

            else -> listOf(
                Note(440.0, 400),
                Note(880.0, 600),
                Note(440.0, 400),
                Note(880.0, 600)
            )
        }

        val totalDurationMs = notes.sumOf { it.durationMs }
        val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        var sampleIndex = 0
        for (note in notes) {
            val noteSamples = (sampleRate * (note.durationMs / 1000.0)).toInt()
            val freq = note.frequency
            val hasSound = freq > 10.0

            for (i in 0 until noteSamples) {
                if (sampleIndex >= totalSamples) break
                if (!hasSound) {
                    buffer[sampleIndex++] = 0
                    continue
                }

                // Envelope: smooth attack (15%), sustain, and soft release decay
                val progress = i.toDouble() / noteSamples
                val envelope = when {
                    progress < 0.15 -> progress / 0.15
                    progress > 0.70 -> (1.0 - progress) / 0.30
                    else -> 1.0
                }

                // Rich acoustic tone with fundamental + warm 2nd and 3rd harmonics
                val t = i.toDouble() / sampleRate
                val fundamental = sin(2.0 * PI * freq * t)
                val harmonic2 = 0.35 * sin(2.0 * PI * (freq * 2.0) * t)
                val harmonic3 = 0.15 * sin(2.0 * PI * (freq * 3.0) * t)
                val rawSample = (fundamental + harmonic2 + harmonic3) / 1.5

                val amplitude = (rawSample * envelope * volume * 32000.0).toInt().coerceIn(-32767, 32767)
                buffer[sampleIndex++] = amplitude.toShort()
            }
        }

        val usage = if (isAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA
        val streamType = if (isAlarm) AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack(
            audioAttributes,
            audioFormat,
            buffer.size * 2.coerceAtLeast(minBufferSize),
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        currentAudioTrack = track
        track.write(buffer, 0, buffer.size)
        track.play()

        // Wait until static buffer playback finishes
        val sleepTime = totalDurationMs.toLong() + 100
        Thread.sleep(sleepTime)
    }

    private data class Note(val frequency: Double, val durationMs: Int)
}
