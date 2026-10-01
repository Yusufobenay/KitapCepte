package com.kitapcepte

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.navigation.Screen
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import com.kitapcepte.domain.model.Session
import com.kitapcepte.domain.model.User
import com.kitapcepte.domain.repository.BookRepository
import com.kitapcepte.domain.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferencesDataStore: UserPreferencesDataStore = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val bookRepository: BookRepository = mockk()

    private val isOnboardingCompletedFlow = MutableStateFlow(false)
    private val sessionStateFlow = MutableStateFlow<Session>(Session.LoggedOut)
    private val cartItemCountFlow = MutableStateFlow(0)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesDataStore.isOnboardingCompleted } returns isOnboardingCompletedFlow
        every { sessionRepository.sessionState } returns sessionStateFlow
        every { bookRepository.getCartItemCount() } returns cartItemCountFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when onboarding is not completed startDestination is Onboarding`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(preferencesDataStore, sessionRepository, bookRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isTrue()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.startDestination).isEqualTo(Screen.Onboarding)
        }
    }

    @Test
    fun `when onboarding is completed and session is LoggedOut startDestination is Auth`() = runTest(testDispatcher) {
        isOnboardingCompletedFlow.value = true
        sessionStateFlow.value = Session.LoggedOut

        val viewModel = MainViewModel(preferencesDataStore, sessionRepository, bookRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isTrue()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.startDestination).isEqualTo(Screen.Auth)
            assertThat(state.session).isEqualTo(Session.LoggedOut)
        }
    }

    @Test
    fun `when onboarding is completed and session is Guest startDestination is Home`() = runTest(testDispatcher) {
        isOnboardingCompletedFlow.value = true
        sessionStateFlow.value = Session.Guest

        val viewModel = MainViewModel(preferencesDataStore, sessionRepository, bookRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isTrue()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.startDestination).isEqualTo(Screen.Home)
            assertThat(state.session).isEqualTo(Session.Guest)
        }
    }

    @Test
    fun `when onboarding is completed and session is LoggedIn startDestination is Home`() = runTest(testDispatcher) {
        isOnboardingCompletedFlow.value = true
        val user = User(1L, "user@test.com", "Serdar")
        sessionStateFlow.value = Session.LoggedIn(user)

        val viewModel = MainViewModel(preferencesDataStore, sessionRepository, bookRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isTrue()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.startDestination).isEqualTo(Screen.Home)
            assertThat(state.session).isEqualTo(Session.LoggedIn(user))
        }
    }

    @Test
    fun `cartItemCount updates live from BookRepository`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(preferencesDataStore, sessionRepository, bookRepository)

        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isTrue()
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem().cartItemCount).isEqualTo(0)

            cartItemCountFlow.value = 5
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem().cartItemCount).isEqualTo(5)
        }
    }
}
