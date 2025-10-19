package com.PRO.propdf.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.PRO.propdf.R
import com.PRO.propdf.databinding.FragmentSettingsBinding
import com.PRO.propdf.utils.ShareUtils
import com.PRO.propdf.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupSettingsList()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSettingsList() {
        val settingsAdapter = SettingsAdapter(getSettingsItems()) { setting ->
            onSettingClicked(setting)
        }
        
        binding.recyclerViewSettings.apply {
            adapter = settingsAdapter
            setHasFixedSize(true)
        }
    }

    private fun getSettingsItems(): List<SettingItem> {
        return listOf(
            SettingItem(
                id = SettingItemId.LANGUAGE,
                title = getString(R.string.language),
                description = getString(R.string.language_description),
                iconRes = R.drawable.ic_language,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.THEME,
                title = getString(R.string.theme),
                description = getCurrentThemeDescription(),
                iconRes = R.drawable.ic_theme,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.VIEW_MODE,
                title = getString(R.string.view_mode),
                description = getCurrentViewModeDescription(),
                iconRes = R.drawable.ic_view_mode,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.BIN,
                title = getString(R.string.bin),
                description = getString(R.string.bin_description),
                iconRes = R.drawable.ic_bin,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.ABOUT,
                title = getString(R.string.about),
                description = getString(R.string.about_description),
                iconRes = R.drawable.ic_info,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.PRIVACY_POLICY,
                title = getString(R.string.privacy_policy),
                description = getString(R.string.privacy_policy_description),
                iconRes = R.drawable.ic_privacy,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.TERMS_OF_SERVICE,
                title = getString(R.string.terms_of_service),
                description = getString(R.string.terms_of_service_description),
                iconRes = R.drawable.ic_terms,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.SHARE_APP,
                title = getString(R.string.share_app),
                description = getString(R.string.share_app_description),
                iconRes = R.drawable.ic_share,
                type = SettingType.NORMAL
            ),
            SettingItem(
                id = SettingItemId.CONTACT_US,
                title = getString(R.string.contact_us),
                description = getString(R.string.contact_us_description),
                iconRes = R.drawable.ic_mail,
                type = SettingType.NORMAL
            )
        )
    }

    private fun getCurrentThemeDescription(): String {
        val theme = ThemeUtils.getCurrentTheme(requireContext())
        return when (theme) {
            ThemeUtils.THEME_LIGHT -> getString(R.string.theme_light)
            ThemeUtils.THEME_DARK -> getString(R.string.theme_dark)
            ThemeUtils.THEME_SYSTEM -> getString(R.string.theme_system)
            else -> getString(R.string.theme_system)
        }
    }

    private fun getCurrentViewModeDescription(): String {
        // This would come from SharedPreferences
        // For now, return default
        return getString(R.string.view_mode_grid)
    }

    private fun setupClickListeners() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
                    true
                }
                R.id.navigation_recents -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_recentsFragment)
                    true
                }
                R.id.navigation_settings -> {
                    // Already on settings
                    true
                }
                else -> false
            }
        }
    }

    private fun onSettingClicked(setting: SettingItem) {
        when (setting.id) {
            SettingItemId.LANGUAGE -> {
                // Navigate to language selection
                findNavController().navigate(R.id.action_settingsFragment_to_languageFragment)
            }
            SettingItemId.THEME -> {
                // Navigate to theme selection
                findNavController().navigate(R.id.action_settingsFragment_to_themeFragment)
            }
            SettingItemId.VIEW_MODE -> {
                // Navigate to view mode selection
                findNavController().navigate(R.id.action_settingsFragment_to_viewModeFragment)
            }
            SettingItemId.BIN -> {
                // Navigate to bin
                findNavController().navigate(R.id.action_settingsFragment_to_binFragment)
            }
            SettingItemId.ABOUT -> {
                // Navigate to about
                findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
            }
            SettingItemId.PRIVACY_POLICY -> {
                // Open privacy policy (web or in-app)
                openPrivacyPolicy()
            }
            SettingItemId.TERMS_OF_SERVICE -> {
                // Open terms of service (web or in-app)
                openTermsOfService()
            }
            SettingItemId.SHARE_APP -> {
                // Share app
                shareApp()
            }
            SettingItemId.CONTACT_US -> {
                // Contact us via email
                contactUs()
            }
        }
    }

    private fun openPrivacyPolicy() {
        val privacyPolicyUrl = "https://yourwebsite.com/privacy-policy"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        startActivity(intent)
    }

    private fun openTermsOfService() {
        val termsUrl = "https://yourwebsite.com/terms-of-service"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
        startActivity(intent)
    }

    private fun shareApp() {
        ShareUtils.shareApp(requireContext())
    }

    private fun contactUs() {
        val email = "support@yourcompany.com"
        val subject = "PRO PDF Viewer - Feedback"
        ShareUtils.contactUs(requireContext(), email, subject)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}