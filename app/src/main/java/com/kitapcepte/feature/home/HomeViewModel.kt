package com.kitapcepte.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.R
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.core.navigation.GuestGuard
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
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
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut

    init {
        observeSession()
        observeFavoritesAndCart()
        loadBooks(BookCategory.DEFAULT)
    }

    private fun observeSession() {
        viewModelScope.launch(dispatchers.io) {
            sessionRepository.sessionState.collectLatest { session ->
                currentSession = session
            }
        }
    }

    private fun observeFavoritesAndCart() {
        viewModelScope.launch(dispatchers.io) {
            bookRepository.getFavoriteBookIds().collectLatest { favoriteIds ->
                _uiState.update { state ->
                    val updatedBooks = state.books.map { book ->
                        book.copy(isFavorite = book.id in favoriteIds)
                    }
                    state.copy(favoriteBookIds = favoriteIds, books = updatedBooks)
                }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            bookRepository.getCartBookIds().collectLatest { cartIds ->
                _uiState.update { state ->
                    state.copy(cartBookIds = cartIds)
                }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.CategorySelected -> {
                val nextCategory = if (_uiState.value.selectedCategory == event.category) {
                    BookCategory.DEFAULT
                } else {
                    event.category
                }
                if (nextCategory != _uiState.value.selectedCategory) {
                    loadBooks(nextCategory)
                }
            }
            is HomeUiEvent.BookClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.NavigateToDetail(event.bookId))
                        }
                    }
                )
            }
            is HomeUiEvent.FavoriteClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.io) {
                            bookRepository.toggleFavorite(event.book)
                        }
                    }
                )
            }
            is HomeUiEvent.PriceClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.io) {
                            val added = bookRepository.toggleCart(event.book)
                            val message = if (added) {
                                UiText.StringResource(R.string.book_added_to_cart, event.book.title)
                            } else {
                                UiText.StringResource(R.string.book_removed_from_cart, event.book.title)
                            }
                            _uiEffect.send(HomeUiEffect.ShowSnackbar(message))
                        }
                    }
                )
            }
            HomeUiEvent.ProfileClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(HomeUiEffect.NavigateToProfile)
                        }
                    }
                )
            }
            HomeUiEvent.RetryClicked -> {
                loadBooks(_uiState.value.selectedCategory)
            }
        }
    }

    private fun loadBooks(category: BookCategory) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(selectedCategory = category, isLoading = true, errorMessage = null) }
            val result = bookRepository.getBooksByCategory(category)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { fetchedBooks ->
                        val favoriteIds = state.favoriteBookIds
                        val booksWithFavorites = fetchedBooks.map { book ->
                            book.copy(isFavorite = book.id in favoriteIds)
                        }
                        state.copy(books = booksWithFavorites, isLoading = false, errorMessage = null)
                    },
                    onFailure = {
                        state.copy(
                            books = emptyList(),
                            isLoading = false,
                            errorMessage = UiText.StringResource(R.string.network_error)
                        )
                    }
                )
            }
        }
    }
}
