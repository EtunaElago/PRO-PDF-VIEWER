package com.PRO.propdf.ads

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdIntegrationHelper @Inject constructor(
    private val adManager: AdManager,
    private val adConfig: AdConfig,
    private val bannerAdHelper: BannerAdHelper
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun initializeAds(activity: Activity) {
        adManager.initialize(activity)
        adManager.loadAppOpenAd(activity)
        adManager.loadInterstitialAd(activity)
    }

    fun showAppOpenAd(activity: Activity): Boolean {
        return adManager.showAppOpenAd(activity)
    }

    fun handleFolderCreation(activity: Activity, onAdDismissed: () -> Unit = {}) {
        coroutineScope.launch {
            adConfig.incrementFolderCreationCount()
            
            if (adConfig.shouldShowFolderCreationAd() && adConfig.canShowAd()) {
                // Show interstitial ad
                coroutineScope.launch(Dispatchers.Main) {
                    val adShown = adManager.showInterstitialAd(activity) {
                        onAdDismissed()
                    }
                    
                    if (adShown) {
                        coroutineScope.launch(Dispatchers.IO) {
                            adConfig.resetFolderCreationCount()
                            adConfig.setLastAdShownTime()
                        }
                    } else {
                        onAdDismissed()
                    }
                }
            } else {
                onAdDismissed()
            }
        }
    }

    fun handlePdfAddition(activity: Activity, onAdDismissed: () -> Unit = {}) {
        coroutineScope.launch {
            adConfig.incrementPdfAdditionCount()
            
            if (adConfig.shouldShowPdfAdditionAd() && adConfig.canShowAd()) {
                // Show interstitial ad
                coroutineScope.launch(Dispatchers.Main) {
                    val adShown = adManager.showInterstitialAd(activity) {
                        onAdDismissed()
                    }
                    
                    if (adShown) {
                        coroutineScope.launch(Dispatchers.IO) {
                            adConfig.resetPdfAdditionCount()
                            adConfig.setLastAdShownTime()
                        }
                    } else {
                        onAdDismissed()
                    }
                }
            } else {
                onAdDismissed()
            }
        }
    }

    fun setupBannerAd(container: android.view.ViewGroup, context: android.content.Context) {
        bannerAdHelper.setupBannerAd(container, context)
    }

    fun pauseAds() {
        bannerAdHelper.pauseBannerAd()
    }

    fun resumeAds() {
        bannerAdHelper.resumeBannerAd()
    }

    fun destroyAds() {
        bannerAdHelper.destroyBannerAd()
    }

    fun hideBannerAd() {
        bannerAdHelper.hideBannerAd()
    }

    fun showBannerAd() {
        bannerAdHelper.showBannerAd()
    }

    suspend fun setAdsEnabled(enabled: Boolean) {
        adConfig.setAdsEnabled(enabled)
    }

    suspend fun setAdFrequency(frequency: Int) {
        adConfig.setAdFrequency(frequency)
    }
}