package com.example.lab8pm.data.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ============ MODELOS DE PEXELS API ============
data class PexelsResponse(
    val page: Int,
    val per_page: Int,
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val height: Int,
    val photographer: String,
    val photographer_url: String,
    val src: PexelsSrc
)

data class PexelsSrc(
    val original: String,
    val large: String,
    val medium: String,
    val small: String
)

// ============ MODELO DE PERFIL DE USUARIO ============
data class UserProfile(
    val name: String = "",
    val photoUri: Uri? = null
)

// ============ ENTIDADES DE ROOM ============

/**
 * Entidad para almacenar fotos favoritas en la base de datos local
 */
@Entity(tableName = "favorite_photos")
data class FavoritePhoto(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "photographer") val photographer: String,
    @ColumnInfo(name = "photographer_url") val photographerUrl: String,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "url_original") val urlOriginal: String,
    @ColumnInfo(name = "url_large") val urlLarge: String,
    @ColumnInfo(name = "url_medium") val urlMedium: String,
    @ColumnInfo(name = "url_small") val urlSmall: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long = System.currentTimeMillis()
)

/**
 * Entidad para historial de búsquedas
 */
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "search_query") val searchQuery: String,
    @ColumnInfo(name = "searched_at") val searchedAt: Long = System.currentTimeMillis()
)

/**
 * Entidad de Usuario para perfil
 */
@Entity(tableName = "user_profile")
data class User(
    @PrimaryKey val uid: Int = 1, // Siempre usamos ID 1 para el perfil único
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "photo_uri") val photoUri: String? // Guardamos URI como String
)

// ============ FUNCIONES DE EXTENSIÓN PARA CONVERSIÓN ============

/**
 * Convierte un PexelsPhoto de la API a una entidad FavoritePhoto de Room
 */
fun PexelsPhoto.toFavoritePhoto(): FavoritePhoto {
    return FavoritePhoto(
        id = this.id,
        photographer = this.photographer,
        photographerUrl = this.photographer_url,
        width = this.width,
        height = this.height,
        urlOriginal = this.src.original,
        urlLarge = this.src.large,
        urlMedium = this.src.medium,
        urlSmall = this.src.small
    )
}

/**
 * Convierte una entidad FavoritePhoto de Room a un PexelsPhoto
 */
fun FavoritePhoto.toPexelsPhoto(): PexelsPhoto {
    return PexelsPhoto(
        id = this.id,
        photographer = this.photographer,
        photographer_url = this.photographerUrl,
        width = this.width,
        height = this.height,
        src = PexelsSrc(
            original = this.urlOriginal,
            large = this.urlLarge,
            medium = this.urlMedium,
            small = this.urlSmall
        )
    )
}