package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GPSPoint
import com.example.data.model.formatPace
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
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

enum class MapColorMode {
    PACE_HEATMAP,
    ELEVATION_PROFILE
}

@Composable
fun PaceHeatmapCanvas(
    points: List<GPSPoint>,
    modifier: Modifier = Modifier,
    isLive: Boolean = false,
    showGrid: Boolean = true,
    initialColorMode: MapColorMode = MapColorMode.PACE_HEATMAP
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedPoint by remember { mutableStateOf<GPSPoint?>(null) }
    var colorMode by remember { mutableStateOf(initialColorMode) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .background(SlateDark, RoundedCornerShape(24.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 4.5f)
                        panOffset += pan
                    }
                }
                .pointerInput(points, scale, panOffset) {
                    detectTapGestures { tapOffset ->
                        // Inspect nearest point on tap
                        if (points.isNotEmpty()) {
                            // Find nearest coordinate in screen space
                            // Handled via state calculations below
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Tactical Grid
            if (showGrid) {
                drawGrid(width, height)
            }

            if (points.isEmpty()) {
                // Empty radar idle state
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.15f),
                    radius = width * 0.25f,
                    center = Offset(width / 2f, height / 2f),
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.08f),
                    radius = width * 0.4f,
                    center = Offset(width / 2f, height / 2f),
                    style = Stroke(width = 1.5f)
                )
                return@Canvas
            }

            // 2. Compute GPS Bounding Box
            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLng = Double.MAX_VALUE
            var maxLng = -Double.MAX_VALUE
            var minAlt = Double.MAX_VALUE
            var maxAlt = -Double.MAX_VALUE
            var minSpeed = Float.MAX_VALUE
            var maxSpeed = Float.MIN_VALUE

            for (p in points) {
                minLat = min(minLat, p.latitude)
                maxLat = max(maxLat, p.latitude)
                minLng = min(minLng, p.longitude)
                maxLng = max(maxLng, p.longitude)
                minAlt = min(minAlt, p.altitude)
                maxAlt = max(maxAlt, p.altitude)
                minSpeed = min(minSpeed, p.speed)
                maxSpeed = max(maxSpeed, p.speed)
            }

            val latRange = max(maxLat - minLat, 0.0004)
            val lngRange = max(maxLng - minLng, 0.0004)
            val speedRange = max(maxSpeed - minSpeed, 1.0f)
            val altRange = max(maxAlt - minAlt, 5.0)

            val padding = 44f
            val usableWidth = width - (padding * 2f)
            val usableHeight = height - (padding * 2f)

            fun toScreenCoord(p: GPSPoint): Offset {
                val xNorm = ((p.longitude - minLng) / lngRange).toFloat()
                val yNorm = (1.0 - ((p.latitude - minLat) / latRange)).toFloat()
                return Offset(
                    x = padding + (xNorm * usableWidth),
                    y = padding + (yNorm * usableHeight)
                )
            }

            // Apply interactive pan and zoom scale
            translate(left = panOffset.x, top = panOffset.y) {
                scale(scale = scale, pivot = Offset(width / 2f, height / 2f)) {
                    // 3. Draw Path Glow
                    if (points.size >= 2) {
                        val glowPath = Path()
                        val firstCoord = toScreenCoord(points.first())
                        glowPath.moveTo(firstCoord.x, firstCoord.y)
                        for (i in 1 until points.size) {
                            val c = toScreenCoord(points[i])
                            glowPath.lineTo(c.x, c.y)
                        }

                        // Wide neon glow backdrop
                        drawPath(
                            path = glowPath,
                            color = if (colorMode == MapColorMode.PACE_HEATMAP) NeonLime.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f),
                            style = Stroke(
                                width = 14f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Segment-by-segment color rendering
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val c1 = toScreenCoord(p1)
                            val c2 = toScreenCoord(p2)

                            val segmentColor = if (colorMode == MapColorMode.PACE_HEATMAP) {
                                val normSpeed = ((p2.speed - minSpeed) / speedRange).coerceIn(0f, 1f)
                                when {
                                    normSpeed > 0.65f -> NeonLime
                                    normSpeed > 0.35f -> ElectricCyan
                                    else -> HyperCoral
                                }
                            } else {
                                // Elevation Gradient
                                val normAlt = ((p2.altitude - minAlt) / altRange).toFloat().coerceIn(0f, 1f)
                                when {
                                    normAlt > 0.7f -> BlazeOrange
                                    normAlt > 0.35f -> AcidYellow
                                    else -> ElectricCyan
                                }
                            }

                            drawLine(
                                color = segmentColor,
                                start = c1,
                                end = c2,
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw KM Milestones (every ~1000m)
                        var accumulatedMeters = 0.0
                        var currentKm = 1
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            accumulatedMeters += hypot(
                                (pCurr.longitude - pPrev.longitude) * 111000.0,
                                (pCurr.latitude - pPrev.latitude) * 111000.0
                            )
                            if (accumulatedMeters >= currentKm * 1000.0) {
                                val kmCoord = toScreenCoord(pCurr)
                                drawCircle(
                                    color = DarkObsidian,
                                    radius = 8f,
                                    center = kmCoord
                                )
                                drawCircle(
                                    color = NeonLime,
                                    radius = 7f,
                                    center = kmCoord,
                                    style = Stroke(width = 2f)
                                )
                                currentKm++
                            }
                        }
                    }

                    // 4. Start Point Badge
                    val startCoord = toScreenCoord(points.first())
                    drawCircle(
                        color = NeonLime,
                        radius = 8f,
                        center = startCoord
                    )
                    drawCircle(
                        color = DarkObsidian,
                        radius = 4f,
                        center = startCoord
                    )

                    // 5. End / Live Point Badge
                    val lastCoord = toScreenCoord(points.last())
                    if (isLive) {
                        drawCircle(
                            color = ElectricCyan.copy(alpha = pulseAlpha),
                            radius = pulseRadius,
                            center = lastCoord
                        )
                        drawCircle(
                            color = ElectricCyan,
                            radius = 8f,
                            center = lastCoord
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = lastCoord
                        )
                    } else {
                        drawCircle(
                            color = HyperCoral,
                            radius = 8f,
                            center = lastCoord
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = lastCoord
                        )
                    }
                }
            }
        }

        // Overlay Controls: Map Mode Switcher & Zoom Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Mode Toggle Pill (Heatmap vs Elevation)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark.copy(alpha = 0.85f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (colorMode == MapColorMode.PACE_HEATMAP) NeonLime else Color.Transparent)
                            .clickable { colorMode = MapColorMode.PACE_HEATMAP }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Pace Heat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colorMode == MapColorMode.PACE_HEATMAP) DarkObsidian else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (colorMode == MapColorMode.ELEVATION_PROFILE) ElectricCyan else Color.Transparent)
                            .clickable { colorMode = MapColorMode.ELEVATION_PROFILE }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Elevasi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colorMode == MapColorMode.ELEVATION_PROFILE) DarkObsidian else TextSecondary
                        )
                    }
                }
            }

            // Zoom In, Zoom Out, Reset Box
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable { scale = (scale * 1.3f).coerceAtMost(4.5f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable { scale = (scale / 1.3f).coerceAtLeast(0.7f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable {
                            scale = 1.0f
                            panOffset = Offset.Zero
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Reset View",
                        tint = NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Map Legend
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark.copy(alpha = 0.85f))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (colorMode == MapColorMode.PACE_HEATMAP) {
                    LegendDot(color = HyperCoral, label = "Tanjakan/Pelan")
                    LegendDot(color = ElectricCyan, label = "Steady")
                    LegendDot(color = NeonLime, label = "Fast Sprint")
                } else {
                    LegendDot(color = ElectricCyan, label = "Dataran")
                    LegendDot(color = AcidYellow, label = "Landai")
                    LegendDot(color = BlazeOrange, label = "Puncak")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun DrawScope.drawGrid(width: Float, height: Float) {
    val step = 44f
    val gridColor = SurfaceBorder.copy(alpha = 0.35f)

    var x = 0f
    while (x <= width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 0.8f
        )
        x += step
    }

    var y = 0f
    while (y <= height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 0.8f
        )
        y += step
    }
}
