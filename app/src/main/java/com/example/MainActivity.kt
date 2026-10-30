package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.LocationDatabase
import com.example.ui.screens.DzematScreen
import com.example.ui.screens.HolidaysScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KazaScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.RamadanTableScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TesbihScreen
import com.example.ui.screens.DovarnikScreen
import com.example.ui.screens.PrayerAlarmScreen
import com.example.ui.theme.VaktijaTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.components.UpdateDialogHost

enum class AppScreen {
    HOME,
    HOLIDAYS,
    QIBLA,
    DZEMAT,
    SETTINGS,
    TESBIH,
    KAZA,
    RAMADAN_TABLE,
    DOVARNIK,
    PRAYER_ALARM
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
            val displaySchedule by viewModel.displaySchedule.collectAsStateWithLifecycle()
            val todaySchedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
            val hijriInfo by viewModel.hijriInfo.collectAsStateWithLifecycle()
            val nextPrayer by viewModel.nextPrayer.collectAsStateWithLifecycle()
            val countdownString by viewModel.countdownString.collectAsStateWithLifecycle()
            val nextHoliday by viewModel.nextHoliday.collectAsStateWithLifecycle()
            val holidaysList by viewModel.holidaysList.collectAsStateWithLifecycle()
            val cachedDaysCount by viewModel.cachedDaysCount.collectAsStateWithLifecycle()
            val isDifferentTimezone by viewModel.isDifferentTimezone.collectAsStateWithLifecycle()
            val deviceZoneId by viewModel.deviceZoneId.collectAsStateWithLifecycle()
            val imsakCountdown by viewModel.imsakCountdown.collectAsStateWithLifecycle()
            val iftarCountdown by viewModel.iftarCountdown.collectAsStateWithLifecycle()
            val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
            val deviceHeading by viewModel.deviceHeading.collectAsStateWithLifecycle()
            val sensorAccuracyWarning by viewModel.sensorAccuracyWarning.collectAsStateWithLifecycle()
            val monthSchedules by viewModel.monthSchedules.collectAsStateWithLifecycle()
            val updateState by viewModel.updateState.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

            // Preload 30-day Ramadan table when switching to RAMADAN_TABLE
            LaunchedEffect(currentScreen) {
                if (currentScreen == AppScreen.RAMADAN_TABLE && monthSchedules.isEmpty()) {
                    viewModel.loadMonthSchedules(java.time.LocalDate.now())
                }
            }

