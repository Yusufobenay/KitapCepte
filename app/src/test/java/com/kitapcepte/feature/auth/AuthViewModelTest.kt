package com.kitapcepte.feature.auth

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.R
import com.kitapcepte.core.common.TestDispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        viewModel = AuthViewModel(
            authRepository = authRepository,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `initial state is Login mode with empty fields`() {
        val state = viewModel.uiState.value
        assertThat(state.mode).isEqualTo(AuthMode.LOGIN)
        assertThat(state.isLogin).isTrue()
        assertThat(state.email).isEmpty()
        assertThat(state.password).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `ModeChanged updates mode in state`() {
        viewModel.onEvent(AuthUiEvent.ModeChanged(AuthMode.REGISTER))
        assertThat(viewModel.uiState.value.mode).isEqualTo(AuthMode.REGISTER)
        assertThat(viewModel.uiState.value.isRegister).isTrue()
    }

    @Test
    fun `login with invalid email sets error`() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.EmailChanged("not-an-email"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("123456"))
        viewModel.onEvent(AuthUiEvent.SubmitClicked)

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        assertThat((viewModel.uiState.value.errorMessage as? UiText.StringResource)?.resId)
            .isEqualTo(R.string.auth_error_invalid_email)
    }

    @Test
    fun `login with short password sets error`() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("123"))
        viewModel.onEvent(AuthUiEvent.SubmitClicked)

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        assertThat((viewModel.uiState.value.errorMessage as? UiText.StringResource)?.resId)
            .isEqualTo(R.string.auth_error_short_password)
    }

    @Test
    fun `login when account not found auto switches to Register mode`() = runTest(testDispatcher) {
        coEvery { authRepository.login("new@test.com", "secret123") } returns
            Result.failure(IllegalArgumentException("Bu e-posta ile kayıtlı bir hesap bulunamadı."))

        viewModel.onEvent(AuthUiEvent.EmailChanged("new@test.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("secret123"))
        viewModel.onEvent(AuthUiEvent.SubmitClicked)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.mode).isEqualTo(AuthMode.REGISTER)
        assertThat((viewModel.uiState.value.errorMessage as? UiText.StringResource)?.resId)
            .isEqualTo(R.string.auth_error_account_not_found)
    }

    @Test
    fun `login with valid credentials emits NavigateToHome`() = runTest(testDispatcher) {
        coEvery { authRepository.login("user@test.com", "secret123") } returns
            Result.success(User(1, "user@test.com", "Serdar"))

        viewModel.onEvent(AuthUiEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("secret123"))

        viewModel.uiEffect.test {
            viewModel.onEvent(AuthUiEvent.SubmitClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.NavigateToHome)
        }
    }

    @Test
    fun `register with mismatched passwords sets error`() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.ModeChanged(AuthMode.REGISTER))
        viewModel.onEvent(AuthUiEvent.NameChanged("Serdar"))
        viewModel.onEvent(AuthUiEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthUiEvent.PasswordConfirmChanged("passwordXYZ"))

        viewModel.onEvent(AuthUiEvent.SubmitClicked)

        assertThat((viewModel.uiState.value.errorMessage as? UiText.StringResource)?.resId)
            .isEqualTo(R.string.auth_error_password_mismatch)
    }

    @Test
    fun `register with valid credentials emits NavigateToHome`() = runTest(testDispatcher) {
        coEvery { authRepository.register("Serdar", "user@test.com", "password123") } returns
            Result.success(User(1, "user@test.com", "Serdar"))

        viewModel.onEvent(AuthUiEvent.ModeChanged(AuthMode.REGISTER))
        viewModel.onEvent(AuthUiEvent.NameChanged("Serdar"))
        viewModel.onEvent(AuthUiEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthUiEvent.PasswordConfirmChanged("password123"))

        viewModel.uiEffect.test {
            viewModel.onEvent(AuthUiEvent.SubmitClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.NavigateToHome)
        }
    }

    @Test
    fun `GuestLoginClicked invokes repository and emits NavigateToHome`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(AuthUiEvent.GuestLoginClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.NavigateToHome)
            coVerify(exactly = 1) { authRepository.loginAsGuest() }
        }
    }
}
