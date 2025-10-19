package com.PRO.propdf.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "recent_files")
data class RecentFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "pdfId")
    val pdfId: Long,
    
    @ColumnInfo(name = "lastOpened")
    val lastOpened: Date,
    
    @ColumnInfo(name = "lastOpenedPage")
    val lastOpenedPage: Int = 0
)