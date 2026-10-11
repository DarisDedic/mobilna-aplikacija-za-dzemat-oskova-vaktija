package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KazaScreen(
    userPreferences: UserPreferences,
    onUpdateKaza: (fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int, witr: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val totalPrayers = userPreferences.kazaFajr + userPreferences.kazaDhuhr +
            userPreferences.kazaAsr + userPreferences.kazaMaghrib +
            userPreferences.kazaIsha + userPreferences.kazaWitr
    val totalDays = totalPrayers / 6

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kaza namazi",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("kaza_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
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
                .testTag("kaza_screen_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ukupno za nadoknaditi",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalPrayers namaza",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "približno $totalDays punih dana (po 6 namaza dnevno s vitrom)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Info note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Podaci o propuštenim namazima se čuvaju isključivo na vašem telefonu radi vaše lične evidencije.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // 6 Prayer Rows
            val prayerRows = listOf(
                "Sabah (Farz)" to userPreferences.kazaFajr,
                "Podne (Farz)" to userPreferences.kazaDhuhr,
                "Ikindija (Farz)" to userPreferences.kazaAsr,
                "Akšam (Farz)" to userPreferences.kazaMaghrib,
                "Jacija (Farz)" to userPreferences.kazaIsha,
                "Vitr-namaz" to userPreferences.kazaWitr
            )

            items(prayerRows.size) { index ->
                val (name, value) = prayerRows[index]
                KazaPrayerItem(
                    name = name,
                    value = value,
                    onIncrement = {
                        val newValues = getUpdatedValues(userPreferences, index, delta = 1)
                        onUpdateKaza(newValues[0], newValues[1], newValues[2], newValues[3], newValues[4], newValues[5])
                    },
                    onDecrement = {
                        val newValues = getUpdatedValues(userPreferences, index, delta = -1)
                        onUpdateKaza(newValues[0], newValues[1], newValues[2], newValues[3], newValues[4], newValues[5])
                    },
                    onAddFive = {
                        val newValues = getUpdatedValues(userPreferences, index, delta = 5)
                        onUpdateKaza(newValues[0], newValues[1], newValues[2], newValues[3], newValues[4], newValues[5])
                    }
                )
            }
        }
    }
}

private fun getUpdatedValues(prefs: UserPreferences, index: Int, delta: Int): IntArray {
    val arr = intArrayOf(
        prefs.kazaFajr, prefs.kazaDhuhr, prefs.kazaAsr,
        prefs.kazaMaghrib, prefs.kazaIsha, prefs.kazaWitr
    )
    arr[index] = (arr[index] + delta).coerceAtLeast(0)
    return arr
}

@Composable
private fun KazaPrayerItem(
    name: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAddFive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$value preostalo",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(onClick = onDecrement, enabled = value > 0) {
                    Icon(Icons.Default.Remove, contentDescription = "Smanji za 1")
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "$value",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                FilledTonalIconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Povećaj za 1")
                }

                OutlinedButton(
                    onClick = onAddFive,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text("+5")
                }
            }
        }
    }
}
