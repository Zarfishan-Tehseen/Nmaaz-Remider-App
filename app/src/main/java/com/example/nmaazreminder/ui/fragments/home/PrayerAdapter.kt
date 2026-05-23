package com.example.nmaazreminder.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemPrayerBinding
import com.example.nmaazreminder.ui.fragments.home.PrayerItem
import com.example.nmaazreminder.utils.setBounceClickListener

class PrayerAdapter(private val onNotificationClick: (PrayerItem) -> Unit) :
    ListAdapter<PrayerItem, PrayerAdapter.PrayerViewHolder>(DiffCallback) {

    class PrayerViewHolder(private val binding: ItemPrayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerItem, onNotificationClick: (PrayerItem) -> Unit) {
            binding.tvPrayerNameEnglish.text = item.name
            binding.tvPrayerTime.text = item.time

            val (arabicName, iconRes) = when (item.name.lowercase().trim()) {
                "fajr" -> "الفجر" to R.drawable.ic_moon
                "sunrise" -> "الشروق" to R.drawable.ic_sun
                "dhuhr" -> "الظهر" to R.drawable.ic_sun
                "asr" -> "العصر" to R.drawable.ic_sun
                "maghrib" -> "المغرب" to R.drawable.ic_cloud
                "isha" -> "العشاء" to R.drawable.ic_moon
                else -> "" to R.drawable.ic_sun_cloud_accent
            }

            binding.tvPrayerNameArabic.text = arabicName
            binding.ivPrayerIcon.setImageResource(iconRes)

            // 🛑 CRITICAL: Clear root view listener to prevent item touch cross-firing during recycling
            binding.root.setOnClickListener(null)
            binding.root.isClickable = false

            // 🎯 Handle Click and Bounce logic on the inner RelativeLayout container
            if (item.name.lowercase().trim() == "sunrise") {
                // Keep Sunrise unclickable and static
                binding.layoutClickableRow.setOnTouchListener(null) // Safe layout clear for bounce extensions
                binding.layoutClickableRow.setOnClickListener(null)
                binding.layoutClickableRow.isClickable = false
                binding.layoutClickableRow.isFocusable = false
                binding.layoutClickableRow.background = null
            } else {
                binding.layoutClickableRow.isClickable = true
                binding.layoutClickableRow.isFocusable = true

                // Reassign background shape setup before applying premium touch interceptors
                binding.layoutClickableRow.setBackgroundResource(R.drawable.bg_row_ripple)

                // ✨ SWAPPED: Applied the premium scale-down bounce click effect to active rows
                binding.layoutClickableRow.setBounceClickListener {
                    onNotificationClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        return PrayerViewHolder(
            ItemPrayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        holder.bind(getItem(position), onNotificationClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<PrayerItem>() {
        override fun areItemsTheSame(old: PrayerItem, new: PrayerItem) = old.name == new.name
        override fun areContentsTheSame(old: PrayerItem, new: PrayerItem) = old == new
    }
}