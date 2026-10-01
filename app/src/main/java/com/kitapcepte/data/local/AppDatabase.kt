package com.kitapcepte.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kitapcepte.data.local.dao.CartDao
import com.kitapcepte.data.local.dao.FavoriteDao
import com.kitapcepte.data.local.dao.SavedCardDao
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.entity.CartItemEntity
import com.kitapcepte.data.local.entity.FavoriteBookEntity
import com.kitapcepte.data.local.entity.SavedCardEntity
import com.kitapcepte.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FavoriteBookEntity::class,
        CartItemEntity::class,
        SavedCardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun savedCardDao(): SavedCardDao

    companion object {
        const val DATABASE_NAME = "kitapcepte.db"
    }
}
