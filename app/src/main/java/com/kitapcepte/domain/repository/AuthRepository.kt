package com.kitapcepte.domain.repository

import com.kitapcepte.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun loginAsGuest()
}
