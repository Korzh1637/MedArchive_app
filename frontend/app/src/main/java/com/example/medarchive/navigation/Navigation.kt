package com.example.medarchive.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medarchive.MainScreen
import com.example.medarchive.splash.SplashActivity
import com.example.medarchive.registration.ConfirmScreen
import com.example.medarchive.registration.LoginScreen
import com.example.medarchive.registration.RegLogMainScreen
import com.example.medarchive.registration.RegistrationScreen
import com.example.medarchive.registration.ResetPasswordScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashActivity(navController)
        }

        composable(Screen.RegLogMain.route) {
            RegLogMainScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(navController,)
        }

        composable(Screen.Confirm.route) {
            ConfirmScreen(navController,)
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(navController,)
        }

        composable(Screen.Main.route) {
            MainScreen(navController)
        }
    }
}