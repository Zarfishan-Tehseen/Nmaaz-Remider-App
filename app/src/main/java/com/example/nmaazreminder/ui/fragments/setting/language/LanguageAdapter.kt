package com.example.nmaazreminder.ui.fragments.setting.language

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemLanguageRowBinding
import com.example.nmaazreminder.utils.setBounceClickListener
import com.example.nmaazreminder.utils.setRoundedCorners
import com.example.nmaazreminder.utils.toPx

class LanguageAdapter(
    private val items: List<LanguageItem>,
    private var selectedPosition: Int,
    private val onLanguageSelected: (LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) =
        holder.bind(items[position], position == selectedPosition)

    override fun getItemCount(): Int = items.size

    inner class LanguageViewHolder(private val binding: ItemLanguageRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem, isSelected: Boolean) {
            binding.tvLanguageTitle.text = item.title
            binding.tvScriptSample.text = item.scriptSample

            // Handle optional subtitle field safely
            if (!item.subtitle.isNullOrEmpty()) {
                binding.tvLanguageSubtitle.visibility = View.VISIBLE
                binding.tvLanguageSubtitle.text = item.subtitle
            } else {
                binding.tvLanguageSubtitle.visibility = View.GONE
            }

            // Sync structural Radio Indicator graphic asset
            if (isSelected) {
                binding.viewRadioIndicator.setBackgroundResource(R.drawable.ic_radio_selected)
            } else {
                binding.viewRadioIndicator.setBackgroundResource(R.drawable.ic_radio_unselected)
            }

            // Sync separator rules
            binding.languageDivider.visibility = if (item.isLastInGroup) View.GONE else View.VISIBLE

            // Apply card grouping canvas corner round masks dynamically
            val radius = 16.toPx
            val whiteBg = Color.WHITE
            val isFirstInGroup = (adapterPosition == 0)

            when {
                isFirstInGroup && item.isLastInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, radius, radius, radius, radius)
                }
                isFirstInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, topLeft = radius, topRight = radius)
                }
                item.isLastInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, bottomLeft = radius, bottomRight = radius)
                }
                else -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, 0f, 0f, 0f, 0f)
                }
            }

            // ✨ SWAPPED: Replaced standard click listener with our dynamic scale-down bounce click micro-interaction
            binding.cardRowContainer.setBounceClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION && selectedPosition != currentPos) {
                    val prevPos = selectedPosition
                    selectedPosition = currentPos
                    notifyItemChanged(prevPos)
                    notifyItemChanged(selectedPosition)
                    onLanguageSelected(item)
                }
            }
        }
    }
}