package com.kitapcepte.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.R
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.core.navigation.GuestGuard
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CartUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut

    init {
        observeSession()
        observeCart()
    }

    private fun observeSession() {
        viewModelScope.launch(dispatchers.io) {
            sessionRepository.sessionState.collectLatest { session ->
                currentSession = session
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookRepository.getCartItems()
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = UiText.StringResource(R.string.error_generic)
                        )
                    }
                }
                .collectLatest { cartItems ->
                    val subtotal = cartItems.sumOf { it.price * it.quantity }
                    val tax = subtotal * 0.10
                    val shipping = if (subtotal > 150.0 || cartItems.isEmpty()) 0.0 else 29.90
                    val total = subtotal + tax + shipping

                    _uiState.update { state ->
                        state.copy(
                            items = cartItems,
                            subtotal = subtotal,
                            tax = tax,
                            shipping = shipping,
                            total = total,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.QuantityChanged -> {
                viewModelScope.launch(dispatchers.io) {
                    bookRepository.updateCartQuantity(event.itemId, event.quantity)
                }
            }
            is CartUiEvent.RemoveItem -> {
                viewModelScope.launch(dispatchers.io) {
                    bookRepository.removeCartItem(event.itemId)
                    val message = if (event.itemTitle.isNotBlank()) {
                        UiText.StringResource(R.string.book_removed_from_cart, event.itemTitle)
                    } else {
                        UiText.StringResource(R.string.book_removed_from_cart, "")
                    }
                    _uiEffect.send(CartUiEffect.ShowSnackbar(message))
                }
            }
            CartUiEvent.CheckoutClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(CartUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(CartUiEffect.NavigateToPayment)
                        }
                    }
                )
            }
            CartUiEvent.ProfileClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(CartUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(CartUiEffect.NavigateToProfile)
                        }
                    }
                )
            }
            CartUiEvent.RetryClicked -> {
                observeCart()
            }
        }
    }
}
