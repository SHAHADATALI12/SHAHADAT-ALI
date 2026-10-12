package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNeonGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariantDark
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisUiState

@Composable
fun DiagnosticsScreen(
    state: JarvisUiState,
    onRefreshTelemetry: () -> Unit,
    onUpdateVoiceSettings: (Float, Float, String) -> Unit,
    onTestVoice: (String) -> Unit,
    onToggleTorch: () -> Unit,
    onSetAge: (Int) -> Unit = {}
) {
    var pitch by remember(state.pitch) { mutableFloatStateOf(state.pitch) }
    var rate by remember(state.speechRate) { mutableFloatStateOf(state.speechRate) }
    var selectedLang by remember(state.preferredLanguage) { mutableStateOf(state.preferredLanguage) }
    var ageInput by remember(state.userAge) { mutableStateOf(state.userAge?.toString() ?: "25") }

    val languages = listOf(
        "auto" to "Auto (ऑटो)",
        "hi" to "Hindi (हिंदी)",
        "en" to "English (UK)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBgDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = 700.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "STARK HARDWARE & VOICE LAB",
                                color = JarvisCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "हार्डवेयर डायग्नोस्टिक्स एवं वॉयस कैलिब्रेशन",
                                color = JarvisTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onRefreshTelemetry,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariantDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = JarvisCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", color = JarvisCyan, fontSize = 11.sp)
                    }
                }
            }

            // 1. Live Hardware Telemetry HUD Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, JarvisBorderCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = "Telemetry", tint = JarvisGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE SYSTEM TELEMETRY",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Battery Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Arc Reactor Power (Battery):", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = "${state.telemetry.batteryPercent}% (${if (state.telemetry.isCharging) "CHARGING" else "DISCHARGING"})",
                                color = if (state.telemetry.batteryPercent > 20) JarvisNeonGreen else JarvisCrimson,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { state.telemetry.batteryPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (state.telemetry.batteryPercent > 20) JarvisNeonGreen else JarvisCrimson,
                            trackColor = JarvisSurfaceVariantDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // RAM Section
                        val ramFraction = if (state.telemetry.totalRamMb > 0) {
                            (state.telemetry.usedRamMb.toFloat() / state.telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)
                        } else 0.5f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Memory Architecture (RAM):", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = "${state.telemetry.usedRamMb} MB / ${state.telemetry.totalRamMb} MB",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ramFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JarvisCyan,
                            trackColor = JarvisSurfaceVariantDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Storage Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Internal Core Storage:", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = "${state.telemetry.freeStorageGb} GB Free / ${state.telemetry.totalStorageGb} GB",
                                color = JarvisGold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Device Details Grid
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisSurfaceVariantDark)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Device Unit: ${state.telemetry.deviceModel}",
                                    color = JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "OS Core: ${state.telemetry.osVersion}",
                                    color = JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Network Telemetry: ${if (state.telemetry.isNetworkConnected) "ONLINE (SYNCED)" else "LOCAL ONLY"}",
                                    color = if (state.telemetry.isNetworkConnected) JarvisNeonGreen else JarvisCrimson,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Flashlight Test Button
                        Button(
                            onClick = onToggleTorch,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isTorchOn) JarvisGold else JarvisSurfaceVariantDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FlashlightOn,
                                contentDescription = "Test Flashlight",
                                tint = if (state.isTorchOn) JarvisBgDark else JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isTorchOn) "Flashlight ACTIVE (Tap to turn OFF)" else "Test Flashlight Hardware (Turn ON)",
                                color = if (state.isTorchOn) JarvisBgDark else JarvisCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 1b. Operator Biometrics & Age Calibration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, JarvisBorderCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "Operator", tint = JarvisNeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OPERATOR BIOMETRICS (उम्र कैलिब्रेशन)",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Registered Operator Age:", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = if (state.userAge != null) "${state.userAge} Years Old" else "Not Registered",
                                color = if (state.userAge != null) JarvisNeonGreen else JarvisGold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = ageInput,
                                onValueChange = { ageInput = it.filter { c -> c.isDigit() }.take(3) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("operator_age_input_field"),
                                label = { Text("Set Age (1-120)", color = JarvisCyan, fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisBorderCyan,
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    cursorColor = JarvisCyan
                                )
                            )

                            Button(
                                onClick = {
                                    val parsed = ageInput.toIntOrNull()
                                    if (parsed != null && parsed in 1..120) {
                                        onSetAge(parsed)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("save_age_button")
                            ) {
                                Text("Update", color = JarvisBgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick age chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(18, 21, 25, 30, 40).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JarvisSurfaceVariantDark)
                                        .border(1.dp, JarvisBorderCyan, RoundedCornerShape(6.dp))
                                        .clickable {
                                            ageInput = preset.toString()
                                            onSetAge(preset)
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$preset yrs", color = JarvisGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Iron Man Voice & Speech Tuning Studio
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, JarvisBorderCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SettingsVoice, contentDescription = "Voice Tuning", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JARVIS VOICE CALIBRATION (आवाज़ ट्यूनर)",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Language Preference
                        Text(
                            text = "भाषा प्राथमिकता (Preferred Speech Language):",
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            languages.forEach { (langCode, langName) ->
                                val selected = selectedLang == langCode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) JarvisCyan else JarvisSurfaceVariantDark)
                                        .clickable {
                                            selectedLang = langCode
                                            onUpdateVoiceSettings(pitch, rate, langCode)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = langName,
                                        color = if (selected) JarvisBgDark else JarvisTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pitch Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Voice Pitch (गंभीरता):", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = "%.2f".format(pitch),
                                color = JarvisGold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = pitch,
                            onValueChange = {
                                pitch = it
                                onUpdateVoiceSettings(pitch, rate, selectedLang)
                            },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisGold,
                                activeTrackColor = JarvisGold,
                                inactiveTrackColor = JarvisSurfaceVariantDark
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rate Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Speech Rate (बोलने की गति):", color = JarvisTextPrimary, fontSize = 12.sp)
                            Text(
                                text = "%.2f".format(rate),
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = rate,
                            onValueChange = {
                                rate = it
                                onUpdateVoiceSettings(pitch, rate, selectedLang)
                            },
                            valueRange = 0.6f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisSurfaceVariantDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Test Voice Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTestVoice("System online, Sir. Arc reactor voice synthesizer is calibrated to optimum Stark frequency.") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyanDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Test EN", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test English", color = Color.White, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onTestVoice("नमस्ते बॉस! जार्विस वॉयस सिस्टम पूरी तरह एक्टिव है और आपकी आज्ञा के लिए तैयार है।") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariantDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Test HI", tint = JarvisGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Hindi", color = JarvisGold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
