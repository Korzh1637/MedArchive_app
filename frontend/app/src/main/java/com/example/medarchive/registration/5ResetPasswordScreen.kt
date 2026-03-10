package com.example.medarchive.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medarchive.R
import com.example.medarchive.navigation.Screen
import com.example.medarchive.ui.theme.DarkModeBar
import com.example.medarchive.ui.theme.LettersAndIcons
import com.example.medarchive.ui.theme.LightSubMainColor
import com.example.medarchive.ui.theme.MainColor
import com.example.medarchive.ui.theme.PoppinsFontFamily
import com.example.medarchive.ui.theme.RegMenu


@Composable
fun ResetPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    val lock = painterResource(id = R.drawable.ic_lock)
    val person = painterResource(id = R.drawable.ic_person)
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
                text = "Восстановление Пароля",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = LettersAndIcons,
                modifier = Modifier.padding(top = 40.dp)
            )

            // Логотип
            Image(
                painter = painterResource(id = R.drawable.ic_signin_cat1),
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(25.dp)
                    ) {

                        Spacer(Modifier.height(15.dp))

                        Text(
                            text = "Восстановление Пароля?",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = LettersAndIcons,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        // "Забыли пароль?" под кнопкой
                        Text(
                            text = "Вы можете изменить пароль, введя адрес электронной почты, к которому привязана ваша учетная запись.",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = LettersAndIcons,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Поле Email
                    DataFields(
                        label = "Электронная Почта",
                        placeholder = "example@example.com",
                        value = email,
                        onValueChange = { email = it },
                        spacerTop = 20, // Первый элемент - отступ не нужен
                        leadingIcon = person,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Кнопка "Далее"
                    Button(
                        onClick = {
                            println("Далее")
                            // navController.navigate(Screen.Main.route)
                        },
                        modifier = Modifier
                            .padding(top = 50.dp)
                            .fillMaxWidth(0.48f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColor,
                            contentColor = LettersAndIcons
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text(
                            text = "Далее",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = DarkModeBar
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Кнопка "Регистрация"
                    Button(
                        onClick = {
                            println("Регистрация")
                            navController.navigate(Screen.Registration.route)
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
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
                }
            }
        }
    }
}
