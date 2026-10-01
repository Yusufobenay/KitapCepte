package com.kitapcepte.data.repository

import com.kitapcepte.core.common.PasswordHasher
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.data.local.entity.UserEntity
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val preferencesDataStore: UserPreferencesDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val userEntity = userDao.getUserByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("Bu e-posta ile kayıtlı bir hesap bulunamadı. Lütfen kayıt olun."))

        val isPasswordValid = passwordHasher.verify(password, userEntity.passwordHash)
        if (!isPasswordValid) {
            return Result.failure(IllegalArgumentException("Girdiğiniz şifre hatalı."))
        }

        preferencesDataStore.setLoggedInUser(userEntity.id)
        return Result.success(User(id = userEntity.id, email = userEntity.email, name = userEntity.name))
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        val existingUserCount = userDao.existsByEmail(normalizedEmail)
        if (existingUserCount > 0) {
            return Result.failure(IllegalArgumentException("Bu e-posta adresi zaten kullanımda."))
        }

        val passwordHash = passwordHasher.hash(password)
        val entity = UserEntity(
            email = normalizedEmail,
            name = trimmedName,
            passwordHash = passwordHash
        )
        val newUserId = userDao.insertUser(entity)
        preferencesDataStore.setLoggedInUser(newUserId)

        return Result.success(User(id = newUserId, email = normalizedEmail, name = trimmedName))
    }

    override suspend fun loginAsGuest() {
        preferencesDataStore.setGuestMode(true)
    }
}
