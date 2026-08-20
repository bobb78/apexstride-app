package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GPSPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val speed: Float = 0f, // in m/s
    val timestamp: Long = System.currentTimeMillis(),
    val distanceFromStartMeters: Double = 0.0
)

data class KmSplit(
    val kmNumber: Int,
    val durationSeconds: Long,
    val paceSecondsPerKm: Int,
    val elevationGainMeters: Int = 0,
    val avgHeartRateBpm: Int = 145
) {
    val formattedPace: String
        get() = formatPace(paceSecondsPerKm)

    val formattedDuration: String
        get() = formatDuration(durationSeconds)
}

data class RunActivity(
    val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long,
    val distanceMeters: Double,
    val avgPaceSecondsPerKm: Int,
    val maxPaceSecondsPerKm: Int = 0,
    val caloriesBurned: Int,
    val elevationGainMeters: Int = 0,
    val avgCadenceSpm: Int = 168,
    val avgHeartRateBpm: Int = 152,
    val routePoints: List<GPSPoint> = emptyList(),
    val splits: List<KmSplit> = emptyList(),
    val aiAnalysis: String? = null,
    val aiThinkingSummary: String? = null,
    val isSyncedToCloud: Boolean = false,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val feelingTag: String = "🔥 Kuat & Berenergi",
    val shoeName: String = "Nike Alphafly 3",
    val weatherCondition: String = "Cerah 28°C"
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val formattedDistance: String
        get() = String.format(Locale.US, "%.2f", distanceKm)

    val formattedDuration: String
        get() = formatDuration(durationSeconds)

    val formattedPace: String
        get() = formatPace(avgPaceSecondsPerKm)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }

    val speedKmh: Double
        get() = if (durationSeconds > 0) (distanceMeters / 1000.0) / (durationSeconds / 3600.0) else 0.0
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val levelTitle: String = "Apex Velocity Master",
    val currentStreakDays: Int = 14,
    val totalDistanceKm: Double = 342.8,
    val totalRunsCount: Int = 48,
    val totalDurationHours: Double = 31.5,
    val best5kSeconds: Long = 1320, // 22:00
    val best10kSeconds: Long = 2760, // 46:00
    val best21kSeconds: Long = 6120, // 1:42:00
    val weeklyGoalKm: Double = 35.0,
    val weeklyProgressKm: Double = 24.6,
    val favoriteShoe: String = "Nike Vaporfly Next% 3",
    val savedRouteIds: List<String> = emptyList()
)

data class ChallengeParticipant(
    val userId: String,
    val userName: String,
    val avatarColorHex: Long = 0xFFD4FF5F,
    val progressKm: Double,
    val targetKm: Double,
    val rank: Int = 1,
    val isCurrentUser: Boolean = false,
    val cheersCount: Int = 12
) {
    val completionPercentage: Float
        get() = (progressKm / targetKm.coerceAtLeast(0.1)).toFloat().coerceIn(0f, 1f)
}

data class ChallengeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetKm: Double,
    val currentProgressKm: Double,
    val participantsCount: Int,
    val daysLeft: Int,
    val rewardBadge: String,
    val rewardXp: Int,
    val isJoined: Boolean = false,
    val category: String = "Jarak Bulanan", // "Jarak Bulanan", "Pace & Kecepatan", "Elevasi Bukit", "Streak Harian"
    val creatorName: String = "Komunitas Apex",
    val participants: List<ChallengeParticipant> = emptyList()
) {
    val progressPercentage: Float
        get() = (currentProgressKm / targetKm.coerceAtLeast(0.1)).toFloat().coerceIn(0f, 1f)
}

data class CommunityPost(
    val id: String,
    val runnerName: String,
    val runnerAvatar: String? = null,
    val locationName: String = "Gelora Bung Karno (GBK), Jakarta",
    val timestamp: Long = System.currentTimeMillis(),
    val activity: RunActivity,
    val boostCount: Int = 24,
    val isBoosted: Boolean = false,
    val commentCount: Int = 6,
    val caption: String = "Pagi yang sejuk di Senayan! Pace stabil dan target 10K tercapai sebelum matahari terbit ⚡🏃‍♂️"
)

