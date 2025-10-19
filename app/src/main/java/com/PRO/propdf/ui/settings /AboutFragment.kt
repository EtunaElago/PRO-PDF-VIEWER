package com.PRO.propdf.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.PRO.propdf.BuildConfig
import com.PRO.propdf.R
import com.PRO.propdf.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupAppInfo()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.about)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAppInfo() {
        binding.apply {
            textAppName.text = getString(R.string.app_name)
            textVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
            textAppDescription.text = getString(R.string.app_description)
            
            // Set app icon (you can use your actual app icon here)
            imageAppIcon.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Privacy Policy
            layoutPrivacyPolicy.setOnClickListener {
                openPrivacyPolicy()
            }
            
            // Terms of Service
            layoutTerms.setOnClickListener {
                openTermsOfService()
            }
            
            // Rate App
            layoutRateApp.setOnClickListener {
                rateApp()
            }
            
            // Share App
            layoutShareApp.setOnClickListener {
                shareApp()
            }
            
            // Developer Website
            layoutWebsite.setOnClickListener {
                openDeveloperWebsite()
            }
        }
    }

    private fun openPrivacyPolicy() {
        val privacyPolicyUrl = "https://yourwebsite.com/privacy-policy"
        openUrl(privacyPolicyUrl)
    }

    private fun openTermsOfService() {
        val termsUrl = "https://yourwebsite.com/terms-of-service"
        openUrl(termsUrl)
    }

    private fun openDeveloperWebsite() {
        val websiteUrl = "https://yourwebsite.com"
        openUrl(websiteUrl)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun rateApp() {
        try {
            val packageName = requireContext().packageName
            val playStoreUrl = "market://details?id=$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web URL if Play Store app is not installed
            val packageName = requireContext().packageName
            val playStoreWebUrl = "https://play.google.com/store/apps/details?id=$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreWebUrl))
            startActivity(intent)
        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_app_text, getString(R.string.app_name))
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_app)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}