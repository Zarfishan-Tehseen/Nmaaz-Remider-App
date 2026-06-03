package com.example.nmaazreminder.ui.fragments.home

data class PrayerCountdownState(
    val currentPrayerName: String,     // e.g., "Zuhr", "Zuhr Qaza", "Asr (Makrooh)"
    val arabicPrayerName: String,      // e.g., "الظهر", "العصر"
    val formattedDisplayTime: String,  // Target prayer shuru hone ka time text form mein (e.g., "12:43 PM")
    val nextPrayerDetails: String,     // Subtitle text (e.g., "Next: Asr at 5:03 PM")
    val targetTimeInMillis: Long,      // Epoch millisecond timestamp jahan tak CountdownTimer chalega
    val isMakrooh: Boolean = false     // Flag for triggering special warning styling layers dynamically
)