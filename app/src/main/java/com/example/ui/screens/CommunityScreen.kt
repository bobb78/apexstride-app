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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ChallengeItem
import com.example.data.model.ChallengeParticipant
import com.example.data.model.CommunityPost
import com.example.data.model.CommunityRoute
import com.example.ui.components.PaceHeatmapCanvas
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
fun CommunityScreen(
    posts: List<CommunityPost>,
    challenges: List<ChallengeItem>,
    routes: List<CommunityRoute> = emptyList(),
    onToggleBoost: (String) -> Unit,
    onToggleJoinChallenge: (String) -> Unit,
    onToggleBookmarkRoute: (String) -> Unit = {},
    onStartRouteRun: (CommunityRoute) -> Unit = {},
    onCreateCustomChallenge: (String, String, Double, Int, String, String, Int) -> Unit = { _, _, _, _, _, _, _ -> },
    onCheerParticipant: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tribe Feed", "Tantangan", "Jelajah Rute")

    var isCreateChallengeDialogOpen by remember { mutableStateOf(false) }
    var selectedChallengeForLeaderboard by remember { mutableStateOf<ChallengeItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Community",
                        tint = NeonLime,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "APEX TRIBE & ARENA",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tantangan, Rute Komunitas & Leaderboard",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (selectedTab == 1) {
                // Button to Create Challenge
                Button(
                    onClick = { isCreateChallengeDialogOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = DarkObsidian
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("create_challenge_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Buat", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkObsidian,
            contentColor = NeonLime,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp)
                        .background(NeonLime)
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SurfaceBorder)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) NeonLime else TextSecondary
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> FeedTab(posts = posts, onToggleBoost = onToggleBoost)
            1 -> ChallengesWithLeaderboardTab(
                challenges = challenges,
                onToggleJoin = onToggleJoinChallenge,
                onOpenLeaderboard = { ch -> selectedChallengeForLeaderboard = ch }
            )
            2 -> RoutesExploreScreen(
                routes = routes,
                onToggleBookmark = onToggleBookmarkRoute,
                onStartRouteRun = onStartRouteRun
            )
        }
    }

    // Modal BottomSheet for Challenge Leaderboard
    selectedChallengeForLeaderboard?.let { challenge ->
        ModalBottomSheet(
            onDismissRequest = { selectedChallengeForLeaderboard = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkObsidian
        ) {
            ChallengeLeaderboardBottomSheet(
                challenge = challenge,
                onCheerParticipant = { userId -> onCheerParticipant(challenge.id, userId) },
                onClose = { selectedChallengeForLeaderboard = null }
            )
        }
    }

    // Create Custom Challenge Dialog
    if (isCreateChallengeDialogOpen) {
        CreateCustomChallengeDialog(
            onDismiss = { isCreateChallengeDialogOpen = false },
            onCreateChallenge = { title, desc, targetKm, days, cat, badge, xp ->
                onCreateCustomChallenge(title, desc, targetKm, days, cat, badge, xp)
                isCreateChallengeDialogOpen = false
            }
        )
    }
}

