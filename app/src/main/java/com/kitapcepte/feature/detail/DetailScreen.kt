package com.kitapcepte.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.component.AppTopBar
import com.kitapcepte.core.designsystem.component.BackNavigationButton
import com.kitapcepte.core.designsystem.component.BookCard
import com.kitapcepte.core.designsystem.component.BookImage
import com.kitapcepte.core.designsystem.component.ErrorView
import com.kitapcepte.core.designsystem.component.FavoriteButton
import com.kitapcepte.core.designsystem.component.LoadingView
import com.kitapcepte.core.designsystem.component.PrimaryButton
import com.kitapcepte.core.designsystem.component.SecondaryButton
import com.kitapcepte.core.designsystem.component.TopItemBadge
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory

@Composable
fun DetailScreen(
    state: DetailUiState,
    onEvent: (DetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopBar: Back, Title, Favorite
            AppTopBar(
                title = stringResource(id = R.string.detail_title),
                navigationIcon = {
                    BackNavigationButton(
                        onBackClick = { onEvent(DetailUiEvent.BackClicked) }
                    )
                },
                actions = {
                    FavoriteButton(
                        isFavorite = state.isFavorite,
                        onToggle = { onEvent(DetailUiEvent.FavoriteClicked) }
                    )
                }
            )

            when {
                state.isLoading -> {
                    LoadingView()
                }
                state.errorMessage != null -> {
                    ErrorView(
                        message = state.errorMessage,
                        onRetry = { onEvent(DetailUiEvent.RetryClicked) }
                    )
                }
                state.book != null -> {
                    val book = state.book
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 100.dp)
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Large Cover Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .shadow(elevation = 6.dp, shape = KitapCepteTheme.shapes.card)
                                    .clip(KitapCepteTheme.shapes.card)
                                    .background(KitapCepteTheme.extendedColors.surfaceWhite)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BookImage(
                                    coverUrl = book.coverUrl,
                                    contentDescription = book.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                                if (book.isTopItem) {
                                    TopItemBadge(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Title & Author Info Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = KitapCepteTheme.shapes.card)
                                    .clip(KitapCepteTheme.shapes.card)
                                    .background(KitapCepteTheme.extendedColors.surfaceWhite)
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = book.author,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (book.firstPublishYear != null || book.editionCount > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            book.firstPublishYear?.let { year ->
                                                DetailBadge(text = "$year Basımı")
                                            }
                                            if (book.editionCount > 0) {
                                                DetailBadge(text = "${book.editionCount} Edisyon")
                                            }
                                        }
                                    }
                                }
                            }

                            // Book Description
                            if (!book.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(elevation = 2.dp, shape = KitapCepteTheme.shapes.card)
                                        .clip(KitapCepteTheme.shapes.card)
                                        .background(KitapCepteTheme.extendedColors.surfaceWhite)
                                        .padding(18.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.book_description_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = book.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Similar Books
                            if (state.similarBooks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = stringResource(id = R.string.similar_books_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(end = 16.dp)
                                ) {
                                    items(
                                        items = state.similarBooks,
                                        key = { it.id }
                                    ) { similar ->
                                        BookCard(
                                            book = similar,
                                            isInCart = similar.id in state.cartBookIds,
                                            onCardClick = { onEvent(DetailUiEvent.SimilarBookClicked(similar.id)) },
                                            onFavoriteToggle = {},
                                            onPriceClick = { onEvent(DetailUiEvent.SimilarBookClicked(similar.id)) },
                                            modifier = Modifier.width(160.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Floating Bar (Price + Cart button)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .shadow(elevation = 12.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(KitapCepteTheme.extendedColors.surfaceWhite.copy(alpha = 0.96f))
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    if (book.originalPrice != null) {
                                        Text(
                                            text = "${"%.2f".format(book.originalPrice)} ₺",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                textDecoration = TextDecoration.LineThrough
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${"%.2f".format(book.price)} ₺",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (state.isInCart) {
                                    SecondaryButton(
                                        text = stringResource(id = R.string.btn_remove_from_cart),
                                        onClick = { onEvent(DetailUiEvent.AddToCartClicked) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                } else {
                                    PrimaryButton(
                                        text = stringResource(id = R.string.btn_add_to_cart),
                                        onClick = { onEvent(DetailUiEvent.AddToCartClicked) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
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

@Composable
private fun DetailBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(KitapCepteTheme.shapes.buttonPill)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    KitapCepteTheme {
        DetailScreen(
            state = DetailUiState(
                book = Book(
                    id = "OL1W",
                    title = "Yüzüklerin Efendisi",
                    author = "J.R.R. Tolkien",
                    coverUrl = null,
                    price = 149.90,
                    originalPrice = 199.90,
                    isTopItem = true,
                    isFavorite = true,
                    description = "Yüzük Kardeşliği Orta Dünya'da büyük bir maceraya atılıyor."
                ),
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
