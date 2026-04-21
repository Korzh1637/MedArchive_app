package com.example.medarchive.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.registration.BackgroundCircles
import com.example.medarchive.ui.theme.*

@Composable
fun MainScaffold(
    navController: NavController,
    mainViewModel: MainViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MainColor, RegMenu)
                )
            )
    ) {
        BackgroundCircles()

        // Основной контент (с отступом снизу под нижнюю панель)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            content()
        }

        // Нижняя панель навигации
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
                        icon = Icons.Default.DateRange,
                        label = "Журнал",
                        isSelected = selectedTab == 0,
                        onClick = { onTabSelected(0) }
                    )
                    NavigationTab(
                        icon = Icons.Default.Home,
                        label = "Дом",
                        isSelected = selectedTab == 1,
                        onClick = { onTabSelected(1) }
                    )
                    NavigationTab(
                        icon = Icons.Default.AccountCircle,
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
    icon: ImageVector,
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
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = LettersAndIcons
            )
        }
    }
}