package com.PRO.propdf.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private const val DATE_FORMAT_FULL = "MMM dd, yyyy 'at' hh:mm a"
    private const val DATE_FORMAT_SHORT = "MMM dd, yyyy"
    private const val DATE_FORMAT_TIME = "hh:mm a"

    fun formatDateFull(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT_FULL, Locale.getDefault()).format(date)
    }

    fun formatDateShort(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT_SHORT, Locale.getDefault()).format(date)
    }

    fun formatTime(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT_TIME, Locale.getDefault()).format(date)
    }

    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "min" else "mins"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> formatDateShort(date)
        }
    }

    fun formatDuration(milliseconds: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun isToday(date: Date): Boolean {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(date) == dateFormat.format(today)
    }

    fun isYesterday(date: Date): Boolean {
        val yesterday = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(date) == dateFormat.format(yesterday)
    }

    fun getStartOfDay(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfDay(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.time
    }
}