package com.qx.orbit.bili.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ScreenMode(val value: Int) {
    AUTO(0), ROUND(1), SQUARE(2)
}

object AppConfig {
    private const val PREFS_NAME = "orbit_config"
    private const val KEY_SCREEN_MODE = "screen_mode"

    var screenMode by mutableStateOf(ScreenMode.AUTO)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_SCREEN_MODE, ScreenMode.AUTO.value)
        screenMode = ScreenMode.entries.find { it.value == saved } ?: ScreenMode.AUTO
    }

    fun saveScreenMode(context: Context, mode: ScreenMode) {
        screenMode = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_SCREEN_MODE, mode.value)
            apply()
        }
    }
}
