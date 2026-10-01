package com.kitapcepte.feature.auth

import com.kitapcepte.core.common.UiText

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val isLogin: Boolean get() = mode == AuthMode.LOGIN
    val isRegister: Boolean get() = mode == AuthMode.REGISTER
}

sealed interface AuthUiEvent {
    data class ModeChanged(val mode: AuthMode) : AuthUiEvent
    data class NameChanged(val name: String) : AuthUiEvent
    data class EmailChanged(val email: String) : AuthUiEvent
    data class PasswordChanged(val password: String) : AuthUiEvent
    data class PasswordConfirmChanged(val passwordConfirm: String) : AuthUiEvent
    data object SubmitClicked : AuthUiEvent
    data object GuestLoginClicked : AuthUiEvent
    data object DismissError : AuthUiEvent
}

sealed interface AuthUiEffect {
    data object NavigateToHome : AuthUiEffect
    data class ShowSnackbar(val message: UiText) : AuthUiEffect
}
