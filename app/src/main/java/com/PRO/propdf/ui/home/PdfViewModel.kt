package com.PRO.propdf.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.repository.RepositoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor() : ViewModel() {

    private val _pdfState = MutableStateFlow<PdfState>(PdfState.Loading)
    val pdfState: StateFlow<PdfState> = _pdfState

    fun loadPdf(pdfId: Long) {
        viewModelScope.launch {
            try {
                val pdf = RepositoryProvider.getPdfRepository().getPdfById(pdfId)
                if (pdf != null) {
                    val pdfModel = com.PRO.propdf.data.model.PdfModel.fromPdfItem(pdf)
                    _pdfState.value = PdfState.Success(pdfModel)
                    
                    // Add to recent files
                    RepositoryProvider.getRecentRepository().addToRecent(pdfId)
                } else {
                    _pdfState.value = PdfState.Error("PDF not found")
                }
            } catch (e: Exception) {
                _pdfState.value = PdfState.Error("Failed to load PDF: ${e.message}")
            }
        }
    }

    fun updateLastOpenedPage(pdfId: Long, pageNumber: Int) {
        viewModelScope.launch {
            RepositoryProvider.getPdfRepository().updateLastOpenedPage(pdfId, pageNumber)
        }
    }

    sealed class PdfState {
        object Loading : PdfState()
        data class Success(val pdf: com.PRO.propdf.data.model.PdfModel) : PdfState()
        data class Error(val message: String) : PdfState()
    }
}