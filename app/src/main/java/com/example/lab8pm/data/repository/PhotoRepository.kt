package com.example.lab8pm.data.repository

import android.util.Log
import com.example.lab8pm.data.local.AppDatabase
import com.example.lab8pm.data.models.*
import com.example.lab8pm.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "PhotoRepository"

/**
 * Repository que maneja la lógica de datos:
 * - Cache de fotos por query
 * - Favoritos persistentes
 * - Historial de búsquedas
 * - Perfil de usuario
 */
class PhotoRepository(private val database: AppDatabase) {

    private val favoritePhotoDao = database.favoritePhotoDao()
    private val searchHistoryDao = database.searchHistoryDao()
    private val userDao = database.userDao()

    // Flows observables
    val allFavorites: Flow<List<FavoritePhoto>> = favoritePhotoDao.getAllFavorites()
    val recentSearches: Flow<List<SearchHistory>> = searchHistoryDao.getRecentSearches()
    val userProfile: Flow<User?> = userDao.getUserProfileFlow()

    /**
     * Buscar fotos (primero intenta red, si falla lee cache)
     */
    suspend fun searchPhotos(query: String, page: Int, perPage: Int): Result<List<PexelsPhoto>> {
        return withContext(Dispatchers.IO) {
            try {
                // Intentar búsqueda por red
                Log.d(TAG, "Buscando en red: query=$query, page=$page")
                val response = ApiClient.pexelsApi.searchPhotos(query, page, perPage)
                val photos = response.photos

                // Guardar búsqueda en historial
                saveSearchQuery(query)

                Log.d(TAG, "Fotos obtenidas de red: ${photos.size}")
                Result.success(photos)

            } catch (e: IOException) {
                // Sin conexión -> intentar leer cache (favoritos)
                Log.e(TAG, "Error de red, intentando cache", e)

                // Por ahora devolvemos lista vacía
                // En una implementación completa, podrías tener una tabla de cache separada
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Error en búsqueda", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener foto por ID
     */
    suspend fun getPhotoById(photoId: Int): Result<PexelsPhoto> {
        return withContext(Dispatchers.IO) {
            try {
                // Primero buscar en favoritos
                val favorite = favoritePhotoDao.getFavoriteById(photoId)
                if (favorite != null) {
                    Log.d(TAG, "Foto encontrada en favoritos")
                    return@withContext Result.success(favorite.toPexelsPhoto())
                }

                // Si no está en favoritos, buscar en red
                Log.d(TAG, "Buscando foto en red: id=$photoId")
                val photo = ApiClient.pexelsApi.getPhotoById(photoId)
                Result.success(photo)

            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener foto", e)
                Result.failure(e)
            }
        }
    }



    // ==================== FAVORITOS ====================

    /**
     * Agregar o quitar favorito
     */
    suspend fun toggleFavorite(photo: PexelsPhoto) {
        withContext(Dispatchers.IO) {
            val isFav = favoritePhotoDao.isFavorite(photo.id)
            if (isFav) {
                Log.d(TAG, "Eliminando favorito: ${photo.id}")
                favoritePhotoDao.deleteFavoriteById(photo.id)
            } else {
                Log.d(TAG, "Agregando favorito: ${photo.id}")
                favoritePhotoDao.insertFavorite(photo.toFavoritePhoto())
            }
        }
    }

    /**
     * Verificar si una foto es favorita
     */
    suspend fun isFavorite(photoId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            favoritePhotoDao.isFavorite(photoId)
        }
    }

    /**
     * Obtener todos los favoritos como PexelsPhoto
     */
    suspend fun getAllFavoritesAsList(): List<PexelsPhoto> {
        return withContext(Dispatchers.IO) {
            favoritePhotoDao.getAllFavorites()
        }.let { flow ->
            // Para obtener valor actual del Flow
            var result = emptyList<FavoritePhoto>()
            flow.collect { result = it }
            result.map { it.toPexelsPhoto() }
        }
    }

    // ==================== HISTORIAL ====================

    /**
     * Guardar búsqueda en historial
     */
    private suspend fun saveSearchQuery(query: String) {
        if (query.isBlank()) return

        withContext(Dispatchers.IO) {
            // Normalizar query (minúsculas, sin espacios extra)
            val normalizedQuery = query.trim().lowercase()

            // Eliminar búsqueda anterior si existe
            searchHistoryDao.deleteSearchByQuery(normalizedQuery)

            // Insertar nueva búsqueda
            searchHistoryDao.insertSearch(
                SearchHistory(searchQuery = normalizedQuery)
            )

            // Mantener solo las últimas 10
            searchHistoryDao.keepOnlyRecentSearches()

            Log.d(TAG, "Búsqueda guardada: $normalizedQuery")
        }
    }

    /**
     * Buscar en historial
     */
    suspend fun searchInHistory(query: String): List<SearchHistory> {
        return withContext(Dispatchers.IO) {
            searchHistoryDao.searchInHistory(query.lowercase())
        }
    }

    /**
     * Limpiar historial
     */
    suspend fun clearSearchHistory() {
        withContext(Dispatchers.IO) {
            searchHistoryDao.clearHistory()
            Log.d(TAG, "Historial limpiado")
        }
    }

    // ==================== PERFIL DE USUARIO ====================

    /**
     * Guardar o actualizar perfil de usuario
     */
    suspend fun saveUserProfile(name: String, photoUri: String?) {
        withContext(Dispatchers.IO) {
            val user = User(
                uid = 1,
                name = name,
                photoUri = photoUri
            )
            userDao.insertUserProfile(user)
            Log.d(TAG, "Perfil guardado: name=$name")
        }
    }

    /**
     * Obtener perfil de usuario
     */
    suspend fun getUserProfile(): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserProfile()
        }
    }
}