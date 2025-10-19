package com.PRO.propdf.data.dao

import androidx.room.*
import com.PRO.propdf.data.entities.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    
    @Query("SELECT * FROM folders WHERE parentFolderId IS NULL ORDER BY name ASC")
    fun getRootFolders(): Flow<List<Folder>>
    
    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId ORDER BY name ASC")
    fun getSubFolders(parentId: Long): Flow<List<Folder>>
    
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): Folder?
    
    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%'")
    fun searchFolders(query: String): Flow<List<Folder>>
    
    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<Folder>>
    
    @Query("SELECT COUNT(*) FROM pdf_items WHERE folderId = :folderId")
    suspend fun getPdfCountInFolder(folderId: Long): Int
    
    @Query("SELECT COUNT(*) FROM folders WHERE parentFolderId = :folderId")
    suspend fun getSubFolderCount(folderId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long
    
    @Update
    suspend fun updateFolder(folder: Folder)
    
    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)
    
    @Query("UPDATE folders SET name = :newName WHERE id = :folderId")
    suspend fun renameFolder(folderId: Long, newName: String)
    
    @Query("SELECT * FROM folders WHERE name = :name AND parentFolderId IS NULL")
    suspend fun getRootFolderByName(name: String): Folder?
    
    @Query("SELECT * FROM folders WHERE name = :name AND parentFolderId = :parentId")
    suspend fun getSubFolderByName(name: String, parentId: Long): Folder?
}