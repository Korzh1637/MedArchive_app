package com.example.medarchive.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun ConfirmScreen(navController: NavController) {
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(MainColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = "Подтверждение",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = LettersAndIcons,
                modifier = Modifier.padding(top = 40.dp)
            )

            // Логотип
            Image(
                painter = painterResource(id = R.drawable.ic_confirm_cat1),
                contentDescription = "Verification Cat",
                modifier = Modifier.size(140.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(topEnd = 60.dp, topStart = 60.dp),
                color = RegMenu,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(40.dp))

                    Text(
                        text = "Введите Секретный Код",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = LettersAndIcons,
                        modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
                    )

                    Spacer(Modifier.height(50.dp))

                    SimpleOtpInput(
                        code = code,
                        onCodeChange = { code = it }
                    )

                    Spacer(Modifier.height(50.dp))

                    // Кнопка "Подтвердить"
                    Button(
                        onClick = {
                            println("Подтвердить")

                            // navController.navigate(Screen.WaiterConf.route)
                        },
                        modifier = Modifier
                            .padding(top = 50.dp)
                            .fillMaxWidth(0.55f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColor,
                            contentColor = LettersAndIcons
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text(
                            text = "Подтвердить",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = DarkModeBar
                        )
                    }

                    // Кнопка "Отправить снова"
                    Button(
                        onClick = {
                            println("Отправить снова")
                            navController.navigate(Screen.Registration.route)
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(0.55f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightSubMainColor,
                            contentColor = LettersAndIcons
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text(
                            text = "Отправить снова",
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


@Composable
fun SimpleOtpInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    // Скрытое поле для ввода
    androidx.compose.foundation.text.BasicTextField(
        value = code,
        onValueChange = { newValue ->
            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                onCodeChange(newValue)
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .focusRequester(focusRequester),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        maxLines = 1,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 2.dp,
                                color = MainColor,
                                shape = CircleShape
                            )
                            .background(
                                color = LightSubMainColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < code.length) {
                            Text(
                                text = code[index].toString(),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = LettersAndIcons
                            )
                        }
                    }
                }
            }
        }
    )

    // Автофокус при появлении
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
