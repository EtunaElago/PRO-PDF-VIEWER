package com.PRO.propdf.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.PRO.propdf.BuildConfig
import com.PRO.propdf.data.model.AppConstants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {

    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    
    private var appOpenAdLoadTime: Long = 0
    private var interstitialAdLoadTime: Long = 0

    companion object {
        private const val TAG = "AdManager"
        private const val AD_UNIT_ID_APP_OPEN = BuildConfig.DEBUG_AD_UNIT_APP_OPEN
        private const val AD_UNIT_ID_INTERSTITIAL = BuildConfig.DEBUG_AD_UNIT_INTERSTITIAL
        private const val AD_UNIT_ID_BANNER = BuildConfig.DEBUG_AD_UNIT_BANNER
        
        // Ad expiration time (4 hours)
        private const val AD_EXPIRY_DURATION: Long = 4 * 60 * 60 * 1000
    }

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
        }

        // Set test device IDs for debugging
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }

    // App Open Ad Methods
    fun loadAppOpenAd(activity: Activity) {
        if (isLoadingAd || isAdAvailable(appOpenAd, appOpenAdLoadTime)) {
            return
        }

        isLoadingAd = true
        
        val request = AdRequest.Builder().build()
        
        AppOpenAd.load(
            activity,
            AD_UNIT_ID_APP_OPEN,
            request,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad was loaded.")
                    appOpenAd = ad
                    appOpenAdLoadTime = Date().time
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity): Boolean {
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return false
        }

        if (!isAdAvailable(appOpenAd, appOpenAdLoadTime)) {
            Log.d(TAG, "The app open ad is not ready yet.")
            loadAppOpenAd(activity)
            return false
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad was dismissed.")
                appOpenAd = null
                isShowingAd = false
                loadAppOpenAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAppOpenAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showed fullscreen content.")
                isShowingAd = true
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
        
        return true
    }

    // Interstitial Ad Methods
    fun loadInterstitialAd(context: Context) {
        if (isLoadingAd || isAdAvailable(interstitialAd, interstitialAdLoadTime)) {
            return
        }

        isLoadingAd = true
        
        val request = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AD_UNIT_ID_INTERSTITIAL,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad was loaded.")
                    interstitialAd = ad
                    interstitialAdLoadTime = Date().time
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingAd = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}): Boolean {
        if (isShowingAd) {
            Log.d(TAG, "The interstitial ad is already showing.")
            return false
        }

        if (!isAdAvailable(interstitialAd, interstitialAdLoadTime)) {
            Log.d(TAG, "The interstitial ad is not ready yet.")
            loadInterstitialAd(activity)
            return false
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad was dismissed.")
                interstitialAd = null
                isShowingAd = false
                loadInterstitialAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                isShowingAd = false
                loadInterstitialAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad showed fullscreen content.")
                isShowingAd = true
            }
        }

        isShowingAd = true
        interstitialAd?.show(activity)
        
        return true
    }

    private fun isAdAvailable(ad: Any?, loadTime: Long): Boolean {
        return ad != null && wasLoadTimeLessThanNHoursAgo(loadTime)
    }

    private fun wasLoadTimeLessThanNHoursAgo(loadTime: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < AD_EXPIRY_DURATION
    }

    fun getBannerAdUnitId(): String {
        return AD_UNIT_ID_BANNER
    }

    fun isInterstitialAdAvailable(): Boolean {
        return isAdAvailable(interstitialAd, interstitialAdLoadTime)
    }

    fun isAppOpenAdAvailable(): Boolean {
        return isAdAvailable(appOpenAd, appOpenAdLoadTime)
    }
}