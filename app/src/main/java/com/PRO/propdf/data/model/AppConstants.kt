package com.PRO.propdf.data.model

object AppConstants {
    
    // Database Constants
    const val DATABASE_NAME = "pro_pdf_database"
    const val DATABASE_VERSION = 1
    
    // Ad Constants
    const val BANNER_AD_REFRESH_RATE = 30000L // 30 seconds
    const val INTERSTITIAL_AD_FREQUENCY = 3 // Show ad every 3rd action
    
    // File Constants
    const val PDF_MIME_TYPE = "application/pdf"
    const val MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
    const val THUMBNAIL_WIDTH = 150
    const val THUMBNAIL_HEIGHT = 200
    
    // Navigation Constants
    const val ARG_PDF_ID = "pdf_id"
    const val ARG_FOLDER_ID = "folder_id"
    const val ARG_PDF_PATH = "pdf_path"
    const val ARG_PDF_NAME = "pdf_name"
    const val ARG_FOLDER_NAME = "folder_name"
    const val ARG_IS_FROM_RECENTS = "is_from_recents"
    
    // Shared Preferences Keys
    const val PREFS_NAME = "pro_pdf_prefs"
    const val KEY_THEME = "app_theme"
    const val KEY_VIEW_MODE = "view_mode"
    const val KEY_SORT_MODE = "sort_mode"
    const val KEY_SORT_ORDER = "sort_order"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_LANGUAGE = "app_language"
    
    // Sort Options
    const val SORT_BY_NAME = "name"
    const val SORT_BY_DATE = "date"
    const val SORT_BY_SIZE = "size"
    const val SORT_BY_LAST_OPENED = "last_opened"
    
    // Sort Order
    const val ORDER_ASCENDING = "ascending"
    const val ORDER_DESCENDING = "descending"
    
    // View Modes
    const val VIEW_MODE_GRID = "grid"
    const val VIEW_MODE_LIST = "list"
    
    // Theme Modes
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    // Request Codes
    const val REQUEST_CODE_PICK_PDF = 1001
    const val REQUEST_CODE_STORAGE_PERMISSION = 1002
    const val REQUEST_CODE_CREATE_FOLDER = 1003
    const val REQUEST_CODE_OPEN_DOCUMENT = 1004
    
    // Ad Unit IDs (Replace with actual IDs)
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294" // Test ID
    
    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // File Operations
    const val OPERATION_COPY = "copy"
    const val OPERATION_MOVE = "move"
    
    // Error Messages
    const val ERROR_FILE_NOT_FOUND = "PDF file not found"
    const val ERROR_FILE_TOO_LARGE = "File is too large"
    const val ERROR_INVALID_PDF = "Invalid PDF file"
    const val ERROR_PERMISSION_DENIED = "Storage permission required"
    const val ERROR_FOLDER_EXISTS = "Folder already exists"
    const val ERROR_FOLDER_NOT_EMPTY = "Folder is not empty"
    
    // Success Messages
    const val SUCCESS_PDF_ADDED = "PDF added successfully"
    const val SUCCESS_FOLDER_CREATED = "Folder created successfully"
    const val SUCCESS_FILE_DELETED = "File moved to bin"
    const val SUCCESS_FILE_RESTORED = "File restored successfully"
    
    // Default Values
    const val DEFAULT_PAGE_NUMBER = 1
    const val DEFAULT_ZOOM_SCALE = 1.0f
    const val MAX_RECENT_FILES = 50
    const val RECENT_CLEANUP_DAYS = 30
}