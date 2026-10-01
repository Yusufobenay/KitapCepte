package com.kitapcepte.feature.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.component.AppTopBar
import com.kitapcepte.core.designsystem.component.BackNavigationButton
import com.kitapcepte.core.designsystem.component.PrimaryButton
import com.kitapcepte.core.designsystem.component.SummaryRow
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import java.util.Locale

@Composable
fun PaymentScreen(
    state: PaymentUiState,
    onEvent: (PaymentUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopBar: Back button and "Ödeme"
            AppTopBar(
                title = stringResource(id = R.string.payment_title),
                navigationIcon = {
                    BackNavigationButton(
                        onBackClick = { onEvent(PaymentUiEvent.BackClicked) }
                    )
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Credit Card Preview Card
                CreditCardPreview(state = state)

                // Input Fields Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = KitapCepteTheme.shapes.card)
                        .clip(KitapCepteTheme.shapes.card)
                        .background(KitapCepteTheme.extendedColors.surfaceWhite)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Cardholder Name
                        OutlinedTextField(
                            value = state.cardHolder,
                            onValueChange = { onEvent(PaymentUiEvent.CardHolderChanged(it)) },
                            label = { Text(stringResource(id = R.string.payment_card_holder_label)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            isError = state.cardHolderError != null,
                            supportingText = state.cardHolderError?.let {
                                { Text(it.asString(context), color = MaterialTheme.colorScheme.error) }
                            },
                            shape = KitapCepteTheme.shapes.input,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                            )
                        )

                        // Card Number
                        OutlinedTextField(
                            value = state.formattedCardNumber,
                            onValueChange = { onEvent(PaymentUiEvent.CardNumberChanged(it)) },
                            label = { Text(stringResource(id = R.string.payment_card_number_label)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            isError = state.cardNumberError != null,
                            supportingText = state.cardNumberError?.let {
                                { Text(it.asString(context), color = MaterialTheme.colorScheme.error) }
                            },
                            shape = KitapCepteTheme.shapes.input,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                            )
                        )

                        // Expiry Date and CVV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.formattedExpiry,
                                onValueChange = { onEvent(PaymentUiEvent.ExpiryChanged(it)) },
                                label = { Text(stringResource(id = R.string.payment_expiry_label)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                isError = state.expiryError != null,
                                supportingText = state.expiryError?.let {
                                    { Text(it.asString(context), color = MaterialTheme.colorScheme.error) }
                                },
                                shape = KitapCepteTheme.shapes.input,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                                )
                            )

                            OutlinedTextField(
                                value = state.cvv,
                                onValueChange = { onEvent(PaymentUiEvent.CvvChanged(it)) },
                                label = { Text(stringResource(id = R.string.payment_cvv_label)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                isError = state.cvvError != null,
                                supportingText = state.cvvError?.let {
                                    { Text(it.asString(context), color = MaterialTheme.colorScheme.error) }
                                },
                                shape = KitapCepteTheme.shapes.input,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = KitapCepteTheme.extendedColors.borderLight
                                )
                            )
                        }

                        // Save Card Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(PaymentUiEvent.SaveCardToggled(!state.saveCard)) }
                        ) {
                            Checkbox(
                                checked = state.saveCard,
                                onCheckedChange = { onEvent(PaymentUiEvent.SaveCardToggled(it)) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.payment_save_card),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Total Amount Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = KitapCepteTheme.shapes.card)
                        .clip(KitapCepteTheme.shapes.card)
                        .background(KitapCepteTheme.extendedColors.surfaceWhite)
                        .padding(16.dp)
                ) {
                    SummaryRow(
                        label = stringResource(id = R.string.payment_total_amount),
                        value = "${String.format(Locale.US, "%.2f", state.total)} ₺",
                        isHighlighted = true
                    )
                }

                // Pay Button
                PrimaryButton(
                    text = if (state.isProcessing) stringResource(id = R.string.loading) else "${stringResource(id = R.string.btn_pay)} (${String.format(Locale.US, "%.2f", state.total)} ₺)",
                    onClick = { onEvent(PaymentUiEvent.PayClicked) },
                    enabled = !state.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Success Confirmation Dialog
        if (state.isSuccess) {
            PaymentSuccessDialog(
                orderNumber = state.orderNumber,
                onReturnHome = { onEvent(PaymentUiEvent.ReturnHomeClicked) }
            )
        }
    }
}

@Composable
private fun CreditCardPreview(state: PaymentUiState) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF281C6A),
            Color(0xFF5B3FE8),
            Color(0xFF8F75FA)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(cardGradient)
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: EMV Chip & Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold EMV Chip
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD4AF37))
                        .border(1.dp, Color(0xFFB8860B), RoundedCornerShape(6.dp))
                )

                Text(
                    text = state.cardBrand,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Card Number
            val displayCardNumber = if (state.formattedCardNumber.isNotBlank()) {
                state.formattedCardNumber
            } else {
                "••••  ••••  ••••  ••••"
            }
            Text(
                text = displayCardNumber,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                maxLines = 1
            )

            // Bottom Row: Holder and Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KART SAHİBİ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (state.cardHolder.isNotBlank()) state.cardHolder.uppercase() else "AD SOYAD",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SON KULLANMA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (state.formattedExpiry.isNotBlank()) state.formattedExpiry else "••/••",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentSuccessDialog(
    orderNumber: String,
    onReturnHome: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onReturnHome
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KitapCepteTheme.shapes.card)
                .background(KitapCepteTheme.extendedColors.surfaceWhite)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Green Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(id = R.string.payment_success_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.payment_order_number, orderNumber),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(id = R.string.payment_success_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(id = R.string.btn_return_home),
                    onClick = onReturnHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentScreenPreview() {
    KitapCepteTheme {
        PaymentScreen(
            state = PaymentUiState(
                cardHolder = "Serdar Akçay",
                cardNumber = "5428000012345678",
                expiryDate = "1228",
                cvv = "345",
                total = 220.0
            ),
            onEvent = {}
        )
    }
}
