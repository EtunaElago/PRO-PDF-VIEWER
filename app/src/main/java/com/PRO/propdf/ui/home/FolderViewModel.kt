package com.PRO.propdf.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.repository.RepositoryProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor() : ViewModel() {

    private val _folderState = MutableStateFlow<FolderState>(FolderState.Loading)
    val folderState: StateFlow<FolderState> = _folderState

    fun loadFolderContents(folderId: Long) {
        viewModelScope.launch {
            combine(
                RepositoryProvider.getFolderRepository().getSubFolders(folderId),
                RepositoryProvider.getPdfRepository().getPdfsByFolder(folderId)
            ) { subFolders, pdfs ->
                val folderModels = subFolders.map { folder ->
                    // Load counts for each subfolder
                    val pdfCount = RepositoryProvider.getFolderRepository().getPdfCountInFolder(folder.id)
                    val subFolderCount = RepositoryProvider.getFolderRepository().getSubFolderCount(folder.id)
                    FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
                }
                
                val pdfModels = pdfs.map { pdf ->
                    com.PRO.propdf.data.model.PdfModel.fromPdfItem(pdf)
                }

                FolderState.Success(folderModels to pdfModels)
            }.collect { state ->
                _folderState.value = state
            }
        }
    }

    sealed class FolderState {
        object Loading : FolderState()
        data class Success(val data: Pair<List<FolderModel>, List<com.PRO.propdf.data.model.PdfModel>>) : FolderState()
        data class Error(val message: String) : FolderState()
    }
}