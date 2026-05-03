package com.example.medarchive.main

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.medarchive.main.journal.JournalScreenContent
import com.example.medarchive.main.mainscreen.HomeScreenContent
import com.example.medarchive.main.profile.ProfileScreenContent
import com.example.medarchive.presentation.viewmodels.MainViewModel

@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    // Читаем сохранённый номер вкладки, если он есть, иначе 1
    var selectedTab by remember {
        val saved = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Int>("selectedTab")
        mutableIntStateOf(saved ?: 1)
    }

    // Сбрасываем флаг, чтобы следующий возврат не приводил к переключению
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("selectedTab")
    }

    MainScaffold(
        navController = navController,
        mainViewModel = mainViewModel,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) {
        when (selectedTab) {
            0 -> JournalScreenContent(navController = navController, mainViewModel = mainViewModel)
            1 -> HomeScreenContent(mainViewModel = mainViewModel)
            2 -> ProfileScreenContent(navController = navController, mainViewModel = mainViewModel)
        }
    }
}