package com.kitapcepte.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kitapcepte.data.local.entity.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_books ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteBookEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE bookId = :bookId)")
    fun isFavorite(bookId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE bookId = :bookId)")
    suspend fun isFavoriteDirect(bookId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE bookId = :bookId")
    suspend fun deleteFavorite(bookId: String)
}
