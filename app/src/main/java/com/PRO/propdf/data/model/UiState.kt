package com.PRO.propdf.data.model

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

data class SortConfig(
    val sortBy: String = AppConstants.SORT_BY_NAME,
    val sortOrder: String = AppConstants.ORDER_ASCENDING
)

data class AppConfig(
    val theme: String = AppConstants.THEME_SYSTEM,
    val viewMode: String = AppConstants.VIEW_MODE_GRID,
    val sortConfig: SortConfig = SortConfig(),
    val language: String = "system"
)