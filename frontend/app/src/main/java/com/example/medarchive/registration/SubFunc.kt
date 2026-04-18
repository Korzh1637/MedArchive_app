package com.example.medarchive.registration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AnimatedBackgroundCircles() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val infiniteTransition = rememberInfiniteTransition(label = "circleMovement")

    // Анимация смещения по X и Y для первого круга
    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = screenWidth.value - 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX1"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = screenHeight.value - 250f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY1"
    )

    // Для второго круга
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = screenWidth.value - 200f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX2"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = screenHeight.value - 250f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = offsetX1.dp, y = offsetY1.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8EC5FC), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = offsetX2.dp, y = offsetY2.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFA18CD1), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}