package com.kitapcepte.data.repository

import com.kitapcepte.core.common.PasswordHasher
import com.kitapcepte.data.local.dao.SavedCardDao
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.entity.SavedCardEntity
import com.kitapcepte.data.mapper.toDomain
import com.kitapcepte.domain.model.SavedCard
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val savedCardDao: SavedCardDao,
    private val passwordHasher: PasswordHasher
) : ProfileRepository {

    override fun getUser(userId: Long): Flow<User?> {
        return userDao.getUserById(userId).map { entity ->
            entity?.let { User(id = it.id, email = it.email, name = it.name) }
        }
    }

    override suspend fun updateName(userId: Long, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("İsim alanı boş bırakılamaz."))
        }
        userDao.updateName(userId, trimmed)
        return Result.success(Unit)
    }

    override suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Lütfen mevcut şifrenizi girin."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Yeni şifre en az 6 karakter olmalıdır."))
        }
        if (currentPassword == newPassword) {
            return Result.failure(IllegalArgumentException("Yeni şifre mevcut şifrenizle aynı olamaz."))
        }

        val userEntity = userDao.findUserById(userId)
            ?: return Result.failure(IllegalStateException("Kullanıcı bulunamadı."))

        val isPasswordValid = passwordHasher.verify(currentPassword, userEntity.passwordHash)
        if (!isPasswordValid) {
            return Result.failure(IllegalArgumentException("Girdiğiniz mevcut şifre hatalı."))
        }

        val newHash = passwordHasher.hash(newPassword)
        userDao.updatePasswordHash(userId, newHash)
        return Result.success(Unit)
    }

    override fun getSavedCards(userId: Long): Flow<List<SavedCard>> {
        return savedCardDao.getCardsByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addCard(
        userId: Long,
        cardHolder: String,
        cardNumber: String,
        expiryDate: String
    ): Result<SavedCard> {
        val trimmedHolder = cardHolder.trim()
        if (trimmedHolder.isBlank()) {
            return Result.failure(IllegalArgumentException("Kart üzerindeki isim boş bırakılamaz."))
        }

        val digits = cardNumber.filter { it.isDigit() }
        if (digits.length != 16) {
            return Result.failure(IllegalArgumentException("Kart numarası 16 haneli olmalıdır."))
        }

        val expiryDigits = expiryDate.filter { it.isDigit() }
        if (expiryDigits.length != 4) {
            return Result.failure(IllegalArgumentException("Son kullanma tarihi AA/YY formatında olmalıdır."))
        }

        val month = expiryDigits.take(2).toIntOrNull() ?: 0
        if (month !in 1..12) {
            return Result.failure(IllegalArgumentException("Geçersiz ay değeri."))
        }

        val monthStr = expiryDigits.substring(0, 2)
        val yearStr = expiryDigits.substring(2, 4)
        val last4 = digits.takeLast(4)
        val maskedNumber = "•••• •••• •••• $last4"

        val cardBrand = when {
            digits.startsWith("4") -> "VISA"
            digits.startsWith("5") -> "MASTERCARD"
            digits.startsWith("9") -> "TROY"
            else -> "KART"
        }

        val entity = SavedCardEntity(
            userId = userId,
            cardHolder = trimmedHolder,
            maskedNumber = maskedNumber,
            cardBrand = cardBrand,
            expiryMonth = monthStr,
            expiryYear = yearStr,
            isDefault = false
        )

        val generatedId = savedCardDao.insertCard(entity)
        return Result.success(entity.copy(id = generatedId).toDomain())
    }

    override suspend fun deleteCard(cardId: Long): Result<Unit> {
        savedCardDao.deleteCardById(cardId)
        return Result.success(Unit)
    }
}
