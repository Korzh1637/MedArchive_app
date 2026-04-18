package com.example.medarchive.registration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, mainViewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val currentUser by mainViewModel.currentUser.collectAsState()
    val loginError by mainViewModel.loginError.collectAsState()

    // Локальное состояние загрузки (можно заменить на mainViewModel.isLoading)
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Анимация появления
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        transitionState.targetState = true
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
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
        // Декоративные размытые круги на фоне
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(animationSpec = tween(1500, delayMillis = 500)) +
                    slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600, delayMillis = 100))
        ) { AnimatedBackgroundCircles() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Анимированный заголовок
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 300)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(1000, delayMillis = 100))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "С возвращением!",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = DarkModeBar,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Рады видеть вас снова",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = DarkModeBar.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Карточка с полями ввода (анимированное появление с задержкой)
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
                        // Email поле с анимацией границы при фокусе
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Пароль поле
                        AnimatedPasswordField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Пароль",
                            leadingIcon = R.drawable.ic_lock,
                            passwordVisible = passwordVisible,
                            onVisibilityToggle = { passwordVisible = !passwordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        scope.launch {
                                            isLoading = true
                                            mainViewModel.login(email, password)
                                            isLoading = false
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Ошибка
                        AnimatedVisibility(
                            visible = loginError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = loginError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Кнопка Войти с состоянием загрузки
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    mainViewModel.login(email, password)
                                    isLoading = false
                                }
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
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = DarkModeBar,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Войти",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ссылка "Забыли пароль?"
                        Text(
                            text = "Забыли пароль?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MainColor,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null) {
                                navController.navigate(Screen.ResetPassword.route)
                            }
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Кнопка регистрации внизу (анимированное появление)
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) +
                        slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(600, delayMillis = 500))
            ) {
                Row(
                    modifier = Modifier.padding(top = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Нет аккаунта? ",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DarkModeBar.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Зарегистрироваться",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MainColor,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ){
                            navController.navigate(Screen.Registration.route)
                        }
                    )
                }
            }
        }
    }
}

// Вспомогательные компоненты для полей с анимированной границей
@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: Int,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MainColor else LettersAndIcons.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "borderColor"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedLeadingIconColor = borderColor,
            unfocusedLeadingIconColor = borderColor
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = borderColor
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        interactionSource = interactionSource
    )
}

@Composable
fun AnimatedPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: Int,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MainColor else LettersAndIcons.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "borderColor"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedLeadingIconColor = borderColor,
            unfocusedLeadingIconColor = borderColor,
            focusedTrailingIconColor = borderColor,
            unfocusedTrailingIconColor = borderColor
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = borderColor
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    painter = painterResource(
                        id = if (passwordVisible) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off
                    ),
                    contentDescription = null,
                    Modifier.size(30.dp)
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else (visualTransformation as? PasswordVisualTransformation) ?: PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        interactionSource = interactionSource
    )
}