package com.example.medarchive.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*

@Composable
fun MainScaffold(
    navController: NavController,
    mainViewModel: MainViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ==================== ФОН ====================
        Image(
            painter = painterResource(id = R.drawable.main_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ==================== ВЕРХНЯЯ ПАНЕЛЬ ====================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 16.dp)
            ) {
                IconButton(onClick = { println("Notifications clicked") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.bell),
                        modifier = Modifier.size(30.dp),
                        contentDescription = "Notifications",
                        tint = LettersAndIcons
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Дом"
                            1 -> "Журнал"
                            2 -> "Профиль"
                            else -> ""
                        },
                        modifier = Modifier.padding(horizontal = 30.dp),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                        color = LettersAndIcons,
                        textAlign = TextAlign.Center,
                    )
                }

                IconButton(onClick = { println("Search clicked") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        modifier = Modifier.size(30.dp),
                        contentDescription = "Search",
                        tint = LettersAndIcons
                    )
                }
            }
        }

        // ==================== ОСНОВНОЙ КОНТЕНТ ====================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp) // оставляем место под нижнюю панель
        ) {
            content()
        }

        // ==================== НИЖНЯЯ ПАНЕЛЬ НАВИГАЦИИ ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = LightSubMainColor,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTab(
                        icon = R.drawable.archive,
                        label = "Дом",
                        isSelected = selectedTab == 0,
                        onClick = { onTabSelected(0) }
                    )
                    NavigationTab(
                        icon = R.drawable.ic_bottom_journal,
                        label = "Журнал",
                        isSelected = selectedTab == 1,
                        onClick = { onTabSelected(1) }
                    )
                    NavigationTab(
                        icon = R.drawable.user_circle,
                        label = "Профиль",
                        isSelected = selectedTab == 2,
                        onClick = { onTabSelected(2) }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationTab(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MainColor else Color.Transparent,
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp),
        shadowElevation = if (isSelected) 6.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(70.dp)
                .height(70.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) DarkModeBar else DarkModeBar.copy(alpha = 0.6f)
            )
        }
    }
}