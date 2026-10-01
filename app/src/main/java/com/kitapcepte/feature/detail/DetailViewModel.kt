package com.kitapcepte.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kitapcepte.R
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.core.common.UiText
import com.kitapcepte.core.navigation.GuestGuard
import com.kitapcepte.core.navigation.Screen
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
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    val bookId: String = savedStateHandle.get<String>("bookId")
        ?: runCatching { savedStateHandle.toRoute<Screen.Detail>().bookId }.getOrDefault("")

    private val _uiState = MutableStateFlow(DetailUiState(bookId = bookId))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DetailUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentSession: Session = Session.LoggedOut

    init {
        observeSession()
        observeFavoritesAndCart()
        loadBookDetail()
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
                val isFav = bookId in favoriteIds
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            bookRepository.getCartBookIds().collectLatest { cartIds ->
                val inCart = bookId in cartIds
                _uiState.update { it.copy(isInCart = inCart, cartBookIds = cartIds) }
            }
        }
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            DetailUiEvent.BackClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(DetailUiEffect.NavigateBack)
                }
            }
            is DetailUiEvent.SimilarBookClicked -> {
                viewModelScope.launch(dispatchers.main) {
                    _uiEffect.send(DetailUiEffect.NavigateToDetail(event.bookId))
                }
            }
            DetailUiEvent.FavoriteClicked -> {
                val book = _uiState.value.book ?: return
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(DetailUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.io) {
                            bookRepository.toggleFavorite(book)
                        }
                    }
                )
            }
            DetailUiEvent.AddToCartClicked -> {
                val book = _uiState.value.book ?: return
                GuestGuard.check(
                    session = currentSession,
                    onGuestRestricted = {
                        viewModelScope.launch(dispatchers.main) {
                            _uiEffect.send(DetailUiEffect.ShowMemberRequiredSheet)
                        }
                    },
                    onAllowed = {
                        viewModelScope.launch(dispatchers.io) {
                            val added = bookRepository.toggleCart(book)
                            val message = if (added) {
                                UiText.StringResource(R.string.book_added_to_cart, book.title)
                            } else {
                                UiText.StringResource(R.string.book_removed_from_cart, book.title)
                            }
                            _uiEffect.send(DetailUiEffect.ShowSnackbar(message))
                        }
                    }
                )
            }
            DetailUiEvent.RetryClicked -> {
                loadBookDetail()
            }
        }
    }

    private fun loadBookDetail() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val detailResult = bookRepository.getBookDetail(bookId)
            detailResult.fold(
                onSuccess = { book ->
                    val similar = bookRepository.getSimilarBooks(bookId, book.category)
                    _uiState.update {
                        it.copy(
                            book = book,
                            similarBooks = similar,
                            isFavorite = book.isFavorite,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.StringResource(R.string.network_error)
                        )
                    }
                }
            )
        }
    }
}
