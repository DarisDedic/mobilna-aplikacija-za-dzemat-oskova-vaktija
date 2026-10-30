package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserPreferences
import com.example.model.DailyPrayerSchedule
import com.example.model.HijriDateInfo
import com.example.model.Holiday
import com.example.model.PrayerItem
import com.example.model.PrayerType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentBright
import com.example.ui.theme.NextPrayerGlow
import com.example.ui.theme.PastPrayerDim
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Composable
fun HomeScreen(
    selectedDate: LocalDate,
    displaySchedule: DailyPrayerSchedule?,
    todaySchedule: DailyPrayerSchedule?,
    hijriInfo: HijriDateInfo?,
    nextPrayer: PrayerItem?,
    countdownString: String,
    nextHoliday: Holiday?,
    userPreferences: UserPreferences,
    cachedDaysCount: Int,
    isDifferentTimezone: Boolean,
    deviceZoneId: String,
    imsakCountdown: String,
    iftarCountdown: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onNavigateToHolidays: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDzemat: () -> Unit,
    onNavigateToTesbih: () -> Unit,
    onNavigateToKaza: () -> Unit,
    onNavigateToRamadanTable: () -> Unit,
    onNavigateToDovarnik: () -> Unit,
    onNavigateToPrayerAlarm: () -> Unit,
    onToggleSilentMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = (selectedDate == LocalDate.now())
    val isFriday = (selectedDate.dayOfWeek == DayOfWeek.FRIDAY)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header: Džemat Oskova & Quick Actions
        item {
            HeaderBar(
                locationName = displaySchedule?.locationName ?: "Živinice",
                onNavigateToHolidays = onNavigateToHolidays,
                onNavigateToQibla = onNavigateToQibla,
                onNavigateToDzemat = onNavigateToDzemat,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToPrayerAlarm = onNavigateToPrayerAlarm
            )
        }

        // Quick Actions Row: Zov na Sedždu, Dovarnik, Tespih, Kaza, Džamijski mod, Ramazan tabela
        item {
            QuickActionsBar(
                silentModeEnabled = userPreferences.silentModeEnabled,
                onToggleSilentMode = onToggleSilentMode,
                onNavigateToPrayerAlarm = onNavigateToPrayerAlarm,
                onNavigateToDovarnik = onNavigateToDovarnik,
                onNavigateToTesbih = onNavigateToTesbih,
                onNavigateToKaza = onNavigateToKaza,
                onNavigateToRamadanTable = onNavigateToRamadanTable
            )
        }

        // 2. Timezone Alert (if device zone differs from Europe/Sarajevo)
        if (isDifferentTimezone) {
            item {
                TimezoneNoticeCard(deviceZoneId = deviceZoneId)
            }
        }

        // 3. Date Navigation Card (Gregorian + Hijri)
        item {
            DateNavigationCard(
                selectedDate = selectedDate,
                formattedDateBosnian = displaySchedule?.formattedDateBosnian() ?: "",
                hijriInfo = hijriInfo,
                isToday = isToday,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday
            )
        }

        // 4. Next Prayer Countdown Hero Card (active today)
        if (isToday && nextPrayer != null) {
            item {
                NextPrayerHeroCard(
                    nextPrayer = nextPrayer,
                    countdownString = countdownString,
                    largeText = userPreferences.largeText
                )
            }
        }

        // 5. Ramadan Card (if Ramadan month 9 or active)
        if (hijriInfo?.isRamadan == true) {
            item {
                RamadanHeroCard(
                    ramadanDay = hijriInfo.ramadanDay ?: 1,
                    imsakCountdown = imsakCountdown,
                    iftarCountdown = iftarCountdown,
                    schedule = displaySchedule
                )
            }
        }

        // 6. Six Prayer Times List
        item {
            PrayerTimesListCard(
                schedule = displaySchedule,
                nextPrayer = if (isToday) nextPrayer else null,
                isFriday = isFriday,
                isRamadan = (hijriInfo?.isRamadan == true),
                userPreferences = userPreferences
            )
        }

        // 7. Upcoming Holiday Card
        if (nextHoliday != null) {
            item {
                UpcomingHolidayCard(
                    holiday = nextHoliday,
                    onClick = onNavigateToHolidays
                )
            }
        }

        // 8. Cache & Source Status Banner
        item {
            StatusFooter(
                isApproximate = displaySchedule?.isApproximate == true,
                cachedDaysCount = cachedDaysCount
            )
        }
    }
}

