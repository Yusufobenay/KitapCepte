package com.kitapcepte.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kitapcepte.data.local.entity.SavedCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCardDao {
    @Query("SELECT * FROM saved_cards WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getCardsByUserId(userId: Long): Flow<List<SavedCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SavedCardEntity): Long

    @Query("DELETE FROM saved_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("DELETE FROM saved_cards WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
