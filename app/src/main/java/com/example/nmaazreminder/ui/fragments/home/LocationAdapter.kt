package com.example.nmaazreminder.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.ItemLocationRowBinding
import com.example.nmaazreminder.ui.fragments.home.LocationItem

class LocationAdapter(
    private val onLocationClicked: (LocationItem) -> Unit
) : ListAdapter<LocationItem, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding, onLocationClicked)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocationViewHolder(
        private val binding: ItemLocationRowBinding,
        private val onLocationClicked: (LocationItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LocationItem) {
            binding.tvCityName.text = item.cityName
            binding.tvCountryName.text = item.countryName

            // Triggers a click callback passing the full data item back upward
            binding.root.setOnClickListener {
                onLocationClicked(item)
            }
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationItem>() {
        override fun areItemsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem.cityName == newItem.cityName && oldItem.countryName == newItem.countryName
        }

        override fun areContentsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem == newItem
        }
    }
}