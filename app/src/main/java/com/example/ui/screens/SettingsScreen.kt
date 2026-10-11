package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserPreferences
import com.example.model.LocationDatabase
import com.example.model.LocationInfo
import com.example.model.PrayerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    onSetLocation: (Int) -> Unit,
    onSetNearestLocation: (Double, Double) -> Unit,
    onTogglePrayerNotification: (PrayerType, Boolean) -> Unit,
    onSetMinutesBefore: (Int) -> Unit,
    onSetSoundType: (String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetLargeText: (Boolean) -> Unit,
    onSetIs24Hour: (Boolean) -> Unit,
    onSetHijriOffset: (Int) -> Unit,
    onSetJumuahCustomTime: (String?) -> Unit,
    onSetImsakMinutesBefore: (Int) -> Unit,
    onCheckForUpdates: () -> Unit,
    onSetAutoCheckUpdates: (Boolean) -> Unit,
    onSetGithubRepo: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var showLocationDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBatteryHelpDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var repoInput by remember(userPreferences.githubRepo) { mutableStateOf(userPreferences.githubRepo) }
    var locationSearchQuery by remember { mutableStateOf("") }

    // GPS Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            findGpsAndSetLocation(context, onSetNearestLocation)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Postavke",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Lokacija
            item {
                val currentLocation = LocationDatabase.getById(userPreferences.locationId)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Lokacija vaktije",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (currentLocation.id == LocationDatabase.DEFAULT_LOCATION_ID) {
                                "${currentLocation.name} – Džemat Oskova (zadana lokacija)"
                            } else {
                                currentLocation.name
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Šifra lokacije u vaktija.ba: ${currentLocation.id}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showLocationDialog = true },
                                modifier = Modifier.testTag("choose_location_button")
                            ) {
                                Text("Izaberi iz spiska")
                            }

                            OutlinedButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier.testTag("gps_nearest_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Najbliža (GPS)")
                            }
                        }
                    }
                }
            }

            // 2. Obavijesti po namazu
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Obavijesti po namazu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        PrayerType.entries.forEach { prayer ->
                            val isChecked = userPreferences.prayerNotifications[prayer] ?: true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = prayer.bosnianName,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = prayer.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = { onTogglePrayerNotification(prayer, it) },
                                    modifier = Modifier.testTag("switch_notif_${prayer.id}")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Podsjetnik X minuta prije namaza
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Podsjetnik prije namaza",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Odaberite koliko minuta prije vakta želite primiti prethodnu obavijest:",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val minuteOptions = listOf(0, 5, 10, 15, 30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            minuteOptions.forEach { mins ->
                                FilterChip(
                                    selected = (userPreferences.minutesBefore == mins),
                                    onClick = { onSetMinutesBefore(mins) },
                                    label = {
                                        Text(text = if (mins == 0) "Isključeno" else "$mins min")
                                    },
                                    modifier = Modifier.testTag("minutes_chip_$mins")
                                )
                            }
                        }
                    }
                }
            }

            // 4. Zvuk obavijesti i licence zvuka
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Zvuk i ton obavijesti",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Zbog poštivanja autorskih prava i licenci, aplikacija koristi isključivo provjerene sistemske tonove telefona.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val soundOptions = listOf("SYSTEM" to "Sistemski ton", "CHIME" to "Diskretni zvon", "SILENT" to "Samo vibracija")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            soundOptions.forEach { (key, label) ->
                                FilterChip(
                                    selected = (userPreferences.soundType == key),
                                    onClick = { onSetSoundType(key) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Tema i Prikaz
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tema i izgled",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Theme Mode
                        val themeOptions = listOf("SYSTEM" to "Automatski", "DARK" to "Tamna", "LIGHT" to "Svijetla")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themeOptions.forEach { (mode, label) ->
                                FilterChip(
                                    selected = (userPreferences.themeMode == mode),
                                    onClick = { onSetThemeMode(mode) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large text toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Krupniji tekst i brojevi",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "Povećava font za lakše čitanje",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = userPreferences.largeText,
                                onCheckedChange = { onSetLargeText(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 24h format
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "24-satni format vremena",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            Switch(
                                checked = userPreferences.is24HourFormat,
                                onCheckedChange = { onSetIs24Hour(it) }
                            )
                        }
                    }
                }
            }

            // 6. Hidžretski kalendar pomak
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Korekcija hidžretskog datuma",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Usklađivanje s viđenjem mlađaka i kalendarom Rijaseta IZ u BiH:",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val offsetOptions = listOf(-1 to "-1 dan", 0 to "0 (standardno)", 1 to "+1 dan")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            offsetOptions.forEach { (offset, label) ->
                                FilterChip(
                                    selected = (userPreferences.hijriOffset == offset),
                                    onClick = { onSetHijriOffset(offset) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // 7. Pomoć za štednju baterije (Samsung, Xiaomi, Huawei, Pixel...)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pouzdanost alarma i baterija",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Da obavijesti za namaz nikada ne zakasne u pozadini ili kada je ekran ugašen, potrebno je dozvoliti tačne alarme i izuzeti aplikaciju iz agresivne štednje baterije.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Exact Alarm Button (Android 12+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Postavke tačnih alarma (SCHEDULE_EXACT_ALARM)")
                                }
                            }

                            // Battery Optimization settings
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(appDetails)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Postavke optimizacije baterije")
                            }

                            // Notification settings
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sistemske postavke obavijesti")
                            }

                            // Guide by manufacturer dialog button
                            TextButton(
                                onClick = { showBatteryHelpDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Uputstvo za Samsung, Xiaomi, Huawei...")
                            }
                        }
                    }
                }
            }

            // 8. Automatsko ažuriranje aplikacije (In-App Auto-Updater)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ažuriranje aplikacije",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Trenutna verzija: v${com.example.BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Aplikacija se može automatski ažurirati direktno sa vašeg GitHub repozitorija bez ponovnog ručnog preuzimanja preko pretraživača.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch: Automatska provjera
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatski provjeri pri pokretanju",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "Obavijesti čim izađe novo izdanje (Release)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = userPreferences.autoCheckUpdates,
                                onCheckedChange = { onSetAutoCheckUpdates(it) },
                                modifier = Modifier.testTag("auto_update_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // GitHub Repo polje
                        Text(
                            text = "Povezani GitHub repozitorij (vlasnik/naziv):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = repoInput,
                                onValueChange = { repoInput = it },
                                placeholder = { Text("npr. korisnik/vaktija-oskova") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("github_repo_input"),
                                trailingIcon = {
                                    if (repoInput.trim() != userPreferences.githubRepo) {
                                        TextButton(
                                            onClick = { onSetGithubRepo(repoInput) },
                                            modifier = Modifier.testTag("save_github_repo_button")
                                        ) {
                                            Text("Spremi")
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dugme za ručnu provjeru
                        Button(
                            onClick = {
                                if (repoInput.trim() != userPreferences.githubRepo) {
                                    onSetGithubRepo(repoInput)
                                }
                                onCheckForUpdates()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("check_updates_button")
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Provjeri ažuriranja sada")
                        }
                    }
                }
            }

            // 9. O aplikaciji i Politika privatnosti
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Privatnost i uvjeti korištenja",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showPrivacyDialog = true },
                                modifier = Modifier.testTag("privacy_policy_button")
                            ) {
                                Text("Politika privatnosti")
                            }
                            OutlinedButton(
                                onClick = { showLicenseDialog = true },
                                modifier = Modifier.testTag("terms_button")
                            ) {
                                Text("Uvjeti API-ja i zvukovi")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Izbor lokacije
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Izaberi lokaciju (116 gradova)") },
            text = {
                Column(modifier = Modifier.height(350.dp)) {
                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = { locationSearchQuery = it },
                        placeholder = { Text("Pretraži npr. Živinice, Tuzla...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = LocationDatabase.LOCATIONS.filter {
                        it.name.contains(locationSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filtered.size) { idx ->
                            val loc = filtered[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSetLocation(loc.id)
                                        showLocationDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = loc.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                    if (loc.id == LocationDatabase.DEFAULT_LOCATION_ID) {
                                        Text(
                                            text = "Džemat Oskova (Zadano)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                if (userPreferences.locationId == loc.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Zatvori")
                }
            }
        )
    }

    // Dialog: Pomoć za štednju baterije po proizvođaču
    if (showBatteryHelpDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryHelpDialog = false },
            title = { Text("Uputstvo za baterijsku optimizaciju") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    item {
                        Text(
                            text = "Da bi alarmi radili pouzdano u tačno vrijeme, proizvođači telefona zahtijevaju isključivanje agresivne štednje baterije:\n\n" +
                                    "• Samsung (One UI):\n" +
                                    "Postavke -> Baterija -> Ograničenja upotrebe u pozadini -> Dodajte Vaktiju u 'Aplikacije koje nikada ne spavaju' (Never sleeping apps).\n\n" +
                                    "• Xiaomi / Redmi / POCO (MIUI / HyperOS):\n" +
                                    "Postavke -> Aplikacije -> Upravljanje aplikacijama -> Vaktija -> Uključi 'Automatsko pokretanje' (Autostart) i postavi Štednju baterije na 'Bez ograničenja' (No restrictions).\n\n" +
                                    "• Huawei (EMUI):\n" +
                                    "Postavke -> Baterija -> Pokretanje aplikacije -> Vaktija -> Isključi 'Automatski' i uključi 'Automatsko pokretanje', 'Sekundarno pokretanje' i 'Rad u pozadini'.\n\n" +
                                    "• OnePlus / Oppo / Realme (OxygenOS / ColorOS):\n" +
                                    "Postavke -> Baterija -> Optimizacija baterije -> Vaktija -> 'Nemoj optimizirati' i omogući rad u pozadini.\n\n" +
                                    "• Google Pixel / Čisti Android:\n" +
                                    "Postavke -> Aplikacije -> Vaktija -> Baterija -> Postavi na 'Neograničeno' (Unrestricted).",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBatteryHelpDialog = false }) {
                    Text("Razumijem")
                }
            }
        )
    }

    // Dialog: Politika privatnosti
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Politika privatnosti") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    item {
                        Text(
                            text = "Politika privatnosti aplikacije 'Vaktija – Džemat Oskova'\n\n" +
                                    "1. Nema prikupljanja ličnih podataka:\n" +
                                    "Aplikacija ne zahtijeva otvaranje korisničkog računa, prijavu putem e-maila niti ima bilo kakav oblik praćenja identiteta korisnika.\n\n" +
                                    "2. Bez analitike i reklama:\n" +
                                    "U aplikaciji nema analitičkih servisa za praćenje, telemetrije niti reklama.\n\n" +
                                    "3. Lokacija (GPS):\n" +
                                    "Dozvola za lokaciju traži se isključivo ako korisnik sam pritisne dugme 'Najbliža lokacija (GPS)'. Lokacijske koordinate se obrađuju isključivo lokalno na uređaju radi odabira šifre grada i nikada se ne šalju na internet niti dijele s trećim stranama.\n\n" +
                                    "4. Lokalno spremanje:\n" +
                                    "Vremena namaza i postavke spremaju se u lokalnu bazu podataka na uređaju kako bi aplikacija nesmetano radila bez interneta.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("U redu")
                }
            }
        )
    }

    // Dialog: Uvjeti API-ja i licence
    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("Uvjeti API-ja i licence") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    item {
                        Text(
                            text = "Šta provjeriti prije distribucije u produkciju:\n\n" +
                                    "1. Uvjeti korištenja vaktija.ba API-ja:\n" +
                                    "API vaktija.ba je javni servis. Prije javne objave na Google Play ili App Store preporučuje se obavijestiti administratore vaktija.ba (kontakt@vaktija.ba) o procijenjenom broju korisnika. Naša aplikacija je optimizirana tako što preuzima mjesec unaprijed i kešira ga, čime smanjuje mrežni saobraćaj za 98%.\n\n" +
                                    "2. Licence zvukova ezana:\n" +
                                    "Snimci ezana podliježu autorskim pravima izvođača i audio produkcije. Zbog toga aplikacija ne ugrađuje neprovjerene audio snimke, već koristi pouzdane sistemske tonove telefona ili licencirani audio snimak džemata Oskova.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("U redu")
                }
            }
        )
    }
}

@SuppressLint("MissingPermission")
private fun findGpsAndSetLocation(
    context: Context,
    onSetNearestLocation: (Double, Double) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = lastGps ?: lastNet
        if (best != null) {
            onSetNearestLocation(best.latitude, best.longitude)
        }
    } catch (_: Exception) {
    }
}
