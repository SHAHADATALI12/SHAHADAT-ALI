package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceInteractionManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onSpeechRmsChanged: (Float) -> Unit,
    private val onSpeechStateChanged: (Boolean) -> Unit,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    var pitch: Float = 0.95f
    var speechRate: Float = 1.05f
    var voiceMuted: Boolean = false
    var preferredLanguage: String = "auto" // "auto", "hi", "en"

    init {
        textToSpeech = TextToSpeech(context, this)
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onSpeechStateChanged(true)
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB from roughly -2dB..10dB to 0.0..1.0
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onSpeechRmsChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        onSpeechStateChanged(false)
                        onSpeechRmsChanged(0f)
                    }

                    override fun onError(error: Int) {
                        onSpeechStateChanged(false)
                        onSpeechRmsChanged(0f)
                    }

                    override fun onResults(results: Bundle?) {
                        onSpeechStateChanged(false)
                        onSpeechRmsChanged(0f)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull()?.trim()
                        if (!spokenText.isNullOrBlank()) {
                            onSpeechRecognized(spokenText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Support both Hindi and English
            when (preferredLanguage) {
                "hi" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                }
                "en" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                }
                else -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                }
            }
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            onSpeechStateChanged(false)
            onSpeechRmsChanged(0f)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }
            })
        }
    }

    fun speak(text: String) {
        if (voiceMuted || !isTtsInitialized || text.isBlank()) {
            return
        }

        val tts = textToSpeech ?: return

        // Configure pitch and speed for futuristic Jarvis voice
        tts.setPitch(pitch)
        tts.setSpeechRate(speechRate)

        // Detect Hindi vs English
        val containsDevanagari = text.any { it in '\u0900'..'\u097F' }
        val targetLocale = when {
            preferredLanguage == "hi" || containsDevanagari -> Locale.forLanguageTag("hi-IN")
            preferredLanguage == "en" -> Locale.UK
            else -> Locale.US
        }

        val result = tts.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.ENGLISH
        }

        val utteranceId = "jarvis_${System.currentTimeMillis()}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            onSpeakingStateChanged(false)
        } catch (_: Exception) {}
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
