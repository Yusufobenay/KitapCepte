package com.kitapcepte.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data class Detail(val bookId: String) : Screen

    @Serializable
    data object Favorites : Screen

    @Serializable
    data object Cart : Screen

    @Serializable
    data object Payment : Screen

    @Serializable
    data object Profile : Screen
}
