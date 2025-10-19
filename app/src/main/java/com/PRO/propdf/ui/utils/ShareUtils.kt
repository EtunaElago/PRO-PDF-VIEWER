package com.PRO.propdf.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.PRO.propdf.R

object ShareUtils {

    fun shareApp(context: Context) {
        val shareText = context.getString(R.string.share_app_text, context.getString(R.string.app_name))
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_app)))
    }

    fun contactUs(context: Context, email: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.contact_us)))
    }

    fun sharePdf(context: Context, pdfUri: Uri, pdfName: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, pdfName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun rateApp(context: Context) {
        try {
            val packageName = context.packageName
            val playStoreUrl = "market://details?id=$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web URL if Play Store app is not installed
            val packageName = context.packageName
            val playStoreWebUrl = "https://play.google.com/store/apps/details?id=$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreWebUrl))
            context.startActivity(intent)
        }
    }

    fun sendFeedback(context: Context, email: String) {
        val deviceInfo = """
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android Version: ${android.os.Build.VERSION.RELEASE}
            App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}
        """.trimIndent()

        val body = """
            $deviceInfo
            
            Please share your feedback below:
            
            
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback - ${context.getString(R.string.app_name)}")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Feedback"))
    }
}