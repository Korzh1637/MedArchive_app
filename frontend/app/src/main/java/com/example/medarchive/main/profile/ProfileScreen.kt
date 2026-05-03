package com.example.medarchive.main.profile

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import android.content.ClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    val othersCount = documents.count { it.documentType !in listOf("analysis", "анализ", "doctor", "врач", "image", "снимок") }
    val analyticsCount = documents.count { it.documentType in listOf("analysis", "анализ") }
    val doctorsCount = documents.count { it.documentType in listOf("doctor", "врач") }
    val imagesCount = documents.count { it.documentType in listOf("image", "снимок") }

    var showHelpDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf("") }
    val currentServerUrl = remember { mainViewModel.getBaseUrl() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок
        Text(
            text = "Профиль",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = LettersAndIcons,
        )

        // Аватар и информация пользователя
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

        // Меню профиля
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
//                ProfileMenuItem(R.drawable.ic_person, "Личные данные") { /* ... */ }
//                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LettersAndIcons.copy(alpha = 0.2f))
//                ProfileMenuItem(R.drawable.ic_lock, "Безопасность") { /* ... */ }
//                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LettersAndIcons.copy(alpha = 0.2f))
                ProfileMenuItem(R.drawable.ic_help, "Помощь и поддержка") {
                    showHelpDialog = true
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LettersAndIcons.copy(alpha = 0.2f))
                ProfileMenuItem(icon = R.drawable.ic_server, label = "Настройки сервера") {
                    serverUrl = currentServerUrl
                    showServerDialog = true
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Статистика
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = RegMenu.copy(alpha = 0.9f)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Ваша статистика", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = LettersAndIcons)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Анализы", analyticsCount.toString(), R.drawable.ic_journal)
                    StatItem("Врачи", doctorsCount.toString(), R.drawable.ic_doctor)
                    StatItem("Снимки", imagesCount.toString(), R.drawable.photo)
                    StatItem("Прочее", othersCount.toString(), R.drawable.info)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка выхода
        Button(
            onClick = {
                mainViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373), contentColor = LettersAndIcons),
            shape = RoundedCornerShape(26.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Выйти", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = LettersAndIcons)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Диалог ввода адреса сервера
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Адрес сервера") },
            text = {
                Column {
                    Text("Введите URL вашего сервера", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("http://...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (serverUrl.isNotBlank()) {
                        mainViewModel.updateBaseUrl(serverUrl)
                        showServerDialog = false
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showHelpDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Помощь и поддержка") },
            text = {
                Column {
                    Text("По всем возникшим вопросам пишите сюда в Telegram:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "@meowmeowmurrrr",
                        fontSize = 20.sp,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Копируем текст в буфер обмена
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("telegram_contact", "@meowmeowmurrrr")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                    showHelpDialog = false
                }) {
                    Text("Скопировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ====== Общие пункты меню ======
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = LettersAndIcons.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ====== Элемент статистики ======
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