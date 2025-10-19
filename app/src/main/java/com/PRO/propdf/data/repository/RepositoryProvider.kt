package com.PRO.propdf.data.repository

import android.content.Context
import com.PRO.propdf.database.AppDatabase

object RepositoryProvider {

    private var pdfRepository: PdfRepository? = null
    private var folderRepository: FolderRepository? = null
    private var recentRepository: RecentRepository? = null

    fun getPdfRepository(context: Context): PdfRepository {
        return pdfRepository ?: synchronized(this) {
            pdfRepository ?: PdfRepository(
                AppDatabase.getInstance(context).pdfDao()
            ).also { pdfRepository = it }
        }
    }

    fun getFolderRepository(context: Context): FolderRepository {
        return folderRepository ?: synchronized(this) {
            folderRepository ?: FolderRepository(
                AppDatabase.getInstance(context).folderDao()
            ).also { folderRepository = it }
        }
    }

    fun getRecentRepository(context: Context): RecentRepository {
        return recentRepository ?: synchronized(this) {
            recentRepository ?: RecentRepository(
                AppDatabase.getInstance(context).recentDao()
            ).also { recentRepository = it }
        }
    }
}