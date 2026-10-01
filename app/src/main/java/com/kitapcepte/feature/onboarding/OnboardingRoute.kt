package com.kitapcepte.feature.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute(
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { state.totalPages })

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                OnboardingUiEffect.NavigateToAuth -> onNavigateToAuth()
                is OnboardingUiEffect.ScrollToPage -> {
                    pagerState.animateScrollToPage(effect.page)
                }
            }
        }
    }

    OnboardingScreen(
        state = state,
        onEvent = viewModel::onEvent,
        pagerState = pagerState,
        modifier = modifier
    )
}
