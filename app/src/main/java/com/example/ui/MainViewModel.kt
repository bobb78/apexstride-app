package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChallengeItem
import com.example.data.model.CommunityPost
import com.example.data.model.CommunityRoute
import com.example.data.model.PerformanceMetrics
import com.example.data.model.RunActivity
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseAuthManager
import com.example.data.remote.GeminiCoachApi
import com.example.data.repository.RunRepository
import com.example.service.LiveRunTelemetry
import com.example.service.LiveRunTracker
import com.example.service.RunTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination {
    HOME,
    LIVE_RUN,
    RUN_SUMMARY,
    ANALYTICS,
    ROUTES,
    COACH,
    COMMUNITY,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val runRepository = RunRepository(application)
    private val authManager = FirebaseAuthManager(application)
    private val liveRunTracker = LiveRunTracker(application)

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Runs list from Room database
    val allRuns: StateFlow<List<RunActivity>> = runRepository.allRunsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Performance Metrics derived dynamically from all runs
    val performanceMetrics: StateFlow<PerformanceMetrics> = allRuns.map { runs ->
        runRepository.computePerformanceMetrics(runs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerformanceMetrics())

    // Community feed, challenges, and routes
    val communityPosts: StateFlow<List<CommunityPost>> = runRepository.communityPosts
    val challenges: StateFlow<List<ChallengeItem>> = runRepository.challenges
    val communityRoutes: StateFlow<List<CommunityRoute>> = runRepository.communityRoutes

    // User profile and Auth
    val userProfile: StateFlow<UserProfile> = authManager.currentUserProfile
    val isSignedIn: StateFlow<Boolean> = authManager.isSignedIn

    // Live Run Telemetry
    val liveTelemetry: StateFlow<LiveRunTelemetry> = liveRunTracker.telemetry

    // Current viewed / completed run for summary screen
    private val _summaryRun = MutableStateFlow<RunActivity?>(null)
    val summaryRun: StateFlow<RunActivity?> = _summaryRun.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi.asStateFlow()

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun startRun(isSimulation: Boolean) {
        liveRunTracker.startRun(isSimulation = isSimulation)
        _currentDestination.value = AppDestination.LIVE_RUN
    }

    fun startRouteRun(route: CommunityRoute) {
        // Start run configured with this community route waypoint guidance
        liveRunTracker.startRun(isSimulation = true)
        _currentDestination.value = AppDestination.LIVE_RUN
    }

    fun pauseRun() {
        liveRunTracker.pauseRun()
    }

    fun resumeRun() {
        liveRunTracker.resumeRun()
    }

    fun toggleVoiceCoach() {
        liveRunTracker.toggleVoiceCoach()
    }

    fun finishRun() {
        val completedRun = liveRunTracker.finishRun()
        _summaryRun.value = completedRun
        _currentDestination.value = AppDestination.RUN_SUMMARY

        // Automatically trigger Gemini 3.1 Pro High Thinking Deep Analysis
        generateAiAuditForSummary(completedRun)
    }

    fun viewRunDetail(runId: String) {
        viewModelScope.launch {
            val run = runRepository.getRunById(runId)
            if (run != null) {
                _summaryRun.value = run
                _currentDestination.value = AppDestination.RUN_SUMMARY
            }
        }
    }

    fun generateAiAuditForSummary(targetRun: RunActivity? = _summaryRun.value) {
        val run = targetRun ?: return
        _isLoadingAi.value = true
        viewModelScope.launch {
            val result = GeminiCoachApi.analyzeRunTelemetry(run)
            val analysisText = result.getOrNull()
            if (analysisText != null) {
                val updatedRun = run.copy(aiAnalysis = analysisText)
                _summaryRun.value = updatedRun
                runRepository.updateRunAiAnalysis(run.id, analysisText)
            }
            _isLoadingAi.value = false
        }
    }

    fun saveSummaryAndClose(title: String, feelingTag: String, shoeName: String) {
        val current = _summaryRun.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                title = title,
                feelingTag = feelingTag,
                shoeName = shoeName
            )
            runRepository.saveRun(updated, userProfile.value.uid)
            _summaryRun.value = null
            _currentDestination.value = AppDestination.HOME
        }
    }

    fun closeSummaryWithoutSaving() {
        val current = _summaryRun.value
        if (current != null) {
            viewModelScope.launch {
                runRepository.saveRun(current, userProfile.value.uid)
            }
        }
        _summaryRun.value = null
        _currentDestination.value = AppDestination.HOME
    }

    fun deleteRun(runId: String) {
        viewModelScope.launch {
            runRepository.deleteRun(runId)
            if (_summaryRun.value?.id == runId) {
                _summaryRun.value = null
                _currentDestination.value = AppDestination.HOME
            }
        }
    }

    fun toggleBoost(postId: String) {
        runRepository.toggleBoostPost(postId)
    }

    fun toggleJoinChallenge(challengeId: String) {
        runRepository.toggleJoinChallenge(challengeId)
    }

    fun toggleBookmarkRoute(routeId: String) {
        runRepository.toggleBookmarkRoute(routeId)
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
        runRepository.createCustomChallenge(
            title = title,
            subtitle = subtitle,
            targetKm = targetKm,
            daysLeft = daysLeft,
            category = category,
            rewardBadge = rewardBadge,
            rewardXp = rewardXp
        )
    }

    fun cheerParticipant(challengeId: String, userId: String) {
        runRepository.cheerParticipant(challengeId, userId)
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    fun updateWeeklyGoal(newGoal: Double) {
        authManager.updateWeeklyGoal(newGoal)
    }

    override fun onCleared() {
        super.onCleared()
        liveRunTracker.release()
    }
}
