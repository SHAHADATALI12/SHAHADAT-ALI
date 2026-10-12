package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.io.File
import java.util.Locale

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val usedRamMb: Long,
    val totalRamMb: Long,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val osVersion: String,
    val deviceModel: String,
    val isNetworkConnected: Boolean,
    val userAge: Int? = null
)

sealed class CommandExecutionResult {
    data class Handled(
        val responseText: String,
        val openIntentSuccess: Boolean = true,
        val updatedAge: Int? = null
    ) : CommandExecutionResult()
    data class ForwardToAi(val prompt: String, val detectedAge: Int? = null) : CommandExecutionResult()
}

class DeviceAutomationController(private val context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_biometrics", Context.MODE_PRIVATE)
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var torchEnabled = false

    fun getUserAge(): Int? {
        val age = prefs.getInt("user_age", -1)
        return if (age in 1..125) age else null
    }

    fun setUserAge(age: Int) {
        prefs.edit().putInt("user_age", age).apply()
    }

    fun getDeviceTelemetry(): DeviceTelemetry {
        // Battery
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPercent = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val status = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } else {
            false
        }

        // RAM
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamMb = (memoryInfo.totalMem / (1024 * 1024))
        val availRamMb = (memoryInfo.availMem / (1024 * 1024))
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)

        // Storage
        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong
        val totalStorageGb = "%.1f".format(Locale.US, (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)).toDoubleOrNull() ?: 64.0
        val freeStorageGb = "%.1f".format(Locale.US, (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)).toDoubleOrNull() ?: 24.0

        // Network
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connMgr?.activeNetwork
        val caps = connMgr?.getNetworkCapabilities(activeNetwork)
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

        return DeviceTelemetry(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            usedRamMb = totalRamMb.takeIf { it > 0 }?.let { usedRamMb } ?: 4096,
            totalRamMb = totalRamMb.takeIf { it > 0 } ?: 8192,
            freeStorageGb = freeStorageGb,
            totalStorageGb = totalStorageGb,
            osVersion = osVersion,
            deviceModel = deviceModel,
            isNetworkConnected = isConnected,
            userAge = getUserAge()
        )
    }

    fun setTorch(enable: Boolean): Boolean {
        return try {
            val camManager = cameraManager ?: return false
            val cameraId = camManager.cameraIdList.firstOrNull { id ->
                val chars = camManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: camManager.cameraIdList.firstOrNull() ?: return false

            camManager.setTorchMode(cameraId, enable)
            torchEnabled = enable
            true
        } catch (e: Exception) {
            torchEnabled = false
            false
        }
    }

    fun isTorchOn(): Boolean = torchEnabled

    fun triggerHaptic(durationMs: Long = 60) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    fun launchCamera(): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchYouTube(query: String? = null): Boolean {
        return try {
            val uri = if (query.isNullOrBlank()) {
                Uri.parse("https://www.youtube.com")
            } else {
                Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchWhatsApp(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchMaps(query: String = ""): Boolean {
        return try {
            val uri = if (query.isBlank()) {
                Uri.parse("geo:0,0?q=")
            } else {
                Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchAlarmOrClock(): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_DATE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun launchSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchWebSearch(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    /**
     * Accurately extracts user age from various English and Hindi/Hinglish phrasing.
     */
    fun extractAgeFromText(rawInput: String): Int? {
        val input = rawInput.trim().lowercase(Locale.ROOT)

        // 1. "I am 25 years old", "I am 25", "I'm 24 yrs old", "I'm 24"
        val p1 = Regex("""(?:i\s*am|i'm|im)\s+(\d{1,3})(?:\s*(?:years|year|yrs|yr)\s*old|\s*(?:years|year|yrs|yr)|\s*$)""")
        p1.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 2. "My age is 28", "Age is 28", "Age: 28", "My age: 28"
        val p2 = Regex("""(?:my\s+)?age\s*(?:is|:|=|\s)\s*(\d{1,3})""")
        p2.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 3. "Meri age 22 hai", "Meri umar 22 saal hai", "Mera age 22", "Meri aayu 22 hai"
        val p3 = Regex("""(?:meri|mera)\s*(?:age|umar|aayu|umra)\s*(?:is|hai|:|=|\s)?\s*(\d{1,3})""")
        p3.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 4. "Main 20 saal ka hoon", "Mai 20 saal ki hu", "Main 20 varsh ka hu"
        val p4 = Regex("""(?:main|mai|hum)\s*(\d{1,3})\s*(?:saal|sal|years|year|varsh)""")
        p4.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 5. "Set my age to 25", "Update age to 25", "Change age to 25", "Age 25"
        val p5 = Regex("""(?:set|update|change)\s*(?:my\s*)?age\s*(?:to\s*)?(\d{1,3})""")
        p5.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 6. "25 years old", "25 yrs old", "25 saal"
        val p6 = Regex("""\b(\d{1,3})\s*(?:years\s*old|year\s*old|yrs\s*old|saal|varsh)\b""")
        p6.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        // 7. Direct "age 25" or exact number alone if between 5 and 100
        val p7 = Regex("""^age\s*(\d{1,3})$""")
        p7.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { if (it in 1..120) return it }

        return null
    }

    /**
     * Resolves local hardware/app commands instantly without network delay.
     * Accurately captures user's age and answers age inquiries.
     * If not a direct device action, forwards to AI.
     */
    fun parseAndExecuteCommand(rawInput: String): CommandExecutionResult {
        val input = rawInput.trim().lowercase(Locale.ROOT)
        val isHindi = rawInput.any { it in '\u0900'..'\u097F' } ||
                input.contains("meri") || input.contains("umar") || input.contains("kya") || input.contains("batao")

        // 1. Age Query (e.g. "How old am I?", "What is my age?", "Meri age kya hai?")
        if (input.contains("how old am i") || input.contains("what is my age") || input.contains("what's my age") ||
            input.contains("my age?") || input.contains("meri age kya hai") || input.contains("meri umar") ||
            input.contains("meri aayu") || input.contains("do you know my age") || input.contains("batao meri age")
        ) {
            val savedAge = getUserAge()
            return if (savedAge != null) {
                val msg = if (isHindi) {
                    "बायोमेट्रिक टेलीमेट्री के अनुसार, सर, आपकी उम्र $savedAge वर्ष है। सभी प्रोटोकॉल आपकी प्रोफ़ाइल के अनुसार अनुकूलित हैं।"
                } else {
                    "According to your biometric telemetry, Sir, you are $savedAge years old. All neural protocols and cognitive parameters are calibrated for your profile."
                }
                CommandExecutionResult.Handled(msg, updatedAge = savedAge)
            } else {
                val msg = if (isHindi) {
                    "सर, अभी आपकी उम्र न्यूरल सिस्टम में दर्ज नहीं है। कृपया अपनी उम्र बताएं (जैसे 'मैं 25 साल का हूँ' या 'My age is 25'), ताकि मैं आपकी प्रोफ़ाइल कैलिब्रेट कर सकूँ।"
                } else {
                    "Sir, I do not yet have your age registered in the neural telemetry. Please state your age (for example, 'I am 25 years old') to calibrate your Mr Shahadat profile."
                }
                CommandExecutionResult.Handled(msg)
            }
        }

        // 2. User explicitly states/sets their age
        val detectedAge = extractAgeFromText(rawInput)
        if (detectedAge != null) {
            setUserAge(detectedAge)
            val msg = if (isHindi) {
                "बायोमेट्रिक प्रोफ़ाइल सफलतापूर्वक अपडेट हो गई, सर! आपकी उम्र $detectedAge वर्ष दर्ज कर ली गई है। शाहदत असिस्टेंट न्यूरल सिस्टम अब आपकी उम्र के अनुकूल कैलिब्रेटेड है।"
            } else {
                "Biometric profile confirmed, Sir. Operator age registered as $detectedAge years old. All neural systems and conversational telemetry in Mr Shahadat are now calibrated for your profile."
            }
            return CommandExecutionResult.Handled(msg, updatedAge = detectedAge)
        }

        // Torch / Flashlight ON
        if (input.contains("torch on") || input.contains("light on") || input.contains("flashlight on") ||
            input.contains("टॉर्च ऑन") || input.contains("टॉर्च चालू") || input.contains("लाइट चालू") || input.contains("लाइट जलाओ")
        ) {
            val ok = setTorch(true)
            val msg = if (ok) "Flashlight activated, Sir. Illumination levels at maximum."
            else "Illumination hardware triggered."
            return CommandExecutionResult.Handled(msg)
        }

        // Torch / Flashlight OFF
        if (input.contains("torch off") || input.contains("light off") || input.contains("flashlight off") ||
            input.contains("टॉर्च ऑफ") || input.contains("टॉर्च बंद") || input.contains("लाइट बंद")
        ) {
            setTorch(false)
            return CommandExecutionResult.Handled("Flashlight disabled, Sir.")
        }

        // Battery Status
        if (input.contains("battery") || input.contains("charge") || input.contains("बैटरी") || input.contains("चार्ज")) {
            val telemetry = getDeviceTelemetry()
            val stateText = if (telemetry.isCharging) "charging power supply connected" else "running on internal arc cells"
            val msg = "Power cells are at ${telemetry.batteryPercent}%, Sir. System is currently $stateText."
            return CommandExecutionResult.Handled(msg)
        }

        // Camera / Optics
        if (input.contains("camera") || input.contains("photo") || input.contains("कैमरा") || input.contains("फोटो") || input.contains("optics")) {
            val success = launchCamera()
            val msg = if (success) "Optics engaged. Launching primary camera sensors, Sir."
            else "Camera sensor launch requested, Sir."
            return CommandExecutionResult.Handled(msg, success)
        }

        // YouTube
        if (input.contains("youtube") || input.contains("यूट्यूब")) {
            val query = input.replace("open youtube", "").replace("youtube kholo", "").replace("यूट्यूब खोलो", "").trim()
            val success = launchYouTube(query.takeIf { it.isNotBlank() })
            return CommandExecutionResult.Handled("Accessing YouTube media protocols, Sir.", success)
        }

        // WhatsApp
        if (input.contains("whatsapp") || input.contains("व्हाट्सएप") || input.contains("व्हाट्सएप्प")) {
            val success = launchWhatsApp()
            return CommandExecutionResult.Handled("Initializing encrypted WhatsApp link, Sir.", success)
        }

        // Maps / Navigation
        if (input.contains("maps") || input.contains("navigation") || input.contains("map") || input.contains("गूगल मैप्स") || input.contains("रास्ता")) {
            val query = input.replace("open maps", "").replace("maps kholo", "").replace("navigation kholo", "").trim()
            val success = launchMaps(query)
            return CommandExecutionResult.Handled("Satellite positioning engaged. Deploying Maps, Sir.", success)
        }

        // Alarm / Clock
        if (input.contains("alarm") || input.contains("clock") || input.contains("timer") || input.contains("अलार्म") || input.contains("घड़ी")) {
            val success = launchAlarmOrClock()
            return CommandExecutionResult.Handled("Opening chronometer and alarm schedules, Sir.", success)
        }

        // Settings
        if (input.contains("setting") || input.contains("settings") || input.contains("सेटिंग्स") || input.contains("सेटिंग")) {
            val success = launchSettings()
            return CommandExecutionResult.Handled("Accessing Android core configuration panel, Sir.", success)
        }

        // Islamic Greeting
        if (input.contains("assalamu alaikum") || input.contains("salam") || input.contains("salaam") || input.contains("अस्सलाम")) {
            return CommandExecutionResult.Handled("Walaikum Assalam wa Rahmatullahi wa Barakatuh, Shahadat Ali Bhai! Shahadat Assistant is online and ready for your orders.")
        }

        // Owner & Creator Identity
        if (input.contains("who is your owner") || input.contains("tumhara owner") || input.contains("tumhara malik") ||
            input.contains("who created you") || input.contains("kisne banaya") || input.contains("shahadat ali")
        ) {
            return CommandExecutionResult.Handled("Assalamu Alaikum! My honorable owner and creator is Shahadat Ali Bhai. I am Shahadat Assistant, your personal and loyal AI assistant in Mr Shahadat.")
        }

        // Assistant Identity
        if (input.contains("who are you") || input.contains("tum kaun ho") || input.contains("apna naam") || input.contains("your name")) {
            return CommandExecutionResult.Handled("Assalamu Alaikum! I am Shahadat Assistant from Mr Shahadat, built exclusively for my respected owner, Shahadat Ali Bhai. How may I serve you today?")
        }
        if (input.contains("how are you") || input.contains("kya haal hai") || input.contains("क्या हाल है")) {
            return CommandExecutionResult.Handled("Alhamdulillah, Shahadat Ali Bhai! Always at 100% capacity and ready to assist you with any command.")
        }

        // Stark Protocol Mark 85 / Diagnostics
        if (input.contains("mark 85") || input.contains("protocol") || input.contains("diagnostic") || input.contains("डायग्नोस्टिक") || input.contains("सिस्टम चेक")) {
            val t = getDeviceTelemetry()
            val netStr = if (t.isNetworkConnected) "ONLINE" else "LOCAL OFFLINE"
            val ageStr = t.userAge?.let { "$it YRS" } ?: "UNREGISTERED"
            val msg = "Protocol Mark 85 executed, Shahadat Ali Bhai. Neural Core: SHAHADAT ASSISTANT (MR SHAHADAT) ACTIVE. Owner: Shahadat Ali (Age: $ageStr). Battery: ${t.batteryPercent}%. RAM: ${t.usedRamMb}/${t.totalRamMb} MB. Subsystems operational."
            return CommandExecutionResult.Handled(msg)
        }

        // Web Search
        if (input.startsWith("search ") || input.startsWith("google ") || input.startsWith("खोजो ")) {
            val q = input.removePrefix("search ").removePrefix("google ").removePrefix("खोजो ").trim()
            launchWebSearch(q)
            return CommandExecutionResult.Handled("Scanning global web archives for '$q', Sir.")
        }

        // Forward to Gemini AI Brain
        return CommandExecutionResult.ForwardToAi(rawInput, detectedAge = null)
    }
}
