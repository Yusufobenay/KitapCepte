package com.kitapcepte.feature.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kitapcepte.core.common.TestDispatcherProvider
import com.kitapcepte.data.local.datastore.UserPreferencesDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private val preferencesDataStore: UserPreferencesDataStore = mockk(relaxed = true)

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        coEvery { preferencesDataStore.setOnboardingCompleted(any()) } just runs
        viewModel = OnboardingViewModel(
            preferencesDataStore = preferencesDataStore,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `initial state has first page selected and 3 total pages`() = runTest(testDispatcher) {
        assertThat(viewModel.uiState.value.currentPageIndex).isEqualTo(0)
        assertThat(viewModel.uiState.value.totalPages).isEqualTo(3)
        assertThat(viewModel.uiState.value.isLastPage).isFalse()
    }

    @Test
    fun `PageChanged event updates currentPageIndex in state`() = runTest(testDispatcher) {
        viewModel.onEvent(OnboardingUiEvent.PageChanged(1))
        assertThat(viewModel.uiState.value.currentPageIndex).isEqualTo(1)
        assertThat(viewModel.uiState.value.isLastPage).isFalse()

        viewModel.onEvent(OnboardingUiEvent.PageChanged(2))
        assertThat(viewModel.uiState.value.currentPageIndex).isEqualTo(2)
        assertThat(viewModel.uiState.value.isLastPage).isTrue()
    }

    @Test
    fun `NextClicked event when on first page scrolls to next page`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.NextClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(OnboardingUiEffect.ScrollToPage(1))
            assertThat(viewModel.uiState.value.currentPageIndex).isEqualTo(1)
        }
    }

    @Test
    fun `NextClicked event on last page completes onboarding and navigates to auth`() = runTest(testDispatcher) {
        viewModel.onEvent(OnboardingUiEvent.PageChanged(2))

        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.NextClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(OnboardingUiEffect.NavigateToAuth)
            coVerify(exactly = 1) { preferencesDataStore.setOnboardingCompleted(true) }
        }
    }

    @Test
    fun `SkipClicked event completes onboarding and navigates to auth`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.SkipClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(OnboardingUiEffect.NavigateToAuth)
            coVerify(exactly = 1) { preferencesDataStore.setOnboardingCompleted(true) }
        }
    }

    @Test
    fun `GetStartedClicked event completes onboarding and navigates to auth`() = runTest(testDispatcher) {
        viewModel.uiEffect.test {
            viewModel.onEvent(OnboardingUiEvent.GetStartedClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(OnboardingUiEffect.NavigateToAuth)
            coVerify(exactly = 1) { preferencesDataStore.setOnboardingCompleted(true) }
        }
    }
}
