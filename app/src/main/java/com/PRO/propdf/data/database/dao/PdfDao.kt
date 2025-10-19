package com.PRO.propdf.data.dao

import androidx.room.*
import com.PRO.propdf.data.entities.PdfItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    
    @Query("SELECT * FROM pdf_items WHERE folderId = :folderId ORDER BY name ASC")
    fun getPdfsByFolder(folderId: Long): Flow<List<PdfItem>>
    
    @Query("SELECT * FROM pdf_items WHERE id = :pdfId")
    suspend fun getPdfById(pdfId: Long): PdfItem?
    
    @Query("SELECT * FROM pdf_items WHERE name LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%'")
    fun searchPdfs(query: String): Flow<List<PdfItem>>
    
    @Query("SELECT * FROM pdf_items WHERE isInBin = 0")
    fun getAllActivePdfs(): Flow<List<PdfItem>>
    
    @Query("SELECT * FROM pdf_items WHERE isInBin = 1")
    fun getPdfsInBin(): Flow<List<PdfItem>>
    
    @Query("UPDATE pdf_items SET isInBin = 1 WHERE id = :pdfId")
    suspend fun moveToBin(pdfId: Long)
    
    @Query("UPDATE pdf_items SET isInBin = 0 WHERE id = :pdfId")
    suspend fun restoreFromBin(pdfId: Long)
    
    @Query("DELETE FROM pdf_items WHERE id = :pdfId")
    suspend fun deletePermanently(pdfId: Long)
    
    @Query("DELETE FROM pdf_items WHERE isInBin = 1")
    suspend fun emptyBin()
    
    @Query("UPDATE pdf_items SET folderId = :newFolderId WHERE id = :pdfId")
    suspend fun moveToFolder(pdfId: Long, newFolderId: Long)
    
    @Query("UPDATE pdf_items SET name = :newName WHERE id = :pdfId")
    suspend fun renamePdf(pdfId: Long, newName: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdfItem: PdfItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPdfs(pdfItems: List<PdfItem>)
    
    @Update
    suspend fun updatePdf(pdfItem: PdfItem)
    
    @Query("SELECT COUNT(*) FROM pdf_items WHERE path = :path")
    suspend fun isPdfExists(path: String): Int
}