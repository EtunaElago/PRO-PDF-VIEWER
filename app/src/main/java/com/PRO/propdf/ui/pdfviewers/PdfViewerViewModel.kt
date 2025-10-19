package com.PRO.propdf.ui.pdfviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.repository.RepositoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor() : ViewModel() {

    private val _pdfState = MutableStateFlow<PdfState>(PdfState.Loading)
    val pdfState: StateFlow<PdfState> = _pdfState

    private val _uiState = MutableStateFlow<UiState>(UiState.SystemUiVisible)
    val uiState: StateFlow<UiState> = _uiState

    private val _pageState = MutableStateFlow(
        PageState(
            currentPage = 0,
            totalPages = 0,
            hasPreviousPage = false,
            hasNextPage = false,
            showNavigation = true
        )
    )
    val pageState: StateFlow<PageState> = _pageState

    private var currentPdfId: Long = -1

    fun loadPdf(pdfId: Long, pdfPath: String, pdfName: String) {
        currentPdfId = pdfId
        _pdfState.value = PdfState.Loading
        
        viewModelScope.launch {
            try {
                // Verify PDF exists and is accessible
                val file = java.io.File(pdfPath)
                if (!file.exists()) {
                    _pdfState.value = PdfState.Error("PDF file not found")
                    return@launch
                }

                // Add to recent files
                RepositoryProvider.getRecentRepository().addToRecent(pdfId)
                
                _pdfState.value = PdfState.Success(pdfPath, pdfName)
            } catch (e: Exception) {
                _pdfState.value = PdfState.Error("Failed to load PDF: ${e.message}")
            }
        }
    }

    fun onPageChanged(page: Int, totalPages: Int) {
        _pageState.value = PageState(
            currentPage = page,
            totalPages = totalPages,
            hasPreviousPage = page > 0,
            hasNextPage = page < totalPages - 1,
            showNavigation = _pageState.value.showNavigation
        )

        // Update last opened page in database
        viewModelScope.launch {
            if (currentPdfId != -1L) {
                RepositoryProvider.getPdfRepository().updateLastOpenedPage(currentPdfId, page + 1)
                RepositoryProvider.getRecentRepository().addToRecent(currentPdfId, page + 1)
            }
        }
    }

    fun onPdfLoaded() {
        // PDF loaded successfully
        // Could trigger analytics or other events here
    }

    fun onPdfLoadError(error: Throwable) {
        _pdfState.value = PdfState.Error("Failed to load PDF: ${error.message}")
    }

    fun searchInPdf(searchQuery: String) {
        // This would integrate with the PDF viewer's search functionality
        // Implementation depends on the PDF viewer library
        viewModelScope.launch {
            // Handle search logic
        }
    }

    fun showSystemUi() {
        _uiState.value = UiState.SystemUiVisible
    }

    fun hideSystemUi() {
        _uiState.value = UiState.SystemUiHidden
    }

    fun toggleNavigationVisibility() {
        _pageState.value = _pageState.value.copy(
            showNavigation = !_pageState.value.showNavigation
        )
    }

    sealed class PdfState {
        object Loading : PdfState()
        data class Success(val pdfPath: String, val pdfName: String) : PdfState()
        data class Error(val message: String) : PdfState()
    }

    sealed class UiState {
        object SystemUiVisible : UiState()
        object SystemUiHidden : UiState()
    }

    data class PageState(
        val currentPage: Int,
        val totalPages: Int,
        val hasPreviousPage: Boolean,
        val hasNextPage: Boolean,
        val showNavigation: Boolean
    )
}