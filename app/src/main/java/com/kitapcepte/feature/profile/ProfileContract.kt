package com.kitapcepte.feature.profile

import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.SavedCard
import com.kitapcepte.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val isGuest: Boolean = false,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val savedCards: List<SavedCard> = emptyList(),
    val isEditingName: Boolean = false,
    val nameInput: String = "",
    val nameError: UiText? = null,
    val isChangingPassword: Boolean = false,
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val passwordError: UiText? = null,
    val isAddingCard: Boolean = false,
    val cardHolderInput: String = "",
    val cardNumberInput: String = "",
    val cardExpiryInput: String = "",
    val cardError: UiText? = null,
    val showLogoutConfirmDialog: Boolean = false
) {
    val userInitials: String
        get() {
            val trimmed = user?.name?.trim().orEmpty()
            if (trimmed.isEmpty()) return "KC"
            val parts = trimmed.split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "KC"
            }
        }

    val formattedCardNumberInput: String
        get() {
            val digits = cardNumberInput.filter { it.isDigit() }.take(16)
            return digits.chunked(4).joinToString(" ")
        }

    val formattedExpiryInput: String
        get() {
            val digits = cardExpiryInput.filter { it.isDigit() }.take(4)
            return when {
                digits.length <= 2 -> digits
                else -> "${digits.substring(0, 2)}/${digits.substring(2)}"
            }
        }

    val cardBrandPreview: String
        get() = when {
            cardNumberInput.startsWith("4") -> "VISA"
            cardNumberInput.startsWith("5") -> "MASTERCARD"
            cardNumberInput.startsWith("9") -> "TROY"
            else -> "KART"
        }
}

sealed interface ProfileUiEvent {
    // Name editing
    data class ToggleEditName(val isEditing: Boolean) : ProfileUiEvent
    data class NameInputChanged(val value: String) : ProfileUiEvent
    data object SaveNameClicked : ProfileUiEvent

    // Password change
    data class ToggleChangePassword(val isChanging: Boolean) : ProfileUiEvent
    data class CurrentPasswordChanged(val value: String) : ProfileUiEvent
    data class NewPasswordChanged(val value: String) : ProfileUiEvent
    data class ConfirmPasswordChanged(val value: String) : ProfileUiEvent
    data object ToggleCurrentPasswordVisibility : ProfileUiEvent
    data object ToggleNewPasswordVisibility : ProfileUiEvent
    data object ToggleConfirmPasswordVisibility : ProfileUiEvent
    data object SubmitChangePasswordClicked : ProfileUiEvent

    // Card management
    data class ToggleAddCard(val isAdding: Boolean) : ProfileUiEvent
    data class CardHolderChanged(val value: String) : ProfileUiEvent
    data class CardNumberChanged(val value: String) : ProfileUiEvent
    data class CardExpiryChanged(val value: String) : ProfileUiEvent
    data object SubmitAddCardClicked : ProfileUiEvent
    data class DeleteCardClicked(val cardId: Long) : ProfileUiEvent

    // Session & navigation
    data class ShowLogoutDialog(val show: Boolean) : ProfileUiEvent
    data object LogoutClicked : ProfileUiEvent
    data object SwitchToGuestClicked : ProfileUiEvent
    data object LoginClicked : ProfileUiEvent
    data object BackClicked : ProfileUiEvent
}

sealed interface ProfileUiEffect {
    data object NavigateToAuth : ProfileUiEffect
    data object NavigateToHome : ProfileUiEffect
    data object NavigateBack : ProfileUiEffect
    data object ShowMemberRequiredSheet : ProfileUiEffect
    data class ShowSnackbar(val message: UiText) : ProfileUiEffect
}
