package com.PRO.propdf.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PdfModel(
    val id: Long = 0,
    val name: String,
    val path: String,
    val uri: Uri? = null,
    val size: Long,
    val sizeFormatted: String,
    val folderId: Long? = null,
    val folderName: String? = null,
    val createdAt: Date,
    val modifiedAt: Date,
    val pageCount: Int = 0,
    val lastOpenedPage: Int = 0,
    val isInBin: Boolean = false,
    val thumbnailPath: String? = null,
    val isSelected: Boolean = false
) : Parcelable {

    companion object {
        fun fromPdfItem(pdfItem: com.PRO.propdf.data.entities.PdfItem): PdfModel {
            return PdfModel(
                id = pdfItem.id,
                name = pdfItem.name,
                path = pdfItem.path,
                size = pdfItem.size,
                sizeFormatted = formatFileSize(pdfItem.size),
                folderId = pdfItem.folderId,
                createdAt = pdfItem.createdAt,
                modifiedAt = pdfItem.modifiedAt,
                pageCount = pdfItem.pageCount,
                lastOpenedPage = pdfItem.lastOpenedPage,
                isInBin = pdfItem.isInBin,
                thumbnailPath = pdfItem.thumbnailPath
            )
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size >= 1024 * 1024 * 1024 -> {
                    String.format("%.1f GB", size.toDouble() / (1024 * 1024 * 1024))
                }
                size >= 1024 * 1024 -> {
                    String.format("%.1f MB", size.toDouble() / (1024 * 1024))
                }
                size >= 1024 -> {
                    String.format("%.1f KB", size.toDouble() / 1024)
                }
                else -> {
                    String.format("%d B", size)
                }
            }
        }
    }

    fun withSelection(selected: Boolean): PdfModel {
        return this.copy(isSelected = selected)
    }

    fun withFolderName(folderName: String?): PdfModel {
        return this.copy(folderName = folderName)
    }

    fun withUri(uri: Uri): PdfModel {
        return this.copy(uri = uri)
    }
}