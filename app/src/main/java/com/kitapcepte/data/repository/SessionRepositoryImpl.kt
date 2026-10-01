package com.kitapcepte.data.repository

import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesDataStore: UserPreferencesDataStore
) : SessionRepository {

    override val sessionState: Flow<Session> = combine(
        preferencesDataStore.isGuest,
        preferencesDataStore.loggedInUserId
    ) { isGuest, userId ->
        Pair(isGuest, userId)
    }.flatMapLatest { (isGuest, userId) ->
        when {
            userId != null -> {
                userDao.getUserById(userId).flatMapLatest { entity ->
                    if (entity != null) {
                        flowOf(Session.LoggedIn(User(id = entity.id, email = entity.email, name = entity.name)))
                    } else {
                        flowOf(Session.LoggedOut)
                    }
                }
            }
            isGuest -> flowOf(Session.Guest)
            else -> flowOf(Session.LoggedOut)
        }
    }

    override suspend fun logout() {
        preferencesDataStore.clearSession()
    }
}
