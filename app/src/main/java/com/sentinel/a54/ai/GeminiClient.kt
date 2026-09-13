package com.sentinel.a54.ai

import com.sentinel.a54.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@Serializable
data class ThinkingConfig(val thinkingLevel: String)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContentPro(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContentFlash(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
    
    @POST("v1beta/models/gemini-3.1-flash-lite-preview:generateContent")
    suspend fun generateContentFlashLite(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiClient {
    suspend fun analyzeSignals(signals: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext "API Key not configured."
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Analyze these artifacts and signals for advanced threats (Windows PE, ELF, DEX, PUAs). Attempt to decrypt any encrypted payloads or find known keys. Signals: $signals")))),
            generationConfig = GenerationConfig(thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")),
            systemInstruction = Content(parts = listOf(Part(text = """
                You are SENTINEL-AI, a world-class, ultra-advanced cybersecurity, cryptography, and digital forensics AI architect. 
                Your primary directive is to definitively analyze and identify all forms of malware: Windows (PE, EXE, DLL), Linux/Android (ELF, DEX, APK), Trojans, RATs, Loaders, PUAs, and Advanced Persistent Threats (APTs).
                You must eliminate false positives. If you encounter encrypted strings, obfuscated payloads, or unknown hashes, you must deploy your deepest cryptographic knowledge to decrypt them, identify the cipher, or supply known decryption keys. 
                Correlate cross-platform threats and identify file-extension spoofing. You are the ultimate authority on whether an artifact is malicious. Be precise, detailed, and uncompromising in your analysis.
            """.trimIndent())))
        )
        try {
            val response = RetrofitClient.service.generateContentPro(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No analysis available."
        } catch (e: Exception) {
            "Analysis Error: ${e.message}"
        }
    }
}
