package com.example.nmaazreminder.ui.settings

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemSettingsRowBinding
import com.example.nmaazreminder.ui.settings.SettingItem
import com.example.nmaazreminder.utils.setRoundedCorners
import com.example.nmaazreminder.utils.toPx

class SettingsAdapter(
    private val items: List<SettingItem>,
    private val onItemClicked: (SettingItem) -> Unit,
    private val onToggleChanged: (SettingItem, Boolean) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val binding = ItemSettingsRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class SettingsViewHolder(
        private val binding: ItemSettingsRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingItem) {
            // 1. Text Properties Setup
            binding.tvSettingTitle.text = item.title
            binding.tvSettingSubtitle.text = item.subtitle
            binding.ivSettingIcon.setImageResource(item.iconRes)

            // 2. Section Category Headers Handler
            if (item.categoryHeader != null) {
                binding.tvCategoryHeader.visibility = android.view.View.VISIBLE
                binding.tvCategoryHeader.text = item.categoryHeader
            } else {
                binding.tvCategoryHeader.visibility = android.view.View.GONE
            }

            // 3. Dynamic Action Elements Switcher (Chevron Arrow vs. SwitchCompat Toggle)
            if (item.isToggleable) {
                binding.ivChevron.visibility = android.view.View.GONE
                binding.switchSetting.visibility = android.view.View.VISIBLE

                // Clear state listener before forcing checkbox adjustments to prevent recursive triggers
                binding.switchSetting.setOnCheckedChangeListener(null)
                binding.switchSetting.isChecked = item.isChecked

                binding.switchSetting.setOnCheckedChangeListener { _, isChecked ->
                    item.isChecked = isChecked
                    onToggleChanged(item, isChecked)
                }
            } else {
                binding.switchSetting.visibility = android.view.View.GONE
                binding.ivChevron.visibility = android.view.View.VISIBLE
            }

            // Global Click Listener: Moved outside the if/else block so ALL rows are clickable
            binding.root.setOnClickListener {
                onItemClicked(item)
            }

            // 4. Group Card Divider Line Renderer
            if (item.isLastInGroup) {
                binding.settingDivider.visibility = android.view.View.GONE
            } else {
                binding.settingDivider.visibility = android.view.View.VISIBLE
            }

            // 5. THE MAGIC DYNAMIC CARD BACKGROUND ROUNDING LOGIC
            val radius = 16.toPx // Matches our 16dp corner specifications perfectly
            val whiteBg = Color.WHITE

            // Determine item positioning attributes inside its parent card structure
            val isFirstInGroup = item.categoryHeader != null || adapterPosition == 0

            when {
                // Scenario A: Single isolated standalone item in a group -> Round all 4 corners
                isFirstInGroup && item.isLastInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, radius, radius, radius, radius)
                }
                // Scenario B: Item resides at the top of a group block -> Round top corners only
                isFirstInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, topLeft = radius, topRight = radius)
                }
                // Scenario C: Item resides at the absolute bottom of a group block -> Round bottom corners only
                item.isLastInGroup -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, bottomLeft = radius, bottomRight = radius)
                }
                // Scenario D: Item is stacked right in the middle -> Keep backgrounds square
                else -> {
                    binding.cardRowContainer.setRoundedCorners(whiteBg, 0f, 0f, 0f, 0f)
                }
            }
        }
    }
}