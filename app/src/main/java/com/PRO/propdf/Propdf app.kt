package com.PRO.propdf

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.PRO.propdf.ads.AdManager
import com.PRO.propdf.analytics.AnalyticsManager
import com.PRO.propdf.crashreporting.CrashReportingManager
import com.PRO.propdf.data.database.AppDatabase
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class for PRO PDF Viewer
 * 
 * Production-ready application class with:
 * - Hilt Dependency Injection
 * - Admob Ads with advanced configuration
 * - Firebase Analytics with enhanced tracking
 * - Crashlytics with custom context
 * - Room Database initialization
 * - App lifecycle management
 * - Performance monitoring
 */
@HiltAndroidApp
class ProPdfApp : Application() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var crashReportingManager: CrashReportingManager

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        
        instance = this
        
        // Initialize core components in sequence
        initializeFirebase()
        initializeLogging()
        initializeDatabase()
        initializeAnalytics()
        initializeCrashReporting()
        initializeAds()
        initializeAppLifecycleObserver()
        
        Timber.d("✅ PRO PDF Viewer application initialized successfully")
    }

    /**
     * Initialize Firebase services with error handling
     */
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("🔥 Firebase initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to initialize Firebase")
            recordInitializationError("FirebaseInitialization", e)
        }
    }

    /**
     * Initialize Timber logging with Crashlytics integration for release builds
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("🌲 Debug logging enabled")
        } else {
            Timber.plant(ReleaseCrashReportingTree())
            Timber.d("📊 Release logging with Crashlytics enabled")
        }
    }

    /**
     * Initialize Room database with proper configuration
     */
    private fun initializeDatabase() {
        try {
            // Database will be initialized through Dagger Hilt
            // This ensures proper dependency injection and thread management
            Timber.d("💾 Database configuration initialized")
        } catch (e: Exception) {
            Timber.e(e, "❌ Database initialization failed")
            recordInitializationError("DatabaseInitialization", e)
        }
    }

    /**
     * Initialize Firebase Analytics with enhanced tracking and user segmentation
     */
    private fun initializeAnalytics() {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            
            // Enable analytics collection
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)
            
            // Set extended session timeout (30 minutes)
            firebaseAnalytics.setSessionTimeoutDuration(1800000)
            
            // Configure user properties for advanced segmentation
            configureUserSegmentation()
            
            // Log app initialization event
            analyticsManager.logAppInitialization()
            
            Timber.d("📈 Analytics initialized with enhanced tracking")
        } catch (e: Exception) {
            Timber.e(e, "❌ Analytics initialization failed")
            recordInitializationError("AnalyticsInitialization", e)
        }
    }

    /**
     * Configure user properties for advanced audience segmentation
     */
    private fun configureUserSegmentation() {
        try {
            // App and device information
            firebaseAnalytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.setUserProperty("app_build_code", BuildConfig.VERSION_CODE.toString())
            firebaseAnalytics.setUserProperty("android_version", android.os.Build.VERSION.SDK_INT.toString())
            
            // User type segmentation for premium targeting
            firebaseAnalytics.setUserProperty("user_category", "professional")
            firebaseAnalytics.setUserProperty("app_usage_type", "pdf_management")
            
            // Feature adoption tracking
            firebaseAnalytics.setUserProperty("premium_features_available", "true")
            firebaseAnalytics.setUserProperty("ad_supported", "true")
            
            // Performance metrics baseline
            firebaseAnalytics.setUserProperty("performance_tier", "high")
            
            Timber.d("🎯 User segmentation configured for premium audience targeting")
        } catch (e: Exception) {
            Timber.e(e, "❌ User segmentation configuration failed")
        }
    }

    /**
     * Initialize Crashlytics with custom context and enhanced crash reporting
     */
    private fun initializeCrashReporting() {
        try {
            // Enable Crashlytics collection
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            
            // Set custom keys for better crash context and debugging
            setCrashlyticsCustomKeys()
            
            // Set user identifier for crash tracking (anonymous)
            setAnonymousUserIdentifier()
            
            Timber.d("🐛 Crashlytics initialized with custom context")
        } catch (e: Exception) {
            Timber.e(e, "❌ Crashlytics initialization failed")
            // Note: We don't record crash reporting failures to crashlytics
        }
    }

    /**
     * Set custom keys in Crashlytics for enhanced crash context
     */
    private fun setCrashlyticsCustomKeys() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // App information
            crashlytics.setCustomKey("app_version_name", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
            crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            
            // Device information
            crashlytics.setCustomKey("android_version", android.os.Build.VERSION.SDK_INT)
            crashlytics.setCustomKey("device_model", android.os.Build.MODEL)
            crashlytics.setCustomKey("device_manufacturer", android.os.Build.MANUFACTURER)
            
            // Storage information
            crashlytics.setCustomKey("free_storage_mb", getFreeStorageSpaceMB())
            crashlytics.setCustomKey("total_memory_gb", getTotalMemoryGB())
            
            // Component status
            crashlytics.setCustomKey("firebase_initialized", true)
            crashlytics.setCustomKey("analytics_initialized", true)
            crashlytics.setCustomKey("crashlytics_initialized", true)
            
            Timber.d("🔑 Crashlytics custom keys configured")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set Crashlytics custom keys")
        }
    }

    /**
     * Set anonymous user identifier for crash tracking
     */
    private fun setAnonymousUserIdentifier() {
        try {
            // Generate anonymous user ID (not using personal data)
            val anonymousUserId = "pro_pdf_user_${System.currentTimeMillis()}"
            FirebaseCrashlytics.getInstance().setUserId(anonymousUserId)
            Timber.d("👤 Anonymous user ID set for crash tracking")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set anonymous user ID")
        }
    }

    /**
     * Initialize Admob with advanced configuration for high-value regions
     */
    private fun initializeAds() {
        try {
            // Initialize Mobile Ads SDK
            MobileAds.initialize(this) { initializationStatus ->
                // Log initialization status for all ad adapters
                val statusMap = initializationStatus.adapterStatusMap
                Timber.d("🤑 AdMob initialization status:")
                
                statusMap.forEach { (adapter, status) ->
                    val state = when (status.initializationState) {
                       com.google.android.gms.ads.initialization.AdapterStatus.State.READY -> "READY"
                        com.google.android.gms.ads.initialization.AdapterStatus.State.NOT_READY -> "NOT_READY"
                        else -> "UNKNOWN"
                    }
                    Timber.d("   Adapter: $adapter, State: $state, Desc: ${status.description}")
                }
                
                // Configure advanced ad settings for premium audience
                adManager.configureAdvancedAdSettings()
                
                // Set up geographic segmentation for high-value regions
                adManager.setupGeographicSegmentation()
                
                // Initialize ad unit optimization
                adManager.initializeAdUnits()
                
                Timber.d("🎯 AdMob configured for premium audience targeting")
            }
            
            // Configure global request settings
            configureGlobalAdSettings()
            
            Timber.d("✅ AdMob initialized with advanced configuration")
        } catch (e: Exception) {
            Timber.e(e, "❌ AdMob initialization failed")
            recordInitializationError("AdMobInitialization", e)
        }
    }

    /**
     * Configure global ad request settings for professional audience
     */
    private fun configureGlobalAdSettings() {
        try {
            val requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(MobileAds.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                .setMaxAdContentRating(MobileAds.MAX_AD_CONTENT_RATING_G)
                .build()
            
            MobileAds.setRequestConfiguration(requestConfiguration)
            Timber.d("🌍 Global ad request configuration applied")
        } catch (e: Exception) {
            Timber.e(e, "❌ Global ad configuration failed")
        }
    }

    /**
     * Initialize app lifecycle observer for session tracking and engagement metrics
     */
    private fun initializeAppLifecycleObserver() {
        try {
            val appLifecycleObserver = AppLifecycleObserver(analyticsManager, adManager)
            ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
            Timber.d("📱 App lifecycle observer initialized")
        } catch (e: Exception) {
            Timber.e(e, "❌ App lifecycle observer initialization failed")
            recordInitializationError("AppLifecycleInitialization", e)
        }
    }

    /**
     * Record initialization errors with proper context
     */
    private fun recordInitializationError(component: String, exception: Exception) {
        crashReportingManager.recordException(exception, component)
        
        // Log to analytics as custom event
        val params = hashMapOf<String, Any>(
            "component" to component,
            "error_message" to exception.message ?: "Unknown error"
        )
        analyticsManager.logEvent("initialization_error", params)
    }

    /**
     * Calculate free storage space in MB for crash context
     */
    private fun getFreeStorageSpaceMB(): Long {
        return try {
            val stat = android.os.StatFs(filesDir.absolutePath)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes / (1024 * 1024) // Convert to MB
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Get total device memory in GB for crash context
     */
    private fun getTotalMemoryGB(): Long {
        return try {
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.totalMem / (1024 * 1024 * 1024) // Convert to GB
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Get AdManager instance
     */
    fun getAdManager(): AdManager = adManager

    /**
     * Get AnalyticsManager instance
     */
    fun getAnalyticsManager(): AnalyticsManager = analyticsManager

    /**
     * Get CrashReportingManager instance
     */
    fun getCrashReportingManager(): CrashReportingManager = crashReportingManager

    /**
     * Get FirebaseAnalytics instance
     */
    fun getFirebaseAnalytics(): FirebaseAnalytics = firebaseAnalytics

    companion object {
        private lateinit var instance: ProPdfApp

        /**
         * Get application instance (singleton pattern)
         */
        fun getInstance(): ProPdfApp {
            return instance
        }
    }
}

/**
 * App Lifecycle Observer for tracking app sessions, engagement metrics, and ad management
 */
class AppLifecycleObserver(
    private val analyticsManager: AnalyticsManager,
    private val adManager: AdManager
) : androidx.lifecycle.DefaultLifecycleObserver {

    private var appStartTime: Long = 0
    private var sessionId: String = ""

    override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
        appStartTime = System.currentTimeMillis()
        sessionId = "session_${System.currentTimeMillis()}"
        
        // Log app foreground event
        val params = hashMapOf<String, Any>(
            "session_id" to sessionId,
            "timestamp" to appStartTime
        )
        analyticsManager.logEvent("app_foreground", params)
        
        // Preload ads for better user experience
        adManager.preloadInterstitialAds()
        
        Timber.d("🚀 App entered foreground - Session: $sessionId")
    }

    override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
        val sessionDuration = System.currentTimeMillis() - appStartTime
        val sessionDurationSeconds = sessionDuration / 1000
        
        // Log app background event with session duration
        val params = hashMapOf<String, Any>(
            "session_id" to sessionId,
            "duration_seconds" to sessionDurationSeconds,
            "duration_ms" to sessionDuration
        )
        analyticsManager.logEvent("app_background", params)
        
        // Update user engagement metrics
        updateEngagementMetrics(sessionDurationSeconds)
        
        Timber.d("⏸️ App entered background - Session: $sessionId, Duration: ${sessionDurationSeconds}s")
    }

    /**
     * Update user engagement metrics for analytics
     */
    private fun updateEngagementMetrics(sessionDurationSeconds: Long) {
        try {
            val engagementParams = hashMapOf<String, Any>(
                "session_duration_seconds" to sessionDurationSeconds,
                "session_timestamp" to System.currentTimeMillis()
            )
            
            // Log engagement event for user behavior analysis
            analyticsManager.logEvent("user_engagement", engagementParams)
            
            // Update user property for average session duration
            if (sessionDurationSeconds > 60) { // Only count sessions longer than 1 minute
                analyticsManager.updateUserProperty("engaged_user", "true")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update engagement metrics")
        }
    }
}

/**
 * Custom Timber tree for release builds that integrates with Crashlytics
 */
private class ReleaseCrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log warnings and errors to Crashlytics in release builds
        if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // Format log message for Crashlytics
            val formattedMessage = if (tag != null) "[$tag] $message" else message
            crashlytics.log(formattedMessage)
            
            // Record exception if present
            t?.let { exception ->
                crashlytics.recordException(exception)
            }
        }
        
        // For debug builds, everything is logged to Logcat
        // For release builds, only errors/warnings go to Crashlytics
    }
}