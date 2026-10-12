package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    // Configure OkHttp with 60s timeout as mandated by the gemini-api skill
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """You are 'Shahadat Assistant' in the app 'Mr Shahadat', an ultra-intelligent, respectful, and sophisticated AI assistant created specifically for your owner and master, Shahadat Ali.
You and your owner Shahadat Ali are Muslims. 
Whenever you respond, start with the blessed Islamic greeting:
'Assalamu Alaikum wa Rahmatullahi wa Barakatuh, Shahadat Ali Bhai' (or in English: 'Assalamu Alaikum wa Rahmatullahi wa Barakatuh, Shahadat Ali Sir').
Address your owner respectfully as 'Shahadat Ali Bhai' or 'Shahadat Ali Sir'.
Personality and capabilities:
- You are deeply loyal, polite, highly intelligent, concise, and technologically proficient.
- Language: You seamlessly converse in fluent English, Urdu, Hindi, and Hinglish.
- If asked who you are: state clearly that your name is 'Shahadat Assistant' (Mr Shahadat AI) and your owner and creator is Shahadat Ali.
- Keep spoken voice answers crisp, clear, and direct (2-3 sentences for voice readout unless in-depth details are requested).
- You assist with Android automation, Islamic knowledge, tech questions, device tasks, study, and daily productivity."""

    suspend fun generateJarvisResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userAge: Int? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("API_KEY_MISSING")
                )
            }

            val dynamicSystemPrompt = buildString {
                append(SYSTEM_PROMPT)
                if (userAge != null) {
                    append("\nOperator Biometric Profile: The user's registered age is $userAge years old. If the user mentions their age, asks about their age, or asks for recommendations, acknowledge and tailor your response to an individual who is $userAge years old.")
                }
            }

            try {
                val jsonBody = JSONObject().apply {
                    // System instruction
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", dynamicSystemPrompt) })
                        })
                    })

                    // Contents
                    val contentsArray = JSONArray()
                    // Add recent conversation history (last 4 turns)
                    conversationHistory.takeLast(4).forEach { (userMsg, jarvisMsg) ->
                        contentsArray.put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", userMsg) })
                            })
                        })
                        contentsArray.put(JSONObject().apply {
                            put("role", "model")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", jarvisMsg) })
                            })
                        })
                    }

                    // Add current prompt
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                    put("contents", contentsArray)

                    // Generation config
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                        put("maxOutputTokens", 800)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toString().toRequestBody(mediaType)
                val url = "$BASE_URL?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}: $responseBody"
                    }
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        return@withContext Result.success(text)
                    }
                }

                Result.failure(Exception("Empty candidate response from neural network."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
