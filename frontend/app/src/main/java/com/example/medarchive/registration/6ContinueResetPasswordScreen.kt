package com.example.medarchive.registration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ResetNewPasswordScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    email: String // передаётся из предыдущего экрана или из ViewModel
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val resetNewPasswordError by mainViewModel.resetNewPasswordError.collectAsState()
    val resetNewPasswordSuccess by mainViewModel.resetNewPasswordSuccess.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        transitionState.targetState = true
    }

    // Навигация при успешной смене пароля
    LaunchedEffect(resetNewPasswordSuccess) {
        if (resetNewPasswordSuccess) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.ResetPassword.route) { inclusive = true }
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
        // Декоративные круги
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(animationSpec = tween(1500, delayMillis = 500)) +
                    slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600, delayMillis = 100))
        ) { AnimatedBackgroundCircles() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600, delayMillis = 100))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Новый пароль",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = DarkModeBar,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Придумайте надёжный пароль",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = DarkModeBar.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                        // Новый пароль
                        AnimatedPasswordField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            placeholder = "Новый пароль",
                            leadingIcon = R.drawable.ic_lock,
                            passwordVisible = newPasswordVisible,
                            onVisibilityToggle = { newPasswordVisible = !newPasswordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Подтверждение пароля
                        AnimatedPasswordField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Подтвердите пароль",
                            leadingIcon = R.drawable.ic_lock,
                            passwordVisible = confirmPasswordVisible,
                            onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    attemptPasswordReset(
                                        newPassword, confirmPassword, email,
                                        mainViewModel, scope,
                                        onValidationError = { localValidationError = it },
                                        onLoadingChange = { isLoading = it }
                                    )
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Ошибки
                        val errorToShow = resetNewPasswordError ?: localValidationError
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

                        Spacer(modifier = Modifier.height(28.dp))

                        // Кнопка "Сохранить пароль"
                        Button(
                            onClick = {
                                attemptPasswordReset(
                                    newPassword, confirmPassword, email,
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
                            enabled = !isLoading && newPassword.length >= 6 && newPassword == confirmPassword
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = DarkModeBar,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Сохранить пароль",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ссылка "Вернуться ко входу"
                        Text(
                            text = "Вернуться ко входу",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MainColor,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

private fun attemptPasswordReset(
    newPassword: String,
    confirmPassword: String,
    email: String,
    mainViewModel: MainViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onValidationError: (String?) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val validationError = when {
        newPassword.length < 6 -> "Пароль должен быть не менее 6 символов"
        newPassword != confirmPassword -> "Пароли не совпадают"
        else -> null
    }

    if (validationError != null) {
        onValidationError(validationError)
        return
    }

    onValidationError(null)
    scope.launch {
        onLoadingChange(true)
        mainViewModel.resetPassword(email, newPassword)
        onLoadingChange(false)
    }
}