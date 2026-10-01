package com.kitapcepte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitapcepte.core.navigation.Screen
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: Screen = Screen.Onboarding,
    val session: Session = Session.LoggedOut,
    val cartItemCount: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesDataStore: UserPreferencesDataStore,
    sessionRepository: SessionRepository,
    bookRepository: BookRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        preferencesDataStore.isOnboardingCompleted,
        sessionRepository.sessionState,
        bookRepository.getCartItemCount()
    ) { isOnboardingCompleted, session, cartItemCount ->
        val startDestination = when {
            !isOnboardingCompleted -> Screen.Onboarding
            session is Session.LoggedOut -> Screen.Auth
            else -> Screen.Home
        }
        MainUiState(
            isLoading = false,
            startDestination = startDestination,
            session = session,
            cartItemCount = cartItemCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(isLoading = true)
    )
}
