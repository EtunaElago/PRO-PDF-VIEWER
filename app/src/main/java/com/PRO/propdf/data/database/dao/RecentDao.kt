package com.PRO.propdf.data.dao

import androidx.room.*
import com.PRO.propdf.data.entities.RecentFile
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {
    
    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT 50")
    fun getRecentFiles(): Flow<List<RecentFile>>
    
    @Query("SELECT * FROM recent_files WHERE pdfId = :pdfId")
    suspend fun getRecentFileByPdfId(pdfId: Long): RecentFile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecent(recentFile: RecentFile)
    
    @Query("DELETE FROM recent_files WHERE id = :recentId")
    suspend fun deleteRecentFile(recentId: Long)
    
    @Query("DELETE FROM recent_files WHERE lastOpened < :timestamp")
    suspend fun deleteOldRecentFiles(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun getRecentFilesCount(): Int
}