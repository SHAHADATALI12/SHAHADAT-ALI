package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGold
import com.example.viewmodel.AssistantState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    state: AssistantState = AssistantState.STANDBY,
    audioRms: Float = 0f,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Outer slow rotation
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_angle"
    )

    // Inner counter-rotation (speeds up when listening or speaking)
    val innerSpeed = when (state) {
        AssistantState.LISTENING -> 4000
        AssistantState.PROCESSING -> 2000
        AssistantState.SPEAKING -> 6000
        AssistantState.STANDBY -> 12000
    }
    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = innerSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_angle"
    )

    // Pulse core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AssistantState.LISTENING) 700 else 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when (state) {
        AssistantState.LISTENING -> Color(0xFF00F5D4)
        AssistantState.PROCESSING -> JarvisGold
        AssistantState.SPEAKING -> JarvisCyan
        AssistantState.STANDBY -> JarvisCyan
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f - 12.dp.toPx()

            // 1. Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.35f * (0.8f + audioRms * 0.5f)),
                        coreColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f,
                center = center
            )

            // 2. Outer Segmented Ring
            rotate(outerAngle, pivot = center) {
                drawCircle(
                    color = JarvisCyanDark.copy(alpha = 0.35f),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 18f, 10f, 18f), 0f)
                    )
                )

                // 8 Tick marks
                val tickCount = 8
                for (i in 0 until tickCount) {
                    val angleRad = (i * 2 * PI / tickCount).toFloat()
                    val startR = radius - 8.dp.toPx()
                    val endR = radius + 4.dp.toPx()
                    val p1 = Offset(center.x + cos(angleRad) * startR, center.y + sin(angleRad) * startR)
                    val p2 = Offset(center.x + cos(angleRad) * endR, center.y + sin(angleRad) * endR)
                    drawLine(
                        color = JarvisCyan.copy(alpha = 0.6f),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 3. Middle Rotating Arc Reactor Chamber (10 Coils)
            rotate(innerAngle, pivot = center) {
                val coilRadius = radius * 0.72f
                val coilCount = 10
                for (i in 0 until coilCount) {
                    val angleRad = (i * 2 * PI / coilCount).toFloat()
                    val coilCenter = Offset(
                        center.x + cos(angleRad) * coilRadius,
                        center.y + sin(angleRad) * coilRadius
                    )
                    // Draw mini glowing power nodule
                    drawCircle(
                        color = coreColor.copy(alpha = 0.85f),
                        radius = 4.5.dp.toPx() * (1f + audioRms * 0.4f),
                        center = coilCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = coilCenter
                    )
                }

                // Inner circle holding coils
                drawCircle(
                    color = JarvisCyan.copy(alpha = 0.45f),
                    radius = coilRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 4. Central Energy Core
            val coreR = radius * 0.42f * (if (state == AssistantState.LISTENING) pulseScale else 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        coreColor.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreR
                ),
                radius = coreR,
                center = center
            )

            // Inner Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = coreR * 0.65f,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Central Stark Tech Triangle
            val triSize = coreR * 0.4f
            val triPath = Path().apply {
                val a1 = (-PI / 2).toFloat()
                val a2 = (PI / 6).toFloat()
                val a3 = (5 * PI / 6).toFloat()
                moveTo(center.x + cos(a1) * triSize, center.y + sin(a1) * triSize)
                lineTo(center.x + cos(a2) * triSize, center.y + sin(a2) * triSize)
                lineTo(center.x + cos(a3) * triSize, center.y + sin(a3) * triSize)
                close()
            }
            drawPath(
                path = triPath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun AudioFrequencyWaveform(
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    rms: Float = 0f,
    activeColor: Color = JarvisCyan
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = (width / barCount) * 0.55f
        val step = width / barCount

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount
            // Sine harmonic
            val wave = (sin(normalizedX * 4 * PI + phase).toFloat() + 1f) / 2f
            val minHeight = 4.dp.toPx()
            val dynamicHeight = minHeight + (height - minHeight) * (wave * (0.25f + rms * 0.75f))
            val x = i * step + step / 2f
            val yTop = (height - dynamicHeight) / 2f
            val yBottom = yTop + dynamicHeight

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.3f),
                        activeColor,
                        activeColor.copy(alpha = 0.3f)
                    )
                ),
                start = Offset(x, yTop),
                end = Offset(x, yBottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
