package com.example.nmaazreminder.ui.fragments.setting.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemPrayerNotificationCardBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class PrayerNotificationAdapter(
    private val dataset: List<PrayerNotificationItem>,
    private val onCardClicked: (PrayerNotificationItem) -> Unit
) : RecyclerView.Adapter<PrayerNotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val itemBinding: ItemPrayerNotificationCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemPrayerNotificationCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = dataset[position]
        holder.itemBinding.apply {
            tvPrayerName.text = item.name
            tvPrayerArabic.text = item.arabicName
            tvNotificationStatus.text = item.statusText
            ivPrayerConditionIcon.setImageResource(item.iconDrawableId)

            rootCardLayout.setBounceClickListener {
                onCardClicked(item)
            }
        }
    }

    override fun getItemCount() = dataset.size
}