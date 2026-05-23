package com.example.nmaazreminder.ui.fragments.setting.adhansound

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemAdhanSoundCardBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class AdhanSoundAdapter(
    private val items: List<AdhanSoundItem>,
    private var selectedPosition: Int,
    private val onPlayStateChanged: (AdhanSoundItem, Boolean) -> Unit
) : RecyclerView.Adapter<AdhanSoundAdapter.SoundViewHolder>() {

    fun getSelectedTrack(): AdhanSoundItem = items[selectedPosition]

    inner class SoundViewHolder(val binding: ItemAdhanSoundCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemAdhanSoundCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val currentItem = items[position]
        val isSelected = (position == selectedPosition)

        holder.binding.apply {
            tvSoundTitle.text = currentItem.title
            tvSoundSubtitle.text = currentItem.subtitle

            // 1. Handle selection states (Text Colors & Icon Colors matching the GIF)
            if (isSelected) {
                layoutInnerContainer.background = ContextCompat.getDrawable(rootCardView.context, R.drawable.bg_card_selected_border)
                viewIconBg.setBackgroundResource(R.drawable.bg_pill_active) // Dark solid green circle
                ivSoundTypeIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)

                tvSoundTitle.setTextColor(Color.parseColor("#143522"))
                btnPlayPause.imageTintList = ColorStateList.valueOf(Color.parseColor("#143522"))
            } else {
                layoutInnerContainer.background = null
                viewIconBg.setBackgroundResource(R.drawable.bg_rounded_bell_tint) // Soft pastel green tint circle
                ivSoundTypeIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#143522"))

                tvSoundTitle.setTextColor(Color.parseColor("#111111"))
                btnPlayPause.imageTintList = ColorStateList.valueOf(Color.parseColor("#143522"))
            }

            // 2. Differentiate structural asset vector icons (Silent option vs Mosque)
            if (currentItem.isSilentOption) {
                ivSoundTypeIcon.setImageResource(R.drawable.ic_vibrate)
            } else {
                ivSoundTypeIcon.setImageResource(R.drawable.ic_mosque32)
            }

            // 3. Sync Play/Pause Icon asset toggle based on preview status
            val playIcon = if (currentItem.isPlayingPreview) R.drawable.ic_pause else R.drawable.ic_preview
            btnPlayPause.setImageResource(playIcon)

            // ✨ ACTION A: SWAPPED to use your premium scale-down bounce click effect on row selection
            rootCardView.setBounceClickListener {
                val currentAdapterPosition = holder.adapterPosition
                if (currentAdapterPosition != RecyclerView.NO_POSITION && selectedPosition != currentAdapterPosition) {
                    val oldPos = selectedPosition
                    selectedPosition = currentAdapterPosition

                    notifyItemChanged(oldPos)
                    notifyItemChanged(selectedPosition)
                }
            }

            // ACTION B: Clicking the play/pause circular button explicitly triggers audio tracking logic (Kept isolated from scale bounce)
            btnPlayPause.setOnClickListener {
                val currentAdapterPosition = holder.adapterPosition
                if (currentAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                // Loop through and cleanly clear play states on any other item previously left playing
                items.forEachIndexed { index, item ->
                    if (index != currentAdapterPosition && item.isPlayingPreview) {
                        item.isPlayingPreview = false
                        notifyItemChanged(index)
                        onPlayStateChanged(item, false)
                    }
                }

                // Toggle state for current item
                currentItem.isPlayingPreview = !currentItem.isPlayingPreview
                notifyItemChanged(currentAdapterPosition)
                onPlayStateChanged(currentItem, currentItem.isPlayingPreview)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}