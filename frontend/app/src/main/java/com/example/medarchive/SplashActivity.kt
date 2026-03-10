    package com.example.medarchive

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.size
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.example.medarchive.navigation.Screen
    import com.example.medarchive.ui.theme.PoppinsFontFamily
    import kotlinx.coroutines.delay
    import com.example.medarchive.ui.theme.MainColor
    import com.example.medarchive.ui.theme.DarkModeBar

    @Composable
    fun SplashActivity(navController: NavController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainColor), // Ваш MainColor
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_dark),
                    contentDescription = "MedArchive Logo",
                    modifier = Modifier.size(200.dp)
                )

                Text(
                    text = "MedArchive",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 52.sp,
                    color = DarkModeBar
                )
            }
        }

        LaunchedEffect(Unit) {
            delay(1000) // Показывать 1 секунду

            // Переход на экран регистрации с очисткой стека
            navController.navigate(Screen.RegLogMain.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true  // Удаляем Splash из стека, чтобы нельзя было вернуться
                }
                launchSingleTop = true // Не создавать дубликаты экрана
            }
        }
    }
