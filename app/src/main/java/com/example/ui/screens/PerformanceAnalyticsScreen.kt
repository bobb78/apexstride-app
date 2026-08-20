package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerformanceMetrics
import com.example.data.model.RunActivity
import com.example.ui.components.StatTile
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HyperCoral
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PerformanceAnalyticsScreen(
    metrics: PerformanceMetrics,
    recentRuns: List<RunActivity>,
    onAskAiCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = "Analytics",
                        tint = NeonLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ANALISIS KINERJA CERDAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Telemetri & Biomekanik Atletik",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // 1. VO2 Max Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceDark, SurfaceElevated)
                        )
                    )
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ESTIMASI VO2 MAX",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${metrics.vo2MaxEstimate}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonLime,
                                lineHeight = 50.sp
                            )
                            Text(
                                text = "ml/kg/min",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = metrics.vo2MaxCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }

                    // Circular Score Ring
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { metrics.vo2MaxEstimate / 70f },
                            modifier = Modifier.fillMaxSize(),
                            color = NeonLime,
                            trackColor = SurfaceElevated,
                            strokeWidth = 8.dp
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trend",
                            tint = NeonLime,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // 2. Pace & Biomechanics Metric Grid
        item {
            Text(
                text = "METRIK BIOMEKANIK & EFISIENSI",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        label = "Pace Consistency",
                        value = "${metrics.paceConsistencyScore}%",
                        unit = "INDEX",
                        icon = Icons.Default.Timeline,
                        accentColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )

                    StatTile(
                        label = "Avg Cadence",
                        value = "${metrics.avgCadenceSpm}",
                        unit = "SPM",
                        icon = Icons.Default.Speed,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        label = "Ground Contact",
                        value = "${metrics.groundContactTimeMs}",
                        unit = "MS",
                        icon = Icons.Default.DirectionsRun,
                        accentColor = BlazeOrange,
                        modifier = Modifier.weight(1f)
                    )

                    StatTile(
                        label = "Cardiac Drift",
                        value = "${metrics.cardiacDriftPercentage}%",
                        unit = "DRIFT",
                        icon = Icons.Default.Favorite,
                        accentColor = HyperCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Heart Rate Zones Distribution
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISTRIBUSI DETAK JANTUNG (HR ZONES)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Aerobic Base 52%",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                }

                val zoneColors = listOf(
                    ElectricCyan,
                    NeonLime,
                    AcidYellow,
                    BlazeOrange,
                    HyperCoral
                )

                metrics.heartRateZonesDurationSeconds.entries.toList().forEachIndexed { index, entry ->
                    val color = zoneColors.getOrElse(index) { NeonLime }
                    val totalSec = metrics.heartRateZonesDurationSeconds.values.sum().coerceAtLeast(1L)
                    val fraction = (entry.value.toFloat() / totalSec).coerceIn(0f, 1f)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = entry.key, fontSize = 11.sp, color = TextPrimary)
                            Text(
                                text = "${(fraction * 100).toInt()}% • ${entry.value / 60}m",
                                fontSize = 11.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = SurfaceElevated
                        )
                    }
                }
            }
        }

        // 4. Race Time Predictor (AI Calibrated)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Race", tint = AcidYellow, modifier = Modifier.size(16.dp))
                    Text(
                        text = "PREDIKSI WAKTU LOMBA (RACE TIME PREDICTOR)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AcidYellow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                metrics.racePredictions.forEach { (distance, predictedTime) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = distance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = predictedTime,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonLime,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 5. Smart Personalized AI Coaching Feedback
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SurfaceDark, SurfaceElevated)
                        )
                    )
                    .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = NeonLime, modifier = Modifier.size(18.dp))
                    Text(
                        text = "SARAN LATIHAN TERPERSONALISASI",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                }

                metrics.smartFeedbackList.forEach { feedback ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", color = NeonLime, fontWeight = FontWeight.Bold)
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onAskAiCoach,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("consult_coach_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = DarkObsidian
                    )
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = "Coach", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Konsultasi Rencana Latihan dengan Coach AI", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
