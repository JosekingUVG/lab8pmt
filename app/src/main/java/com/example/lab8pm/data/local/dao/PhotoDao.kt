package com.example.lab8pm.data.local.dao

import androidx.room.*
import com.example.lab8pm.data.models.FavoritePhoto
import com.example.lab8pm.data.models.SearchHistory
import com.example.lab8pm.data.models.User
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestionar fotos favoritas
 */
@Dao
interface FavoritePhotoDao {

    @Query("SELECT * FROM favorite_photos ORDER BY saved_at DESC")
    fun getAllFavorites(): Flow<List<FavoritePhoto>>

    @Query("SELECT * FROM favorite_photos WHERE id = :photoId")
    suspend fun getFavoriteById(photoId: Int): FavoritePhoto?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_photos WHERE id = :photoId)")
    suspend fun isFavorite(photoId: Int): Boolean



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(photo: FavoritePhoto)

    @Delete
    suspend fun deleteFavorite(photo: FavoritePhoto)

    @Query("DELETE FROM favorite_photos WHERE id = :photoId")
    suspend fun deleteFavoriteById(photoId: Int)

    @Query("DELETE FROM favorite_photos")
    suspend fun deleteAllFavorites()

    @Query("SELECT COUNT(*) FROM favorite_photos")
    suspend fun getFavoritesCount(): Int
}

/**
 * DAO para gestionar historial de búsquedas
 */
@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE search_query LIKE :query || '%' ORDER BY searched_at DESC LIMIT 5")
    suspend fun searchInHistory(query: String): List<SearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    @Query("DELETE FROM search_history WHERE search_query = :query")
    suspend fun deleteSearchByQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searched_at DESC LIMIT 10)")
    suspend fun keepOnlyRecentSearches()
}

/**
 * DAO para gestionar perfil de usuario
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM user_profile WHERE uid = 1 LIMIT 1")
    suspend fun getUserProfile(): User?

    @Query("SELECT * FROM user_profile WHERE uid = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: User)

    @Query("UPDATE user_profile SET name = :name WHERE uid = 1")
    suspend fun updateName(name: String)

    @Query("UPDATE user_profile SET photo_uri = :photoUri WHERE uid = 1")
    suspend fun updatePhotoUri(photoUri: String?)

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()
}