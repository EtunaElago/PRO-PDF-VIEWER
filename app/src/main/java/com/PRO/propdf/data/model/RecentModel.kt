package com.PRO.propdf.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class RecentModel(
    val id: Long = 0,
    val pdfId: Long,
    val pdfName: String,
    val pdfPath: String,
    val pdfUri: Uri? = null,
    val lastOpened: Date,
    val lastOpenedFormatted: String,
    val lastOpenedPage: Int = 0,
    val totalPages: Int = 0,
    val folderName: String? = null,
    val thumbnailPath: String? = null,
    val isPdfExists: Boolean = true
) : Parcelable {

    companion object {
        fun fromRecentFile(
            recentFile: com.PRO.propdf.data.entities.RecentFile,
            pdfItem: com.PRO.propdf.data.entities.PdfItem? = null,
            folderName: String? = null
        ): RecentModel {
            val pdfName = pdfItem?.name ?: "Unknown PDF"
            val pdfPath = pdfItem?.path ?: ""
            val totalPages = pdfItem?.pageCount ?: 0
            
            return RecentModel(
                id = recentFile.id,
                pdfId = recentFile.pdfId,
                pdfName = pdfName,
                pdfPath = pdfPath,
                lastOpened = recentFile.lastOpened,
                lastOpenedFormatted = formatDate(recentFile.lastOpened),
                lastOpenedPage = recentFile.lastOpenedPage,
                totalPages = totalPages,
                folderName = folderName,
                thumbnailPath = pdfItem?.thumbnailPath,
                isPdfExists = pdfItem != null
            )
        }

        private fun formatDate(date: Date): String {
            val now = Date()
            val diff = now.time - date.time
            
            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
                else -> java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
            }
        }
    }

    val pageProgress: String
        get() = when {
            totalPages == 0 -> "Page $lastOpenedPage"
            else -> "Page $lastOpenedPage of $totalPages"
        }

    val progressPercentage: Float
        get() = when {
            totalPages == 0 -> 0f
            else -> (lastOpenedPage.toFloat() / totalPages.toFloat()) * 100f
        }

    fun withUri(uri: Uri): RecentModel {
        return this.copy(pdfUri = uri)
    }

    fun withFolderName(folderName: String): RecentModel {
        return this.copy(folderName = folderName)
    }
}