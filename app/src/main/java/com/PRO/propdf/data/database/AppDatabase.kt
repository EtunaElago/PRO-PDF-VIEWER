package com.PRO.propdf.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.PRO.propdf.data.entities.PdfItem
import com.PRO.propdf.data.entities.Folder
import com.PRO.propdf.data.entities.RecentFile
import com.PRO.propdf.data.dao.PdfDao
import com.PRO.propdf.data.dao.FolderDao
import com.PRO.propdf.data.dao.RecentDao
import com.PRO.propdf.database.converters.DateConverter

@Database(
    entities = [PdfItem::class, Folder::class, RecentFile::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun pdfDao(): PdfDao
    abstract fun folderDao(): FolderDao
    abstract fun recentDao(): RecentDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pro_pdf_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}