data class CommunityRoute(
    val id: String,
    val title: String,
    val authorName: String,
    val locationName: String,
    val distanceKm: Double,
    val elevationGainMeters: Int,
    val estDurationSeconds: Long,
    val avgPaceSecondsPerKm: Int,
    val difficulty: String, // "Mudah (Easy)", "Sedang (Moderate)", "Menantang (Challenging)", "Trail Ekstrem"
    val sceneryCategory: String, // "Taman Kota", "Tepi Pantai & Danau", "Jalur CFD Bebas Polusi", "Perkotaan Modern", "Hutan & Bukit"
    val rating: Double,
    val reviewsCount: Int,
    val bookmarksCount: Int,
    val isBookmarked: Boolean = false,
    val routePoints: List<GPSPoint> = emptyList(),
    val description: String,
    val surfaceType: String = "Aspal Mulus",
    val bestTimeToRun: String = "05:30 - 07:00 Pagi",
    val recommendedShoeType: String = "Road Cushion / Superblast"
) {
    val formattedDistance: String
        get() = String.format(Locale.US, "%.1f KM", distanceKm)

    val formattedDuration: String
        get() = formatDuration(estDurationSeconds)

    val formattedPace: String
        get() = formatPace(avgPaceSecondsPerKm)
}

data class PerformanceMetrics(
    val vo2MaxEstimate: Int = 54,
    val vo2MaxCategory: String = "Unggul (Top 5%)",
    val paceConsistencyScore: Int = 92,
    val avgCadenceSpm: Int = 172,
    val groundContactTimeMs: Int = 214,
    val strideLengthMeters: Double = 1.28,
    val verticalOscillationCm: Double = 7.4,
    val cardiacDriftPercentage: Double = 2.8,
    val aerobicEfficiencyScore: Int = 88,
    val racePredictions: Map<String, String> = mapOf(
        "5K" to "21:15",
        "10K" to "44:48",
        "Half Marathon (21.1K)" to "1:39:30",
        "Full Marathon (42.2K)" to "3:29:10"
    ),
    val heartRateZonesDurationSeconds: Map<String, Long> = mapOf(
        "Zone 1 (Pemulihan Aktif)" to 320L,
        "Zone 2 (Pondasi Aerobik)" to 1480L,
        "Zone 3 (Tempo Stride)" to 820L,
        "Zone 4 (Ambang Laktat)" to 240L,
        "Zone 5 (Puncak Anaerobik)" to 60L
    ),
    val weeklyVolumeKm: List<Pair<String, Double>> = listOf(
        "Sen" to 6.2,
        "Sel" to 0.0,
        "Rab" to 8.5,
        "Kam" to 5.0,
        "Jum" to 0.0,
        "Sab" to 12.4,
        "Min" to 10.0
    ),
    val smartFeedbackList: List<String> = listOf(
        "Irama langkah rata-rata 172 SPM sangat efisien, mengurangi beban benturan lutut hingga 14%.",
        "Pace consistency 92% menunjukkan kontrol energi yang luar biasa di kilometer 4-8.",
        "Zona 2 mencakup 52% total latihan minggu ini, optimal untuk pembakaran lemak dan mitokondria."
    )
)

data class TrainingPlanDay(
    val dayName: String,
    val sessionType: String, // "Interval 400m", "Tempo Run", "Easy Recovery", "Long Run", "Rest"
    val targetDistanceKm: Double,
    val targetPace: String,
    val description: String,
    val isCompleted: Boolean = false
)

fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

fun formatPace(paceSecondsPerKm: Int): String {
    if (paceSecondsPerKm <= 0 || paceSecondsPerKm > 3600) return "--'--\""
    val minutes = paceSecondsPerKm / 60
    val seconds = paceSecondsPerKm % 60
    return String.format(Locale.US, "%d'%02d\"", minutes, seconds)
}
