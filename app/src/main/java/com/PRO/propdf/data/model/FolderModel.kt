package com.PRO.propdf.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class FolderModel(
    val id: Long = 0,
    val name: String,
    val parentFolderId: Long? = null,
    val parentFolderName: String? = null,
    val createdAt: Date,
    val pdfCount: Int = 0,
    val subFolderCount: Int = 0,
    val totalItems: Int = 0,
    val color: Int? = null,
    val icon: String? = null,
    val isSelected: Boolean = false,
    val hierarchyPath: String = "",
    val canBeDeleted: Boolean = true
) : Parcelable {

    companion object {
        fun fromFolder(
            folder: com.PRO.propdf.data.entities.Folder,
            pdfCount: Int = 0,
            subFolderCount: Int = 0
        ): FolderModel {
            return FolderModel(
                id = folder.id,
                name = folder.name,
                parentFolderId = folder.parentFolderId,
                createdAt = folder.createdAt,
                pdfCount = pdfCount,
                subFolderCount = subFolderCount,
                totalItems = pdfCount + subFolderCount,
                color = folder.color,
                icon = folder.icon,
                canBeDeleted = pdfCount == 0 && subFolderCount == 0
            )
        }

        fun createRootFolder(): FolderModel {
            return FolderModel(
                id = -1,
                name = "Root",
                createdAt = Date(),
                canBeDeleted = false
            )
        }
    }

    fun withSelection(selected: Boolean): FolderModel {
        return this.copy(isSelected = selected)
    }

    fun withParentName(parentName: String?): FolderModel {
        return this.copy(parentFolderName = parentName)
    }

    fun withHierarchyPath(path: String): FolderModel {
        return this.copy(hierarchyPath = path)
    }

    fun withItemCounts(pdfCount: Int, subFolderCount: Int): FolderModel {
        return this.copy(
            pdfCount = pdfCount,
            subFolderCount = subFolderCount,
            totalItems = pdfCount + subFolderCount,
            canBeDeleted = pdfCount == 0 && subFolderCount == 0
        )
    }

    val displayItemCount: String
        get() = when {
            totalItems == 0 -> "Empty"
            totalItems == 1 -> "1 item"
            else -> "$totalItems items"
        }

    val hasContents: Boolean
        get() = totalItems > 0
}