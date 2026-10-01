package com.kitapcepte.feature.payment

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.TestDispatcherProvider
import com.kitapcepte.domain.model.CartItem
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(User(1L, "user@test.com", "Serdar")))
    private val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    private val cartItem = CartItem(
        id = 1L,
        bookId = "OL1W",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 100.0,
        quantity = 2
    )

    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { bookRepository.getCartItems() } returns cartItemsFlow

        viewModel = PaymentViewModel(
            bookRepository = bookRepository,
            sessionRepository = sessionRepository,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load observes cart and calculates total correctly`() = runTest(testDispatcher) {
        cartItemsFlow.value = listOf(cartItem)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.subtotal).isEqualTo(200.0)
        assertThat(state.tax).isEqualTo(20.0)
        assertThat(state.shipping).isEqualTo(0.0)
        assertThat(state.total).isEqualTo(220.0)
    }

    @Test
    fun `CardHolderChanged updates state`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.CardHolderChanged("Serdar Akçay"))

        val state = viewModel.uiState.value
        assertThat(state.cardHolder).isEqualTo("Serdar Akçay")
        assertThat(state.cardHolderError).isNull()
    }

    @Test
    fun `CardNumberChanged formats digits and detects brand`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.CardNumberChanged("4123456789012345"))

        val state = viewModel.uiState.value
        assertThat(state.cardNumber).isEqualTo("4123456789012345")
        assertThat(state.formattedCardNumber).isEqualTo("4123 4567 8901 2345")
        assertThat(state.cardBrand).isEqualTo("VISA")

        viewModel.onEvent(PaymentUiEvent.CardNumberChanged("5123456789012345"))
        assertThat(viewModel.uiState.value.cardBrand).isEqualTo("MASTERCARD")
    }

    @Test
    fun `ExpiryChanged formats to month and year`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.ExpiryChanged("1228"))

        val state = viewModel.uiState.value
        assertThat(state.expiryDate).isEqualTo("1228")
        assertThat(state.formattedExpiry).isEqualTo("12/28")
    }

    @Test
    fun `CvvChanged filters non digits and caps at 3`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.CvvChanged("12345"))

        val state = viewModel.uiState.value
        assertThat(state.cvv).isEqualTo("123")
    }

    @Test
    fun `PayClicked with invalid inputs sets validation errors`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.CardHolderChanged(""))
        viewModel.onEvent(PaymentUiEvent.CardNumberChanged("1234"))
        viewModel.onEvent(PaymentUiEvent.ExpiryChanged("9999"))
        viewModel.onEvent(PaymentUiEvent.CvvChanged("1"))

        viewModel.onEvent(PaymentUiEvent.PayClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.cardHolderError).isNotNull()
        assertThat(state.cardNumberError).isNotNull()
        assertThat(state.expiryError).isNotNull()
        assertThat(state.cvvError).isNotNull()
        assertThat(state.isSuccess).isFalse()
    }

    @Test
    fun `PayClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(PaymentUiEvent.PayClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(PaymentUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `PayClicked with valid inputs clears cart and sets isSuccess true`() = runTest(testDispatcher) {
        viewModel.onEvent(PaymentUiEvent.CardHolderChanged("Serdar Akçay"))
        viewModel.onEvent(PaymentUiEvent.CardNumberChanged("5428000012345678"))
        viewModel.onEvent(PaymentUiEvent.ExpiryChanged("1228"))
        viewModel.onEvent(PaymentUiEvent.CvvChanged("345"))

        viewModel.onEvent(PaymentUiEvent.PayClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isSuccess).isTrue()
        assertThat(state.orderNumber).isNotEmpty()
        coVerify(exactly = 1) { bookRepository.clearCart() }
    }

    @Test
    fun `ReturnHomeClicked emits NavigateToHome`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(PaymentUiEvent.ReturnHomeClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(PaymentUiEffect.NavigateToHome)
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(PaymentUiEvent.BackClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(PaymentUiEffect.NavigateBack)
        }
    }
}
