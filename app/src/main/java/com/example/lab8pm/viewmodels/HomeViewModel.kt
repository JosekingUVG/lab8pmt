package com.example.lab8pm.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab8pm.data.models.PexelsPhoto
import com.example.lab8pm.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeVM"

/**
 * ViewModel para la pantalla principal (HomeScreen)
 * Maneja la búsqueda de fotos, paginación y estados de carga
 */
class HomeViewModel : ViewModel() {

    // Estado de la lista de fotos
    private val _photos = MutableStateFlow<List<PexelsPhoto>>(emptyList())
    val photos: StateFlow<List<PexelsPhoto>> = _photos.asStateFlow()

    // Estado de carga
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Control de paginación
    private var currentPage = 1
    private val perPage = 20
    private var lastQuery: String? = null

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

            try {
                val resp = ApiClient.pexelsApi.searchPhotos(query, currentPage, perPage)
                val list = resp.photos
                Log.d(TAG, "searchPhotos: recibidos=${list.size}")

                _photos.value = list

                if (list.isEmpty()) {
                    _error.value = "No se encontraron resultados para \"$query\""
                }
            } catch (t: Throwable) {
                Log.e(TAG, "searchPhotos: error", t)
                _error.value = t.message ?: "Error desconocido"
                _photos.value = emptyList()
            } finally {
                _loading.value = false
            }
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

            try {
                val resp = ApiClient.pexelsApi.searchPhotos(q, currentPage, perPage)
                val more = resp.photos
                Log.d(TAG, "loadNextPage: recibidos=${more.size}")

                if (more.isNotEmpty()) {
                    _photos.value = _photos.value + more
                } else {
                    // Si no hay más resultados, retroceder el contador
                    currentPage -= 1
                    Log.d(TAG, "loadNextPage: página vacía, no incrementar")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "loadNextPage: error", t)
                _error.value = t.message ?: "Error en paginación"
                currentPage -= 1
            } finally {
                _loading.value = false
            }
        }
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