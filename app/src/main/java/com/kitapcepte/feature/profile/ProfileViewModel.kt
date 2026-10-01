package com.kitapcepte.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.R
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.repository.AuthRepository
import com.kitapcepte.domain.repository.ProfileRepository
import com.kitapcepte.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut
    private var cardsJob: Job? = null

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch(dispatchers.io) {
            sessionRepository.sessionState.collectLatest { session ->
                currentSession = session
                when (session) {
                    is Session.LoggedIn -> {
                        _uiState.update { state ->
                            state.copy(
                                user = session.user,
                                isGuest = false,
                                isLoading = false,
                                nameInput = if (!state.isEditingName) session.user.name else state.nameInput
                            )
                        }
                        observeSavedCards(session.user.id)
                    }
                    is Session.Guest -> {
                        cardsJob?.cancel()
                        _uiState.update {
                            it.copy(
                                user = null,
                                isGuest = true,
                                isLoading = false,
                                savedCards = emptyList()
                            )
                        }
                    }
                    is Session.LoggedOut -> {
                        cardsJob?.cancel()
                        _uiState.update {
                            it.copy(
                                user = null,
                                isGuest = false,
                                isLoading = false,
                                savedCards = emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeSavedCards(userId: Long) {
        cardsJob?.cancel()
        cardsJob = viewModelScope.launch(dispatchers.io) {
            profileRepository.getSavedCards(userId).collectLatest { cards ->
                _uiState.update { it.copy(savedCards = cards) }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.ToggleEditName -> {
                _uiState.update {
                    it.copy(
                        isEditingName = event.isEditing,
                        nameInput = it.user?.name.orEmpty(),
                        nameError = null
                    )
                }
            }
            is ProfileUiEvent.NameInputChanged -> {
                _uiState.update { it.copy(nameInput = event.value, nameError = null) }
            }
            ProfileUiEvent.SaveNameClicked -> saveName()

            is ProfileUiEvent.ToggleChangePassword -> {
                _uiState.update {
                    it.copy(
                        isChangingPassword = event.isChanging,
                        currentPasswordInput = "",
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null
                    )
                }
            }
            is ProfileUiEvent.CurrentPasswordChanged -> {
                _uiState.update { it.copy(currentPasswordInput = event.value, passwordError = null) }
            }
            is ProfileUiEvent.NewPasswordChanged -> {
                _uiState.update { it.copy(newPasswordInput = event.value, passwordError = null) }
            }
            is ProfileUiEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPasswordInput = event.value, passwordError = null) }
            }
            ProfileUiEvent.ToggleCurrentPasswordVisibility -> {
                _uiState.update { it.copy(currentPasswordVisible = !it.currentPasswordVisible) }
            }
            ProfileUiEvent.ToggleNewPasswordVisibility -> {
                _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
            }
            ProfileUiEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            }
            ProfileUiEvent.SubmitChangePasswordClicked -> submitChangePassword()

            is ProfileUiEvent.ToggleAddCard -> {
                _uiState.update {
                    it.copy(
                        isAddingCard = event.isAdding,
                        cardHolderInput = "",
                        cardNumberInput = "",
                        cardExpiryInput = "",
                        cardError = null
                    )
                }
            }
            is ProfileUiEvent.CardHolderChanged -> {
                _uiState.update { it.copy(cardHolderInput = event.value, cardError = null) }
            }
            is ProfileUiEvent.CardNumberChanged -> {
                val cleanDigits = event.value.filter { it.isDigit() }.take(16)
                _uiState.update { it.copy(cardNumberInput = cleanDigits, cardError = null) }
            }
            is ProfileUiEvent.CardExpiryChanged -> {
                val cleanDigits = event.value.filter { it.isDigit() }.take(4)
                _uiState.update { it.copy(cardExpiryInput = cleanDigits, cardError = null) }
            }
            ProfileUiEvent.SubmitAddCardClicked -> submitAddCard()
            is ProfileUiEvent.DeleteCardClicked -> deleteCard(event.cardId)

            is ProfileUiEvent.ShowLogoutDialog -> {
                _uiState.update { it.copy(showLogoutConfirmDialog = event.show) }
            }
            ProfileUiEvent.LogoutClicked -> logout()
            ProfileUiEvent.SwitchToGuestClicked -> switchToGuest()
            ProfileUiEvent.LoginClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(ProfileUiEffect.NavigateToAuth)
                }
            }
            ProfileUiEvent.BackClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(ProfileUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun saveName() {
        val user = _uiState.value.user ?: return
        val newName = _uiState.value.nameInput.trim()

        if (newName.isBlank()) {
            _uiState.update {
                it.copy(nameError = UiText.StringResource(R.string.profile_error_name_empty))
            }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = profileRepository.updateName(user.id, newName)
            _uiState.update { it.copy(isActionLoading = false) }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isEditingName = false) }
                    _uiEffect.send(ProfileUiEffect.ShowSnackbar(UiText.StringResource(R.string.profile_name_updated)))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(nameError = UiText.DynamicString(error.message ?: "Hata oluştu."))
                    }
                }
            )
        }
    }

    private fun submitChangePassword() {
        val user = _uiState.value.user ?: return
        val currentPass = _uiState.value.currentPasswordInput
        val newPass = _uiState.value.newPasswordInput
        val confirmPass = _uiState.value.confirmPasswordInput

        if (currentPass.isBlank()) {
            _uiState.update {
                it.copy(passwordError = UiText.StringResource(R.string.profile_error_current_password_empty))
            }
            return
        }

        if (newPass.length < 6) {
            _uiState.update {
                it.copy(passwordError = UiText.StringResource(R.string.profile_error_new_password_short))
            }
            return
        }

        if (newPass != confirmPass) {
            _uiState.update {
                it.copy(passwordError = UiText.StringResource(R.string.profile_error_password_mismatch))
            }
            return
        }

        if (currentPass == newPass) {
            _uiState.update {
                it.copy(passwordError = UiText.StringResource(R.string.profile_error_same_password))
            }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = profileRepository.changePassword(user.id, currentPass, newPass)
            _uiState.update { it.copy(isActionLoading = false) }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            currentPasswordInput = "",
                            newPasswordInput = "",
                            confirmPasswordInput = "",
                            passwordError = null
                        )
                    }
                    _uiEffect.send(ProfileUiEffect.ShowSnackbar(UiText.StringResource(R.string.profile_password_updated)))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(passwordError = UiText.DynamicString(error.message ?: "Şifre güncellenemedi."))
                    }
                }
            )
        }
    }

    private fun submitAddCard() {
        val user = _uiState.value.user ?: return
        val holder = _uiState.value.cardHolderInput.trim()
        val number = _uiState.value.cardNumberInput
        val expiry = _uiState.value.cardExpiryInput

        val holderValid = holder.isNotBlank()
        val numberValid = number.length == 16
        val expiryMonth = expiry.take(2).toIntOrNull() ?: 0
        val expiryValid = expiry.length == 4 && expiryMonth in 1..12

        if (!holderValid || !numberValid || !expiryValid) {
            _uiState.update {
                it.copy(cardError = UiText.StringResource(R.string.profile_error_invalid_card))
            }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = profileRepository.addCard(user.id, holder, number, expiry)
            _uiState.update { it.copy(isActionLoading = false) }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isAddingCard = false,
                            cardHolderInput = "",
                            cardNumberInput = "",
                            cardExpiryInput = "",
                            cardError = null
                        )
                    }
                    _uiEffect.send(ProfileUiEffect.ShowSnackbar(UiText.StringResource(R.string.profile_card_added)))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(cardError = UiText.DynamicString(error.message ?: "Kart eklenemedi."))
                    }
                }
            )
        }
    }

    private fun deleteCard(cardId: Long) {
        viewModelScope.launch(dispatchers.io) {
            profileRepository.deleteCard(cardId)
            _uiEffect.send(ProfileUiEffect.ShowSnackbar(UiText.StringResource(R.string.profile_card_deleted)))
        }
    }

    private fun logout() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(showLogoutConfirmDialog = false) }
            sessionRepository.logout()
            _uiEffect.send(ProfileUiEffect.NavigateToAuth)
        }
    }

    private fun switchToGuest() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(showLogoutConfirmDialog = false) }
            authRepository.loginAsGuest()
            _uiEffect.send(ProfileUiEffect.NavigateToHome)
        }
    }
}
