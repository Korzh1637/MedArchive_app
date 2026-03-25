package com.example.medarchive.main.mainscreen

import android.R.attr.maxHeight
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    var chosenElement by remember { mutableStateOf(0) } // 0 = свернуто, 1/2/3 = категория
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val contentHeight by animateDpAsState(
        targetValue = if (chosenElement == 0) {
            screenHeight * 0.42f
        } else {
            screenHeight * 0.9f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentHeight"
    )

    // Анимация прозрачности фона
    val overlayAlpha by animateFloatAsState(
        targetValue = if (chosenElement == 0) 0f else 0.1f,
        label = "overlayAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ==================== ФОН ====================
        Image(
            painter = painterResource(id = R.drawable.main_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                IconButton(onClick = {
                    println("Notifications clicked")
                }) {
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
                    contentAlignment = Alignment.Center,

                    ) {
                    Text(
                        text = "Дом",
                        modifier = Modifier.padding(horizontal = 30.dp),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                        color = LettersAndIcons,
                        textAlign = TextAlign.Center,
                    )
                }

                IconButton(onClick = {
                    println("Search clicked")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        modifier = Modifier.size(30.dp),
                        contentDescription = "Search",
                        tint = LettersAndIcons
                    )
                }
            }
        }


        // ==================== ОСНОВНОЙ КОНТЕНТ (ПРИЖАТ К НИЗУ) ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MainColor.copy(alpha = 0.7f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ----- Заголовок + Кнопка закрытия -----
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Показываем название категории или скрываем
                        if (chosenElement != 0) {
                            Text(
                                text = when (chosenElement) {
                                    1 -> "Анализы"
                                    2 -> "Врачи"
                                    3 -> "Снимки"
                                    else -> ""
                                },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = DarkModeBar
                            )
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }

                        // Кнопка закрытия (показывается только в развёрнутом состоянии)
                        if (chosenElement != 0) {
                            IconButton(onClick = { chosenElement = 0 }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.close_md),
                                    contentDescription = "Close",
                                    tint = DarkModeBar,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }

                    // ----- Свёрнутое состояние: Категории + Кнопка "Недавние" -----
                    if (chosenElement == 0) {
                        // Категории
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CategoryButton(
                                icon = R.drawable.ic_journal,
                                label = "Анализы",
                                isSelected = false,
                                onClick = { chosenElement = 1 }
                            )
                            CategoryButton(
                                icon = R.drawable.ic_doctor,
                                label = "Врачи",
                                isSelected = false,
                                onClick = { chosenElement = 2 }
                            )
                            CategoryButton(
                                icon = R.drawable.ic_doctor,
                                label = "Снимки",
                                isSelected = false,
                                onClick = { chosenElement = 3 }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопка "Недавние"
                        Button(
                            onClick = { println("Recent clicked") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RegMenu,
                                contentColor = LettersAndIcons
                            ),
                            shape = RoundedCornerShape(22.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = "Недавние",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = LettersAndIcons
                            )
                        }
                    }

                    // ----- Развёрнутое состояние: Список файлов ======
                    if (chosenElement != 0) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Скроллируемый список файлов
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f), // Занимает оставшееся место
                            horizontalAlignment = Alignment.Start
                        ) {
//                            // Список из базы данных
//                            items(
//                                items = recentFiles.filter {
//                                    // Фильтруем по выбранной категории
//                                    when (chosenElement) {
//                                        1 -> it.fileType == FileType.ANALYSIS
//                                        2 -> it.fileType == FileType.DOCTOR
//                                        3 -> it.fileType == FileType.IMAGE
//                                        else -> false
//                                    }
//                                },
//                                key = { file -> file.id }
//                            ) { file ->
//                                RecentFileItem(
//                                    file = file,
//                                    onClick = {
//                                        println("Clicked: ${file.title}")
//                                        navController.navigate(
//                                            Screen.FileDetail.route + "/${file.id}"
//                                        )
//                                    },
//                                    modifier = Modifier.padding(vertical = 6.dp)
//                                )
//                            }

                            // Если файлов нет
//                            if (recentFiles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Нет файлов в этой категории",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = DarkModeBar.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
//                            }
                        }
                    }
                }
            }
        }

        // ==================== FLOATING ACTION BUTTON ====================
        // Скрываем FAB при развёрнутом контенте (опционально)
        if (chosenElement == 0) {
            FloatingActionButton(
                onClick = { println("Add clicked") },
                containerColor = LightSubMainColor,
                contentColor = LettersAndIcons,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp),
                    tint = DarkModeBar
                )
            }
        }

        // ==================== ЗАКРУГЛЁННАЯ НИЖНЯЯ ПАНЕЛЬ ====================
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
                    // Вкладки (Дом, Журнал, Профиль)
                    NavigationTab(
                        icon = R.drawable.archive,
                        label = "Дом",
                        isSelected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            chosenElement = 0
                        }
                    )

                    NavigationTab(
                        icon = R.drawable.ic_bottom_journal,
                        label = "Журнал",
                        isSelected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            chosenElement = 0
                        }
                    )
                    NavigationTab(
                        icon = R.drawable.user_circle,
                        label = "Профиль",
                        isSelected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            chosenElement = 0
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp).background(Color.Transparent)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            color = LightSubMainColor,
            shadowElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = LettersAndIcons
        )
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