package com.PRO.propdf.data.repository

import com.PRO.propdf.data.dao.PdfDao
import com.PRO.propdf.data.entities.PdfItem
import kotlinx.coroutines.flow.Flow
import java.util.Date

class PdfRepository(private val pdfDao: PdfDao) {

    fun getPdfsByFolder(folderId: Long): Flow<List<PdfItem>> {
        return pdfDao.getPdfsByFolder(folderId)
    }

    suspend fun getPdfById(pdfId: Long): PdfItem? {
        return pdfDao.getPdfById(pdfId)
    }

    fun searchPdfs(query: String): Flow<List<PdfItem>> {
        return pdfDao.searchPdfs(query)
    }

    fun getAllActivePdfs(): Flow<List<PdfItem>> {
        return pdfDao.getAllActivePdfs()
    }

    fun getPdfsInBin(): Flow<List<PdfItem>> {
        return pdfDao.getPdfsInBin()
    }

    suspend fun addPdf(
        name: String,
        path: String,
        size: Long,
        folderId: Long?,
        pageCount: Int = 0
    ): Long {
        val currentTime = Date()
        val pdfItem = PdfItem(
            name = name,
            path = path,
            size = size,
            folderId = folderId,
            createdAt = currentTime,
            modifiedAt = currentTime,
            pageCount = pageCount
        )
        return pdfDao.insertPdf(pdfItem)
    }

    suspend fun addMultiplePdfs(pdfItems: List<PdfItem>) {
        pdfDao.insertAllPdfs(pdfItems)
    }

    suspend fun moveToBin(pdfId: Long) {
        pdfDao.moveToBin(pdfId)
    }

    suspend fun restoreFromBin(pdfId: Long) {
        pdfDao.restoreFromBin(pdfId)
    }

    suspend fun deletePermanently(pdfId: Long) {
        pdfDao.deletePermanently(pdfId)
    }

    suspend fun emptyBin() {
        pdfDao.emptyBin()
    }

    suspend fun moveToFolder(pdfId: Long, newFolderId: Long) {
        pdfDao.moveToFolder(pdfId, newFolderId)
    }

    suspend fun renamePdf(pdfId: Long, newName: String) {
        pdfDao.renamePdf(pdfId, newName)
    }

    suspend fun updatePdf(pdfItem: PdfItem) {
        pdfDao.updatePdf(pdfItem)
    }

    suspend fun updateLastOpenedPage(pdfId: Long, pageNumber: Int) {
        val pdfItem = pdfDao.getPdfById(pdfId)
        pdfItem?.let {
            val updatedPdf = it.copy(lastOpenedPage = pageNumber, modifiedAt = Date())
            pdfDao.updatePdf(updatedPdf)
        }
    }

    suspend fun isPdfExists(path: String): Boolean {
        return pdfDao.isPdfExists(path) > 0
    }

    suspend fun getPdfCount(): Int {
        return pdfDao.getAllActivePdfs().first().size
    }
}