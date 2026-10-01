package com.kitapcepte.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.Book

@Composable
fun BookCard(
    book: Book,
    isInCart: Boolean,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPriceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .clickable(onClick = onCardClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(KitapCepteTheme.shapes.card)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                BookImage(
                    coverUrl = book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Fit
                )

                // Favorite button placed at top right
                FavoriteButton(
                    isFavorite = book.isFavorite,
                    onToggle = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Top Item badge at top left if top item
                if (book.isTopItem) {
                    TopItemBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Book Title
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price or "Sepette" Pill Button
            PriceButton(
                price = book.price,
                isInCart = isInCart,
                onClick = onPriceClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
