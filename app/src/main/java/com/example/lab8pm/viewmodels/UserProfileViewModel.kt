package com.example.lab8pm.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab8pm.data.local.AppDatabase
import com.example.lab8pm.data.models.User
import com.example.lab8pm.data.models.UserProfile
import com.example.lab8pm.data.repository.PhotoRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de perfil de usuario
 * Maneja el estado del perfil (nombre y foto) con persistencia en Room
 */
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PhotoRepository(database)

        // Cargar perfil al iniciar
        loadUserProfile()
    }

    // Estado del perfil del usuario (para UI)
    var userProfile by mutableStateOf(UserProfile())
        private set

    /**
     * Cargar perfil desde Room
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            repository.userProfile.collect { user ->
                if (user != null) {
                    userProfile = UserProfile(
                        name = user.name ?: "",
                        photoUri = user.photoUri?.let { Uri.parse(it) }
                    )
                }
            }
        }
    }

    /**
     * Actualiza el perfil del usuario y lo guarda en Room
     * @param name nombre del usuario
     * @param photoUri URI de la foto de perfil
     */
    fun updateProfile(name: String, photoUri: Uri?) {
        // Actualizar UI inmediatamente
        userProfile = userProfile.copy(name = name, photoUri = photoUri)

        // Guardar en Room
        viewModelScope.launch {
            repository.saveUserProfile(
                name = name,
                photoUri = photoUri?.toString()
            )
        }
    }
}