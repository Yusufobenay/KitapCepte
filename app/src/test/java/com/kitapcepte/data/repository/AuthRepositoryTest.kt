package com.kitapcepte.data.repository

import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.PasswordHasher
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.data.local.entity.UserEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private val userDao: UserDao = mockk()
    private val passwordHasher = PasswordHasher()
    private val preferencesDataStore: UserPreferencesDataStore = mockk(relaxed = true)

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        coEvery { preferencesDataStore.setLoggedInUser(any()) } just runs
        coEvery { preferencesDataStore.setGuestMode(any()) } just runs

        authRepository = AuthRepositoryImpl(
            userDao = userDao,
            passwordHasher = passwordHasher,
            preferencesDataStore = preferencesDataStore
        )
    }

    @Test
    fun `login with non-existent email returns failure`() = runTest {
        coEvery { userDao.getUserByEmail("unknown@test.com") } returns null

        val result = authRepository.login("unknown@test.com", "pass123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("bulunamadı")
    }

    @Test
    fun `login with invalid password returns failure`() = runTest {
        val hash = passwordHasher.hash("correctPass")
        val entity = UserEntity(id = 1, email = "user@test.com", name = "Serdar", passwordHash = hash)
        coEvery { userDao.getUserByEmail("user@test.com") } returns entity

        val result = authRepository.login("user@test.com", "wrongPass")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("hatalı")
    }

    @Test
    fun `login with correct credentials returns user and sets session`() = runTest {
        val hash = passwordHasher.hash("correctPass")
        val entity = UserEntity(id = 42, email = "user@test.com", name = "Serdar", passwordHash = hash)
        coEvery { userDao.getUserByEmail("user@test.com") } returns entity

        val result = authRepository.login("user@test.com", "correctPass")

        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user?.id).isEqualTo(42)
        assertThat(user?.email).isEqualTo("user@test.com")
        coVerify(exactly = 1) { preferencesDataStore.setLoggedInUser(42) }
    }

    @Test
    fun `register with duplicate email returns failure`() = runTest {
        coEvery { userDao.existsByEmail("user@test.com") } returns 1

        val result = authRepository.register("Serdar", "user@test.com", "pass123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("kullanımda")
    }

    @Test
    fun `register with new email succeeds and sets session`() = runTest {
        coEvery { userDao.existsByEmail("new@test.com") } returns 0
        coEvery { userDao.insertUser(any()) } returns 99L

        val result = authRepository.register("Serdar", "new@test.com", "pass123")

        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user?.id).isEqualTo(99L)
        assertThat(user?.email).isEqualTo("new@test.com")
        coVerify(exactly = 1) { preferencesDataStore.setLoggedInUser(99L) }
    }

    @Test
    fun `loginAsGuest sets guest mode in datastore`() = runTest {
        authRepository.loginAsGuest()
        coVerify(exactly = 1) { preferencesDataStore.setGuestMode(true) }
    }
}
