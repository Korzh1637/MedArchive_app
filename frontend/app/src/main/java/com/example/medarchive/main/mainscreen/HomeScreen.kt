package com.example.medarchive.main.mainscreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medarchive.R
import com.example.medarchive.domain.models.Document
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*

@Composable
fun HomeScreenContent(
    mainViewModel: MainViewModel,
    chosenElement: Int,
    onChosenElementChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val currentUser by mainViewModel.currentUser.collectAsState()
    val documents by mainViewModel.documents.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            mainViewModel.loadDocuments(it.id)
        }
    }

    val filteredDocuments = remember(documents, chosenElement) {
        when (chosenElement) {
            1 -> documents.filter { it.documentType == "analysis" || it.documentType == "анализ" }
            2 -> documents.filter { it.documentType == "doctor" || it.documentType == "врач" }
            3 -> documents.filter { it.documentType == "image" || it.documentType == "снимок" }
            else -> emptyList()
        }
    }

    val contentHeight by animateDpAsState(
        targetValue = if (chosenElement == 0) screenHeight * 0.42f else screenHeight * 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentHeight"
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
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
                // Заголовок + Кнопка закрытия
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    if (chosenElement != 0) {
                        IconButton(onClick = { onChosenElementChange(0) }) {
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

                // Свёрнутое состояние
                if (chosenElement == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CategoryButton(
                            icon = R.drawable.ic_journal,
                            label = "Анализы",
                            isSelected = false,
                            onClick = { onChosenElementChange(1) }
                        )
                        CategoryButton(
                            icon = R.drawable.ic_doctor,
                            label = "Врачи",
                            isSelected = false,
                            onClick = { onChosenElementChange(2) }
                        )
                        CategoryButton(
                            icon = R.drawable.ic_doctor,
                            label = "Снимки",
                            isSelected = false,
                            onClick = { onChosenElementChange(3) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* Показать все недавние */ },
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

                // Развёрнутое состояние: список файлов
                if (chosenElement != 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredDocuments.isEmpty()) {
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
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            items(filteredDocuments, key = { it.localId }) { document ->
                                DocumentItem(
                                    document = document,
                                    onClick = { println("Clicked: ${document.title}") },
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB для добавления документа
        if (chosenElement == 0 && currentUser != null) {
            FloatingActionButton(
                onClick = {
                    mainViewModel.createDocument(
                        imagePath = null,
                        title = "Новый документ",
                        type = "analysis",
                        text = "Содержимое документа"
                    )
                },
                containerColor = LightSubMainColor,
                contentColor = LettersAndIcons,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp),
                    tint = DarkModeBar
                )
            }
        }
    }
}

@Composable
fun DocumentItem(document: Document, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = LightSubMainColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = when (document.documentType) {
                        "analysis", "анализ" -> R.drawable.ic_journal
                        "doctor", "врач" -> R.drawable.ic_doctor
                        else -> R.drawable.ic_doctor
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = DarkModeBar
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title ?: "Без названия",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkModeBar
                )
                Text(
                    text = document.createdAt.toString().substringBefore('T'),
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = DarkModeBar.copy(alpha = 0.7f)
                )
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
                    tint = DarkModeBar
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