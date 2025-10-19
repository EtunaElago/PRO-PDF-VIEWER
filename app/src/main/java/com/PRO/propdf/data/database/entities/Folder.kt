package com.PRO.propdf.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "parentFolderId")
    val parentFolderId: Long?,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date,
    
    @ColumnInfo(name = "color")
    val color: Int? = null,
    
    @ColumnInfo(name = "icon")
    val icon: String? = null
)