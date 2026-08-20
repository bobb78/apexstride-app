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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiCoachApi
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

data class ChatMessage(
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiCoachScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Blueprint Latihan", "Tanya Coach", "Kalkulator Pacing")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
    ) {
        // Top Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        contentDescription = "AI",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "APEX INTELLIGENCE",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonLime)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "GEMINI 3.1 PRO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkObsidian
                            )
                        }
                    }
                    Text(
                        text = "High Thinking Mode • AI Running Coach",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Tab Row
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
            0 -> TrainingBlueprintTab()
            1 -> CoachChatTab()
            2 -> PacingCalculatorTab()
        }
    }
}

@Composable
fun TrainingBlueprintTab() {
    val coroutineScope = rememberCoroutineScope()
    var selectedGoal by remember { mutableStateOf("Sub-25 Menit 5K Blitz") }
    var currentPace by remember { mutableStateOf("5:30") }
    var weeklyKm by remember { mutableIntStateOf(35) }
    var daysPerWeek by remember { mutableIntStateOf(4) }
    var generatedBlueprint by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val goalOptions = listOf(
        "Sub-25 Menit 5K Blitz",
        "Sub-50 Menit 10K Target",
        "Half Marathon 21K Sub-2 Jam",
        "Marathon 42K Finisher Plan",
        "Aerobic Base & Fat Loss"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "PILIH TARGET LATIHAN",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                goalOptions.forEach { goal ->
                    val isSelected = selectedGoal == goal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) SurfaceElevated else SurfaceDark)
                            .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedGoal = goal }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = goal,
                                tint = if (isSelected) NeonLime else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = goal,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item {
            // Form parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = currentPace,
                    onValueChange = { currentPace = it },
                    label = { Text("Pace Saat Ini (/km)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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

                OutlinedTextField(
                    value = "$weeklyKm",
                    onValueChange = { weeklyKm = it.toIntOrNull() ?: 30 },
                    label = { Text("Target Km/Minggu", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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
            }
        }

        item {
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val result = GeminiCoachApi.generateCustomTrainingPlan(
                            targetGoal = selectedGoal,
                            currentPace = currentPace,
                            weeklyMileageKm = weeklyKm,
                            daysPerWeek = daysPerWeek
                        )
                        generatedBlueprint = result.getOrNull()
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_training_plan_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = DarkObsidian
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DarkObsidian,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEEP THINKING COMPUTING...",
                        fontWeight = FontWeight.Black,
                        color = DarkObsidian
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate",
                        tint = DarkObsidian
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RANCANG BLUEPRINT 7-HARI",
                        fontWeight = FontWeight.Black,
                        color = DarkObsidian,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Blueprint Output Card
        if (generatedBlueprint != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, ElectricCyan, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Blueprint",
                                tint = NeonLime
                            )
                            Text(
                                text = "JADWAL LATIHAN PERSONALISASI",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonLime,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = generatedBlueprint!!,
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

@Composable
fun CoachChatTab() {
    val coroutineScope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "coach",
                text = "Halo Atlet! Saya Apex Coach (Gemini 3.1 Pro High Thinking). Tanyakan apa saja seputar strategi lari, cadence, perbaikan form, nutrisi carbo-loading, atau pencegahan cedera shin splints!"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isCoach = msg.sender == "coach"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isCoach) 4.dp else 16.dp,
                                    bottomEnd = if (isCoach) 16.dp else 4.dp
                                )
                            )
                            .background(if (isCoach) SurfaceDark else ElectricCyan)
                            .border(
                                1.dp,
                                if (isCoach) SurfaceBorder else ElectricCyan,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCoach) TextPrimary else DarkObsidian,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ElectricCyan,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Apex Coach sedang menalar (High Thinking)...",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        // Chat Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Tanya strategi lari, nutrisi, dll...", color = TextMuted) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank() && !isLoading) {
                        val userText = inputQuery.trim()
                        inputQuery = ""
                        messages.add(ChatMessage(sender = "user", text = userText))
                        isLoading = true

                        coroutineScope.launch {
                            val result = GeminiCoachApi.askCoachChat(userText)
                            val answer = result.getOrNull() ?: "Maaf, terjadi kendala saat memproses jawaban. Silakan coba kembali."
                            messages.add(ChatMessage(sender = "coach", text = answer))
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NeonLime)
                    .testTag("send_coach_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = DarkObsidian
                )
            }
        }
    }
}

@Composable
fun PacingCalculatorTab() {
    var distanceKm by remember { mutableStateOf("10.0") }
    var targetMinutes by remember { mutableStateOf("50") }

    val dist = distanceKm.toDoubleOrNull() ?: 10.0
    val totalMin = targetMinutes.toDoubleOrNull() ?: 50.0

    val paceSecPerKm = if (dist > 0) ((totalMin * 60.0) / dist).toInt() else 300
    val paceMin = paceSecPerKm / 60
    val paceSec = paceSecPerKm % 60
    val speedKmh = if (totalMin > 0) (dist / (totalMin / 60.0)) else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "KALKULATOR TARGET PACING & SPLIT",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = distanceKm,
                    onValueChange = { distanceKm = it },
                    label = { Text("Jarak Target (KM)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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

                OutlinedTextField(
                    value = targetMinutes,
                    onValueChange = { targetMinutes = it },
                    label = { Text("Target Waktu (Menit)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, NeonLime, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PACE WAJIB DIPERLUKAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = String.format(Locale.US, "%d'%02d\" /km", paceMin, paceSec),
                        style = MaterialTheme.typography.displayLarge,
                        color = NeonLime,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format(Locale.US, "Kecepatan rata-rata: %.2f km/jam", speedKmh),
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "⚡ Rekomendasi Taktik: Buka kilometer pertama 5-8 detik lebih lambat untuk pemanasan aerobik, lalu kunci tempo di target pace pada KM 2-${dist.toInt()}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
