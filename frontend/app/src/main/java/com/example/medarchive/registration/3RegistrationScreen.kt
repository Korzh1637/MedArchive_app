package com.example.medarchive.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.DateVisualTransformation
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.ui.theme.DarkModeBar
import com.example.medarchive.ui.theme.LettersAndIcons
import com.example.medarchive.ui.theme.MainColor
import com.example.medarchive.ui.theme.PoppinsFontFamily
import com.example.medarchive.ui.theme.RegMenu


@Composable
fun RegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birth_day by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var conf_password by remember { mutableStateOf("") }
    var passwordVisible1 by remember { mutableStateOf(false) }
    var passwordVisible2 by remember { mutableStateOf(false) }

    val person = painterResource(id = R.drawable.ic_person)
    val lock = painterResource(id = R.drawable.ic_lock)
    val visibilityIcon = painterResource(id = R.drawable.ic_visibility_on)
    val visibilityOffIcon = painterResource(id = R.drawable.ic_visibility_off)

    Box(
        modifier = Modifier.fillMaxSize().background(MainColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = "Создать Аккаунт",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = LettersAndIcons,
                modifier = Modifier.padding(top = 40.dp)
            )

            // Логотип
            Image(
                painter = painterResource(id = R.drawable.ic_singup_cat1),
                contentDescription = "Welcome Cat",
                modifier = Modifier.size(140.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(topEnd = 60.dp, topStart = 60.dp), // Скругление углов
                color = RegMenu,     // Цвет карточки (или CardBackground)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Поле имени
                    DataFields(
                        label = "Полное имя",
                        placeholder = "Иван Иваныч",
                        value = name,
                        onValueChange = { name = it },
                        spacerTop = 40,
                        leadingIcon = person,
                    )

                    // Поле Password
                    DataFields(
                        label = "Почта",
                        placeholder = "example@example.com",
                        value = email,
                        onValueChange = { email = it },
                        spacerTop = 18,
                        leadingIcon = person,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Поле дня рождения
                    DataFields(
                        label = "Дата Рождения",
                        placeholder = "DD / MM / YYYY",
                        value = birth_day,
                        onValueChange = {
                            val filtered = it.filter { char -> char.isDigit() || char == '/' }
                            if (filtered.length <= 10) {
                                birth_day = filtered
                            }
                        },
                        spacerTop = 18,
                        leadingIcon = person,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = DateVisualTransformation()
                    )

                    // Поле Password
                    DataFields(
                        label = "Пароль",
                        placeholder = "••••••••",
                        value = password,
                        onValueChange = { password = it },
                        spacerTop = 18,
                        leadingIcon = lock,
                        trailingIcon = if (passwordVisible1) visibilityIcon else visibilityOffIcon,
                        onTrailingIconClick = { passwordVisible1 = !passwordVisible1 },
                        visualTransformation = if (passwordVisible1)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Поле подтверждения Password
                    DataFields(
                        label = "Подтверждение Пароля",
                        placeholder = "••••••••",
                        value = conf_password,
                        onValueChange = { conf_password = it },
                        spacerTop = 18,
                        leadingIcon = lock,
                        trailingIcon = if (passwordVisible2) visibilityIcon else visibilityOffIcon,
                        onTrailingIconClick = { passwordVisible2 = !passwordVisible2 },
                        visualTransformation = if (passwordVisible2)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(Modifier.weight(1f))

                    // Кнопка "Регистрация"
                    Button(
                        onClick = {
                            println("Регистрация")
                            navController.navigate(Screen.Confirm.route)
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(0.48f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColor,
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


                    // "Don't have an account? Sign Up"
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "У вас уже есть аккаунт? ",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = LettersAndIcons.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Войти",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color.Blue,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }

                    Spacer(Modifier.height(25.dp))
                }
            }
        }
    }
}