package com.kitapcepte.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.core.common.DispatcherProvider
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.PageChanged -> {
                _uiState.update { it.copy(currentPageIndex = event.page) }
            }
            OnboardingUiEvent.NextClicked -> {
                val current = _uiState.value.currentPageIndex
                if (current < _uiState.value.totalPages - 1) {
                    val next = current + 1
                    _uiState.update { it.copy(currentPageIndex = next) }
                    viewModelScope.launch(dispatchers.main) {
                        _uiEffect.send(OnboardingUiEffect.ScrollToPage(next))
                    }
                } else {
                    completeOnboarding()
                }
            }
            OnboardingUiEvent.SkipClicked,
            OnboardingUiEvent.GetStartedClicked -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch(dispatchers.io) {
            preferencesDataStore.setOnboardingCompleted(true)
            _uiEffect.send(OnboardingUiEffect.NavigateToAuth)
        }
    }
}
