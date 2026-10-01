package com.kitapcepte.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.data.local.entity.UserEntity
import com.kitapcepte.domain.model.Session
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SessionRepositoryTest {

    private val userDao: UserDao = mockk()
    private val preferencesDataStore: UserPreferencesDataStore = mockk(relaxed = true)

    private val isGuestFlow = MutableStateFlow(false)
    private val loggedInUserIdFlow = MutableStateFlow<Long?>(null)

    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setUp() {
        every { preferencesDataStore.isGuest } returns isGuestFlow
        every { preferencesDataStore.loggedInUserId } returns loggedInUserIdFlow
        coEvery { preferencesDataStore.clearSession() } just runs

        repository = SessionRepositoryImpl(
            userDao = userDao,
            preferencesDataStore = preferencesDataStore
        )
    }

    @Test
    fun `when neither guest nor user id then session is LoggedOut`() = runTest {
        repository.sessionState.test {
            val session = awaitItem()
            assertThat(session).isEqualTo(Session.LoggedOut)
        }
    }

    @Test
    fun `when guest mode is true then session is Guest`() = runTest {
        isGuestFlow.value = true

        repository.sessionState.test {
            val session = awaitItem()
            assertThat(session).isEqualTo(Session.Guest)
        }
    }

    @Test
    fun `when user id is set and user exists then session is LoggedIn`() = runTest {
        val userEntity = UserEntity(id = 12L, email = "serdar@test.com", passwordHash = "hash", name = "Serdar")
        every { userDao.getUserById(12L) } returns flowOf(userEntity)

        loggedInUserIdFlow.value = 12L

        repository.sessionState.test {
            val session = awaitItem()
            assertThat(session).isInstanceOf(Session.LoggedIn::class.java)
            val loggedIn = session as Session.LoggedIn
            assertThat(loggedIn.user.id).isEqualTo(12L)
            assertThat(loggedIn.user.email).isEqualTo("serdar@test.com")
            assertThat(loggedIn.user.name).isEqualTo("Serdar")
        }
    }

    @Test
    fun `when user id is set but entity is null then session is LoggedOut`() = runTest {
        every { userDao.getUserById(99L) } returns flowOf(null)

        loggedInUserIdFlow.value = 99L

        repository.sessionState.test {
            val session = awaitItem()
            assertThat(session).isEqualTo(Session.LoggedOut)
        }
    }

    @Test
    fun `logout clears preferences session`() = runTest {
        repository.logout()
        coVerify(exactly = 1) { preferencesDataStore.clearSession() }
    }
}
