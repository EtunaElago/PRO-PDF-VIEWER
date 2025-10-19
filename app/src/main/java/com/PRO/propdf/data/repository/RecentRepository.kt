package com.PRO.propdf.data.repository

import com.PRO.propdf.data.dao.RecentDao
import com.PRO.propdf.data.entities.RecentFile
import kotlinx.coroutines.flow.Flow
import java.util.Date

class RecentRepository(private val recentDao: RecentDao) {

    fun getRecentFiles(): Flow<List<RecentFile>> {
        return recentDao.getRecentFiles()
    }

    suspend fun addToRecent(pdfId: Long, lastOpenedPage: Int = 0) {
        val recentFile = RecentFile(
            pdfId = pdfId,
            lastOpened = Date(),
            lastOpenedPage = lastOpenedPage
        )
        recentDao.insertOrUpdateRecent(recentFile)
        
        // Clean up old entries if we have more than 50
        val count = recentDao.getRecentFilesCount()
        if (count > 50) {
            val cutoffTime = Date().time - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
            recentDao.deleteOldRecentFiles(cutoffTime)
        }
    }

    suspend fun updateLastOpenedPage(recentId: Long, pageNumber: Int) {
        val recentFile = recentDao.getRecentFileByPdfId(recentId)
        recentFile?.let {
            val updatedRecent = it.copy(lastOpenedPage = pageNumber)
            recentDao.insertOrUpdateRecent(updatedRecent)
        }
    }

    suspend fun clearRecentFiles() {
        // Get all recent files and delete them
        val recentFiles = recentDao.getRecentFiles().first()
        recentFiles.forEach { recentFile ->
            recentDao.deleteRecentFile(recentFile.id)
        }
    }

    suspend fun removeFromRecent(recentId: Long) {
        recentDao.deleteRecentFile(recentId)
    }

    suspend fun getRecentFilesCount(): Int {
        return recentDao.getRecentFilesCount()
    }

    suspend fun cleanupOldRecentFiles() {
        val cutoffTime = Date().time - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        recentDao.deleteOldRecentFiles(cutoffTime)
    }
}