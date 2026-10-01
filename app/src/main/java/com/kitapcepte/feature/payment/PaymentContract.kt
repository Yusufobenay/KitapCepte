package com.kitapcepte.feature.payment

import com.kitapcepte.core.common.UiText

data class PaymentUiState(
    val cardHolder: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val saveCard: Boolean = false,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = 0.0,
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val orderNumber: String = "",
    val cardHolderError: UiText? = null,
    val cardNumberError: UiText? = null,
    val expiryError: UiText? = null,
    val cvvError: UiText? = null
) {
    val formattedCardNumber: String
        get() {
            val digits = cardNumber.filter { it.isDigit() }.take(16)
            return digits.chunked(4).joinToString(" ")
        }

    val formattedExpiry: String
        get() {
            val digits = expiryDate.filter { it.isDigit() }.take(4)
            return when {
                digits.length <= 2 -> digits
                else -> "${digits.substring(0, 2)}/${digits.substring(2)}"
            }
        }

    val cardBrand: String
        get() = when {
            cardNumber.startsWith("4") -> "VISA"
            cardNumber.startsWith("5") -> "MASTERCARD"
            cardNumber.startsWith("9") -> "TROY"
            else -> "KART"
        }
}

sealed interface PaymentUiEvent {
    data class CardHolderChanged(val value: String) : PaymentUiEvent
    data class CardNumberChanged(val value: String) : PaymentUiEvent
    data class ExpiryChanged(val value: String) : PaymentUiEvent
    data class CvvChanged(val value: String) : PaymentUiEvent
    data class SaveCardToggled(val value: Boolean) : PaymentUiEvent
    data object PayClicked : PaymentUiEvent
    data object ReturnHomeClicked : PaymentUiEvent
    data object BackClicked : PaymentUiEvent
}

sealed interface PaymentUiEffect {
    data object NavigateToHome : PaymentUiEffect
    data object NavigateBack : PaymentUiEffect
    data object ShowMemberRequiredSheet : PaymentUiEffect
    data class ShowSnackbar(val message: UiText) : PaymentUiEffect
}
