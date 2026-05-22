package com.example.nmaazreminder.ui.fragments.setting

data class AdhanSoundItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isSilentOption: Boolean = false,
    var isPlayingPreview: Boolean = false
)