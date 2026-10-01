package com.kitapcepte.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kitapcepte.core.designsystem.component.AppBottomBar
import com.kitapcepte.core.designsystem.component.BottomBarDestination
import com.kitapcepte.core.designsystem.component.MemberRequiredBottomSheet
import com.kitapcepte.core.designsystem.theme.BackgroundGradient
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.domain.model.Session
import com.kitapcepte.feature.auth.AuthRoute
import com.kitapcepte.feature.cart.CartRoute
import com.kitapcepte.feature.detail.DetailRoute
import com.kitapcepte.feature.favorites.FavoritesRoute
import com.kitapcepte.feature.home.HomeRoute
import com.kitapcepte.feature.onboarding.OnboardingRoute
import com.kitapcepte.feature.payment.PaymentRoute
import com.kitapcepte.feature.profile.ProfileRoute

@Composable
fun KitapCepteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Onboarding,
    session: Session = Session.LoggedOut,
    cartItemCount: Int = 0
) {
    var showMemberRequiredSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute?.let { route ->
        !route.contains("Onboarding") && !route.contains("Auth") && !route.contains("Detail") && !route.contains("Payment")
    } ?: false

    val currentBottomDestination = when {
        currentRoute?.contains("Favorites") == true -> BottomBarDestination.FAVORITES
        currentRoute?.contains("Cart") == true -> BottomBarDestination.CART
        currentRoute?.contains("Profile") == true -> BottomBarDestination.PROFILE
        else -> BottomBarDestination.HOME
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Screen.Onboarding> {
                OnboardingRoute(
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth) {
                            popUpTo(Screen.Onboarding) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Auth> {
                AuthRoute(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Auth) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Home> {
                HomeRoute(
                    onNavigateToDetail = { bookId ->
                        navController.navigate(Screen.Detail(bookId))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile)
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
            composable<Screen.Detail> {
                DetailRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDetail = { bookId ->
                        navController.navigate(Screen.Detail(bookId))
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
            composable<Screen.Favorites> {
                FavoritesRoute(
                    onNavigateToDetail = { bookId ->
                        navController.navigate(Screen.Detail(bookId))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile)
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
            composable<Screen.Cart> {
                CartRoute(
                    onNavigateToPayment = {
                        navController.navigate(Screen.Payment)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile)
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
            composable<Screen.Payment> {
                PaymentRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
            composable<Screen.Profile> {
                ProfileRoute(
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    onShowMemberRequiredSheet = {
                        showMemberRequiredSheet = true
                    }
                )
            }
        }

        if (showBottomBar) {
            AppBottomBar(
                currentDestination = currentBottomDestination,
                cartItemCount = cartItemCount,
                onNavigateToDestination = { destination ->
                    when (destination) {
                        BottomBarDestination.HOME -> {
                            navController.navigate(Screen.Home) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        BottomBarDestination.FAVORITES -> {
                            GuestGuard.check(
                                session = session,
                                onGuestRestricted = { showMemberRequiredSheet = true },
                                onAllowed = {
                                    navController.navigate(Screen.Favorites) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        BottomBarDestination.CART -> {
                            GuestGuard.check(
                                session = session,
                                onGuestRestricted = { showMemberRequiredSheet = true },
                                onAllowed = {
                                    navController.navigate(Screen.Cart) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        BottomBarDestination.PROFILE -> {
                            GuestGuard.check(
                                session = session,
                                onGuestRestricted = { showMemberRequiredSheet = true },
                                onAllowed = {
                                    navController.navigate(Screen.Profile) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showMemberRequiredSheet) {
            MemberRequiredBottomSheet(
                onDismissRequest = { showMemberRequiredSheet = false },
                onNavigateToAuth = {
                    showMemberRequiredSheet = false
                    navController.navigate(Screen.Auth)
                }
            )
        }
    }
}
