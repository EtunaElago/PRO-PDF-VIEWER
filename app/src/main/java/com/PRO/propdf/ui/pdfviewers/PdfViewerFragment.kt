package com.PRO.propdf.ui.pdfviewer

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.PRO.propdf.R
import com.PRO.propdf.databinding.FragmentPdfViewerBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfViewerViewModel by viewModels()
    private val args: PdfViewerFragmentArgs by navArgs()

    private var isSystemUiVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupPdfViewer()
        setupObservers()
        setupClickListeners()
        setupGestures()
        
        // Load the PDF
        viewModel.loadPdf(args.pdfId, args.pdfPath, args.pdfName)
    }

    private fun setupPdfViewer() {
        binding.pdfViewer.apply {
            // Configure PDF viewer settings
            setSwipeEnabled(true)
            setNightMode(isNightMode())
            setPageSnap(true)
            setPageFling(true)
            
            // Set page change listener
            setOnPageChangeListener { page, total ->
                viewModel.onPageChanged(page, total)
                updatePageIndicator(page + 1, total)
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pdfState.collectLatest { state ->
                when (state) {
                    is PdfViewerViewModel.PdfState.Loading -> {
                        showLoading(true)
                        hideError()
                    }
                    is PdfViewerViewModel.PdfState.Success -> {
                        showLoading(false)
                        hideError()
                        displayPdf(state.pdfPath)
                        updateToolbarTitle(state.pdfName)
                    }
                    is PdfViewerViewModel.PdfState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                updateUiState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pageState.collectLatest { state ->
                updatePageState(state)
            }
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    showSearchDialog()
                    true
                }
                R.id.action_zoom_in -> {
                    binding.pdfViewer.zoomIn()
                    true
                }
                R.id.action_zoom_out -> {
                    binding.pdfViewer.zoomOut()
                    true
                }
                R.id.action_fit_width -> {
                    binding.pdfViewer.fitToWidth()
                    true
                }
                R.id.action_fit_page -> {
                    binding.pdfViewer.fitToPage()
                    true
                }
                else -> false
            }
        }

        // Page navigation buttons
        binding.fabPrevious.setOnClickListener {
            binding.pdfViewer.previousPage()
        }

        binding.fabNext.setOnClickListener {
            binding.pdfViewer.nextPage()
        }

        // Toggle UI visibility
        binding.pdfViewer.setOnClickListener {
            toggleSystemUi()
        }
    }

    private fun setupGestures() {
        // Double tap to zoom
        binding.pdfViewer.setOnDoubleTapListener { e ->
            binding.pdfViewer.zoomWithAnimation(e.x, e.y)
            true
        }
    }

    private fun displayPdf(pdfPath: String) {
        try {
            binding.pdfViewer.fromFile(java.io.File(pdfPath))
                .onLoad { 
                    viewModel.onPdfLoaded()
                }
                .onError { error ->
                    viewModel.onPdfLoadError(error)
                }
                .load()
        } catch (e: Exception) {
            viewModel.onPdfLoadError(e)
        }
    }

    private fun updateToolbarTitle(pdfName: String) {
        binding.toolbar.title = pdfName
    }

    private fun updatePageIndicator(currentPage: Int, totalPages: Int) {
        binding.textPageIndicator.text = getString(R.string.page_indicator, currentPage, totalPages)
    }

    private fun updateUiState(state: PdfViewerViewModel.UiState) {
        when (state) {
            is PdfViewerViewModel.UiState.SystemUiVisible -> {
                showSystemUi()
            }
            is PdfViewerViewModel.UiState.SystemUiHidden -> {
                hideSystemUi()
            }
        }
    }

    private fun updatePageState(state: PdfViewerViewModel.PageState) {
        binding.fabPrevious.isEnabled = state.hasPreviousPage
        binding.fabNext.isEnabled = state.hasNextPage
        
        // Update page navigation FAB visibility based on settings
        binding.fabPrevious.visibility = if (state.showNavigation) View.VISIBLE else View.GONE
        binding.fabNext.visibility = if (state.showNavigation) View.VISIBLE else View.GONE
        binding.textPageIndicator.visibility = if (state.showNavigation) View.VISIBLE else View.GONE
    }

    private fun showSystemUi() {
        isSystemUiVisible = true
        binding.appBarLayout.visibility = View.VISIBLE
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.fabPrevious.show()
        binding.fabNext.show()
    }

    private fun hideSystemUi() {
        isSystemUiVisible = false
        binding.appBarLayout.visibility = View.GONE
        binding.bottomAppBar.visibility = View.GONE
        binding.fabPrevious.hide()
        binding.fabNext.hide()
    }

    private fun toggleSystemUi() {
        if (isSystemUiVisible) {
            viewModel.hideSystemUi()
        } else {
            viewModel.showSystemUi()
        }
    }

    private fun showSearchDialog() {
        SearchPdfDialog { searchQuery ->
            viewModel.searchInPdf(searchQuery)
        }.show(parentFragmentManager, "SearchPdfDialog")
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorView.root.visibility = View.VISIBLE
        binding.errorView.textError.text = message
        binding.errorView.buttonRetry.setOnClickListener {
            viewModel.loadPdf(args.pdfId, args.pdfPath, args.pdfName)
        }
    }

    private fun hideError() {
        binding.errorView.root.visibility = View.GONE
    }

    private fun isNightMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}