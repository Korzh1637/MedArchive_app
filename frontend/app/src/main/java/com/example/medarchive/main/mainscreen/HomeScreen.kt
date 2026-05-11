package com.example.medarchive.main.mainscreen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.medarchive.R
import com.example.medarchive.domain.models.Document
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// ==================== ТИПЫ ДАННЫХ ====================

data class Category(
    val name: String,
    val iconRes: Int,
    val color: Color,
    val typeKey: String
)

// ==================== ГЛАВНЫЙ ЭКРАН ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by mainViewModel.currentUser.collectAsState()
    val documents by mainViewModel.documents.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedCategoryType by remember { mutableStateOf<String?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    var pendingImagePath by remember { mutableStateOf<String?>(null) }

    // Состояния для парсинга
    var isParsing by remember { mutableStateOf(false) }
    var parsedDoc by remember { mutableStateOf<MainViewModel.ParsedDocument?>(null) }

    // Для полноэкранного просмотра изображения
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }

    // Лончер камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                val path = saveImageToInternalStorage(context, uri)
                if (path != null) {
                    pendingImagePath = path
                }
            }
        }
    }

    // Лончер галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = saveImageToInternalStorage(context, it)
            if (path != null) {
                pendingImagePath = path
            }
        }
    }

    // Лончер разрешения камеры
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createTempImageFile(context)?.let { file ->
                tempImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(tempImageUri!!)
            }
        }
    }

    // Загрузка документов при смене пользователя
    LaunchedEffect(currentUser) {
        currentUser?.let {
            mainViewModel.loadDocuments(it.id)
        }
    }

    // Запуск парсинга при появлении пути к изображению
    LaunchedEffect(pendingImagePath) {
        val path = pendingImagePath ?: return@LaunchedEffect
        val user = currentUser ?: return@LaunchedEffect

        isParsing = true

        parsedDoc = try {
            mainViewModel.parseDocumentFromServer(path)
        } catch (e: Exception) {
            Log.e("HomeScreen", "Parse exception", e)
            null
        }

        isParsing = false
        showMetadataDialog = true
    }

    // Показываем Toast с результатом парсинга
    LaunchedEffect(isParsing, parsedDoc) {
        if (!isParsing && pendingImagePath != null) {
            if (parsedDoc != null) {
                Toast.makeText(context, "Документ успешно распознан", Toast.LENGTH_SHORT).show()
                Log.d("HomeScreen", "Parsed OK: title=${parsedDoc!!.title}, type=${parsedDoc!!.documentType}")
            } else {
                Toast.makeText(context, "Не удалось распознать документ", Toast.LENGTH_SHORT).show()
                Log.e("HomeScreen", "Parsing failed")
            }
        }
    }

    // Фильтрация документов по категории
    val filteredDocuments = remember(documents, selectedCategoryType) {
        if (selectedCategoryType == null) documents
        else documents.filter { it.documentType == selectedCategoryType }
    }

    // ========== ДИАЛОГИ ==========
    if (showSourceDialog) {
        SourceChoiceDialog(
            onDismiss = { showSourceDialog = false },
            onCameraClick = {
                showSourceDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                showSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    if (isParsing) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Обработка снимка") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Идёт распознавание...")
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }

    if (showMetadataDialog && pendingImagePath != null && !isParsing) {
        val isTypeLocked = parsedDoc != null
        AddDocumentMetadataDialog(
            initialTitle = "Ваше название",
            initialType = mapServerTypeToLocal(parsedDoc?.title),
            initialNotes = parsedDoc?.content,
            isTypeLocked = isTypeLocked,
            onDismiss = {
                showMetadataDialog = false
                pendingImagePath = null
                parsedDoc = null
            },
            onAdd = { title, type, notes ->
                currentUser?.id?.let { userId ->
                    if (parsedDoc != null) {
                        mainViewModel.createDocument(
                            imagePath = pendingImagePath,
                            title = title.ifEmpty { parsedDoc!!.title },
                            type = type,
                            text = notes
                        )
                    } else {
                        mainViewModel.createDocument(
                            imagePath = pendingImagePath,
                            title = title,
                            type = type,
                            text = notes
                        )
                    }
                }
                showMetadataDialog = false
                pendingImagePath = null
                parsedDoc = null
            }
        )
    }

    // Полноэкранный просмотр изображения
    if (fullScreenImagePath != null) {
        FullScreenImageDialog(
            imagePath = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
        )
    }

    // Основной контент
    HomeScreenContentScaffold(
        currentUser = currentUser,
        documents = filteredDocuments,
        selectedCategoryType = selectedCategoryType,
        onCategorySelected = { selectedCategoryType = it },
        onShowAll = { selectedCategoryType = null },
        onAddClick = { showSourceDialog = true },
        onDocumentClick = { doc ->
            doc.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    fullScreenImagePath = path
                } else {
                    Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(context, "Нет изображения", Toast.LENGTH_SHORT).show()
        },
        onDeleteDocument = { doc ->
            mainViewModel.deleteDocument(doc.localId)
        },
        modifier = modifier
    )
}

