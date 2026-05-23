package com.example.nmaazreminder.ui.fragments.setting.method

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ItemCalculationMethodBinding
import com.example.nmaazreminder.utils.setBounceClickListener

class MethodAdapter(
    private val items: List<MethodItem>,
    private var selectedPosition: Int, // Pass currently active selection index here
    private val onMethodSelected: (MethodItem) -> Unit
) : RecyclerView.Adapter<MethodAdapter.MethodViewHolder>() {

    inner class MethodViewHolder(val binding: ItemCalculationMethodBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MethodViewHolder {
        val binding = ItemCalculationMethodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MethodViewHolder, position: Int) {
        val currentItem = items[position]

        holder.binding.apply {
            tvMethodTitle.text = currentItem.title
            tvMethodSubtitle.text = currentItem.subtitle

            if (position == selectedPosition) {
                ivRadioIndicator.setImageResource(R.drawable.ic_radio_selected)
            } else {
                ivRadioIndicator.setImageResource(R.drawable.ic_radio_unselected)
            }

            // Hide the lower item divider line for the last element automatically
            dividerLine.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE

            // Replaced standard click listener with our dynamic scale-down bounce click micro-interaction
            rootItemLayout.setBounceClickListener {
                if (selectedPosition != holder.adapterPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = holder.adapterPosition

                    // Update only changed index states to avoid layout stutter flashes
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    onMethodSelected(currentItem)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}