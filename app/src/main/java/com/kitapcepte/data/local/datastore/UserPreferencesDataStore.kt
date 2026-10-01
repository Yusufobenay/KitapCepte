package com.kitapcepte.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val isGuest: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_GUEST] ?: false
        }

    val loggedInUserId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_USER_ID]
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GUEST] = isGuest
            if (isGuest) {
                preferences.remove(PreferencesKeys.LOGGED_IN_USER_ID)
            }
        }
    }

    suspend fun setLoggedInUser(userId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_USER_ID] = userId
            preferences[PreferencesKeys.IS_GUEST] = false
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOGGED_IN_USER_ID)
            preferences[PreferencesKeys.IS_GUEST] = false
        }
    }
}
