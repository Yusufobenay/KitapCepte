package com.kitapcepte.feature.payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PaymentRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onShowMemberRequiredSheet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PaymentUiEffect.NavigateBack -> onNavigateBack()
                PaymentUiEffect.NavigateToHome -> onNavigateToHome()
                PaymentUiEffect.ShowMemberRequiredSheet -> onShowMemberRequiredSheet()
                is PaymentUiEffect.ShowSnackbar -> { /* snackbar if needed */ }
            }
        }
    }

    PaymentScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
