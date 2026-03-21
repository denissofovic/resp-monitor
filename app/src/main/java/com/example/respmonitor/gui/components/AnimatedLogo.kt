package com.example.respmonitor.gui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    iconSize: Dp = 60.dp,
    showTitle: Boolean = true,
    titleText: String = "RespMonitor"
) {

    val infiniteTransition = rememberInfiniteTransition(label = "logo_animations")

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(breathingScale)
            ) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = Color(0xFF00BCD4).copy(alpha = 0.2f * glowPulse)
                        style = PaintingStyle.Fill
                        asFrameworkPaint().apply {
                            setShadowLayer(
                                40.dp.toPx(),
                                0f,
                                0f,
                                Color(0xFF00BCD4).copy(alpha = 0.4f * glowPulse).toArgb()
                            )
                        }
                    }
                    canvas.drawCircle(
                        Offset(this.size.width / 2, this.size.height / 2),
                        this.size.width / 2,
                        paint
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(size - 20.dp)
                    .scale(breathingScale),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00BCD4).copy(alpha = 0.3f),
                                Color(0xFF00BCD4).copy(alpha = 0.1f)
                            )
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    // Lungs icon (simple representation)
                    Canvas(modifier = Modifier.size(iconSize)) {
                        val centerX = this.size.width / 2
                        val centerY = this.size.height / 2

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00BCD4),
                                    Color(0xFF0097A7)
                                )
                            ),
                            radius = 20.dp.toPx(),
                            center = Offset(centerX - 15.dp.toPx(), centerY),
                            alpha = 0.8f
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00BCD4),
                                    Color(0xFF0097A7)
                                )
                            ),
                            radius = 20.dp.toPx(),
                            center = Offset(centerX + 15.dp.toPx(), centerY),
                            alpha = 0.8f
                        )

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00BCD4),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(centerX, centerY - 20.dp.toPx()),
                            end = Offset(centerX, centerY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }


        if (showTitle) {

            Text(
                text = titleText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BCD4),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = Color(0xFF00BCD4).copy(alpha = 0.3f),
                        offset = Offset(0f, 4f),
                        blurRadius = 12f
                    )
                ),
                letterSpacing = 1.sp
            )
        }
    }
}