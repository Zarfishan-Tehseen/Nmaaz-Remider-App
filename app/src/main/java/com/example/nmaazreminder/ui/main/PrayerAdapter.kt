package com.example.nmaazreminder.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemPrayerBinding
class PrayerAdapter(private val onNotificationClick: (PrayerItem) -> Unit) :
    ListAdapter<PrayerItem, PrayerAdapter.PrayerViewHolder>(DiffCallback) {

    class PrayerViewHolder(private val binding: ItemPrayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerItem, onNotificationClick: (PrayerItem) -> Unit) {
            binding.tvPrayerName.text = item.name
            binding.tvPrayerTime.text = item.time

            binding.root.setOnClickListener {
                onNotificationClick(item)
            }

            binding.btnNotif.setOnClickListener {
                // You can add separate logic here later if needed
                // For now, it will trigger the same navigation logic
                onNotificationClick(item)
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