package com.kitapcepte.feature.detail

import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.Book

data class DetailUiState(
    val bookId: String = "",
    val book: Book? = null,
    val similarBooks: List<Book> = emptyList(),
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val cartBookIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)

sealed interface DetailUiEvent {
    data object FavoriteClicked : DetailUiEvent
    data object AddToCartClicked : DetailUiEvent
    data class SimilarBookClicked(val bookId: String) : DetailUiEvent
    data object BackClicked : DetailUiEvent
    data object RetryClicked : DetailUiEvent
}

sealed interface DetailUiEffect {
    data object NavigateBack : DetailUiEffect
    data class NavigateToDetail(val bookId: String) : DetailUiEffect
    data object ShowMemberRequiredSheet : DetailUiEffect
    data class ShowSnackbar(val message: UiText) : DetailUiEffect
}
