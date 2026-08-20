package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KmSplit
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun CircularSpeedRing(
    currentSpeedKmh: Double,
    currentPaceText: String,
    cadenceSpm: Int,
    modifier: Modifier = Modifier
) {
    val maxSpeed = 20.0
    val targetSweep = ((currentSpeedKmh / maxSpeed) * 260.0).toFloat().coerceIn(10f, 260f)
    val animatedSweep by animateFloatAsState(targetValue = targetSweep, label = "speedSweep")

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 12f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background track arc (from 140° to 400° -> 260° total)
            drawArc(
                color = SurfaceElevated.copy(alpha = 0.6f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Radiant Immersive Volt sweep
            val brush = Brush.sweepGradient(
                colors = listOf(
                    NeonLime.copy(alpha = 0.4f),
                    NeonLime,
                    ElectricCyan
                )
            )

            drawArc(
                brush = brush,
                startAngle = 140f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CURRENT PACE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentPaceText,
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speed",
                    tint = NeonLime,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.1f km/h • %d SPM", currentSpeedKmh, cadenceSpm),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    accentColor: Color = NeonLime,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier
                    .size(22.dp)
                    .padding(bottom = 2.dp)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            Text(
                text = label.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun SplitsBarChart(
    splits: List<KmSplit>,
    modifier: Modifier = Modifier
) {
    if (splits.isEmpty()) return

    val fastestPace = splits.minOf { it.paceSecondsPerKm }
    val slowestPace = max(splits.maxOf { it.paceSecondsPerKm }, fastestPace + 1)
    val paceSpread = (slowestPace - fastestPace).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPLIT PER KILOMETER",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Tercepat: ${com.example.data.model.formatPace(fastestPace)}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonLime,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        splits.forEach { split ->
            val isFastest = split.paceSecondsPerKm == fastestPace
            val barRatio = 1.0f - ((split.paceSecondsPerKm - fastestPace).toFloat() / (paceSpread * 2f))
            val safeRatio = barRatio.coerceIn(0.25f, 1.0f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KM ${split.kmNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFastest) NeonLime else TextSecondary,
                    fontWeight = if (isFastest) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(42.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // Background rail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceElevated)
                    )

                    // Active split bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(safeRatio)
                            .height(6.dp)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isFastest) NeonLime else ElectricCyan
                            )
                    )
                }

                Text(
                    text = split.formattedPace,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isFastest) NeonLime else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(55.dp)
                )

                Text(
                    text = "+${split.elevationGainMeters}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}

@Composable
fun ElevationProfileCanvas(
    splits: List<KmSplit>,
    modifier: Modifier = Modifier
) {
    val totalElev = splits.sumOf { it.elevationGainMeters }.coerceAtLeast(45)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ELEVATION PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "+${totalElev}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }

            // Segmented Vertical Pill Bars (as in Immersive UI design!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.30f, 0.45f, 0.35f, 0.60f, 0.85f, 0.70f, 0.50f, 0.40f, 0.30f, 0.20f)
                heights.forEachIndexed { index, fraction ->
                    val isPeak = index == 3 || index == 4
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(fraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (isPeak) NeonLime else SurfaceElevated
                            )
                    )
                }
            }
        }
    }
}
