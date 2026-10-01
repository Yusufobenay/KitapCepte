package com.kitapcepte.feature.home

import com.kitapcepte.core.common.UiText
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory

data class HomeUiState(
    val selectedCategory: BookCategory = BookCategory.DEFAULT,
    val categories: List<BookCategory> = BookCategory.entries,
    val books: List<Book> = emptyList(),
    val favoriteBookIds: Set<String> = emptySet(),
    val cartBookIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && books.isEmpty()
}

sealed interface HomeUiEvent {
    data class CategorySelected(val category: BookCategory) : HomeUiEvent
    data class BookClicked(val bookId: String) : HomeUiEvent
    data class FavoriteClicked(val book: Book) : HomeUiEvent
    data class PriceClicked(val book: Book) : HomeUiEvent
    data object ProfileClicked : HomeUiEvent
    data object RetryClicked : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class NavigateToDetail(val bookId: String) : HomeUiEffect
    data object NavigateToProfile : HomeUiEffect
    data object ShowMemberRequiredSheet : HomeUiEffect
    data class ShowSnackbar(val message: UiText) : HomeUiEffect
}
