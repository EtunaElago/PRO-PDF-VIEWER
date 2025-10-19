package com.PRO.propdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.PRO.propdf.ads.AdIntegrationHelper
import com.PRO.propdf.data.model.AppConstants
import com.PRO.propdf.databinding.ActivityMainBinding
import com.PRO.propdf.service.ServiceHelper
import com.PRO.propdf.ui.home.HomeViewModel
import com.PRO.propdf.utils.FileUtils
import com.PRO.propdf.utils.PermissionUtils
import com.PRO.propdf.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var adIntegrationHelper: AdIntegrationHelper

    private val homeViewModel: HomeViewModel by viewModels()

    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handlePdfSelection(it) }
    }

    private val multiplePdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri -> handlePdfSelection(uri) }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted, proceed with PDF selection
            showPdfPicker()
        } else {
            // Handle permission denial
            handlePermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        ThemeUtils.initializeTheme(this)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupAds()
        checkPermissions()
        setupObservers()
        
        // Generate thumbnails for existing PDFs on first launch
        lifecycleScope.launch {
            if (isFirstLaunch()) {
                ServiceHelper.startBatchThumbnailGeneration(this@MainActivity)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Show app open ad when activity starts
        adIntegrationHelper.showAppOpenAd(this)
    }

    override fun onResume() {
        super.onResume()
        adIntegrationHelper.resumeAds()
    }

    override fun onPause() {
        super.onPause()
        adIntegrationHelper.pauseAds()
    }

    override fun onDestroy() {
        super.onDestroy()
        adIntegrationHelper.destroyAds()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up bottom navigation
        binding.bottomNavigationView.setupWithNavController(navController)

        // Hide bottom navigation on certain destinations
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> showBottomNavigation()
                R.id.recentsFragment -> showBottomNavigation()
                R.id.settingsFragment -> showBottomNavigation()
                else -> hideBottomNavigation()
            }
        }
    }

    private fun setupAds() {
        adIntegrationHelper.initializeAds(this)
        
        // Setup banner ad
        adIntegrationHelper.setupBannerAd(binding.bannerAdContainer, this)
    }

    private fun checkPermissions() {
        if (!PermissionUtils.hasStoragePermissions(this)) {
            // Request permissions if not granted
            PermissionUtils.requestStoragePermissions(this)
        }
    }

    private fun setupObservers() {
        // Observe PDF addition requests from ViewModel
        homeViewModel.addPdfEvent.observe(this) {
            if (PermissionUtils.hasStoragePermissions(this)) {
                showPdfPicker()
            } else {
                requestStoragePermissions()
            }
        }

        // Observe theme changes
        lifecycleScope.launch {
            homeViewModel.themeChanged.collect { theme ->
                ThemeUtils.applyTheme(theme)
                recreate() // Recreate activity to apply new theme
            }
        }
    }

    private fun showPdfPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        
        try {
            pdfPickerLauncher.launch("application/pdf")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching PDF picker", e)
            // Fallback to multiple content picker
            multiplePdfPickerLauncher.launch("application/pdf")
        }
    }

    private fun handlePdfSelection(uri: Uri) {
        if (!FileUtils.isValidPdfFile(uri, this)) {
            // Show error - not a valid PDF file
            showError("Selected file is not a valid PDF")
            return
        }

        val fileSize = FileUtils.getFileSize(uri, this)
        if (FileUtils.isFileTooLarge(fileSize)) {
            // Show error - file too large
            showError("PDF file is too large. Maximum size is ${FileUtils.formatFileSize(AppConstants.MAX_FILE_SIZE_BYTES)}")
            return
        }

        val fileName = FileUtils.getFileName(uri, this)
        
        // Process the PDF file through ViewModel
        homeViewModel.processSelectedPdf(uri, fileName, fileSize)
        
        // Show interstitial ad after PDF addition
        adIntegrationHelper.handlePdfAddition(this) {
            Log.d(TAG, "PDF addition ad dismissed")
        }
    }

    private fun requestStoragePermissions() {
        val permissions = PermissionUtils.getStoragePermissions()
        permissionLauncher.launch(permissions)
    }

    private fun handlePermissionDenied() {
        if (PermissionUtils.shouldShowPermissionRationale(this)) {
            // Show rationale dialog
            showPermissionRationaleDialog()
        } else if (PermissionUtils.isPermissionPermanentlyDenied(this)) {
            // Show settings dialog
            showPermissionSettingsDialog()
        } else {
            showError("Storage permission is required to access PDF files")
        }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to access and manage your PDF files.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestStoragePermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Storage permission has been permanently denied. Please enable it in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                PermissionUtils.openAppSettings(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = android.view.View.VISIBLE
    }

    private fun hideBottomNavigation() {
        binding.bottomNavigationView.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        // You can use Snackbar or Toast to show error messages
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    private suspend fun isFirstLaunch(): Boolean {
        // Check if this is the first app launch
        // You can use SharedPreferences or DataStore for this
        val prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(AppConstants.KEY_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            prefs.edit().putBoolean(AppConstants.KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirstLaunch
    }

    // Handle back press for navigation
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController?.currentDestination?.id == R.id.homeFragment) {
            // If we're on home fragment, minimize the app
            moveTaskToBack(true)
        } else {
            // Otherwise, use normal back navigation
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}