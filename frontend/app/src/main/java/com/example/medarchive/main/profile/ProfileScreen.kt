package com.example.medarchive.main.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*

@Composable
fun ProfileScreenContent(
    navController: NavController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val documents by mainViewModel.documents.collectAsState()
    val healthEntries by mainViewModel.healthEntries.collectAsState()

    val analyticsCount = documents.count { it.documentType in listOf("analysis", "анализ") }
    val doctorsCount = documents.count { it.documentType in listOf("doctor", "врач") }
    val imagesCount = documents.count { it.documentType in listOf("image", "снимок") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // ====== ВЕРХНЯЯ ПАНЕЛЬ ======
        Text(
            text = "Профиль",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = LettersAndIcons,
        )

        // ====== АВАТАР + ИНФО ПОЛЬЗОВАТЕЛЯ ======
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(LightSubMainColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_circle),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    alpha = 0.8f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentUser?.fullName ?: "Гость",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = LettersAndIcons
            )

            Text(
                text = currentUser?.email ?: "не авторизован",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = LettersAndIcons.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ====== МЕНЮ ПРОФИЛЯ ======
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = RegMenu.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ProfileMenuItem(
                    icon = R.drawable.ic_person,
                    label = "Личные данные",
                    onClick = { /* Навигация или действие */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = LettersAndIcons.copy(alpha = 0.2f)
                )
                ProfileMenuItem(
                    icon = R.drawable.ic_lock,
                    label = "Безопасность",
                    onClick = { /* Навигация или действие */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = LettersAndIcons.copy(alpha = 0.2f)
                )
                ProfileMenuItem(
                    icon = R.drawable.bell,
                    label = "Уведомления",
                    onClick = { /* Навигация или действие */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = LettersAndIcons.copy(alpha = 0.2f)
                )
                ProfileMenuItem(
                    icon = R.drawable.ic_help,
                    label = "Помощь и поддержка",
                    onClick = { /* Навигация или действие */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ====== СТАТИСТИКА ======
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = RegMenu.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ваша статистика",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LettersAndIcons,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Анализы",
                        value = analyticsCount.toString(),
                        icon = R.drawable.ic_journal
                    )
                    StatItem(
                        label = "Врачи",
                        value = doctorsCount.toString(),
                        icon = R.drawable.ic_doctor
                    )
                    StatItem(
                        label = "Снимки",
                        value = imagesCount.toString(),
                        icon = R.drawable.ic_add
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ====== КНОПКА ВЫХОДА ======
        Button(
            onClick = {
                mainViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373), // Мягкий красный для выхода
                contentColor = LettersAndIcons
            ),
            shape = RoundedCornerShape(26.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = "Выйти",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = LettersAndIcons
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ====== ВСПОМОГАТЕЛЬНЫЙ КОМПОНЕНТ: ПУНКТ МЕНЮ ======
@Composable
private fun ProfileMenuItem(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = LettersAndIcons,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = LettersAndIcons,
            modifier = Modifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = "Navigate",
            tint = LettersAndIcons.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ====== ВСПОМОГАТЕЛЬНЫЙ КОМПОНЕНТ: ЭЛЕМЕНТ СТАТИСТИКИ ======
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = LettersAndIcons.copy(alpha = 0.8f),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = LettersAndIcons,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = LettersAndIcons.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}