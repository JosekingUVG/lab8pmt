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
/**
 * DAO (Data Access Object) para gestionar la tabla del historial de búsquedas (search_history).
 * Este contrato define todas las operaciones de base de datos permitidas para esta entidad.
 */
@Dao
interface SearchHistoryDao {

    /**
     * Obtiene las 10 búsquedas más recientes.
     * - @Query: Define la consulta SQL.
     * - "ORDER BY searched_at DESC": Ordena los resultados desde el más reciente al más antiguo.
     * - "LIMIT 10": Limita el resultado a los 10 primeros registros.
     * - "fun ...(): Flow<List<SearchHistory>>": Devuelve un Flow. Esto es excelente porque la UI
     *   se actualizará automáticamente cada vez que los datos de la tabla cambien, sin necesidad
     *   de volver a llamar a la función manualmente.
     */
    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistory>>

    /**
     * Busca en el historial términos que comiencen con la 'query' proporcionada.
     * Útil para dar sugerencias de autocompletado en la barra de búsqueda.
     * - "suspend fun": Indica que es una operación que puede tardar y debe ser llamada desde
     *   una corrutina (un "one-shot read"). No necesita ser un Flow porque solo se ejecuta
     *   cuando el usuario escribe.
     * - "WHERE query LIKE :query || '%'": Es la clave de la búsqueda. El operador '||' concatena
     *   el término de búsqueda con el comodín '%', buscando cualquier cosa que empiece con 'query'.
     *   Por ejemplo, si query es "nat", encontrará "nature", "national park", etc.
     */
    @Query("SELECT * FROM search_history WHERE search_query LIKE :query || '%' ORDER BY searched_at DESC LIMIT 5")
    suspend fun searchInHistory(query: String): List<SearchHistory>


    /**
     * Inserta o reemplaza un término de búsqueda.
     * - @Insert: Anotación de Room para operaciones de inserción.
     * - "onConflict = OnConflictStrategy.REPLACE": Estrategia de conflicto muy inteligente aquí.
     *   Si el usuario busca algo que ya está en el historial, en lugar de duplicarlo, esta
     *   estrategia reemplaza la entrada antigua. Al combinar esto con el campo 'searched_at'
     *   de tu entidad 'SearchHistory', efectivamente actualiza la fecha de la búsqueda,
     *   haciéndola la más reciente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    /**
     * Elimina una entrada específica del historial por su texto.
     * Útil si quieres permitir que el usuario elimine una sugerencia de búsqueda.
     */
    @Query("DELETE FROM search_history WHERE search_query = :query")
    suspend fun deleteSearchByQuery(query: String)


    /**
     * Borra todo el historial de búsqueda.
     * Una función necesaria para la típica opción "Limpiar historial".
     */
    @Query("DELETE FROM search_history")
    suspend fun clearHistory()

    /**
     * Función de "limpieza" para mantener la tabla optimizada.
     * Elimina todas las entradas excepto las 10 más recientes. Esto evita que la tabla
     * crezca indefinidamente. Es una excelente práctica de mantenimiento.
     * - "WHERE id NOT IN (...)": La subconsulta selecciona los IDs de las 10 búsquedas más
     *   recientes, y la consulta principal elimina todo lo que NO esté en esa lista.
     */
    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searched_at DESC LIMIT 10)")
    suspend fun keepOnlyRecentSearches()
}



/**
 * DAO para gestionar usuarios - ejemplo base
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM user")
    fun deleteAll()
}