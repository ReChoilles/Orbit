package com.qx.orbit.bili.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatBiliTime(timestampSeconds: Long): String {
    val timestampMillis = timestampSeconds * 1000
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val current = Calendar.getInstance()
    
    val diff = current.timeInMillis - target.timeInMillis
    
    val currentYear = current.get(Calendar.YEAR)
    val targetYear = target.get(Calendar.YEAR)
    
    val currentDay = current.get(Calendar.DAY_OF_YEAR)
    val targetDay = target.get(Calendar.DAY_OF_YEAR)
    
    val diffMinutes = diff / (60 * 1000)
    val diffHours = diff / (60 * 60 * 1000)
    
    return if (currentYear == targetYear) {
        if (currentDay == targetDay) {
            when {
                diffMinutes < 1 -> "刚刚"
                diffMinutes < 60 -> "${diffMinutes}分钟前"
                else -> "${diffHours}小时前"
            }
        } else if (currentDay - targetDay == 1) {
            "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(target.time)
        } else if (currentDay - targetDay > 1 && currentDay - targetDay <= 7) {
            "${currentDay - targetDay}天前"
        } else {
            SimpleDateFormat("M月d日", Locale.getDefault()).format(target.time)
        }
    } else {
        SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(target.time)
    }
}
