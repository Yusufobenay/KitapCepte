package com.kitapcepte.feature.home

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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(User(1L, "user@test.com", "Serdar")))
    private val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val cartIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val sampleBook = Book(
        id = "OL1W",
        title = "Lord of the Rings",
        author = "Tolkien",
        coverUrl = null,
        price = 129.90,
        category = BookCategory.FANTASY
    )

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { bookRepository.getFavoriteBookIds() } returns favoriteIdsFlow
        every { bookRepository.getCartBookIds() } returns cartIdsFlow
        coEvery { bookRepository.getBooksByCategory(any()) } returns Result.success(listOf(sampleBook))

        viewModel = HomeViewModel(
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
    fun `initial load sets books and default category`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedCategory).isEqualTo(BookCategory.DEFAULT)
        assertThat(state.books).containsExactly(sampleBook)
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `CategorySelected updates category and loads books`() = runTest(testDispatcher) {
        val sciFiBook = sampleBook.copy(id = "OL2W", category = BookCategory.SCI_FI)
        coEvery { bookRepository.getBooksByCategory(BookCategory.SCI_FI) } returns Result.success(listOf(sciFiBook))

        viewModel.onEvent(HomeUiEvent.CategorySelected(BookCategory.SCI_FI))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedCategory).isEqualTo(BookCategory.SCI_FI)
        assertThat(state.books).containsExactly(sciFiBook)
    }

    @Test
    fun `selecting already selected category reverts to DEFAULT category`() = runTest(testDispatcher) {
        // First select SCI_FI
        viewModel.onEvent(HomeUiEvent.CategorySelected(BookCategory.SCI_FI))
        testDispatcher.scheduler.advanceUntilIdle()

        // Re-selecting SCI_FI should revert to DEFAULT
        viewModel.onEvent(HomeUiEvent.CategorySelected(BookCategory.SCI_FI))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedCategory).isEqualTo(BookCategory.DEFAULT)
    }

    @Test
    fun `BookClicked when user is guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.BookClicked("OL1W"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `BookClicked when user is logged in emits NavigateToDetail`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.BookClicked("OL1W"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.NavigateToDetail("OL1W"))
        }
    }

    @Test
    fun `FavoriteClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.FavoriteClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `FavoriteClicked when logged in invokes toggleFavorite on repository`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(HomeUiEvent.FavoriteClicked(sampleBook))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { bookRepository.toggleFavorite(sampleBook) }
    }

    @Test
    fun `PriceClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `PriceClicked when logged in toggles cart and shows snackbar`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        coEvery { bookRepository.toggleCart(sampleBook) } returns true
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(HomeUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(sampleBook) }
        }
    }

    @Test
    fun `PriceClicked when logged in and removed from cart shows removed snackbar`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        coEvery { bookRepository.toggleCart(sampleBook) } returns false
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.PriceClicked(sampleBook))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(HomeUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(sampleBook) }
        }
    }

    @Test
    fun `ProfileClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `ProfileClicked when logged in emits NavigateToProfile`() = runTest(testDispatcher) {
        sessionFlow.value = Session.LoggedIn(User(1L, "user@test.com", "Serdar"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(HomeUiEvent.ProfileClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(HomeUiEffect.NavigateToProfile)
        }
    }

    @Test
    fun `repository error sets errorMessage in uiState`() = runTest(testDispatcher) {
        coEvery { bookRepository.getBooksByCategory(BookCategory.MYSTERY) } returns Result.failure(IOException("Server error"))

        viewModel.onEvent(HomeUiEvent.CategorySelected(BookCategory.MYSTERY))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNotNull()
        assertThat(state.books).isEmpty()
    }
}