@Composable
private fun HeaderBar(
    locationName: String,
    onNavigateToHolidays: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToDzemat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrayerAlarm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vaktija",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Džemat Oskova",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Text(
                text = "$locationName (BiH)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(
                onClick = onNavigateToPrayerAlarm,
                modifier = Modifier.testTag("prayer_alarm_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Zov na Sedždu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onNavigateToDzemat,
                modifier = Modifier.testTag("dzemat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = "Džemat Oskova",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(
                onClick = onNavigateToQibla,
                modifier = Modifier.testTag("qibla_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Kibla kompas",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onNavigateToHolidays,
                modifier = Modifier.testTag("holidays_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Mubarek dani i blagdani",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Postavke",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    silentModeEnabled: Boolean,
    onToggleSilentMode: (Boolean) -> Unit,
    onNavigateToPrayerAlarm: () -> Unit,
    onNavigateToDovarnik: () -> Unit,
    onNavigateToTesbih: () -> Unit,
    onNavigateToKaza: () -> Unit,
    onNavigateToRamadanTable: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Džamijski tihi režim toggle banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (silentModeEnabled) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .clickable { onToggleSilentMode(!silentModeEnabled) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (silentModeEnabled) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (silentModeEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Džamijski tihi mod",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (silentModeEnabled) "Utišano za vrijeme namaza u džamiji" else "Dodirni za utišavanje telefona u safu",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                androidx.compose.material3.Switch(
                    checked = silentModeEnabled,
                    onCheckedChange = { onToggleSilentMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Highlight Row: 1. Zov na Sedždu, 2. Dovarnik (Zbirka dova)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zov na Sedždu Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPrayerAlarm() }
                        .testTag("quick_prayer_alarm_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zov na Sedždu",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Budilnik namaza",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Dovarnik Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToDovarnik() }
                        .testTag("quick_dovarnik_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zbirka dova",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                            Text(
                                text = "Dovarnik i zikrovi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Quick Action Buttons: Tespih, Kaza namazi, Ramazan vaktija
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Tespih
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTesbih() }
                        .testTag("quick_tesbih_button")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Tespih",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // 2. Kaza namazi
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToKaza() }
                        .testTag("quick_kaza_button")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Kaza namazi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // 3. Ramazan Vaktija tabela
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToRamadanTable() }
                        .testTag("quick_ramadan_table_button")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Ramazan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimezoneNoticeCard(deviceZoneId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Vremena namaza su prikazana u zoni Europe/Sarajevo. Vaš uređaj je u zoni: $deviceZoneId.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
private fun DateNavigationCard(
    selectedDate: LocalDate,
    formattedDateBosnian: String,
    hijriInfo: HijriDateInfo?,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousDay,
                    modifier = Modifier.size(40.dp).testTag("prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Prethodni dan"
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (formattedDateBosnian.isNotEmpty()) formattedDateBosnian else selectedDate.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    if (hijriInfo != null) {
                        Text(
                            text = hijriInfo.formatBosnian(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                IconButton(
                    onClick = onNextDay,
                    modifier = Modifier.size(40.dp).testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Sljedeći dan"
                    )
                }
            }

            if (!isToday) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { onToday() }
                            .testTag("jump_today_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vrati na Danas",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NextPrayerHeroCard(
    nextPrayer: PrayerItem,
    countdownString: String,
    largeText: Boolean
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = NextPrayerGlow,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("next_prayer_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "SLJEDEĆI NAMAZ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Text(
                        text = nextPrayer.type.arabicName,
                        fontFamily = AmiriFontFamily,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = nextPrayer.type.bosnianName,
                        style = if (largeText) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = nextPrayer.formattedTime,
                        style = if (largeText) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Giant countdown with tabular figures
                Text(
                    text = countdownString,
                    style = if (largeText) {
                        MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp)
                    } else {
                        MaterialTheme.typography.displayLarge
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("countdown_text")
                )

                Text(
                    text = "preostalo vrijeme do vakta",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun RamadanHeroCard(
    ramadanDay: Int,
    imsakCountdown: String,
    iftarCountdown: String,
    schedule: DailyPrayerSchedule?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mubarek Ramazan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Dan $ramadanDay",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sehur / Imsak
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sehur (Imsak)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = imsakCountdown,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "nastupa: ${schedule?.fajr?.formattedTime ?: "--:--"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Iftar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iftar (Akšam)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = iftarCountdown,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Text(
                        text = "nastupa: ${schedule?.maghrib?.formattedTime ?: "--:--"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimesListCard(
    schedule: DailyPrayerSchedule?,
    nextPrayer: PrayerItem?,
    isFriday: Boolean,
    isRamadan: Boolean,
    userPreferences: UserPreferences
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Dnevna vaktija (6 namaskih vremena)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (schedule != null) {
                val nowTime = LocalTime.now()

                schedule.prayers.forEach { prayer ->
                    val isNext = (nextPrayer?.type == prayer.type)
                    val isPast = (nextPrayer != null && prayer.time.isBefore(nowTime))
                    val isEnabled = userPreferences.prayerNotifications[prayer.type] ?: true

                    PrayerRow(
                        prayer = prayer,
                        isNext = isNext,
                        isPast = isPast,
                        isNotificationEnabled = isEnabled,
                        largeText = userPreferences.largeText
                    )

                    // Friday Jumu'ah row after Dhuhr
                    if (isFriday && prayer.type == PrayerType.DHUHR) {
                        JumuahRow(
                            dhuhrTime = prayer.formattedTime,
                            customTime = userPreferences.jumuahCustomTime,
                            largeText = userPreferences.largeText
                        )
                    }

                    // Ramadan Teravih row after Isha
                    if (isRamadan && prayer.type == PrayerType.ISHA) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val teravihTime = prayer.time.plusMinutes(userPreferences.teravihMinutesAfterIsha.toLong())
                        val formattedTeravih = String.format(java.util.Locale.US, "%02d:%02d", teravihTime.hour, teravihTime.minute)
                        TeravihRow(
                            teravihTime = formattedTeravih,
                            largeText = userPreferences.largeText
                        )
                    }

                    if (prayer.type != PrayerType.ISHA) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            } else {
                Text(
                    text = "Učitavanje vaktije...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TeravihRow(
    teravihTime: String,
    largeText: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Mosque,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Teravih-namaz (Ramazan)",
                    style = if (largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Džemat Oskova (15 min poslije jacije)",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "التراويح",
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = teravihTime,
                style = if (largeText) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: PrayerItem,
    isNext: Boolean,
    isPast: Boolean,
    isNotificationEnabled: Boolean,
    largeText: Boolean
) {
    val backgroundColor = when {
        isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    val textColor = when {
        isNext -> MaterialTheme.colorScheme.onSurface
        isPast -> PastPrayerDim
        else -> MaterialTheme.colorScheme.onSurface
    }

    val timeColor = when {
        isNext -> MaterialTheme.colorScheme.secondary
        isPast -> PastPrayerDim
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isNext) 1.5.dp else 0.dp,
                color = if (isNext) NextPrayerGlow else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("prayer_row_${prayer.type.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isNotificationEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = if (isNotificationEnabled) timeColor else PastPrayerDim,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = prayer.type.bosnianName,
                    style = if (largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
                if (isNext) {
                    Text(
                        text = "Sljedeći namaz",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.type.arabicName,
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = if (isNext) MaterialTheme.colorScheme.secondary else textColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 14.dp)
            )

            Text(
                text = prayer.formattedTime,
                style = if (largeText) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = timeColor
            )
        }
    }
}

@Composable
private fun JumuahRow(
    dhuhrTime: String,
    customTime: String?,
    largeText: Boolean
) {
    val displayJumuahTime = customTime ?: dhuhrTime

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Mosque,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Džuma namaz (Petak)",
                    style = if (largeText) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (customTime != null) "Ručno podešeno vrijeme" else "Podne po vaktiji",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "الجمعة",
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = displayJumuahTime,
                style = if (largeText) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun UpcomingHolidayCard(
    holiday: Holiday,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("upcoming_holiday_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Najbliži mubarek dan / noć",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = holiday.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = holiday.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = holiday.remainingTimeDescription(ZonedDateTime.now()),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusFooter(
    isApproximate: Boolean,
    cachedDaysCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isApproximate) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Približno (astronomski proračun)",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Text(
                text = "Izvor: vaktija.ba • Lokalni keš: $cachedDaysCount dana spremno za offline rad",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
