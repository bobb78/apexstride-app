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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityRoute
import com.example.ui.components.PaceHeatmapCanvas
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesExploreScreen(
    routes: List<CommunityRoute>,
    onToggleBookmark: (String) -> Unit,
    onStartRouteRun: (CommunityRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedDifficulty by remember { mutableStateOf("Semua") }
    var selectedRouteForDetail by remember { mutableStateOf<CommunityRoute?>(null) }
    var onlyBookmarked by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Taman Kota", "Tepi Pantai & Danau", "Jalur CFD Bebas Polusi", "Perkotaan Modern", "Hutan & Bukit")
    val difficulties = listOf("Semua", "Mudah (Easy)", "Sedang (Moderate)", "Menantang (Challenging)")

    val filteredRoutes = routes.filter { route ->
        val matchesSearch = route.title.contains(searchQuery, ignoreCase = true) ||
                route.locationName.contains(searchQuery, ignoreCase = true) ||
                route.authorName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Semua" || route.sceneryCategory == selectedCategory
        val matchesDifficulty = selectedDifficulty == "Semua" || route.difficulty == selectedDifficulty
        val matchesBookmark = !onlyBookmarked || route.isBookmarked

        matchesSearch && matchesCategory && matchesDifficulty && matchesBookmark
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
    ) {
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari rute, lokasi (GBK, PIK, Sudirman)...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonLime,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("route_search_field"),
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // Horizontal Filter Chips: Category
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            // Bookmark filter toggle chip
            item {
                FilterPill(
                    label = "⭐ Tersimpan",
                    isSelected = onlyBookmarked,
                    onClick = { onlyBookmarked = !onlyBookmarked }
                )
            }

            items(categories) { cat ->
                FilterPill(
                    label = cat,
                    isSelected = selectedCategory == cat && !onlyBookmarked,
                    onClick = {
                        selectedCategory = cat
                        onlyBookmarked = false
                    }
                )
            }
        }

        // Horizontal Filter Chips: Difficulty
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(difficulties) { diff ->
                FilterPill(
                    label = diff,
                    isSelected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff },
                    isSecondary = true
                )
            }
        }

        // Routes List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DITEMUKAN ${filteredRoutes.size} RUTE REKOMENDASI",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            items(filteredRoutes, key = { it.id }) { route ->
                CommunityRouteCard(
                    route = route,
                    onCardClick = { selectedRouteForDetail = route },
                    onToggleBookmark = { onToggleBookmark(route.id) },
                    onStartRun = { onStartRouteRun(route) }
                )
            }
        }
    }

    // Modal Bottom Sheet for Route Detail & Full Preview
    selectedRouteForDetail?.let { route ->
        ModalBottomSheet(
            onDismissRequest = { selectedRouteForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkObsidian
        ) {
            RouteDetailBottomSheet(
                route = route,
                onToggleBookmark = { onToggleBookmark(route.id) },
                onStartRun = {
                    selectedRouteForDetail = null
                    onStartRouteRun(route)
                },
                onClose = { selectedRouteForDetail = null }
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isSecondary: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    if (isSecondary) ElectricCyan else NeonLime
                } else SurfaceDark
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent else SurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) DarkObsidian else TextSecondary
        )
    }
}

@Composable
fun CommunityRouteCard(
    route: CommunityRoute,
    onCardClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onStartRun: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark.copy(alpha = 0.7f))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .clickable { onCardClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Title, Author & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonLime.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = route.sceneryCategory.uppercase(Locale.ROOT),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = route.difficulty,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = route.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = route.locationName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Bookmark Icon Button
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (route.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (route.isBookmarked) AcidYellow else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Route Map Preview Canvas (Mini Heatmap)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                PaceHeatmapCanvas(
                    points = route.routePoints,
                    modifier = Modifier.fillMaxSize(),
                    showGrid = false
                )
            }

            // Key Metrics Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("JARAK", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(route.formattedDistance, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ELEVASI", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("+${route.elevationGainMeters}m", fontSize = 14.sp, color = BlazeOrange, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EST. PACE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(route.formattedPace, fontSize = 14.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RATING", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = AcidYellow, modifier = Modifier.size(12.dp))
                        Text(" ${route.rating}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartRun,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = DarkObsidian
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Run",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lari Rute Ini", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun RouteDetailBottomSheet(
    route: CommunityRoute,
    onToggleBookmark: () -> Unit,
    onStartRun: () -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DETAIL RUTE KOMUNITAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = route.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (route.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (route.isBookmarked) AcidYellow else TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Full Interactive Map Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                PaceHeatmapCanvas(
                    points = route.routePoints,
                    modifier = Modifier.fillMaxSize(),
                    showGrid = true
                )
            }
        }

        // Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(
                    label = "Total Jarak",
                    value = route.formattedDistance,
                    unit = "",
                    icon = Icons.Default.DirectionsRun,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Tanjakan",
                    value = "+${route.elevationGainMeters}",
                    unit = "METER",
                    icon = Icons.Default.Terrain,
                    accentColor = BlazeOrange,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Est. Durasi",
                    value = route.formattedDuration,
                    unit = "",
                    icon = Icons.Default.Timer,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Route Specifications
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PANDUAN & SPESIFIKASI",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                SpecificationRow(label = "Tipe Permukaan:", value = route.surfaceType)
                SpecificationRow(label = "Waktu Terbaik:", value = route.bestTimeToRun)
                SpecificationRow(label = "Rekomendasi Sepatu:", value = route.recommendedShoeType)
                SpecificationRow(label = "Dibuat Oleh:", value = route.authorName)
            }
        }

        // Action CTA
        item {
            Button(
                onClick = onStartRun,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_route_run_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = DarkObsidian
                )
            ) {
                Icon(imageVector = Icons.Default.Navigation, contentDescription = "Start")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mulai Lari di Rute Ini", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}
