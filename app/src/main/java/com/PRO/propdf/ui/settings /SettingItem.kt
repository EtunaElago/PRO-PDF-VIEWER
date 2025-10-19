package com.PRO.propdf.ui.settings

data class SettingItem(
    val id: SettingItemId,
    val title: String,
    val description: String,
    val iconRes: Int,
    val type: SettingType = SettingType.NORMAL
)

enum class SettingItemId {
    LANGUAGE,
    THEME,
    VIEW_MODE,
    BIN,
    ABOUT,
    PRIVACY_POLICY,
    TERMS_OF_SERVICE,
    SHARE_APP,
    CONTACT_US
}

enum class SettingType {
    NORMAL,
    SWITCH
}