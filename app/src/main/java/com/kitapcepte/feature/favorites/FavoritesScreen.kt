package com.kitapcepte.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.component.AppTopBar
import com.kitapcepte.core.designsystem.component.BookCard
import com.kitapcepte.core.designsystem.component.EmptyView
import com.kitapcepte.core.designsystem.component.ErrorView
import com.kitapcepte.core.designsystem.component.LoadingView
import com.kitapcepte.core.designsystem.component.ProfileActionButton
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onEvent: (FavoritesUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopBar: "Favoriler" with Profile button at top-right
            AppTopBar(
                title = stringResource(id = R.string.nav_favorites),
                actions = {
                    ProfileActionButton(
                        onProfileClick = { onEvent(FavoritesUiEvent.ProfileClicked) }
                    )
                }
            )

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when {
                    state.isLoading -> {
                        LoadingView()
                    }
                    state.errorMessage != null -> {
                        ErrorView(
                            message = state.errorMessage,
                            onRetry = { onEvent(FavoritesUiEvent.RetryClicked) }
                        )
                    }
                    state.isEmpty -> {
                        EmptyView(
                            title = stringResource(id = R.string.empty_favorites)
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.books,
                                key = { it.id }
                            ) { book ->
                                BookCard(
                                    book = book,
                                    isInCart = book.id in state.cartBookIds,
                                    onCardClick = { onEvent(FavoritesUiEvent.BookClicked(book.id)) },
                                    onFavoriteToggle = { onEvent(FavoritesUiEvent.FavoriteClicked(book)) },
                                    onPriceClick = { onEvent(FavoritesUiEvent.PriceClicked(book)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    KitapCepteTheme {
        val sampleBooks = listOf(
            Book(
                id = "1",
                title = "Yüzüklerin Efendisi",
                author = "J.R.R. Tolkien",
                coverUrl = null,
                price = 149.90,
                originalPrice = 199.90,
                category = BookCategory.FANTASY,
                isTopItem = true,
                isFavorite = true
            ),
            Book(
                id = "2",
                title = "Harry Potter ve Felsefe Taşı",
                author = "J.K. Rowling",
                coverUrl = null,
                price = 89.90,
                category = BookCategory.FANTASY,
                isTopItem = false,
                isFavorite = true
            )
        )
        FavoritesScreen(
            state = FavoritesUiState(
                books = sampleBooks,
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
