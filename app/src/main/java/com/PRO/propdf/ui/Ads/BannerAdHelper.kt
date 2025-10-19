package com.PRO.propdf.ads

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerAdHelper @Inject constructor(
    private val adManager: AdManager
) {

    private var bannerAdView: AdView? = null
    private var adRefreshJob: Job? = null

    companion object {
        private const val TAG = "BannerAdHelper"
        private const val BANNER_AD_REFRESH_INTERVAL = 30000L // 30 seconds
    }

    fun setupBannerAd(container: ViewGroup, context: Context) {
        // Remove existing banner if any
        bannerAdView?.let {
            container.removeView(it)
        }

        // Create new banner ad
        bannerAdView = AdView(context).apply {
            adUnitId = adManager.getBannerAdUnitId()
            adSize = AdSize.BANNER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(bannerAdView)

        // Load the first ad
        loadBannerAd()

        // Start auto-refresh
        startAutoRefresh()
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        
        bannerAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
                bannerAdView?.visibility = android.view.View.VISIBLE
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "Banner ad failed to load: ${loadAdError.message}")
                bannerAdView?.visibility = android.view.View.GONE
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner ad was clicked")
            }

            override fun onAdOpened() {
                Log.d(TAG, "Banner ad opened")
            }

            override fun onAdClosed() {
                Log.d(TAG, "Banner ad closed")
            }
        }

        bannerAdView?.loadAd(adRequest)
    }

    private fun startAutoRefresh() {
        adRefreshJob?.cancel()
        
        adRefreshJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(BANNER_AD_REFRESH_INTERVAL)
                loadBannerAd()
            }
        }
    }

    fun pauseBannerAd() {
        bannerAdView?.pause()
        adRefreshJob?.cancel()
    }

    fun resumeBannerAd() {
        bannerAdView?.resume()
        startAutoRefresh()
    }

    fun destroyBannerAd() {
        adRefreshJob?.cancel()
        bannerAdView?.destroy()
        bannerAdView = null
    }

    fun hideBannerAd() {
        bannerAdView?.visibility = android.view.View.GONE
    }

    fun showBannerAd() {
        bannerAdView?.visibility = android.view.View.VISIBLE
    }

    fun isBannerAdLoaded(): Boolean {
        return bannerAdView?.visibility == android.view.View.VISIBLE
    }
}