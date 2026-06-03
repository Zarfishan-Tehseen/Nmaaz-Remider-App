package com.example.nmaazreminder.ui.fragments.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemPrayerBinding
import com.example.nmaazreminder.databinding.ItemPrayerColumnBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class PrayerAdapter(
    private val viewTypeMode: Int, // 🌟 0 = Row, 1 = Dial Grid, 2 = Arch Grid
    private val onNotificationClick: (PrayerItem) -> Unit
) : ListAdapter<PrayerItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return viewTypeMode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val binding = ItemPrayerBinding.inflate(inflater, parent, false)
            RowViewHolder(binding)
        } else {
            // 💡 Dial (1) aur Arch (2) dono isi single column XML ko inflate karenge
            val binding = ItemPrayerColumnBinding.inflate(inflater, parent, false)
            ColumnViewHolder(binding, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is RowViewHolder) {
            holder.bind(item, onNotificationClick)
        } else if (holder is ColumnViewHolder) {
            holder.bind(item, onNotificationClick)
        }
    }

    // 🔥 1. VIEW HOLDER FOR THE HORIZONTAL LIST VIEW (Row Style)
    class RowViewHolder(private val binding: ItemPrayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerItem, onNotificationClick: (PrayerItem) -> Unit) {
            binding.tvPrayerNameEnglish.text = item.name
            binding.tvPrayerTime.text = item.time

            val (arabicName, iconRes) = getIconAndArabicName(item.name)

            binding.tvPrayerNameArabic.text = arabicName
            binding.ivPrayerIcon.setImageResource(iconRes)

            binding.root.setOnClickListener(null)
            binding.root.isClickable = false

            if (item.name.lowercase().trim() == "sunrise") {
                binding.layoutClickableRow.setOnTouchListener(null)
                binding.layoutClickableRow.setOnClickListener(null)
                binding.layoutClickableRow.isClickable = false
                binding.layoutClickableRow.background = null
            } else {
                binding.layoutClickableRow.isClickable = true
                binding.layoutClickableRow.isFocusable = true
                binding.layoutClickableRow.setBackgroundResource(R.drawable.bg_row_ripple)
                binding.layoutClickableRow.setBounceClickListener {
                    onNotificationClick(item)
                }
            }
        }
    }

    // 🔥 2. VIEW HOLDER FOR THE DIAL & ARCH LAYOUTS (Column Style)
    class ColumnViewHolder(
        private val binding: ItemPrayerColumnBinding,
        private val currentMode: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerItem, onNotificationClick: (PrayerItem) -> Unit) {
            binding.tvPrayerNameEnglish.text = item.name
            binding.tvPrayerTime.text = item.time

            // 🎨 DYNAMIC THEMING LOGIC BASED ON MODE
            if (currentMode == 2) {
                // 🟢 ARCH LAYOUT CONFIGURATION (White Theme)
                binding.tvPrayerNameEnglish.setTextColor(Color.WHITE)
                binding.tvPrayerTime.setTextColor(Color.parseColor("#B3FFFFFF")) // 70% Alpha White

                if (item.name.lowercase().trim() == "sunrise") {
                    binding.rowContainerColumn.setOnTouchListener(null)
                    binding.rowContainerColumn.setOnClickListener(null)
                    binding.rowContainerColumn.isClickable = false
                    binding.rowContainerColumn.background = null
                } else {
                    binding.rowContainerColumn.isClickable = true
                    binding.rowContainerColumn.isFocusable = true

                    // Reference image jaisa custom dynamic green cell background color bitha diya
                    binding.rowContainerColumn.setBackgroundResource(R.drawable.bg_arch_item_selector)
                    binding.rowContainerColumn.setBounceClickListener {
                        onNotificationClick(item)
                    }
                }
            } else {
                // ⚪ DIAL LAYOUT CONFIGURATION (Original Cream Theme)
                binding.tvPrayerNameEnglish.setTextColor(Color.parseColor("#333333"))
                binding.tvPrayerTime.setTextColor(Color.parseColor("#6E726E"))

                if (item.name.lowercase().trim() == "sunrise") {
                    binding.rowContainerColumn.setOnTouchListener(null)
                    binding.rowContainerColumn.setOnClickListener(null)
                    binding.rowContainerColumn.isClickable = false
                    binding.rowContainerColumn.background = null
                } else {
                    binding.rowContainerColumn.isClickable = true
                    binding.rowContainerColumn.isFocusable = true
                    binding.rowContainerColumn.setBackgroundResource(R.drawable.bg_row_ripple)
                    binding.rowContainerColumn.setBounceClickListener {
                        onNotificationClick(item)
                    }
                }
            }
        }
    }

    companion object {
        private fun getIconAndArabicName(name: String): Pair<String, Int> {
            return when (name.lowercase().trim()) {
                "fajr" -> "الفجر" to R.drawable.ic_moon
                "sunrise" -> "الشروق" to R.drawable.ic_sun
                "dhuhr" -> "الظهر" to R.drawable.ic_sun
                "asr" -> "العصر" to R.drawable.ic_sun
                "maghrib" -> "المغرب" to R.drawable.ic_cloud
                "isha" -> "العشاء" to R.drawable.ic_moon
                else -> "" to R.drawable.ic_sun_cloud_accent
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<PrayerItem>() {
        override fun areItemsTheSame(old: PrayerItem, new: PrayerItem) = old.name == new.name
        override fun areContentsTheSame(old: PrayerItem, new: PrayerItem) = old == new
    }
}