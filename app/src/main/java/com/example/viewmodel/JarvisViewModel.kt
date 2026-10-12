package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiClient
import com.example.data.JarvisGuideRepository
import com.example.service.CommandExecutionResult
import com.example.service.DeviceAutomationController
import com.example.service.DeviceTelemetry
import com.example.service.VoiceInteractionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class JarvisScreen {
    CORE_ASSISTANT,
    SETUP_GUIDES,
    BLUEPRINTS,
    DIAGNOSTICS
}

enum class AssistantState {
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class JarvisUiState(
    val currentScreen: JarvisScreen = JarvisScreen.CORE_ASSISTANT,
    val assistantState: AssistantState = AssistantState.STANDBY,
    val liveRms: Float = 0f,
    val lastUserQuery: String = "",
    val currentJarvisResponse: String = "Assalamu Alaikum wa Rahmatullahi wa Barakatuh, Shahadat Ali Bhai! Shahadat Assistant is fully operational. How may I serve you?",
    val messages: List<ChatMessage> = listOf(
        ChatMessage(sender = "SHAHADAT ASSISTANT", text = "Bismillah ir-Rahman ir-Rahim. Assalamu Alaikum wa Rahmatullahi wa Barakatuh, Shahadat Ali Bhai! Shahadat Assistant neural core is online and ready in Mr Shahadat.")
    ),
    val isTorchOn: Boolean = false,
    val telemetry: DeviceTelemetry = DeviceTelemetry(
        batteryPercent = 85,
        isCharging = false,
        usedRamMb = 4096,
        totalRamMb = 8192,
        freeStorageGb = 24.5,
        totalStorageGb = 64.0,
        osVersion = "Android 14",
        deviceModel = "Stark Neural Unit",
        isNetworkConnected = true
    ),
    val pitch: Float = 0.95f,
    val speechRate: Float = 1.05f,
    val voiceMuted: Boolean = false,
    val preferredLanguage: String = "auto", // "auto", "hi", "en"
    val soundFxEnabled: Boolean = true,
    val userAge: Int? = null,
    // Guide state
    val selectedGuideCategory: String = "All",
    val guideSearchQuery: String = "",
    val expandedGuideId: String? = "guide_1",
    // Blueprint builder state
    val selectedTriggerIndex: Int = 0,
    val selectedActionIndex: Int = 0,
    val bannerMessage: String? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private val automationController = DeviceAutomationController(application)
    private var voiceManager: VoiceInteractionManager? = null
    private var processingJob: Job? = null

    init {
        voiceManager = VoiceInteractionManager(
            context = application,
            onSpeechRecognized = { recognizedText ->
                processUserCommand(recognizedText)
            },
            onSpeechRmsChanged = { rms ->
                _uiState.update { it.copy(liveRms = rms) }
            },
            onSpeechStateChanged = { isListening ->
                _uiState.update {
                    it.copy(
                        assistantState = if (isListening) AssistantState.LISTENING else AssistantState.STANDBY,
                        liveRms = if (isListening) it.liveRms else 0f
                    )
                }
            },
            onSpeakingStateChanged = { isSpeaking ->
                _uiState.update {
                    it.copy(
                        assistantState = if (isSpeaking) AssistantState.SPEAKING else AssistantState.STANDBY,
                        liveRms = if (isSpeaking) 0.6f else 0f
                    )
                }
            }
        )

        refreshTelemetry()
    }

    fun refreshTelemetry() {
        val t = automationController.getDeviceTelemetry()
        val torch = automationController.isTorchOn()
        _uiState.update { it.copy(telemetry = t, isTorchOn = torch, userAge = t.userAge) }
    }

    fun setUserAge(age: Int) {
        automationController.setUserAge(age)
        automationController.triggerHaptic(40)
        _uiState.update { it.copy(userAge = age) }
        val msg = "Biometric profile confirmed, Sir. Operator age registered as $age years old. Neural systems synchronized."
        speakJarvisResponse(msg)
        refreshTelemetry()
    }

    fun setScreen(screen: JarvisScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        automationController.triggerHaptic(25)
    }

    fun startListening() {
        automationController.triggerHaptic(40)
        _uiState.update { it.copy(assistantState = AssistantState.LISTENING) }
        voiceManager?.preferredLanguage = _uiState.value.preferredLanguage
        voiceManager?.startListening()
    }

    fun stopListening() {
        voiceManager?.stopListening()
        _uiState.update { it.copy(assistantState = AssistantState.STANDBY, liveRms = 0f) }
    }

    fun toggleListening() {
        if (_uiState.value.assistantState == AssistantState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun toggleTorch() {
        val newState = !_uiState.value.isTorchOn
        automationController.triggerHaptic(45)
        automationController.setTorch(newState)
        _uiState.update { it.copy(isTorchOn = automationController.isTorchOn()) }
        val msg = if (newState) "Illumination protocol online, Sir." else "Illumination disengaged, Sir."
        speakJarvisResponse(msg)
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.voiceMuted
        automationController.triggerHaptic(30)
        voiceManager?.voiceMuted = newMuted
        if (newMuted) {
            voiceManager?.stopSpeaking()
        }
        _uiState.update { it.copy(voiceMuted = newMuted) }
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, language: String) {
        voiceManager?.pitch = pitch
        voiceManager?.speechRate = rate
        voiceManager?.preferredLanguage = language
        _uiState.update {
            it.copy(pitch = pitch, speechRate = rate, preferredLanguage = language)
        }
    }

    fun testVoiceSynthesizer(sampleText: String) {
        automationController.triggerHaptic(30)
        speakJarvisResponse(sampleText)
    }

    fun processUserCommand(input: String) {
        if (input.isBlank()) return
        val userQuery = input.trim()

        automationController.triggerHaptic(35)

        // Append to chat history
        _uiState.update {
            it.copy(
                lastUserQuery = userQuery,
                messages = it.messages + ChatMessage(sender = "USER", text = userQuery),
                assistantState = AssistantState.PROCESSING
            )
        }

        // 1. Try local automation/hardware command resolution first
        val result = automationController.parseAndExecuteCommand(userQuery)
        when (result) {
            is CommandExecutionResult.Handled -> {
                if (result.updatedAge != null) {
                    _uiState.update { it.copy(userAge = result.updatedAge) }
                }
                speakJarvisResponse(result.responseText)
                refreshTelemetry()
            }
            is CommandExecutionResult.ForwardToAi -> {
                val detectedAge = automationController.extractAgeFromText(userQuery)
                if (detectedAge != null) {
                    automationController.setUserAge(detectedAge)
                    _uiState.update { it.copy(userAge = detectedAge) }
                }
                // 2. Query Gemini 3.5 Flash Brain
                queryGeminiAi(userQuery)
            }
        }
    }

    private fun queryGeminiAi(prompt: String) {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            _uiState.update { it.copy(assistantState = AssistantState.PROCESSING) }

            // Create conversation history tuples for contextual replies
            val history = _uiState.value.messages.takeLast(6).chunked(2).mapNotNull { chunk ->
                val user = chunk.firstOrNull { it.sender == "USER" }?.text
                val bot = chunk.firstOrNull { it.sender == "JARVIS" }?.text
                if (user != null && bot != null) user to bot else null
            }

            val currentAge = _uiState.value.userAge ?: automationController.getUserAge()
            val aiResult = GeminiClient.generateJarvisResponse(prompt, history, currentAge)
            aiResult.onSuccess { replyText ->
                speakJarvisResponse(replyText)
            }.onFailure { err ->
                val fallbackReply = if (err.message == "API_KEY_MISSING") {
                    "Sir, I have processed your request locally. To unlock unlimited neural reasoning with Gemini 3.5 Flash, add your GEMINI_API_KEY in the Secrets panel. All device automation protocols remain fully operational."
                } else {
                    "Protocol executed, Sir. Neural transmission noted: '${prompt.take(40)}'. Arc reactor and device subsystems are nominal."
                }
                speakJarvisResponse(fallbackReply)
            }
        }
    }

    private fun speakJarvisResponse(text: String) {
        _uiState.update {
            it.copy(
                currentJarvisResponse = text,
                messages = it.messages + ChatMessage(sender = "JARVIS", text = text),
                assistantState = if (!it.voiceMuted) AssistantState.SPEAKING else AssistantState.STANDBY
            )
        }
        voiceManager?.speak(text)
    }

    fun clearBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    fun showBanner(message: String) {
        _uiState.update { it.copy(bannerMessage = message) }
        viewModelScope.launch {
            delay(3500)
            _uiState.update { it.copy(bannerMessage = null) }
        }
    }

    // Guides
    fun setGuideCategory(cat: String) {
        _uiState.update { it.copy(selectedGuideCategory = cat) }
    }

    fun setGuideSearchQuery(query: String) {
        _uiState.update { it.copy(guideSearchQuery = query) }
    }

    fun toggleGuideExpanded(id: String) {
        _uiState.update {
            it.copy(expandedGuideId = if (it.expandedGuideId == id) null else id)
        }
    }

    // Blueprints
    fun selectBlueprintTrigger(index: Int) {
        _uiState.update { it.copy(selectedTriggerIndex = index) }
    }

    fun selectBlueprintAction(index: Int) {
        _uiState.update { it.copy(selectedActionIndex = index) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
