package com.kitapcepte.domain.repository

import com.kitapcepte.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val sessionState: Flow<Session>
    suspend fun logout()
}
