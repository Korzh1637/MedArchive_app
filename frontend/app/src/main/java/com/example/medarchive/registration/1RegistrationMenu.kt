package com.example.medarchive.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen

@Composable
fun RegLogMainScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RegMenu),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип
            Image(
                painter = painterResource(id = R.drawable.ic_logo_dark),
                contentDescription = "MedArchive Logo",
                modifier = Modifier.size(200.dp)
            )

            // Заголовок
            Text(
                text = "MedArchive",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 52.sp,
                color = DarkModeBar
            )

            // Кнопка "Войти"
            Button(
                onClick = {
                    println("Войти")
                    // Переход на экран входа
                    navController.navigate(Screen.Login.route)
                },
                modifier = Modifier
                    .padding(top = 28.dp)
                    .fillMaxWidth(0.48f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColor,
                    contentColor = LettersAndIcons
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Войти",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = DarkModeBar
                )
            }

            // Кнопка "Регистрация"
            Button(
                onClick = {
                    println("Регистрация")
                    // Переход на экран регистрации
                    navController.navigate(Screen.Registration.route)
                },
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.48f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightSubMainColor,
                    contentColor = LettersAndIcons
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Регистрация",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = DarkModeBar
                )
            }

            // Текст "Забыли пароль?"
            Text(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable {
                        println("Забыли пароль?")
                        // Переход на восстановление пароля
                        navController.navigate(Screen.ResetPassword.route)
                    },
                text = "Забыли пароль?",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DarkModeBar,
            )
        }
    }
}


@Composable
fun DataFields(
    label: String,                    // Название поля (Username, Password)
    placeholder: String,              // Текст-подсказка
    value: String,                    // Текущее значение (из state)
    onValueChange: (String) -> Unit,  // Callback для обновления state
    spacerTop: Int = 0,               // Отступ сверху в dp
    leadingIcon: Painter? = null,     // Иконка слева (опционально)
    trailingIcon: Painter? = null,    // Иконка справа (опционально)
    onTrailingIconClick: (() -> Unit)? = null, // Действие при клике на правую иконку
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // Тип клавиатуры
    visualTransformation: VisualTransformation = VisualTransformation.None, // Маска ввода (пароль)
    modifier: Modifier = Modifier     // Дополнительные модификаторы
) {

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.background(RegMenu)
    ) {
        // Отступ сверху если нужен
        if (spacerTop > 0) {
            Spacer(Modifier.height(spacerTop.dp))
        }

        // Label поля
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = LettersAndIcons,

            modifier = Modifier.padding(bottom = 8.dp, start = 20.dp),
        )

        // Поле ввода
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth(0.85f)
                .height(55.dp),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightSubMainColor,    // Фон при фокусе
                unfocusedContainerColor = LightSubMainColor,  // Фон без фокуса
            ),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LettersAndIcons.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                leadingIcon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = LettersAndIcons.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                trailingIcon?.let { icon ->
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = LettersAndIcons.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions
        )
    }
}