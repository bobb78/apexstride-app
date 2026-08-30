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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.tan

enum class MapTileType(val label: String, val icon: ImageVector) {
    VOYAGER_STREET("Peta Jalan", Icons.Default.Map),
    SATELLITE("Satelit Nyata", Icons.Default.Satellite),
    OUTDOOR_TERRAIN("Topografi & Alam", Icons.Default.Terrain),
    DARK_MODE("Mode Malam", Icons.Default.DarkMode)
}

/**
 * Real Geographic Slippy Map View
 * Fetches and displays actual OpenStreetMap / Satellite / Voyager tiles with GPS route heatmaps overlay.
 */
@Composable
fun RealRouteMapView(
    points: List<GPSPoint>,
    modifier: Modifier = Modifier,
    isLive: Boolean = false,
    initialTileType: MapTileType = MapTileType.VOYAGER_STREET,
    onSwitchTo3D: (() -> Unit)? = null
) {
    var tileType by remember { mutableStateOf(initialTileType) }
    var showLayerMenu by remember { mutableStateOf(false) }

    // Center coordinates
    var centerLat by remember { mutableDoubleStateOf(-6.2185) }
    var centerLng by remember { mutableDoubleStateOf(106.8025) }
    var zoomLevel by remember { mutableIntStateOf(16) }
    var userHasPanned by remember { mutableStateOf(false) }

    // Pulsing radar animation for live runner
    val infiniteTransition = rememberInfiniteTransition(label = "runner_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Auto-fit coordinates to route
    LaunchedEffect(points.size, isLive) {
        if (points.isNotEmpty()) {
            if (isLive && !userHasPanned) {
                // Follow the latest point
                val latest = points.last()
                centerLat = latest.latitude
                centerLng = latest.longitude
            } else if (!userHasPanned) {
                // Auto center bounding box
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLng = points.minOf { it.longitude }
                val maxLng = points.maxOf { it.longitude }
                centerLat = (minLat + maxLat) / 2.0
                centerLng = (minLng + maxLng) / 2.0

                val deltaLat = max(0.002, maxLat - minLat)
                val deltaLng = max(0.002, maxLng - minLng)
                val maxDelta = max(deltaLat, deltaLng)

                zoomLevel = when {
                    maxDelta < 0.005 -> 17
                    maxDelta < 0.015 -> 16
                    maxDelta < 0.035 -> 15
                    maxDelta < 0.08 -> 14
                    maxDelta < 0.20 -> 13
                    else -> 12
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(DarkObsidian, RoundedCornerShape(24.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val tileSizePx = 512f // Retina 2x tiles
        val nTiles = 1 shl zoomLevel

        // Project Center to World Pixel
        val centerNormX = lonToNormX(centerLng)
        val centerNormY = latToNormY(centerLat)
        val centerWorldX = centerNormX * nTiles * tileSizePx
        val centerWorldY = centerNormY * nTiles * tileSizePx

        // Calculate Visible Tiles Range
        val minWorldX = centerWorldX - widthPx / 2f
        val maxWorldX = centerWorldX + widthPx / 2f
        val minWorldY = centerWorldY - heightPx / 2f
        val maxWorldY = centerWorldY + heightPx / 2f

        val minTileX = floor(minWorldX / tileSizePx).toInt()
        val maxTileX = floor(maxWorldX / tileSizePx).toInt()
        val minTileY = floor(minWorldY / tileSizePx).toInt().coerceIn(0, nTiles - 1)
        val maxTileY = floor(maxWorldY / tileSizePx).toInt().coerceIn(0, nTiles - 1)

        // 1. Real Map Raster Tiles Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        userHasPanned = true
                        // Handle Pan
                        val panNormDeltaX = -pan.x / (nTiles * tileSizePx)
                        val panNormDeltaY = -pan.y / (nTiles * tileSizePx)

                        val curNormX = lonToNormX(centerLng) + panNormDeltaX
                        val curNormY = (latToNormY(centerLat) + panNormDeltaY).coerceIn(0.001, 0.999)

                        centerLng = normXToLon(curNormX)
                        centerLat = normYToLat(curNormY)

                        // Handle Pinch Zoom
                        if (zoom > 1.35f && zoomLevel < 19) {
                            zoomLevel += 1
                        } else if (zoom < 0.75f && zoomLevel > 11) {
                            zoomLevel -= 1
                        }
                    }
                }
        ) {
            for (ty in minTileY..maxTileY) {
                for (tx in minTileX..maxTileX) {
                    val wrappedTx = ((tx % nTiles) + nTiles) % nTiles
                    val tileLeftPx = widthPx / 2f + (tx * tileSizePx - centerWorldX).toFloat()
                    val tileTopPx = heightPx / 2f + (ty * tileSizePx - centerWorldY).toFloat()

                    val tileUrl = getTileUrl(tileType, zoomLevel, wrappedTx, ty)

                    val leftDp = with(density) { tileLeftPx.toDp() }
                    val topDp = with(density) { tileTopPx.toDp() }
                    val sizeDp = with(density) { tileSizePx.toDp() }

                    AsyncImage(
                        model = tileUrl,
                        contentDescription = "Peta Asli",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .offset { IntOffset(tileLeftPx.roundToInt(), tileTopPx.roundToInt()) }
                            .size(sizeDp)
                    )
                }
            }

            // 2. Real Vector Route & Pace Heatmap Overlay Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.isNotEmpty()) {
                    val minSpeed = points.minOf { it.speed }.coerceAtLeast(0.5f)
                    val maxSpeed = points.maxOf { it.speed }.coerceAtLeast(minSpeed + 0.5f)

                    // Draw Route Polyline with Dynamic Pace Gradient
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]

                        val p1WorldX = lonToNormX(p1.longitude) * nTiles * tileSizePx
                        val p1WorldY = latToNormY(p1.latitude) * nTiles * tileSizePx
                        val p1ScreenX = widthPx / 2f + (p1WorldX - centerWorldX).toFloat()
                        val p1ScreenY = heightPx / 2f + (p1WorldY - centerWorldY).toFloat()

                        val p2WorldX = lonToNormX(p2.longitude) * nTiles * tileSizePx
                        val p2WorldY = latToNormY(p2.latitude) * nTiles * tileSizePx
                        val p2ScreenX = widthPx / 2f + (p2WorldX - centerWorldX).toFloat()
                        val p2ScreenY = heightPx / 2f + (p2WorldY - centerWorldY).toFloat()

                        // Speed Pace Color Mapping
                        val ratio = ((p2.speed - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0f, 1f)
                        val segmentColor = when {
                            ratio > 0.7f -> NeonLime
                            ratio > 0.4f -> ElectricCyan
                            ratio > 0.2f -> BlazeOrange
                            else -> HyperCoral
                        }

                        // Outer Glow
                        drawLine(
                            color = segmentColor.copy(alpha = 0.35f),
                            start = Offset(p1ScreenX, p1ScreenY),
                            end = Offset(p2ScreenX, p2ScreenY),
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )

                        // Solid Path
                        drawLine(
                            color = segmentColor,
                            start = Offset(p1ScreenX, p1ScreenY),
                            end = Offset(p2ScreenX, p2ScreenY),
                            strokeWidth = 7f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw START Pin
                    val startP = points.first()
                    val startX = widthPx / 2f + (lonToNormX(startP.longitude) * nTiles * tileSizePx - centerWorldX).toFloat()
                    val startY = heightPx / 2f + (latToNormY(startP.latitude) * nTiles * tileSizePx - centerWorldY).toFloat()

                    // Start outer ring & pin
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.5f),
                        radius = 16f,
                        center = Offset(startX + 2f, startY + 2f)
                    )
                    drawCircle(
                        color = NeonLime,
                        radius = 14f,
                        center = Offset(startX, startY)
                    )
                    drawCircle(
                        color = DarkObsidian,
                        radius = 7f,
                        center = Offset(startX, startY)
                    )

                    // Draw FINISH / CURRENT Marker
                    val lastP = points.last()
                    val lastX = widthPx / 2f + (lonToNormX(lastP.longitude) * nTiles * tileSizePx - centerWorldX).toFloat()
                    val lastY = heightPx / 2f + (latToNormY(lastP.latitude) * nTiles * tileSizePx - centerWorldY).toFloat()

                    if (isLive) {
                        // Pulsing Live Radar
                        drawCircle(
                            color = ElectricCyan.copy(alpha = pulseAlpha),
                            radius = 24f * pulseScale,
                            center = Offset(lastX, lastY)
                        )
                        drawCircle(
                            color = ElectricCyan,
                            radius = 16f,
                            center = Offset(lastX, lastY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 8f,
                            center = Offset(lastX, lastY)
                        )
                    } else {
                        // Finish Flag Marker
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 16f,
                            center = Offset(lastX + 2f, lastY + 2f)
                        )
                        drawCircle(
                            color = HyperCoral,
                            radius = 14f,
                            center = Offset(lastX, lastY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(lastX, lastY)
                        )
                    }
                } else {
                    // Default Standby Pulse on current coordinate
                    val defaultX = widthPx / 2f
                    val defaultY = heightPx / 2f

                    drawCircle(
                        color = ElectricCyan.copy(alpha = pulseAlpha),
                        radius = 32f * pulseScale,
                        center = Offset(defaultX, defaultY)
                    )
                    drawCircle(
                        color = ElectricCyan,
                        radius = 14f,
                        center = Offset(defaultX, defaultY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(defaultX, defaultY)
                    )
                }
            }
        }

        // 3. Top Status / Map Style Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status or Map Info Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkObsidian.copy(alpha = 0.85f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (points.isNotEmpty()) NeonLime else ElectricCyan)
                    )
                    Text(
                        text = if (isLive) "GPS SATELIT LIVE" else tileType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Layer & 3D Switch Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Layer Selector Button
                Box {
                    IconButton(
                        onClick = { showLayerMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkObsidian.copy(alpha = 0.85f))
                            .border(1.dp, SurfaceBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Pilih Lapisan Peta",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showLayerMenu,
                        onDismissRequest = { showLayerMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        MapTileType.values().forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = type.label,
                                            tint = if (tileType == type) NeonLime else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = type.label,
                                            color = if (tileType == type) NeonLime else TextPrimary,
                                            fontWeight = if (tileType == type) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    tileType = type
                                    showLayerMenu = false
                                }
                            )
                        }
                    }
                }

                // Switch to 3D Ribbon if requested
                if (onSwitchTo3D != null) {
                    IconButton(
                        onClick = onSwitchTo3D,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkObsidian.copy(alpha = 0.85f))
                            .border(1.dp, SurfaceBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "Mode 3D",
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 4. Floating Map Navigation Controls (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Re-center / GPS Lock Button
            IconButton(
                onClick = {
                    userHasPanned = false
                    if (points.isNotEmpty()) {
                        centerLat = points.last().latitude
                        centerLng = points.last().longitude
                        zoomLevel = 16
                    } else {
                        centerLat = -6.2185
                        centerLng = 106.8025
                        zoomLevel = 16
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkObsidian.copy(alpha = 0.9f))
                    .border(1.dp, if (!userHasPanned) NeonLime else SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Pusatkan Lokasi",
                    tint = if (!userHasPanned) NeonLime else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom In (+)
            IconButton(
                onClick = {
                    if (zoomLevel < 19) zoomLevel += 1
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkObsidian.copy(alpha = 0.9f))
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Perbesar",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out (-)
            IconButton(
                onClick = {
                    if (zoomLevel > 11) zoomLevel -= 1
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkObsidian.copy(alpha = 0.9f))
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Perkecil",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 5. Bottom Left Pace Heatmap Legend
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkObsidian.copy(alpha = 0.85f))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LAMBAT",
                    fontSize = 8.sp,
                    color = HyperCoral,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(HyperCoral, BlazeOrange, ElectricCyan, NeonLime)
                            )
                        )
                )
                Text(
                    text = "CEPAT",
                    fontSize = 8.sp,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getTileUrl(type: MapTileType, z: Int, x: Int, y: Int): String {
    return when (type) {
        MapTileType.VOYAGER_STREET -> "https://a.basemaps.cartocdn.com/rastertiles/voyager/$z/$x/$y@2x.png"
        MapTileType.SATELLITE -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
        MapTileType.OUTDOOR_TERRAIN -> "https://a.tile.openstreetmap.fr/hot/$z/$x/$y.png"
        MapTileType.DARK_MODE -> "https://a.basemaps.cartocdn.com/dark_all/$z/$x/$y@2x.png"
    }
}

// Normalized Mercator Math
private fun lonToNormX(lon: Double): Double {
    return (lon + 180.0) / 360.0
}

private fun latToNormY(lat: Double): Double {
    val clampedLat = lat.coerceIn(-85.05112878, 85.05112878)
    val sinLat = sin(Math.toRadians(clampedLat))
    return 0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)
}

private fun normXToLon(normX: Double): Double {
    return normX * 360.0 - 180.0
}

private fun normYToLat(normY: Double): Double {
    val y2 = (0.5 - normY) * 2.0 * Math.PI
    val sinhVal = sinh(y2)
    return Math.toDegrees(atan(sinhVal))
}
