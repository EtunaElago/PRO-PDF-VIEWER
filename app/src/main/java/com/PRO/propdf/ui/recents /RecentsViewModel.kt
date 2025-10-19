package com.PRO.propdf.ui.recents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.model.RecentModel
import com.PRO.propdf.data.repository.RepositoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentsViewModel @Inject constructor() : ViewModel() {

    private val _recentsState = MutableStateFlow<RecentsState>(RecentsState.Loading)
    val recentsState: StateFlow<RecentsState> = _recentsState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    init {
        loadRecentFiles()
    }

    fun loadRecentFiles() {
        viewModelScope.launch {
            RepositoryProvider.getRecentRepository().getRecentFiles()
                .map { recentFiles ->
                    val recentModels = recentFiles.map { recentFile ->
                        // Get PDF details for each recent file
                        val pdf = RepositoryProvider.getPdfRepository().getPdfById(recentFile.pdfId)
                        val folderName = pdf?.folderId?.let { folderId ->
                            RepositoryProvider.getFolderRepository().getFolderById(folderId)?.name
                        }
                        
                        RecentModel.fromRecentFile(recentFile, pdf, folderName)
                    }
                    RecentsState.Success(recentModels)
                }
                .catch { e ->
                    RecentsState.Error(e.message ?: "Failed to load recent files")
                }
                .collect { state ->
                    _recentsState.value = state
                }
        }
    }

    fun clearAllRecentFiles() {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getRecentRepository().clearRecentFiles()
                _operationState.value = OperationState.Success("All recent files cleared")
                loadRecentFiles() // Refresh the list
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to clear recent files")
            }
        }
    }

    fun removeRecentFile(recentId: Long) {
        viewModelScope.launch {
            try {
                RepositoryProvider.getRecentRepository().removeFromRecent(recentId)
                _operationState.value = OperationState.Success("Removed from recent files")
                loadRecentFiles() // Refresh the list
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to remove from recent files")
            }
        }
    }

    fun removeMultipleRecentFiles(recentIds: List<Long>) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                recentIds.forEach { recentId ->
                    RepositoryProvider.getRecentRepository().removeFromRecent(recentId)
                }
                _operationState.value = OperationState.Success("${recentIds.size} items removed")
                loadRecentFiles() // Refresh the list
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to remove items")
            }
        }
    }

    sealed class RecentsState {
        object Loading : RecentsState()
        data class Success(val recents: List<RecentModel>) : RecentsState()
        data class Error(val message: String) : RecentsState()
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}