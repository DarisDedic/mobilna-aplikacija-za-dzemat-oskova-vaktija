package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSize: Long = 0L,
    val publishedAt: String = ""
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data class UpToDate(val currentVersion: String) : UpdateState()
    data class Downloading(val percent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateManager(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    suspend fun checkForUpdates(
        repoPath: String,
        isManualCheck: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (_updateState.value is UpdateState.Downloading) {
            return@withContext
        }

        val cleanRepo = repoPath.trim().removePrefix("https://github.com/").removeSuffix("/")
        if (cleanRepo.isBlank() || !cleanRepo.contains("/")) {
            if (isManualCheck) {
                _updateState.value = UpdateState.Error("Nevažeći format GitHub repozitorija. Koristite format: vlasnik/naziv-repozitorija")
            }
            return@withContext
        }

        if (isManualCheck) {
            _updateState.value = UpdateState.Checking
        }

        try {
            val apiUrl = "https://api.github.com/repos/$cleanRepo/releases/latest"
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Vaktija-Oskova-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                response.close()
                if (code == 404) {
                    if (isManualCheck) {
                        _updateState.value = UpdateState.Error("Repozitorij ili Release nije pronađen na GitHubu (404). Kreirajte Release sa .apk datotekom.")
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                    return@withContext
                } else {
                    if (isManualCheck) {
                        _updateState.value = UpdateState.Error("GitHub greška ($code). Pokušajte ponovo kasnije.")
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                    return@withContext
                }
            }

            val bodyString = response.body?.string()
            if (bodyString.isNullOrBlank()) {
                if (isManualCheck) {
                    _updateState.value = UpdateState.Error("Prazan odgovor sa GitHuba.")
                } else {
                    _updateState.value = UpdateState.Idle
                }
                return@withContext
            }

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "").ifBlank { json.optString("name", "") }
            val releaseNotes = json.optString("body", "Nema opisa izmjena.")
            val publishedAt = json.optString("published_at", "")

            // Find APK asset
            var apkDownloadUrl = ""
            var apkSize = 0L

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkDownloadUrl.isBlank()) {
                if (isManualCheck) {
                    _updateState.value = UpdateState.Error("Nova verzija ($tagName) je objavljena, ali ne sadrži .apk datoteku u Assets.")
                } else {
                    _updateState.value = UpdateState.Idle
                }
                return@withContext
            }

            val currentVer = BuildConfig.VERSION_NAME
            val isNewer = isVersionNewer(tagName, currentVer)

            if (isNewer) {
                val info = UpdateInfo(
                    versionName = tagName,
                    releaseNotes = releaseNotes,
                    downloadUrl = apkDownloadUrl,
                    apkSize = apkSize,
                    publishedAt = publishedAt
                )
                _updateState.value = UpdateState.Available(info)
                showUpdateNotification(context, info)
            } else {
                if (isManualCheck) {
                    _updateState.value = UpdateState.UpToDate(currentVer)
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Greška pri provjeri ažuriranja", e)
            if (isManualCheck) {
                _updateState.value = UpdateState.Error("Neuspješna provjera: ${e.localizedMessage ?: "Greška mreže"}")
            } else {
                _updateState.value = UpdateState.Idle
            }
        }
    }

    suspend fun downloadAndInstall(
        context: Context,
        info: UpdateInfo
    ) = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateState.Downloading(0, 0L, info.apkSize)

            val request = Request.Builder()
                .url(info.downloadUrl)
                .header("User-Agent", "Vaktija-Oskova-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                _updateState.value = UpdateState.Error("Greška pri preuzimanju datoteke (${response.code}).")
                response.close()
                return@withContext
            }

            val responseBody = response.body ?: throw IOException("Prazno tijelo odgovora")
            val totalBytes = if (responseBody.contentLength() > 0) responseBody.contentLength() else info.apkSize

            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val destinationFile = File(updatesDir, "vaktija_oskova_${info.versionName.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            responseBody.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastReportTime = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastReportTime >= 200 || totalRead == totalBytes) {
                            lastReportTime = now
                            val percent = if (totalBytes > 0) {
                                ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                -1
                            }
                            _updateState.value = UpdateState.Downloading(percent, totalRead, totalBytes)
                        }
                    }
                    output.flush()
                }
            }

            _updateState.value = UpdateState.ReadyToInstall(destinationFile)

            // Attempt installation on main thread / context
            withContext(Dispatchers.Main) {
                promptInstall(context, destinationFile)
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Greška pri preuzimanju APK-a", e)
            _updateState.value = UpdateState.Error("Preuzimanje prekinuto: ${e.localizedMessage}")
        }
    }

    fun promptInstall(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            _updateState.value = UpdateState.Error("APK datoteka ne postoji na disku.")
            return
        }

        // Check unknown sources on Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(manageIntent)
                } catch (e: Exception) {
                    Log.e("UpdateManager", "Ne mogu otvoriti ACTION_MANAGE_UNKNOWN_APP_SOURCES", e)
                }
                // Leave state in ReadyToInstall so user can click Install once enabled
                return
            }
        }

        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Greška pri pozivu sistemskog instalera", e)
            _updateState.value = UpdateState.Error("Nije moguće pokrenuti instalaciju: ${e.localizedMessage}")
        }
    }

    private fun showUpdateNotification(context: Context, info: UpdateInfo) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "channel_vaktija_updates"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Ažuriranja aplikacije",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Obavijesti o novim verzijama i nadogradnjama Vaktije"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_OPEN_UPDATE_DIALOG", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                7777,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Dostupno ažuriranje: Vaktija ${info.versionName}")
                .setContentText("Dodirnite da preuzmete i instalirate novu verziju.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Objavljena je nova verzija ${info.versionName} za Džemat Oskova!\n\n${info.releaseNotes.take(200)}")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(UPDATE_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e("UpdateManager", "Ne mogu prikazati obavijest o ažuriranju", e)
        }
    }

    companion object {
        const val UPDATE_NOTIFICATION_ID = 8888
        fun isVersionNewer(latest: String, current: String): Boolean {
            val cleanLatest = latest.removePrefix("v").removePrefix("V").trim()
            val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()

            if (cleanLatest == cleanCurrent) return false

            val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

            if (latestParts.isEmpty() || currentParts.isEmpty()) {
                return cleanLatest != cleanCurrent
            }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        }
    }
}
