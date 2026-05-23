package com.example.nmaazreminder.ui.fragments.setting.language

data class LanguageItem(
    val id: Int,
    val languageCode: String, // e.g., "en", "en_ar", "ar", "ur"
    val title: String,
    val subtitle: String?,
    val scriptSample: String, // The right-aligned preview text like "الفجر"
    val isLastInGroup: Boolean = false
)