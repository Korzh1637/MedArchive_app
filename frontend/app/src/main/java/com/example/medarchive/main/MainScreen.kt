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
    var selectedTab by remember { mutableStateOf(0) }
    var chosenElement by remember { mutableStateOf(0) } // для HomeScreen

    MainScaffold(
        navController = navController,
        mainViewModel = mainViewModel,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) {
        when (selectedTab) {
            0 -> HomeScreenContent(
                mainViewModel = mainViewModel,
                chosenElement = chosenElement,
                onChosenElementChange = { chosenElement = it }
            )
            1 -> JournalScreenContent(mainViewModel = mainViewModel)
            2 -> ProfileScreenContent(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
    }
}