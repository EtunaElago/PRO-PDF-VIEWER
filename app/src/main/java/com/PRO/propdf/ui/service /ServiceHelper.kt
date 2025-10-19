package com.PRO.propdf.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat

object ServiceHelper {

    fun startThumbnailGeneration(context: Context, pdfId: Long, pdfUri: Uri) {
        val intent = Intent(context, PdfThumbnailService::class.java).apply {
            action = PdfThumbnailService.ACTION_GENERATE_THUMBNAIL
            putExtra(PdfThumbnailService.EXTRA_PDF_ID, pdfId)
            putExtra(PdfThumbnailService.EXTRA_PDF_URI, pdfUri)
        }
        
        startService(context, intent)
    }

    fun startBatchThumbnailGeneration(context: Context) {
        val intent = Intent(context, PdfThumbnailService::class.java).apply {
            action = PdfThumbnailService.ACTION_GENERATE_ALL_THUMBNAILS
        }
        
        startService(context, intent)
    }

    fun startThumbnailCleanup(context: Context) {
        val intent = Intent(context, PdfThumbnailService::class.java).apply {
            action = PdfThumbnailService.ACTION_CLEANUP_THUMBNAILS
        }
        
        startService(context, intent)
    }

    private fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopThumbnailService(context: Context) {
        val intent = Intent(context, PdfThumbnailService::class.java)
        context.stopService(intent)
    }

    fun isThumbnailServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        return manager?.getRunningServices(Integer.MAX_VALUE)?.any { service ->
            service.service.className == PdfThumbnailService::class.java.name
        } ?: false
    }
}