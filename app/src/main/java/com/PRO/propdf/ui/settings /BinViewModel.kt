package com.PRO.propdf.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.data.repository.RepositoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinViewModel @Inject constructor() : ViewModel() {

    private val _binState = MutableStateFlow<BinState>(BinState.Loading)
    val binState: StateFlow<BinState> = _binState

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    init {
        loadBinItems()
    }

    fun loadBinItems() {
        viewModelScope.launch {
            _binState.value = BinState.Loading
            try {
                val pdfsInBin = RepositoryProvider.getPdfRepository().getPdfsInBin()
                    .map { pdfItems ->
                        pdfItems.map { pdfItem ->
                            PdfModel.fromPdfItem(pdfItem)
                        }
                    }
                
                pdfsInBin.collect { pdfs ->
                    _binState.value = BinState.Success(pdfs)
                }
            } catch (e: Exception) {
                _binState.value = BinState.Error(e.message ?: "Failed to load bin items")
            }
        }
    }

    fun restoreFromBin(pdfId: Long) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getPdfRepository().restoreFromBin(pdfId)
                _operationState.value = OperationState.Success("File restored successfully")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to restore file")
            }
        }
    }

    fun deletePermanently(pdfId: Long) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getPdfRepository().deletePermanently(pdfId)
                _operationState.value = OperationState.Success("File permanently deleted")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to delete file")
            }
        }
    }

    fun emptyBin() {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getPdfRepository().emptyBin()
                _operationState.value = OperationState.Success("Bin emptied successfully")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to empty bin")
            }
        }
    }

    fun restoreMultiple(pdfIds: List<Long>) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                pdfIds.forEach { pdfId ->
                    RepositoryProvider.getPdfRepository().restoreFromBin(pdfId)
                }
                _operationState.value = OperationState.Success("${pdfIds.size} files restored")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to restore files")
            }
        }
    }

    fun deleteMultiple(pdfIds: List<Long>) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                pdfIds.forEach { pdfId ->
                    RepositoryProvider.getPdfRepository().deletePermanently(pdfId)
                }
                _operationState.value = OperationState.Success("${pdfIds.size} files deleted")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to delete files")
            }
        }
    }

    sealed class BinState {
        object Loading : BinState()
        data class Success(val pdfs: List<PdfModel>) : BinState()
        data class Error(val message: String) : BinState()
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}