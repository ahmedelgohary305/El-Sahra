package com.example.elsahra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.elsahra.navigation.AppNavigation
import com.example.elsahra.ui.theme.ElSahraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            ElSahraTheme {
                // Removed the outer Scaffold to ensure screens handle their own insets
                // consistently through their own Scaffolds and TopAppBars.
                AppNavigation(navController = navController)
            }
        }
    }
}
