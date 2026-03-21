package com.example.respmonitor.gui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.respmonitor.gui.components.*

@Composable
fun MonitoringScreen(
    isPositionValid: Boolean,
    breathsPerMinute: Float?,
    statusMessage: String,
    onBackClick: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "monitoring_animations")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height * 0.3f

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    shader = RadialGradientShader(
                        center = Offset(centerX, centerY),
                        radius = size.width * 0.6f,
                        colors = listOf(
                            Color(0xFF00BCD4).copy(alpha = 0.1f * glowPulse),
                            Color.Transparent
                        ),
                        colorStops = listOf(0f, 1f)
                    )
                }
                canvas.drawCircle(Offset(centerX, centerY), size.width * 0.6f, paint)
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(30.dp))


            AnimatedLogo(
                size = 100.dp,
                iconSize = 60.dp,
                showTitle = true,
                titleText = "RespMonitor"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Box(
                contentAlignment = Alignment.Center
            ) {
                if (isPositionValid) {
                    BreathingRateGauge(breathsPerMinute = breathsPerMinute)
                } else {
                    PositionStatusMinimal(statusMessage = statusMessage, onBackClick = onBackClick)
                }
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Text(
                    text = "Back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}