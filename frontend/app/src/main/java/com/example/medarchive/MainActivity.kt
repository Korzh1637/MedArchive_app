package com.example.medarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medarchive.data.local.DatabaseRepository
import com.example.medarchive.navigation.AppNavigation
import com.example.medarchive.presentation.viewmodels.MainViewModel
import com.example.medarchive.presentation.viewmodels.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = DatabaseRepository(applicationContext)
        viewModelFactory = MainViewModelFactory(application, repository)

        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
            AppNavigation(mainViewModel = mainViewModel)
        }
    }
}