// ========== ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ ==========
private fun mapServerTypeToLocal(serverType: String?): String {
    return when (serverType?.lowercase()) {
        "lab_analysis"        -> "analysis"
        "instrumental_study"  -> "image"
        "doctor_conclusion"   -> "doctor"
        else                  -> "others"
    }
}

// ==================== ПОЛНОЭКРАННЫЙ ДИАЛОГ ====================
@Composable
private fun FullScreenImageDialog(imagePath: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // на весь экран
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ==================== КОМПОНОВКА ЭКРАНА ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContentScaffold(
    currentUser: com.example.medarchive.domain.models.User?,
    documents: List<Document>,
    selectedCategoryType: String?,
    onCategorySelected: (String?) -> Unit,
    onShowAll: () -> Unit,
    onAddClick: () -> Unit,
    onDocumentClick: (Document) -> Unit,
    onDeleteDocument: (Document) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        TopBar(currentUser = currentUser, onAddClick = onAddClick)
        Spacer(modifier = Modifier.height(24.dp))
        SearchBarSection()
        Spacer(modifier = Modifier.height(24.dp))
        CategoriesSection(
            selectedType = selectedCategoryType,
            onCategorySelected = onCategorySelected
        )
        Spacer(modifier = Modifier.height(24.dp))
        RecentDocumentsHeader(onShowAll = onShowAll)
        Spacer(modifier = Modifier.height(12.dp))
        DocumentsList(documents = documents, onDocumentClick = onDocumentClick,  onDeleteDocument = onDeleteDocument)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==================== КОМПОНЕНТЫ ВЕРХНЕЙ ЧАСТИ ====================
@Composable
private fun TopBar(
    currentUser: com.example.medarchive.domain.models.User?,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Архив",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = LettersAndIcons
            )
        }
        AddButton(onClick = onAddClick)
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(LightSubMainColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Добавить документ",
            tint = LettersAndIcons,
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarSection() {
    SearchBar(
        query = "",
        onQueryChange = {},
        onSearch = {},
        active = false,
        onActiveChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        placeholder = {
            Text(
                "Поиск документов...",
                fontFamily = PoppinsFontFamily,
                color = LettersAndIcons
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = LettersAndIcons
            )
        },
        colors = SearchBarDefaults.colors(
            containerColor = LightSubMainColor.copy(alpha = 0.8f)
        )
    ) {}
}

// ==================== КАТЕГОРИИ ====================
@Composable
private fun CategoriesSection(
    selectedType: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = remember {
        listOf(
            Category("Анализы", R.drawable.ic_journal, Color(0xFFE1BEE7), "analysis"),
            Category("Врачи", R.drawable.ic_doctor, Color(0xFFBBDEFB), "doctor"),
            Category("Снимки", R.drawable.photo, Color(0xFFC8E6C9), "image"),
            Category("Прочее", R.drawable.info, Color(0xFFC8E6C9), "others")
        )
    }

    Column {
        Text(
            text = "Категории",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = LettersAndIcons
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedType == category.typeKey,
                    onClick = {
                        onCategorySelected(
                            if (selectedType == category.typeKey) null else category.typeKey
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MainColor else LightSubMainColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = null,
                    tint = DarkModeBar,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) DarkModeBar else LettersAndIcons
            )
        }
    }
}

// ==================== ЗАГОЛОВОК НЕДАВНИХ ДОКУМЕНТОВ ====================
@Composable
private fun RecentDocumentsHeader(onShowAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Список документов",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = LettersAndIcons
        )
        TextButton(onClick = onShowAll) {
            Text(
                "Показать все",
                fontFamily = PoppinsFontFamily,
                color = DarkModeBar
            )
        }
    }
}

