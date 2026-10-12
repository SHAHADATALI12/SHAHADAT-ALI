package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.AudioFrequencyWaveform
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNeonGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariantDark
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.AssistantState
import com.example.viewmodel.JarvisUiState

@Composable
fun AssistantScreen(
    state: JarvisUiState,
    onToggleListening: () -> Unit,
    onSendQuery: (String) -> Unit,
    onToggleTorch: () -> Unit,
    onToggleMute: () -> Unit,
    onRepeatSpeech: (String) -> Unit,
    onSetAge: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showAgeDialog by remember { mutableStateOf(false) }
    var ageDialogInput by remember { mutableStateOf(state.userAge?.toString() ?: "25") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleListening()
        }
    }

    fun handleMicClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onToggleListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val stateColor by animateColorAsState(
        targetValue = when (state.assistantState) {
            AssistantState.LISTENING -> JarvisNeonGreen
            AssistantState.PROCESSING -> JarvisGold
            AssistantState.SPEAKING -> JarvisCyan
            AssistantState.STANDBY -> JarvisCyan
        },
        label = "state_color"
    )

    val stateLabel = when (state.assistantState) {
        AssistantState.LISTENING -> "● LISTENING TO COMMAND (आज्ञा दीजिए)..."
        AssistantState.PROCESSING -> "▲ NEURAL PROCESSING..."
        AssistantState.SPEAKING -> "■ SPEAKING PROTOCOL..."
        AssistantState.STANDBY -> "◆ SHAHADAT ASSISTANT ONLINE"
    }

    val quickProtocols = listOf(
        "🤲 Assalamu Alaikum" to "Assalamu Alaikum",
        "👤 Owner Name?" to "Who is your owner?",
        "🎂 My Age?" to "What is my age?",
        "👤 Set Age 25" to "I am 25 years old",
        "⚡ Torch On/Off" to if (state.isTorchOn) "Torch off" else "Torch on",
        "🔋 Battery Status" to "Check battery status",
        "📷 Open Camera" to "Open camera",
        "🗺️ Maps" to "Open Google Maps",
        "🎵 YouTube" to "Open YouTube",
        "💬 WhatsApp" to "Open WhatsApp",
        "⏰ Alarms" to "Open Alarm Clock",
        "⚙️ Settings" to "Open Settings",
        "🧠 Protocol Mark 85" to "Protocol Mark 85"
    )

    // Age Input Dialog
    if (showAgeDialog) {
        AlertDialog(
            onDismissRequest = { showAgeDialog = false },
            containerColor = JarvisSurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPERATOR BIOMETRIC AGE",
                        color = JarvisCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter or calibrate your age. Shahadat Assistant will register this in neural telemetry and personalize all AI responses and protocols accordingly:",
                        color = JarvisTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ageDialogInput,
                        onValueChange = { ageDialogInput = it.filter { char -> char.isDigit() }.take(3) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("age_dialog_input"),
                        label = { Text("Your Age (वर्ष)", color = JarvisCyan, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = JarvisCyan
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Quick age presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(18, 21, 25, 30, 45).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisSurfaceVariantDark)
                                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(6.dp))
                                    .clickable { ageDialogInput = preset.toString() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "$preset", color = JarvisGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = ageDialogInput.toIntOrNull()
                        if (parsed != null && parsed in 1..120) {
                            onSetAge(parsed)
                            showAgeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Calibrate Age", color = JarvisBgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgeDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Stark Telemetry HUD Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp)
                    .border(1.dp, JarvisBorderCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // System online badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(JarvisNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MR SHAHADAT AI",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    // Battery Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = "Battery Status",
                            tint = if (state.telemetry.batteryPercent > 20) JarvisNeonGreen else JarvisCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.telemetry.batteryPercent}%",
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Action Toggles (Torch & Mute)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleTorch,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("torch_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Toggle Torch",
                                tint = if (state.isTorchOn) JarvisGold else JarvisTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mute_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.voiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Toggle Voice Mute",
                                tint = if (state.voiceMuted) JarvisCrimson else JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 1b. Operator Biometric Age Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (state.userAge != null) JarvisSurfaceVariantDark.copy(alpha = 0.8f) else JarvisSurfaceDark)
                    .border(
                        1.dp,
                        if (state.userAge != null) JarvisNeonGreen.copy(alpha = 0.5f) else JarvisGold.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        ageDialogInput = state.userAge?.toString() ?: "25"
                        showAgeDialog = true
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("operator_age_badge")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Operator",
                            tint = if (state.userAge != null) JarvisNeonGreen else JarvisGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OWNER: SHAHADAT ALI •",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.userAge != null) "AGE ${state.userAge} (CALIBRATED)" else "AGE NOT SET",
                            color = if (state.userAge != null) JarvisNeonGreen else JarvisGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Age",
                            tint = JarvisCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (state.userAge != null) "Edit" else "Set Age",
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Central Interactive Arc Reactor
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .testTag("arc_reactor_interactive_core"),
                contentAlignment = Alignment.Center
            ) {
                ArcReactorCore(
                    size = 220.dp,
                    state = state.assistantState,
                    audioRms = state.liveRms,
                    onClick = { handleMicClick() }
                )
            }

            // 3. Status Text Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(stateColor.copy(alpha = 0.12f))
                    .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stateLabel,
                    color = stateColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Audio Visualizer Frequency Waveform
            AudioFrequencyWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 24.dp),
                barCount = 28,
                rms = state.liveRms,
                activeColor = stateColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Response / Transcript HUD Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisBorderCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Last user query (if present)
                    AnimatedVisibility(
                        visible = state.lastUserQuery.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "YOU: ",
                                    color = JarvisGold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = state.lastUserQuery,
                                    color = JarvisTextSecondary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(JarvisBorderCyan.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Jarvis response
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SHAHADAT ASSISTANT OUTPUT:",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.currentJarvisResponse,
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Replay audio button
                        IconButton(
                            onClick = { onRepeatSpeech(state.currentJarvisResponse) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(JarvisSurfaceVariantDark)
                                .testTag("replay_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay Jarvis voice",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Quick Protocols Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickProtocols.forEach { (label, command) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(JarvisSurfaceVariantDark)
                            .border(1.dp, JarvisBorderCyan.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                            .clickable { onSendQuery(command) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Command Input & Mic Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    placeholder = {
                        Text(
                            text = "Command, question or age (e.g. 'I am 25')...",
                            color = JarvisTextSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderCyan,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedContainerColor = JarvisSurfaceDark,
                        unfocusedContainerColor = JarvisSurfaceDark,
                        cursorColor = JarvisCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendQuery(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    trailingIcon = {
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSendQuery(inputText)
                                    inputText = ""
                                },
                                modifier = Modifier.testTag("send_command_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Command",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone Toggle Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    stateColor,
                                    stateColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .clickable { handleMicClick() }
                        .testTag("mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.assistantState == AssistantState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone Trigger",
                        tint = JarvisBgDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
