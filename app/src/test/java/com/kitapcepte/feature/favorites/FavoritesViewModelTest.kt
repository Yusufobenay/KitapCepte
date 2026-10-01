package com.kitapcepte.feature.favorites

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.TestDispatcherProvider
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.SessionRepository
import io.mockk.coEvery
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
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(User(1L, "user@test.com", "Serdar")))
    private val favoriteBooksFlow = MutableStateFlow<List<Book>>(emptyList())
    private val cartIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val sampleBook = Book(
        id = "OL123W",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 99.90,
        category = BookCategory.FANTASY,
        isFavorite = true
    )

    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { bookRepository.getFavoriteBooks() } returns favoriteBooksFlow
        every { bookRepository.getCartBookIds() } returns cartIdsFlow

        viewModel = FavoritesViewModel(
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
    fun `initial load with favorite books sets state correctly`() = runTest(testDispatcher) {
        favoriteBooksFlow.value = listOf(sampleBook)
        cartIdsFlow.value = setOf("OL123W")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.books).containsExactly(sampleBook)
        assertThat(state.cartBookIds).containsExactly("OL123W")
        assertThat(state.isLoading).isFalse()
        assertThat(state.isEmpty).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `initial load with empty favorites sets isEmpty true`() = runTest(testDispatcher) {
        favoriteBooksFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.books).isEmpty()
        assertThat(state.isEmpty).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `BookClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.BookClicked("OL123W"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `BookClicked when logged in emits NavigateToDetail`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.BookClicked("OL123W"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.NavigateToDetail("OL123W"))
        }
    }

    @Test
    fun `FavoriteClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.FavoriteClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `FavoriteClicked when logged in toggles favorite and shows snackbar`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        coEvery { bookRepository.toggleFavorite(sampleBook) } returns false
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.FavoriteClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(FavoritesUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleFavorite(sampleBook) }
        }
    }

    @Test
    fun `PriceClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `PriceClicked when logged in and added to cart shows added snackbar`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        coEvery { bookRepository.toggleCart(sampleBook) } returns true
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(FavoritesUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(sampleBook) }
        }
    }

    @Test
    fun `PriceClicked when logged in and removed from cart shows removed snackbar`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        coEvery { bookRepository.toggleCart(sampleBook) } returns false
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(FavoritesUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(sampleBook) }
        }
    }

    @Test
    fun `ProfileClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `ProfileClicked when logged in emits NavigateToProfile`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(FavoritesUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(FavoritesUiEffect.NavigateToProfile)
        }
    }

    @Test
    fun `RetryClicked triggers observeFavoritesAndCart`() = runTest(testDispatcher) {
        viewModel.onEvent(FavoritesUiEvent.RetryClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { bookRepository.getFavoriteBooks() }
    }
}
