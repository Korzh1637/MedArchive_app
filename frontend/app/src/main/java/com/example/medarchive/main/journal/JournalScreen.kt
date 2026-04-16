package com.example.medarchive.main.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medarchive.R
import com.example.medarchive.domain.models.HealthEntry
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*
import java.util.Date

@Composable
fun JournalScreenContent(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val healthEntries by mainViewModel.healthEntries.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            mainViewModel.loadHealthEntries(it.id)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Записи здоровья",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = DarkModeBar
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (healthEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Пока нет записей о здоровье",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = DarkModeBar.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(healthEntries, key = { it.localId }) { entry ->
                        HealthEntryCard(entry = entry)
                    }
                }
            }
        }

        // FAB поверх всего
        if (currentUser != null) {
            FloatingActionButton(
                onClick = {
                    mainViewModel.createHealthEntry(
                        entryType = "pressure",
                        unit = "мм рт.ст.",
                        value1 = 120.0,
                        value2 = 80.0,
                        value3 = 72.0,
                        notes = "После прогулки",
                        entryDate = Date()
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
fun HealthEntryCard(entry: HealthEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
            // Иконка типа записи
            Icon(
                painter = painterResource(
                    id = when (entry.entryType) {
                        "pressure" -> R.drawable.ic_doctor
                        "glucose" -> R.drawable.ic_journal
                        else -> R.drawable.ic_journal
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = DarkModeBar
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (entry.entryType) {
                        "pressure" -> "Давление"
                        "glucose" -> "Глюкоза"
                        else -> entry.entryType
                    },
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkModeBar
                )
                Text(
                    text = buildString {
                        append(entry.value1?.let { "%.1f".format(it) } ?: "-")
                        if (entry.value2 != null) append("/${"%.1f".format(entry.value2)}")
                        if (entry.value3 != null) append("  пульс ${"%.0f".format(entry.value3)}")
                        entry.unit?.let { append(" $it") }
                    },
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.sp,
                    color = DarkModeBar.copy(alpha = 0.8f)
                )
                entry.notes?.let {
                    Text(
                        text = it,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = DarkModeBar.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = entry.entryDate.toString().substringBefore('T'),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = DarkModeBar.copy(alpha = 0.6f)
            )
        }
    }
}