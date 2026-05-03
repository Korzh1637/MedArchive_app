package com.example.medarchive.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.ui.theme.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen

@Composable
fun RegLogMainScreen(navController: NavController) {
    // Анимация появления элементов
    val infiniteTransition1 = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition1.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    val transitionState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        transitionState.targetState = true
    }
    val infiniteTransition2 = rememberInfiniteTransition(label = "tint")
    val tintColor by infiniteTransition2.animateColor(
        initialValue = DarkModeBar,
        targetValue = LettersAndIcons,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tintColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MainColor,
                        RegMenu
                    )
                )
            )
    ) {
        BackgroundCircles()
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(600, delayMillis = 300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.scale(scale))
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_dark),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(16.dp),
                        tint = tintColor
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок с градиентом
                Text(
                    text = "MedArchive",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp,
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(DarkModeBar, LettersAndIcons)
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ваше здоровье под контролем",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = LettersAndIcons.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Карточка с кнопками
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightSubMainColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Кнопка "Войти"
                        Button(
                            onClick = { navController.navigate(Screen.Login.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColor,
                                contentColor = LettersAndIcons
                            ),
                            shape = RoundedCornerShape(30.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Войти",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Кнопка "Регистрация"
                        Button(
                            onClick = { navController.navigate(Screen.Registration.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LettersAndIcons,
                                contentColor = MainColor
                            ),
                            shape = RoundedCornerShape(30.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Создать аккаунт",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ссылка "Забыли пароль?"
                        Text(
                            text = "Забыли пароль?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MainColor,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(Screen.ResetPassword.route)
                            }.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Текст с информацией
                Text(
                    text = "Войдите или зарегистрируйтесь,\nчтобы получить доступ к функциям",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = LettersAndIcons.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}