package com.PRO.propdf.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.PRO.propdf.data.model.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {

    private const val TAG = "FileUtils"

    suspend fun getPdfPageCount(context: Context, pdfUri: Uri): Int = withContext(Dispatchers.IO) {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            parcelFileDescriptor?.let { pfd ->
                pdfRenderer = PdfRenderer(pfd)
                return@withContext pdfRenderer!!.pageCount
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting PDF page count", e)
        } finally {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
        return@withContext 0
    }

    suspend fun generatePdfThumbnail(context: Context, pdfUri: Uri, pdfId: Long): String? = withContext(Dispatchers.IO) {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            parcelFileDescriptor?.let { pfd ->
                pdfRenderer = PdfRenderer(pfd)
                
                // Open the first page
                val page = pdfRenderer.openPage(0)
                
                // Create bitmap for thumbnail
                val bitmap = Bitmap.createBitmap(
                    AppConstants.THUMBNAIL_WIDTH,
                    AppConstants.THUMBNAIL_HEIGHT,
                    Bitmap.Config.ARGB_8888
                )
                
                // Render the page to bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                // Save thumbnail to internal storage
                val thumbnailFile = File(context.cacheDir, "thumbnail_$pdfId.png")
                val outputStream = FileOutputStream(thumbnailFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
                
                page.close()
                
                return@withContext thumbnailFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF thumbnail", e)
        } finally {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
        return@withContext null
    }

    fun loadBitmapFromPath(filePath: String?): Bitmap? {
        if (filePath.isNullOrEmpty()) return null
        
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from path: $filePath", e)
            null
        }
    }

    fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    it.getLong(sizeIndex)
                } else {
                    0L
                }
            } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size", e)
            0L
        }
    }

    fun getFileName(uri: Uri, context: Context): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    it.getString(nameIndex) ?: "Unknown PDF"
                } else {
                    "Unknown PDF"
                }
            } ?: "Unknown PDF"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name", e)
            "Unknown PDF"
        }
    }

    fun isValidPdfFile(uri: Uri, context: Context): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating PDF file", e)
            false
        }
    }

    fun isFileTooLarge(size: Long): Boolean {
        return size > AppConstants.MAX_FILE_SIZE_BYTES
    }

    fun formatFileSize(size: Long): String {
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

    fun deleteThumbnail(context: Context, thumbnailPath: String?) {
        if (thumbnailPath.isNullOrEmpty()) return
        
        try {
            val thumbnailFile = File(thumbnailPath)
            if (thumbnailFile.exists()) {
                thumbnailFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting thumbnail", e)
        }
    }

    fun copyFileToAppDirectory(context: Context, sourceUri: Uri, destinationFileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val outputDir = File(context.filesDir, "pdfs")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val outputFile = File(outputDir, destinationFileName)
            val outputStream = FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file to app directory", e)
            null
        }
    }
}