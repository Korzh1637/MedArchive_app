package com.example.medarchive.main.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.domain.models.HealthCategory
import com.example.medarchive.domain.models.HealthEntry
import com.example.medarchive.domain.models.HealthStats
import com.example.medarchive.domain.models.calculateTrend
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.registration.BackgroundCircles
import com.example.medarchive.ui.theme.*
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    navController: NavController,
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val categories by mainViewModel.healthCategories.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            mainViewModel.loadHealthCategories()
        }
    }

    val categoryName = remember(categoryId, categories) {
        categories.find { it.id == categoryId }?.name ?: ""
    }
    val category = remember(categoryId, categories) {
        categories.find { it.id == categoryId }
    }

    val entries by mainViewModel.getEntriesForCategory(categoryName)
        .collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<HealthEntry?>(null) }

    val stats = remember(entries) {
        if (entries.isNotEmpty()) {
            val values = entries.mapNotNull { it.value1 }
            HealthStats(
                average = values.average(),
                min = values.minOrNull() ?: 0.0,
                max = values.maxOrNull() ?: 0.0,
                trend = calculateTrend(values)
            )
        } else null
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                colors = listOf(MainColor, RegMenu)
            )
        )
    ) {
        // Декоративный фон
        BackgroundCircles()

        // Основной контент без Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = LettersAndIcons
                    )
                }
                Text(
                    text = category?.name ?: "",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = LettersAndIcons,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // График
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightSubMainColor)
            ) {
                if (entries.any { it.value1 != null }) {
                    LineChartView(entries = entries, unit = category?.unit ?: "")
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Нет данных для графика",
                            fontFamily = PoppinsFontFamily,
                            color = LettersAndIcons.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Отчёт
            if (stats != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSubMainColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Отчёт",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = LettersAndIcons
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Среднее", String.format("%.1f", stats.average), category?.unit ?: "")
                            StatItem("Мин", String.format("%.1f", stats.min), category?.unit ?: "")
                            StatItem("Макс", String.format("%.1f", stats.max), category?.unit ?: "")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "История измерений",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = LettersAndIcons
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(entries) { entry ->
                    EntryListItem(
                        entry = entry,
                        unit = category?.unit ?: "",
                        onDelete = { entryToDelete = entry }   // запоминаем запись для диалога
                    )
                }
            }
        }

        // FAB для добавления записи
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = DarkModeBar,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить",
                tint = LettersAndIcons
            )
        }

        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Удалить запись?") },
                text = {
                    Text("Значение: ${entryToDelete!!.value1} ${category?.unit ?: ""}\n" +
                            "Дата: ${SimpleDateFormat("dd.MM.yyyy").format(entryToDelete!!.entryDate)}")
                },
                confirmButton = {
                    TextButton(onClick = {
                        mainViewModel.deleteHealthEntry(entryToDelete!!.localId)
                        entryToDelete = null
                    }) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            category = category,
            onDismiss = { showAddDialog = false },
            onAdd = { value, notes, date ->
                mainViewModel.addHealthEntry(categoryId, value, notes, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LineChartView(entries: List<HealthEntry>, unit: String) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val rawValues = remember(entries) {
        entries.mapNotNull { it.value1?.toDouble() }
    }

    val minVal = (rawValues.minOrNull() ?: 0.0) - 20
    val maxVal = (rawValues.maxOrNull() ?: 0.0) + 20

    // Вспомогательная серия – содержит только граничные значения, остальные NaN
    val extendedValues = remember(rawValues, minVal, maxVal) {
        if (rawValues.isEmpty()) rawValues
        else rawValues.mapIndexed { index, _ ->
            when (index) {
                0 -> minVal
                rawValues.size - 1 -> maxVal
                else -> Double.NaN
            }
        }
    }

    LaunchedEffect(rawValues) {
        if (rawValues.isNotEmpty()) {
            modelProducer.runTransaction {
                // Видимая основная линия
                lineSeries { series(rawValues) }
                // Невидимая линия для расширения оси Y
                lineSeries { series(extendedValues) }
            }
        }
    }

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                // Первая линия – реальные значения
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(
                        Fill(Color(0xFFA18CD1).toArgb())
                    ),
                    stroke = LineCartesianLayer.LineStroke.Continuous(
                        thicknessDp = 10f
                    )
                ),
                // Вторая линия – прозрачная, только для расширения диапазона
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent.toArgb())),
                    stroke = LineCartesianLayer.LineStroke.Continuous(
                        thicknessDp = 0f,
                    )
                )
            )
        ),
        startAxis = VerticalAxis.rememberStart(
            label = rememberAxisLabelComponent(
                color = LettersAndIcons,
                textSize = 10.sp
            )
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(
                color = LettersAndIcons,
                textSize = 10.sp
            ),
            valueFormatter = { x, _, _ ->
                val index = (x as? Number)?.toInt() ?: 0
                if (index in entries.indices) {
                    SimpleDateFormat("dd.MM", Locale.getDefault()).format(entries[index].entryDate)
                } else ""
            }
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun EntryListItem(
    entry: HealthEntry,
    unit: String,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightSubMainColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${entry.value1} $unit",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LettersAndIcons
                )
                if (!entry.notes.isNullOrBlank()) {
                    Text(
                        text = entry.notes,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = LettersAndIcons.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.End) {
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(entry.entryDate),
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.sp,
                    color = LettersAndIcons.copy(alpha = 0.7f)
                )
            }
            if (onDelete != null) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkModeBar)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete, // убедитесь, что иконка есть в ресурсах
                            contentDescription = "Удалить",
                            tint = LettersAndIcons,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = LettersAndIcons.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LettersAndIcons
        )
        Text(
            text = unit,
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = LettersAndIcons.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AddEntryDialog(
    category: HealthCategory?,
    onDismiss: () -> Unit,
    onAdd: (value: Double, notes: String?, date: Date) -> Unit
) {
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val date = remember { Date() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить запись ${category?.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Значение (${category?.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметка") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val num = value.toDoubleOrNull()
                    if (num != null) {
                        onAdd(num, notes.takeIf { it.isNotBlank() }, date)
                    }
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}