@Composable
fun FeedTab(
    posts: List<CommunityPost>,
    onToggleBoost: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            CommunityPostCard(post = post, onToggleBoost = { onToggleBoost(post.id) })
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onToggleBoost: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // User Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.runnerName.take(2).uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.titleSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = post.runnerName,
                            style = MaterialTheme.typography.titleSmall,
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
                                text = post.locationName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Caption
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            // Activity Stats Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("JARAK", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${post.activity.formattedDistance} km",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG PACE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${post.activity.formattedPace}/km",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WAKTU", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        post.activity.formattedDuration,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Route Canvas Map
            PaceHeatmapCanvas(
                points = post.activity.routePoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                isLive = false,
                showGrid = false
            )

            // Interaction Bar (Kudos Boost & Comments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Boost Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleBoost() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Boost",
                        tint = if (post.isBoosted) BlazeOrange else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${post.boostCount} Kudos",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (post.isBoosted) BlazeOrange else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comment Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.commentCount} Komentar",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengesWithLeaderboardTab(
    challenges: List<ChallengeItem>,
    onToggleJoin: (String) -> Unit,
    onOpenLeaderboard: (ChallengeItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(challenges, key = { it.id }) { ch ->
            val progress = ch.progressPercentage

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, if (ch.isJoined) NeonLime.copy(alpha = 0.6f) else SurfaceBorder, RoundedCornerShape(24.dp))
                    .clickable { onOpenLeaderboard(ch) }
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = ch.category.uppercase(Locale.ROOT),
                                fontSize = 10.sp,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "⏳ ${ch.daysLeft} Hari Tersisa",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = ch.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = ch.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Real-time Progress Bar
                    if (ch.isJoined) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progresmu: ${String.format(Locale.US, "%.1f", ch.currentProgressKm)} / ${ch.targetKm.toInt()} KM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonLime,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonLime,
                                trackColor = SurfaceElevated
                            )
                        }
                    }

                    // Reward badge & Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = ch.rewardBadge,
                                style = MaterialTheme.typography.labelMedium,
                                color = AcidYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${ch.rewardXp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Open Leaderboard button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .clickable { onOpenLeaderboard(ch) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Leaderboard, contentDescription = "Rank", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                    Text("Papan Peringkat", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { onToggleJoin(ch.id) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ch.isJoined) SurfaceElevated else NeonLime,
                                    contentColor = if (ch.isJoined) TextPrimary else DarkObsidian
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (ch.isJoined) "TERGABUNG" else "IKUT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeLeaderboardBottomSheet(
    challenge: ChallengeItem,
    onCheerParticipant: (String) -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PAPAN PERINGKAT TANTANGAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL PESERTA", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("${challenge.participantsCount} Pelari", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("${challenge.targetKm.toInt()} KM", fontSize = 14.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HADIAH XP", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("+${challenge.rewardXp} XP", fontSize = 14.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Leaderboard Ranking List
        items(challenge.participants) { participant ->
            val rankIcon = when (participant.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#${participant.rank}"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (participant.isCurrentUser) SurfaceElevated else SurfaceDark)
                    .border(
                        1.dp,
                        if (participant.isCurrentUser) NeonLime else SurfaceBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = rankIcon,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (participant.rank <= 3) AcidYellow else TextSecondary
                        )

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(participant.avatarColorHex).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participant.userName.take(2).uppercase(Locale.ROOT),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(participant.avatarColorHex)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = participant.userName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (participant.isCurrentUser) NeonLime else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (participant.isCurrentUser) {
                                    Text("(Kamu)", fontSize = 10.sp, color = NeonLime)
                                }
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.1f", participant.progressKm)} / ${participant.targetKm.toInt()} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Cheer Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .clickable { onCheerParticipant(participant.userId) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Cheer",
                            tint = BlazeOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${participant.cheersCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlazeOrange
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CreateCustomChallengeDialog(
    onDismiss: () -> Unit,
    onCreateChallenge: (String, String, Double, Int, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var targetKmText by remember { mutableStateOf("50") }
    var durationDaysText by remember { mutableStateOf("14") }
    var selectedCategory by remember { mutableStateOf("Jarak Bulanan") }

    val categories = listOf("Jarak Bulanan", "Pace & Kecepatan", "Elevasi Bukit", "Streak Harian")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkObsidian,
        title = {
            Text("Buat Tantangan Baru", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Tantangan (e.g. 50K Half Century)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Deskripsi singkat", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetKmText,
                        onValueChange = { targetKmText = it },
                        label = { Text("Target (KM)", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    OutlinedTextField(
                        value = durationDaysText,
                        onValueChange = { durationDaysText = it },
                        label = { Text("Durasi (Hari)", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetKmText.toDoubleOrNull() ?: 50.0
                    val days = durationDaysText.toIntOrNull() ?: 14
                    val finalTitle = title.ifEmpty { "Apex Challenge ${target.toInt()}K" }
                    val finalSub = subtitle.ifEmpty { "Capai target $target KM dalam $days hari!" }
                    onCreateChallenge(
                        finalTitle,
                        finalSub,
                        target,
                        days,
                        selectedCategory,
                        "🏆 Apex Titan",
                        400
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = DarkObsidian
                )
            ) {
                Text("Publikasikan Tantangan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}
