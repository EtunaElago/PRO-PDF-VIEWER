package com.PRO.propdf.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.PRO.propdf.data.model.AppConstants

object ThemeUtils {

    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    fun applyTheme(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getCurrentTheme(context: Context): String {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> THEME_LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> THEME_DARK
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> THEME_SYSTEM
            else -> THEME_SYSTEM
        }
    }

    fun saveThemePreference(context: Context, theme: String) {
        val sharedPref = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(AppConstants.KEY_THEME, theme)
            apply()
        }
    }

    fun getSavedTheme(context: Context): String {
        val sharedPref = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(AppConstants.KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun initializeTheme(context: Context) {
        val savedTheme = getSavedTheme(context)
        applyTheme(savedTheme)
    }

    fun isDarkTheme(context: Context): Boolean {
        return when (getCurrentTheme(context)) {
            THEME_DARK -> true
            THEME_SYSTEM -> isSystemInDarkTheme(context)
            else -> false
        }
    }

    private fun isSystemInDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    fun getAttributeColor(context: Context, attrId: Int): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attrId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    fun getColorWithAlpha(color: Int, alpha: Float): Int {
        val alphaValue = (alpha * 255).toInt()
        return (alphaValue shl 24) or (color and 0x00FFFFFF)
    }
}