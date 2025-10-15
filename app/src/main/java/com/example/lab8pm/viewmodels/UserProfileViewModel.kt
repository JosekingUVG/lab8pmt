package com.example.lab8pm.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lab8pm.data.models.UserProfile

/**
 * ViewModel para la pantalla de perfil de usuario
 * Maneja el estado del perfil (nombre y foto)
 */
class UserProfileViewModel : ViewModel() {

    // Estado del perfil del usuario
    var userProfile by mutableStateOf(UserProfile())
        private set

    /**
     * Actualiza el perfil del usuario
     * @param name nombre del usuario
     * @param photoUri URI de la foto de perfil
     */
    fun updateProfile(name: String, photoUri: Uri?) {
        userProfile = userProfile.copy(name = name, photoUri = photoUri)
    }
}