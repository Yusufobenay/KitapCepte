package com.kitapcepte.feature.detail

import androidx.lifecycle.SavedStateHandle
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
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val sessionFlow = MutableStateFlow<Session>(Session.LoggedIn(User(1L, "user@test.com", "Serdar")))
    private val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val cartIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val testBook = Book(
        id = "OL123W",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 99.90,
        category = BookCategory.FANTASY,
        description = "An unexpected journey"
    )

    private val similarBook = Book(
        id = "OL999W",
        title = "The Silmarillion",
        author = "J.R.R. Tolkien",
        coverUrl = null,
        price = 119.90,
        category = BookCategory.FANTASY
    )

    private lateinit var viewModel: DetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { sessionRepository.sessionState } returns sessionFlow
        every { bookRepository.getFavoriteBookIds() } returns favoriteIdsFlow
        every { bookRepository.getCartBookIds() } returns cartIdsFlow
        coEvery { bookRepository.getBookDetail("OL123W") } returns Result.success(testBook)
        coEvery { bookRepository.getSimilarBooks("OL123W", any()) } returns listOf(similarBook)

        val savedStateHandle = SavedStateHandle(mapOf("bookId" to "OL123W"))
        viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
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
    fun `initial load sets book detail and similar books`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.bookId).isEqualTo("OL123W")
        assertThat(state.book).isEqualTo(testBook)
        assertThat(state.similarBooks).containsExactly(similarBook)
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `BackClicked emits NavigateBack effect`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.BackClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(DetailUiEffect.NavigateBack)
        }
    }

    @Test
    fun `SimilarBookClicked emits NavigateToDetail effect`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.SimilarBookClicked("OL999W"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(DetailUiEffect.NavigateToDetail("OL999W"))
        }
    }

    @Test
    fun `FavoriteClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.FavoriteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(DetailUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `FavoriteClicked when logged in calls bookRepository toggleFavorite`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.FavoriteClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { bookRepository.toggleFavorite(testBook) }
    }

    @Test
    fun `AddToCartClicked when guest emits ShowMemberRequiredSheet`() = runTest(testDispatcher) {
        sessionFlow.value = Session.Guest
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.AddToCartClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(DetailUiEffect.ShowMemberRequiredSheet)
        }
    }

    @Test
    fun `AddToCartClicked when logged in toggles cart and shows added snackbar`() = runTest(testDispatcher) {
        coEvery { bookRepository.toggleCart(testBook) } returns true
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.AddToCartClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(DetailUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(testBook) }
        }
    }

    @Test
    fun `AddToCartClicked when already in cart toggles cart and shows removed snackbar`() = runTest(testDispatcher) {
        coEvery { bookRepository.toggleCart(testBook) } returns false
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.AddToCartClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(DetailUiEffect.ShowSnackbar::class.java)
            coVerify(exactly = 1) { bookRepository.toggleCart(testBook) }
        }
    }

    @Test
    fun `repository error sets errorMessage in uiState`() = runTest(testDispatcher) {
        coEvery { bookRepository.getBookDetail("OL123W") } returns Result.failure(IOException("Server timeout"))

        viewModel.onEvent(DetailUiEvent.RetryClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNotNull()
        assertThat(state.isLoading).isFalse()
    }
}
