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
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerKm: Int = 0,
    val avgPaceSecondsPerKm: Int = 0,
    val currentSpeedKmh: Double = 0.0,
    val caloriesBurned: Int = 0,
    val currentCadenceSpm: Int = 0,
    val estimatedHeartRateBpm: Int = 0,
    val elevationGainMeters: Int = 0,
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
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                isTtsReady = true
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (_telemetry.value.state != RunTrackingState.RUNNING) return
            val loc = result.lastLocation ?: return
            handleNewLocation(loc)
        }
    }

    fun startRun(isSimulation: Boolean = false) {
        _telemetry.value = LiveRunTelemetry(
            state = RunTrackingState.RUNNING,
            isSimulationMode = isSimulation,
            estimatedHeartRateBpm = 135,
            currentCadenceSpm = 168
        )
        lastRecordedLocation = null
        lastSplitDistanceMeters = 0.0
        splitStartSeconds = 0L
        simAngle = 0.0

        speakVoiceCue("Sesi lari dimulai! Selamat berlari bersama ApexStride.")

        startTimer()

        if (isSimulation) {
            startSimulationLoop()
        } else {
            startGpsTracking()
        }
    }

    fun pauseRun() {
        if (_telemetry.value.state == RunTrackingState.RUNNING) {
            _telemetry.update { it.copy(state = RunTrackingState.PAUSED) }
            speakVoiceCue("Sesi lari dijeda.")
        }
    }

    fun resumeRun() {
        if (_telemetry.value.state == RunTrackingState.PAUSED) {
            _telemetry.update { it.copy(state = RunTrackingState.RUNNING) }
            speakVoiceCue("Sesi lari dilanjutkan!")
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

        speakVoiceCue("Sesi lari selesai! Total jarak ${String.format(Locale.US, "%.2f", current.distanceKm)} kilometer.")

        return RunActivity(
            id = UUID.randomUUID().toString(),
            title = if (current.isSimulationMode) "⚡ GBK Sunset Speed Stride" else "🏃 Real-time Outdoor Stride",
            timestamp = System.currentTimeMillis(),
            durationSeconds = current.elapsedSeconds,
            distanceMeters = current.distanceMeters,
            avgPaceSecondsPerKm = finalAvgPace,
            maxPaceSecondsPerKm = if (finalAvgPace > 30) finalAvgPace - 30 else finalAvgPace,
            caloriesBurned = current.caloriesBurned,
            elevationGainMeters = current.elevationGainMeters,
            avgCadenceSpm = if (current.currentCadenceSpm > 0) current.currentCadenceSpm else 172,
            avgHeartRateBpm = if (current.estimatedHeartRateBpm > 0) current.estimatedHeartRateBpm else 152,
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
                        val splitSec = newSeconds - splitStartSeconds
                        val cal = (prev.distanceKm * 68).toInt()
                        val hr = (135 + (newSeconds % 30) / 2).coerceIn(120, 180).toInt()
                        val cadence = (170 + (newSeconds % 10) - 5).coerceIn(160, 188).toInt()

                        val avgPace = if (prev.distanceKm > 0.02) {
                            (newSeconds / prev.distanceKm).toInt()
                        } else {
                            0
                        }

                        prev.copy(
                            elapsedSeconds = newSeconds,
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

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            val earthRadius = 6371000.0
            val radiusMeters = 350.0

            while (isActive) {
                delay(1000L)
                if (_telemetry.value.state == RunTrackingState.RUNNING) {
                    simAngle += 0.045 // Realistic running speed around oval
                    val xOffset = radiusMeters * 1.4 * Math.cos(simAngle)
                    val yOffset = radiusMeters * 0.9 * Math.sin(simAngle)

                    val latOffset = (yOffset / earthRadius) * (180.0 / Math.PI)
                    val lngOffset = (xOffset / (earthRadius * Math.cos(Math.toRadians(simCenterLat)))) * (180.0 / Math.PI)

                    val lat = simCenterLat + latOffset
                    val lng = simCenterLng + lngOffset
                    val alt = 15.0 + Math.sin(simAngle) * 3.0

                    val simulatedSpeedMs = (3.4f + (Math.sin(simAngle * 4) * 0.3f)).toFloat()
                    val instPaceSec = if (simulatedSpeedMs > 0.5f) (1000f / simulatedSpeedMs).toInt() else 300

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

                        prev.copy(
                            distanceMeters = newDist,
                            currentPaceSecondsPerKm = instPaceSec,
                            currentSpeedKmh = (simulatedSpeedMs * 3.6),
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
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(1.5f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // Fallback to simulation if GPS permission fails
            startSimulationLoop()
        }
    }

    private fun handleNewLocation(loc: Location) {
        val prevLoc = lastRecordedLocation
        val deltaMeters = if (prevLoc != null) loc.distanceTo(prevLoc).toDouble() else 0.0
        lastRecordedLocation = loc

        val speedMs = if (loc.hasSpeed()) loc.speed else 3.2f
        val instPaceSec = if (speedMs > 0.5f) (1000f / speedMs).toInt() else 300

        _telemetry.update { prev ->
            val newDist = prev.distanceMeters + deltaMeters
            val newPoint = GPSPoint(
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitude = loc.altitude,
                speed = speedMs,
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
                currentSpeedKmh = (speedMs * 3.6),
                points = updatedPoints,
                splits = splits
            )
        }
    }

    private fun speakSplitAnnouncement(kmNumber: Int, paceSecondsPerKm: Int) {
        val paceText = formatPace(paceSecondsPerKm)
        val msg = "Kilometer $kmNumber selesai! Pace split: $paceText per kilometer. Pertahankan ritme larimu!"
        speakVoiceCue(msg)
    }

    private fun speakVoiceCue(message: String) {
        if (!_telemetry.value.isVoiceCoachEnabled) return
        if (isTtsReady) {
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "apex_cue_${System.currentTimeMillis()}")
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
        tts?.stop()
        tts?.shutdown()
    }
}
