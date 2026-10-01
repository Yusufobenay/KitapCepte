package com.kitapcepte.feature.favorites

import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.Book

data class FavoritesUiState(
    val books: List<Book> = emptyList(),
    val cartBookIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && books.isEmpty()
}

sealed interface FavoritesUiEvent {
    data class BookClicked(val bookId: String) : FavoritesUiEvent
    data class FavoriteClicked(val book: Book) : FavoritesUiEvent
    data class PriceClicked(val book: Book) : FavoritesUiEvent
    data object ProfileClicked : FavoritesUiEvent
    data object RetryClicked : FavoritesUiEvent
}

sealed interface FavoritesUiEffect {
    data class NavigateToDetail(val bookId: String) : FavoritesUiEffect
    data object NavigateToProfile : FavoritesUiEffect
    data object ShowMemberRequiredSheet : FavoritesUiEffect
    data class ShowSnackbar(val message: UiText) : FavoritesUiEffect
}
