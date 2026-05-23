package com.example.nmaazreminder.ui.fragments.setting.homestyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemHomeStyleCardBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class HomeStyleAdapter(
    private val items: List<HomeStyleItem>,
    private var selectedPosition: Int,
    private val onStyleSelected: (HomeStyleItem) -> Unit
) : RecyclerView.Adapter<HomeStyleAdapter.StyleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        val binding = ItemHomeStyleCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) =
        holder.bind(items[position], position == selectedPosition)

    override fun getItemCount(): Int = items.size

    inner class StyleViewHolder(private val binding: ItemHomeStyleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeStyleItem, isSelected: Boolean) {
            binding.tvStyleTitle.text = item.title
            binding.tvStyleSubtitle.text = item.subtitle
            binding.ivStyleIllustration.setImageResource(item.illustrationRes)

            // Handle selection visual state frames matching your Adhan component standard styles
            if (isSelected) {
                binding.layoutInnerContainer.background = ContextCompat.getDrawable(
                    binding.rootCardView.context,
                    R.drawable.bg_card_selected_border
                )
                binding.ivCheckIndicator.visibility = View.VISIBLE
            } else {
                binding.layoutInnerContainer.background = null
                binding.ivCheckIndicator.visibility = View.GONE
            }

            // ✨ SWAPPED: Replaced standard click listener with our dynamic scale-down bounce click micro-interaction
            binding.rootCardView.setBounceClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION && selectedPosition != currentPos) {
                    val prevPos = selectedPosition
                    selectedPosition = currentPos

                    notifyItemChanged(prevPos)
                    notifyItemChanged(selectedPosition)
                    onStyleSelected(item)
                }
            }
        }
    }
}