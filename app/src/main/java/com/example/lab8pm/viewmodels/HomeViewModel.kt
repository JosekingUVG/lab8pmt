package com.example.lab8pm.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab8pm.data.local.AppDatabase
import com.example.lab8pm.data.models.PexelsPhoto
import com.example.lab8pm.data.models.SearchHistory
import com.example.lab8pm.data.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeVM"

/**
 * ViewModel para la pantalla principal (HomeScreen)
 * Maneja la búsqueda de fotos, paginación, favoritos y búsquedas recientes
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PhotoRepository(database)
    }

    // Estado de la lista de fotos
    private val _photos = MutableStateFlow<List<PexelsPhoto>>(emptyList())
    val photos: StateFlow<List<PexelsPhoto>> = _photos.asStateFlow()

    // Estado de carga
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Búsquedas recientes
    private val _recentSearches = MutableStateFlow<List<SearchHistory>>(emptyList())
    val recentSearches: StateFlow<List<SearchHistory>> = _recentSearches.asStateFlow()

    // IDs de favoritos para UI
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    // Control de paginación
    private var currentPage = 1
    private val perPage = 20
    private var lastQuery: String? = null

    init {
        // Observar búsquedas recientes
        viewModelScope.launch {
            repository.recentSearches.collect { searches ->
                _recentSearches.value = searches
            }
        }

        // Cargar IDs de favoritos
        loadFavoriteIds()
    }

    /**
     * Busca fotos según el término de búsqueda
     * Reinicia la paginación a la página 1
     */
    fun searchPhotos(query: String) {
        if (query.isBlank()) {
            Log.d(TAG, "searchPhotos: query vacío -> limpio lista")
            clear()
            return
        }

        // Reiniciar búsqueda
        lastQuery = query
        currentPage = 1

        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            Log.d(TAG, "searchPhotos: q='$query' page=$currentPage")

            val result = repository.searchPhotos(query, currentPage, perPage)

            result.onSuccess { list ->
                Log.d(TAG, "searchPhotos: recibidos=${list.size}")
                _photos.value = list

                if (list.isEmpty()) {
                    _error.value = "No se encontraron resultados para \"$query\""
                }
            }.onFailure { error ->
                Log.e(TAG, "searchPhotos: error", error)
                _error.value = error.message ?: "Error desconocido. Verifica tu conexión."
                _photos.value = emptyList()
            }

            _loading.value = false
        }
    }

    /**
     * Carga la siguiente página de resultados
     * Utilizado para scroll infinito
     */
    fun loadNextPage() {
        val q = lastQuery ?: run {
            Log.d(TAG, "loadNextPage: no hay query previa")
            return
        }

        // Evitar llamadas concurrentes
        if (_loading.value) {
            Log.d(TAG, "loadNextPage: ya cargando, se ignora")
            return
        }

        currentPage += 1

        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            Log.d(TAG, "loadNextPage: q='$q' page=$currentPage")

            val result = repository.searchPhotos(q, currentPage, perPage)

            result.onSuccess { more ->
                Log.d(TAG, "loadNextPage: recibidos=${more.size}")

                if (more.isNotEmpty()) {
                    _photos.value = _photos.value + more
                } else {
                    // Si no hay más resultados, retroceder el contador
                    currentPage -= 1
                    Log.d(TAG, "loadNextPage: página vacía, no incrementar")
                }
            }.onFailure { error ->
                Log.e(TAG, "loadNextPage: error", error)
                _error.value = error.message ?: "Error en paginación"
                currentPage -= 1
            }

            _loading.value = false
        }
    }

    // ==================== FAVORITOS ====================

    /**
     * Alternar favorito de una foto
     */
    fun toggleFavorite(photo: PexelsPhoto) {
        viewModelScope.launch {
            repository.toggleFavorite(photo)
            loadFavoriteIds() // Recargar la lista de IDs
        }
    }

    /**
     * Cargar IDs de favoritos
     */
    private fun loadFavoriteIds() {
        viewModelScope.launch {
            repository.allFavorites.collect { favorites ->
                _favoriteIds.value = favorites.map { it.id }.toSet()
            }
        }
    }

    /**
     * Verificar si una foto es favorita
     */
    fun isFavorite(photoId: Int): Boolean {
        return _favoriteIds.value.contains(photoId)
    }

    /**
     * Limpia el estado del ViewModel
     */
    fun clear() {
        lastQuery = null
        currentPage = 1
        _photos.value = emptyList()
        _error.value = null
    }
}