package com.kitapcepte.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthRoute(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AuthUiEffect.NavigateToHome -> onNavigateToHome()
                is AuthUiEffect.ShowSnackbar -> { /* snackbar can be handled via host */ }
            }
        }
    }

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
