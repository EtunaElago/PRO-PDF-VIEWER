package com.PRO.propdf.analytics

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.PRO.propdf.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "AnalyticsManager"
        
        // Event Names
        private const val EVENT_APP_LAUNCH = "app_launch"
        private const val EVENT_PDF_OPENED = "pdf_opened"
        private const val EVENT_PDF_ADDED = "pdf_added"
        private const val EVENT_FOLDER_CREATED = "folder_created"
        private const val EVENT_SEARCH_PERFORMED = "search_performed"
        private const val EVENT_AD_SHOWN = "ad_shown"
        private const val EVENT_AD_CLICKED = "ad_clicked"
        private const val EVENT_USER_ENGAGEMENT = "user_engagement"
        private const val EVENT_ERROR_OCCURRED = "error_occurred"
        private const val EVENT_SESSION_END = "session_end"
        
        // User Properties
        private const val PROPERTY_USER_REGION = "user_region"
        private const val PROPERTY_USER_LANGUAGE = "user_language"
        private const val PROPERTY_APP_VERSION = "app_version"
        private const val PROPERTY_DEVICE_TYPE = "device_type"
        private const val PROPERTY_IS_PREMIUM = "is_premium"
        private const val PROPERTY_ECPM_TIER = "ecpm_tier"
        
        // Parameter Names
        private const val PARAM_PDF_ID = "pdf_id"
        private const val PARAM_PDF_NAME = "pdf_name"
        private const val PARAM_PDF_SIZE = "pdf_size"
        private const val PARAM_PDF_PAGES = "pdf_pages"
        private const val PARAM_FOLDER_ID = "folder_id"
        private const val PARAM_FOLDER_NAME = "folder_name"
        private const val PARAM_SEARCH_QUERY = "search_query"
        private const val PARAM_SEARCH_RESULTS = "search_results"
        private const val PARAM_AD_TYPE = "ad_type"
        private const val PARAM_AD_UNIT = "ad_unit"
        private const val PARAM_AD_REVENUE = "ad_revenue"
        private const val PARAM_SESSION_DURATION = "session_duration"
        private const val PARAM_SCREEN_NAME = "screen_name"
        private const val PARAM_ERROR_MESSAGE = "error_message"
        private const val PARAM_ERROR_CODE = "error_code"
        private const val PARAM_SOURCE = "source"
        private const val PARAM_ACTION = "action"
        private const val PARAM_VALUE = "value"
        private const val PARAM_REGION = "region"
        private const val PARAM_ECPM_TIER = "ecpm_tier"
    }

    init {
        setupUserProperties()
        setupCrashlytics()
        trackAppLaunch()
    }

    private fun setupUserProperties() {
        coroutineScope.launch {
            // Set user properties for better segmentation
            setUserProperty(PROPERTY_APP_VERSION, "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            setUserProperty(PROPERTY_DEVICE_TYPE, if (isTablet()) "tablet" else "phone")
            setUserProperty(PROPERTY_USER_REGION, getDeviceRegion())
            setUserProperty(PROPERTY_USER_LANGUAGE, getDeviceLanguage())
            setUserProperty(PROPERTY_IS_PREMIUM, "false")
            setUserProperty(PROPERTY_ECPM_TIER, if (isHighECPMRegion()) "high" else "standard")
            
            // Set user ID for cross-platform tracking
            // firebaseAnalytics.setUserId("user_id_here")
        }
    }

    private fun setupCrashlytics() {
        // Configure Crashlytics
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Set custom keys for better crash reporting
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("android_version", Build.VERSION.SDK_INT.toString())
        crashlytics.setCustomKey("device_model", Build.MODEL)
        crashlytics.setCustomKey("user_region", getDeviceRegion())
        crashlytics.setCustomKey("ecpm_tier", if (isHighECPMRegion()) "high" else "standard")
    }

    private fun trackAppLaunch() {
        logEvent(EVENT_APP_LAUNCH) {
            param(PARAM_SOURCE, "cold_start")
            param(PARAM_REGION, getDeviceRegion())
            param(PARAM_ECPM_TIER, if (isHighECPMRegion()) "high" else "standard")
        }
        
        // Also track standard Firebase app open
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }

    // Region-based targeting for high eCPM
    fun getDeviceRegion(): String {
        return context.resources.configuration.locales.getOrNull(0)?.country ?: "Unknown"
    }

    private fun getDeviceLanguage(): String {
        return context.resources.configuration.locales.getOrNull(0)?.language ?: "en"
    }

    private fun isTablet(): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    fun isHighECPMRegion(): Boolean {
        val region = getDeviceRegion()
        // USA, Canada, Western Europe, Australia, Japan, South Korea
        val highECPMCountries = listOf("US", "CA", "GB", "DE", "FR", "IT", "ES", "AU", "JP", "KR", 
                                      "NL", "SE", "NO", "DK", "FI", "CH", "AT", "BE", "IE", "NZ")
        return region in highECPMCountries
    }

    // User Properties
    fun setUserProperty(property: String, value: String) {
        firebaseAnalytics.setUserProperty(property, value)
        crashlytics.setCustomKey(property, value)
    }

    // Screen Tracking
    fun trackScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        
        // Also track as custom event for better filtering
        logEvent(EVENT_USER_ENGAGEMENT) {
            param(PARAM_SCREEN_NAME, screenName)
            param(PARAM_ACTION, "screen_view")
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // PDF Events
    fun trackPdfOpened(pdfId: Long, pdfName: String, pdfSize: Long, pageCount: Int) {
        logEvent(EVENT_PDF_OPENED) {
            param(PARAM_PDF_ID, pdfId.toString())
            param(PARAM_PDF_NAME, pdfName)
            param(PARAM_PDF_SIZE, pdfSize)
            param(PARAM_PDF_PAGES, pageCount.toLong())
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    fun trackPdfAdded(pdfId: Long, pdfName: String, source: String) {
        logEvent(EVENT_PDF_ADDED) {
            param(PARAM_PDF_ID, pdfId.toString())
            param(PARAM_PDF_NAME, pdfName)
            param(PARAM_SOURCE, source)
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Folder Events
    fun trackFolderCreated(folderId: Long, folderName: String) {
        logEvent(EVENT_FOLDER_CREATED) {
            param(PARAM_FOLDER_ID, folderId.toString())
            param(PARAM_FOLDER_NAME, folderName)
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Search Events
    fun trackSearchPerformed(query: String, resultCount: Int) {
        logEvent(EVENT_SEARCH_PERFORMED) {
            param(PARAM_SEARCH_QUERY, query)
            param(PARAM_SEARCH_RESULTS, resultCount.toLong())
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Ad Events with Revenue Tracking
    fun trackAdShown(adType: String, adUnit: String, estimatedRevenue: Double = 0.0) {
        logEvent(EVENT_AD_SHOWN) {
            param(PARAM_AD_TYPE, adType)
            param(PARAM_AD_UNIT, adUnit)
            param(PARAM_AD_REVENUE, estimatedRevenue)
            param(PARAM_REGION, getDeviceRegion())
            param(PARAM_ECPM_TIER, if (isHighECPMRegion()) "high" else "standard")
        }
        
        // Also log to Firebase Analytics for AdMob integration
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.AD_PLATFORM, "AdMob")
            param(FirebaseAnalytics.Param.AD_FORMAT, adType)
            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnit)
        }
    }

    fun trackAdClicked(adType: String, adUnit: String) {
        logEvent(EVENT_AD_CLICKED) {
            param(PARAM_AD_TYPE, adType)
            param(PARAM_AD_UNIT, adUnit)
            param(PARAM_REGION, getDeviceRegion())
        }
        
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_CLICK) {
            param(FirebaseAnalytics.Param.AD_PLATFORM, "AdMob")
            param(FirebaseAnalytics.Param.AD_FORMAT, adType)
            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnit)
        }
    }

    // Revenue Tracking (for future in-app purchases)
    fun trackRevenue(productId: String, price: Double, currency: String = "USD") {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.CURRENCY, currency)
            param(FirebaseAnalytics.Param.VALUE, price)
            param(FirebaseAnalytics.Param.ITEMS, arrayOf(
                createItemBundle(productId, "Premium Subscription", price)
            ))
        }
        
        // Update user property
        setUserProperty(PROPERTY_IS_PREMIUM, "true")
    }

    // Error Tracking
    fun trackError(errorMessage: String, errorCode: String? = null, screen: String? = null) {
        logEvent(EVENT_ERROR_OCCURRED) {
            param(PARAM_ERROR_MESSAGE, errorMessage)
            errorCode?.let { param(PARAM_ERROR_CODE, it) }
            screen?.let { param(PARAM_SCREEN_NAME, it) }
            param(PARAM_REGION, getDeviceRegion())
        }
        
        // Also log to Crashlytics as non-fatal
        crashlytics.log("Error: $errorMessage")
        errorCode?.let { crashlytics.setCustomKey("error_code", it) }
        screen?.let { crashlytics.setCustomKey("screen", it) }
    }

    // Session Tracking
    fun trackSessionStart() {
        logEvent(FirebaseAnalytics.Event.APP_OPEN) {
            param(PARAM_SOURCE, "session_start")
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    fun trackSessionEnd(duration: Long) {
        logEvent(EVENT_SESSION_END) {
            param(PARAM_SESSION_DURATION, duration)
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // User Engagement
    fun trackUserEngagement(action: String, value: String? = null) {
        logEvent(EVENT_USER_ENGAGEMENT) {
            param(PARAM_ACTION, action)
            value?.let { param(PARAM_VALUE, it) }
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Custom Events
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        firebaseAnalytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                }
            }
            // Always include region for segmentation
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Crash Reporting
    fun logCrash(message: String) {
        crashlytics.log(message)
    }

    fun setCrashCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    // Performance Monitoring
    fun trackPerformance(eventName: String, duration: Long) {
        logEvent("performance_$eventName") {
            param("duration_ms", duration)
            param(PARAM_REGION, getDeviceRegion())
        }
    }

    // Helper method for item bundle creation
    private fun createItemBundle(itemId: String, itemName: String, price: Double): Bundle {
        return Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            putDouble(FirebaseAnalytics.Param.PRICE, price)
        }
    }
}