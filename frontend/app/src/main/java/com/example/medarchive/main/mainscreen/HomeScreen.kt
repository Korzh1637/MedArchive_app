package com.example.medarchive.main.mainscreen

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.medarchive.R
import com.example.medarchive.domain.models.Document
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import android.os.Environment
import java.io.IOException
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by mainViewModel.currentUser.collectAsState()
    val documents by mainViewModel.documents.collectAsState()

    var showSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Лончер для камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                saveImageAndCreateDocument(context, uri, mainViewModel, currentUser?.id)
            }
        }
    }

    // Лончер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveImageAndCreateDocument(context, it, mainViewModel, currentUser?.id)
        }
    }

    // Лончер для разрешения камеры
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = createTempImageFile(context)
            file?.let {
                tempImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    it
                )
                cameraLauncher.launch(tempImageUri!!)
            }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            mainViewModel.loadDocuments(it.id)
        }
    }

    // Диалог выбора источника
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Добавить документ") },
            text = { Text("Выберите источник изображения") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Камера")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Галерея")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: уведомления */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Уведомления",
                    tint = LettersAndIcons,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Добрый день,",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 16.sp,
                    color = LettersAndIcons.copy(alpha = 0.8f)
                )
                Text(
                    text = currentUser?.fullName ?: "Гость",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = LettersAndIcons
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightSubMainColor)
                    .clickable { showSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add elem",
                    tint = DarkModeBar,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Категории",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = LettersAndIcons
        )
        Spacer(modifier = Modifier.height(12.dp))

        val categories = listOf(
            Category("Анализы", R.drawable.ic_journal, Color(0xFFE1BEE7)),
            Category("Врачи", R.drawable.ic_doctor, Color(0xFFBBDEFB)),
            Category("Снимки", R.drawable.ic_add, Color(0xFFC8E6C9)),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    onClick = { /* фильтр по категории */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Недавние документы",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = LettersAndIcons
            )
            TextButton(onClick = { /* показать все */ }) {
                Text(
                    "Все",
                    fontFamily = PoppinsFontFamily,
                    color = DarkModeBar
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_journal),
                        contentDescription = null,
                        tint = DarkModeBar.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Пока нет документов",
                        fontFamily = PoppinsFontFamily,
                        color = DarkModeBar.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Нажмите + чтобы добавить",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = DarkModeBar.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(documents, key = { it.localId }) { doc ->
                    DocumentGridCard(document = doc)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

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

private fun saveImageAndCreateDocument(
    context: Context,
    imageUri: Uri,
    mainViewModel: MainViewModel,
    userId: Int?
) {
    userId ?: return
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
        val fileName = "doc_${System.currentTimeMillis()}.jpg"
        val destinationFile = File(context.filesDir, fileName)
        destinationFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        mainViewModel.createDocument(
            imagePath = destinationFile.absolutePath,
            title = "Новый документ",
            type = "analysis",
            text = null
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class Category(
    val name: String,
    val iconRes: Int,
    val color: Color
)

@Composable
fun CategoryChip(
    category: Category,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick),
        color = LightSubMainColor,
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
                color = LettersAndIcons
            )
        }
    }
}

@Composable
fun DocumentGridCard(document: Document) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { /* переход к деталям */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSubMainColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Иконка типа
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MainColor, MainColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when (document.documentType) {
                            "analysis", "анализ" -> R.drawable.ic_journal
                            "doctor", "врач" -> R.drawable.ic_doctor
                            else -> R.drawable.ic_add
                        }
                    ),
                    contentDescription = null,
                    tint = DarkModeBar,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = document.title ?: "Без названия",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = DarkModeBar,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = document.createdAt.toString().substringBefore('T'),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = DarkModeBar.copy(alpha = 0.6f)
            )
        }
    }
}