package com.example.respmonitor.gui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BreathingRateGauge(
    breathsPerMinute: Float?,
    modifier: Modifier = Modifier
) {

    val minBpm = 0f
    val maxBpm = 40f
    val normalMin = 12f
    val normalMax = 20f


    val animatedBpm by animateFloatAsState(
        targetValue = breathsPerMinute ?: 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "bpm_animation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val progressPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress_pulse"
    )

    // Subtle rotation animation for gradient
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    val progress = (animatedBpm / maxBpm).coerceIn(0f, 1f)

    val progressColor = when {
        animatedBpm < normalMin -> Color(0xFF4CAF50)
        animatedBpm <= normalMax -> Color(0xFF00BCD4)
        animatedBpm <= 30f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .padding(10.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2
                val centerY = canvasHeight / 2
                val radius = (canvasWidth / 2) - 50.dp.toPx()

                val startAngle = 150f
                val sweepAngle = 240f


                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = progressColor.copy(alpha = 0.1f * glowPulse)
                        style = PaintingStyle.Stroke
                        strokeWidth = 60.dp.toPx()
                        this.asFrameworkPaint().apply {
                            setShadowLayer(
                                40.dp.toPx(),
                                0f,
                                0f,
                                progressColor.copy(alpha = 0.3f * glowPulse).toArgb()
                            )
                        }
                    }

                    canvas.drawArc(
                        rect = androidx.compose.ui.geometry.Rect(
                            left = centerX - radius - 10.dp.toPx(),
                            top = centerY - radius - 10.dp.toPx(),
                            right = centerX + radius + 10.dp.toPx(),
                            bottom = centerY + radius + 10.dp.toPx()
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        paint = paint
                    )
                }

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.1f),
                            Color.Gray.copy(alpha = 0.15f),
                            Color.Gray.copy(alpha = 0.1f)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = 40.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )


                val zoneColors = arrayOf(
                    0.0f to Color(0xFF4CAF50),      // Green start
                    0.2f to Color(0xFF8BC34A),      // Light green
                    0.35f to Color(0xFF00BCD4),     // Cyan
                    0.5f to Color(0xFF03A9F4),      // Blue
                    0.6f to Color(0xFFFFEB3B),      // Yellow
                    0.75f to Color(0xFFFF9800),     // Orange
                    0.85f to Color(0xFFFF5722),     // Deep orange
                    1.0f to Color(0xFFF44336)       // Red end
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = zoneColors,
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = 35.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    alpha = 0.3f
                )


                val progressSweep = progress * sweepAngle

                drawArc(
                    color = Color.Black.copy(alpha = 0.2f),
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = Offset(
                        centerX - radius + 2.dp.toPx(),
                        centerY - radius + 2.dp.toPx()
                    ),
                    size = Size(radius * 2 - 4.dp.toPx(), radius * 2 - 4.dp.toPx()),
                    style = Stroke(
                        width = 35.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.6f),
                            progressColor,
                            progressColor.copy(alpha = 0.9f)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = 35.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    alpha = progressPulse
                )

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = progressColor.copy(alpha = 0.4f * glowPulse)
                        style = PaintingStyle.Stroke
                        strokeWidth = 40.dp.toPx()
                        strokeCap = StrokeCap.Round
                        this.asFrameworkPaint().apply {
                            setShadowLayer(
                                20.dp.toPx(),
                                0f,
                                0f,
                                progressColor.copy(alpha = 0.6f * glowPulse).toArgb()
                            )
                        }
                    }

                    canvas.drawArc(
                        rect = androidx.compose.ui.geometry.Rect(
                            left = centerX - radius,
                            top = centerY - radius,
                            right = centerX + radius,
                            bottom = centerY + radius
                        ),
                        startAngle = startAngle,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        paint = paint
                    )
                }

                for (i in 0..8) {
                    val tickValue = i * 5f
                    val tickProgress = tickValue / maxBpm
                    val tickAngle = startAngle + (tickProgress * sweepAngle)
                    val tickAngleRad = Math.toRadians(tickAngle.toDouble())

                    val isMajorTick = i % 2 == 0
                    val tickStartRadius = if (isMajorTick) radius + 8.dp.toPx() else radius + 12.dp.toPx()
                    val tickEndRadius = radius + 20.dp.toPx()

                    val tickStartX = centerX + tickStartRadius * cos(tickAngleRad).toFloat()
                    val tickStartY = centerY + tickStartRadius * sin(tickAngleRad).toFloat()
                    val tickEndX = centerX + tickEndRadius * cos(tickAngleRad).toFloat()
                    val tickEndY = centerY + tickEndRadius * sin(tickAngleRad).toFloat()

                    drawLine(
                        color = if (isMajorTick)
                            Color.White.copy(alpha = 0.3f)
                        else
                            Color.White.copy(alpha = 0.15f),
                        start = Offset(tickStartX, tickStartY),
                        end = Offset(tickEndX, tickEndY),
                        strokeWidth = if (isMajorTick) 3.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }


                val needleAngle = startAngle + progressSweep
                val needleAngleRad = Math.toRadians(needleAngle.toDouble())
                val needleLength = radius - 5.dp.toPx()
                val needleBaseRadius = 15.dp.toPx()
                val needleEndX = centerX + needleLength * cos(needleAngleRad).toFloat()
                val needleEndY = centerY + needleLength * sin(needleAngleRad).toFloat()

                drawIntoCanvas { canvas ->
                    val shadowPaint = Paint().apply {
                        color = Color.Black.copy(alpha = 0.3f)
                        strokeWidth = 8.dp.toPx()
                        strokeCap = StrokeCap.Round
                        this.asFrameworkPaint().apply {
                            setShadowLayer(
                                10.dp.toPx(),
                                2.dp.toPx(),
                                2.dp.toPx(),
                                Color.Black.copy(alpha = 0.5f).toArgb()
                            )
                        }
                    }

                    canvas.drawLine(
                        p1 = Offset(centerX, centerY),
                        p2 = Offset(needleEndX, needleEndY),
                        paint = shadowPaint
                    )
                }

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.8f),
                            progressColor,
                            Color.White
                        ),
                        start = Offset(centerX, centerY),
                        end = Offset(needleEndX, needleEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )


                drawCircle(
                    color = Color.Black.copy(alpha = 0.2f),
                    radius = needleBaseRadius + 4.dp.toPx(),
                    center = Offset(centerX + 1.dp.toPx(), centerY + 1.dp.toPx())
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.8f),
                            progressColor
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    radius = needleBaseRadius,
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = needleBaseRadius - 3.dp.toPx(),
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            progressColor.copy(alpha = 0.5f)
                        ),
                        center = Offset(centerX - 2.dp.toPx(), centerY - 2.dp.toPx()),
                        radius = 8.dp.toPx()
                    ),
                    radius = 8.dp.toPx(),
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 3.dp.toPx(),
                    center = Offset(centerX - 3.dp.toPx(), centerY - 3.dp.toPx())
                )
            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-80).dp)
            ) {
                Text(
                    text = if (breathsPerMinute != null) "%.1f".format(animatedBpm) else "--",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = Shadow(
                            color = progressColor.copy(alpha = 0.5f * glowPulse),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "BREATHS PER MINUTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val statusText = when {
                    breathsPerMinute == null -> "INITIALIZING"
                    animatedBpm < 6f -> "VERY LOW"
                    animatedBpm < normalMin -> "LOW"
                    animatedBpm <= normalMax -> "NORMAL"
                    animatedBpm <= 30f -> "ELEVATED"
                    else -> "VERY HIGH"
                }

                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    color = progressColor.copy(alpha = 0.15f),
                    contentColor = progressColor
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .offset(y = (-60).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "10", "20", "30", "40").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}