package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RunActivity
import com.example.ui.components.ElevationProfileCanvas
import com.example.ui.components.PaceHeatmapCanvas
import com.example.ui.components.SplitsBarChart
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
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunSummaryScreen(
    run: RunActivity,
    isLoadingAi: Boolean,
    onGenerateAiAudit: () -> Unit,
    onSaveAndClose: (title: String, feeling: String, shoe: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleText by remember { mutableStateOf(run.title) }
    var selectedFeeling by remember { mutableStateOf(run.feelingTag) }
    var shoeText by remember { mutableStateOf(run.shoeName) }
    var showShareSheet by remember { mutableStateOf(false) }

    val feelingOptions = listOf("🔥 Kuat", "🚀 Luar Biasa", "✨ Segar", "💨 Cepat", "💪 Capek Tapi Puas")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RINGKASAN AKTIVITAS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkObsidian,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkObsidian)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onSaveAndClose(titleText, selectedFeeling, shoeText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_run_summary_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = DarkObsidian
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Save and Sync",
                        tint = DarkObsidian,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SIMPAN & SINKRONKAN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        containerColor = DarkObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkObsidian),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Title & Feeling Tag Edit Box
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Judul Sesi Lari", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Feeling selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        feelingOptions.take(3).forEach { feeling ->
                            val isSelected = selectedFeeling == feeling
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) NeonLime else SurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(20.dp))
                                    .clickable { selectedFeeling = feeling }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = feeling,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) DarkObsidian else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Giant Headline Telemetry
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SurfaceDark,
                                    SurfaceElevated
                                )
                            )
                        )
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TOTAL JARAK TEMPUH",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = run.formattedDistance,
                                style = MaterialTheme.typography.displayLarge,
                                color = NeonLime,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "KM",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DURASI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = run.formattedDuration,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(SurfaceBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AVG PACE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${run.formattedPace}/km",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(SurfaceBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "KALORI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${run.caloriesBurned}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BlazeOrange,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Interactive Pace Heatmap Canvas
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RUTE & HEATMAP PACE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    PaceHeatmapCanvas(
                        points = run.routePoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        isLive = false,
                        showGrid = true
                    )
                }
            }

            // 4. Biometrics Stat Tiles Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            label = "Cadence Rata-rata",
                            value = "${run.avgCadenceSpm}",
                            unit = "SPM",
                            icon = Icons.Default.DirectionsRun,
                            accentColor = NeonLime,
                            modifier = Modifier.weight(1f)
                        )

                        StatTile(
                            label = "Heart Rate",
                            value = "${run.avgHeartRateBpm}",
                            unit = "BPM",
                            icon = Icons.Default.Favorite,
                            accentColor = HyperCoral,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            label = "Elevasi Mendaki",
                            value = "+${run.elevationGainMeters}",
                            unit = "METER",
                            icon = Icons.Default.Terrain,
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )

                        StatTile(
                            label = "Top Speed Pace",
                            value = com.example.data.model.formatPace(run.maxPaceSecondsPerKm),
                            unit = "/KM",
                            icon = Icons.Default.Speed,
                            accentColor = BlazeOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Splits & Elevation Charts
            item {
                SplitsBarChart(splits = run.splits)
            }

            item {
                ElevationProfileCanvas(splits = run.splits)
            }

            // 6. Gemini 3.1 Pro High Thinking Deep Analysis Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Coach",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "APEX INTELLIGENCE AUDIT",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Powered by Gemini 3.1 Pro (High Thinking)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan
                                    )
                                }
                            }

                            if (isLoadingAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = NeonLime,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                IconButton(
                                    onClick = onGenerateAiAudit,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh AI",
                                        tint = NeonLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (run.aiAnalysis.isNullOrBlank()) {
                            Text(
                                text = "Dapatkan audit fisiologi biomekanik, stabilitas pace, dan rekomendasi pemulihan dari AI Coach kelas dunia.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Button(
                                onClick = onGenerateAiAudit,
                                enabled = !isLoadingAi,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = DarkObsidian
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Audit",
                                    tint = DarkObsidian
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MULAI AUDIT HIGH THINKING",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            Text(
                                text = run.aiAnalysis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Share Poster Modal Sheet
    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor = DarkObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "POSTER PREVIEW ATLET",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )

                // Share Poster Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceDark, SlateDark)
                            )
                        )
                        .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "APEX STRIDE // ATLET PROTOCOL",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonLime,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${run.formattedDistance} KM",
                            style = MaterialTheme.typography.displayLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Pace ${run.formattedPace}/km • Waktu ${run.formattedDuration}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        PaceHeatmapCanvas(
                            points = run.routePoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            isLive = false,
                            showGrid = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${run.title} • ${run.formattedDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Button(
                    onClick = { showShareSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = DarkObsidian
                    )
                ) {
                    Text("BAGIKAN KE MEDIA SOSIAL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
