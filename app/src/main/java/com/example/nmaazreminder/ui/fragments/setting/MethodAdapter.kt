package com.example.nmaazreminder.ui.fragments.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemCalculationMethodBinding

data class CalculationMethod(val id: Int, val title: String, val subtitle: String)

class MethodAdapter(
    private val items: List<CalculationMethod>,
    private var selectedPosition: Int, // Pass currently active selection index here
    private val onMethodSelected: (CalculationMethod) -> Unit
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

            // Sync selection visual status state
            radioButton.isChecked = (position == selectedPosition)

            // Hide the lower item divider line for the last element automatically
            dividerLine.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE

            // Click listener on entire row container block
            rootItemLayout.setOnClickListener {
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