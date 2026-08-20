package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.RunEntity
import com.example.data.local.RunJsonConverter
import com.example.data.model.ChallengeItem
import com.example.data.model.ChallengeParticipant
import com.example.data.model.CommunityPost
import com.example.data.model.CommunityRoute
import com.example.data.model.GPSPoint
import com.example.data.model.KmSplit
import com.example.data.model.PerformanceMetrics
import com.example.data.model.RunActivity
import com.example.data.remote.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

class RunRepository(
    private val context: Context,
    private val firestoreSyncManager: FirestoreSyncManager = FirestoreSyncManager()
) {
    private val database = AppDatabase.getInstance(context)
    private val runDao = database.runDao()

    val allRunsFlow: Flow<List<RunActivity>> = runDao.getAllRuns().map { entities ->
        entities.map { entity ->
            val points = RunJsonConverter.jsonToRoutePoints(entity.routePointsJson)
            val splits = RunJsonConverter.jsonToSplits(entity.splitsJson)
            entity.toDomain(points, splits)
        }
    }

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _challenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val challenges: StateFlow<List<ChallengeItem>> = _challenges.asStateFlow()

    private val _communityRoutes = MutableStateFlow<List<CommunityRoute>>(emptyList())
    val communityRoutes: StateFlow<List<CommunityRoute>> = _communityRoutes.asStateFlow()

    init {
        initSampleDataIfEmpty()
        initChallenges()
        initCommunityPosts()
        initCommunityRoutes()
    }

    private fun initSampleDataIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = runDao.getAllRuns()
            val sampleRuns = generateStarterRuns()
            for (run in sampleRuns) {
                val entity = RunEntity.fromDomain(
                    domain = run,
                    routePointsJson = RunJsonConverter.routePointsToJson(run.routePoints),
                    splitsJson = RunJsonConverter.splitsToJson(run.splits)
                )
                runDao.insertRun(entity)
            }
        }
    }

    private fun initChallenges() {
        val participants1 = listOf(
            ChallengeParticipant("u_1", "Arya Wibowo", 0xFFD4FF5F, 94.2, 100.0, 1, false, 34),
            ChallengeParticipant("u_current", "Kamu (Apex Runner)", 0xFF38BDF8, 58.4, 100.0, 2, true, 28),
            ChallengeParticipant("u_2", "Siti Nurhaliza", 0xFFFF8533, 52.1, 100.0, 3, false, 19),
            ChallengeParticipant("u_3", "Rian Hidayat", 0xFFA78BFA, 44.8, 100.0, 4, false, 12),
            ChallengeParticipant("u_4", "Budi Santoso", 0xFFFF4B72, 38.0, 100.0, 5, false, 8)
        )

        val participants2 = listOf(
            ChallengeParticipant("u_current", "Kamu (Apex Runner)", 0xFF38BDF8, 5.0, 5.0, 1, true, 45),
            ChallengeParticipant("u_1", "Dimas Satria", 0xFFD4FF5F, 5.0, 5.0, 2, false, 39),
            ChallengeParticipant("u_5", "Kevin Sanjaya", 0xFFFDE047, 4.2, 5.0, 3, false, 21)
        )

        _challenges.value = listOf(
            ChallengeItem(
                id = "ch_1",
                title = "Apex 100K Century Month",
                subtitle = "Capai 100 kilometer lari dalam 30 hari",
                targetKm = 100.0,
                currentProgressKm = 58.4,
                participantsCount = 1420,
                daysLeft = 13,
                rewardBadge = "🏅 Century Master",
                rewardXp = 500,
                isJoined = true,
                category = "Jarak Bulanan",
                creatorName = "Apex Stride Official",
                participants = participants1
            ),
            ChallengeItem(
                id = "ch_2",
                title = "Sub-25 Blitz 5K Challenge",
                subtitle = "Tembus rekor 5 kilometer di bawah 25:00",
                targetKm = 5.0,
                currentProgressKm = 5.0,
                participantsCount = 890,
                daysLeft = 6,
                rewardBadge = "⚡ Sub-25 Lightning",
                rewardXp = 350,
                isJoined = true,
                category = "Pace & Kecepatan",
                creatorName = "Apex Speed Club",
                participants = participants2
            ),
            ChallengeItem(
                id = "ch_3",
                title = "Dawn Patrol 5AM Streak",
                subtitle = "Lari 5 hari berturut-turut sebelum pukul 06:30 pagi",
                targetKm = 25.0,
                currentProgressKm = 15.0,
                participantsCount = 630,
                daysLeft = 4,
                rewardBadge = "🌅 Early Bird Titan",
                rewardXp = 400,
                isJoined = false,
                category = "Streak Harian",
                creatorName = "Jakarta Morning Striders",
                participants = listOf(
                    ChallengeParticipant("u_6", "Dewi Lestari", 0xFFD4FF5F, 25.0, 25.0, 1, false, 50),
                    ChallengeParticipant("u_7", "Eko Prasetyo", 0xFFFF8533, 20.0, 25.0, 2, false, 22),
                    ChallengeParticipant("u_current", "Kamu", 0xFF38BDF8, 15.0, 25.0, 3, true, 14)
                )
            ),
            ChallengeItem(
                id = "ch_4",
                title = "King of the Hill: Dago Peak +200m",
                subtitle = "Akumulasi elevasi mendaki 200 meter",
                targetKm = 12.0,
                currentProgressKm = 4.2,
                participantsCount = 412,
                daysLeft = 18,
                rewardBadge = "⛰️ Apex Mountain Goat",
                rewardXp = 450,
                isJoined = false,
                category = "Elevasi Bukit",
                creatorName = "Bandung Trail Runners",
                participants = listOf(
                    ChallengeParticipant("u_8", "Gilang Ramadhan", 0xFFD4FF5F, 12.0, 12.0, 1, false, 41),
                    ChallengeParticipant("u_current", "Kamu", 0xFF38BDF8, 4.2, 12.0, 2, true, 9)
                )
            )
        )
    }

    private fun initCommunityRoutes() {
        val gbkRoutePoints = generateRealisticCircuit(-6.2185, 106.8025, 380.0, 5, 3.5f)
        val pikRoutePoints = generateRealisticCircuit(-6.0950, 106.7450, 550.0, 8, 3.4f)
        val sudirmanRoutePoints = generateRealisticCircuit(-6.2088, 106.8220, 600.0, 10, 3.3f)
        val tahuraRoutePoints = generateRealisticCircuit(-6.8580, 107.6320, 400.0, 7, 2.9f)
        val scbdRoutePoints = generateRealisticCircuit(-6.2250, 106.8080, 420.0, 6, 3.6f)

        _communityRoutes.value = listOf(
            CommunityRoute(
                id = "route_1",
                title = "GBK Inner Loop Speed Track",
                authorName = "Reza Fahlevi (Pacer Jakarta)",
                locationName = "Gelora Bung Karno, Senayan",
                distanceKm = 5.0,
                elevationGainMeters = 15,
                estDurationSeconds = 1500, // 25:00
                avgPaceSecondsPerKm = 300, // 5'00"/km
                difficulty = "Mudah (Easy)",
                sceneryCategory = "Taman Kota",
                rating = 4.9,
                reviewsCount = 342,
                bookmarksCount = 820,
                isBookmarked = true,
                routePoints = gbkRoutePoints,
                description = "Jalur lingkar dalam GBK dengan aspal karet empuk berstandar atletik internasional. Bebas kendaraan bermotor 100%, sangat aman untuk interval speed work dan sub-25 training.",
                surfaceType = "Tartan Karet & Aspal Mulus",
                bestTimeToRun = "05:30 - 08:00 Pagi / 19:00 Malam",
                recommendedShoeType = "Carbon Racer / Tempo Trainer"
            ),
            CommunityRoute(
                id = "route_2",
                title = "PIK 2 Ocean Coastline Breeze",
                authorName = "Jessica Wijaya",
                locationName = "Pantai Pasir Putih PIK 2, Tangerang",
                distanceKm = 10.5,
                elevationGainMeters = 8,
                estDurationSeconds = 3465, // 57m 45s
                avgPaceSecondsPerKm = 330, // 5'30"/km
                difficulty = "Sedang (Moderate)",
                sceneryCategory = "Tepi Pantai & Danau",
                rating = 4.8,
                reviewsCount = 215,
                bookmarksCount = 540,
                isBookmarked = false,
                routePoints = pikRoutePoints,
                description = "Rute lurus pesisir laut utara Jakarta dengan semilir angin laut yang menyegarkan. Trotoar ekstra lebar dengan penerangan modern di malam hari. Cocok untuk long run akhir pekan.",
                surfaceType = "Paving Block Rata & Aspal",
                bestTimeToRun = "05:45 - 07:15 Pagi",
                recommendedShoeType = "Max Cushion / Daily Trainer"
            ),
            CommunityRoute(
                id = "route_3",
                title = "Sudirman - Thamrin CFD Grand Boulevard",
                authorName = "Komunitas RunID",
                locationName = "Bundaran HI ke Senayan, Jakarta Pusat",
                distanceKm = 12.0,
                elevationGainMeters = 24,
                estDurationSeconds = 3840, // 1h 04m
                avgPaceSecondsPerKm = 320, // 5'20"/km
                difficulty = "Sedang (Moderate)",
                sceneryCategory = "Jalur CFD Bebas Polusi",
                rating = 5.0,
                reviewsCount = 560,
                bookmarksCount = 1290,
                isBookmarked = true,
                routePoints = sudirmanRoutePoints,
                description = "Ikon rute lari Jakarta setiap Minggu pagi! Melintasi pencakar langit megah ibu kota tanpa polusi dan deru kendaraan. Suasana super meriah dengan ribuan pelari lain.",
                surfaceType = "Aspal Mulus Grade A",
                bestTimeToRun = "Minggu Pagi 06:00 - 10:00",
                recommendedShoeType = "Super Trainer / Plated Shoes"
            ),
            CommunityRoute(
                id = "route_4",
                title = "Tahura Dago Pine Forest Trail",
                authorName = "Rangga Mountain Scout",
                locationName = "Taman Hutan Raya Ir. H. Djuanda, Bandung",
                distanceKm = 8.2,
                elevationGainMeters = 245,
                estDurationSeconds = 3690, // 1h 01m
                avgPaceSecondsPerKm = 450, // 7'30"/km
                difficulty = "Menantang (Challenging)",
                sceneryCategory = "Hutan & Bukit",
                rating = 4.7,
                reviewsCount = 188,
                bookmarksCount = 375,
                isBookmarked = false,
                routePoints = tahuraRoutePoints,
                description = "Jalur trail menanjak di tengah rindangnya pepohonan pinus dan udara pegunungan Bandung yang sejuk 19°C. Tanjakan teknikal yang membangun kekuatan otot paha dan betis.",
                surfaceType = "Tanah Padat, Bebatuan & Kayu",
                bestTimeToRun = "06:30 - 09:30 Pagi",
                recommendedShoeType = "Trail Grip Shoes (Vibram Outsole)"
            ),
            CommunityRoute(
                id = "route_5",
                title = "SCBD Twilight Skyline Loop",
                authorName = "Aldi Putra (Night Striders)",
                locationName = "SCBD Lot 8 & Pacific Place, Jakarta Selatan",
                distanceKm = 6.4,
                elevationGainMeters = 18,
                estDurationSeconds = 1984, // 33m 04s
                avgPaceSecondsPerKm = 310, // 5'10"/km
                difficulty = "Mudah (Easy)",
                sceneryCategory = "Perkotaan Modern",
                rating = 4.8,
                reviewsCount = 278,
                bookmarksCount = 612,
                isBookmarked = false,
                routePoints = scbdRoutePoints,
                description = "Rute favorit para pekerja urban setelah jam kantor. Suasana gemerlap lampu gedung bertingkat kaca dengan security 24 jam dan pedestrian yang terawat.",
                surfaceType = "Granit Trotoar & Aspal Halus",
                bestTimeToRun = "18:30 - 21:00 Malam",
                recommendedShoeType = "Lightweight Daily Trainer"
            )
        )
    }

    private fun initCommunityPosts() {
        val runs = generateStarterRuns()
        _communityPosts.value = listOf(
            CommunityPost(
                id = "post_1",
                runnerName = "Dimas Satria",
                runnerAvatar = null,
                locationName = "Gelora Bung Karno (GBK), Jakarta",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                activity = runs[0],
                boostCount = 42,
                isBoosted = true,
                commentCount = 7,
                caption = "Senayan Sunrise Loop! Sensasi negative split di 3 km terakhir terasa luar biasa. Siap untuk HM bulan depan! ⚡🔥"
            ),
            CommunityPost(
                id = "post_2",
                runnerName = "Nadia Kirana",
                runnerAvatar = null,
                locationName = "Sudirman Car Free Day, Jakarta",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                activity = runs[1],
                boostCount = 68,
                isBoosted = false,
                commentCount = 12,
                caption = "Long Run santai Zone 2 ditemani cuaca mendung Jakarta yang adem. Cadence terjaga rapi di 174 SPM! 💨👟"
            ),
            CommunityPost(
                id = "post_3",
                runnerName = "Fajar Pratama",
                runnerAvatar = null,
                locationName = "Taman Kota BSD, Tangerang",
                timestamp = System.currentTimeMillis() - 3600000 * 22,
                activity = runs[2],
                boostCount = 31,
                isBoosted = false,
                commentCount = 4,
                caption = "Interval workout 6x800m. Heart rate tembus Zone 4. Capek tapi nagih banget! 🚀💪"
            )
        )
    }

    suspend fun saveRun(run: RunActivity, userId: String = "apex_runner_pro"): RunActivity = withContext(Dispatchers.IO) {
        val entity = RunEntity.fromDomain(
            domain = run,
            routePointsJson = RunJsonConverter.routePointsToJson(run.routePoints),
            splitsJson = RunJsonConverter.splitsToJson(run.splits)
        )
        runDao.insertRun(entity)

        // Automatically update progress across joined challenges
        updateJoinedChallengesProgress(run.distanceKm)

        try {
            firestoreSyncManager.syncRunToFirestore(userId, run)
        } catch (e: Exception) {
            // Safe offline fallback
        }

        run
    }

    private fun updateJoinedChallengesProgress(addedKm: Double) {
        val updated = _challenges.value.map { ch ->
            if (ch.isJoined) {
                val newProgress = (ch.currentProgressKm + addedKm).coerceAtMost(ch.targetKm * 2)
                val updatedParticipants = ch.participants.map { p ->
                    if (p.isCurrentUser) {
                        p.copy(progressKm = p.progressKm + addedKm)
                    } else p
                }.sortedByDescending { it.progressKm }.mapIndexed { index, participant ->
                    participant.copy(rank = index + 1)
                }
                ch.copy(
                    currentProgressKm = newProgress,
                    participants = updatedParticipants
                )
            } else ch
        }
        _challenges.value = updated
    }

    suspend fun getRunById(id: String): RunActivity? = withContext(Dispatchers.IO) {
        val entity = runDao.getRunById(id) ?: return@withContext null
        val points = RunJsonConverter.jsonToRoutePoints(entity.routePointsJson)
        val splits = RunJsonConverter.jsonToSplits(entity.splitsJson)
        entity.toDomain(points, splits)
    }

    suspend fun updateRunAiAnalysis(runId: String, aiAnalysis: String) = withContext(Dispatchers.IO) {
        val entity = runDao.getRunById(runId)
        if (entity != null) {
            val updated = entity.copy(aiAnalysis = aiAnalysis)
            runDao.updateRun(updated)
        }
    }

    suspend fun deleteRun(runId: String) = withContext(Dispatchers.IO) {
        runDao.deleteRunById(runId)
    }

    fun toggleBoostPost(postId: String) {
        val current = _communityPosts.value.toMutableList()
        val index = current.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = current[index]
            val newBoosted = !post.isBoosted
            val newCount = if (newBoosted) post.boostCount + 1 else post.boostCount - 1
            current[index] = post.copy(isBoosted = newBoosted, boostCount = newCount)
            _communityPosts.value = current
        }
    }

    fun toggleJoinChallenge(challengeId: String) {
        val current = _challenges.value.toMutableList()
        val index = current.indexOfFirst { it.id == challengeId }
        if (index != -1) {
            val ch = current[index]
            val newJoined = !ch.isJoined
            val newCount = if (newJoined) ch.participantsCount + 1 else ch.participantsCount - 1
            current[index] = ch.copy(isJoined = newJoined, participantsCount = newCount)
            _challenges.value = current
        }
    }

    fun toggleBookmarkRoute(routeId: String) {
        val current = _communityRoutes.value.toMutableList()
        val index = current.indexOfFirst { it.id == routeId }
        if (index != -1) {
            val route = current[index]
            val newBookmarked = !route.isBookmarked
            val newBookmarksCount = if (newBookmarked) route.bookmarksCount + 1 else route.bookmarksCount - 1
            current[index] = route.copy(isBookmarked = newBookmarked, bookmarksCount = newBookmarksCount)
            _communityRoutes.value = current
        }
    }

    fun createCustomChallenge(
        title: String,
        subtitle: String,
        targetKm: Double,
        daysLeft: Int,
        category: String,
        rewardBadge: String,
        rewardXp: Int
    ) {
        val newChallenge = ChallengeItem(
            id = "custom_ch_${UUID.randomUUID().toString().take(8)}",
            title = title,
            subtitle = subtitle,
            targetKm = targetKm,
            currentProgressKm = 0.0,
            participantsCount = 1,
            daysLeft = daysLeft,
            rewardBadge = rewardBadge,
            rewardXp = rewardXp,
            isJoined = true,
            category = category,
            creatorName = "Kamu",
            participants = listOf(
                ChallengeParticipant("u_current", "Kamu (Apex Runner)", 0xFF38BDF8, 0.0, targetKm, 1, true, 1)
            )
        )
        _challenges.value = listOf(newChallenge) + _challenges.value
    }

    fun cheerParticipant(challengeId: String, userId: String) {
        val current = _challenges.value.toMutableList()
        val index = current.indexOfFirst { it.id == challengeId }
        if (index != -1) {
            val ch = current[index]
            val updatedParticipants = ch.participants.map { p ->
                if (p.userId == userId) p.copy(cheersCount = p.cheersCount + 1) else p
            }
            current[index] = ch.copy(participants = updatedParticipants)
            _challenges.value = current
        }
    }

    fun computePerformanceMetrics(runs: List<RunActivity>): PerformanceMetrics {
        if (runs.isEmpty()) return PerformanceMetrics()

        val totalDistKm = runs.sumOf { it.distanceKm }
        val avgPace = runs.map { it.avgPaceSecondsPerKm }.average().toInt()
        val avgCadence = runs.map { it.avgCadenceSpm }.average().toInt()

        // Estimate VO2 Max based on 5K pace and heart rate efficiency
        val vo2Score = (15.0 * (1000.0 / avgPace.coerceAtLeast(180)) - 5.0).roundToInt().coerceIn(38, 68)
        val vo2Cat = when {
            vo2Score >= 56 -> "Elite Atlet (Top 1%)"
            vo2Score >= 50 -> "Unggul (Top 5%)"
            vo2Score >= 44 -> "Sangat Baik (Top 15%)"
            else -> "Sehat & Berkembang"
        }

        // Compute Pace Consistency
        val paceConsistency = (100 - (runs.map { it.maxPaceSecondsPerKm - it.avgPaceSecondsPerKm }.average() * 0.2)).roundToInt().coerceIn(70, 98)

        // Race predictions derived from Riegel's formula
        val predicted5kSec = (avgPace * 5 * 0.92).toLong()
        val predicted10kSec = (avgPace * 10 * 0.96).toLong()
        val predictedHmSec = (avgPace * 21.1 * 1.02).toLong()
        val predictedFmSec = (avgPace * 42.2 * 1.08).toLong()

        val raceMap = mapOf(
            "5K" to com.example.data.model.formatDuration(predicted5kSec),
            "10K" to com.example.data.model.formatDuration(predicted10kSec),
            "Half Marathon (21.1K)" to com.example.data.model.formatDuration(predictedHmSec),
            "Full Marathon (42.2K)" to com.example.data.model.formatDuration(predictedFmSec)
        )

        return PerformanceMetrics(
            vo2MaxEstimate = vo2Score,
            vo2MaxCategory = vo2Cat,
            paceConsistencyScore = paceConsistency,
            avgCadenceSpm = avgCadence,
            groundContactTimeMs = 210 + (180 - avgCadence).coerceAtLeast(0) * 2,
            strideLengthMeters = 1.25,
            verticalOscillationCm = 7.2,
            cardiacDriftPercentage = 2.4,
            aerobicEfficiencyScore = 90,
            racePredictions = raceMap,
            smartFeedbackList = listOf(
                "Cadence rata-rata $avgCadence SPM optimal untuk biomekanik hemat energi.",
                "Pace consistency $paceConsistency% menandakan ritme lari stabil dan minim fluktuasi lelah.",
                "Pola volume mingguan menunjukkan peningkatan aerobik base yang kokoh."
            )
        )
    }

    private fun generateStarterRuns(): List<RunActivity> {
        val now = System.currentTimeMillis()

        // Run 1: 10.2 KM Tempo run at GBK
        val points1 = generateRealisticCircuit(
            centerLat = -6.2185,
            centerLng = 106.8025,
            radiusMeters = 400.0,
            laps = 11,
            baseSpeedMs = 3.6f
        )
        val splits1 = listOf(
            KmSplit(1, 315, 315, 4, 138),
            KmSplit(2, 308, 308, 3, 144),
            KmSplit(3, 302, 302, 2, 148),
            KmSplit(4, 298, 298, 5, 153),
            KmSplit(5, 295, 295, 2, 155),
            KmSplit(6, 292, 292, 3, 158),
            KmSplit(7, 288, 288, 4, 161),
            KmSplit(8, 285, 285, 2, 164),
            KmSplit(9, 280, 280, 3, 168),
            KmSplit(10, 272, 272, 5, 172)
        )
        val run1 = RunActivity(
            id = "starter_run_1",
            title = "⚡ Senayan Speed Circuit (Negative Split)",
            timestamp = now - 86400000L,
            durationSeconds = 2935,
            distanceMeters = 10240.0,
            avgPaceSecondsPerKm = 286,
            maxPaceSecondsPerKm = 240,
            caloriesBurned = 720,
            elevationGainMeters = 33,
            avgCadenceSpm = 176,
            avgHeartRateBpm = 156,
            routePoints = points1,
            splits = splits1,
            aiAnalysis = """
                ⚡ **APEX INTELLIGENCE AUDIT (Deep Thinking Engine)**
                
                🎯 **Pace Execution: Masterclass Negative Split!**
                Pacingmu dieksekusi dengan presisi tinggi: KM 1 dibuka pada **5'15"/km** dan dipacu progresif hingga KM 10 pada **4'32"/km**. Pola ini membakar glikogen secara sangat efisien.
                
                🏃‍♂️ **Biomekanik & Efisiensi:**
                - **Cadence Rata-rata:** 176 SPM (Zona emas ground contact time).
                - **Kardiovaskular:** Heart rate rata-rata 156 BPM dengan kontrol respirasi stabil di Zone 3.
                
                🛡️ **Recovery Protocol:**
                - Waktu pemulihan optimal: **24 Jam**.
                - Hidrasi 500ml elektrolit & asupan glikogen.
            """.trimIndent(),
            feelingTag = "🚀 Luar Biasa",
            shoeName = "Nike Alphafly 3",
            weatherCondition = "Cerah Berawan 27°C"
        )

        // Run 2: 6.5 KM Morning Easy Run
        val points2 = generateRealisticCircuit(
            centerLat = -6.2000,
            centerLng = 106.8200,
            radiusMeters = 300.0,
            laps = 6,
            baseSpeedMs = 3.0f
        )
        val splits2 = listOf(
            KmSplit(1, 345, 345, 6, 132),
            KmSplit(2, 340, 340, 4, 136),
            KmSplit(3, 338, 338, 5, 140),
            KmSplit(4, 335, 335, 8, 142),
            KmSplit(5, 330, 330, 5, 144),
            KmSplit(6, 325, 325, 4, 148)
        )
        val run2 = RunActivity(
            id = "starter_run_2",
            title = "🌅 Sudirman Morning Aerobic Base",
            timestamp = now - 86400000L * 3,
            durationSeconds = 2213,
            distanceMeters = 6500.0,
            avgPaceSecondsPerKm = 340,
            maxPaceSecondsPerKm = 310,
            caloriesBurned = 460,
            elevationGainMeters = 32,
            avgCadenceSpm = 171,
            avgHeartRateBpm = 140,
            routePoints = points2,
            splits = splits2,
            feelingTag = "✨ Segar & Nyaman",
            shoeName = "Asics Novablast 4",
            weatherCondition = "Sejuk 24°C"
        )

        // Run 3: 5.0 KM PB Blitz
        val points3 = generateRealisticCircuit(
            centerLat = -6.2185,
            centerLng = 106.8025,
            radiusMeters = 400.0,
            laps = 5,
            baseSpeedMs = 3.9f
        )
        val splits3 = listOf(
            KmSplit(1, 270, 270, 2, 160),
            KmSplit(2, 265, 265, 3, 165),
            KmSplit(3, 262, 262, 1, 168),
            KmSplit(4, 260, 260, 4, 172),
            KmSplit(5, 253, 253, 2, 178)
        )
        val run3 = RunActivity(
            id = "starter_run_3",
            title = "🔥 5K All-Out PR Blitz",
            timestamp = now - 86400000L * 6,
            durationSeconds = 1310,
            distanceMeters = 5000.0,
            avgPaceSecondsPerKm = 262,
            maxPaceSecondsPerKm = 230,
            caloriesBurned = 380,
            elevationGainMeters = 12,
            avgCadenceSpm = 182,
            avgHeartRateBpm = 169,
            routePoints = points3,
            splits = splits3,
            feelingTag = "🔥 Rekor Pribadi!",
            shoeName = "Nike Vaporfly 3",
            weatherCondition = "Mendung 26°C"
        )

        return listOf(run1, run2, run3)
    }

    private fun generateRealisticCircuit(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Double,
        laps: Int,
        baseSpeedMs: Float
    ): List<GPSPoint> {
        val points = mutableListOf<GPSPoint>()
        val pointsPerLap = 36
        val earthRadius = 6371000.0
        var totalDist = 0.0

        for (lap in 0 until laps) {
            for (i in 0 until pointsPerLap) {
                val angle = (2.0 * Math.PI * i) / pointsPerLap
                val xOffset = radiusMeters * 1.4 * Math.cos(angle)
                val yOffset = radiusMeters * 0.8 * Math.sin(angle)

                val latOffset = (yOffset / earthRadius) * (180.0 / Math.PI)
                val lngOffset = (xOffset / (earthRadius * Math.cos(Math.toRadians(centerLat)))) * (180.0 / Math.PI)

                val lat = centerLat + latOffset
                val lng = centerLng + lngOffset
                val alt = 15.0 + Math.sin(angle * 2) * 5.0

                if (points.isNotEmpty()) {
                    totalDist += (2.0 * Math.PI * radiusMeters) / pointsPerLap
                }

                val speedVariance = baseSpeedMs + (Math.sin(angle * 3) * 0.4).toFloat()
                points.add(
                    GPSPoint(
                        latitude = lat,
                        longitude = lng,
                        altitude = alt,
                        speed = speedVariance,
                        timestamp = System.currentTimeMillis() - ((laps * pointsPerLap - (lap * pointsPerLap + i)) * 1000L),
                        distanceFromStartMeters = totalDist
                    )
                )
            }
        }
        return points
    }
}
