package com.kitapcepte.feature.cart

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
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(User(1L, "user@test.com", "Serdar")))
    private val cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())

    private val item1 = CartItem(
        id = 1L,
        bookId = "OL1W",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 100.0,
        quantity = 1
    )

    private val item2 = CartItem(
        id = 2L,
        bookId = "OL2W",
        title = "The Silmarillion",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 200.0,
        quantity = 1
    )

    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { bookRepository.getCartItems() } returns cartItemsFlow

        viewModel = CartViewModel(
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
    fun `initial load calculates subtotal, tax, shipping, and total correctly`() = runTest(testDispatcher) {
        cartItemsFlow.value = listOf(item1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.items).containsExactly(item1)
        assertThat(state.subtotal).isEqualTo(100.0)
        assertThat(state.tax).isEqualTo(10.0)
        assertThat(state.shipping).isEqualTo(29.90)
        assertThat(state.total).isEqualTo(139.90)
        assertThat(state.isLoading).isFalse()
        assertThat(state.isEmpty).isFalse()
    }

    @Test
    fun `initial load qualifies for free shipping when subtotal above 150`() = runTest(testDispatcher) {
        cartItemsFlow.value = listOf(item2)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.items).containsExactly(item2)
        assertThat(state.subtotal).isEqualTo(200.0)
        assertThat(state.tax).isEqualTo(20.0)
        assertThat(state.shipping).isEqualTo(0.0)
        assertThat(state.total).isEqualTo(220.0)
    }

    @Test
    fun `initial load with empty cart sets isEmpty true and zero totals`() = runTest(testDispatcher) {
        cartItemsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.items).isEmpty()
        assertThat(state.isEmpty).isTrue()
        assertThat(state.subtotal).isEqualTo(0.0)
        assertThat(state.total).isEqualTo(0.0)
    }

    @Test
    fun `QuantityChanged invokes repository updateCartQuantity`() = runTest(testDispatcher) {
        viewModel.onEvent(CartUiEvent.QuantityChanged(1L, 3))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { bookRepository.updateCartQuantity(1L, 3) }
    }

    @Test
    fun `RemoveItem invokes repository removeCartItem and emits snackbar`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(CartUiEvent.RemoveItem(1L, "The Hobbit"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(CartUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.removeCartItem(1L) }
        }
    }

    @Test
    fun `CheckoutClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(CartUiEvent.CheckoutClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CartUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `CheckoutClicked when logged in emits NavigateToPayment`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(CartUiEvent.CheckoutClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CartUiEffect.NavigateToPayment)
        }
    }

    @Test
    fun `ProfileClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(CartUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CartUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `ProfileClicked when logged in emits NavigateToProfile`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(CartUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CartUiEffect.NavigateToProfile)
        }
    }

    @Test
    fun `RetryClicked triggers observeCart`() = runTest(testDispatcher) {
        viewModel.onEvent(CartUiEvent.RetryClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { bookRepository.getCartItems() }
    }
}
