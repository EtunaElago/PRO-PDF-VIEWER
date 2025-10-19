package com.PRO.propdf.utils

import java.text.DecimalFormat

object FileSizeFormatter {

    private val decimalFormat = DecimalFormat("#.##")

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> {
                val size = bytes.toDouble() / (1024 * 1024 * 1024)
                "${decimalFormat.format(size)} GB"
            }
            bytes >= 1024 * 1024 -> {
                val size = bytes.toDouble() / (1024 * 1024)
                "${decimalFormat.format(size)} MB"
            }
            bytes >= 1024 -> {
                val size = bytes.toDouble() / 1024
                "${decimalFormat.format(size)} KB"
            }
            else -> {
                "$bytes B"
            }
        }
    }

    fun formatBytesWithUnit(bytes: Long): Pair<String, String> {
        return when {
            bytes >= 1024 * 1024 * 1024 -> {
                val size = bytes.toDouble() / (1024 * 1024 * 1024)
                Pair(decimalFormat.format(size), "GB")
            }
            bytes >= 1024 * 1024 -> {
                val size = bytes.toDouble() / (1024 * 1024)
                Pair(decimalFormat.format(size), "MB")
            }
            bytes >= 1024 -> {
                val size = bytes.toDouble() / 1024
                Pair(decimalFormat.format(size), "KB")
            }
            else -> {
                Pair(bytes.toString(), "B")
            }
        }
    }

    fun parseFileSize(sizeString: String): Long {
        return try {
            val parts = sizeString.split(" ")
            if (parts.size != 2) return 0L
            
            val value = parts[0].toDouble()
            val unit = parts[1].uppercase()
            
            when (unit) {
                "GB" -> (value * 1024 * 1024 * 1024).toLong()
                "MB" -> (value * 1024 * 1024).toLong()
                "KB" -> (value * 1024).toLong()
                "B" -> value.toLong()
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun isSizeWithinLimit(size: Long, maxSize: Long): Boolean {
        return size <= maxSize
    }

    fun getReadableMaxFileSize(maxSize: Long): String {
        return formatBytes(maxSize)
    }
}