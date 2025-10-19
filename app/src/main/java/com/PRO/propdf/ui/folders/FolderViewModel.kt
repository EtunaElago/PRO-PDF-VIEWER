package com.PRO.propdf.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.model.*
import com.PRO.propdf.data.repository.RepositoryProvider
import com.PRO.propdf.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _folderState = MutableStateFlow<FolderState>(FolderState.Loading)
    val folderState: StateFlow<FolderState> = _folderState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val sortConfig = MutableStateFlow(
        SortConfig(
            sortBy = preferenceManager.getSortBy(),
            sortOrder = preferenceManager.getSortOrder()
        )
    )

    fun loadFolderContents(folderId: Long) {
        viewModelScope.launch {
            combine(
                RepositoryProvider.getFolderRepository().getSubFolders(folderId),
                RepositoryProvider.getPdfRepository().getPdfsByFolder(folderId),
                sortConfig
            ) { folders, pdfs, config ->
                val folderModels = folders.map { folder ->
                    // Load counts for each subfolder
                    val pdfCount = RepositoryProvider.getFolderRepository().getPdfCountInFolder(folder.id)
                    val subFolderCount = RepositoryProvider.getFolderRepository().getSubFolderCount(folder.id)
                    FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
                }
                
                val pdfModels = pdfs.map { pdf ->
                    PdfModel.fromPdfItem(pdf)
                }

                // Apply sorting
                val sortedFolders = sortFolders(folderModels, config)
                val sortedPdfs = sortPdfs(pdfModels, config)

                FolderState.Success(sortedFolders, sortedPdfs)
            }.collect { state ->
                _folderState.value = state
            }
        }
    }

    private fun sortFolders(folders: List<FolderModel>, config: SortConfig): List<FolderModel> {
        return when (config.sortBy) {
            AppConstants.SORT_BY_NAME -> folders.sortedBy { it.name }
            AppConstants.SORT_BY_DATE -> folders.sortedBy { it.createdAt }
            else -> folders.sortedBy { it.name }
        }.let { sorted ->
            if (config.sortOrder == AppConstants.ORDER_DESCENDING) sorted.reversed() else sorted
        }
    }

    private fun sortPdfs(pdfs: List<PdfModel>, config: SortConfig): List<PdfModel> {
        return when (config.sortBy) {
            AppConstants.SORT_BY_NAME -> pdfs.sortedBy { it.name }
            AppConstants.SORT_BY_DATE -> pdfs.sortedBy { it.createdAt }
            AppConstants.SORT_BY_SIZE -> pdfs.sortedBy { it.size }
            else -> pdfs.sortedBy { it.name }
        }.let { sorted ->
            if (config.sortOrder == AppConstants.ORDER_DESCENDING) sorted.reversed() else sorted
        }
    }

    fun createFolder(name: String, parentFolderId: Long? = null) {
        if (name.isBlank()) {
            _operationState.value = OperationState.Error("Folder name cannot be empty")
            return
        }

        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getFolderRepository().createFolder(name, parentFolderId)
                _operationState.value = OperationState.Success("Folder created successfully")
                // Refresh current folder contents
                parentFolderId?.let { loadFolderContents(it) }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Failed to create folder")
            }
        }
    }

    fun addPdfToFolder(folderId: Long) {
        // This will be handled by the fragment using Activity Result API
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            try {
                RepositoryProvider.getFolderRepository().deleteFolder(folderId)
                _operationState.value = OperationState.Success("Folder deleted successfully")
                // Refresh will be handled by navigation
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Failed to delete folder")
            }
        }
    }

    fun updateSortConfig(sortConfig: SortConfig) {
        preferenceManager.setSortBy(sortConfig.sortBy)
        preferenceManager.setSortOrder(sortConfig.sortOrder)
        this.sortConfig.value = sortConfig
    }

    fun getViewMode(): String = preferenceManager.getViewMode()

    sealed class FolderState {
        object Loading : FolderState()
        data class Success(val folders: List<FolderModel>, val pdfs: List<PdfModel>) : FolderState()
        data class Error(val message: String) : FolderState()
    }
}