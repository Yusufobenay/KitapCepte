package com.kitapcepte.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitapcepte.R
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme

@Composable
fun TopItemBadge(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.badge_top_item)
) {
    Box(
        modifier = modifier
            .clip(KitapCepteTheme.shapes.buttonPill)
            .background(KitapCepteTheme.extendedColors.badgeTopItem)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = KitapCepteTheme.extendedColors.onBadgeTopItem,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CartCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                .clip(KitapCepteTheme.shapes.pill)
                .background(KitapCepteTheme.extendedColors.badgeCart)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = KitapCepteTheme.extendedColors.onBadgeCart,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
