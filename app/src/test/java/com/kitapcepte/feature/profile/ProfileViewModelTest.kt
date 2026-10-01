package com.kitapcepte.feature.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.TestDispatcherProvider
import com.kitapcepte.domain.model.SavedCard
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.AuthRepository
import com.kitapcepte.domain.repository.ProfileRepository
import com.kitapcepte.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val initialUser = User(id = 10L, email = "yusuf@test.com", name = "Yusuf Çelik")
    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(initialUser))
    private val cardsFlow = MutableStateFlow<List<SavedCard>>(emptyList())

    private val sampleCard = SavedCard(
        id = 1L,
        userId = 10L,
        cardHolder = "YUSUF CELIK",
        maskedNumber = "•••• •••• •••• 4242",
        cardBrand = "VISA",
        expiryMonth = "12",
        expiryYear = "28",
        isDefault = false
    )

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { profileRepository.getSavedCards(10L) } returns cardsFlow
        coEvery { sessionRepository.logout() } just runs

        viewModel = ProfileViewModel(
            profileRepository = profileRepository,
            sessionRepository = sessionRepository,
            authRepository = authRepository,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load with logged-in user sets user and observes saved cards`() = runTest(testDispatcher) {
        cardsFlow.value = listOf(sampleCard)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.user).isEqualTo(initialUser)
        assertThat(state.isGuest).isFalse()
        assertThat(state.isLoading).isFalse()
        assertThat(state.nameInput).isEqualTo("Yusuf Çelik")
        assertThat(state.savedCards).containsExactly(sampleCard)
        assertThat(state.userInitials).isEqualTo("YÇ")
    }

    @Test
    fun `initial load with guest user sets isGuest true and clears cards`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isGuest).isTrue()
        assertThat(state.user).isNull()
        assertThat(state.savedCards).isEmpty()
    }

    @Test
    fun `NameInputChanged updates name input and clears error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.NameInputChanged("Ahmet Yılmaz"))

        val state = viewModel.uiState.value
        assertThat(state.nameInput).isEqualTo("Ahmet Yılmaz")
        assertThat(state.nameError).isNull()
    }

    @Test
    fun `SaveNameClicked with blank name sets error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.NameInputChanged("   "))
        viewModel.onEvent(ProfileUiEvent.SaveNameClicked)

        val state = viewModel.uiState.value
        assertThat(state.nameError).isNotNull()
        coVerify(exactly = 0) { profileRepository.updateName(any(), any()) }
    }

    @Test
    fun `SaveNameClicked with valid name updates name in repository and shows snackbar`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { profileRepository.updateName(10L, "Yusuf Updated") } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.NameInputChanged("Yusuf Updated"))
            viewModel.onEvent(ProfileUiEvent.SaveNameClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProfileUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.isEditingName).isFalse()
            coVerify(exactly = 1) { profileRepository.updateName(10L, "Yusuf Updated") }
        }
    }

    @Test
    fun `ToggleChangePassword clears inputs and toggles flag`() = runTest(testDispatcher) {
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
        assertThat(viewModel.uiState.value.isChangingPassword).isTrue()

        viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged("123456"))
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(false))
        assertThat(viewModel.uiState.value.isChangingPassword).isFalse()
        assertThat(viewModel.uiState.value.currentPasswordInput).isEmpty()
    }

    @Test
    fun `SubmitChangePasswordClicked with empty current password sets error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
        viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged(""))
        viewModel.onEvent(ProfileUiEvent.NewPasswordChanged("secret123"))
        viewModel.onEvent(ProfileUiEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(ProfileUiEvent.SubmitChangePasswordClicked)

        assertThat(viewModel.uiState.value.passwordError).isNotNull()
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `SubmitChangePasswordClicked with short new password sets error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
        viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged("oldPass123"))
        viewModel.onEvent(ProfileUiEvent.NewPasswordChanged("123"))
        viewModel.onEvent(ProfileUiEvent.ConfirmPasswordChanged("123"))
        viewModel.onEvent(ProfileUiEvent.SubmitChangePasswordClicked)

        assertThat(viewModel.uiState.value.passwordError).isNotNull()
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `SubmitChangePasswordClicked with mismatched confirmation sets error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
        viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged("oldPass123"))
        viewModel.onEvent(ProfileUiEvent.NewPasswordChanged("newPass123"))
        viewModel.onEvent(ProfileUiEvent.ConfirmPasswordChanged("newPassDifferent"))
        viewModel.onEvent(ProfileUiEvent.SubmitChangePasswordClicked)

        assertThat(viewModel.uiState.value.passwordError).isNotNull()
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `SubmitChangePasswordClicked with same old and new password sets error`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
        viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged("samePass123"))
        viewModel.onEvent(ProfileUiEvent.NewPasswordChanged("samePass123"))
        viewModel.onEvent(ProfileUiEvent.ConfirmPasswordChanged("samePass123"))
        viewModel.onEvent(ProfileUiEvent.SubmitChangePasswordClicked)

        assertThat(viewModel.uiState.value.passwordError).isNotNull()
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `SubmitChangePasswordClicked with valid passwords updates password and shows snackbar`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { profileRepository.changePassword(10L, "oldPass123", "newPass456") } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.ToggleChangePassword(true))
            viewModel.onEvent(ProfileUiEvent.CurrentPasswordChanged("oldPass123"))
            viewModel.onEvent(ProfileUiEvent.NewPasswordChanged("newPass456"))
            viewModel.onEvent(ProfileUiEvent.ConfirmPasswordChanged("newPass456"))
            viewModel.onEvent(ProfileUiEvent.SubmitChangePasswordClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProfileUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.isChangingPassword).isFalse()
            coVerify(exactly = 1) { profileRepository.changePassword(10L, "oldPass123", "newPass456") }
        }
    }

    @Test
    fun `SubmitAddCardClicked with invalid input sets cardError`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ProfileUiEvent.ToggleAddCard(true))
        viewModel.onEvent(ProfileUiEvent.CardHolderChanged(""))
        viewModel.onEvent(ProfileUiEvent.CardNumberChanged("123"))
        viewModel.onEvent(ProfileUiEvent.CardExpiryChanged("9999"))
        viewModel.onEvent(ProfileUiEvent.SubmitAddCardClicked)

        assertThat(viewModel.uiState.value.cardError).isNotNull()
        coVerify(exactly = 0) { profileRepository.addCard(any(), any(), any(), any()) }
    }

    @Test
    fun `SubmitAddCardClicked with valid input calls repository, resets form, and shows snackbar`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery {
            profileRepository.addCard(10L, "Yusuf Çelik", "4123456789012345", "1228")
        } returns Result.success(sampleCard)

        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.ToggleAddCard(true))
            viewModel.onEvent(ProfileUiEvent.CardHolderChanged("Yusuf Çelik"))
            viewModel.onEvent(ProfileUiEvent.CardNumberChanged("4123456789012345"))
            viewModel.onEvent(ProfileUiEvent.CardExpiryChanged("1228"))
            viewModel.onEvent(ProfileUiEvent.SubmitAddCardClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProfileUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.isAddingCard).isFalse()
            assertThat(viewModel.uiState.value.cardNumberInput).isEmpty()
            coVerify(exactly = 1) { profileRepository.addCard(10L, "Yusuf Çelik", "4123456789012345", "1228") }
        }
    }

    @Test
    fun `DeleteCardClicked calls repository and shows snackbar`() = runTest(testDispatcher) {
        coEvery { profileRepository.deleteCard(1L) } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.DeleteCardClicked(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ProfileUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { profileRepository.deleteCard(1L) }
        }
    }

    @Test
    fun `LogoutClicked logs out and navigates to Auth`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.LogoutClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(ProfileUiEffect.NavigateToAuth)
            coVerify(exactly = 1) { sessionRepository.logout() }
        }
    }

    @Test
    fun `SwitchToGuestClicked sets guest and navigates to Home`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.SwitchToGuestClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(ProfileUiEffect.NavigateToHome)
            coVerify(exactly = 1) { authRepository.loginAsGuest() }
        }
    }

    @Test
    fun `LoginClicked emits NavigateToAuth`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.LoginClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(ProfileUiEffect.NavigateToAuth)
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(ProfileUiEvent.BackClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(ProfileUiEffect.NavigateBack)
        }
    }
}
