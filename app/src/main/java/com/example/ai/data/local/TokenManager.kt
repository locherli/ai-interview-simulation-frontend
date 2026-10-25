package com.example.ai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    suspend fun saveAuthInfo(token: String, email: String, userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[EMAIL_KEY] = email
            preferences[USER_ID_KEY] = userId.toString()
        }
    }

    suspend fun clearAuthInfo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
