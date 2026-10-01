package com.kitapcepte.data.repository

import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.PasswordHasher
import com.kitapcepte.data.local.dao.SavedCardDao
import com.kitapcepte.data.local.dao.UserDao
import com.kitapcepte.data.local.entity.SavedCardEntity
import com.kitapcepte.data.local.entity.UserEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProfileRepositoryTest {

    private val userDao: UserDao = mockk()
    private val savedCardDao: SavedCardDao = mockk()
    private val passwordHasher = PasswordHasher()

    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        repository = ProfileRepositoryImpl(
            userDao = userDao,
            savedCardDao = savedCardDao,
            passwordHasher = passwordHasher
        )
    }

    @Test
    fun `getUser maps entity to domain User`() = runTest {
        val userEntity = UserEntity(id = 1L, email = "test@user.com", name = "Test User", passwordHash = "hash")
        every { userDao.getUserById(1L) } returns flowOf(userEntity)

        val user = repository.getUser(1L).first()
        assertThat(user).isNotNull()
        assertThat(user?.id).isEqualTo(1L)
        assertThat(user?.email).isEqualTo("test@user.com")
        assertThat(user?.name).isEqualTo("Test User")
    }

    @Test
    fun `updateName with blank name returns failure`() = runTest {
        val result = repository.updateName(1L, "   ")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("boş bırakılamaz")
    }

    @Test
    fun `updateName with valid name calls dao updateName`() = runTest {
        coEvery { userDao.updateName(1L, "New Name") } just runs

        val result = repository.updateName(1L, "  New Name  ")
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { userDao.updateName(1L, "New Name") }
    }

    @Test
    fun `changePassword with blank current password returns failure`() = runTest {
        val result = repository.changePassword(1L, "", "newPassword123")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("mevcut şifrenizi girin")
    }

    @Test
    fun `changePassword with short new password returns failure`() = runTest {
        val result = repository.changePassword(1L, "currentPass1", "123")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("en az 6 karakter")
    }

    @Test
    fun `changePassword with same old and new password returns failure`() = runTest {
        val result = repository.changePassword(1L, "samePassword", "samePassword")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("aynı olamaz")
    }

    @Test
    fun `changePassword with non-existent user returns failure`() = runTest {
        coEvery { userDao.findUserById(99L) } returns null

        val result = repository.changePassword(99L, "currentPass", "newPass123")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("bulunamadı")
    }

    @Test
    fun `changePassword with wrong current password returns failure`() = runTest {
        val currentHash = passwordHasher.hash("realPassword")
        val entity = UserEntity(id = 1L, email = "test@user.com", name = "Test", passwordHash = currentHash)
        coEvery { userDao.findUserById(1L) } returns entity

        val result = repository.changePassword(1L, "wrongPassword", "newPassword123")
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("hatalı")
    }

    @Test
    fun `changePassword with valid inputs hashes new password and updates dao`() = runTest {
        val currentHash = passwordHasher.hash("realPassword")
        val entity = UserEntity(id = 1L, email = "test@user.com", name = "Test", passwordHash = currentHash)
        coEvery { userDao.findUserById(1L) } returns entity
        coEvery { userDao.updatePasswordHash(1L, any()) } just runs

        val result = repository.changePassword(1L, "realPassword", "newPassword123")
        assertThat(result.isSuccess).isTrue()

        val expectedNewHash = passwordHasher.hash("newPassword123")
        coVerify(exactly = 1) { userDao.updatePasswordHash(1L, expectedNewHash) }
    }

    @Test
    fun `addCard with blank holder returns failure`() = runTest {
        val result = repository.addCard(1L, "", "4123456789012345", "1228")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `addCard with invalid card number returns failure`() = runTest {
        val result = repository.addCard(1L, "Serdar Akçay", "1234", "1228")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `addCard with invalid expiry returns failure`() = runTest {
        val result = repository.addCard(1L, "Serdar Akçay", "4123456789012345", "9928")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `addCard with valid inputs masks number, detects brand, and saves in dao`() = runTest {
        val cardSlot = slot<SavedCardEntity>()
        coEvery { savedCardDao.insertCard(capture(cardSlot)) } returns 5L

        val result = repository.addCard(1L, "Serdar Akçay", "5123456789012345", "1127")
        assertThat(result.isSuccess).isTrue()

        val savedCard = result.getOrNull()
        assertThat(savedCard).isNotNull()
        assertThat(savedCard?.id).isEqualTo(5L)
        assertThat(savedCard?.maskedNumber).isEqualTo("•••• •••• •••• 2345")
        assertThat(savedCard?.cardBrand).isEqualTo("MASTERCARD")
        assertThat(savedCard?.expiryMonth).isEqualTo("11")
        assertThat(savedCard?.expiryYear).isEqualTo("27")

        assertThat(cardSlot.captured.maskedNumber).isEqualTo("•••• •••• •••• 2345")
        assertThat(cardSlot.captured.cardBrand).isEqualTo("MASTERCARD")
    }

    @Test
    fun `deleteCard calls dao deleteCardById`() = runTest {
        coEvery { savedCardDao.deleteCardById(3L) } just runs

        val result = repository.deleteCard(3L)
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { savedCardDao.deleteCardById(3L) }
    }
}
