package com.kitapcepte.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.R
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AuthUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.ModeChanged -> {
                _uiState.update { it.copy(mode = event.mode, errorMessage = null) }
            }
            is AuthUiEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, errorMessage = null) }
            }
            is AuthUiEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, errorMessage = null) }
            }
            is AuthUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, errorMessage = null) }
            }
            is AuthUiEvent.PasswordConfirmChanged -> {
                _uiState.update { it.copy(passwordConfirm = event.passwordConfirm, errorMessage = null) }
            }
            AuthUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            AuthUiEvent.SubmitClicked -> {
                if (_uiState.value.isLogin) {
                    performLogin()
                } else {
                    performRegister()
                }
            }
            AuthUiEvent.GuestLoginClicked -> {
                performGuestLogin()
            }
        }
    }

    private fun performLogin() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank() || !EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_invalid_email)) }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_short_password)) }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(email, password)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _uiEffect.send(AuthUiEffect.NavigateToHome)
            }.onFailure { error ->
                val errorMsg = error.message ?: "Giriş başarısız oldu."
                // If account not found, prompt and auto-switch to register as specified in MAIN_PROMPT
                if (errorMsg.contains("bulunamadı", ignoreCase = true)) {
                    _uiState.update {
                        it.copy(
                            mode = AuthMode.REGISTER,
                            errorMessage = UiText.StringResource(R.string.auth_error_account_not_found)
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = UiText.DynamicString(errorMsg)) }
                }
            }
        }
    }

    private fun performRegister() {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val password = state.password
        val confirm = state.passwordConfirm

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_name_empty)) }
            return
        }
        if (email.isBlank() || !EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_invalid_email)) }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_short_password)) }
            return
        }
        if (password != confirm) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.auth_error_password_mismatch)) }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(name, email, password)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                _uiEffect.send(AuthUiEffect.NavigateToHome)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = UiText.DynamicString(error.message ?: "Kayıt başarısız oldu.")) }
            }
        }
    }

    private fun performGuestLogin() {
        viewModelScope.launch(dispatchers.io) {
            authRepository.loginAsGuest()
            _uiEffect.send(AuthUiEffect.NavigateToHome)
        }
    }
}
