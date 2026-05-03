package com.example.medarchive.registration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(navController: NavController, mainViewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible1 by remember { mutableStateOf(false) }
    var passwordVisible2 by remember { mutableStateOf(false) }

    val currentUser by mainViewModel.currentUser.collectAsState()
    val registrationError by mainViewModel.registrationError.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        transitionState.targetState = true
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Registration.route) { inclusive = true }
            }
        }
    }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600, delayMillis = 100))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Создать аккаунт",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = DarkModeBar,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Присоединяйтесь к MedArchive",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = DarkModeBar.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                        slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(600, delayMillis = 300))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(36.dp)),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSubMainColor.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Полное имя
                        AnimatedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Имя",
                            leadingIcon = R.drawable.ic_person,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email
                        AnimatedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Email",
                            leadingIcon = R.drawable.ic_person,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Пароль
                        AnimatedPasswordField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Пароль",
                            leadingIcon = R.drawable.ic_lock,
                            passwordVisible = passwordVisible1,
                            onVisibilityToggle = { passwordVisible1 = !passwordVisible1 },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Подтверждение пароля
                        AnimatedPasswordField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Подтвердите пароль",
                            leadingIcon = R.drawable.ic_lock,
                            passwordVisible = passwordVisible2,
                            onVisibilityToggle = { passwordVisible2 = !passwordVisible2 },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    attemptRegistration(
                                        name, email, password, confirmPassword,
                                        mainViewModel, scope,
                                        onValidationError = { localValidationError = it },
                                        onLoadingChange = { isLoading = it }
                                    )
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Ошибки
                        val errorToShow = registrationError ?: localValidationError
                        AnimatedVisibility(
                            visible = errorToShow != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = errorToShow ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопка "Зарегистрироваться"
                        Button(
                            onClick = {
                                attemptRegistration(
                                    name, email, password, confirmPassword,
                                    mainViewModel, scope,
                                    onValidationError = { localValidationError = it },
                                    onLoadingChange = { isLoading = it }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .alpha(if (isLoading) 0.8f else 1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColor,
                                contentColor = DarkModeBar,
                                disabledContainerColor = MainColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(30.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            enabled = !isLoading && name.isNotBlank() && email.isNotBlank() &&
                                    password.length >= 6 && password == confirmPassword
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = DarkModeBar,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Зарегистрироваться",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Переход на вход
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Уже есть аккаунт? ",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = DarkModeBar.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Войти",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MainColor,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(Screen.Login.route)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun attemptRegistration(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    mainViewModel: MainViewModel,
    scope: CoroutineScope,
    onValidationError: (String?) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val validationError = when {
        name.isBlank() -> "Введите полное имя"
        email.isBlank() -> "Введите email"
        password.length < 6 -> "Пароль должен быть не менее 6 символов"
        password != confirmPassword -> "Пароли не совпадают"
        else -> null
    }

    if (validationError != null) {
        onValidationError(validationError)
        return
    }

    onValidationError(null)
    scope.launch {
        onLoadingChange(true)
        mainViewModel.register(email, password, name)
        onLoadingChange(false)
    }
}