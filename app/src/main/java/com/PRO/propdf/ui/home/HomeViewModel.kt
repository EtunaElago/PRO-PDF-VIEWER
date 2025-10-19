package com.PRO.propdf.ui.home

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
class HomeViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val sortConfig = MutableStateFlow(
        SortConfig(
            sortBy = preferenceManager.getSortBy(),
            sortOrder = preferenceManager.getSortOrder()
        )
    )

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                RepositoryProvider.getFolderRepository().getRootFolders(),
                RepositoryProvider.getPdfRepository().getAllActivePdfs(),
                sortConfig
            ) { folders, pdfs, config ->
                val folderModels = folders.map { folder ->
                    FolderModel.fromFolder(folder).withItemCounts(0, 0) // Counts will be loaded separately
                }
                val pdfModels = pdfs.map { pdf ->
                    PdfModel.fromPdfItem(pdf)
                }

                val allItems = mutableListOf<Any>().apply {
                    addAll(folderModels)
                    addAll(pdfModels)
                }

                // Apply sorting
                sortItems(allItems, config)
            }.collect { items ->
                _uiState.value = HomeUiState.Success(items)
            }
        }
    }

    private fun sortItems(items: MutableList<Any>, config: SortConfig) {
        items.sortWith { item1, item2 ->
            when {
                item1 is FolderModel && item2 is PdfModel -> -1
                item1 is PdfModel && item2 is FolderModel -> 1
                item1 is FolderModel && item2 is FolderModel -> {
                    compareFolders(item1, item2, config)
                }
                item1 is PdfModel && item2 is PdfModel -> {
                    comparePdfs(item1, item2, config)
                }
                else -> 0
            }
        }

        if (config.sortOrder == AppConstants.ORDER_DESCENDING) {
            items.reverse()
        }
    }

    private fun compareFolders(folder1: FolderModel, folder2: FolderModel, config: SortConfig): Int {
        return when (config.sortBy) {
            AppConstants.SORT_BY_NAME -> folder1.name.compareTo(folder2.name, ignoreCase = true)
            AppConstants.SORT_BY_DATE -> folder1.createdAt.compareTo(folder2.createdAt)
            else -> folder1.name.compareTo(folder2.name, ignoreCase = true)
        }
    }

    private fun comparePdfs(pdf1: PdfModel, pdf2: PdfModel, config: SortConfig): Int {
        return when (config.sortBy) {
            AppConstants.SORT_BY_NAME -> pdf1.name.compareTo(pdf2.name, ignoreCase = true)
            AppConstants.SORT_BY_DATE -> pdf1.createdAt.compareTo(pdf2.createdAt)
            AppConstants.SORT_BY_SIZE -> pdf1.size.compareTo(pdf2.size)
            else -> pdf1.name.compareTo(pdf2.name, ignoreCase = true)
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) {
            _operationState.value = OperationState.Error("Folder name cannot be empty")
            return
        }

        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                RepositoryProvider.getFolderRepository().createFolder(name)
                _operationState.value = OperationState.Success("Folder created successfully")
                loadHomeData() // Refresh data
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Failed to create folder")
            }
        }
    }

    fun addPdf() {
        // This will be handled by the fragment using Activity Result API
        // The actual PDF addition logic will be in the fragment
    }

    fun moveToBin(pdfId: Long) {
        viewModelScope.launch {
            try {
                RepositoryProvider.getPdfRepository().moveToBin(pdfId)
                _operationState.value = OperationState.Success("File moved to bin")
                loadHomeData() // Refresh data
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to move file to bin")
            }
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            try {
                RepositoryProvider.getFolderRepository().deleteFolder(folderId)
                _operationState.value = OperationState.Success("Folder deleted successfully")
                loadHomeData() // Refresh data
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to delete folder")
            }
        }
    }

    fun updateSortConfig(sortConfig: SortConfig) {
        preferenceManager.setSortBy(sortConfig.sortBy)
        preferenceManager.setSortOrder(sortConfig.sortOrder)
        this.sortConfig.value = sortConfig
    }

    fun updateTheme(theme: String) {
        preferenceManager.setTheme(theme)
    }

    fun updateViewMode(viewMode: String) {
        preferenceManager.setViewMode(viewMode)
    }

    fun getSortConfig(): SortConfig = sortConfig.value

    fun getCurrentTheme(): String = preferenceManager.getTheme()

    fun getViewMode(): String = preferenceManager.getViewMode()

    sealed class HomeUiState {
        object Loading : HomeUiState()
        data class Success(val items: List<Any>) : HomeUiState()
        data class Error(val message: String) : HomeUiState()
    }
}