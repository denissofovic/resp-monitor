package com.example.respmonitor.gui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.respmonitor.gui.components.AnimatedLogo

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {


    val infiniteTransition = rememberInfiniteTransition(label = "welcome_animations")

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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(12.dp))


                        Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HOW TO USE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MinimalistInstructionItem(
                        number = "1",
                        text = "Lie down or sit comfortably",
                        progress = 0.2f
                    )

                    MinimalistInstructionItem(
                        number = "2",
                        text = "Place phone flat on chest",
                        progress = 0.4f
                    )

                    MinimalistInstructionItem(
                        number = "3",
                        text = "Screen facing up",
                        progress = 0.6f
                    )

                    MinimalistInstructionItem(
                        number = "4",
                        text = "Keep still, breathe normally",
                        progress = 0.8f
                    )

                    MinimalistInstructionItem(
                        number = "5",
                        text = "Wait for position detection",
                        progress = 1.0f
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))



            Button(
                onClick = onStartClick,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Start",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )


                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MinimalistInstructionItem(
    number: String,
    text: String,
    progress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "line_pulse")
    val linePulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(
                        color = Color(0xFF00BCD4).copy(alpha = 0.3f * linePulse),
                        radius = 10.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFF00BCD4),
                        radius = 4.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))


            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(start = 44.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.1f))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(progress * linePulse)
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00BCD4).copy(alpha = 0.3f),
                                Color(0xFF00BCD4)
                            )
                        )
                    )
            )
        }
    }
}