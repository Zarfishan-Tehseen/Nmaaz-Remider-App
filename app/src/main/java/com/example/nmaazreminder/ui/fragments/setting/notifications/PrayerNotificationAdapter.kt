package com.example.nmaazreminder.ui.fragments.setting.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemPrayerNotificationCardBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class PrayerNotificationAdapter(
    private val onCardClicked: (PrayerNotificationItem) -> Unit
) : ListAdapter<PrayerNotificationItem, PrayerNotificationAdapter.NotificationViewHolder>(DiffCallback) {

    class NotificationViewHolder(private val itemBinding: ItemPrayerNotificationCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: PrayerNotificationItem, onCardClicked: (PrayerNotificationItem) -> Unit) {
            itemBinding.apply {
                // 🌟 Using your exact naming schema properties here
                tvPrayerName.text = item.name
                tvPrayerArabic.text = item.arabicName
                tvNotificationStatus.text = item.statusText
                ivPrayerConditionIcon.setImageResource(item.iconDrawableId)

                // Optional: Visually dim the row card slightly if it is disabled/muted
                rootCardLayout.alpha = if (item.isEnabled) 1.0f else 0.5f

                rootCardLayout.setBounceClickListener {
                    onCardClicked(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemPrayerNotificationCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position), onCardClicked)
    }

    object DiffCallback : DiffUtil.ItemCallback<PrayerNotificationItem>() {
        override fun areItemsTheSame(old: PrayerNotificationItem, new: PrayerNotificationItem): Boolean {
            return old.name == new.name
        }

        override fun areContentsTheSame(old: PrayerNotificationItem, new: PrayerNotificationItem): Boolean {
            return old == new
        }
    }
}