package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GPSPoint
import com.example.data.model.KmSplit
import com.example.data.model.RunActivity

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val avgPaceSecondsPerKm: Int,
    val maxPaceSecondsPerKm: Int,
    val caloriesBurned: Int,
    val elevationGainMeters: Int,
    val avgCadenceSpm: Int,
    val avgHeartRateBpm: Int,
    val routePointsJson: String,
    val splitsJson: String,
    val aiAnalysis: String?,
    val isSyncedToCloud: Boolean = false,
    val likesCount: Int = 0,
    val feelingTag: String = "🔥 Kuat",
    val shoeName: String = "Nike Alphafly 3",
    val weatherCondition: String = "Cerah 28°C"
) {
    fun toDomain(
        routePoints: List<GPSPoint>,
        splits: List<KmSplit>
    ): RunActivity {
        return RunActivity(
            id = id,
            title = title,
            timestamp = timestamp,
            durationSeconds = durationSeconds,
            distanceMeters = distanceMeters,
            avgPaceSecondsPerKm = avgPaceSecondsPerKm,
            maxPaceSecondsPerKm = maxPaceSecondsPerKm,
            caloriesBurned = caloriesBurned,
            elevationGainMeters = elevationGainMeters,
            avgCadenceSpm = avgCadenceSpm,
            avgHeartRateBpm = avgHeartRateBpm,
            routePoints = routePoints,
            splits = splits,
            aiAnalysis = aiAnalysis,
            isSyncedToCloud = isSyncedToCloud,
            likesCount = likesCount,
            feelingTag = feelingTag,
            shoeName = shoeName,
            weatherCondition = weatherCondition
        )
    }

    companion object {
        fun fromDomain(
            domain: RunActivity,
            routePointsJson: String,
            splitsJson: String
        ): RunEntity {
            return RunEntity(
                id = domain.id,
                title = domain.title,
                timestamp = domain.timestamp,
                durationSeconds = domain.durationSeconds,
                distanceMeters = domain.distanceMeters,
                avgPaceSecondsPerKm = domain.avgPaceSecondsPerKm,
                maxPaceSecondsPerKm = domain.maxPaceSecondsPerKm,
                caloriesBurned = domain.caloriesBurned,
                elevationGainMeters = domain.elevationGainMeters,
                avgCadenceSpm = domain.avgCadenceSpm,
                avgHeartRateBpm = domain.avgHeartRateBpm,
                routePointsJson = routePointsJson,
                splitsJson = splitsJson,
                aiAnalysis = domain.aiAnalysis,
                isSyncedToCloud = domain.isSyncedToCloud,
                likesCount = domain.likesCount,
                feelingTag = domain.feelingTag,
                shoeName = domain.shoeName,
                weatherCondition = domain.weatherCondition
            )
        }
    }
}
