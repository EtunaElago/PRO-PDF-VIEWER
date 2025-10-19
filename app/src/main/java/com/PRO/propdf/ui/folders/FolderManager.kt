package com.PRO.propdf.ui.folders

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FolderManager(private val context: Context) {

    fun getRootFolders(): Flow<List<FolderModel>> {
        return RepositoryProvider.getFolderRepository(context).getRootFolders().map { folders ->
            folders.map { folder ->
                // Load item counts for each folder
                val pdfCount = RepositoryProvider.getPdfRepository(context).getPdfsByFolder(folder.id).first().size
                val subFolderCount = RepositoryProvider.getFolderRepository(context).getSubFolders(folder.id).first().size
                FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
            }
        }
    }

    fun getSubFolders(parentFolderId: Long): Flow<List<FolderModel>> {
        return RepositoryProvider.getFolderRepository(context).getSubFolders(parentFolderId).map { folders ->
            folders.map { folder ->
                val pdfCount = RepositoryProvider.getPdfRepository(context).getPdfsByFolder(folder.id).first().size
                val subFolderCount = RepositoryProvider.getFolderRepository(context).getSubFolders(folder.id).first().size
                FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
            }
        }
    }

    suspend fun createFolder(name: String, parentFolderId: Long? = null): Result<Long> {
        return try {
            val folderId = RepositoryProvider.getFolderRepository(context).createFolder(name, parentFolderId)
            Result.success(folderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameFolder(folderId: Long, newName: String): Result<Unit> {
        return try {
            RepositoryProvider.getFolderRepository(context).renameFolder(folderId, newName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(folderId: Long): Result<Unit> {
        return try {
            // Check if folder is empty
            val pdfCount = RepositoryProvider.getFolderRepository(context).getPdfCountInFolder(folderId)
            val subFolderCount = RepositoryProvider.getFolderRepository(context).getSubFolderCount(folderId)
            
            if (pdfCount > 0 || subFolderCount > 0) {
                return Result.failure(IllegalStateException("Folder is not empty"))
            }
            
            RepositoryProvider.getFolderRepository(context).deleteFolder(folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFolders(folderIds: List<Long>): Result<Int> {
        return try {
            var successCount = 0
            folderIds.forEach { folderId ->
                val result = deleteFolder(folderId)
                if (result.isSuccess) {
                    successCount++
                }
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFolderHierarchy(folderId: Long): List<FolderModel> {
        return RepositoryProvider.getFolderRepository(context).getFolderHierarchy(folderId).map { folder ->
            FolderModel.fromFolder(folder)
        }
    }

    suspend fun moveFolder(sourceFolderId: Long, targetFolderId: Long): Result<Unit> {
        return try {
            // Prevent moving folder into itself or its subfolders
            if (sourceFolderId == targetFolderId) {
                return Result.failure(IllegalArgumentException("Cannot move folder into itself"))
            }

            val hierarchy = getFolderHierarchy(targetFolderId)
            if (hierarchy.any { it.id == sourceFolderId }) {
                return Result.failure(IllegalArgumentException("Cannot move folder into its subfolder"))
            }

            // Update folder's parent
            val folder = RepositoryProvider.getFolderRepository(context).getFolderById(sourceFolderId)
            folder?.let {
                val updatedFolder = it.copy(parentFolderId = targetFolderId)
                RepositoryProvider.getFolderRepository(context).updateFolder(updatedFolder)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFolders(query: String): Flow<List<FolderModel>> {
        return RepositoryProvider.getFolderRepository(context).searchFolders(query).map { folders ->
            folders.map { folder ->
                val pdfCount = RepositoryProvider.getPdfRepository(context).getPdfsByFolder(folder.id).first().size
                val subFolderCount = RepositoryProvider.getFolderRepository(context).getSubFolders(folder.id).first().size
                FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
            }
        }
    }

    suspend fun getFolderWithContents(folderId: Long): FolderModel? {
        return RepositoryProvider.getFolderRepository(context).getFolderById(folderId)?.let { folder ->
            val pdfCount = RepositoryProvider.getPdfRepository(context).getPdfsByFolder(folderId).first().size
            val subFolderCount = RepositoryProvider.getFolderRepository(context).getSubFolders(folderId).first().size
            FolderModel.fromFolder(folder).withItemCounts(pdfCount, subFolderCount)
        }
    }

    fun validateFolderName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Folder name cannot be empty")
            name.length > 50 -> ValidationResult.Error("Folder name is too long")
            name.contains("/") || name.contains("\\") -> ValidationResult.Error("Folder name contains invalid characters")
            else -> ValidationResult.Valid
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}