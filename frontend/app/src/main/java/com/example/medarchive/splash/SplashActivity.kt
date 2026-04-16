package com.example.medarchive.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.navigation.Screen
import com.example.medarchive.ui.theme.PoppinsFontFamily
import com.example.medarchive.ui.theme.MainColor
import com.example.medarchive.ui.theme.DarkModeBar
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import com.example.medarchive.R
import com.example.medarchive.ui.theme.LettersAndIcons
import com.example.medarchive.ui.theme.RegMenu

@Composable
fun SplashActivity(navController: NavController) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = 3

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновое изображение в зависимости от этапа
        when (currentStep) {
            0 -> SplashScreenBackground(R.drawable.first)
            1 -> SplashScreenBackground(R.drawable.second)
            2 -> SplashScreenBackground(R.drawable.third)
        }

        // Контент поверх фона
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(35.dp))
            // Индикатор прогресса
            GradientProgressBar((currentStep + 1).toFloat() / steps)

            Spacer(Modifier.weight(1f))

            WavyTextContainer(
                step = currentStep,
                onStepChange = { newStep ->
                    if (newStep == 3) {
                        // Переход на следующий экран
                        navController.navigate(Screen.RegLogMain.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        currentStep = newStep
                    }
                },
            )
        }
    }
}

@Composable
fun SplashScreenBackground(screen: Int) {
    Image(
        painter = painterResource(id = screen),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun WavyTextContainer(step: Int, onStepChange: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(WaveShape())
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MainColor.copy(0.85f),
                        LettersAndIcons
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(50.dp))

            when (step) {
                0 -> {
                    CustomBox(
                        "Вы получили медицинские анализы,\nно вам негде их хранить?",
                        "Мы поможем вам организовать\nхранение документов",
                        "Далее", onNext = { onStepChange(1) }, onBack = null
                    )
                }
                1 -> {
                    CustomBox(
                        "Сфотографируйте или отсканируйте\nрезультаты теста",
                        "Просто сделайте фото документа\nи загрузите в приложение",
                        "Далее", onNext = { onStepChange(2) }, onBack = { onStepChange(0) }
                    )
                }
                2 -> {
                    CustomBox(
                        "Загружайте их в наше приложение,\nчтобы они всегда были под рукой",
                        "Все ваши медицинские документы\nв одном безопасном месте",
                        "Начать", onNext = { onStepChange(3) }, onBack = { onStepChange(1)}
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBox(firstText: String, secondText: String, text: String, onNext: () -> Unit, onBack: (() -> Unit)?){
    Text(
        text = firstText,
        color = LettersAndIcons,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = PoppinsFontFamily,
        textAlign = TextAlign.Center,
        lineHeight = 26.sp
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = secondText,
        color = LettersAndIcons,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(Modifier.height(24.dp))

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка "Назад" только если onBack != null
        onBack?.let {
            CustomButton("Назад", onClick = it, isSecondary = true)
        } ?: Spacer(modifier = Modifier.width(140.dp)) // ← заглушка для центрирования
        Spacer(modifier = Modifier.width(20.dp))
        CustomButton(text, onClick = onNext)
    }

}

@Composable
fun CustomButton(text: String, onClick: () -> Unit, isSecondary: Boolean = false){
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) Color.Transparent else MainColor,
            contentColor = MainColor
        ),
        shape = RoundedCornerShape(30.dp),
        border = if (isSecondary)
            BorderStroke(2.dp, MainColor.copy(alpha = 0.8f))
        else null
    ) {
        Text(
            text = text,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = if (isSecondary) MainColor else LettersAndIcons
        )
    }
}

@Composable
fun GradientProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MainColor, LettersAndIcons)
                    )
                )
        )
    }
}
