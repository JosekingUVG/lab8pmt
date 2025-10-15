package com.example.lab8pm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lab8pm.ui.screens.DetailsScreen
import com.example.lab8pm.ui.screens.HomeScreen
import com.example.lab8pm.ui.screens.ProfileScreen

/**
 * Configuración de navegación de la aplicación
 * Define las rutas y las pantallas correspondientes
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Pantalla principal con la lista de fotos
        composable("home") {
            HomeScreen(navController)
        }

        // Pantalla de detalles de una foto específica
        composable("details/{photoId}") { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            DetailsScreen(
                photoId = photoId,
                navController = navController
            )
        }

        // Pantalla de perfil de usuario
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}