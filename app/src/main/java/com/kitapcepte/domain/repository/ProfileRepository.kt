package com.kitapcepte.domain.repository

import com.kitapcepte.domain.model.SavedCard
import com.kitapcepte.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUser(userId: Long): Flow<User?>
    suspend fun updateName(userId: Long, newName: String): Result<Unit>
    suspend fun changePassword(userId: Long, currentPassword: String, newPassword: String): Result<Unit>
    fun getSavedCards(userId: Long): Flow<List<SavedCard>>
    suspend fun addCard(
        userId: Long,
        cardHolder: String,
        cardNumber: String,
        expiryDate: String
    ): Result<SavedCard>
    suspend fun deleteCard(cardId: Long): Result<Unit>
}
