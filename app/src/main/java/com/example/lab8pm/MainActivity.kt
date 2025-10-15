package com.example.lab8pm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.lab8pm.navigation.AppNavHost
import com.example.lab8pm.ui.theme.Lab8PmTheme

/**
 * Activity principal de la aplicación
 * Configura la navegación y el tema
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab8PmTheme {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}