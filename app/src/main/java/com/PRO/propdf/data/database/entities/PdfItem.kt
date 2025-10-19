package com.PRO.propdf.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "pdf_items")
data class PdfItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "path")
    val path: String,
    
    @ColumnInfo(name = "size")
    val size: Long,
    
    @ColumnInfo(name = "folderId")
    val folderId: Long?,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date,
    
    @ColumnInfo(name = "modifiedAt")
    val modifiedAt: Date,
    
    @ColumnInfo(name = "pageCount")
    val pageCount: Int = 0,
    
    @ColumnInfo(name = "lastOpenedPage")
    val lastOpenedPage: Int = 0,
    
    @ColumnInfo(name = "isInBin")
    val isInBin: Boolean = false,
    
    @ColumnInfo(name = "thumbnailPath")
    val thumbnailPath: String? = null
)