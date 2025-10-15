package com.example.lab8pm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lab8pm.data.local.dao.FavoritePhotoDao
import com.example.lab8pm.data.local.dao.SearchHistoryDao
import com.example.lab8pm.data.local.dao.UserDao
import com.example.lab8pm.data.models.FavoritePhoto
import com.example.lab8pm.data.models.SearchHistory
import com.example.lab8pm.data.models.User

/**
 * Base de datos principal de la aplicación usando Room
 */
@Database(
    entities = [
        FavoritePhoto::class,
        SearchHistory::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoritePhotoDao(): FavoritePhotoDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lab8pm_database"
                )
                    .fallbackToDestructiveMigration() // Para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}