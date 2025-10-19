package com.PRO.propdf.data.repository

import com.PRO.propdf.data.dao.FolderDao
import com.PRO.propdf.data.entities.Folder
import kotlinx.coroutines.flow.Flow
import java.util.Date

class FolderRepository(private val folderDao: FolderDao) {

    fun getRootFolders(): Flow<List<Folder>> {
        return folderDao.getRootFolders()
    }

    fun getSubFolders(parentId: Long): Flow<List<Folder>> {
        return folderDao.getSubFolders(parentId)
    }

    suspend fun getFolderById(folderId: Long): Folder? {
        return folderDao.getFolderById(folderId)
    }

    fun searchFolders(query: String): Flow<List<Folder>> {
        return folderDao.searchFolders(query)
    }

    fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders()
    }

    suspend fun createFolder(name: String, parentFolderId: Long? = null): Long {
        // Check if folder with same name already exists in the same location
        val existingFolder = if (parentFolderId == null) {
            folderDao.getRootFolderByName(name)
        } else {
            folderDao.getSubFolderByName(name, parentFolderId)
        }

        if (existingFolder != null) {
            throw IllegalArgumentException("Folder with name '$name' already exists")
        }

        val folder = Folder(
            name = name,
            parentFolderId = parentFolderId,
            createdAt = Date()
        )
        return folderDao.insertFolder(folder)
    }

    suspend fun renameFolder(folderId: Long, newName: String) {
        folderDao.renameFolder(folderId, newName)
    }

    suspend fun deleteFolder(folderId: Long) {
        folderDao.deleteFolder(folderId)
    }

    suspend fun getPdfCountInFolder(folderId: Long): Int {
        return folderDao.getPdfCountInFolder(folderId)
    }

    suspend fun getSubFolderCount(folderId: Long): Int {
        return folderDao.getSubFolderCount(folderId)
    }

    suspend fun getFolderContentsCount(folderId: Long): Int {
        val pdfCount = folderDao.getPdfCountInFolder(folderId)
        val subFolderCount = folderDao.getSubFolderCount(folderId)
        return pdfCount + subFolderCount
    }

    suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder)
    }

    suspend fun getFolderHierarchy(folderId: Long): List<Folder> {
        val hierarchy = mutableListOf<Folder>()
        var currentFolder = folderDao.getFolderById(folderId)
        
        while (currentFolder != null) {
            hierarchy.add(0, currentFolder)
            currentFolder = currentFolder.parentFolderId?.let { folderDao.getFolderById(it) }
        }
        
        return hierarchy
    }
}