// ==================== СЕТКА ДОКУМЕНТОВ ====================
@Composable
private fun DocumentsList(
    documents: List<Document>,
    onDocumentClick: (Document) -> Unit,
    onDeleteDocument: (Document) -> Unit
) {
    if (documents.isEmpty()) {
        EmptyDocumentsPlaceholder()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(documents, key = { it.localId }) { doc ->
                DocumentListItem(
                    document = doc,
                    onClick = { onDocumentClick(doc) },
                    onDelete = { onDeleteDocument(doc) }
                )
            }
        }
    }
}

@Composable
private fun EmptyDocumentsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_journal),
                contentDescription = null,
                tint = LettersAndIcons.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Пока данная категория пустая",
                fontFamily = PoppinsFontFamily,
                color = LettersAndIcons.copy(alpha = 0.8f)
            )
            Text(
                text = "Нажмите + чтобы добавить",
                fontFamily = PoppinsFontFamily,
                fontSize = 14.sp,
                color = LettersAndIcons.copy(alpha = 0.7f)
            )
        }
    }
}

// ==================== КАРТОЧКА ДОКУМЕНТА ====================
@Composable
private fun DocumentListItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSubMainColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                AsyncImage(
                    model = document.imagePath?.let { File(it) },
                    contentDescription = null,
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title ?: "Без названия",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = LettersAndIcons
                    )
                    Text(
                        text = document.content ?: "Без замечаний",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = LettersAndIcons
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = document.createdAt.toString().substringBefore('T'),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = LettersAndIcons.copy(alpha = 0.6f)
                    )
                }
            }

            // Иконка удаления в правом верхнем углу
            if (onDelete != null) {
                Box(modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
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

// ==================== ДИАЛОГИ ====================
@Composable
private fun SourceChoiceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить документ") },
        text = { Text("Выберите источник изображения") },
        confirmButton = {
            TextButton(onClick = onCameraClick) {
                Text("Камера")
            }
        },
        dismissButton = {
            TextButton(onClick = onGalleryClick) {
                Text("Галерея")
            }
        }
    )
}

@Composable
private fun AddDocumentMetadataDialog(
    initialTitle: String? = null,
    initialType: String = "analysis",
    initialNotes: String? = null,
    isTypeLocked: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (title: String, type: String, notes: String?) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue(initialTitle ?: "")) }
    var selectedType by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf(TextFieldValue(initialNotes ?: "")) }
    val types = listOf("analysis" to "Анализ", "doctor" to "Врач", "image" to "Снимок", "others" to "Прочее")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Информация о документе") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Категория", fontWeight = FontWeight.Medium)
                    if (isTypeLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(определено автоматически)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
                Column {
                    types.forEach { (type, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = if (!isTypeLocked) {
                                    { selectedType = type }
                                } else null,
                                enabled = !isTypeLocked
                            )
                            Text(label)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        title.text.ifEmpty { "Документ ${Date()}" },
                        selectedType,
                        notes.text.ifEmpty { null }
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// ==================== УТИЛИТЫ ====================
private fun createTempImageFile(context: Context): File? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return try {
        File.createTempFile(imageFileName, ".jpg", storageDir)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

private fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val fileName = "doc_${System.currentTimeMillis()}.jpg"
        val destinationFile = File(context.filesDir, fileName)
        destinationFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        destinationFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}