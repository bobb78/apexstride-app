package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.RunActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiCoachApi {
    private const val MODEL_NAME = "gemini-3.1-pro-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Deep Run Telemetry Audit with Gemini 3.1 Pro (High Thinking Mode)
     */
    suspend fun analyzeRunTelemetry(run: RunActivity): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(generateOfflineCoachAnalysis(run))
        }

        val splitsSummary = if (run.splits.isNotEmpty()) {
            run.splits.joinToString("\n") {
                "- KM ${it.kmNumber}: Durasi ${it.formattedDuration}, Pace ${it.formattedPace}/km, Elevasi +${it.elevationGainMeters}m, HR ${it.avgHeartRateBpm} bpm"
            }
        } else {
            "Data split per-km tercatat rata-rata ${run.formattedPace}/km."
        }

        val prompt = """
            Kamu adalah "Apex Intelligence", AI Running Coach elit dan ahli fisiologi olahraga biomekanik.
            Tolong lakukan audit mendalam dengan mode High Thinking untuk sesi lari berikut:
            
            [DATA TELEMETRI AKTIVITAS]
            - Judul: ${run.title}
            - Total Jarak: ${run.formattedDistance} km
            - Total Durasi: ${run.formattedDuration}
            - Pace Rata-rata: ${run.formattedPace} /km
            - Kalori Terbakar: ${run.caloriesBurned} kcal
            - Elevasi Gain: ${run.elevationGainMeters} meter
            - Cadence Rata-rata: ${run.avgCadenceSpm} SPM (Langkah per menit)
            - Heart Rate Rata-rata: ${run.avgHeartRateBpm} BPM
            - Sepatu: ${run.shoeName}
            - Kondisi: ${run.weatherCondition}
            
            [DATA SPLITS PER KM]
            $splitsSummary
            
            [TUGAS COACHING]
            1. Analisis Pace Volatility & Ritme (apakah negative split, positive split, atau steady state).
            2. Evaluasi Biomekanik & Efisiensi Energi (Analisis Cadence ${run.avgCadenceSpm} SPM dan beban Heart Rate).
            3. Jendela Pemulihan (Recovery Window) & Hidrasi/Nutrisi pasca lari.
            4. 2 Tips Taktis untuk sesi lari berikutnya (misal latihan interval, strides, atau perbaikan postur).
            
            Format respon secara terstruktur, futuristik, energetik, dan berbobot dalam Bahasa Indonesia dengan poin-poin tebal yang mudah dibaca atlet.
        """.trimIndent()

        try {
            val responseText = executeGeminiHighThinking(apiKey, prompt)
            Result.success(responseText)
        } catch (e: Exception) {
            Result.success(generateOfflineCoachAnalysis(run))
        }
    }

    /**
     * Generate Custom Marathon/10K/5K Training Strategy with Gemini 3.1 Pro High Thinking
     */
    suspend fun generateCustomTrainingPlan(
        targetGoal: String,
        currentPace: String,
        weeklyMileageKm: Int,
        daysPerWeek: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getFallbackTrainingPlan(targetGoal))
        }

        val prompt = """
            Kamu adalah Head Coach di ApexStride.
            Gunakan Deep Reasoning & High Thinking untuk membuat "Master Training Blueprint" 7 Hari yang adaptif bagi atlet lari:
            
            - Target Ambisius: $targetGoal
            - Current Baseline Pace: $currentPace /km
            - Target Jarak Mingguan: $weeklyMileageKm km
            - Frekuensi Lari: $daysPerWeek hari/minggu
            
            Rancang jadwal 7 hari spesifik dengan:
            - Jenis sesi (Easy, Tempo/Threshold, VO2 Max Intervals, Long Run, Active Recovery)
            - Target pace zona dan durasi/jarak per hari
            - Strategi pemanasan dinamis & pendinginan
            - Catatan nutrisi carbo-loading & hidrasi elektrolit.
            
            Format rapi dengan emoji kinetik dan layout yang memukau dalam Bahasa Indonesia.
        """.trimIndent()

        try {
            val text = executeGeminiHighThinking(apiKey, prompt)
            Result.success(text)
        } catch (e: Exception) {
            Result.success(getFallbackTrainingPlan(targetGoal))
        }
    }

    /**
     * Ask Coach Chat with Gemini 3.1 Pro High Thinking
     */
    suspend fun askCoachChat(userQuery: String, chatHistoryContext: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success("Coach Apex siap! Berdasarkan metodologi latihan modern, kunci meningkatkan performa lari adalah konsistensi volume aerobik 80% (Zone 2) dan 20% latihan kualitas tinggi (Threshold & VO2 Max Interval). Jaga cadensimu di kisaran 170-180 SPM untuk meminimalisir impact benturan pada lutut.")
        }

        val prompt = """
            Kamu adalah Apex Coach, asisten pelatih lari pintar kelas dunia bertenaga AI.
            Konteks Sebelumnya:
            $chatHistoryContext
            
            Pertanyaan Pelari:
            $userQuery
            
            Jawablah dengan penalaran fisiologi lari mendalam, solutif, memotivasi, dan aplikatif dalam Bahasa Indonesia.
        """.trimIndent()

        try {
            val text = executeGeminiHighThinking(apiKey, prompt)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeGeminiHighThinking(apiKey: String, prompt: String): String {
        val jsonBody = JSONObject().apply {
            // Contents
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            put("contents", contentsArr)

            // GenerationConfig with High Thinking
            val genConfig = JSONObject()
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", "HIGH")
            genConfig.put("thinkingConfig", thinkingConfig)
            genConfig.put("temperature", 0.7)
            put("generationConfig", genConfig)

            // System Instruction
            val sysInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", "Kamu adalah Apex Intelligence, pelatih lari profesional & spesialis biomekanik atletik. Analisislah performa dengan penalaran mendalam (Deep Thinking) berlandaskan sains olahraga."))
            sysInstruction.put("parts", sysParts)
            put("systemInstruction", sysInstruction)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP Error: ${response.code} ${response.message}")
            }
            val resString = response.body?.string() ?: throw Exception("Empty response body")
            val resJson = JSONObject(resString)
            val candidates = resJson.optJSONArray("candidates") ?: throw Exception("No candidates returned")
            if (candidates.length() == 0) throw Exception("Empty candidates array")
            
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: throw Exception("No content in candidate")
            val parts = content.optJSONArray("parts") ?: throw Exception("No parts in candidate content")
            
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text", "")
                sb.append(text)
            }
            return sb.toString().trim()
        }
    }

    private fun generateOfflineCoachAnalysis(run: RunActivity): String {
        val cadenceAssessment = when {
            run.avgCadenceSpm >= 175 -> "Sangat optimal (175+ SPM)! Menandakan ground contact time singkat dan efisiensi mekanik tinggi."
            run.avgCadenceSpm >= 165 -> "Bagus (${run.avgCadenceSpm} SPM). Coba tingkatkan 5% lagi untuk mengurangi beban torsi sendi lutut."
            else -> "Cukup (${run.avgCadenceSpm} SPM). Tingkatkan frekuensi langkah kecil cepat untuk meminimalisir overstriding."
        }

        return """
            ⚡ **APEX INTELLIGENCE AUDIT**
            
            🎯 **Pace & Ritme Analitik:**
            Sesi lari sejauh **${run.formattedDistance} km** diselesaikan dengan pace rata-rata **${run.formattedPace} /km**. Distribusi tenaga terkontrol dengan baik di medan elevasi +${run.elevationGainMeters}m.
            
            🏃‍♂️ **Evaluasi Biomekanik & Cadence:**
            - **Cadence Rata-rata:** ${run.avgCadenceSpm} SPM — $cadenceAssessment
            - **Intensitas Kardio:** Rata-rata ${run.avgHeartRateBpm} BPM (Zona Aerobik Terkontrol).
            
            🛡️ **Rekomendasi Recovery:**
            - Jendela pemulihan disarankan: **18 - 24 Jam**.
            - Hidrasi: Konsumsi 500-750ml air berelektrolit + asupan protein 20-30g dalam 45 menit pertama pasca lari.
            
            💡 **Tips Taktis Sesi Berikutnya:**
            Lakukan 4x100m lari akselerasi (strides) di akhir sesi easy run berikutnya untuk melatih motor neuron dan elastisitas tendon achilles!
        """.trimIndent()
    }

    private fun getFallbackTrainingPlan(targetGoal: String): String {
        return """
            📋 **APEX 7-DAY MASTER BLUEPRINT: $targetGoal**
            
            🔥 **Senin (Easy Recovery + Strides)**
            - Jarak: 5.0 KM @ Pace 6'15"/km (Zone 2)
            - 4x Strides 100m di rumput/lintasan datar.
            
            ⚡ **Selasa (Threshold / Tempo Intervals)**
            - Pemanasan: 1.5 KM Jogging
            - Main Set: 4 x 1000m @ Pace 4'45"/km (Rest 90s jog)
            - Pendinginan: 1.5 KM Easy Jog
            
            🧘 **Rabu (Cross Training / Core & Mobility)**
            - Istirahat aktif: 30 menit plank, lunges, foam rolling.
            
            🚀 **Kamis (Aerobic Endurance Builder)**
            - Jarak: 7.0 KM @ Pace 5'30"/km stabil.
            
            🛑 **Jumat (Total Rest Day)**
            - Istirahat total & carbo refueling.
            
            🏆 **Sabtu (Apex Progressive Long Run)**
            - Jarak: 14.0 KM
            - 10 KM pertama @ Pace 5'50"/km, 4 KM terakhir @ Race Pace 5'15"/km.
            
            🌱 **Minggu (Shakeout Run & Recovery)**
            - Jarak: 3.0 KM @ Pace santai + stretching statis.
        """.trimIndent()
    }
}
