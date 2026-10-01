package com.kitapcepte.feature.cart

import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.CartItem

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && items.isEmpty()
}

sealed interface CartUiEvent {
    data class QuantityChanged(val itemId: Long, val quantity: Int) : CartUiEvent
    data class RemoveItem(val itemId: Long, val itemTitle: String = "") : CartUiEvent
    data object CheckoutClicked : CartUiEvent
    data object ProfileClicked : CartUiEvent
    data object RetryClicked : CartUiEvent
}

sealed interface CartUiEffect {
    data object NavigateToPayment : CartUiEffect
    data object NavigateToProfile : CartUiEffect
    data object ShowMemberRequiredSheet : CartUiEffect
    data class ShowSnackbar(val message: UiText) : CartUiEffect
}
