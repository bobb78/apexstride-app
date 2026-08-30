package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.GPSPoint

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
    var show3DView by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (show3DView) {
            PaceHeatmap3DCanvas(
                points = points,
                modifier = Modifier.fillMaxSize(),
                isLive = isLive,
                showGrid = showGrid,
                initialIs3D = true
            )
        } else {
            RealRouteMapView(
                points = points,
                modifier = Modifier.fillMaxSize(),
                isLive = isLive,
                onSwitchTo3D = { show3DView = true }
            )
        }
    }
}

