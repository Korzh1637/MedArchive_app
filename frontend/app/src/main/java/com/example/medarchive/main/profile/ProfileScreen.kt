package com.example.medarchive.main.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    navController: NavController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by mainViewModel.currentUser.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Аватар с градиентной рамкой
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(MainColor, LightSubMainColor, MainColor)
                        )
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(LightSubMainColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_circle),
                    contentDescription = "Аватар",
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = currentUser?.fullName ?: "Гость",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = DarkModeBar
            )
            Text(
                text = currentUser?.email ?: "Войдите в аккаунт",
                fontFamily = PoppinsFontFamily,
                fontSize = 15.sp,
                color = DarkModeBar.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Сетка быстрых действий
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    listOf(
                        ActionItem("Личные данные", R.drawable.ic_person, Color(0xFFE1BEE7)),
                        ActionItem("Безопасность", R.drawable.ic_lock, Color(0xFFBBDEFB)),
                        ActionItem("Уведомления", R.drawable.bell, Color(0xFFFFF9C4)),
                        ActionItem("Помощь", R.drawable.ic_help, Color(0xFFC8E6C9))
                    )
                ) { action ->
                    ProfileActionCard(action = action) {
                        // TODO: навигация
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Статистика
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = LightSubMainColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ваша активность",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = DarkModeBar
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(icon = R.drawable.ic_journal, value = "12", label = "Анализов")
                        StatChip(icon = R.drawable.ic_doctor, value = "5", label = "Врачей")
                        StatChip(icon = R.drawable.ic_add, value = "8", label = "Снимков")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка выхода
            Button(
                onClick = {
                    mainViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF9A9A),
                    contentColor = DarkModeBar
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close_md),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Выйти из аккаунта",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class ActionItem(val label: String, val icon: Int, val color: Color)

@Composable
fun ProfileActionCard(action: ActionItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightSubMainColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(action.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = action.icon),
                    contentDescription = null,
                    tint = DarkModeBar,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = action.label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DarkModeBar
            )
        }
    }
}

@Composable
fun StatChip(icon: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = DarkModeBar,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = DarkModeBar
        )
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = DarkModeBar.copy(alpha = 0.6f)
        )
    }
}