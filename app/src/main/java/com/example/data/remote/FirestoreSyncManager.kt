package com.example.data.remote

import com.example.data.local.RunJsonConverter
import com.example.data.model.CommunityPost
import com.example.data.model.RunActivity
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun syncRunToFirestore(userId: String, run: RunActivity): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val runMap = hashMapOf(
                "id" to run.id,
                "userId" to userId,
                "title" to run.title,
                "timestamp" to run.timestamp,
                "durationSeconds" to run.durationSeconds,
                "distanceMeters" to run.distanceMeters,
                "avgPaceSecondsPerKm" to run.avgPaceSecondsPerKm,
                "caloriesBurned" to run.caloriesBurned,
                "elevationGainMeters" to run.elevationGainMeters,
                "avgCadenceSpm" to run.avgCadenceSpm,
                "avgHeartRateBpm" to run.avgHeartRateBpm,
                "routePointsJson" to RunJsonConverter.routePointsToJson(run.routePoints),
                "splitsJson" to RunJsonConverter.splitsToJson(run.splits),
                "aiAnalysis" to run.aiAnalysis,
                "feelingTag" to run.feelingTag,
                "shoeName" to run.shoeName,
                "weatherCondition" to run.weatherCondition
            )

            // Save in user subcollection
            firestore.collection("users")
                .document(userId)
                .collection("runs")
                .document(run.id)
                .set(runMap)
                .await()

            // Also post to public community feed
            firestore.collection("public_feed")
                .document(run.id)
                .set(runMap)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            // Local persistence remains intact in Room
            Result.failure(e)
        }
    }

    suspend fun fetchCommunityFeed(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("public_feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val posts = mutableListOf<CommunityPost>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Morning Speed Stride"
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val duration = doc.getLong("durationSeconds") ?: 1800L
                val distance = doc.getDouble("distanceMeters") ?: 5000.0
                val pace = doc.getLong("avgPaceSecondsPerKm")?.toInt() ?: 360
                val calories = doc.getLong("caloriesBurned")?.toInt() ?: 350
                val elev = doc.getLong("elevationGainMeters")?.toInt() ?: 45
                val cadence = doc.getLong("avgCadenceSpm")?.toInt() ?: 170
                val hr = doc.getLong("avgHeartRateBpm")?.toInt() ?: 148
                val points = RunJsonConverter.jsonToRoutePoints(doc.getString("routePointsJson"))
                val splits = RunJsonConverter.jsonToSplits(doc.getString("splitsJson"))
                val aiAnalysis = doc.getString("aiAnalysis")

                val activity = RunActivity(
                    id = id,
                    title = title,
                    timestamp = timestamp,
                    durationSeconds = duration,
                    distanceMeters = distance,
                    avgPaceSecondsPerKm = pace,
                    caloriesBurned = calories,
                    elevationGainMeters = elev,
                    avgCadenceSpm = cadence,
                    avgHeartRateBpm = hr,
                    routePoints = points,
                    splits = splits,
                    aiAnalysis = aiAnalysis
                )

                posts.add(
                    CommunityPost(
                        id = id,
                        runnerName = doc.getString("userId") ?: "Apex Athlete",
                        activity = activity,
                        boostCount = 12,
                        commentCount = 3
                    )
                )
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
