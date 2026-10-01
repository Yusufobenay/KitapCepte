package com.kitapcepte.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.kitapcepte.core.designsystem.component.CartItemCard
import com.kitapcepte.core.designsystem.component.EmptyView
import com.kitapcepte.core.designsystem.component.ErrorView
import com.kitapcepte.core.designsystem.component.LoadingView
import com.kitapcepte.core.designsystem.component.OrderSummaryCard
import com.kitapcepte.core.designsystem.component.PrimaryButton
import com.kitapcepte.core.designsystem.component.ProfileActionButton
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.CartItem
import java.util.Locale

@Composable
fun CartScreen(
    state: CartUiState,
    onEvent: (CartUiEvent) -> Unit,
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
            // TopBar: "Sepet" with Profile button at top-right
            AppTopBar(
                title = stringResource(id = R.string.nav_cart),
                actions = {
                    ProfileActionButton(
                        onProfileClick = { onEvent(CartUiEvent.ProfileClicked) }
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
                            onRetry = { onEvent(CartUiEvent.RetryClicked) }
                        )
                    }
                    state.isEmpty -> {
                        EmptyView(
                            title = stringResource(id = R.string.empty_cart)
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 104.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Cart items
                            state.items.forEach { item ->
                                CartItemCard(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        onEvent(CartUiEvent.QuantityChanged(item.id, newQuantity))
                                    },
                                    onRemoveConfirmed = {
                                        onEvent(CartUiEvent.RemoveItem(item.id, item.title))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Order summary
                            OrderSummaryCard(
                                subtotal = state.subtotal,
                                tax = state.tax,
                                shipping = state.shipping
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Checkout Button
                            PrimaryButton(
                                text = "${stringResource(id = R.string.btn_checkout)} (${String.format(Locale.US, "%.2f", state.total)} ₺)",
                                onClick = { onEvent(CartUiEvent.CheckoutClicked) },
                                modifier = Modifier.fillMaxWidth()
                            )
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
private fun CartScreenPreview() {
    KitapCepteTheme {
        val sampleItems = listOf(
            CartItem(
                id = 1L,
                bookId = "OL1W",
                title = "Yüzüklerin Efendisi",
                author = "J.R.R. Tolkien",
                coverUrl = null,
                price = 149.90,
                quantity = 2
            ),
            CartItem(
                id = 2L,
                bookId = "OL2W",
                title = "Harry Potter ve Felsefe Taşı",
                author = "J.K. Rowling",
                coverUrl = null,
                price = 89.90,
                quantity = 1
            )
        )
        val subtotal = sampleItems.sumOf { it.price * it.quantity }
        val tax = subtotal * 0.10
        val shipping = 0.0
        val total = subtotal + tax + shipping

        CartScreen(
            state = CartUiState(
                items = sampleItems,
                subtotal = subtotal,
                tax = tax,
                shipping = shipping,
                total = total,
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
