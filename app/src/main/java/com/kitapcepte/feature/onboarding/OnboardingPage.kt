package com.kitapcepte.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val stepLabel: String
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Binlerce Kitap Parmaklarının Ucunda",
        description = "Fantastikten bilim kurguya, klasikten maceraya aradığın tüm kitaplar tek tıkla seninle.",
        icon = Icons.Outlined.AutoStories,
        stepLabel = "1. Adım: Keşfet"
    ),
    OnboardingPage(
        title = "Favorile, Sepete At ve Güvenle Al",
        description = "Beğendiğin kitapları favorilerine ekle, anında sepetine at ve avantajlı fiyatlarla güvenle sipariş et.",
        icon = Icons.Outlined.ShoppingBag,
        stepLabel = "2. Adım: Alışveriş"
    ),
    OnboardingPage(
        title = "Nasıl İlerlersin?",
        description = "İster misafir olarak kategorileri gez, istersen saniyeler içinde üye olup siparişlerini ve favorilerini yönet.",
        icon = Icons.Outlined.Explore,
        stepLabel = "3. Adım: Başla"
    )
)
