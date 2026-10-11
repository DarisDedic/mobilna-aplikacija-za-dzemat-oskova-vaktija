package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentBright
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    locationName: String,
    qiblaBearing: Float, // Calculated target angle to Kaaba
    deviceHeading: Float, // Current device rotation from North
    sensorAccuracyWarning: Boolean,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    // Compass needle points towards Kaaba relative to the phone's top
    val relativeAngle = (qiblaBearing - deviceHeading + 360f) % 360f
    val animatedAngle by animateFloatAsState(
        targetValue = relativeAngle,
        animationSpec = spring(stiffness = 300f),
        label = "compass_rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kibla kompas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("qibla_back_button")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .testTag("qibla_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pravac Kabe: $locationName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Azimut: ${qiblaBearing.toInt()}° (Jugoistok) • Kaaba (21.4225° N, 39.8262° E)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Circular Compass Dial
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // Background Compass Dial
                CompassDialCanvas(
                    deviceHeading = deviceHeading,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    goldColor = GoldAccent
                )

                // Rotating Needle pointing to Qibla
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedAngle),
                    contentAlignment = Alignment.Center
                ) {
                    // Needle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Kaaba icon / Gold tip pointing towards Kaaba
                        Surface(
                            shape = CircleShape,
                            color = GoldAccentBright,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Kaaba pravac",
                                    tint = Color(0xFF451A03),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Needle line
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(90.dp)
                                .background(GoldAccent)
                        )

                        // Center circle
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(20.dp)
                        ) {}

                        // Tail of needle (North indicator / dim)
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(70.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                }
            }

            // Calibration & Status Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sensorAccuracyWarning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
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
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Senzor kompasa zahtijeva kalibraciju. Pomjerajte uređaj u obliku broja 8 u zraku.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Držite uređaj vodoravno i udaljeno od jakih metalnih i magnetnih predmeta za maksimalnu tačnost.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassDialCanvas(
    deviceHeading: Float,
    primaryColor: Color,
    goldColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 16.dp.toPx()

        // Outer ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.15f),
            radius = radius * 0.75f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Cardinal tick marks
        for (i in 0 until 360 step 30) {
            val angleRad = Math.toRadians(i.toDouble() - 90.0)
            val isCardinal = (i % 90 == 0)
            val tickLength = if (isCardinal) 16.dp.toPx() else 8.dp.toPx()
            val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
            val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
            val endX = (center.x + radius * cos(angleRad)).toFloat()
            val endY = (center.y + radius * sin(angleRad)).toFloat()

            drawLine(
                color = if (isCardinal) goldColor else primaryColor.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx()
            )
        }
    }
}
