package com.kitapcepte.feature.payment

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PaymentUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut

    init {
        observeSession()
        observeCartTotal()
    }

    private fun observeSession() {
        viewModelScope.launch(dispatchers.io) {
            sessionRepository.sessionState.collectLatest { session ->
                currentSession = session
            }
        }
    }

    private fun observeCartTotal() {
        viewModelScope.launch(dispatchers.io) {
            bookRepository.getCartItems().collectLatest { cartItems ->
                val subtotal = cartItems.sumOf { it.price * it.quantity }
                val tax = subtotal * 0.10
                val shipping = if (subtotal > 150.0 || cartItems.isEmpty()) 0.0 else 29.90
                val total = subtotal + tax + shipping

                _uiState.update { state ->
                    state.copy(
                        subtotal = subtotal,
                        tax = tax,
                        shipping = shipping,
                        total = total
                    )
                }
            }
        }
    }

    fun onEvent(event: PaymentUiEvent) {
        when (event) {
            is PaymentUiEvent.CardHolderChanged -> {
                _uiState.update { it.copy(cardHolder = event.value, cardHolderError = null) }
            }
            is PaymentUiEvent.CardNumberChanged -> {
                val cleanDigits = event.value.filter { it.isDigit() }.take(16)
                _uiState.update { it.copy(cardNumber = cleanDigits, cardNumberError = null) }
            }
            is PaymentUiEvent.ExpiryChanged -> {
                val cleanDigits = event.value.filter { it.isDigit() }.take(4)
                _uiState.update { it.copy(expiryDate = cleanDigits, expiryError = null) }
            }
            is PaymentUiEvent.CvvChanged -> {
                val cleanDigits = event.value.filter { it.isDigit() }.take(3)
                _uiState.update { it.copy(cvv = cleanDigits, cvvError = null) }
            }
            is PaymentUiEvent.SaveCardToggled -> {
                _uiState.update { it.copy(saveCard = event.value) }
            }
            PaymentUiEvent.PayClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(PaymentUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        processPayment()
                    }
                )
            }
            PaymentUiEvent.ReturnHomeClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(PaymentUiEffect.NavigateToHome)
                }
            }
            PaymentUiEvent.BackClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(PaymentUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun processPayment() {
        val currentState = _uiState.value

        val holderValid = currentState.cardHolder.isNotBlank()
        val numberValid = currentState.cardNumber.length == 16
        val expiryDigits = currentState.expiryDate.filter { it.isDigit() }
        val month = expiryDigits.take(2).toIntOrNull() ?: 0
        val expiryValid = expiryDigits.length == 4 && month in 1..12
        val cvvValid = currentState.cvv.length == 3

        if (!holderValid || !numberValid || !expiryValid || !cvvValid) {
            _uiState.update {
                it.copy(
                    cardHolderError = if (!holderValid) UiText.StringResource(R.string.payment_error_card_holder) else null,
                    cardNumberError = if (!numberValid) UiText.StringResource(R.string.payment_error_card_number) else null,
                    expiryError = if (!expiryValid) UiText.StringResource(R.string.payment_error_expiry) else null,
                    cvvError = if (!cvvValid) UiText.StringResource(R.string.payment_error_cvv) else null
                )
            }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isProcessing = true) }
            bookRepository.clearCart()
            val orderNo = System.currentTimeMillis().toString().takeLast(6)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    isSuccess = true,
                    orderNumber = orderNo
                )
            }
        }
    }
}
