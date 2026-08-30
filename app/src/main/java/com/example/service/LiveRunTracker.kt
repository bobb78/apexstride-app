package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.example.data.model.GPSPoint
import com.example.data.model.KmSplit
import com.example.data.model.RunActivity
import com.example.data.model.formatPace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class RunTrackingState {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

data class LiveRunTelemetry(
    val state: RunTrackingState = RunTrackingState.IDLE,
    val activityType: String = "Lari", // "Lari" or "Jalan Kaki"
    val elapsedSeconds: Long = 0L,
    val activeMovingSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerKm: Int = 0,
    val avgPaceSecondsPerKm: Int = 0,
    val currentSpeedKmh: Double = 0.0,
    val caloriesBurned: Int = 0,
    val currentCadenceSpm: Int = 0,
    val estimatedHeartRateBpm: Int = 0,
    val elevationGainMeters: Int = 0,
    val isActivelyMoving: Boolean = false,
    val movementStatusText: String = "Menunggu Gerakan",
    val gpsAccuracyMeters: Float = 0.0f,
    val points: List<GPSPoint> = emptyList(),
    val splits: List<KmSplit> = emptyList(),
    val isSimulationMode: Boolean = true,
    val isVoiceCoachEnabled: Boolean = true,
    val currentSplitTimeSeconds: Long = 0L
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0
}

class LiveRunTracker(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var simulationJob: Job? = null

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _telemetry = MutableStateFlow(LiveRunTelemetry())
    val telemetry: StateFlow<LiveRunTelemetry> = _telemetry.asStateFlow()

    private var lastRecordedLocation: Location? = null
    private var lastSplitDistanceMeters = 0.0
    private var splitStartSeconds = 0L

    // Simulation track variables
    private var simAngle = 0.0
    private val simCenterLat = -6.2185
    private val simCenterLng = 106.8025

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = tts?.setLanguage(Locale("id", "ID"))
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.setLanguage(Locale.US)
                        }
                        isTtsReady = true
                    } catch (e: Throwable) {
                        isTtsReady = false
                    }
                }
            }
        } catch (e: Throwable) {
            isTtsReady = false
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (_telemetry.value.state != RunTrackingState.RUNNING) return
            val loc = result.lastLocation ?: return
            handleNewLocation(loc)
        }
    }

    fun startRun(isSimulation: Boolean = false, activityType: String = "Lari") {
        _telemetry.value = LiveRunTelemetry(
            state = RunTrackingState.RUNNING,
            activityType = activityType,
            isSimulationMode = isSimulation,
            isActivelyMoving = isSimulation,
            movementStatusText = if (isSimulation) "Simulasi $activityType Aktif" else "Mencari Sinyal GPS...",
            estimatedHeartRateBpm = if (activityType == "Lari") 135 else 105,
            currentCadenceSpm = if (activityType == "Lari") 168 else 110
        )
        lastRecordedLocation = null
        lastSplitDistanceMeters = 0.0
        splitStartSeconds = 0L
        simAngle = 0.0

        speakVoiceCue("Sesi $activityType dimulai! Selamat berolahraga.")

        startTimer()

        if (isSimulation) {
            startSimulationLoop(activityType)
        } else {
            startGpsTracking()
        }
    }

    fun pauseRun() {
        if (_telemetry.value.state == RunTrackingState.RUNNING) {
            _telemetry.update { it.copy(state = RunTrackingState.PAUSED) }
            speakVoiceCue("Sesi latihan dijeda.")
        }
    }

    fun resumeRun() {
        if (_telemetry.value.state == RunTrackingState.PAUSED) {
            _telemetry.update { it.copy(state = RunTrackingState.RUNNING) }
            speakVoiceCue("Sesi latihan dilanjutkan!")
        }
    }

    fun toggleVoiceCoach() {
        _telemetry.update { it.copy(isVoiceCoachEnabled = !it.isVoiceCoachEnabled) }
    }

    fun finishRun(): RunActivity {
        stopTracking()
        val current = _telemetry.value
        _telemetry.update { it.copy(state = RunTrackingState.FINISHED) }

        val finalAvgPace = if (current.distanceKm > 0.05) {
            (current.elapsedSeconds / current.distanceKm).toInt()
        } else {
            0
        }

        // Add remaining partial split if any
        val finalSplits = current.splits.toMutableList()
        val remainingDist = current.distanceMeters - lastSplitDistanceMeters
        if (remainingDist > 200) {
            val partialDur = current.elapsedSeconds - splitStartSeconds
            val partialPace = if (remainingDist > 0) ((partialDur / (remainingDist / 1000.0)).toInt()) else finalAvgPace
            finalSplits.add(
                KmSplit(
                    kmNumber = finalSplits.size + 1,
                    durationSeconds = partialDur,
                    paceSecondsPerKm = partialPace,
                    elevationGainMeters = 2,
                    avgHeartRateBpm = current.estimatedHeartRateBpm
                )
            )
        }

        speakVoiceCue("Sesi latihan selesai! Total jarak ${String.format(Locale.US, "%.2f", current.distanceKm)} kilometer.")

        return RunActivity(
            id = UUID.randomUUID().toString(),
            title = if (current.activityType == "Lari") "Sesi Lari Outdoor" else "Sesi Jalan Kaki Outdoor",
            activityType = current.activityType,
            timestamp = System.currentTimeMillis(),
            durationSeconds = current.elapsedSeconds,
            distanceMeters = current.distanceMeters,
            avgPaceSecondsPerKm = finalAvgPace,
            maxPaceSecondsPerKm = if (finalAvgPace > 30) finalAvgPace - 30 else finalAvgPace,
            caloriesBurned = current.caloriesBurned,
            elevationGainMeters = current.elevationGainMeters,
            avgCadenceSpm = if (current.currentCadenceSpm > 0) current.currentCadenceSpm else if (current.activityType == "Lari") 172 else 115,
            avgHeartRateBpm = if (current.estimatedHeartRateBpm > 0) current.estimatedHeartRateBpm else if (current.activityType == "Lari") 152 else 110,
            routePoints = current.points,
            splits = finalSplits,
            feelingTag = "🚀 Kuat & Bertenaga",
            shoeName = "Nike Alphafly 3",
            weatherCondition = "Cerah 28°C"
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (_telemetry.value.state == RunTrackingState.RUNNING) {
                    _telemetry.update { prev ->
                        val newSeconds = prev.elapsedSeconds + 1
                        val newMovingSeconds = if (prev.isActivelyMoving || prev.isSimulationMode) {
                            prev.activeMovingSeconds + 1
                        } else {
                            prev.activeMovingSeconds
                        }
                        val splitSec = newSeconds - splitStartSeconds
                        val calPerKm = if (prev.activityType == "Lari") 68 else 45
                        val cal = (prev.distanceKm * calPerKm).toInt()

                        val isRunningType = prev.activityType == "Lari"
                        val baseHr = if (isRunningType) 138 else 102
                        val hr = if (prev.isActivelyMoving || prev.isSimulationMode) {
                            (baseHr + (newSeconds % 25) / 2).coerceIn(80, 185).toInt()
                        } else {
                            (85 + (newSeconds % 10)).coerceIn(70, 100).toInt()
                        }

                        val cadence = if (prev.isActivelyMoving || prev.isSimulationMode) {
                            if (isRunningType) prev.currentCadenceSpm.coerceIn(150, 195) else prev.currentCadenceSpm.coerceIn(90, 135)
                        } else {
                            0
                        }

                        val avgPace = if (prev.distanceKm > 0.02 && newMovingSeconds > 0) {
                            (newMovingSeconds / prev.distanceKm).toInt()
                        } else {
                            0
                        }

                        prev.copy(
                            elapsedSeconds = newSeconds,
                            activeMovingSeconds = newMovingSeconds,
                            currentSplitTimeSeconds = splitSec,
                            caloriesBurned = cal,
                            estimatedHeartRateBpm = hr,
                            currentCadenceSpm = cadence,
                            avgPaceSecondsPerKm = avgPace
                        )
                    }
                }
            }
        }
    }

    private fun startSimulationLoop(activityType: String) {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            val earthRadius = 6371000.0
            val radiusMeters = 350.0
            val isRunning = activityType == "Lari"
            val angularSpeed = if (isRunning) 0.045 else 0.018
            val baseSpeedMs = if (isRunning) 3.4f else 1.35f

            while (isActive) {
                delay(1000L)
                if (_telemetry.value.state == RunTrackingState.RUNNING) {
                    simAngle += angularSpeed
                    val xOffset = radiusMeters * 1.4 * Math.cos(simAngle)
                    val yOffset = radiusMeters * 0.9 * Math.sin(simAngle)

                    val latOffset = (yOffset / earthRadius) * (180.0 / Math.PI)
                    val lngOffset = (xOffset / (earthRadius * Math.cos(Math.toRadians(simCenterLat)))) * (180.0 / Math.PI)

                    val lat = simCenterLat + latOffset
                    val lng = simCenterLng + lngOffset
                    val alt = 15.0 + Math.sin(simAngle) * 3.0

                    val simulatedSpeedMs = (baseSpeedMs + (Math.sin(simAngle * 4) * (if (isRunning) 0.3f else 0.1f))).toFloat()
                    val instPaceSec = if (simulatedSpeedMs > 0.3f) (1000f / simulatedSpeedMs).toInt() else 600

                    _telemetry.update { prev ->
                        val addedMeters = simulatedSpeedMs.toDouble()
                        val newDist = prev.distanceMeters + addedMeters
                        val newPoint = GPSPoint(
                            latitude = lat,
                            longitude = lng,
                            altitude = alt,
                            speed = simulatedSpeedMs,
                            timestamp = System.currentTimeMillis(),
                            distanceFromStartMeters = newDist
                        )
                        val updatedPoints = prev.points + newPoint

                        // Check for split completion (every 1000m)
                        val splits = prev.splits.toMutableList()
                        if (newDist - lastSplitDistanceMeters >= 1000.0) {
                            val kmNum = splits.size + 1
                            val splitDur = prev.elapsedSeconds - splitStartSeconds
                            val splitPace = splitDur.toInt()
                            splits.add(
                                KmSplit(
                                    kmNumber = kmNum,
                                    durationSeconds = splitDur,
                                    paceSecondsPerKm = splitPace,
                                    elevationGainMeters = 3,
                                    avgHeartRateBpm = prev.estimatedHeartRateBpm
                                )
                            )
                            lastSplitDistanceMeters = newDist
                            splitStartSeconds = prev.elapsedSeconds

                            speakSplitAnnouncement(kmNum, splitPace)
                        }

                        val cadenceVal = if (isRunning) {
                            (168 + (Math.sin(simAngle * 5) * 4)).toInt()
                        } else {
                            (112 + (Math.sin(simAngle * 3) * 3)).toInt()
                        }

                        val statusLabel = if (isRunning) "Berlari Aktif" else "Jalan Kaki Aktif"

                        prev.copy(
                            distanceMeters = newDist,
                            currentPaceSecondsPerKm = instPaceSec,
                            currentSpeedKmh = (simulatedSpeedMs * 3.6),
                            currentCadenceSpm = cadenceVal,
                            isActivelyMoving = true,
                            movementStatusText = "$statusLabel (${String.format(Locale.US, "%.1f", simulatedSpeedMs * 3.6)} km/jam)",
                            elevationGainMeters = (newDist / 250.0).toInt(),
                            points = updatedPoints,
                            splits = splits
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGpsTracking() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(800L)
                .setMinUpdateDistanceMeters(1.0f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // Fallback to simulation if GPS permission fails
            startSimulationLoop(_telemetry.value.activityType)
        }
    }

    private fun handleNewLocation(loc: Location) {
        // 1. Filter out poor accuracy GPS readings to prevent jitter spikes
        if (loc.hasAccuracy() && loc.accuracy > 25.0f) {
            _telemetry.update {
                it.copy(
                    gpsAccuracyMeters = loc.accuracy,
                    movementStatusText = "Akurasi GPS (${loc.accuracy.toInt()}m) - Menunggu Sinyal Jernih"
                )
            }
            return
        }

        val prevLoc = lastRecordedLocation
        val deltaMeters = if (prevLoc != null) loc.distanceTo(prevLoc).toDouble() else 0.0
        val deltaTimeSec = if (prevLoc != null) (loc.time - prevLoc.time) / 1000.0 else 1.0

        // Calculate speed based on GPS hardware speed or computed distance over time
        val calculatedSpeedMs = if (deltaTimeSec > 0.3) (deltaMeters / deltaTimeSec).toFloat() else 0f
        val rawSpeedMs = if (loc.hasSpeed() && loc.speed > 0f) loc.speed else calculatedSpeedMs

        // WALKING & RUNNING MOVEMENT FILTER:
        // A user swinging their arms / shaking hands without displacing will have deltaMeters < 1.4m or speed < 0.6 m/s (~2.16 km/h).
        // Only count as true displacement when:
        // - WALKING: deltaMeters >= 1.4m AND speed between 0.6 m/s (~2.16 km/h) and 1.8 m/s (~6.48 km/h)
        // - RUNNING: deltaMeters >= 1.8m AND speed >= 1.8 m/s (~6.48 km/h) up to 12.0 m/s (~43.2 km/h)
        val isWalking = (deltaMeters >= 1.4 && rawSpeedMs >= 0.6f && rawSpeedMs < 1.8f)
        val isRunning = (deltaMeters >= 1.8 && rawSpeedMs >= 1.8f && rawSpeedMs <= 12.0f)
        val isTrueMovement = isWalking || isRunning

        if (isTrueMovement) {
            lastRecordedLocation = loc
            val instPaceSec = (1000f / rawSpeedMs).toInt()
            val dynamicCadence = if (isRunning) {
                (155 + (rawSpeedMs * 7.5f)).toInt().coerceIn(150, 195)
            } else {
                (95 + (rawSpeedMs * 18.0f)).toInt().coerceIn(90, 135)
            }

            val statusText = if (isRunning) {
                "Berlari Aktif (${String.format(Locale.US, "%.1f", rawSpeedMs * 3.6)} km/jam)"
            } else {
                "Jalan Kaki (${String.format(Locale.US, "%.1f", rawSpeedMs * 3.6)} km/jam)"
            }

            _telemetry.update { prev ->
                val newDist = prev.distanceMeters + deltaMeters
                val newPoint = GPSPoint(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitude,
                    speed = rawSpeedMs,
                    timestamp = System.currentTimeMillis(),
                    distanceFromStartMeters = newDist
                )
                val updatedPoints = prev.points + newPoint

                val splits = prev.splits.toMutableList()
                if (newDist - lastSplitDistanceMeters >= 1000.0) {
                    val kmNum = splits.size + 1
                    val splitDur = prev.elapsedSeconds - splitStartSeconds
                    splits.add(
                        KmSplit(
                            kmNumber = kmNum,
                            durationSeconds = splitDur,
                            paceSecondsPerKm = splitDur.toInt(),
                            elevationGainMeters = 2,
                            avgHeartRateBpm = prev.estimatedHeartRateBpm
                        )
                    )
                    lastSplitDistanceMeters = newDist
                    splitStartSeconds = prev.elapsedSeconds

                    speakSplitAnnouncement(kmNum, splitDur.toInt())
                }

                prev.copy(
                    distanceMeters = newDist,
                    currentPaceSecondsPerKm = instPaceSec,
                    currentSpeedKmh = (rawSpeedMs * 3.6),
                    currentCadenceSpm = dynamicCadence,
                    isActivelyMoving = true,
                    movementStatusText = statusText,
                    gpsAccuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 5.0f,
                    points = updatedPoints,
                    splits = splits
                )
            }
        } else {
            // User is stationary or just moving hand/arm in place
            _telemetry.update { prev ->
                prev.copy(
                    currentPaceSecondsPerKm = 0,
                    currentSpeedKmh = 0.0,
                    currentCadenceSpm = 0,
                    isActivelyMoving = false,
                    movementStatusText = "Diam / Menunggu Langkah Nyata",
                    gpsAccuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 5.0f
                )
            }
        }
    }

    private fun speakSplitAnnouncement(kmNumber: Int, paceSecondsPerKm: Int) {
        val paceText = formatPace(paceSecondsPerKm)
        val msg = "Kilometer $kmNumber selesai! Pace: $paceText per kilometer."
        speakVoiceCue(msg)
    }

    private fun speakVoiceCue(message: String) {
        if (!_telemetry.value.isVoiceCoachEnabled) return
        try {
            if (isTtsReady) {
                tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "apex_cue_${System.currentTimeMillis()}")
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }

    private fun stopTracking() {
        timerJob?.cancel()
        timerJob = null
        simulationJob?.cancel()
        simulationJob = null
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        stopTracking()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Throwable) {
            // Ignore
        }
    }
}
