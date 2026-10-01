package com.kitapcepte.feature.onboarding

data class OnboardingUiState(
    val currentPageIndex: Int = 0,
    val totalPages: Int = onboardingPages.size
) {
    val isLastPage: Boolean get() = currentPageIndex == totalPages - 1
}

sealed interface OnboardingUiEvent {
    data class PageChanged(val page: Int) : OnboardingUiEvent
    data object NextClicked : OnboardingUiEvent
    data object SkipClicked : OnboardingUiEvent
    data object GetStartedClicked : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data object NavigateToAuth : OnboardingUiEffect
    data class ScrollToPage(val page: Int) : OnboardingUiEffect
}
