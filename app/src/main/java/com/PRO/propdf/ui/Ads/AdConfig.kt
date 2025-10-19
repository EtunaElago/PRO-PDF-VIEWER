package com.PRO.propdf.ads

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ad_preferences")

class AdConfig(private val context: Context) {

    companion object {
        private val ADS_ENABLED = booleanPreferencesKey("ads_enabled")
        private val AD_FREQUENCY = intPreferencesKey("ad_frequency")
        private val LAST_AD_SHOWN_TIME = intPreferencesKey("last_ad_shown_time")
        private val FOLDER_CREATION_COUNT = intPreferencesKey("folder_creation_count")
        private val PDF_ADDITION_COUNT = intPreferencesKey("pdf_addition_count")
        
        // Default ad frequency (show ad every 3 actions)
        private const val DEFAULT_AD_FREQUENCY = 3
    }

    // Ads enabled/disabled
    suspend fun setAdsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ADS_ENABLED] = enabled
        }
    }

    val isAdsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ADS_ENABLED] ?: true // Default to enabled
        }

    // Ad frequency
    suspend fun setAdFrequency(frequency: Int) {
        context.dataStore.edit { preferences ->
            preferences[AD_FREQUENCY] = frequency
        }
    }

    val adFrequency: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[AD_FREQUENCY] ?: DEFAULT_AD_FREQUENCY
        }

    // Action counters for interstitial ads
    suspend fun incrementFolderCreationCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[FOLDER_CREATION_COUNT] ?: 0
            preferences[FOLDER_CREATION_COUNT] = currentCount + 1
        }
    }

    suspend fun incrementPdfAdditionCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PDF_ADDITION_COUNT] ?: 0
            preferences[PDF_ADDITION_COUNT] = currentCount + 1
        }
    }

    suspend fun getFolderCreationCount(): Int {
        return context.dataStore.data
            .map { preferences -> preferences[FOLDER_CREATION_COUNT] ?: 0 }
            .first()
    }

    suspend fun getPdfAdditionCount(): Int {
        return context.dataStore.data
            .map { preferences -> preferences[PDF_ADDITION_COUNT] ?: 0 }
            .first()
    }

    suspend fun resetFolderCreationCount() {
        context.dataStore.edit { preferences ->
            preferences[FOLDER_CREATION_COUNT] = 0
        }
    }

    suspend fun resetPdfAdditionCount() {
        context.dataStore.edit { preferences ->
            preferences[PDF_ADDITION_COUNT] = 0
        }
    }

    // Check if should show ad based on action count
    suspend fun shouldShowFolderCreationAd(): Boolean {
        val count = getFolderCreationCount()
        val frequency = adFrequency.first()
        return count >= frequency
    }

    suspend fun shouldShowPdfAdditionAd(): Boolean {
        val count = getPdfAdditionCount()
        val frequency = adFrequency.first()
        return count >= frequency
    }

    // Last ad shown time (for rate limiting)
    suspend fun setLastAdShownTime() {
        context.dataStore.edit { preferences ->
            preferences[LAST_AD_SHOWN_TIME] = (System.currentTimeMillis() / 1000).toInt()
        }
    }

    suspend fun getLastAdShownTime(): Int {
        return context.dataStore.data
            .map { preferences -> preferences[LAST_AD_SHOWN_TIME] ?: 0 }
            .first()
    }

    suspend fun canShowAd(): Boolean {
        val lastShown = getLastAdShownTime()
        val currentTime = (System.currentTimeMillis() / 1000).toInt()
        // Minimum 30 seconds between ads
        return (currentTime - lastShown) >= 30
    }
}

// Extension function to get first value from Flow
private suspend fun <T> Flow<T>.first(): T {
    return this.collect { return it }
}