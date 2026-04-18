package com.example.medarchive.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object RegLogMain : Screen("reg_log")
    data object Login : Screen("login")
    data object Registration : Screen("registration")
    data object Confirm : Screen("confirm")
    data object ResetPassword : Screen("reset_password")
    data object ContinueResetPassword : Screen("continue_reset_password/{email}") {
        fun createRoute(email: String) = "continue_reset_password/$email"
    }
    data object Main : Screen("main")
}