package com.kitapcepte.feature.favorites

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FavoritesRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onShowMemberRequiredSheet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FavoritesUiEffect.NavigateToDetail -> onNavigateToDetail(effect.bookId)
                FavoritesUiEffect.NavigateToProfile -> onNavigateToProfile()
                FavoritesUiEffect.ShowMemberRequiredSheet -> onShowMemberRequiredSheet()
                is FavoritesUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    FavoritesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}
