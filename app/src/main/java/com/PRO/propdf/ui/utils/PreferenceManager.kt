package com.PRO.propdf.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class PreferenceManager(private val context: Context) {

    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val VIEW_MODE = stringPreferencesKey("view_mode")
        private val SORT_BY = stringPreferencesKey("sort_by")
        private val SORT_ORDER = stringPreferencesKey("sort_order")
        private val LANGUAGE = stringPreferencesKey("language")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        
        // Default values
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_VIEW_MODE = "grid"
        private const val DEFAULT_SORT_BY = "name"
        private const val DEFAULT_SORT_ORDER = "ascending"
        private const val DEFAULT_LANGUAGE = "system"
    }

    // Theme
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    val theme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME] ?: DEFAULT_THEME
        }

    // View Mode
    suspend fun setViewMode(viewMode: String) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE] = viewMode
        }
    }

    val viewMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[VIEW_MODE] ?: DEFAULT_VIEW_MODE
        }

    // Sort By
    suspend fun setSortBy(sortBy: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_BY] = sortBy
        }
    }

    val sortBy: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SORT_BY] ?: DEFAULT_SORT_BY
        }

    // Sort Order
    suspend fun setSortOrder(sortOrder: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER] = sortOrder
        }
    }

    val sortOrder: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SORT_ORDER] ?: DEFAULT_SORT_ORDER
        }

    // Language
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE] ?: DEFAULT_LANGUAGE
        }

    // First Launch
    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = isFirstLaunch
        }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FIRST_LAUNCH] ?: true
        }

    // Helper methods to get current values (for synchronous access)
    suspend fun getTheme(): String = theme.first()
    suspend fun getViewMode(): String = viewMode.first()
    suspend fun getSortBy(): String = sortBy.first()
    suspend fun getSortOrder(): String = sortOrder.first()
    suspend fun getLanguage(): String = language.first()
    suspend fun getIsFirstLaunch(): Boolean = isFirstLaunch.first()
}

// Extension function to get first value from Flow
private suspend fun <T> Flow<T>.first(): T {
    return this.collect { return it }
}