            // Request Notification permission for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Granted or dismissed */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (intent?.getBooleanExtra("EXTRA_OPEN_UPDATE_DIALOG", false) == true) {
                    viewModel.checkForUpdates(isManual = true)
                }
            }

            VaktijaTheme(themeMode = userPreferences.themeMode) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                selectedDate = selectedDate,
                                displaySchedule = displaySchedule,
                                todaySchedule = todaySchedule,
                                hijriInfo = hijriInfo,
                                nextPrayer = nextPrayer,
                                countdownString = countdownString,
                                nextHoliday = nextHoliday,
                                userPreferences = userPreferences,
                                cachedDaysCount = cachedDaysCount,
                                isDifferentTimezone = isDifferentTimezone,
                                deviceZoneId = deviceZoneId,
                                imsakCountdown = imsakCountdown,
                                iftarCountdown = iftarCountdown,
                                onPreviousDay = { viewModel.previousDay() },
                                onNextDay = { viewModel.nextDay() },
                                onToday = { viewModel.jumpToToday() },
                                onNavigateToHolidays = { currentScreen = AppScreen.HOLIDAYS },
                                onNavigateToQibla = { currentScreen = AppScreen.QIBLA },
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                onNavigateToDzemat = { currentScreen = AppScreen.DZEMAT },
                                onNavigateToTesbih = { currentScreen = AppScreen.TESBIH },
                                onNavigateToKaza = { currentScreen = AppScreen.KAZA },
                                onNavigateToRamadanTable = { currentScreen = AppScreen.RAMADAN_TABLE },
                                onNavigateToDovarnik = { currentScreen = AppScreen.DOVARNIK },
                                onNavigateToPrayerAlarm = { currentScreen = AppScreen.PRAYER_ALARM },
                                onToggleSilentMode = { viewModel.toggleSilentMode(it) }
                            )
                        }

                        AppScreen.HOLIDAYS -> {
                            HolidaysScreen(
                                holidays = holidaysList,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.QIBLA -> {
                            val loc = LocationDatabase.getById(userPreferences.locationId)
                            QiblaScreen(
                                locationName = loc.name,
                                qiblaBearing = qiblaBearing,
                                deviceHeading = deviceHeading,
                                sensorAccuracyWarning = sensorAccuracyWarning,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.DZEMAT -> {
                            DzematScreen(
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                userPreferences = userPreferences,
                                onSetLocation = { viewModel.setLocation(it) },
                                onSetNearestLocation = { lat, lon -> viewModel.setNearestLocation(lat, lon) },
                                onTogglePrayerNotification = { type, enabled ->
                                    viewModel.togglePrayerNotification(type, enabled)
                                },
                                onSetMinutesBefore = { viewModel.setMinutesBefore(it) },
                                onSetSoundType = { viewModel.setSoundType(it) },
                                onSetThemeMode = { viewModel.setThemeMode(it) },
                                onSetLargeText = { viewModel.setLargeText(it) },
                                onSetIs24Hour = { viewModel.setIs24HourFormat(it) },
                                onSetHijriOffset = { viewModel.setHijriOffset(it) },
                                onSetJumuahCustomTime = { viewModel.setJumuahCustomTime(it) },
                                onSetImsakMinutesBefore = { viewModel.setImsakMinutesBefore(it) },
                                onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) },
                                onSetAutoCheckUpdates = { viewModel.setAutoCheckUpdates(it) },
                                onSetGithubRepo = { viewModel.setGithubRepo(it) },
                                onNavigateToPrayerAlarm = { currentScreen = AppScreen.PRAYER_ALARM },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.TESBIH -> {
                            TesbihScreen(
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.KAZA -> {
                            KazaScreen(
                                userPreferences = userPreferences,
                                onUpdateKaza = { f, d, a, m, i, w ->
                                    viewModel.updateKaza(f, d, a, m, i, w)
                                },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.RAMADAN_TABLE -> {
                            RamadanTableScreen(
                                locationName = displaySchedule?.locationName ?: "Živinice",
                                schedules = monthSchedules.ifEmpty { listOfNotNull(todaySchedule) },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.DOVARNIK -> {
                            DovarnikScreen(
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.PRAYER_ALARM -> {
                            PrayerAlarmScreen(
                                alarmConfig = userPreferences.prayerAlarmConfig,
                                todaySchedule = todaySchedule,
                                onToggleAlarm = { target, enabled -> viewModel.togglePrayerAlarm(target, enabled) },
                                onSetMinutesBefore = { target, min -> viewModel.setPrayerAlarmMinutesBefore(target, min) },
                                onSetSoundId = { viewModel.setPrayerAlarmSound(it) },
                                onSetVibrate = { viewModel.setPrayerAlarmVibrate(it) },
                                onSetVolume = { viewModel.setPrayerAlarmVolume(it) },
                                onTestAlarm = { viewModel.testAlarmNow(it) },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }
                    }
                }

                // In-App Auto-Updater Dialog
                UpdateDialogHost(
                    updateState = updateState,
                    onDismiss = { viewModel.dismissUpdateDialog() },
                    onStartDownload = { info -> viewModel.downloadAndInstallUpdate(this@MainActivity, info) },
                    onInstallApk = { apk -> viewModel.retryInstall(this@MainActivity, apk) }
                )
            }
        }
    }
}
