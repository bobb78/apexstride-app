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
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
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
import com.example.ui.components.RealRouteMapView
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
fun LiveRunScreen(
    telemetry: LiveRunTelemetry,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onToggleVoiceCoach: () -> Unit = {},
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
        // 1. Athletic Session Header with GPS Movement Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (telemetry.isActivelyMoving) NeonLime else HyperCoral)
                    )
                    Text(
                        text = if (telemetry.state == RunTrackingState.RUNNING) {
                            if (telemetry.isActivelyMoving) "PELACAKAN AKTIF" else "SENSOR: DIAM"
                        } else {
                            "SESI DIJEDA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (telemetry.isActivelyMoving) NeonLime else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = telemetry.movementStatusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (telemetry.isActivelyMoving) ElectricCyan else TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice Guidance Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.6f))
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

        // 2. Main Center: Giant Telemetry or Real Geographic GPS Route Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isMapViewActive) {
                RealRouteMapView(
                    points = telemetry.points,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 10.dp),
                    isLive = true
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Soft Volt Atmospheric Glow
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
                            text = "KILOMETER",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Speed Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = "Cadence",
                                    tint = NeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${telemetry.currentCadenceSpm} SPM • ${String.format(Locale.US, "%.1f", telemetry.currentSpeedKmh)} KM/H",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 2-Column Metrics & Elevation Profile
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "Pace Rata-rata",
                    value = formatPace(telemetry.avgPaceSecondsPerKm),
                    unit = "/KM",
                    icon = Icons.Default.Speed,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    label = "Detak Jantung",
                    value = "${telemetry.estimatedHeartRateBpm}",
                    unit = "BPM",
                    icon = Icons.Default.Favorite,
                    accentColor = HyperCoral,
                    modifier = Modifier.weight(1f)
                )
            }

            // Elevation Profile Bar
            ElevationProfileCanvas(splits = telemetry.splits)
        }

        // 4. Action Controls & Map Switcher
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

                // Peta / Telemetry Switcher Button
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
                        contentDescription = "Buka Peta Asli",
                        tint = DarkObsidian,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
