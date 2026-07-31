package com.example.elsahra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.elsahra.navigation.AppNavigation
import com.example.elsahra.ui.screens.SettingsViewModel
import com.example.elsahra.ui.theme.ElSahraTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            !viewModel.uiState.value.isInitialized
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsState()
            
            // If the user hasn't set a preference, use the system setting.
            val darkTheme = uiState.isDarkMode ?: isSystemInDarkTheme()

            ElSahraTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
