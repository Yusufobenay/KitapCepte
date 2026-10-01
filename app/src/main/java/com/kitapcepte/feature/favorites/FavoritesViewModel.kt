package com.kitapcepte.feature.favorites

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
class FavoritesViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<FavoritesUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut

    init {
        observeSession()
        observeFavoritesAndCart()
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookRepository.getFavoriteBooks()
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = UiText.StringResource(R.string.error_generic)
                        )
                    }
                }
                .collectLatest { favoriteBooks ->
                    _uiState.update { state ->
                        state.copy(
                            books = favoriteBooks,
                            isLoading = false,
                            errorMessage = null
                        )
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

    fun onEvent(event: FavoritesUiEvent) {
        when (event) {
            is FavoritesUiEvent.BookClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.NavigateToDetail(event.bookId))
                        }
                    }
                )
            }
            is FavoritesUiEvent.FavoriteClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.io) {
                            val isAdded = bookRepository.toggleFavorite(event.book)
                            val message = if (isAdded) {
                                UiText.StringResource(R.string.book_added_to_cart, event.book.title)
                            } else {
                                UiText.StringResource(R.string.book_removed_from_favorites, event.book.title)
                            }
                            _uiEffect.send(FavoritesUiEffect.ShowSnackbar(message))
                        }
                    }
                )
            }
            is FavoritesUiEvent.PriceClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.ShowMemberRequiredSheet)
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
                            _uiEffect.send(FavoritesUiEffect.ShowSnackbar(message))
                        }
                    }
                )
            }
            FavoritesUiEvent.ProfileClicked -> {
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(FavoritesUiEffect.NavigateToProfile)
                        }
                    }
                )
            }
            FavoritesUiEvent.RetryClicked -> {
                observeFavoritesAndCart()
            }
        }
    }
}
