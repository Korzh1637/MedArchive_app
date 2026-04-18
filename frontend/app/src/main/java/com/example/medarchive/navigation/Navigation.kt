package com.example.medarchive.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medarchive.main.MainScreen
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.registration.ConfirmScreen
import com.example.medarchive.registration.LoginScreen
import com.example.medarchive.registration.RegLogMainScreen
import com.example.medarchive.registration.RegistrationScreen
import com.example.medarchive.registration.ResetNewPasswordScreen
import com.example.medarchive.registration.ResetPasswordScreen
import com.example.medarchive.splash.SplashActivity

@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
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
            LoginScreen(navController, mainViewModel)
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(navController, mainViewModel)
        }
        composable(Screen.Confirm.route) {
            ConfirmScreen(navController)
        }
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(navController, mainViewModel)
        }
        composable(
            route = Screen.ContinueResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetNewPasswordScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                email = email
            )
        }
        composable(Screen.Main.route) {
            MainScreen(navController, mainViewModel)
        }
    }
}