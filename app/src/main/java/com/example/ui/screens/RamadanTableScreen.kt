package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyPrayerSchedule
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanTableScreen(
    locationName: String,
    schedules: List<DailyPrayerSchedule>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    fun shareRamadanTable() {
        val sb = StringBuilder()
        sb.append("🌙 RAMAZANSKA VAKTIJA – DŽEMAT OSKOVA ($locationName)\n\n")
        sb.append("Dan | Datum | Imsak | Sabah | Podne | Ikindija | Iftar | Jacija\n")
        sb.append("-------------------------------------------------------------\n")
        schedules.take(30).forEachIndexed { idx, s ->
            val dayNum = idx + 1
            val dStr = s.date.format(DateTimeFormatter.ofPattern("dd.MM."))
            val imsak = s.fajr.time.minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm"))
            sb.append(String.format(
                java.util.Locale.US,
                "%02d. | %s | %s | %s | %s | %s | %s | %s\n",
                dayNum, dStr, imsak, s.fajr.formattedTime, s.dhuhr.formattedTime,
                s.asr.formattedTime, s.maghrib.formattedTime, s.isha.formattedTime
            ))
        }
        sb.append("\nIzvor: vaktija.ba • Aplikacija 'Vaktija – Džemat Oskova'")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Ramazanska vaktija – Džemat Oskova")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Podijeli Ramazansku vaktiju"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ramazanska vaktija",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ramadan_table_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = { shareRamadanTable() }, modifier = Modifier.testTag("share_ramadan_button")) {
                        Icon(Icons.Default.Share, contentDescription = "Podijeli vaktiju")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ramazanska vaktija (30 dana)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "$locationName – Džemat Oskova",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    }

                    Button(onClick = { shareRamadanTable() }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Podijeli")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable Table
            val horizontalScroll = rememberScrollState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScroll)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("Dan", width = 45.dp, isHeader = true)
                        TableCell("Datum", width = 75.dp, isHeader = true)
                        TableCell("Sehur", width = 60.dp, isHeader = true, isHighlighted = true)
                        TableCell("Zora", width = 60.dp, isHeader = true)
                        TableCell("Podne", width = 60.dp, isHeader = true)
                        TableCell("Ikindija", width = 60.dp, isHeader = true)
                        TableCell("Iftar", width = 60.dp, isHeader = true, isHighlighted = true)
                        TableCell("Jacija", width = 60.dp, isHeader = true)
                    }

                    HorizontalDivider()

                    // Rows
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(schedules.take(30)) { idx, s ->
                            val dayNum = idx + 1
                            val dateStr = s.date.format(DateTimeFormatter.ofPattern("dd. MMM"))
                            val imsakStr = s.fajr.time.minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm"))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("$dayNum.", width = 45.dp)
                                TableCell(dateStr, width = 75.dp)
                                TableCell(imsakStr, width = 60.dp, isHighlighted = true)
                                TableCell(s.fajr.formattedTime, width = 60.dp)
                                TableCell(s.dhuhr.formattedTime, width = 60.dp)
                                TableCell(s.asr.formattedTime, width = 60.dp)
                                TableCell(s.maghrib.formattedTime, width = 60.dp, isHighlighted = true)
                                TableCell(s.isha.formattedTime, width = 60.dp)
                            }
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isHighlighted: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = if (isHeader) {
            MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )
        } else {
            MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )
        },
        textAlign = TextAlign.Center
    )
}
