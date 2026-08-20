package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.formatDuration
import com.example.data.model.formatPace
import com.example.service.LiveRunTelemetry
import com.example.service.RunTrackingState
import com.example.ui.components.ElevationProfileCanvas
import com.example.ui.components.PaceHeatmapCanvas
import com.example.ui.components.StatTile
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
fun LiveRunScreen(
    telemetry: LiveRunTelemetry,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onToggleVoiceCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMapViewActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Immersive UI Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (telemetry.state == RunTrackingState.RUNNING) "SESSION ACTIVE" else "SESSION PAUSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (telemetry.state == RunTrackingState.RUNNING) NeonLime else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (telemetry.isSimulationMode) "GBK Circuit Sprint" else "Outdoor Stride",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice Coach Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable { onToggleVoiceCoach() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (telemetry.isVoiceCoachEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Voice Coach",
                        tint = if (telemetry.isVoiceCoachEnabled) NeonLime else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Main Center: Giant Immersive Distance Metric or Route Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isMapViewActive) {
                PaceHeatmapCanvas(
                    points = telemetry.points,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    isLive = true,
                    showGrid = true
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Soft Neon Volt Atmospheric Glow
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.06f))
                            .blur(40.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", telemetry.distanceKm),
                            fontSize = 88.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            lineHeight = 90.sp
                        )
                        Text(
                            text = "KILOMETERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }
        }

        // 3. Immersive 2-Column Metrics & Elevation Profile
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2-Column Grid for Pace & BPM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Avg Pace",
                    value = formatPace(telemetry.avgPaceSecondsPerKm),
                    unit = "",
                    icon = Icons.Default.Speed,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    label = "Heart Rate",
                    value = "${telemetry.estimatedHeartRateBpm}",
                    unit = "BPM",
                    icon = Icons.Default.Favorite,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )
            }

            // Elevation Profile Bar
            ElevationProfileCanvas(splits = telemetry.splits)
        }

        // 4. Immersive Capsule Footer Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pill Container with Pause/Resume Button & Digital Stopwatch
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(40.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Play/Pause circular white button
                        if (telemetry.state == RunTrackingState.RUNNING) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onPause() }
                                    .testTag("pause_run_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = DarkObsidian,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(NeonLime)
                                        .clickable { onResume() }
                                        .testTag("resume_run_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = DarkObsidian,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(HyperCoral)
                                        .clickable { onFinish() }
                                        .testTag("finish_run_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Finish",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // Digital Stopwatch
                        Text(
                            text = formatDuration(telemetry.elapsedSeconds),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }

                // Circular Neon Volt Map / Dial Switcher
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(NeonLime)
                        .clickable { isMapViewActive = !isMapViewActive },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMapViewActive) Icons.Default.Speed else Icons.Default.Map,
                        contentDescription = "Toggle Map",
                        tint = DarkObsidian,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
