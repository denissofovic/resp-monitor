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
import androidx.compose.ui.draw.scale

@Composable
fun BreathingRateGauge(
    breathsPerMinute: Float?,
    modifier: Modifier = Modifier
) {
    val animatedBpm by animateFloatAsState(
        targetValue = breathsPerMinute ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "bpm"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val progressColor = Color(0xFF00BCD4)

    Box(
        modifier = modifier
            .size(300.dp)
            .scale(breathingScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth

            drawCircle(
                color = progressColor.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            val sweepAngle = (animatedBpm / 40f).coerceIn(0f, 1f) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(progressColor.copy(alpha = 0.5f), progressColor)
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(progressColor.copy(alpha = 0.15f), Color.Transparent),
                    radius = radius + 40.dp.toPx()
                ),
                radius = radius + 40.dp.toPx()
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (breathsPerMinute != null) "%.0f".format(animatedBpm) else "--",
                fontSize = 80.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "BPM",
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}