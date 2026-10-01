package com.kitapcepte.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import com.kitapcepte.core.designsystem.component.AppChipRow
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
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
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
        // TopBar: "Kitap Cepte" with Profile button at top-right
        AppTopBar(
            title = stringResource(id = R.string.app_name),
            actions = {
                ProfileActionButton(
                    onProfileClick = { onEvent(HomeUiEvent.ProfileClicked) }
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Categories Chip Row
        AppChipRow(
            items = state.categories,
            selectedItem = state.selectedCategory,
            itemLabel = { it.displayName },
            onItemSelected = { onEvent(HomeUiEvent.CategorySelected(it)) },
            contentPadding = PaddingValues(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                        onRetry = { onEvent(HomeUiEvent.RetryClicked) }
                    )
                }
                state.isEmpty -> {
                    EmptyView(
                        title = stringResource(id = R.string.empty_books)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
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
                                onCardClick = { onEvent(HomeUiEvent.BookClicked(book.id)) },
                                onFavoriteToggle = { onEvent(HomeUiEvent.FavoriteClicked(book)) },
                                onPriceClick = { onEvent(HomeUiEvent.PriceClicked(book)) }
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
private fun HomeScreenPreview() {
    KitapCepteTheme {
        val sampleBooks = listOf(
            Book(
                id = "1",
                title = "Yüzüklerin Efendisi: Yüzük Kardeşliği",
                author = "J.R.R. Tolkien",
                coverUrl = null,
                price = 149.90,
                originalPrice = 199.90,
                isTopItem = true,
                isFavorite = true
            ),
            Book(
                id = "2",
                title = "Harry Potter ve Felsefe Taşı",
                author = "J.K. Rowling",
                coverUrl = null,
                price = 89.90,
                isTopItem = false,
                isFavorite = false
            )
        )
        HomeScreen(
            state = HomeUiState(books = sampleBooks),
            onEvent = {}
        )
    }
}
