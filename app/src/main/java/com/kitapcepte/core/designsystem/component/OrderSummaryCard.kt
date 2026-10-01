package com.kitapcepte.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme

@Composable
fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun OrderSummaryCard(
    subtotal: Double,
    tax: Double,
    shipping: Double = 0.0,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₺"
) {
    val total = subtotal + tax + shipping

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = KitapCepteTheme.shapes.card)
            .clip(KitapCepteTheme.shapes.card)
            .background(KitapCepteTheme.extendedColors.surfaceWhite)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(id = R.string.subtotal),
                value = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", subtotal)}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = stringResource(id = R.string.tax),
                value = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", tax)}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = stringResource(id = R.string.shipping),
                value = if (shipping <= 0.0) stringResource(id = R.string.free_shipping) else "$currencySymbol${String.format(java.util.Locale.US, "%.2f", shipping)}"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KitapCepteTheme.extendedColors.borderLight)
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(id = R.string.total),
                value = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", total)}",
                isHighlighted = true
            )
        }
    }
}
