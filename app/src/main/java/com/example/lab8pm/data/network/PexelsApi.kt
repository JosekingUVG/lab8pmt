package com.example.lab8pm.data.network

import com.example.lab8pm.data.models.PexelsPhoto
import com.example.lab8pm.data.models.PexelsResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface de Retrofit para consumir la API de Pexels
 * Documentación: https://www.pexels.com/api/documentation/
 */
interface PexelsApi {

    /**
     * Buscar fotos por palabra clave
     * @param query término de búsqueda
     * @param page número de página (default: 1)
     * @param perPage cantidad de fotos por página (default: 20, max: 80)
     */
    @Headers("Authorization: GhIaofVN0wdkOvbd6D3WiAc3LUTv68E0Ag3CYgNxVTZ7w7izy4Vq2SUH")
    @GET("v1/search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): PexelsResponse

    /**
     * Obtener una foto específica por su ID
     * @param id identificador único de la foto
     */
    @Headers("Authorization: GhIaofVN0wdkOvbd6D3WiAc3LUTv68E0Ag3CYgNxVTZ7w7izy4Vq2SUH")
    @GET("v1/photos/{id}")
    suspend fun getPhotoById(@Path("id") id: Int): PexelsPhoto

    /**
     * Obtener fotos curadas (seleccionadas por Pexels)
     * @param page número de página
     * @param perPage cantidad de fotos por página
     */
    @Headers("Authorization: GhIaofVN0wdkOvbd6D3WiAc3LUTv68E0Ag3CYgNxVTZ7w7izy4Vq2SUH")
    @GET("v1/curated")
    suspend fun getCuratedPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): PexelsResponse
}